package com.thomas.core.context

import com.thomas.core.model.security.SecurityUser
import io.mockk.every
import io.mockk.mockk
import java.util.Locale
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

@DisplayName("SessionContextHolder Tests")
class SessionContextHolderTest {

    private val mockUser = mockk<SecurityUser> {
        every { userId } returns UUID.randomUUID()
        every { fullName } returns "Test User"
    }

    @BeforeEach
    @AfterEach
    fun cleanup() {
        SessionContextHolder.clearContext()
    }

    @Nested
    @DisplayName("Context Management")
    inner class ContextManagementTests {

        @Test
        fun `should initialize with empty context by default`() {
            val context = SessionContextHolder.context

            assertThrows<UnauthenticatedUserException> { context.currentUser }
            assertNull(context.currentToken)
            assertEquals(Locale.ROOT, context.currentLocale)
            assertEquals(emptyMap<String, String>(), context.sessionProperties())
        }

        @Test
        fun `should set and get context correctly`() {
            val newContext = SessionContext.create(
                user = mockUser,
                token = "test-token",
                locale = Locale.US,
                properties = mapOf("key" to "value")
            )

            SessionContextHolder.context = newContext

            assertEquals(newContext, SessionContextHolder.context)
            assertEquals(mockUser, SessionContextHolder.currentUser)
            assertEquals("test-token", SessionContextHolder.currentToken)
            assertEquals(Locale.US, SessionContextHolder.currentLocale)
        }

        @Test
        fun `should clear context successfully`() {
            val context = SessionContext.create(user = mockUser, token = "test")
            SessionContextHolder.context = context

            assertEquals(mockUser, SessionContextHolder.currentUser)

            SessionContextHolder.clearContext()

            assertThrows<UnauthenticatedUserException> {
                SessionContextHolder.currentUser
            }
            assertNull(SessionContextHolder.currentToken)
        }
    }

    @Nested
    @DisplayName("User Management")
    inner class UserManagementTests {

        @Test
        fun `should set and get current user`() {
            SessionContextHolder.currentUser = mockUser

            assertEquals(mockUser, SessionContextHolder.currentUser)
        }

        @Test
        fun `should throw UnauthenticatedUserException when no user set`() {
            SessionContextHolder.clearContext()

            assertThrows<UnauthenticatedUserException> {
                SessionContextHolder.currentUser
            }
        }

        @Test
        fun `should update user and preserve other context properties`() {
            val initialContext = SessionContext.create(
                token = "test-token",
                locale = Locale.FRENCH,
                properties = mapOf("key" to "value")
            )
            SessionContextHolder.context = initialContext

            SessionContextHolder.currentUser = mockUser

            assertEquals(mockUser, SessionContextHolder.currentUser)
            assertEquals("test-token", SessionContextHolder.currentToken)
            assertEquals(Locale.FRENCH, SessionContextHolder.currentLocale)
            assertEquals("value", SessionContextHolder.getSessionProperty("key"))
        }
    }

    @Nested
    @DisplayName("Token Management")
    inner class TokenManagementTests {

        @Test
        fun `should set and get current token`() {
            val token = "test-token-123"

            SessionContextHolder.currentToken = token

            assertEquals(token, SessionContextHolder.currentToken)
        }

        @Test
        fun `should handle null token`() {
            SessionContextHolder.currentToken = "initial-token"
            assertEquals("initial-token", SessionContextHolder.currentToken)

            SessionContextHolder.currentToken = null

            assertNull(SessionContextHolder.currentToken)
        }

        @Test
        fun `should update token and preserve other context properties`() {
            val initialContext = SessionContext.create(
                user = mockUser,
                locale = Locale.GERMAN,
                properties = mapOf("test" to "data")
            )
            SessionContextHolder.context = initialContext

            SessionContextHolder.currentToken = "new-token"

            assertEquals("new-token", SessionContextHolder.currentToken)
            assertEquals(mockUser, SessionContextHolder.currentUser)
            assertEquals(Locale.GERMAN, SessionContextHolder.currentLocale)
            assertEquals("data", SessionContextHolder.getSessionProperty("test"))
        }
    }

    @Nested
    @DisplayName("Locale Management")
    inner class LocaleManagementTests {

        @Test
        fun `should set and get current locale`() {
            val locale = Locale.JAPANESE

            SessionContextHolder.currentLocale = locale

            assertEquals(locale, SessionContextHolder.currentLocale)
        }

        @Test
        fun `should have ROOT locale as default`() {
            SessionContextHolder.clearContext()

            assertEquals(Locale.ROOT, SessionContextHolder.currentLocale)
        }

        @Test
        fun `should update locale and preserve other context properties`() {
            val initialContext = SessionContext.create(
                user = mockUser,
                token = "locale-token",
                properties = mapOf("locale-test" to "value")
            )
            SessionContextHolder.context = initialContext

            SessionContextHolder.currentLocale = Locale.ITALIAN

            assertEquals(Locale.ITALIAN, SessionContextHolder.currentLocale)
            assertEquals(mockUser, SessionContextHolder.currentUser)
            assertEquals("locale-token", SessionContextHolder.currentToken)
            assertEquals("value", SessionContextHolder.getSessionProperty("locale-test"))
        }
    }

    @Nested
    @DisplayName("Session Properties Management")
    inner class SessionPropertiesTests {

        @Test
        fun `should set and get session property`() {
            val key = "test-key"
            val value = "test-value"

            SessionContextHolder.setSessionProperty(key, value)

            assertEquals(value, SessionContextHolder.getSessionProperty(key))
        }

        @Test
        fun `should return null for non-existent property`() {
            assertNull(SessionContextHolder.getSessionProperty("non-existent"))
        }

        @Test
        fun `should remove property when setting null value`() {
            SessionContextHolder.setSessionProperty("key", "value")
            assertEquals("value", SessionContextHolder.getSessionProperty("key"))

            SessionContextHolder.setSessionProperty("key", null)

            assertNull(SessionContextHolder.getSessionProperty("key"))
        }

        @Test
        fun `should return all session properties`() {
            val properties = mapOf(
                "key1" to "value1",
                "key2" to "value2",
                "key3" to "value3"
            )

            properties.forEach { (key, value) ->
                SessionContextHolder.setSessionProperty(key, value)
            }

            val retrievedProperties = SessionContextHolder.sessionProperties()
            assertEquals(properties, retrievedProperties)
        }

        @Test
        fun `should update multiple properties and preserve other context`() {
            val initialContext = SessionContext.create(
                user = mockUser,
                token = "prop-token"
            )
            SessionContextHolder.context = initialContext

            SessionContextHolder.setSessionProperty("new-key", "new-value")

            assertEquals("new-value", SessionContextHolder.getSessionProperty("new-key"))
            assertEquals(mockUser, SessionContextHolder.currentUser)
            assertEquals("prop-token", SessionContextHolder.currentToken)
        }
    }

    @Nested
    @DisplayName("Context Updates")
    inner class ContextUpdateTests {

        @Test
        fun `should update context with function`() {
            val initialContext = SessionContext.create(
                token = "initial",
                properties = mapOf("counter" to "0")
            )
            SessionContextHolder.context = initialContext

            SessionContextHolder.updateContext { context ->
                val counter = context.getProperty("counter")?.toInt() ?: 0
                context.setProperty("counter", (counter + 10).toString())
                    .withToken("updated")
            }

            assertEquals("updated", SessionContextHolder.currentToken)
            assertEquals("10", SessionContextHolder.getSessionProperty("counter"))
        }

        @Test
        fun `should handle complex context updates`() {
            SessionContextHolder.updateContext { context ->
                context.withUser(mockUser)
                    .withToken("complex-token")
                    .withLocale(Locale.CANADA)
                    .setProperty("operation", "complex")
            }

            assertEquals(mockUser, SessionContextHolder.currentUser)
            assertEquals("complex-token", SessionContextHolder.currentToken)
            assertEquals(Locale.CANADA, SessionContextHolder.currentLocale)
            assertEquals("complex", SessionContextHolder.getSessionProperty("operation"))
        }
    }

    @Nested
    @DisplayName("Thread Safety")
    inner class ThreadSafetyTests {

        @Test
        fun `should handle concurrent context updates safely`() {
            val threadCount = 20
            val barrier = CyclicBarrier(threadCount)
            val latch = CountDownLatch(threadCount)
            val errors = AtomicInteger(0)

            repeat(threadCount) { index ->
                thread {
                    try {
                        barrier.await()

                        val context = SessionContext.create(
                            token = "thread-$index",
                            properties = mapOf("thread-id" to index.toString())
                        )
                        SessionContextHolder.context = context

                        Thread.sleep(10)

                        val token = SessionContextHolder.currentToken
                        val threadId = SessionContextHolder.getSessionProperty("thread-id")

                        assertEquals("thread-$index", token)
                        assertEquals(index.toString(), threadId)

                    } catch (e: Exception) {
                        errors.incrementAndGet()
                        e.printStackTrace()
                    } finally {
                        latch.countDown()
                    }
                }
            }

            latch.await()
            assertEquals(0, errors.get(), "Should not have errors in concurrent access")
        }

        @Test
        fun `should maintain thread isolation for properties`() {
            val threadCount = 15
            val barrier = CyclicBarrier(threadCount)
            val latch = CountDownLatch(threadCount)
            val results = mutableMapOf<Int, String?>()
            val errors = AtomicInteger(0)

            repeat(threadCount) { index ->
                thread {
                    try {
                        barrier.await()

                        SessionContextHolder.setSessionProperty("thread-prop", "value-$index")

                        Thread.sleep(20)

                        val value = SessionContextHolder.getSessionProperty("thread-prop")
                        synchronized(results) {
                            results[index] = value
                        }

                    } catch (e: Exception) {
                        errors.incrementAndGet()
                    } finally {
                        latch.countDown()
                    }
                }
            }

            latch.await()
            assertEquals(0, errors.get())
            assertEquals(threadCount, results.size)

            results.forEach { (index, value) ->
                assertEquals("value-$index", value)
            }
        }

        @Test
        fun `should handle concurrent context clearing safely`() {
            val threadCount = 10
            val latch = CountDownLatch(threadCount)
            val errors = AtomicInteger(0)

            repeat(threadCount) { index ->
                thread {
                    try {
                        val context = SessionContext.create(token = "clear-test-$index")
                        SessionContextHolder.context = context

                        Thread.sleep(5)

                        SessionContextHolder.clearContext()

                        assertDoesNotThrow {
                            SessionContextHolder.context
                        }

                        assertThrows<UnauthenticatedUserException> {
                            SessionContextHolder.currentUser
                        }

                    } catch (e: Exception) {
                        errors.incrementAndGet()
                    } finally {
                        latch.countDown()
                    }
                }
            }

            latch.await()
            assertEquals(0, errors.get())
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    inner class EdgeCaseTests {

        @Test
        fun `should handle rapid successive updates`() {
            repeat(100) { index ->
                SessionContextHolder.updateContext { context ->
                    context.setProperty("rapid-update", index.toString())
                }
            }

            assertEquals("99", SessionContextHolder.getSessionProperty("rapid-update"))
        }

        @Test
        fun `should preserve context integrity during multiple operations`() {
            SessionContextHolder.currentUser = mockUser
            SessionContextHolder.currentToken = "integrity-token"
            SessionContextHolder.currentLocale = Locale.KOREA
            SessionContextHolder.setSessionProperty("integrity", "test")

            assertEquals(mockUser, SessionContextHolder.currentUser)
            assertEquals("integrity-token", SessionContextHolder.currentToken)
            assertEquals(Locale.KOREA, SessionContextHolder.currentLocale)
            assertEquals("test", SessionContextHolder.getSessionProperty("integrity"))

            SessionContextHolder.setSessionProperty("new-prop", "new-value")
            SessionContextHolder.currentToken = "updated-token"

            assertEquals(mockUser, SessionContextHolder.currentUser)
            assertEquals("updated-token", SessionContextHolder.currentToken)
            assertEquals(Locale.KOREA, SessionContextHolder.currentLocale)
            assertEquals("test", SessionContextHolder.getSessionProperty("integrity"))
            assertEquals("new-value", SessionContextHolder.getSessionProperty("new-prop"))
        }

        @Test
        fun `should test both branches of setSessionProperty`() {
            SessionContextHolder.setSessionProperty("test-key", "test-value")
            assertEquals("test-value", SessionContextHolder.getSessionProperty("test-key"))

            SessionContextHolder.setSessionProperty("test-key", null)
            assertNull(SessionContextHolder.getSessionProperty("test-key"))

            SessionContextHolder.setSessionProperty("keep", "this")
            SessionContextHolder.setSessionProperty("remove", "this")

            assertEquals("this", SessionContextHolder.getSessionProperty("keep"))
            assertEquals("this", SessionContextHolder.getSessionProperty("remove"))

            SessionContextHolder.setSessionProperty("remove", null)

            assertEquals("this", SessionContextHolder.getSessionProperty("keep"))
            assertNull(SessionContextHolder.getSessionProperty("remove"))
        }

        @Test
        fun `should test updateContext with complex scenarios`() {
            val initialContext = SessionContext.create(
                token = "initial",
                properties = mapOf("counter" to "0", "flag" to "false")
            )
            SessionContextHolder.context = initialContext

            SessionContextHolder.updateContext { context ->
                val counter = context.getProperty("counter")?.toInt() ?: 0
                val flag = context.getProperty("flag")?.toBoolean() ?: false

                var updatedContext = context.setProperty("counter", (counter + 1).toString())

                if (!flag) {
                    updatedContext = updatedContext.setProperty("flag", "true")
                }

                updatedContext.withToken("updated-${counter + 1}")
            }

            assertEquals("updated-1", SessionContextHolder.currentToken)
            assertEquals("1", SessionContextHolder.getSessionProperty("counter"))
            assertEquals("true", SessionContextHolder.getSessionProperty("flag"))
        }

        @Test
        fun `should handle empty and null values in all operations`() {
            SessionContextHolder.setSessionProperty("empty", "")
            assertEquals("", SessionContextHolder.getSessionProperty("empty"))

            SessionContextHolder.currentToken = null
            assertNull(SessionContextHolder.currentToken)

            val emptyPropsContext = SessionContext.create(properties = emptyMap())
            SessionContextHolder.context = emptyPropsContext
            assertEquals(emptyMap<String, String>(), SessionContextHolder.sessionProperties())
        }
    }
}
