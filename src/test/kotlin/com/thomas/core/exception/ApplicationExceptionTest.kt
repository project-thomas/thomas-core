package com.thomas.core.exception

import com.thomas.core.exception.ErrorType.APPLICATION_ERROR
import com.thomas.core.exception.ErrorType.NOT_FOUND
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

internal class ApplicationExceptionTest {

    @Test
    internal fun `should create exception with message only`() {
        val message = "Test application exception"

        val exception = TestApplicationException(message)

        assertEquals(message, exception.message)
        assertNull(exception.cause)
        assertEquals(APPLICATION_ERROR, exception.type)
    }

    @Test
    internal fun `should create exception with message and cause`() {
        val message = "Test application exception"
        val cause = RuntimeException("Underlying cause")

        val exception = TestApplicationException(message, cause = cause)

        assertEquals(message, exception.message)
        assertSame(cause, exception.cause)
    }

    @Test
    internal fun `should create exception with all parameters`() {
        val message = "Test exception"
        val type = NOT_FOUND
        val detail = mapOf("field1" to listOf("error1", "error2"))
        val cause = IllegalStateException("Underlying cause")

        val exception = TestApplicationException(message, type, detail, cause)

        assertEquals(message, exception.message)
        assertEquals(type, exception.type)
        assertEquals(detail, exception.detail)
        assertSame(cause, exception.cause)
    }

    @Test
    internal fun `should create exception with default message`() {
        val exception = TestApplicationException()

        assertNotNull(exception.message)
        assertEquals(APPLICATION_ERROR, exception.type)
        assertNull(exception.detail)
        assertNull(exception.cause)
    }

    @Test
    internal fun `should create exception with empty details`() {
        val message = "Test message"
        val details = emptyMap<String, List<String>>()

        val exception = TestApplicationException(message, detail = details)

        assertEquals(message, exception.message)
        assertEquals(details, exception.detail)
    }

    @Test
    internal fun `should be throwable and catchable`() {
        val message = "Throwable test"
        var caughtException: ApplicationException? = null

        try {
            throw TestApplicationException(message)
        } catch (e: ApplicationException) {
            caughtException = e
        }

        assertEquals(message, caughtException?.message)
    }

    @Test
    internal fun `should maintain stack trace information`() {
        val exception = TestApplicationException("Stack trace test")

        val stackTrace = exception.stackTrace

        assert(stackTrace.isNotEmpty())
        assertEquals("ApplicationExceptionTest", stackTrace[0].className.substringAfterLast('.'))
    }

    @Test
    internal fun `should handle nested exceptions properly`() {
        val rootCause = IllegalArgumentException("Root cause")
        val intermediateCause = RuntimeException("Intermediate", rootCause)
        val exception = TestApplicationException("Top level", cause = intermediateCause)

        assertEquals("Top level", exception.message)
        assertEquals("Intermediate", exception.cause?.message)
        assertEquals("Root cause", exception.cause?.cause?.message)
    }

    @Test
    internal fun `should handle error types correctly`() {
        ErrorType.entries.forEach { errorType ->
            val exception = TestApplicationException("Test", errorType)
            assertEquals(errorType, exception.type)
        }
    }

    private class TestApplicationException(
        message: String = "Default test message",
        type: ErrorType = APPLICATION_ERROR,
        detail: Map<String, List<String>>? = null,
        cause: Throwable? = null
    ) : ApplicationException(message, type, detail, cause)
}
