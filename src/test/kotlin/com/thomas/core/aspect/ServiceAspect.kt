package com.thomas.core.aspect

import com.thomas.core.util.NumberUtils.randomBigDecimal
import com.thomas.core.util.NumberUtils.randomInteger
import com.thomas.core.util.StringUtils.randomString
import java.math.BigDecimal
import java.math.BigInteger

internal class MethodAspectService {

    @MethodLog(
        logParameters = true,
        logResult = true,
        logMetadata = true,
        logDuration = true,
        maskResult = false,
    )
    fun processData(
        username: String,
        @MaskField password: String,
        userData: String?,
        isActive: Boolean?,
        aspectData: AspectData?,
        totalAmount: BigInteger,
    ): String {
        return "Processed user: $username"
    }

    @MethodLog(
        logParameters = true,
        logResult = true,
        logMetadata = false,
        logDuration = true,
        maskResult = false,
    )
    fun withoutMetadata(
        username: String,
        @MaskField password: String,
        userData: String?,
        isActive: Boolean?,
    ): String {
        return "Processed with data: $username - $userData - $isActive"
    }

    @MethodLog(
        logParameters = false,
        logResult = true,
        logMetadata = true,
        logDuration = true,
        maskResult = false,
    )
    fun withoutParameters(
        value: Int,
        username: String,
        total: BigDecimal,
    ): AspectData {
        return AspectData(value, username, total)
    }

    @MethodLog(
        logParameters = true,
        logResult = true,
        logMetadata = true,
        logDuration = false,
        maskResult = false,
    )
    fun withoutDuration(
        value: Int,
        username: String,
        total: BigDecimal,
    ): AspectData {
        return AspectData(value, username, total)
    }

    @MethodLog(
        logParameters = true,
        logResult = true,
        logMetadata = true,
        logDuration = true,
        maskResult = true,
    )
    fun maskingResult(
        username: String,
        @MaskField password: String,
        userData: String?,
        isActive: Boolean?,
        aspectData: AspectData?,
        totalAmount: BigInteger,
        genericOne: GenericData<String, Int, AspectData>,
        genericTwo: GenericData<Boolean, BigInteger, GenericData<String, BigDecimal, String>>,
    ): GenericData<String, BigInteger, String> = GenericData(
        "Processed with data: $username - $userData - $isActive",
        totalAmount,
        "Generic Two: $genericTwo"
    )

    @MethodLog(
        logParameters = false,
        logResult = false,
        logMetadata = false,
        logDuration = false,
        maskResult = true,
    )
    fun logNothing(
        username: String,
        @MaskField password: String,
        userData: String?,
        isActive: Boolean?,
        aspectData: AspectData?,
        totalAmount: BigInteger,
    ): List<String> = listOf(
        "Processed with data: $username - $userData - $isActive",
        "Aspect data: $aspectData",
        "Total amount: $totalAmount"
    )

    @MethodLog(
        logParameters = true,
        logResult = true,
        logMetadata = true,
        logDuration = true,
        maskResult = false,
    )
    fun defaultValues(
        value: Int = randomInteger(),
        username: String = randomString(),
        total: BigDecimal = randomBigDecimal(),
    ): AspectData = AspectData(value, username, total)

    @MethodLog
    fun defaultAnnotation(
        username: String,
        userData: String?,
        isActive: Boolean?,
    ): String {
        return "Processed with data: $username - $userData - $isActive"
    }

    @MethodLog
    fun proceedException(
        username: String,
        userData: String?,
        isActive: Boolean?,
    ): Boolean = throw RuntimeException("Exception occurred")

    @MethodLog
    fun unitReturn(
        username: String,
        userData: String?,
        isActive: Boolean?,
    ) {
    }

    @MethodLog
    fun unitReturnException(
        username: String,
        userData: String?,
        isActive: Boolean?,
    ) {
        throw IllegalArgumentException("Exception occurred on unit call")
    }

    @MethodLog
    fun nullReturn(
        username: String,
        userData: String?,
        isActive: Boolean?,
    ): Integer? = null

    @MethodLog(
        logParameters = true,
        logResult = true,
        logMetadata = true,
        logDuration = true,
        maskResult = false,
    )
    fun logResultWithException(
        username: String,
        userData: String?,
        isActive: Boolean?,
    ): String = throw RuntimeException("Exception with logResult enabled")

    @MethodLog(
        logParameters = true,
        logResult = false,
        logMetadata = true,
        logDuration = true,
        maskResult = false,
    )
    fun logResultFalseWithException(
        username: String,
        userData: String?,
        isActive: Boolean?,
    ): String = throw RuntimeException("Exception with logResult enabled")

}

internal data class AspectData(val value: Int, val description: String?, val totalValue: BigDecimal)

internal data class GenericData<S, N : Number, T>(val name: S, val value: N, val typed: T)
