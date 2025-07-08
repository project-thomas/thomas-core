package com.thomas.core.extension

import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CollectionExtensionTest {

    @Test
    fun `When element is not in list, should add element`() {
        val uuid = UUID.randomUUID().toString()
        val new = UUID.randomUUID().toString()
        val list = mutableListOf(uuid)
        assertEquals(1, list.size)
        assertTrue(list.addIfAbsent(new))
        assertEquals(2, list.size)
    }

    @Test
    fun `When element is in list, should not add element`() {
        val uuid = UUID.randomUUID().toString()
        val list = mutableListOf(uuid)
        assertEquals(1, list.size)
        assertFalse(list.addIfAbsent(uuid))
        assertEquals(1, list.size)
    }

    @Test
    fun `When condition is true, should add element`() {
        val list = mutableListOf(0)
        list.addIf(4) { list.sum() < 10 }
        assertTrue(list.contains(4))
        list.addIf(2) { list.sum() < 10 }
        assertTrue(list.contains(2))
        list.addIf(5) { list.sum() < 10 }
        assertTrue(list.contains(5))
    }

    @Test
    fun `When condition is false, should add element`() {
        val list = mutableListOf(10)
        list.addIf(4) { list.sum() < 10 }
        assertFalse(list.contains(4))
        list.addIf(2) { list.sum() < 10 }
        assertFalse(list.contains(2))
        list.addIf(5) { list.sum() < 10 }
        assertFalse(list.contains(5))
    }

}
