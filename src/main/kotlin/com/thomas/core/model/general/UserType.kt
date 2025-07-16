package com.thomas.core.model.general

import com.thomas.core.i18n.BundleResolver
import com.thomas.core.model.general.UserType.UserTypeStringsI18N.coreUserTypeDescription
import com.thomas.core.model.general.UserType.UserTypeStringsI18N.coreUserTypeName

enum class UserType {

    MASTER,
    ADMINISTRATOR,
    COMMON;

    val typeName: String
        get() = coreUserTypeName(this.name.lowercase())

    val typeDescription: String
        get() = coreUserTypeDescription(this.name.lowercase())

    object UserTypeStringsI18N : BundleResolver("strings/core-user-types") {

        fun coreUserTypeName(
            name: String
        ): String = coreUserTypesString(name, "name")

        fun coreUserTypeDescription(
            name: String
        ): String = coreUserTypesString(name, "description")

        private fun coreUserTypesString(
            name: String,
            attribute: String
        ): String = formattedMessage("model.user-type.$name.$attribute")

    }

}