package com.thomas.core.model.entity

import com.thomas.core.model.entity.EntityValidationScope.Companion.EMPTY
import kotlin.reflect.KProperty1


data class DeferredEntityValidation<T : BaseEntity<T>>(
    val field: KProperty1<T, *>,
    val message: suspend (T) -> String,
    val validate: suspend (T) -> Boolean,
    val scope: EntityValidationScope = EMPTY
)
