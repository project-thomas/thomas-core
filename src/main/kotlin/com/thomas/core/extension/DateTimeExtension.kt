package com.thomas.core.extension

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
import java.time.format.DateTimeFormatter.ISO_LOCAL_TIME
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.time.format.DateTimeFormatter.ISO_OFFSET_TIME
import kotlin.time.toKotlinDuration
import kotlin.time.Duration as KDuration

val ISO_OFFSET_DATE_TIME_FORMATTER: DateTimeFormatter = ISO_OFFSET_DATE_TIME
val ISO_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_DATE

fun LocalDate.toIsoDate(): String = ISO_DATE_FORMATTER.format(this)

fun LocalTime.toIsoTime(): String = ISO_LOCAL_TIME.format(this)

fun LocalDateTime.toIsoDateTime(): String = ISO_LOCAL_DATE_TIME.format(this)

fun OffsetTime.toIsoOffsetTime(): String = ISO_OFFSET_TIME.format(this)

fun OffsetDateTime.toIsoOffsetDateTime(): String = ISO_OFFSET_DATE_TIME_FORMATTER.format(this)

fun ZonedDateTime.toIsoZonedDateTime(): String = ISO_OFFSET_DATE_TIME.format(this)

fun KDuration.toHoursPattern(): String = this.let {
    val totalSeconds: Long = it.inWholeNanoseconds / 1_000_000_000
    val hours: Long = totalSeconds / 3600
    val minutes: Long = (totalSeconds % 3600) / 60
    val seconds: Long = totalSeconds % 60
    val nanos: Long = it.inWholeNanoseconds % 1_000_000_000

    String.format("%02d:%02d:%02d.%09d", hours, minutes, seconds, nanos)
}

fun KDuration.toMinutesPattern(): String = this.let {
    val totalSeconds: Long = it.inWholeNanoseconds / 1_000_000_000
    val minutes: Long = totalSeconds / 60
    val seconds: Long = totalSeconds % 60
    val nanos: Long = it.inWholeNanoseconds % 1_000_000_000

    String.format("%03d:%02d.%09d", minutes, seconds, nanos)
}

fun KDuration.toSecondsPattern(): String = this.let {
    val totalSeconds: Long = it.inWholeNanoseconds / 1_000_000_000
    val nanos: Long = it.inWholeNanoseconds % 1_000_000_000
    String.format("%04d.%09d", totalSeconds, nanos)
}

fun Duration.toHoursPattern(): String = this.toKotlinDuration().toHoursPattern()

fun Duration.toMinutesPattern(): String = this.toKotlinDuration().toMinutesPattern()

fun Duration.toSecondsPattern(): String = this.toKotlinDuration().toSecondsPattern()
