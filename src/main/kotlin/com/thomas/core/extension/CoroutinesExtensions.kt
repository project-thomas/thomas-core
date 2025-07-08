package com.thomas.core.extension

import com.thomas.core.context.CoroutineSessionContext
import com.thomas.core.context.SessionContextHolder
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

val Dispatchers.VT
    get() = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()

suspend fun <T> withSessionContext(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> T
): T = withContext(context = (context + CoroutineSessionContext(context = SessionContextHolder.context)), block)

suspend fun <T> withSessionContextIO(
    block: suspend CoroutineScope.() -> T
): T = withSessionContext(Dispatchers.IO, block)

suspend fun <T> withSessionContextVT(
    block: suspend CoroutineScope.() -> T
): T = withSessionContext(Dispatchers.VT, block)

fun <T> CoroutineScope.asyncSessionContext(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T
): Deferred<T> = async(context = (context + CoroutineSessionContext(context = SessionContextHolder.context)), start, block)

fun <T> CoroutineScope.asyncSessionContextIO(
    block: suspend CoroutineScope.() -> T
): Deferred<T> = asyncSessionContext(context = Dispatchers.IO, block = block)

fun <T> CoroutineScope.asyncSessionContextVT(
    block: suspend CoroutineScope.() -> T
): Deferred<T> = asyncSessionContext(context = Dispatchers.VT, block = block)
