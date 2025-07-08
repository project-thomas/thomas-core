package com.thomas.core.extension

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZoneOffset
import java.time.ZoneOffset.UTC
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

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

}
