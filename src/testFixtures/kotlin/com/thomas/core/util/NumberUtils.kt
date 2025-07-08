package com.thomas.core.util

import java.math.BigDecimal
import java.math.BigInteger
import kotlin.random.Random

object NumberUtils {

    fun randomInteger(
        start: Int = 0,
        end: Int = 9999,
    ): Int = Random.nextInt(start, end)

    fun randomLong(
        start: Long = 0,
        end: Long = 9999999,
    ): Long = Random.nextLong(start, end)

    fun randomDouble(
        start: Double = 0.0,
        end: Double = 9999.9999,
    ): Double = Random.nextDouble(start, end)

    fun randomBigInteger(
        start: Long = 0,
        end: Long = 99999999,
    ): BigInteger = Random.nextLong(start, end).let { BigInteger.valueOf(it) }

    fun randomBigDecimal(
        start: Double = 0.0,
        end: Double = 9999.9999,
    ): BigDecimal = Random.nextDouble(start, end).let { BigDecimal.valueOf(it) }

}
