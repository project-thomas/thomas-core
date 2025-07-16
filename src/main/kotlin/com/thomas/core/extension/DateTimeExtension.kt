package com.thomas.core.extension

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
import java.time.format.DateTimeFormatter.ISO_LOCAL_TIME
import java.time.format.DateTimeFormatter.ISO_OFFSET_TIME

val ISO_OFFSET_DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
val ISO_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_DATE

fun LocalDate.toIsoDate(): String = ISO_DATE_FORMATTER.format(this)

fun LocalTime.toIsoTime(): String = ISO_LOCAL_TIME.format(this)

fun LocalDateTime.toIsoDateTime(): String = ISO_LOCAL_DATE_TIME.format(this)

fun OffsetTime.toIsoOffsetTime(): String = ISO_OFFSET_TIME.format(this)

fun OffsetDateTime.toIsoOffsetDateTime(): String = ISO_OFFSET_DATE_TIME_FORMATTER.format(this)
