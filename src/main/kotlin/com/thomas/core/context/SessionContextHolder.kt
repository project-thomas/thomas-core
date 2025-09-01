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
        get() = context.currentUser ?: throw UnauthenticatedUserException()
        set(value) {
            updateContextAtomically { it.withUser(value) }
        }

    var currentToken: String?
        get() = context.currentToken
        set(value) {
            updateContextAtomically { it.withToken(value) }
        }

    var currentLocale: Locale
        get() = context.currentLocale
        set(value) {
            updateContextAtomically { it.withLocale(value) }
        }

    fun updateContext(updater: (SessionContext) -> SessionContext) {
        updateContextAtomically(updater)
    }

    private fun updateContextAtomically(updater: (SessionContext) -> SessionContext) {
        val currentContext = contextHolder.get()
        val newContext = updater(currentContext)
        contextHolder.set(newContext)
    }

    fun getSessionProperty(property: String): String? = context.getProperty(property)

    fun setSessionProperty(property: String, value: String?) {
        updateContextAtomically { it.setProperty(property, value) }
    }

    fun sessionProperties(): Map<String, String> = context.sessionProperties

    fun clearContext() {
        contextHolder.set(SessionContext.empty())
    }

    fun withContext(sessionContext: SessionContext): AutoCloseable {
        val previousContext = context
        context = sessionContext
        return AutoCloseable { context = previousContext }
    }

}
