package com.thomas.core.model.entity

import com.thomas.core.extension.asyncSessionContext
import com.thomas.core.extension.asyncSessionContextIO
import com.thomas.core.extension.asyncSessionContextVT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred

fun interface EntityValidationScope : (CoroutineScope, suspend CoroutineScope.() -> Pair<String, String>?) -> Deferred<Pair<String, String>?> {

    companion object {

        val EMPTY: EntityValidationScope
            get() = EntityValidationScope { scope, block -> scope.asyncSessionContext(block = block) }

        val IO: EntityValidationScope
            get() = EntityValidationScope { scope, block -> scope.asyncSessionContextIO(block = block) }

        val VT: EntityValidationScope
            get() = EntityValidationScope { scope, block -> scope.asyncSessionContextVT(block = block) }

    }

}
