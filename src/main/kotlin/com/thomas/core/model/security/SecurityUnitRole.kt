package com.thomas.core.model.security

import com.thomas.core.model.security.SecurityUnitRoleSubgroup.ACCOUNTING_COA
import com.thomas.core.model.security.SecurityUnitRoleSubgroup.UNIT_SUBGROUP

enum class SecurityUnitRole(
    override val roleCode: Int,
    override val roleOrder: Int,
    override val roleSubgroup: SecurityUnitRoleSubgroup,
    override val roleDisplayable: Boolean,
) : SecurityRole<SecurityUnitRole, SecurityUnitRoleSubgroup, SecurityUnitRoleGroup> {

    UNIT_ALL(0, 0, UNIT_SUBGROUP, true),

    COA_READ(1, 1, ACCOUNTING_COA, true),
    COA_CREATE(2, 2, ACCOUNTING_COA, true),
    COA_UPDATE(3, 3, ACCOUNTING_COA, true),
    COA_DELETE(4, 4, ACCOUNTING_COA, true);

    companion object {
        fun byCode(code: Int): SecurityUnitRole? =
            entries.firstOrNull { it.roleCode == code }
    }

}
