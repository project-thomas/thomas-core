package com.thomas.core.context

import com.thomas.core.model.security.SecurityUser
import java.util.Locale
import java.util.Locale.ROOT
import java.util.UUID

data class SessionContext(
    private val sessionProperties: MutableMap<String, String?> = mutableMapOf(),
) {

    private var _currentUser: SecurityUser? = null

    internal var currentToken: String? = null

    internal var currentLocale: Locale = ROOT

    internal var currentUnit: UUID? = null

    internal var currentUser: SecurityUser
        get() = _currentUser ?: throw UnauthenticatedUserException()
        set(value) {
            _currentUser = value
        }
    internal val currentOrganization: UUID
        get() = (_currentUser?: throw UnresolvedOrganizationException()).securityOrganization.organizationId

    internal fun getProperty(property: String): String? = sessionProperties[property]

    internal fun setProperty(property: String, value: String?) {
        sessionProperties[property] = value
    }

    internal fun clear() {
        _currentUser = null
        currentLocale = ROOT
        currentUnit = null
        sessionProperties.clear()
    }

}
