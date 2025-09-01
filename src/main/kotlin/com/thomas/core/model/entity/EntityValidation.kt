package com.thomas.core.model.entity

import kotlin.reflect.KProperty1

data class EntityValidation<T : BaseEntity<*>>(
    val field: KProperty1<T, *>,
    val message: (T) -> String,
    val validate: (T) -> Boolean
)
