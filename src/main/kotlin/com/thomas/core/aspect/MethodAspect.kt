package com.thomas.core.aspect

import com.thomas.core.aspect.AspectSerializerContext.aspectSerializer
import com.thomas.core.context.SessionContextHolder.currentLocale
import com.thomas.core.context.SessionContextHolder.currentUser
import com.thomas.core.context.SessionContextHolder.sessionProperties
import com.thomas.core.context.UnauthenticatedUserException
import com.thomas.core.extension.simpleTypedName
import com.thomas.core.extension.toSecondsPattern
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import java.lang.System.lineSeparator
import java.lang.reflect.Parameter
import kotlin.time.TimedValue
import kotlin.time.measureTimedValue
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature

@Aspect
class MethodAspect {

    companion object {

        private val METHOD_LOG_MESSAGE = "${lineSeparator()}Method Logging"

        private val ONE_TAB_LINE = "${lineSeparator()}\t"

        private val TWO_TAB_LINE = "${lineSeparator()}\t\t"

        private const val EMPTY_STRING_VALUE = ""

    }

    @Around(value = "@annotation(com.thomas.core.aspect.MethodLog)")
    fun methodLogging(point: ProceedingJoinPoint): Any? {
        val logger = point.logger()
        val annotation = point.logAnnotation()
        val measured = point.measuredResult()
        logger.info { point.logMessage(annotation, measured) }
        return measured.returnValue()
    }

    private fun TimedValue<Any?>.returnValue(): Any? = this.value?.takeIf {
        it is Throwable
    }?.let {
        throw it as Throwable
    } ?: this.value

    private fun JoinPoint.logger(): KLogger = KotlinLogging.logger(this.sourceLocation.withinType.name)

    private fun JoinPoint.logAnnotation() = this.methodSignature().method.getAnnotation(MethodLog::class.java)

    private fun ProceedingJoinPoint.measuredResult() = measureTimedValue {
        try {
            this.proceed()
        } catch (e: Throwable) {
            e
        }
    }

    private fun JoinPoint.logMessage(
        annotation: MethodLog,
        measured: TimedValue<Any?>
    ): String = listOf(
        METHOD_LOG_MESSAGE,
        annotation.metadataLog(),
        this.methodLog(),
        this.parametersLog(annotation),
        this.resultLog(annotation, measured.value),
        annotation.durationLog(measured),
    ).joinToString(EMPTY_STRING_VALUE)

    private fun MethodLog.metadataLog(): String = if (this.logMetadata) {
        (listOf(
            "${ONE_TAB_LINE}Metadata -> ",
            "${TWO_TAB_LINE}UserId -> ${userId()}",
            "${TWO_TAB_LINE}Locale -> ${currentLocale.toLanguageTag()}",
        ) + sessionProperties().map {
            "${TWO_TAB_LINE}${it.key} -> ${it.value}"
        }).joinToString("")
    } else {
        EMPTY_STRING_VALUE
    }

    private fun JoinPoint.methodLog() = "${ONE_TAB_LINE}Call -> ${this.className()}.${this.methodName()}"

    private fun JoinPoint.parametersLog(methodLog: MethodLog): String = if (methodLog.logParameters) {
        val signature = this.methodSignature()
        this.args.mapIndexed { index, arg ->
            val argumentLog = aspectSerializer.serialize(arg, signature.method.parameters[index].maskValue())
            "${TWO_TAB_LINE}parameter[$index] -> ${signature.parameterNames[index]}: ${
                signature.parameterizedType(index).simpleTypedName()
            } = $argumentLog"
        }.joinToString("")
    } else {
        EMPTY_STRING_VALUE
    }

    private fun JoinPoint.resultLog(
        annotation: MethodLog,
        result: Any?,
    ): String = EMPTY_STRING_VALUE.takeIf {
        !annotation.logResult && result !is Throwable
    } ?: result.let {
        val signature = this.methodSignature()
        val prefix = "$TWO_TAB_LINE      return -> type: ${signature.method.genericReturnType.simpleTypedName()} = "
        "$prefix${aspectSerializer.serialize(it, annotation.maskResult)}"
    }

    private fun MethodLog.durationLog(
        duration: TimedValue<Any?>
    ): String = EMPTY_STRING_VALUE.takeIf {
        !this.logDuration
    } ?: "${ONE_TAB_LINE}Duration (second.nano) -> ${duration.duration.toSecondsPattern()}"

    private fun userId() = try {
        currentUser.userId.toString()
    } catch (_: UnauthenticatedUserException) {
        "anonymous"
    }

    private fun JoinPoint.className() = this.target::class.qualifiedName

    private fun JoinPoint.methodName() = this.signature.name

    private fun JoinPoint.methodSignature() = (this.signature as MethodSignature)

    private fun Parameter.maskValue() = this.isAnnotationPresent(MaskField::class.java)

    private fun MethodSignature.parameterizedType(index: Int) = this.method.parameters[index].parameterizedType

}
