package com.thomas.core.model.security

import com.thomas.core.model.security.SecurityOrganizationRoleSubgroup.MANAGEMENT_GROUP
import com.thomas.core.model.security.SecurityOrganizationRoleSubgroup.MANAGEMENT_UNIT
import com.thomas.core.model.security.SecurityOrganizationRoleSubgroup.MANAGEMENT_USER
import com.thomas.core.model.security.SecurityOrganizationRoleSubgroup.MASTER_SUBGROUP
import com.thomas.core.model.security.SecurityOrganizationRoleSubgroup.ORGANIZATION_SUBGROUP

enum class SecurityOrganizationRole(
    override val roleCode: Int,
    override val roleOrder: Int,
    override val roleSubgroup: SecurityOrganizationRoleSubgroup,
    override val roleDisplayable: Boolean,
) : SecurityRole<SecurityOrganizationRole, SecurityOrganizationRoleSubgroup, SecurityOrganizationRoleGroup> {

    MASTER_ROLE(0, 1, MASTER_SUBGROUP, false),

    ORGANIZATION_ALL(1, 1, ORGANIZATION_SUBGROUP, true),

    USER_READ(2, 1, MANAGEMENT_USER, true),
    USER_CREATE(3, 2, MANAGEMENT_USER, true),
    USER_UPDATE(4, 3, MANAGEMENT_USER, true),

    GROUP_READ(5, 1, MANAGEMENT_GROUP, true),
    GROUP_CREATE(6, 2, MANAGEMENT_GROUP, true),
    GROUP_UPDATE(7, 3, MANAGEMENT_GROUP, true),
    GROUP_DELETE(8, 4, MANAGEMENT_GROUP, true),

    UNIT_READ(9, 1, MANAGEMENT_UNIT, true),
    UNIT_CREATE(10, 2, MANAGEMENT_UNIT, true),
    UNIT_UPDATE(11, 3, MANAGEMENT_UNIT, true),
    UNIT_DELETE(12, 4, MANAGEMENT_UNIT, true);

    companion object {
        fun byCode(code: Int): SecurityOrganizationRole? = entries.firstOrNull { it.roleCode == code }
    }

}
