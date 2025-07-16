package com.thomas.core.extension

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GeneralExtensionTest {

    @Test
    fun `Class logger`(){
        assertEquals($$"com.thomas.core.extension.GeneralExtensionTest$FirstClass", FirstClass().logger().name)
        assertEquals($$"com.thomas.core.extension.GeneralExtensionTest$SecondClass", SecondClass().logger().name)
    }

    private class FirstClass

    private class SecondClass

}