package com.veosps.game.models.serialization

import com.veosps.game.cache.types.item.ItemTypeList
import com.veosps.game.config.models.GameConfig
import com.veosps.game.models.entities.player.privilege.PrivilegeMap
import com.veosps.game.models.item.container.ItemContainerKeyMap
import com.veosps.game.models.serialization.mappers.DefaultClientMapper
import com.veosps.game.models.serialization.serializers.YamlClientSerializer
import com.veosps.game.util.security.BCryptEncryption
import com.veosps.game.util.yamlMapper
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class SerializerProvider(
    private val gameConfig: GameConfig,
    private val privilegeMap: PrivilegeMap,
    private val itemTypes: ItemTypeList,
    private val containerKeys: ItemContainerKeyMap
) {

    @Bean
    fun clientSerializer(): ClientSerializer {
        return YamlClientSerializer(gameConfig, yamlMapper, DefaultClientMapper(
            config = gameConfig,
            encryption = BCryptEncryption(),
            privilegeMap = privilegeMap,
            itemTypes = itemTypes,
            containerKeys = containerKeys
        ))
    }
}