package com.thomas.core.model.security

import com.thomas.core.i18n.BundleResolver
import com.thomas.core.model.security.SecurityRoleGroup.RoleStringsI18N.coreRolesGroupDescription
import com.thomas.core.model.security.SecurityRoleGroup.RoleStringsI18N.coreRolesGroupName

enum class SecurityRoleGroup(
    val groupOrder: Int
) {

    MASTER(0),
    ADMINISTRATION(1),
    MANAGEMENT(2);

    val groupName: String
        get() = coreRolesGroupName(this.name.lowercase())

    val groupDescription: String
        get() = coreRolesGroupDescription(this.name.lowercase())

    val categories: Set<SecurityRoleCategory>
        get() = SecurityRoleCategory.entries
            .filter { it.subgroupGroup == this }
            .sortedBy { it.subgroupOrder }
            .toSet()

    private object RoleStringsI18N : BundleResolver("strings/core-roles-groups") {

        fun coreRolesGroupName(
            group: String,
        ): String = coreRoleGroupsString(group, "name")

        fun coreRolesGroupDescription(
            group: String,
        ): String = coreRoleGroupsString(group, "description")

        private fun coreRoleGroupsString(
            group: String,
            attribute: String,
        ): String = formattedMessage("security.role-group.$group.$attribute")

    }

}
