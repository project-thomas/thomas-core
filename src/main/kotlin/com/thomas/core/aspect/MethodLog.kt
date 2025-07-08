package com.thomas.core.aspect

import com.thomas.core.aspect.MethodLogLevel.INFO
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FUNCTION

@Target(FUNCTION)
@Retention(RUNTIME)
annotation class MethodLog(
    val logLevel: MethodLogLevel = INFO,
    val logParameters: Boolean = true,
    val logResult: Boolean = false,
    val maskResult: Boolean = false,
    val logUser: Boolean = false,
)
