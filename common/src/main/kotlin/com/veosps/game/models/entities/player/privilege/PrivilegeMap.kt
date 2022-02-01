package com.veosps.game.models.entities.player.privilege

import com.veosps.game.name.NamedTypeMap
import com.veosps.game.util.BeanScope
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(BeanScope.SCOPE_SINGLETON)
class PrivilegeMap : NamedTypeMap<Privilege>() {

    fun register(privilege: Privilege) {
        this[privilege.nameId] = privilege
    }
}
