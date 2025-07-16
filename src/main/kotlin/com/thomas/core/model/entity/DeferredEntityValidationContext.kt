package com.thomas.core.model.entity

import com.thomas.core.extension.asyncSessionContext
import com.thomas.core.extension.asyncSessionContextIO
import com.thomas.core.extension.asyncSessionContextVT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred

class DeferredEntityValidationContext private constructor(
    val defer: CoroutineScope.(suspend () -> Unit) -> Deferred<Unit>
) {

    companion object {
        val EMPTY = DeferredEntityValidationContext { asyncSessionContext { it() } }
        val IO = DeferredEntityValidationContext { asyncSessionContextIO { it() } }
        val VT = DeferredEntityValidationContext { asyncSessionContextVT { it() } }
    }

}
