package com.thomas.core.aspect

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FUNCTION

@Target(FUNCTION)
@Retention(RUNTIME)
annotation class MethodLog(
    val logParameters: Boolean = true,
    val logResult: Boolean = true,
    val logMetadata: Boolean = true,
    val logDuration: Boolean = true,
    val maskResult: Boolean = false,
)
