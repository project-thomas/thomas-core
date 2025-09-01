package com.thomas.core.extension

import com.thomas.core.model.entity.BaseEntity
import com.thomas.core.model.entity.DeferredEntityValidation
import com.thomas.core.model.entity.EntityValidationException
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll

suspend fun <T : BaseEntity<T>> List<DeferredEntityValidation<T>>.validate(
    entity: T,
    errorMessage: String,
) = withCurrentSessionContext {
    val errors = ConcurrentHashMap<String, MutableList<String>>()

    this@validate.map { this.defer(entity, it, errors) }.awaitAll()

    errors.takeIf {
        it.isNotEmpty()
    }?.throws {
        EntityValidationException(errorMessage, it)
    }
}

private fun <T : BaseEntity<T>> CoroutineScope.defer(
    entity: T,
    validation: DeferredEntityValidation<T>,
    errors: ConcurrentHashMap<String, MutableList<String>>
): Deferred<*> = validation.scope(this) {
    validation.takeIf {
        !it.validate(entity)
    }?.let {
        errors.getOrPut(validation.field.name.toSnakeCase()) {
            mutableListOf()
        }.add(validation.message(entity))
    }
}
