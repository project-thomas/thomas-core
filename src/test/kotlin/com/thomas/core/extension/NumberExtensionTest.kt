package com.thomas.core.extension

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class NumberExtensionTest {

    @Test
    fun `Int is within range`() {
        assertTrue(10.isBetween(9, 11))
        assertTrue(10.isBetween(10, 11))
        assertTrue(10.isBetween(10, 10))
        assertTrue(10.isBetween(9, 10))
    }

    @Test
    fun `Int is not within range`() {
        assertFalse(10.isBetween(11, 15))
        assertFalse(10.isBetween(11, 11))
        assertFalse(10.isBetween(9, 9))
        assertFalse(10.isBetween(11, 9))
    }

    @Test
    fun `Long is higher`() {
        assertTrue(10L.isHigher(-1L))
        assertTrue(10L.isHigher(0L))
        assertTrue(10L.isHigher(1L))
        assertFalse(10L.isHigher(10L))
        assertFalse(10L.isHigher(90L))
    }

    @Test
    fun `Long is lower`() {
        assertFalse(10L.isLower(-1L))
        assertFalse(10L.isLower(0L))
        assertFalse(10L.isLower(1L))
        assertFalse(10L.isLower(10L))
        assertTrue(10L.isLower(90L))
        assertTrue(10L.isLower(11L))
        assertTrue(10L.isLower(100L))
    }

}
