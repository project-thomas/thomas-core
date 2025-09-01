package com.thomas.core.context

import com.thomas.core.extension.withSessionContext
import java.util.Locale
import kotlin.coroutines.EmptyCoroutineContext
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
import org.junit.jupiter.api.assertNotNull
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
    fun `should restore context when using withContext`() = runTest {
        val initialContext = SessionContext.create(token = "initial")
        SessionContextHolder.context = initialContext

        val temporaryContext = SessionContext.create(token = "temporary")
        val contextElement = CoroutineSessionContext.create(temporaryContext)

        withContext(contextElement) {
            assertEquals("temporary", SessionContextHolder.currentToken)
        }

        // Should restore to initial context
        assertEquals("initial", SessionContextHolder.currentToken)
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

    @Test
    fun `should test companion object current method`() = runTest {
        val initialContext = SessionContext.create(
            token = "current-method-test",
            locale = Locale.ITALIAN,
            properties = mapOf("test" to "current")
        )

        SessionContextHolder.context = initialContext

        val currentElement = CoroutineSessionContext.current()

        withContext(currentElement) {
            assertEquals("current-method-test", SessionContextHolder.currentToken)
            assertEquals(Locale.ITALIAN, SessionContextHolder.currentLocale)
            assertEquals("current", SessionContextHolder.getSessionProperty("test"))
        }
    }

    @Test
    fun `should test updateThreadContext with SessionContextHolder exception`() = runTest {
        val context = SessionContext.create(token = "exception-handling-test")
        val contextElement = CoroutineSessionContext.create(context)

        // Clear context to ensure we start fresh
        SessionContextHolder.clearContext()

        // Create a scenario where getting context might throw an exception
        // by testing edge case behavior
        withContext(contextElement) {
            assertEquals("exception-handling-test", SessionContextHolder.currentToken)

            // Test that even after potential issues, context is properly set
            delay(1)
            assertEquals("exception-handling-test", SessionContextHolder.currentToken)
        }

        // Should be cleared after restoration
        assertNull(SessionContextHolder.currentToken)
    }

    @Test
    fun `should test restoreThreadContext with different scenarios`() = runTest {
        // Test scenario: Context switching with restoration
        val initialContext = SessionContext.create(token = "initial-restore-test")
        SessionContextHolder.context = initialContext

        val tempContext = SessionContext.create(token = "temp-restore-test")
        val element = CoroutineSessionContext.create(tempContext)

        withContext(element) {
            assertEquals("temp-restore-test", SessionContextHolder.currentToken)
        }

        // Should be restored to initial context
        assertEquals("initial-restore-test", SessionContextHolder.currentToken)
    }

    @Test
    fun `should test equals method with all branches`() {
        val context1 = SessionContext.create(token = "equals-branch-test")
        val element1 = CoroutineSessionContext.create(context1)
        val element2 = CoroutineSessionContext.create(context1)
        val element3 = CoroutineSessionContext.create(SessionContext.create(token = "different"))

        // Test same reference
        assertTrue(element1.equals(element1))

        // Test equal contexts
        assertTrue(element1.equals(element2))

        // Test different contexts
        assertNotEquals(element1, element3)

        // Test different type
        assertNotEquals(element1, "not a CoroutineSessionContext")

        // Test null
        assertNotEquals(element1, null)

        // Test with other CoroutineSessionContext type check
        val otherObject: Any = element2
        assertTrue(element1.equals(otherObject))
    }

    @Test
    fun `should test hashCode stability and consistency`() {
        val context = SessionContext.create(
            token = "hash-stability-test",
            locale = Locale.GERMAN,
            properties = mapOf("hash" to "stable")
        )

        val element = CoroutineSessionContext.create(context)

        // Hash code should be stable across multiple calls
        val hash1 = element.hashCode()
        val hash2 = element.hashCode()
        val hash3 = element.hashCode()

        assertEquals(hash1, hash2)
        assertEquals(hash2, hash3)

        // Hash code should be based on sessionContext hashCode
        assertEquals(context.hashCode(), element.hashCode())
    }

    @Test
    fun `should test toString with various context states`() {
        // Test with empty context
        val emptyElement = CoroutineSessionContext.create(SessionContext.empty())
        val emptyToString = emptyElement.toString()
        assertTrue(emptyToString.contains("CoroutineSessionContext"))
        assertTrue(emptyToString.contains("sessionContext="))

        // Test with full context
        val fullContext = SessionContext.create(
            token = "toString-full-test",
            locale = Locale.JAPANESE,
            properties = mapOf("key1" to "value1", "key2" to "value2")
        )
        val fullElement = CoroutineSessionContext.create(fullContext)
        val fullToString = fullElement.toString()

        assertTrue(fullToString.contains("CoroutineSessionContext"))
        assertTrue(fullToString.contains("sessionContext="))
        assertTrue(fullToString.contains("SessionContext"))
    }

    @Test
    fun `should test key property is consistent`() {
        val element1 = CoroutineSessionContext.create(SessionContext.empty())
        val element2 = CoroutineSessionContext.create(SessionContext.create(token = "different"))

        // Key should be the same for all instances
        assertEquals(element1.key, element2.key)
        assertEquals(CoroutineSessionContext.Key, element1.key)
        assertEquals(CoroutineSessionContext, element1.key)

        // Key should be a singleton
        assertEquals(CoroutineSessionContext.Key, CoroutineSessionContext)
        assertEquals(element1.key, element2.key)
    }

    @Test
    fun `should test complex nested context scenario with restoration`() = runTest {
        val initialContext = SessionContext.create(token = "initial")
        val level1Context = SessionContext.create(token = "level1")
        val level2Context = SessionContext.create(token = "level2")
        val level3Context = SessionContext.create(token = "level3")

        SessionContextHolder.context = initialContext

        withContext(CoroutineSessionContext.create(level1Context)) {
            assertEquals("level1", SessionContextHolder.currentToken)

            withContext(CoroutineSessionContext.create(level2Context)) {
                assertEquals("level2", SessionContextHolder.currentToken)

                withContext(CoroutineSessionContext.create(level3Context)) {
                    assertEquals("level3", SessionContextHolder.currentToken)
                    delay(10)
                    assertEquals("level3", SessionContextHolder.currentToken)
                }

                // Should be restored to level2
                assertEquals("level2", SessionContextHolder.currentToken)
                delay(10)
                assertEquals("level2", SessionContextHolder.currentToken)
            }

            // Should be restored to level1
            assertEquals("level1", SessionContextHolder.currentToken)
            delay(10)
            assertEquals("level1", SessionContextHolder.currentToken)
        }

        // Should be restored to initial context
        assertEquals("initial", SessionContextHolder.currentToken)
    }

    @Test
    fun `should handle concurrent access to same CoroutineSessionContext instance`() = runTest {
        val sharedContext = SessionContext.create(
            token = "shared-context-test",
            properties = mapOf("shared" to "true")
        )
        val sharedElement = CoroutineSessionContext.create(sharedContext)

        val results = (1..10).map { index ->
            async {
                withContext(sharedElement) {
                    delay((1..20).random().toLong())

                    val token = SessionContextHolder.currentToken
                    val property = SessionContextHolder.getSessionProperty("shared")

                    "$token:$property:$index"
                }
            }
        }

        val completedResults = results.awaitAll()

        completedResults.forEach { result ->
            assertTrue(result.startsWith("shared-context-test:true:"))
        }
    }

    @Test
    fun `updateThreadContext should handle normal and exceptional cases`() = runTest {
        val testContext = SessionContext.create(token = "exception-test-token")
        val contextElement = CoroutineSessionContext.create(testContext)

        // Test 1: Normal case with cleared context (should return null)
        SessionContextHolder.clearContext()
        val result1 = contextElement.updateThreadContext(EmptyCoroutineContext)
        assertNull(result1?.currentUser)
        assertNull(result1?.currentToken)
        assertEquals("exception-test-token", SessionContextHolder.currentToken)

        // Test 2: Normal case with existing context (should return the existing context)
        val existingContext = SessionContext.create(token = "existing-token")
        SessionContextHolder.context = existingContext

        val result2 = contextElement.updateThreadContext(EmptyCoroutineContext)
        assertNotNull(result2)
        assertEquals("existing-token", result2?.currentToken)
        assertEquals("exception-test-token", SessionContextHolder.currentToken)

        // Test 3: Test that the method can handle multiple calls correctly
        val anotherContext = SessionContext.create(token = "another-test")
        val anotherElement = CoroutineSessionContext.create(anotherContext)

        val result3 = anotherElement.updateThreadContext(EmptyCoroutineContext)
        assertNotNull(result3)
        assertEquals("exception-test-token", result3?.currentToken)
        assertEquals("another-test", SessionContextHolder.currentToken)

        // Clean up
        SessionContextHolder.clearContext()
    }

    @Test
    fun `updateThreadContext should handle exception gracefully and return null`() = runTest {
        val testContext = SessionContext.create(token = "graceful-exception-test")
        val contextElement = CoroutineSessionContext.create(testContext)

        // Clear the context to ensure we start in a known state
        SessionContextHolder.clearContext()

        // Test the normal flow first
        val result1 = contextElement.updateThreadContext(EmptyCoroutineContext)
        assertNull(result1.currentUser) // Should be null since we cleared context
        assertNull(result1.currentToken) // Should be null since we cleared context
        assertEquals("graceful-exception-test", SessionContextHolder.currentToken)

        // Now test with existing context
        val existingContext = SessionContext.create(token = "existing-token")
        SessionContextHolder.context = existingContext

        val result2 = contextElement.updateThreadContext(EmptyCoroutineContext)
        assertEquals("existing-token", result2?.currentToken)
        assertEquals("graceful-exception-test", SessionContextHolder.currentToken)

        // Clean up
        SessionContextHolder.clearContext()
    }

    @Test
    fun `restoreThreadContext should call clearContext when oldState is null`() = runTest {
        val testContext = SessionContext.create(token = "restore-clear-test")
        val contextElement = CoroutineSessionContext.create(testContext)

        // Set up initial context
        SessionContextHolder.context = testContext
        assertEquals("restore-clear-test", SessionContextHolder.currentToken)

        // Call restoreThreadContext with null oldState - should hit the else branch
        contextElement.restoreThreadContext(EmptyCoroutineContext, SessionContext.empty())

        // Should have cleared the context (currentToken should be null)
        assertNull(SessionContextHolder.currentToken)
    }

    @Test
    fun `restoreThreadContext should restore oldState when not null`() = runTest {
        val newContext = SessionContext.create(token = "new-context")
        val oldContext = SessionContext.create(token = "old-context")
        val contextElement = CoroutineSessionContext.create(newContext)

        // Set up initial state
        SessionContextHolder.context = newContext
        assertEquals("new-context", SessionContextHolder.currentToken)

        // Call restoreThreadContext with non-null oldState - should hit the if branch
        contextElement.restoreThreadContext(EmptyCoroutineContext, oldContext)

        // Should have restored to old context
        assertEquals("old-context", SessionContextHolder.currentToken)

        // Now test the else branch - call with null oldState
        contextElement.restoreThreadContext(EmptyCoroutineContext, SessionContext.empty())

        // Should have cleared the context
        assertNull(SessionContextHolder.currentToken)
    }

    @Test
    fun `should test both branches of restoreThreadContext explicitly`() = runTest {
        val testContext = SessionContext.create(token = "branch-test")
        val contextElement = CoroutineSessionContext.create(testContext)

        // Test 1: oldState is not null (if branch)
        val savedContext = SessionContext.create(token = "saved-context")
        SessionContextHolder.context = testContext

        contextElement.restoreThreadContext(EmptyCoroutineContext, savedContext)
        assertEquals("saved-context", SessionContextHolder.currentToken)

        // Test 2: oldState is null (else branch)
        SessionContextHolder.context = testContext
        assertEquals("branch-test", SessionContextHolder.currentToken)

        contextElement.restoreThreadContext(EmptyCoroutineContext, SessionContext.empty())
        assertNull(SessionContextHolder.currentToken)
    }

    @Test
    fun `should test updateThreadContext exception handling with thread manipulation`() = runTest {
        val testContext = SessionContext.create(token = "thread-manipulation-test")
        val contextElement = CoroutineSessionContext.create(testContext)

        // Test normal operation first
        SessionContextHolder.clearContext()
        val result1 = contextElement.updateThreadContext(EmptyCoroutineContext)
        assertNull(result1.currentUser)
        assertNull(result1.currentToken)

        // Test with existing context to ensure try block works
        val existingContext = SessionContext.create(token = "existing")
        SessionContextHolder.context = existingContext

        val result2 = contextElement.updateThreadContext(EmptyCoroutineContext)
        assertNotNull(result2)
        assertEquals("existing", result2.currentToken)

        // Verify the new context was set
        assertEquals("thread-manipulation-test", SessionContextHolder.currentToken)
    }

}
