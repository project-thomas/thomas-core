package com.thomas.core.model.security

import com.thomas.core.i18n.BundleResolver
import com.thomas.core.model.security.SecurityRole.RoleStringsI18N.coreRolesDescription
import com.thomas.core.model.security.SecurityRole.RoleStringsI18N.coreRolesName

interface SecurityRole<R, S, G> where
G : SecurityRoleGroup<R, S, G>,
S : SecurityRoleSubgroup<R, S, G>,
R : SecurityRole<R, S, G>,
R : Enum<R>,
S : Enum<S>,
G : Enum<G> {

    val name: String

    val roleCode: Int

    val roleOrder: Int

    val roleSubgroup: S

    val roleDisplayable: Boolean

    val roleName: String
        get() = coreRolesName(
            this.roleSubgroup.subgroupGroup.groupCategory.name.lowercase(),
            this.name.lowercase(),
        )

    val roleDescription: String
        get() = coreRolesDescription(
            this.roleSubgroup.subgroupGroup.groupCategory.name.lowercase(),
            this.name.lowercase(),
        )

    object RoleStringsI18N : BundleResolver("strings/core-roles") {

        fun coreRolesName(
            category: String,
            role: String
        ): String = coreRolesString(category, role, "name")

        fun coreRolesDescription(
            category: String,
            role: String
        ): String = coreRolesString(category, role, "description")

        private fun coreRolesString(
            category: String,
            role: String,
            attribute: String
        ): String = formattedMessage("security.role.$category.$role.$attribute")

    }

}
