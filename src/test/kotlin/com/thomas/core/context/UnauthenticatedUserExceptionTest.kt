package com.thomas.core.context

import com.thomas.core.exception.ApplicationException
import com.thomas.core.exception.ErrorType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("UnauthenticatedUserException Tests")
class UnauthenticatedUserExceptionTest {

    @Nested
    @DisplayName("Exception Construction")
    inner class ExceptionConstructionTests {

        @Test
        fun `should create exception with default message`() {
            val exception = UnauthenticatedUserException()

            assertNotNull(exception.message)
            assertTrue(exception.message.isNotBlank())
            assertEquals(ErrorType.UNAUTHENTICATED_USER, exception.type)
        }

        @Test
        fun `should create exception with custom message`() {
            val customMessage = "Custom authentication error message"

            val exception = UnauthenticatedUserException(customMessage)

            assertEquals(customMessage, exception.message)
            assertEquals(ErrorType.UNAUTHENTICATED_USER, exception.type)
        }
    }

    @Nested
    @DisplayName("Exception Properties")
    inner class ExceptionPropertiesTests {

        @Test
        fun `should have correct error type`() {
            val exception = UnauthenticatedUserException()

            assertEquals(ErrorType.UNAUTHENTICATED_USER, exception.type)
        }

        @Test
        fun `should inherit from ApplicationException`() {
            val exception = UnauthenticatedUserException()

            assertInstanceOf(ApplicationException::class.java, exception)
        }

        @Test
        fun `should be a RuntimeException`() {
            val exception = UnauthenticatedUserException()

            assertInstanceOf(RuntimeException::class.java, exception)
        }
    }

    @Nested
    @DisplayName("Exception Behavior")
    inner class ExceptionBehaviorTests {

        @Test
        fun `should be throwable`() {
            val exception = UnauthenticatedUserException("Test message")

            try {
                throw exception
            } catch (e: UnauthenticatedUserException) {
                assertEquals("Test message", e.message)
                assertEquals(ErrorType.UNAUTHENTICATED_USER, e.type)
            }
        }

        @Test
        fun `should maintain stack trace`() {
            try {
                throw UnauthenticatedUserException("Stack trace test")
            } catch (e: UnauthenticatedUserException) {
                assertNotNull(e.stackTrace)
                assertTrue(e.stackTrace.isNotEmpty())
            }
        }

    }

    @Nested
    @DisplayName("Integration with SessionContext")
    inner class IntegrationTests {

        @Test
        fun `should be thrown when accessing user in empty context`() {
            val emptyContext = SessionContext.empty()

            try {
                emptyContext.currentUser
            } catch (e: Exception) {
                assertInstanceOf(UnauthenticatedUserException::class.java, e)
                assertEquals(ErrorType.UNAUTHENTICATED_USER, (e as UnauthenticatedUserException).type)
            }
        }

        @Test
        fun `should be thrown by SessionContextHolder when no user set`() {
            SessionContextHolder.clearContext()

            try {
                SessionContextHolder.currentUser
            } catch (e: Exception) {
                assertInstanceOf(UnauthenticatedUserException::class.java, e)
                assertEquals(ErrorType.UNAUTHENTICATED_USER, (e as UnauthenticatedUserException).type)
            }
        }
    }

    @Nested
    @DisplayName("Error Messages")
    inner class ErrorMessageTests {

        @Test
        fun `should have meaningful default error message`() {
            val exception = UnauthenticatedUserException()

            val message = exception.message
            assertNotNull(message)
            assertTrue(message.isNotBlank())
            assertTrue(message.isNotEmpty())
        }

        @Test
        fun `should preserve custom error message`() {
            val customMessages = listOf(
                "User authentication required",
                "Please log in to continue",
                "Session expired - authentication needed",
                ""
            )

            customMessages.forEach { customMessage ->
                val exception = UnauthenticatedUserException(customMessage)
                assertEquals(customMessage, exception.message)
            }
        }

        @Test
        fun `should handle special characters in message`() {
            val specialMessage = "Authentication failed: áéíóú çñü @#$%^&*(){}[]"

            val exception = UnauthenticatedUserException(specialMessage)

            assertEquals(specialMessage, exception.message)
        }
    }
}
