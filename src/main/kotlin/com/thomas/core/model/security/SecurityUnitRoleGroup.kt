package com.thomas.core.model.security

import com.thomas.core.model.security.SecurityRoleGroupCategory.UNIT
import kotlin.reflect.KClass

enum class SecurityUnitRoleGroup(
    override val groupOrder: Int
) : SecurityRoleGroup<SecurityUnitRole, SecurityUnitRoleSubgroup, SecurityUnitRoleGroup> {

    UNITS(0),
    ACCOUNTING(1);

    override val kclass: KClass<SecurityUnitRoleSubgroup> = SecurityUnitRoleSubgroup::class

    override val groupCategory: SecurityRoleGroupCategory = UNIT

}
