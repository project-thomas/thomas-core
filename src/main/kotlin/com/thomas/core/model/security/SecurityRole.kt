package com.thomas.core.model.security

import com.thomas.core.i18n.BundleResolver
import com.thomas.core.model.security.SecurityRole.RoleStringsI18N.coreRolesDescription
import com.thomas.core.model.security.SecurityRole.RoleStringsI18N.coreRolesName
import com.thomas.core.model.security.SecurityRoleCategory.ADMINISTRATION_CATEGORY
import com.thomas.core.model.security.SecurityRoleCategory.MANAGEMENT_GROUP
import com.thomas.core.model.security.SecurityRoleCategory.MANAGEMENT_USER
import com.thomas.core.model.security.SecurityRoleCategory.MASTER_CATEGORY

enum class SecurityRole(
    val roleCode: Int,
    val roleOrder: Int,
    val roleCategory: SecurityRoleCategory,
    val roleDisplayable: Boolean,
) {

    MASTER_USER(0, 1, MASTER_CATEGORY, false),

    ADMINISTRATOR_USER(1, 1, ADMINISTRATION_CATEGORY, true),

    USER_READ(2, 1, MANAGEMENT_USER, true),
    USER_CREATE(3, 2, MANAGEMENT_USER, true),
    USER_UPDATE(4, 3, MANAGEMENT_USER, true),

    GROUP_READ(5, 1, MANAGEMENT_GROUP, true),
    GROUP_CREATE(6, 2, MANAGEMENT_GROUP, true),
    GROUP_UPDATE(7, 3, MANAGEMENT_GROUP, true),
    GROUP_DELETE(8, 4, MANAGEMENT_GROUP, true);

    companion object {

        private val ROLES_MAP: Map<Int, SecurityRole> = entries.associateBy { it.roleCode }

        fun byCode(code: Int): SecurityRole? = ROLES_MAP[code]

    }

    val roleName: String
        get() = coreRolesName(this.name.lowercase())

    val roleDescription: String
        get() = coreRolesDescription(this.name.lowercase())

    object RoleStringsI18N : BundleResolver("strings/core-roles") {

        fun coreRolesName(
            role: String
        ): String = coreRolesString(role, "name")

        fun coreRolesDescription(
            role: String
        ): String = coreRolesString(role, "description")

        private fun coreRolesString(
            role: String,
            attribute: String
        ): String = formattedMessage("security.role.$role.$attribute")

    }

}
