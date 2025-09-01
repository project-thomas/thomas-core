package com.thomas.core.context

import com.thomas.core.model.security.SecurityUser
import java.util.Locale
import java.util.Locale.ROOT
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

class SessionContext private constructor(
    initialProperties: Map<String, String> = emptyMap(),
    initialUser: SecurityUser? = null,
    initialToken: String? = null,
    initialLocale: Locale = ROOT
) {

    private val _sessionProperties: ConcurrentHashMap<String, String> = ConcurrentHashMap(initialProperties)
    private val _currentUser: AtomicReference<SecurityUser?> = AtomicReference(initialUser)
    private val _currentToken: AtomicReference<String?> = AtomicReference(initialToken)
    private val _currentLocale: AtomicReference<Locale> = AtomicReference(initialLocale)

    internal val currentUser: SecurityUser
        get() = _currentUser.get() ?: throw UnauthenticatedUserException()

    internal val currentToken: String?
        get() = _currentToken.get()

    internal val currentLocale: Locale
        get() = _currentLocale.get()

    internal fun getProperty(property: String): String? = _sessionProperties[property]

    internal fun setProperty(property: String, value: String?): SessionContext {
        return if (value == null) {
            withProperties(_sessionProperties.toMutableMap().apply { remove(property) })
        } else {
            withProperties(_sessionProperties.toMutableMap().apply { put(property, value) })
        }
    }

    internal fun withUser(
        user: SecurityUser?
    ): SessionContext = this.copy(user = user)

    internal fun withToken(
        token: String?
    ): SessionContext = this.copy(token = token)

    internal fun withLocale(
        locale: Locale
    ): SessionContext = this.copy(locale = locale)

    internal fun withProperties(
        properties: Map<String, String>
    ): SessionContext = this.copy(properties = properties)

    internal fun sessionProperties(): Map<String, String> = _sessionProperties.toMap()

    internal fun copy(
        properties: Map<String, String> = _sessionProperties.toMap(),
        user: SecurityUser? = _currentUser.get(),
        token: String? = _currentToken.get(),
        locale: Locale = _currentLocale.get()
    ): SessionContext = SessionContext(
        initialProperties = properties,
        initialUser = user,
        initialToken = token,
        initialLocale = locale
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SessionContext

        if (_sessionProperties != other._sessionProperties) return false
        if (_currentUser.get() != other._currentUser.get()) return false
        if (_currentToken.get() != other._currentToken.get()) return false
        if (_currentLocale.get() != other._currentLocale.get()) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _sessionProperties.hashCode()
        result = 31 * result + (_currentUser.get()?.hashCode() ?: 0)
        result = 31 * result + (_currentToken.get()?.hashCode() ?: 0)
        result = 31 * result + _currentLocale.get().hashCode()
        return result
    }

    override fun toString(): String = "SessionContext(_currentLocale=$_currentLocale, " +
        "_currentToken=$_currentToken, " +
        "_currentUser=$_currentUser, " +
        "sessionProperties=$_sessionProperties)"


    companion object {
        fun empty(): SessionContext = SessionContext()

        fun create(
            properties: Map<String, String> = emptyMap(),
            user: SecurityUser? = null,
            token: String? = null,
            locale: Locale = ROOT
        ): SessionContext = SessionContext(properties, user, token, locale)
    }

}
