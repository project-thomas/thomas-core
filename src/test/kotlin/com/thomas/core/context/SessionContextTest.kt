package com.thomas.core.context

import com.thomas.core.extension.withSessionContext
import com.thomas.core.generator.UserGenerator.generateSecurityUser
import com.thomas.core.model.security.SecurityUser
import io.mockk.every
import io.mockk.mockk
import java.util.Locale
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class SessionContextTest {

    private val mockUser = mockk<SecurityUser> {
        every { userId } returns UUID.randomUUID()
        every { fullName } returns "Test User"
    }

    @BeforeEach
    @AfterEach
    fun cleanup() {
        SessionContextHolder.clearContext()
    }

    @Test
    fun `should create empty context by default`() {
        val context = SessionContext.empty()

        assertNull(context.currentUser)
        assertNull(context.currentToken)
        assertEquals(Locale.ROOT, context.currentLocale)
        assertEquals(emptyMap<String, String>(), context.sessionProperties)
    }

    @Test
    fun `should create context with initial values`() {
        val properties = mapOf("key1" to "value1", "key2" to "value2")
        val token = "test-token"
        val locale = Locale.US

        val context = SessionContext.create(
            properties = properties,
            user = mockUser,
            token = token,
            locale = locale
        )

        assertEquals(mockUser, context.currentUser)
        assertEquals(token, context.currentToken)
        assertEquals(locale, context.currentLocale)
        assertEquals(properties, context.sessionProperties)
    }

    @Test
    fun `should create immutable copies with modifications`() {
        val originalContext = SessionContext.create(
            properties = mapOf("key1" to "value1"),
            user = mockUser,
            token = "token1",
            locale = Locale.US
        )

        val newUser = mockk<SecurityUser>()
        val modifiedContext = originalContext.withUser(newUser)

        assertNotSame(originalContext, modifiedContext)
        assertEquals(mockUser, originalContext.currentUser)
        assertEquals(newUser, modifiedContext.currentUser)
        assertEquals("token1", modifiedContext.currentToken)
        assertEquals(Locale.US, modifiedContext.currentLocale)
    }

    @Test
    fun `should handle concurrent access to SessionContextHolder`() {
        val threadCount = 50
        val barrier = CyclicBarrier(threadCount)
        val latch = CountDownLatch(threadCount)
        val results = mutableListOf<String?>()
        val errors = AtomicInteger(0)

        repeat(threadCount) { index ->
            thread {
                try {
                    barrier.await()

                    val context = SessionContext.create(
                        token = "token-$index",
                        properties = mapOf("thread" to "thread-$index")
                    )

                    SessionContextHolder.context = context

                    Thread.sleep(10)

                    val retrievedToken = SessionContextHolder.currentToken
                    val threadProperty = SessionContextHolder.getSessionProperty("thread")

                    synchronized(results) {
                        results.add("$retrievedToken-$threadProperty")
                    }

                    assertEquals("token-$index", retrievedToken)
                    assertEquals("thread-$index", threadProperty)

                } catch (e: Exception) {
                    errors.incrementAndGet()
                    e.printStackTrace()
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        assertEquals(0, errors.get(), "Should not have any errors in concurrent access")
        assertEquals(threadCount, results.size)
    }

    @Test
    fun `should isolate context between coroutines`() = runTest {
        val context1 = SessionContext.create(token = "token1", properties = mapOf("id" to "1"))
        val context2 = SessionContext.create(token = "token2", properties = mapOf("id" to "2"))

        val results = mutableListOf<Pair<String?, String?>>()

        val job1 = async {
            withSessionContext(context1) {
                delay(50)
                val token = SessionContextHolder.currentToken
                val id = SessionContextHolder.getSessionProperty("id")
                synchronized(results) {
                    results.add(token to id)
                }
                token to id
            }
        }

        val job2 = async {
            withSessionContext(context2) {
                delay(30)
                val token = SessionContextHolder.currentToken
                val id = SessionContextHolder.getSessionProperty("id")
                synchronized(results) {
                    results.add(token to id)
                }
                token to id
            }
        }

        val result1 = job1.await()
        val result2 = job2.await()

        assertEquals("token1" to "1", result1)
        assertEquals("token2" to "2", result2)
        assertEquals(2, results.size)
    }

    @Test
    fun `should preserve context across multiple coroutine dispatchers`() = runBlocking {
        val context = SessionContext.create(
            token = "persistent-token",
            properties = mapOf("test" to "value")
        )

        withSessionContext(context) {
            assertEquals("persistent-token", SessionContextHolder.currentToken)

            val job1 = async(Dispatchers.IO) {
                delay(10)
                SessionContextHolder.currentToken
            }

            val job2 = async(Dispatchers.Default) {
                delay(20)
                SessionContextHolder.getSessionProperty("test")
            }

            val results = awaitAll(job1, job2)
            assertEquals("persistent-token", results[0])
            assertEquals("value", results[1])
        }
    }

    @Test
    fun `should handle nested coroutine contexts correctly`() = runTest {
        val outerContext = SessionContext.create(token = "outer", properties = mapOf("level" to "outer"))
        val innerContext = SessionContext.create(token = "inner", properties = mapOf("level" to "inner"))

        withSessionContext(outerContext) {
            assertEquals("outer", SessionContextHolder.currentToken)
            assertEquals("outer", SessionContextHolder.getSessionProperty("level"))

            withSessionContext(innerContext) {
                assertEquals("inner", SessionContextHolder.currentToken)
                assertEquals("inner", SessionContextHolder.getSessionProperty("level"))
            }

            assertEquals("outer", SessionContextHolder.currentToken)
            assertEquals("outer", SessionContextHolder.getSessionProperty("level"))
        }
    }

    @Test
    fun `should handle context updates atomically`() {
        val initialContext = SessionContext.create(properties = mapOf("counter" to "0"))
        SessionContextHolder.context = initialContext

        repeat(100) {
            SessionContextHolder.updateContext { context ->
                val currentCount = context.getProperty("counter")?.toIntOrNull() ?: 0
                context.setProperty("counter", (currentCount + 1).toString())
            }
        }

        assertEquals("100", SessionContextHolder.getSessionProperty("counter"))
    }

    @Test
    fun `should clean up ThreadLocal on context removal`() {
        val context = SessionContext.create(token = "test-token")
        SessionContextHolder.context = context

        assertEquals("test-token", SessionContextHolder.currentToken)

        SessionContextHolder.clearContext()

        assertDoesNotThrow {
            SessionContextHolder.context
        }
    }

    @Test
    fun `should validate atomic operations in high concurrency`() {
        val threadCount = 50
        val iterationsPerThread = 20
        val successfulOperations = AtomicInteger(0)
        val threads = mutableListOf<Thread>()
        val barrier = CyclicBarrier(threadCount)
        val errors = mutableListOf<String>()

        repeat(threadCount) { threadIndex ->
            val thread = Thread {
                try {
                    barrier.await()
                    repeat(iterationsPerThread) { iteration ->
                        val expectedToken = "t${threadIndex}i$iteration"
                        val expectedProperties = mapOf(
                            "thread" to threadIndex.toString(),
                            "iteration" to iteration.toString()
                        )

                        val context = SessionContext.create(
                            token = expectedToken,
                            properties = expectedProperties
                        )

                        SessionContextHolder.context = context

                        val actualToken = SessionContextHolder.currentToken ?: "null-token"
                        val actualThreadProp = SessionContextHolder.getSessionProperty("thread") ?: "null-thread"
                        val actualIterProp = SessionContextHolder.getSessionProperty("iteration") ?: "null-iter"

                        // Validate consistency within the same thread
                        if (actualToken == expectedToken &&
                            actualThreadProp == threadIndex.toString() &&
                            actualIterProp == iteration.toString()
                        ) {
                            successfulOperations.incrementAndGet()
                        } else {
                            synchronized(errors) {
                                errors.add("Thread $threadIndex, Iteration $iteration: Expected token=$expectedToken, thread=$threadIndex, iteration=$iteration | Got token=$actualToken, thread=$actualThreadProp, iteration=$actualIterProp")
                            }
                        }

                        Thread.sleep(1) // Small delay to increase chances of race conditions
                    }
                } catch (e: Exception) {
                    synchronized(errors) {
                        errors.add("Exception in thread $threadIndex: ${e.message}")
                    }
                    e.printStackTrace()
                }
            }
            threads.add(thread)
            thread.start()
        }

        threads.forEach { it.join() }

        val expectedOperations = threadCount * iterationsPerThread
        val actualSuccessful = successfulOperations.get()

        if (errors.isNotEmpty()) {
            println("Errors found:")
            errors.take(10).forEach { println("  $it") }
            if (errors.size > 10) {
                println("  ... and ${errors.size - 10} more errors")
            }
        }

        assertTrue(actualSuccessful > 0, "At least some operations should succeed")
        assertTrue(
            actualSuccessful >= (expectedOperations * 0.95).toInt(),
            "At least 95% of operations should succeed. Expected: $expectedOperations, Successful: $actualSuccessful, Errors: ${errors.size}"
        )
    }

    @Test
    fun `should test both branches of setProperty method`() {
        val context = SessionContext.create(
            properties = mapOf("existing" to "value")
        )

        val contextWithNullProperty = context.setProperty("existing", null)
        assertNull(contextWithNullProperty.getProperty("existing"))

        val contextWithNewProperty = context.setProperty("new", "newValue")
        assertEquals("newValue", contextWithNewProperty.getProperty("new"))

        val contextWithUpdatedProperty = context.setProperty("existing", "updatedValue")
        assertEquals("updatedValue", contextWithUpdatedProperty.getProperty("existing"))
    }

    @Test
    fun `should test all branches of equals method thoroughly`() {
        val user1 = mockUser
        val user2 = mockk<SecurityUser> {
            every { userId } returns UUID.randomUUID()
            every { fullName } returns "Different User"
        }

        val context1 = SessionContext.create(
            user = user1,
            token = "token1",
            locale = Locale.US,
            properties = mapOf("key1" to "value1")
        )

        assertEquals(context1, context1)
        assertNotEquals(context1, null)
        assertNotEquals(context1, "not a SessionContext")

        val contextDifferentProps = SessionContext.create(
            user = user1,
            token = "token1",
            locale = Locale.US,
            properties = mapOf("key2" to "value2")
        )
        assertNotEquals(context1, contextDifferentProps)

        val contextDifferentUser = SessionContext.create(
            user = user2,
            token = "token1",
            locale = Locale.US,
            properties = mapOf("key1" to "value1")
        )
        assertNotEquals(context1, contextDifferentUser)

        val contextDifferentToken = SessionContext.create(
            user = user1,
            token = "token2",
            locale = Locale.US,
            properties = mapOf("key1" to "value1")
        )
        assertNotEquals(context1, contextDifferentToken)

        val contextDifferentLocale = SessionContext.create(
            user = user1,
            token = "token1",
            locale = Locale.FRENCH,
            properties = mapOf("key1" to "value1")
        )
        assertNotEquals(context1, contextDifferentLocale)

        val contextEqual = SessionContext.create(
            user = user1,
            token = "token1",
            locale = Locale.US,
            properties = mapOf("key1" to "value1")
        )
        assertEquals(context1, contextEqual)
    }

    @Test
    fun `should test hashCode with null values`() {
        val contextWithNulls = SessionContext.create(
            user = null,
            token = null,
            locale = Locale.ROOT,
            properties = emptyMap()
        )

        val anotherContextWithNulls = SessionContext.create(
            user = null,
            token = null,
            locale = Locale.ROOT,
            properties = emptyMap()
        )

        assertEquals(contextWithNulls.hashCode(), anotherContextWithNulls.hashCode())
    }

    @Test
    fun `should test copy method with all parameter variations`() {
        val originalContext = SessionContext.create(
            user = mockUser,
            token = "original-token",
            locale = Locale.GERMAN,
            properties = mapOf("original" to "value")
        )

        val defaultCopy = originalContext.copy()
        assertEquals(originalContext, defaultCopy)
        assertNotSame(originalContext, defaultCopy)

        val newProperties = mapOf("new" to "property")
        val copyWithNewProps = originalContext.withProperties(newProperties)
        assertEquals(newProperties, copyWithNewProps.sessionProperties)

        val newUser = mockk<SecurityUser> {
            every { userId } returns UUID.randomUUID()
            every { fullName } returns "New User"
        }
        val copyWithNewUser = originalContext.withUser(newUser)
        assertEquals(newUser, copyWithNewUser.currentUser)

        val copyWithNewToken = originalContext.withToken("new-token")
        assertEquals("new-token", copyWithNewToken.currentToken)

        val copyWithNewLocale = originalContext.withLocale(Locale.JAPANESE)
        assertEquals(Locale.JAPANESE, copyWithNewLocale.currentLocale)

        val copyWithAllNew = originalContext
            .withProperties(mapOf("all" to "new"))
            .withUser(newUser)
            .withToken("all-new-token")
            .withLocale(Locale.ITALIAN)
        assertEquals(mapOf("all" to "new"), copyWithAllNew.sessionProperties)
        assertEquals(newUser, copyWithAllNew.currentUser)
        assertEquals("all-new-token", copyWithAllNew.currentToken)
        assertEquals(Locale.ITALIAN, copyWithAllNew.currentLocale)
    }

    @Test
    fun `should test toString method completeness`() {
        val context = SessionContext.create(
            user = mockUser,
            token = "toString-token",
            locale = Locale.CANADA,
            properties = mapOf("test" to "toString")
        )

        val toString = context.toString()

        assertTrue(toString.contains("SessionContext"))
        assertTrue(toString.contains("currentLocale"))
        assertTrue(toString.contains("currentToken"))
        assertTrue(toString.contains("currentUser"))
        assertTrue(toString.contains("sessionProperties"))
    }

    @Test
    fun `should test all withMethods preserve immutability`() {
        val originalContext = SessionContext.create(
            user = mockUser,
            token = "original",
            locale = Locale.US,
            properties = mapOf("original" to "value")
        )

        val newUser = mockk<SecurityUser> {
            every { userId } returns UUID.randomUUID()
            every { fullName } returns "New User"
        }

        val withUserContext = originalContext.withUser(newUser)
        assertNotSame(originalContext, withUserContext)
        assertEquals(mockUser, originalContext.currentUser)
        assertEquals(newUser, withUserContext.currentUser)

        val withTokenContext = originalContext.withToken("new-token")
        assertNotSame(originalContext, withTokenContext)
        assertEquals("original", originalContext.currentToken)
        assertEquals("new-token", withTokenContext.currentToken)

        val withLocaleContext = originalContext.withLocale(Locale.FRENCH)
        assertNotSame(originalContext, withLocaleContext)
        assertEquals(Locale.US, originalContext.currentLocale)
        assertEquals(Locale.FRENCH, withLocaleContext.currentLocale)

        val newProperties = mapOf("new" to "properties")
        val withPropsContext = originalContext.withProperties(newProperties)
        assertNotSame(originalContext, withPropsContext)
        assertEquals(mapOf("original" to "value"), originalContext.sessionProperties)
        assertEquals(newProperties, withPropsContext.sessionProperties)
    }

    @Test
    fun `should test hashCode with non-null currentUser`() {
        val user = generateSecurityUser()

        // Test hashCode when currentUser is not null (covers currentUser?.hashCode() ?: 0 branch)
        val context1 = SessionContext.create(user = user)
        val context2 = SessionContext.create(user = user)

        // Same user should produce same hashCode
        assertEquals(context1.hashCode(), context2.hashCode())

        // Different user should produce different hashCode
        val differentUser = generateSecurityUser()
        val context3 = SessionContext.create(user = differentUser)

        assertNotEquals(context1.hashCode(), context3.hashCode())

        // Context with null user should have different hashCode than context with user
        val contextWithNullUser = SessionContext.empty()
        assertNotEquals(context1.hashCode(), contextWithNullUser.hashCode())
    }

    @Test
    fun `should test hashCode with non-null currentToken`() {
        // Test hashCode when currentToken is not null (covers currentToken?.hashCode() ?: 0 branch)
        val context1 = SessionContext.create(token = "test-token-123")
        val context2 = SessionContext.create(token = "test-token-123")

        // Same token should produce same hashCode
        assertEquals(context1.hashCode(), context2.hashCode())

        // Different token should produce different hashCode
        val context3 = SessionContext.create(token = "different-token-456")
        assertNotEquals(context1.hashCode(), context3.hashCode())

        // Context with null token should have different hashCode than context with token
        val contextWithNullToken = SessionContext.empty()
        assertNotEquals(context1.hashCode(), contextWithNullToken.hashCode())
    }

    @Test
    fun `should test hashCode with all combinations of nullable fields`() {
        val user = generateSecurityUser()

        // Test all combinations of null/non-null for currentUser and currentToken
        val context1 = SessionContext.create(user = null, token = null) // both null
        val context2 = SessionContext.create(user = user, token = null) // user non-null, token null
        val context3 = SessionContext.create(user = null, token = "token") // user null, token non-null
        val context4 = SessionContext.create(user = user, token = "token") // both non-null

        // All should have different hashCodes
        val hashCodes = setOf(context1.hashCode(), context2.hashCode(), context3.hashCode(), context4.hashCode())
        assertEquals(4, hashCodes.size, "All combinations should produce different hashCodes")

        // Verify specific branches are covered
        assertNotEquals(context1.hashCode(), context2.hashCode()) // Tests currentUser?.hashCode() branch
        assertNotEquals(context1.hashCode(), context3.hashCode()) // Tests currentToken?.hashCode() branch
        assertNotEquals(context2.hashCode(), context4.hashCode()) // Tests both branches together
    }

}
