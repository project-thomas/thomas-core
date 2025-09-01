package com.thomas.core.context

import com.thomas.core.extension.withSessionContext
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull

class CoroutineSessionContextTest {

    @BeforeEach
    @AfterEach
    fun cleanup() {
        SessionContextHolder.clearContext()
    }

    @Test
    fun `should preserve context across coroutine suspension points`() = runTest {
        val originalContext = SessionContext.create(
            token = "test-token",
            locale = Locale.FRENCH,
            properties = mapOf("key" to "value")
        )

        val contextElement = CoroutineSessionContext.create(originalContext)

        withContext(contextElement) {
            assertEquals("test-token", SessionContextHolder.currentToken)
            assertEquals(Locale.FRENCH, SessionContextHolder.currentLocale)
            assertEquals("value", SessionContextHolder.getSessionProperty("key"))

            delay(100)

            assertEquals("test-token", SessionContextHolder.currentToken)
            assertEquals(Locale.FRENCH, SessionContextHolder.currentLocale)
            assertEquals("value", SessionContextHolder.getSessionProperty("key"))
        }
    }

    @Test
    fun `should restore previous context after coroutine completion`() = runBlocking {
        val initialContext = SessionContext.create(token = "initial")
        val temporaryContext = SessionContext.create(token = "temporary")

        SessionContextHolder.context = initialContext
        assertEquals("initial", SessionContextHolder.currentToken)

        withContext(CoroutineSessionContext.create(temporaryContext)) {
            assertEquals("temporary", SessionContextHolder.currentToken)
        }

        assertEquals("initial", SessionContextHolder.currentToken)
    }

    @Test
    fun `should handle nested coroutine contexts`() = runTest {
        val context1 = SessionContext.create(token = "context1")
        val context2 = SessionContext.create(token = "context2")
        val context3 = SessionContext.create(token = "context3")

        withContext(CoroutineSessionContext.create(context1)) {
            assertEquals("context1", SessionContextHolder.currentToken)

            withContext(CoroutineSessionContext.create(context2)) {
                assertEquals("context2", SessionContextHolder.currentToken)

                withContext(CoroutineSessionContext.create(context3)) {
                    assertEquals("context3", SessionContextHolder.currentToken)
                }

                assertEquals("context2", SessionContextHolder.currentToken)
            }

            assertEquals("context1", SessionContextHolder.currentToken)
        }
    }

    @Test
    fun `should work correctly with different dispatchers`() = runBlocking {
        val context = SessionContext.create(
            token = "cross-dispatcher-token",
            properties = mapOf("dispatcher" to "test")
        )

        withContext(CoroutineSessionContext.create(context)) {
            assertEquals("cross-dispatcher-token", SessionContextHolder.currentToken)

            val ioResult = async(Dispatchers.IO) {
                delay(10)
                SessionContextHolder.currentToken to SessionContextHolder.getSessionProperty("dispatcher")
            }

            val defaultResult = async(Dispatchers.Default) {
                delay(20)
                SessionContextHolder.currentToken to SessionContextHolder.getSessionProperty("dispatcher")
            }

            val (ioToken, ioProperty) = ioResult.await()
            val (defaultToken, defaultProperty) = defaultResult.await()

            assertEquals("cross-dispatcher-token", ioToken)
            assertEquals("test", ioProperty)
            assertEquals("cross-dispatcher-token", defaultToken)
            assertEquals("test", defaultProperty)
        }
    }

    @Test
    fun `should handle concurrent coroutines with different contexts`() = runTest {
        val contexts = (1..10).map { index ->
            SessionContext.create(
                token = "token-$index",
                properties = mapOf("id" to index.toString())
            )
        }

        val results = contexts.map { context ->
            async {
                withContext(CoroutineSessionContext.create(context)) {
                    delay((1..50).random().toLong())

                    val token = SessionContextHolder.currentToken
                    val id = SessionContextHolder.getSessionProperty("id")

                    token to id
                }
            }
        }

        val completedResults = results.awaitAll()

        completedResults.forEachIndexed { index, (token, id) ->
            assertEquals("token-${index + 1}", token)
            assertEquals("${index + 1}", id)
        }
    }

    @Test
    fun `should properly implement equals and hashCode`() {
        val context1 = SessionContext.create(token = "test")
        val context2 = SessionContext.create(token = "test")
        val context3 = SessionContext.create(token = "different")

        val element1 = CoroutineSessionContext.create(context1)
        val element2 = CoroutineSessionContext.create(context2)
        val element3 = CoroutineSessionContext.create(context3)

        assertEquals(element1, element2)
        assertEquals(element1.hashCode(), element2.hashCode())

        assertNotEquals(element1, element3)
        assertNotEquals(element1.hashCode(), element3.hashCode())
    }

    @Test
    fun `should handle null previous context gracefully`() = runTest {
        SessionContextHolder.clearContext()

        val context = SessionContext.create(token = "new-context")

        withContext(CoroutineSessionContext.create(context)) {
            assertEquals("new-context", SessionContextHolder.currentToken)
        }

        assertEquals(SessionContext.empty().currentToken, SessionContextHolder.currentToken)
    }

    @Test
    fun `should work with extension functions`() = runTest {
        val context = SessionContext.create(
            token = "extension-token",
            properties = mapOf("source" to "extension")
        )

        withSessionContext(context) {
            assertEquals("extension-token", SessionContextHolder.currentToken)
            assertEquals("extension", SessionContextHolder.getSessionProperty("source"))

            delay(50)

            assertEquals("extension-token", SessionContextHolder.currentToken)
            assertEquals("extension", SessionContextHolder.getSessionProperty("source"))
        }
    }

    @Test
    fun `should handle exception in updateThreadContext gracefully`() = runTest {
        val context = SessionContext.create(token = "exception-test")
        val contextElement = CoroutineSessionContext.create(context)

        SessionContextHolder.clearContext()

        withContext(contextElement) {
            assertEquals("exception-test", SessionContextHolder.currentToken)
        }
    }

    @Test
    fun `should restore context when oldState is null`() = runTest {
        SessionContextHolder.clearContext()

        val context = SessionContext.create(token = "restore-test")
        val contextElement = CoroutineSessionContext.create(context)

        withContext(contextElement) {
            assertEquals("restore-test", SessionContextHolder.currentToken)
        }

        assertNull(SessionContextHolder.currentToken)
    }

    @Test
    fun `should restore context when oldState is not null`() = runTest {
        val initialContext = SessionContext.create(token = "initial-token")
        SessionContextHolder.context = initialContext

        val temporaryContext = SessionContext.create(token = "temporary-token")
        val contextElement = CoroutineSessionContext.create(temporaryContext)

        withContext(contextElement) {
            assertEquals("temporary-token", SessionContextHolder.currentToken)
        }

        assertEquals("initial-token", SessionContextHolder.currentToken)
    }

    @Test
    fun `should test all branches of equals method`() {
        val context1 = SessionContext.create(token = "equals-test")
        val context2 = SessionContext.create(token = "equals-test")
        val context3 = SessionContext.create(token = "different-token")

        val element1 = CoroutineSessionContext.create(context1)
        val element2 = CoroutineSessionContext.create(context2)
        val element3 = CoroutineSessionContext.create(context3)

        assertEquals(element1, element1)
        assertEquals(element1, element2)
        assertNotEquals(element1, element3)
        assertNotEquals(element1, "not a CoroutineSessionContext")
        assertNotEquals(element1, null)
    }

    @Test
    fun `should test hashCode consistency`() {
        val context = SessionContext.create(token = "hash-test")
        val element1 = CoroutineSessionContext.create(context)
        val element2 = CoroutineSessionContext.create(context)

        assertEquals(element1.hashCode(), element2.hashCode())
        assertEquals(element1.hashCode(), element1.hashCode()) // Consistency
    }

    @Test
    fun `should test toString method`() {
        val context = SessionContext.create(
            token = "toString-test",
            properties = mapOf("key" to "value")
        )
        val element = CoroutineSessionContext.create(context)

        val toString = element.toString()
        assertTrue(toString.contains("CoroutineSessionContext"))
        assertTrue(toString.contains("sessionContext="))
    }

    @Test
    fun `should test companion object key property`() {
        val element = CoroutineSessionContext.create(SessionContext.empty())
        assertEquals(CoroutineSessionContext.Key, element.key)
        assertEquals(CoroutineSessionContext, element.key)
    }

}
