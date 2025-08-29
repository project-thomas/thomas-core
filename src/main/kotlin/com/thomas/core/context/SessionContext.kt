package com.thomas.core.context

import com.thomas.core.model.security.SecurityUser
import java.util.Locale
import java.util.Locale.ROOT
import java.util.concurrent.ConcurrentHashMap

class SessionContext(
    sessionProperties: Map<String, String?> = emptyMap()
) {

    private val sessionProperties = ConcurrentHashMap(sessionProperties)
    private var _currentUser: SecurityUser? = null

    internal var currentToken: String? = null

    internal var currentLocale: Locale = ROOT

    internal var currentUser: SecurityUser
        get() = _currentUser ?: throw UnauthenticatedUserException()
        set(value) {
            _currentUser = value
        }

    internal fun getProperty(property: String): String? = sessionProperties[property]

    internal fun setProperty(property: String, value: String?) {
        if (value == null) {
            sessionProperties.remove(property)
        } else {
            sessionProperties[property] = value
        }
    }

    internal fun sessionProperties(): Map<String, String?> = sessionProperties.toMap()

    internal fun clear() {
        _currentUser = null
        currentToken = null
        currentLocale = ROOT
        sessionProperties.clear()
    }

}
