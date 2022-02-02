package com.veosps.game.models.serialization.mappers

import com.veosps.game.cache.types.item.ItemTypeList
import com.veosps.game.config.models.GameConfig
import com.veosps.game.config.models.GameEnvironment
import com.veosps.game.models.map.Coordinates
import com.veosps.game.models.Client
import com.veosps.game.models.entities.PlayerEntity
import com.veosps.game.models.entities.mob.Player
import com.veosps.game.models.entities.mob.PlayerId
import com.veosps.game.models.entities.player.privilege.Privilege
import com.veosps.game.models.entities.player.privilege.PrivilegeMap
import com.veosps.game.models.entities.player.stat.Stat
import com.veosps.game.models.entities.player.stat.StatKey
import com.veosps.game.models.entities.player.stat.StatMap
import com.veosps.game.models.item.Item
import com.veosps.game.models.item.container.ItemContainer
import com.veosps.game.models.item.container.ItemContainerKeyMap
import com.veosps.game.models.item.container.ItemContainerMap
import com.veosps.game.models.serialization.ClientData
import com.veosps.game.models.serialization.ClientDataMapper
import com.veosps.game.models.serialization.ClientDeserializeRequest
import com.veosps.game.models.serialization.ClientDeserializeResponse
import com.veosps.game.models.vars.VarpMap
import com.veosps.game.models.world.move.MovementSpeed
import com.veosps.game.util.security.PasswordEncryption

class DefaultClientMapper(
    private val config: GameConfig,
    private val encryption: PasswordEncryption,
    private val privilegeMap: PrivilegeMap,
    private val itemTypes: ItemTypeList,
    private val containerKeys: ItemContainerKeyMap
) : ClientDataMapper<DefaultClientData> {

    override val type = DefaultClientData::class

    /**
     * Check if client passwords should not be verified on [deserialize].
     */
    private fun skipPasswordCheck(): Boolean {
        return config.env == GameEnvironment.Development
    }

    override fun deserialize(request: ClientDeserializeRequest, data: DefaultClientData): ClientDeserializeResponse {
        val password = request.plaintTextPass
        if (password == null) {
            val reconnectXteas = request.reconnectXteas
            if (reconnectXteas == null || !reconnectXteas.contentEquals(data.loginXteas)) {
                return ClientDeserializeResponse.BadCredentials
            }
        } else if (!skipPasswordCheck() && !encryption.verify(password, data.encryptedPass)) {
            return ClientDeserializeResponse.BadCredentials
        }
        val privileges = data.privileges.toPrivilegeList()
        val entity = PlayerEntity(
            username = data.displayName,
            privilege = privileges.firstOrNull()?.clientId ?: 0
        )
        val player = Player(
            id = PlayerId(data.loginName),
            loginName = data.loginName,
            eventBus = request.eventBus,
            actionBus = request.actionBus,
            entity = entity,
            privileges = privileges.toMutableList(),
            messageListeners = listOf(request.messageListener)
        )
        val client = Client(
            player = player,
            device = request.device,
            machine = request.machine,
            settings = request.settings,
            encryptedPass = data.encryptedPass,
            loginXteas = request.loginXteas,
            bufAllocator = request.bufAllocator
        )
        entity.coordinates = when (data.coords.size) {
            2 -> Coordinates(data.coords[0], data.coords[1])
            3 -> Coordinates(data.coords[0], data.coords[1], data.coords[2])
            else -> error("Invalid coordinate values: ${data.coords}.")
        }
        player.speed = if (data.moveSpeed == 1) MovementSpeed.Run else MovementSpeed.Walk
        player.runEnergy = data.runEnergy
        player.stats.putAll(data.skills)
        player.varpMap.putAll(data.varps)
        player.containers.putAll(data.containers, containerKeys, itemTypes)
        return ClientDeserializeResponse.Success(client)
    }

    override fun serialize(client: Client): DefaultClientData {
        val player = client.player
        val entity = player.entity
        return DefaultClientData(
            loginName = player.loginName,
            displayName = player.username,
            encryptedPass = client.encryptedPass,
            loginXteas = client.loginXteas,
            coords = intArrayOf(entity.coordinates.x, entity.coordinates.y, entity.coordinates.level),
            privileges = player.privileges.map { it.nameId },
            moveSpeed = if (player.speed == MovementSpeed.Run) 1 else 0,
            runEnergy = player.runEnergy,
            skills = player.stats.toIntKeyMap(),
            varps = player.varpMap.toMap(),
            containers = player.containers.toContainerMap()
        )
    }

    override fun newClient(request: ClientDeserializeRequest): Client {
        val password = request.plaintTextPass ?: error("New client must have an input password.")
        val entity = PlayerEntity(
            username = request.loginName,
            privilege = 0
        )
        val player = Player(
            id = PlayerId(request.loginName),
            loginName = request.loginName,
            eventBus = request.eventBus,
            actionBus = request.actionBus,
            entity = entity,
            messageListeners = listOf(request.messageListener)
        )
        val encryptedPass = encryption.encrypt(password)
        entity.coordinates = config.spawnPosition.unwrap()
        return Client(
            player = player,
            device = request.device,
            machine = request.machine,
            settings = request.settings,
            encryptedPass = encryptedPass,
            loginXteas = request.loginXteas,
            bufAllocator = request.bufAllocator
        )
    }

    private fun List<String>.toPrivilegeList(): List<Privilege> {
        if (isEmpty()) return emptyList()
        return mapNotNull { privilegeMap[it] }
    }
}

data class DefaultClientData(
    val loginName: String,
    val displayName: String,
    val encryptedPass: String,
    val loginXteas: IntArray,
    val coords: IntArray,
    val privileges: List<String>,
    val runEnergy: Double,
    val moveSpeed: Int,
    val skills: Map<Int, Stat>,
    val containers: Map<String, List<ContainerItem>>,
    val varps: Map<Int, Int>
) : ClientData {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DefaultClientData

        if (loginName != other.loginName) return false
        if (displayName != other.displayName) return false
        if (encryptedPass != other.encryptedPass) return false
        if (!loginXteas.contentEquals(other.loginXteas)) return false
        if (!coords.contentEquals(other.coords)) return false
        if (privileges != other.privileges) return false
        if (runEnergy != other.runEnergy) return false
        if (moveSpeed != other.moveSpeed) return false
        if (skills != other.skills) return false
        if (varps != other.varps) return false
        if (containers != other.containers) return false

        return true
    }

    override fun hashCode(): Int {
        var result = loginName.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + encryptedPass.hashCode()
        result = 31 * result + loginXteas.contentHashCode()
        result = 31 * result + coords.contentHashCode()
        result = 31 * result + privileges.hashCode()
        result = 31 * result + runEnergy.hashCode()
        result = 31 * result + moveSpeed
        result = 31 * result + skills.hashCode()
        result = 31 * result + varps.hashCode()
        result = 31 * result + containers.hashCode()
        return result
    }
}

data class ContainerItem(
    val slot: Int,
    val id: Int,
    val amount: Int
)

private fun StatMap.toIntKeyMap(): Map<Int, Stat> {
    return entries.associate { (key, value) -> key.id to value }
}

private fun StatMap.putAll(intKeyMap: Map<Int, Stat>) {
    val map = intKeyMap.entries.associate { (key, value) -> StatKey(key) to value }
    putAll(map)
}

private fun VarpMap.putAll(from: Map<Int, Int>) {
    from.forEach { (key, value) -> this[key] = value }
}

private fun ItemContainerMap.toContainerMap(): Map<String, List<ContainerItem>> = entries.associate { entry ->
    val key = entry.key
    val container = entry.value
    key.name to container.toItemMap()
}

private fun ItemContainerMap.putAll(
    from: Map<String, List<ContainerItem>>,
    keys: ItemContainerKeyMap,
    itemTypes: ItemTypeList
) {
    from.forEach { (keyName, items) ->
        val key = keys[keyName] ?: error("Container key \"$keyName\" does not exist.")
        val container = ItemContainer(key.capacity, key.stack)
        items.forEach { item ->
            val type = itemTypes[item.id]
            container[item.slot] = Item(type, item.amount)
        }
        this[key] = container
    }
}

private fun ItemContainer.toItemMap(): List<ContainerItem> {
    val items = mutableListOf<ContainerItem>()
    forEachIndexed { index, item ->
        if (item != null) {
            val containerItem = ContainerItem(index, item.id, item.amount)
            items.add(containerItem)
        }
    }
    return items
}
