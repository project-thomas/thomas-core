package com.thomas.core.model.security

import kotlin.reflect.KClass

enum class SecurityOrganizationRoleGroup(
    override val groupOrder: Int
) : SecurityRoleGroup<SecurityOrganizationRole, SecurityOrganizationRoleSubgroup, SecurityOrganizationRoleGroup> {

    MASTER(0),
    ORGANIZATION(1),
    MANAGEMENT(2);

    override val kclass: KClass<SecurityOrganizationRoleSubgroup> = SecurityOrganizationRoleSubgroup::class

    override val groupCategory: SecurityRoleGroupCategory = SecurityRoleGroupCategory.ORGANIZATION

}
