package com.veosps.game.config

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.veosps.game.config.models.GameConfig
import com.veosps.game.config.models.RsaConfig
import com.veosps.game.util.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.io.pem.PemReader
import java.nio.file.Files
import java.security.KeyFactory
import java.security.Security
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec

@Component
class ConfigProvider {

    private val mapper = ObjectMapper(YAMLFactory())
        .findAndRegisterModules()
        .registerKotlinModule()
        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        .enable(SerializationFeature.INDENT_OUTPUT)

    @Bean
    @Scope(BeanScope.SCOPE_SINGLETON)
    fun loadGameConfig(): GameConfig {
        return mapper.readValue("./config.yml".toFile())
    }

    @Bean
    @Scope(BeanScope.SCOPE_SINGLETON)
    fun loadRsaConfig(serverConfig: GameConfig): RsaConfig {
        val keyPath = serverConfig.rsaPath
        if (Files.notExists(keyPath)) error("RSA key file must be generated and stored in: ${keyPath.toAbsolutePath()}")

        PemReader(Files.newBufferedReader(keyPath)).use { reader ->
            val pem = reader.readPemObject()
            val keySpec = PKCS8EncodedKeySpec(pem.content)

            Security.addProvider(BouncyCastleProvider())
            val factory = KeyFactory.getInstance("RSA", "BC")

            val privateKey = factory.generatePrivate(keySpec) as RSAPrivateKey
            val exponent = privateKey.privateExponent
            val modulus = privateKey.modulus

            return RsaConfig(exponent, modulus)
        }
    }
}