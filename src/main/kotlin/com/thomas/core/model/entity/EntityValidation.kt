package com.thomas.core.model.entity

data class EntityValidation<T : BaseEntity<*>>(
    val field: String,
    val message: (T) -> String,
    val validate: (T) -> Boolean
)
