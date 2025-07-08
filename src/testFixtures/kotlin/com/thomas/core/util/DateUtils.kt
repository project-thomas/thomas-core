package com.thomas.core.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
import java.time.format.DateTimeFormatter.ISO_LOCAL_TIME
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.time.format.DateTimeFormatter.ISO_OFFSET_TIME
import java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME
import kotlin.random.Random

object DateUtils {

    private val startZonedDateTime: ZonedDateTime = ZonedDateTime.parse("2000-01-01T00:00:00.000000Z", ISO_ZONED_DATE_TIME)
    private val endZonedDateTime: ZonedDateTime = ZonedDateTime.parse("2099-12-31T23:59:59.999999Z", ISO_ZONED_DATE_TIME)

    private val startOffsetDateTime: OffsetDateTime = OffsetDateTime.parse("2000-01-01T00:00:00.000000Z", ISO_OFFSET_DATE_TIME)
    private val endOffsetDateTime: OffsetDateTime = OffsetDateTime.parse("2099-12-31T23:59:59.999999Z", ISO_OFFSET_DATE_TIME)

    private val startLocalDateTime: LocalDateTime = LocalDateTime.parse("2000-01-01T00:00:00.000000", ISO_LOCAL_DATE_TIME)
    private val endLocalDateTime: LocalDateTime = LocalDateTime.parse("2099-12-31T23:59:59.999999", ISO_LOCAL_DATE_TIME)

    private val startLocalDate: LocalDate = LocalDate.parse("2000-01-01", ISO_LOCAL_DATE)
    private val endLocalDate: LocalDate = LocalDate.parse("2099-12-31", ISO_LOCAL_DATE)

    private val startOffsetTime: OffsetTime = OffsetTime.parse("00:00:00.000000Z", ISO_OFFSET_TIME)
    private val endOffsetTime: OffsetTime = OffsetTime.parse("23:59:59.999999Z", ISO_OFFSET_TIME)

    private val startLocalTime: LocalTime = LocalTime.parse("00:00:00.000000", ISO_LOCAL_TIME)
    private val endLocalTime: LocalTime = LocalTime.parse("23:59:59.999999", ISO_LOCAL_TIME)

    fun randomZonedDateTime(
        start: ZonedDateTime = startZonedDateTime,
        end: ZonedDateTime = endZonedDateTime,
    ): ZonedDateTime {
        val millis = Random.nextLong(
            start.toInstant().toEpochMilli(),
            end.toInstant().toEpochMilli()
        )
        return Instant.ofEpochMilli(millis).let { ZonedDateTime.ofInstant(it, UTC) }
    }

    fun randomOffsetDateTime(
        start: OffsetDateTime = startOffsetDateTime,
        end: OffsetDateTime = endOffsetDateTime,
    ): OffsetDateTime {
        val millis = Random.nextLong(
            start.toInstant().toEpochMilli(),
            end.toInstant().toEpochMilli()
        )
        return Instant.ofEpochMilli(millis).let { OffsetDateTime.ofInstant(it, UTC) }
    }

    fun randomLocalDateTime(
        start: LocalDateTime = startLocalDateTime,
        end: LocalDateTime = endLocalDateTime,
    ): LocalDateTime {
        val millis = Random.nextLong(
            start.toInstant(UTC).toEpochMilli(),
            end.toInstant(UTC).toEpochMilli()
        )
        return Instant.ofEpochMilli(millis).let { LocalDateTime.ofInstant(it, UTC) }
    }

    fun randomLocalDate(
        start: LocalDate = startLocalDate,
        end: LocalDate = endLocalDate,
    ): LocalDate {
        val millis = Random.nextLong(
            start.atStartOfDay().toInstant(UTC).toEpochMilli(),
            end.atTime(23, 59, 59, 999999).toInstant(UTC).toEpochMilli()
        )
        return Instant.ofEpochMilli(millis).let { LocalDate.ofInstant(it, UTC) }
    }

    fun randomOffsetTime(
        start: OffsetTime = startOffsetTime,
        end: OffsetTime = endOffsetTime,
    ): OffsetTime {
        val millis = Random.nextLong(
            start.atDate(LocalDate.now()).toInstant().toEpochMilli(),
            end.atDate(LocalDate.now()).toInstant().toEpochMilli()
        )
        return Instant.ofEpochMilli(millis).let { OffsetDateTime.ofInstant(it, UTC).toOffsetTime() }
    }

    fun randomLocalTime(
        start: LocalTime = startLocalTime,
        end: LocalTime = endLocalTime,
    ): LocalTime {
        val millis = Random.nextLong(
            start.atDate(LocalDate.now()).toInstant(UTC).toEpochMilli(),
            end.atDate(LocalDate.now()).toInstant(UTC).toEpochMilli()
        )
        return Instant.ofEpochMilli(millis).let { LocalDateTime.ofInstant(it, UTC).toLocalTime() }
    }

}