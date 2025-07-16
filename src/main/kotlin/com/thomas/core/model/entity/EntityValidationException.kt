package com.thomas.core.model.entity

import com.thomas.core.exception.ApplicationException
import com.thomas.core.exception.ErrorType.INVALID_ENTITY

data class EntityValidationException(
    override val message: String,
    val errors: Map<String, List<String>>
) : ApplicationException(
    message = message,
    type = INVALID_ENTITY,
    detail = errors
)
