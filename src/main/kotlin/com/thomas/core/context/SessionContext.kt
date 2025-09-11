package com.thomas.core.context

import com.thomas.core.model.security.SecurityUser
import java.util.Locale
import java.util.Locale.ROOT

class SessionContext private constructor(
    val currentUser: SecurityUser?,
    val currentToken: String?,
    val currentLocale: Locale,
    val sessionProperties: Map<String, String>,
) {

    companion object {

        fun empty(): SessionContext = SessionContext(
            sessionProperties = emptyMap(),
            currentUser = null,
            currentToken = null,
            currentLocale = ROOT
        )

        fun create(
            properties: Map<String, String> = emptyMap(),
            user: SecurityUser? = null,
            token: String? = null,
            locale: Locale = ROOT
        ): SessionContext = SessionContext(
            sessionProperties = properties,
            currentUser = user,
            currentToken = token,
            currentLocale = locale
        )
    }

    internal fun getProperty(property: String): String? = sessionProperties[property]

    internal fun setProperty(property: String, value: String?): SessionContext {
        val newProperties = sessionProperties.toMutableMap()
        if (value == null) {
            newProperties.remove(property)
        } else {
            newProperties[property] = value
        }
        return copy(properties = newProperties)
    }

    internal fun withUser(
        user: SecurityUser?
    ): SessionContext = copy(user = user)

    internal fun withToken(
        token: String?
    ): SessionContext = copy(token = token)

    internal fun withLocale(
        locale: Locale
    ): SessionContext = copy(locale = locale)

    internal fun withProperties(
        properties: Map<String, String>
    ): SessionContext = copy(properties = properties)

    internal fun copy(
        user: SecurityUser? = this.currentUser,
        token: String? = this.currentToken,
        locale: Locale = this.currentLocale,
        properties: Map<String, String> = this.sessionProperties,
    ): SessionContext = SessionContext(
        currentUser = user,
        currentToken = token,
        currentLocale = locale,
        sessionProperties = properties,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SessionContext

        if (currentUser != other.currentUser) return false
        if (currentToken != other.currentToken) return false
        if (currentLocale != other.currentLocale) return false
        if (sessionProperties != other.sessionProperties) return false

        return true
    }

    override fun hashCode(): Int {
        var result = currentUser?.hashCode() ?: 0
        result = 31 * result + (currentToken?.hashCode() ?: 0)
        result = 31 * result + currentLocale.hashCode()
        result = 31 * result + sessionProperties.hashCode()
        return result
    }

    override fun toString(): String {
        return "SessionContext(" +
            "currentUser=$currentUser, " +
            "currentToken=$currentToken, " +
            "currentLocale=$currentLocale, " +
            "sessionProperties=$sessionProperties)"
    }

}
