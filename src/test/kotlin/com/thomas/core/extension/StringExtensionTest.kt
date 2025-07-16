package com.thomas.core.extension

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class StringExtensionTest {

    companion object {

        @JvmStatic
        fun strings() = listOf(
            Arguments.of("test", "test"),
            Arguments.of("testCamel", "test_camel"),
            Arguments.of("testCamelCase", "test_camel_case"),
            Arguments.of("testCamelCase", "test_camel_case"),
            Arguments.of("test_camel_case", "test_camel_case"),
            Arguments.of("test_camelCase", "test_camel_case"),
        )

    }

    @Test
    fun `Remove letters from string should leave only numbers`() {
        assertEquals("9876543210", "9876543210qwerty".onlyNumbers())
        assertEquals("9876543210", "9876qwerty543210".onlyNumbers())
        assertEquals("9876543210", "qwerty9876543210".onlyNumbers())
    }

    @Test
    fun `Remove symbols from string should leave only numbers`() {
        assertEquals("9876543210", "9!@#8$%¨7&*(6)_+5-=¬4§,.3;/<2>:?1`´\"0'[]{}^~ªº|\\".onlyNumbers())
    }

    @Test
    fun `Remove symbols from string should leave only numbers and letters`() {
        assertEquals("a9b8c7d6e5f4g3h2i1j0", "a9b!@#8$%¨c7&*d(6e)_+5f-=¬4§g,.3;h/<2i>:?1`´j\"0'[]{}^~|\\".onlyLettersAndNumbers())
    }

    @Test
    fun `Given a valid UUID string it should return a UUID`() {
        assertNotNull("34ca8da1-b484-4167-bcc9-e9498b6d3ae2".toUUIDOrNull())
        assertNotNull("23028e1b-ec24-4026-8a5b-9d7be867b155".toUUIDOrNull())
        assertNotNull("9c27c9cb-d51f-42ea-84d7-a5e9d00cd1e0".toUUIDOrNull())
        assertNotNull("e00fff05-9bd3-488f-86cf-ecdd388f8e11".toUUIDOrNull())
    }

    @Test
    fun `Given a invalid UUID string it should return null`() {
        assertNull("".toUUIDOrNull())
        assertNull("qwerty".toUUIDOrNull())
        assertNull("01FXTPFJCJFGSRVPHCMKZWCPTG".toUUIDOrNull())
        assertNull("cl0levgka00005440rr0vruzf".toUUIDOrNull())
        assertNull("94f0dd72-71dx-4bea-809d-d1e7ef839a96".toUUIDOrNull())
    }

    @ParameterizedTest
    @MethodSource("strings")
    fun `Given a string it should returns in snake case`(value: String, expected: String) {
        assertEquals(expected, value.toSnakeCase())
    }

    @Test
    fun `Convert string to unaccented lower case`() {
        assertEquals("", "".unaccentedLower())
        assertEquals(" ", " ".unaccentedLower())
        assertEquals("qwerty", "Qwerty".unaccentedLower())
        assertEquals("aaaaaaaa cc eeeeee iiiiii nn oooooooo uuuuuu", "ÂâÃãÀàÁá Çç ÊêÈèÉé ÎîÌìÍí Ññ ÔôÕõÒòÓó ÛûÙùÚú".unaccentedLower())
    }

}
