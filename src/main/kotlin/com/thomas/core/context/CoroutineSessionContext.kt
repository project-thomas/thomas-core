package com.thomas.core.context

import com.thomas.core.context.SessionContextHolder.clearContext
import com.thomas.core.context.SessionContextHolder.context
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.ThreadContextElement

class CoroutineSessionContext private constructor(
    private val sessionContext: SessionContext
) : ThreadContextElement<SessionContext?> {

    companion object Key : CoroutineContext.Key<CoroutineSessionContext> {

        fun create(context: SessionContext): CoroutineSessionContext {
            return CoroutineSessionContext(context)
        }

        fun current(): CoroutineSessionContext {
            return CoroutineSessionContext(context)
        }
    }

    override val key: CoroutineContext.Key<CoroutineSessionContext>
        get() = Key

    override fun updateThreadContext(context: CoroutineContext): SessionContext? {
        val previousContext = try {
            SessionContextHolder.context
        } catch (e: Exception) {
            null
        }

        SessionContextHolder.context = sessionContext
        return previousContext
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: SessionContext?) {
        if (oldState != null) {
            SessionContextHolder.context = oldState
        } else {
            clearContext()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CoroutineSessionContext) return false
        return sessionContext == other.sessionContext
    }

    override fun hashCode(): Int {
        return sessionContext.hashCode()
    }

    override fun toString(): String {
        return "CoroutineSessionContext(sessionContext=$sessionContext)"
    }

}
