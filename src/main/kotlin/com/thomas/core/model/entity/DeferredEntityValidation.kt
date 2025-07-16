package com.thomas.core.model.entity

import com.thomas.core.model.entity.DeferredEntityValidationContext.Companion.EMPTY
import kotlin.reflect.KProperty1


data class DeferredEntityValidation<T : BaseEntity<*>>(
    val field: KProperty1<*, *>,
    val message: suspend (T) -> String,
    val validate: suspend (T) -> Boolean,
    val context: DeferredEntityValidationContext = EMPTY,
)