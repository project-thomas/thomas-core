package com.thomas.core.aspect

import ch.qos.logback.classic.Level
import io.github.oshai.kotlinlogging.Level as KLevel

enum class MethodLogLevel(
    val level: Level,
    val klevel: KLevel,
) {

    ERROR(Level.ERROR, KLevel.ERROR),
    WARN(Level.WARN, KLevel.WARN),
    INFO(Level.INFO, KLevel.INFO),
    DEBUG(Level.DEBUG, KLevel.DEBUG),
    TRACE(Level.TRACE, KLevel.TRACE),

}