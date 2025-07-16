package com.thomas.core.model.security

import com.thomas.core.i18n.BundleResolver
import com.thomas.core.model.security.SecurityRoleSubgroup.RoleStringsI18N.coreRolesSubgroupDescription
import com.thomas.core.model.security.SecurityRoleSubgroup.RoleStringsI18N.coreRolesSubgroupName
import kotlin.reflect.KClass

interface SecurityRoleSubgroup<R, S, G> where
G : SecurityRoleGroup<R, S, G>,
S : SecurityRoleSubgroup<R, S, G>,
R : SecurityRole<R, S, G>,
R : Enum<R>,
S : Enum<S>,
G : Enum<G> {

    val kclass: KClass<R>
    val name: String
    val subgroupGroup: G
    val subgroupOrder: Int

    val subgroupName: String
        get() = coreRolesSubgroupName(
            this.subgroupGroup.groupCategory.name.lowercase(),
            this.name.lowercase(),
        )

    val subgroupDescription: String
        get() = coreRolesSubgroupDescription(
            this.subgroupGroup.groupCategory.name.lowercase(),
            this.name.lowercase(),
        )

    val roles: Set<R>
        get() = kclass.java.enumConstants
            .filter { (it.roleSubgroup as S) == (this as S) }
            .sortedBy { it.roleOrder }
            .toSet()

    object RoleStringsI18N : BundleResolver("strings/core-roles-subgroups") {

        fun coreRolesSubgroupName(
            category: String,
            subgroup: String,
        ): String = coreRoleSubgroupsString(category, subgroup, "name")

        fun coreRolesSubgroupDescription(
            category: String,
            subgroup: String,
        ): String = coreRoleSubgroupsString(category, subgroup, "description")

        private fun coreRoleSubgroupsString(
            category: String,
            subgroup: String,
            attribute: String,
        ): String = formattedMessage("security.role-subgroup.$category.$subgroup.$attribute")

    }

}
