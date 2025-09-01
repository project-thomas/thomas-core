package com.thomas.core.context

import com.thomas.core.model.security.SecurityUser
import java.util.Locale

object SessionContextHolder {

    private val contextHolder = ThreadLocal.withInitial { SessionContext.empty() }

    var context: SessionContext
        get() = contextHolder.get()
        set(value) {
            contextHolder.set(value)
        }

    var currentUser: SecurityUser
        get() = context.currentUser
        set(value) {
            updateContext { it.withUser(value) }
        }

    var currentToken: String?
        get() = context.currentToken
        set(value) {
            updateContext { it.withToken(value) }
        }

    var currentLocale: Locale
        get() = context.currentLocale
        set(value) {
            updateContext { it.withLocale(value) }
        }

    fun updateContext(updater: (SessionContext) -> SessionContext) {
        contextHolder.set(updater(context))
    }

    fun getSessionProperty(property: String): String? = context.getProperty(property)

    fun setSessionProperty(property: String, value: String?) {
        updateContext { it.setProperty(property, value) }
    }

    fun sessionProperties(): Map<String, String> = context.sessionProperties()

    fun clearContext() {
        contextHolder.set(SessionContext.empty())
    }

}
