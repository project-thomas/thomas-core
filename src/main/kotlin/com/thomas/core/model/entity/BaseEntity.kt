package com.thomas.core.model.entity

import com.thomas.core.extension.throws
import com.thomas.core.i18n.CoreMessageI18N.validationEntityValidationInvalidErrorMessage
import java.util.UUID

abstract class BaseEntity<T : BaseEntity<T>> {

    abstract val id: UUID

    @Suppress("UNCHECKED_CAST")
    fun validate() = validations().validate(this as T, errorMessage())

    open fun errorMessage(): String = validationEntityValidationInvalidErrorMessage()

    open fun validations(): List<EntityValidation<T>> = listOf()

    private fun <T : BaseEntity<T>> List<EntityValidation<T>>.validate(
        entity: T,
        errorMessage: String
    ) = this.filter {
        !it.validate(entity)
    }.takeIf {
        it.isNotEmpty()
    }?.groupBy(
        { it.field },
        { it.message(entity) }
    )?.throws {
        EntityValidationException(errorMessage, it)
    }

}
