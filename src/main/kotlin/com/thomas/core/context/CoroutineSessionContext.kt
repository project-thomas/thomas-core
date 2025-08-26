package com.thomas.core.context

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.ThreadContextElement

data class CoroutineSessionContext(
    private val context: SessionContext
) : ThreadContextElement<SessionContext> {

    companion object Key : CoroutineContext.Key<CoroutineSessionContext>

    override val key: CoroutineContext.Key<CoroutineSessionContext>
        get() = Key

    override fun updateThreadContext(context: CoroutineContext): SessionContext {
        val oldContext = SessionContextHolder.context
        SessionContextHolder.context = this.context
        return oldContext
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: SessionContext) {
        SessionContextHolder.context = oldState
    }

}
