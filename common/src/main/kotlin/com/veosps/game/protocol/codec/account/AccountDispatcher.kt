package com.veosps.game.protocol.codec.account

import com.github.michaelbull.logging.InlineLogger
import com.github.michaelbull.retry.RetryFailure
import com.github.michaelbull.retry.RetryInstruction
import com.github.michaelbull.retry.policy.binaryExponentialBackoff
import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.policy.plus
import com.github.michaelbull.retry.retry
import com.veosps.game.action.ActionBus
import com.veosps.game.config.models.RsaConfig
import com.veosps.game.coroutines.ioCoroutineScope
import com.veosps.game.dispatch.GameJobDispatcher
import com.veosps.game.event.EventBus
import com.veosps.game.event.impl.AccountCreation
import com.veosps.game.event.impl.ClientRegister
import com.veosps.game.event.impl.ClientUnregister
import com.veosps.game.models.Client
import com.veosps.game.models.ClientList
import com.veosps.game.models.entities.mob.PlayerList
import com.veosps.game.models.repository.XteaRepository
import com.veosps.game.protocol.codec.login.LoginRequest
import com.veosps.game.util.BeanScope
import kotlinx.coroutines.launch
import com.veosps.game.models.map.MapIsolation
import com.veosps.game.models.map.Viewport
import com.veosps.game.models.map.viewport
import com.veosps.game.models.entities.mob.Player
import com.veosps.game.models.map.of
import com.veosps.game.models.serialization.ClientDeserializeRequest
import com.veosps.game.models.serialization.ClientDeserializeResponse
import com.veosps.game.models.serialization.ClientSerializer
import com.veosps.game.protocol.Device
import com.veosps.game.protocol.codec.HandshakeConstants
import com.veosps.game.protocol.codec.ResponseType
import com.veosps.game.protocol.codec.game.ChannelMessageListener
import com.veosps.game.protocol.codec.game.GameSessionDecoder
import com.veosps.game.protocol.codec.game.GameSessionEncoder
import com.veosps.game.protocol.codec.game.GameSessionHandler
import com.veosps.game.protocol.codec.login.LoginResponse
import com.veosps.game.protocol.packet.server.InitialPlayerInfo
import com.veosps.game.protocol.packet.server.RebuildNormal
import com.veosps.game.protocol.structure.DevicePacketStructureMap
import com.veosps.game.util.security.IsaacRandom
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelPipeline
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentLinkedQueue

private val logger = InlineLogger()

private const val SERIALIZE_ATTEMPTS = 5
private const val BACKOFF_BASE = 100L
private const val BACKOFF_MAX = 10000L

@Component
class AccountDispatcherProvider(
    private val rsaConfig: RsaConfig,
    private val clientList: ClientList,
    private val playerList: PlayerList,
    private val deviceStructures: DevicePacketStructureMap,
    private val gameJobDispatcher: GameJobDispatcher,
    private val xteaRepository: XteaRepository,
    private val clientSerializer: ClientSerializer,
    private val mapIsolation: MapIsolation,
    private val eventBus: EventBus,
    private val actionBus: ActionBus
) {

    @Bean
    @Scope(BeanScope.SCOPE_SINGLETON)
    fun accountDispatcher(): AccountDispatcher {
        return AccountDispatcher(
            rsaConfig = rsaConfig,
            playerList = playerList,
            clientList = clientList,
            gameJobDispatcher = gameJobDispatcher,
            mapIsolation = mapIsolation,
            xteas = xteaRepository,
            deviceStructures = deviceStructures,
            eventBus = eventBus,
            actionBus = actionBus,
            serializer = clientSerializer
        )
    }
}

class AccountDispatcher(
    private val rsaConfig: RsaConfig,
    private val playerList: PlayerList,
    private val clientList: ClientList,
    private val xteas: XteaRepository,
    private val serializer: ClientSerializer,
    private val deviceStructures: DevicePacketStructureMap,
    private val gameJobDispatcher: GameJobDispatcher,
    private val mapIsolation: MapIsolation,
    private val eventBus: EventBus,
    private val actionBus: ActionBus
) {
    private val gameTickDelay = 600
    private val actionsPerCycle = 25
    private val loginsPerCycle = 25
    private val logoutsPerCycle = 10

    private val registerQueue = ConcurrentLinkedQueue<Account>()
    private val unregisterQueue = ConcurrentLinkedQueue<Client>()

    fun start() {
        gameJobDispatcher.schedule(::gameCycle)
        logger.debug { "Ready to dispatch incoming login requests" }
    }

    private fun gameCycle() {
        for (i in 0 until loginsPerCycle) {
            val account = registerQueue.poll() ?: break
            logger.debug { "Register account to game (account=$account)" }
            login(account)
        }

        for (i in 0 until logoutsPerCycle) {
            val client = unregisterQueue.poll() ?: break
            logger.debug { "Unregister player from game (player=${client.player})" }
            logout(client)
        }
    }

    fun register(request: LoginRequest) {
        ioCoroutineScope.launch {
            val account = deserialize(request) ?: return@launch
            registerQueue.add(account)
        }
    }

    fun unregister(client: Client) {
        ioCoroutineScope.launch {
            retry(serializePolicy()) {
                serializer.serialize(client)
                unregisterQueue.add(client)
            }
        }
    }

    private fun deserialize(request: LoginRequest): Account? {
        val channel = request.channel
        val xteas = request.xteas
        val clientRequest = ClientDeserializeRequest(
            loginName = request.username,
            device = request.device,
            plaintTextPass = request.password,
            loginXteas = request.xteas,
            reconnectXteas = request.reconnectXteas,
            settings = request.settings,
            machine = request.machine,
            eventBus = eventBus,
            actionBus = actionBus,
            messageListener = ChannelMessageListener(channel),
            bufAllocator = channel.alloc()
        )
        val deserialize = serializer.deserialize(clientRequest)
        logger.debug { "Deserialized login request (request=$request, response=$deserialize)" }
        when (deserialize) {
            is ClientDeserializeResponse.BadCredentials -> {
                channel.writeAndFlush(ResponseType.INVALID_CREDENTIALS)
                    .addListener(ChannelFutureListener.CLOSE)
                return null
            }
            is ClientDeserializeResponse.ReadError -> {
                channel.writeAndFlush(ResponseType.COULD_NOT_COMPLETE_LOGIN)
                    .addListener(ChannelFutureListener.CLOSE)
                return null
            }
            is ClientDeserializeResponse.Success -> {
                val client = deserialize.client
                val decodeIsaac = if (rsaConfig.enabled) IsaacRandom() else IsaacRandom.ZERO
                val encodeIsaac = if (rsaConfig.enabled) IsaacRandom() else IsaacRandom.ZERO
                if (rsaConfig.enabled) {
                    decodeIsaac.init(xteas)
                    encodeIsaac.init(IntArray(xteas.size) { xteas[it] + 50 })
                }
                return Account(
                    channel = channel,
                    client = client,
                    device = request.device,
                    decodeIsaac = decodeIsaac,
                    encodeIsaac = encodeIsaac,
                    newAccount = deserialize.newAccount
                )
            }
        }
    }

    private fun login(account: Account) {
        val (channel, client, device, decodeIsaac, encodeIsaac, newAccount) = account
        val online = playerList.any { it?.id?.value == client.player.id.value }
        if (online) {
            channel.writeAndFlush(ResponseType.ACCOUNT_ONLINE).addListener(ChannelFutureListener.CLOSE)
            return
        }
        val registered = playerList.register(client.player)
        if (!registered) {
            channel.writeAndFlush(ResponseType.WORLD_FULL).addListener(ChannelFutureListener.CLOSE)
            return
        }
        clientList.register(client)
        if (newAccount) {
            eventBus.publish(AccountCreation(client))
        }
        eventBus.publish(ClientRegister(client))
        client.register(channel, device, decodeIsaac, encodeIsaac)
        channel.flush()
    }

    private fun logout(client: Client) {
        clientList.remove(client)
        playerList.remove(client.player)
        eventBus.publish(ClientUnregister(client))
    }

    private fun Client.register(
        channel: Channel,
        device: Device,
        decodeIsaac: IsaacRandom,
        encodeIsaac: IsaacRandom
    ) {
        val gpi = player.gpi()
        val reconnect = false
        if (!channel.isActive) return
        writeResponse(channel, encodeIsaac, reconnect, gpi)
        channel.pipeline().addGameCodec(
            this,
            device,
            decodeIsaac,
            encodeIsaac
        )
        player.login(reconnect, gpi)
    }

    private fun Client.writeResponse(
        channel: Channel,
        encodeIsaac: IsaacRandom,
        reconnect: Boolean,
        gpi: InitialPlayerInfo
    ) {
        val response = if (reconnect) {
            LoginResponse.Reconnect(gpi)
        } else {
            LoginResponse.Normal(
                playerIndex = player.index,
                privilege = player.entity.privilege,
                moderator = true,
                rememberDevice = false,
                encodeIsaac = encodeIsaac,
                members = true
            )
        }
        channel.writeAndFlush(response)
            .addListener { channel.pipeline().removePreviousCodec() }
    }

    private fun ChannelPipeline.addGameCodec(
        client: Client,
        device: Device,
        decodeIsaac: IsaacRandom,
        encodeIsaac: IsaacRandom
    ) {
        val structures = device.packetStructures()
        val decoder = GameSessionDecoder(decodeIsaac, structures.client)
        val encoder = GameSessionEncoder(encodeIsaac, structures.server)
        val handler = GameSessionHandler(client, this@AccountDispatcher)

        addLast("gameDecoder", decoder)
        addLast("gameEncoder", encoder)
        addLast("gameHandler", handler)
    }

    private fun ChannelPipeline.removePreviousCodec() {
        remove(HandshakeConstants.RESPONSE_PIPELINE)
        remove(HandshakeConstants.DECODER_PIPELINE)
        remove(HandshakeConstants.ENCODER_PIPELINE)
        remove(HandshakeConstants.ADAPTER_PIPELINE)
    }

    private fun Player.login(reconnect: Boolean, gpi: InitialPlayerInfo) {
        val newViewport = coordinates.zone().viewport(mapIsolation)
        if (!reconnect) {
            val rebuildNormal = RebuildNormal(
                gpi = gpi,
                playerZone = coordinates.zone(),
                viewport = newViewport,
                xteas = xteas
            )
            write(rebuildNormal)
            flush()
        }
        viewport = Viewport.of(coordinates, newViewport)
        login()
    }

    private fun PlayerList.playerCoords(excludeIndex: Int): IntArray {
        var index = 0
        val coordinates = IntArray(capacity - 1)
        for (i in indices) {
            if (i == excludeIndex) {
                continue
            }
            val player = this[i]
            val coords = player?.coordinates?.packed18Bits ?: 0
            coordinates[index++] = coords
        }
        return coordinates
    }

    private fun Player.gpi() = InitialPlayerInfo(
        playerCoordsAs30Bits = coordinates.packed30Bits,
        otherPlayerCoords = playerList.playerCoords(index)
    )

    private fun Device.packetStructures() = deviceStructures.getCodec(this)

    private fun serializePolicy(): suspend RetryFailure<Throwable>.() -> RetryInstruction {
        return limitAttempts(SERIALIZE_ATTEMPTS) + binaryExponentialBackoff(BACKOFF_BASE, BACKOFF_MAX)
    }
}