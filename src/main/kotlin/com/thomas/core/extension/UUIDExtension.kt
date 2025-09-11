package com.thomas.core.extension

import java.lang.System.currentTimeMillis
import java.security.SecureRandom
import java.util.UUID

fun randomUUIDv7(): UUID = randomBytes(currentTimeMillis()).let {
    UUID(it.toLong(0), it.toLong(8))
}

private fun randomBytes(millis: Long): ByteArray = ByteArray(16).apply {
    SecureRandom().nextBytes(this)
}.also { array ->
    (0..5).forEach { array[it] = ((millis shr (40 - (it * 8))) and 0xFF).toByte() }
    array[6] = ((array[6].toInt() and 0x0F) or 0x70).toByte()
    array[8] = ((array[8].toInt() and 0x3F) or 0x80).toByte()
}

private fun ByteArray.toLong(
    offset: Int
): Long = (0..7).map {
    this.longValueShift(offset, it)
}.reduce { a, l -> a or l }

private fun ByteArray.longValueShift(
    offset: Int,
    index: Int,
): Long = this.longValueBitwise(offset, index) shl (56 - (index * 8))

private fun ByteArray.longValueBitwise(
    offset: Int,
    index: Int,
): Long = this[offset + index].toLong() and 0xFF
