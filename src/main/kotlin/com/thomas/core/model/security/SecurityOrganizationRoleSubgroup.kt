package com.thomas.core.model.security

import com.thomas.core.model.security.SecurityOrganizationRoleGroup.MANAGEMENT
import com.thomas.core.model.security.SecurityOrganizationRoleGroup.MASTER
import com.thomas.core.model.security.SecurityOrganizationRoleGroup.ORGANIZATION
import kotlin.reflect.KClass

enum class SecurityOrganizationRoleSubgroup(
    override val subgroupGroup: SecurityOrganizationRoleGroup,
    override val subgroupOrder: Int
) : SecurityRoleSubgroup<SecurityOrganizationRole, SecurityOrganizationRoleSubgroup, SecurityOrganizationRoleGroup> {

    MASTER_SUBGROUP(MASTER, 0),
    ORGANIZATION_SUBGROUP(ORGANIZATION, 1),
    MANAGEMENT_USER(MANAGEMENT, 2),
    MANAGEMENT_GROUP(MANAGEMENT, 3),
    MANAGEMENT_UNIT(MANAGEMENT, 4);

    override val kclass: KClass<SecurityOrganizationRole> = SecurityOrganizationRole::class

}
