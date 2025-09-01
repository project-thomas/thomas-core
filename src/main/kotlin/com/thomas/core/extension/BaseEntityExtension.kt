package com.thomas.core.extension

import com.thomas.core.model.entity.BaseEntity
import com.thomas.core.model.entity.DeferredEntityValidation
import com.thomas.core.model.entity.EntityValidationException
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.awaitAll

suspend fun <T : BaseEntity<T>> List<DeferredEntityValidation<T>>.validate(
    entity: T,
    errorMessage: String,
) = withCurrentSessionContext {
    if (isEmpty()) return@withCurrentSessionContext

    val errorMap = ConcurrentHashMap<String, MutableList<String>>(size)

    map { validation ->
        validation.scope(this) { validation.result(entity) }
    }.awaitAll().filterNotNull().forEach { (field, message) ->
        errorMap.addError(field, message)
    }

    errorMap.throwsOnError(errorMessage)
}

private suspend fun <T : BaseEntity<T>> DeferredEntityValidation<T>.result(
    entity: T,
): Pair<String, String>? = takeIf {
    !validate(entity)
}?.let { validation ->
    validation.field.name.toSnakeCase() to validation.message(entity)
}

fun ConcurrentHashMap<String, MutableList<String>>.addError(
    field: String,
    message: String,
) = computeIfAbsent(field) { mutableListOf() }.add(message)

fun ConcurrentHashMap<String, MutableList<String>>.throwsOnError(
    message: String
) = takeIf { it.isNotEmpty() }?.throws {
    EntityValidationException(message, this)
}
