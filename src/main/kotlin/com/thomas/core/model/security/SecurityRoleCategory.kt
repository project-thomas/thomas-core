package com.thomas.core.model.security

import com.thomas.core.i18n.BundleResolver
import com.thomas.core.model.security.SecurityRoleCategory.RoleStringsI18N.coreRolesCategoryDescription
import com.thomas.core.model.security.SecurityRoleCategory.RoleStringsI18N.coreRolesCategoryName
import com.thomas.core.model.security.SecurityRoleGroup.ADMINISTRATION
import com.thomas.core.model.security.SecurityRoleGroup.MANAGEMENT
import com.thomas.core.model.security.SecurityRoleGroup.MASTER

enum class SecurityRoleCategory(
    val categoryGroup: SecurityRoleGroup,
    val categoryOrder: Int
) {

    MASTER_CATEGORY(MASTER, 0),
    ADMINISTRATION_CATEGORY(ADMINISTRATION, 1),
    MANAGEMENT_USER(MANAGEMENT, 2),
    MANAGEMENT_GROUP(MANAGEMENT, 3);

    val categoryName: String
        get() = coreRolesCategoryName(this.name.lowercase())

    val categoryDescription: String
        get() = coreRolesCategoryDescription(this.name.lowercase())

    val roles: Set<SecurityRole>
        get() = SecurityRole.entries
            .filter { it.roleCategory == this }
            .sortedBy { it.roleOrder }
            .toSet()

    object RoleStringsI18N : BundleResolver("strings/core-roles-categories") {

        fun coreRolesCategoryName(
            subgroup: String,
        ): String = coreRoleCategoryString(subgroup, "name")

        fun coreRolesCategoryDescription(
            subgroup: String,
        ): String = coreRoleCategoryString(subgroup, "description")

        private fun coreRoleCategoryString(
            subgroup: String,
            attribute: String,
        ): String = formattedMessage("security.role-category.$subgroup.$attribute")

    }

}
