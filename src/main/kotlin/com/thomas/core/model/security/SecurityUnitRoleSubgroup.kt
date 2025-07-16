package com.thomas.core.model.security

import com.thomas.core.model.security.SecurityUnitRoleGroup.ACCOUNTING
import kotlin.reflect.KClass

enum class SecurityUnitRoleSubgroup(
    override val subgroupGroup: SecurityUnitRoleGroup,
    override val subgroupOrder: Int
) : SecurityRoleSubgroup<SecurityUnitRole, SecurityUnitRoleSubgroup, SecurityUnitRoleGroup> {

    UNIT_SUBGROUP(ACCOUNTING, 0),
    ACCOUNTING_COA(ACCOUNTING, 1);

    override val kclass: KClass<SecurityUnitRole> = SecurityUnitRole::class

}
