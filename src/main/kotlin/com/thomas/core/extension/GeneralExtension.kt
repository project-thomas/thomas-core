package com.thomas.core.extension

import io.github.oshai.kotlinlogging.KotlinLogging

fun <T, R : Throwable> T.throws(block: (T) -> R): Nothing = throw block(this)

inline fun <reified T> T.logger() = KotlinLogging.logger(T::class.java.name)