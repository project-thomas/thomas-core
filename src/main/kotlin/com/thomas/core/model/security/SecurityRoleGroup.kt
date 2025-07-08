package com.thomas.core.model.security

import com.thomas.core.i18n.BundleResolver
import com.thomas.core.model.security.SecurityRoleGroup.RoleStringsI18N.coreRolesGroupDescription
import com.thomas.core.model.security.SecurityRoleGroup.RoleStringsI18N.coreRolesGroupName
import kotlin.reflect.KClass

interface SecurityRoleGroup<R, S, G> where
G : SecurityRoleGroup<R, S, G>,
S : SecurityRoleSubgroup<R, S, G>,
R : SecurityRole<R, S, G>,
R : Enum<R>,
S : Enum<S>,
G : Enum<G> {

    val kclass: KClass<S>
    val name: String
    val groupOrder: Int
    val groupCategory: SecurityRoleGroupCategory

    val groupName: String
        get() = coreRolesGroupName(
            this.groupCategory.name.lowercase(),
            this.name.lowercase(),
        )

    val groupDescription: String
        get() = coreRolesGroupDescription(
            this.groupCategory.name.lowercase(),
            this.name.lowercase(),
        )

    val subgroups: Set<S>
        get() = kclass.java.enumConstants
            .filter { (it.subgroupGroup as G) == (this as G) }
            .sortedBy { it.subgroupOrder }
            .toSet()

    private object RoleStringsI18N : BundleResolver("strings/core-roles-groups") {

        fun coreRolesGroupName(
            category: String,
            group: String,
        ): String = coreRoleGroupsString(category, group, "name")

        fun coreRolesGroupDescription(
            category: String,
            group: String,
        ): String = coreRoleGroupsString(category, group, "description")

        private fun coreRoleGroupsString(
            category: String,
            group: String,
            attribute: String,
        ): String = formattedMessage("security.role-group.$category.$group.$attribute")

    }

}
