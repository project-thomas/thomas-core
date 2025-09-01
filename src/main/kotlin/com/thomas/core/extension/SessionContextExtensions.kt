package com.thomas.core.extension

import com.thomas.core.context.CoroutineSessionContext
import com.thomas.core.context.SessionContext
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
    context: SessionContext,
    block: suspend CoroutineScope.() -> T
): T = withContext(CoroutineSessionContext.create(context), block)

suspend fun <T> withCurrentSessionContext(
    block: suspend CoroutineScope.() -> T
): T = withContext(CoroutineSessionContext.current(), block)

fun CoroutineContext.withSessionContext(
    context: SessionContext
): CoroutineContext = this + CoroutineSessionContext.create(context)

fun CoroutineContext.withCurrentSessionContext(): CoroutineContext = this + CoroutineSessionContext.current()

suspend fun <T> withSessionContextIO(
    context: SessionContext,
    block: suspend CoroutineScope.() -> T
): T = withContext(Dispatchers.IO + CoroutineSessionContext.create(context), block)

suspend fun <T> withCurrentSessionContextIO(
    block: suspend CoroutineScope.() -> T
): T = withContext(Dispatchers.IO + CoroutineSessionContext.current(), block)

suspend fun <T> withSessionContextVT(
    context: SessionContext,
    block: suspend CoroutineScope.() -> T
): T = withContext(Dispatchers.VT + CoroutineSessionContext.create(context), block)

suspend fun <T> withCurrentSessionContextVT(
    block: suspend CoroutineScope.() -> T
): T = withContext(Dispatchers.VT + CoroutineSessionContext.current(), block)

fun <T> CoroutineScope.asyncSessionContext(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T
): Deferred<T> = async(
    context = context + CoroutineSessionContext.current(),
    start = start,
    block = block
)

fun <T> CoroutineScope.asyncSessionContext(
    sessionContext: SessionContext,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T
): Deferred<T> = async(
    context = context + CoroutineSessionContext.create(sessionContext),
    start = start,
    block = block
)

fun <T> CoroutineScope.asyncSessionContextIO(
    block: suspend CoroutineScope.() -> T
): Deferred<T> = asyncSessionContext(
    context = Dispatchers.IO,
    block = block
)

fun <T> CoroutineScope.asyncSessionContextIO(
    sessionContext: SessionContext,
    block: suspend CoroutineScope.() -> T
): Deferred<T> = asyncSessionContext(
    sessionContext = sessionContext,
    context = Dispatchers.IO,
    block = block
)

fun <T> CoroutineScope.asyncSessionContextVT(
    block: suspend CoroutineScope.() -> T
): Deferred<T> = asyncSessionContext(
    context = Dispatchers.VT,
    block = block
)

fun <T> CoroutineScope.asyncSessionContextVT(
    sessionContext: SessionContext,
    block: suspend CoroutineScope.() -> T
): Deferred<T> = asyncSessionContext(
    sessionContext = sessionContext,
    context = Dispatchers.VT,
    block = block
)
