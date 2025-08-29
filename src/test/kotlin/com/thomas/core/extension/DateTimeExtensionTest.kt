package com.thomas.core.extension

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.time.Duration as KDuration
import java.time.Duration

internal class DateTimeExtensionTest {

    @Test
    fun `LocalDate to ISO date should return a ISO String correctly`() {
        assertEquals("1990-04-28", LocalDate.of(1990, Month.APRIL, 28).toIsoDate())
        assertEquals("2020-08-05", LocalDate.of(2020, Month.AUGUST, 5).toIsoDate())
    }

    @Test
    fun `LocalDateTime to ISO date time should return a ISO String correctly`() {
        assertEquals(
            "1990-04-28T09:30:27.657",
            LocalDateTime.of(
                1990,
                Month.APRIL.value,
                28,
                9,
                30,
                27,
                657000000
            ).toIsoDateTime()
        )
        assertEquals(
            "2005-12-31T17:04:01.992135171",
            LocalDateTime.of(
                2005,
                Month.DECEMBER.value,
                31,
                17,
                4,
                1,
                992135171
            ).toIsoDateTime()
        )
    }

    @Test
    fun `LocalTime to ISO date time should return a ISO String correctly`() {
        assertEquals(
            "09:30:27.657",
            LocalTime.of(
                9,
                30,
                27,
                657000000
            ).toIsoTime()
        )
        assertEquals(
            "17:04:01.992135171",
            LocalTime.of(
                17,
                4,
                1,
                992135171
            ).toIsoTime()
        )
    }

    @Test
    fun `OffsetDateTime to ISO date time should return a ISO String correctly`() {
        assertEquals(
            "1990-04-28T09:30:27.657-03:00",
            OffsetDateTime.of(
                1990,
                Month.APRIL.value,
                28,
                9,
                30,
                27,
                657000000,
                ZoneOffset.ofHours(-3)
            ).toIsoOffsetDateTime()
        )
        assertEquals(
            "2005-12-31T17:04:01.992135171Z",
            OffsetDateTime.of(
                2005,
                Month.DECEMBER.value,
                31,
                17,
                4,
                1,
                992135171,
                UTC
            ).toIsoOffsetDateTime()
        )
    }

    @Test
    fun `OffsetTime to ISO date time should return a ISO String correctly`() {
        assertEquals(
            "09:30:27.657-03:00",
            OffsetTime.of(
                9,
                30,
                27,
                657000000,
                ZoneOffset.ofHours(-3)
            ).toIsoOffsetTime()
        )
        assertEquals(
            "17:04:01.992135171Z",
            OffsetTime.of(
                17,
                4,
                1,
                992135171,
                UTC
            ).toIsoOffsetTime()
        )
    }

    @Test
    fun `ZonedDateTime to ISO zoned date time should return ISO String correctly`() {
        assertEquals(
            "1990-04-28T09:30:27.657-03:00",
            ZonedDateTime.of(
                1990,
                Month.APRIL.value,
                28,
                9,
                30,
                27,
                657000000,
                ZoneId.of("America/Sao_Paulo")
            ).toIsoZonedDateTime()
        )
        assertEquals(
            "2005-12-31T17:04:01.992135171Z",
            ZonedDateTime.of(
                2005,
                Month.DECEMBER.value,
                31,
                17,
                4,
                1,
                992135171,
                ZoneId.of("UTC")
            ).toIsoZonedDateTime()
        )
        assertEquals(
            "2023-06-15T14:30:00+02:00",
            ZonedDateTime.of(
                2023,
                Month.JUNE.value,
                15,
                14,
                30,
                0,
                0,
                ZoneId.of("Europe/Berlin")
            ).toIsoZonedDateTime()
        )
        assertEquals(
            "2020-01-01T00:00:00+09:00",
            ZonedDateTime.of(
                2020,
                Month.JANUARY.value,
                1,
                0,
                0,
                0,
                0,
                ZoneId.of("Asia/Tokyo")
            ).toIsoZonedDateTime()
        )
    }

    @Test
    fun `Kotlin Duration to hours pattern should return HH colon mm colon ss dot nanos format correctly`() {
        assertEquals(
            "00:00:00.000000000",
            KDuration.ZERO.toHoursPattern()
        )
        assertEquals(
            "01:30:45.000000000",
            (1.hours + 30.minutes + 45.seconds).toHoursPattern()
        )
        assertEquals(
            "10:05:30.500000000",
            (10.hours + 5.minutes + 30.seconds + 500000000.nanoseconds).toHoursPattern()
        )
        assertEquals(
            "23:59:59.999999999",
            (23.hours + 59.minutes + 59.seconds + 999999999.nanoseconds).toHoursPattern()
        )
        assertEquals(
            "25:15:42.123456789",
            (25.hours + 15.minutes + 42.seconds + 123456789.nanoseconds).toHoursPattern()
        )
    }

    @Test
    fun `Kotlin Duration to minutes pattern should return mmm colon ss dot nanos format correctly`() {
        assertEquals(
            "000:00.000000000",
            KDuration.ZERO.toMinutesPattern()
        )
        assertEquals(
            "005:30.000000000",
            (5.minutes + 30.seconds).toMinutesPattern()
        )
        assertEquals(
            "090:45.000000000",
            (1.hours + 30.minutes + 45.seconds).toMinutesPattern()
        )
        assertEquals(
            "015:00.500000000",
            (15.minutes + 500000000.nanoseconds).toMinutesPattern()
        )
        assertEquals(
            "120:30.123456789",
            (2.hours + 30.seconds + 123456789.nanoseconds).toMinutesPattern()
        )
    }

    @Test
    fun `Kotlin Duration to seconds pattern should return ssss dot nanos format correctly`() {
        assertEquals(
            "0000.000000000",
            KDuration.ZERO.toSecondsPattern()
        )
        assertEquals(
            "0030.000000000",
            30.seconds.toSecondsPattern()
        )
        assertEquals(
            "0090.000000000",
            (1.minutes + 30.seconds).toSecondsPattern()
        )
        assertEquals(
            "3600.000000000",
            1.hours.toSecondsPattern()
        )
        assertEquals(
            "5445.123456789",
            (5445.seconds + 123456789.nanoseconds).toSecondsPattern()
        )
    }


    @Test
    fun `Duration to hours pattern should return HH colon mm colon ss dot nanos format correctly`() {
        assertEquals(
            "00:00:00.000000000",
            Duration.ZERO.toHoursPattern()
        )
        assertEquals(
            "01:30:45.000000000",
            Duration.ofNanos(5_445_000_000_000L).toHoursPattern()
        )
        assertEquals(
            "10:05:30.500000000",
            Duration.ofNanos(36_330_500_000_000L).toHoursPattern()
        )
        assertEquals(
            "23:59:59.999999999",
            Duration.ofNanos(86_399_999_999_999L).toHoursPattern()
        )
        assertEquals(
            "25:15:42.123456789",
            Duration.ofNanos(90_942_123_456_789L).toHoursPattern()
        )
    }

    @Test
    fun `Duration to minutes pattern should return mmm colon ss dot nanos format correctly`() {
        assertEquals(
            "000:00.000000000",
            Duration.ZERO.toMinutesPattern()
        )
        assertEquals(
            "005:30.000000000",
            Duration.ofNanos(330_000_000_000L).toMinutesPattern()
        )
        assertEquals(
            "090:45.000000000",
            Duration.ofNanos(5_445_000_000_000L).toMinutesPattern()
        )
        assertEquals(
            "015:00.500000000",
            Duration.ofNanos(900_500_000_000L).toMinutesPattern()
        )
        assertEquals(
            "120:30.123456789",
            Duration.ofNanos(7_230_123_456_789L).toMinutesPattern()
        )
    }

    @Test
    fun `Duration to seconds pattern should return ssss dot nanos format correctly`() {
        assertEquals(
            "0000.000000000",
            Duration.ZERO.toSecondsPattern()
        )
        assertEquals(
            "0030.000000000",
            Duration.ofNanos(30_000_000_000L).toSecondsPattern()
        )
        assertEquals(
            "0090.000000000",
            Duration.ofNanos(90_000_000_000L).toSecondsPattern()
        )
        assertEquals(
            "3600.000000000",
            Duration.ofNanos(3_600_000_000_000L).toSecondsPattern()
        )
        assertEquals(
            "5445.123456789",
            Duration.ofNanos(5_445_123_456_789L).toSecondsPattern()
        )
    }

}
