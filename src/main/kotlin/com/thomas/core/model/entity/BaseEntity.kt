package com.thomas.core.model.entity

import com.thomas.core.extension.addError
import com.thomas.core.extension.throwsOnError
import com.thomas.core.extension.toSnakeCase
import com.thomas.core.i18n.CoreMessageI18N.validationEntityValidationInvalidErrorMessage
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

abstract class BaseEntity<T : BaseEntity<T>> {

    abstract val id: UUID

    @Suppress("UNCHECKED_CAST")
    fun validate() = validations().validate(this as T, errorMessage())

    open fun errorMessage(): String = validationEntityValidationInvalidErrorMessage()

    open fun validations(): List<EntityValidation<T>> = listOf()

    private fun <T : BaseEntity<T>> List<EntityValidation<T>>.validate(
        entity: T,
        errorMessage: String
    ) {
        if (isEmpty()) return

        val errors = ConcurrentHashMap<String, MutableList<String>>(size)
        asSequence().filter {
            !it.validate(entity)
        }.forEach { validation ->
            val fieldName = validation.field.name.toSnakeCase()
            errors.addError(fieldName, validation.message(entity))
        }

        errors.throwsOnError(errorMessage)
    }

}
