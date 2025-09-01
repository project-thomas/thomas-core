@file:OptIn(ExperimentalStdlibApi::class)

package com.thomas.core.extension

import com.thomas.core.context.CoroutineSessionContext
import com.thomas.core.context.SessionContext
import com.thomas.core.context.SessionContextHolder
import com.thomas.core.model.security.SecurityUser
import io.mockk.every
import io.mockk.mockk
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull

@DisplayName("SessionContextExtensions Tests")
class SessionContextExtensionsTest {

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
    @DisplayName("withSessionContext Function")
    inner class WithSessionContextTests {

        @Test
        fun `should execute block with provided session context`() = runTest {
            val testContext = SessionContext.create(
                user = mockUser,
                token = "test-token",
                locale = Locale.FRENCH,
                properties = mapOf("test" to "value")
            )

            val result = withSessionContext(testContext) {
                assertEquals(mockUser, SessionContextHolder.currentUser)
                assertEquals("test-token", SessionContextHolder.currentToken)
                assertEquals(Locale.FRENCH, SessionContextHolder.currentLocale)
                assertEquals("value", SessionContextHolder.getSessionProperty("test"))

                "success"
            }

            assertEquals("success", result)
        }

        @Test
        fun `should preserve context across suspension points`() = runTest {
            val testContext = SessionContext.create(
                token = "suspension-token",
                properties = mapOf("suspend" to "test")
            )

            withSessionContext(testContext) {
                assertEquals("suspension-token", SessionContextHolder.currentToken)
                assertEquals("test", SessionContextHolder.getSessionProperty("suspend"))

                delay(50)

                assertEquals("suspension-token", SessionContextHolder.currentToken)
                assertEquals("test", SessionContextHolder.getSessionProperty("suspend"))
            }
        }

        @Test
        fun `should handle nested withSessionContext calls`() = runTest {
            val outerContext = SessionContext.create(
                token = "outer-token",
                properties = mapOf("level" to "outer")
            )
            val innerContext = SessionContext.create(
                token = "inner-token",
                properties = mapOf("level" to "inner")
            )

            withSessionContext(outerContext) {
                assertEquals("outer-token", SessionContextHolder.currentToken)
                assertEquals("outer", SessionContextHolder.getSessionProperty("level"))

                withSessionContext(innerContext) {
                    assertEquals("inner-token", SessionContextHolder.currentToken)
                    assertEquals("inner", SessionContextHolder.getSessionProperty("level"))
                }

                assertEquals("outer-token", SessionContextHolder.currentToken)
                assertEquals("outer", SessionContextHolder.getSessionProperty("level"))
            }
        }

        @Test
        fun `should work with different dispatchers`() = runBlocking {
            val testContext = SessionContext.create(
                token = "dispatcher-token",
                properties = mapOf("dispatcher" to "test")
            )

            withSessionContext(testContext) {
                val ioResult = async(Dispatchers.IO) {
                    delay(10)
                    SessionContextHolder.currentToken to SessionContextHolder.getSessionProperty(
                        "dispatcher"
                    )
                }

                val defaultResult = async(Dispatchers.Default) {
                    delay(20)
                    SessionContextHolder.currentToken to SessionContextHolder.getSessionProperty(
                        "dispatcher"
                    )
                }

                val (ioToken, ioProperty) = ioResult.await()
                val (defaultToken, defaultProperty) = defaultResult.await()

                assertEquals("dispatcher-token", ioToken)
                assertEquals("test", ioProperty)
                assertEquals("dispatcher-token", defaultToken)
                assertEquals("test", defaultProperty)
            }
        }

        @Test
        fun `should handle exceptions properly`() = runTest {
            val testContext = SessionContext.create(token = "exception-token")

            try {
                withSessionContext(testContext) {
                    assertEquals("exception-token", SessionContextHolder.currentToken)
                    throw RuntimeException("Test exception")
                }
            } catch (e: RuntimeException) {
                assertEquals("Test exception", e.message)
            }
        }
    }

    @Nested
    @DisplayName("withCurrentSessionContext Function")
    inner class WithCurrentSessionContextTests {

        @Test
        fun `should execute block with current session context`() = runTest {
            val currentContext = SessionContext.create(
                user = mockUser,
                token = "current-token",
                locale = Locale.GERMAN
            )
            SessionContextHolder.context = currentContext

            val result = withCurrentSessionContext {
                assertEquals(mockUser, SessionContextHolder.currentUser)
                assertEquals("current-token", SessionContextHolder.currentToken)
                assertEquals(Locale.GERMAN, SessionContextHolder.currentLocale)

                "current-success"
            }

            assertEquals("current-success", result)
        }

        @Test
        fun `should work with empty context`() = runTest {
            SessionContextHolder.clearContext()

            withCurrentSessionContext {
                assertEquals(
                    SessionContext.Companion.empty().currentToken,
                    SessionContextHolder.currentToken
                )
                assertEquals(
                    SessionContext.Companion.empty().currentLocale,
                    SessionContextHolder.currentLocale
                )
            }
        }

        @Test
        fun `should preserve current context modifications`() = runTest {
            val initialContext = SessionContext.create(
                token = "initial-current",
                properties = mapOf("initial" to "value")
            )
            SessionContextHolder.context = initialContext

            withCurrentSessionContext {
                assertEquals("initial-current", SessionContextHolder.currentToken)
                assertEquals("value", SessionContextHolder.getSessionProperty("initial"))

                delay(30)

                assertEquals("initial-current", SessionContextHolder.currentToken)
                assertEquals("value", SessionContextHolder.getSessionProperty("initial"))
            }
        }

        @Test
        fun `should handle concurrent access to current context`() = runTest {
            val baseContext = SessionContext.create(
                token = "concurrent-base",
                properties = mapOf("base" to "value")
            )
            SessionContextHolder.context = baseContext

            val results = (1..5).map { index ->
                async {
                    withCurrentSessionContext {
                        delay(10L * index)
                        SessionContextHolder.currentToken to SessionContextHolder.getSessionProperty(
                            "base"
                        )
                    }
                }
            }

            results.forEach { deferred ->
                val (token, property) = deferred.await()
                assertEquals("concurrent-base", token)
                assertEquals("value", property)
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Nested
    @DisplayName("CoroutineContext.withSessionContext Extension")
    inner class CoroutineContextWithSessionContextTests {

        @Test
        fun `should add session context to coroutine context`() = runTest {
            val testContext = SessionContext.create(
                token = "context-extension-token",
                properties = mapOf("extension" to "test")
            )

            val combinedContext = EmptyCoroutineContext.withSessionContext(testContext)

            assertNotNull(combinedContext[CoroutineSessionContext.Key])

            withContext(combinedContext) {
                assertEquals("context-extension-token", SessionContextHolder.currentToken)
                assertEquals("test", SessionContextHolder.getSessionProperty("extension"))
            }
        }

        @Test
        fun `should combine with existing coroutine context`() = runTest {
            val testContext = SessionContext.create(token = "combined-token")

            val combinedContext = (Dispatchers.IO + EmptyCoroutineContext).withSessionContext(testContext)

            launch(combinedContext) {
                assertEquals("combined-token", SessionContextHolder.currentToken)
                assertTrue(coroutineContext[CoroutineDispatcher.Key] == Dispatchers.IO)
            }.join()
        }

        @Test
        fun `should override existing session context`() = runTest {
            val firstContext = SessionContext.create(token = "first-token")
            val secondContext = SessionContext.create(token = "second-token")

            val firstCoroutineContext = EmptyCoroutineContext.withSessionContext(firstContext)
            val secondCoroutineContext = firstCoroutineContext.withSessionContext(secondContext)

            withContext(secondCoroutineContext) {
                assertEquals("second-token", SessionContextHolder.currentToken)
            }
        }
    }

    @Nested
    @DisplayName("CoroutineContext.withCurrentSessionContext Extension")
    inner class CoroutineContextWithCurrentSessionContextTests {

        @Test
        fun `should add current session context to coroutine context`() = runTest {
            val currentContext = SessionContext.create(
                token = "current-extension-token",
                properties = mapOf("current-ext" to "value")
            )
            SessionContextHolder.context = currentContext

            val combinedContext = EmptyCoroutineContext.withCurrentSessionContext()

            assertNotNull(combinedContext[CoroutineSessionContext.Key])

            withContext(combinedContext) {
                assertEquals("current-extension-token", SessionContextHolder.currentToken)
                assertEquals("value", SessionContextHolder.getSessionProperty("current-ext"))
            }
        }

        @Test
        fun `should work with empty current context`() = runTest {
            SessionContextHolder.clearContext()

            val combinedContext = EmptyCoroutineContext.withCurrentSessionContext()

            withContext(combinedContext) {
                assertEquals(
                    SessionContext.Companion.empty().currentToken,
                    SessionContextHolder.currentToken
                )
            }
        }

        @Test
        fun `should combine with dispatcher context`() = runTest {
            val currentContext = SessionContext.create(token = "dispatcher-current")
            SessionContextHolder.context = currentContext

            val combinedContext = Dispatchers.Default.withCurrentSessionContext()

            launch(combinedContext) {
                assertEquals("dispatcher-current", SessionContextHolder.currentToken)
                assertTrue(coroutineContext[CoroutineDispatcher.Key] == Dispatchers.Default)
            }.join()
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    inner class IntegrationTests {

        @Test
        fun `should work seamlessly with existing CoroutineSessionContext tests`() = runTest {
            val testContext = SessionContext.create(
                user = mockUser,
                token = "integration-token",
                locale = Locale.CANADA,
                properties = mapOf("integration" to "test")
            )

            withSessionContext(testContext) {
                withContext(CoroutineSessionContext.create(testContext)) {
                    assertEquals(mockUser, SessionContextHolder.currentUser)
                    assertEquals("integration-token", SessionContextHolder.currentToken)
                    assertEquals(Locale.CANADA, SessionContextHolder.currentLocale)
                    assertEquals("test", SessionContextHolder.getSessionProperty("integration"))
                }
            }
        }

        @Test
        fun `should handle complex nesting scenarios`() = runTest {
            val context1 = SessionContext.create(token = "level1")
            val context2 = SessionContext.create(token = "level2")
            val context3 = SessionContext.create(token = "level3")

            SessionContextHolder.context = context1

            withCurrentSessionContext {
                assertEquals("level1", SessionContextHolder.currentToken)

                withSessionContext(context2) {
                    assertEquals("level2", SessionContextHolder.currentToken)

                    val combinedContext = Dispatchers.IO.withSessionContext(context3)
                    launch(combinedContext) {
                        assertEquals("level3", SessionContextHolder.currentToken)
                    }.join()

                    assertEquals("level2", SessionContextHolder.currentToken)
                }

                assertEquals("level1", SessionContextHolder.currentToken)
            }
        }

        @Test
        fun `should maintain thread safety across all extension functions`() = runTest {
            val contexts = (1..10).map { index ->
                SessionContext.create(
                    token = "thread-safe-$index",
                    properties = mapOf("id" to index.toString())
                )
            }

            val results = contexts.mapIndexed { index, context ->
                async {
                    withSessionContext(context) {
                        delay((1..20).random().toLong())

                        val token = SessionContextHolder.currentToken
                        val id = SessionContextHolder.getSessionProperty("id")

                        token to id
                    }
                }
            }

            val completedResults = results.awaitAll()

            completedResults.forEachIndexed { index, (token, id) ->
                assertEquals("thread-safe-${index + 1}", token)
                assertEquals("${index + 1}", id)
            }
        }

        @Test
        fun `should handle all possible exception scenarios in extensions`() = runTest {
            val testContext = SessionContext.create(token = "exception-handling")

            try {
                withSessionContext(testContext) {
                    assertEquals("exception-handling", SessionContextHolder.currentToken)
                    throw IllegalStateException("Test exception in withSessionContext")
                }
            } catch (e: IllegalStateException) {
                assertEquals("Test exception in withSessionContext", e.message)
            }

            SessionContextHolder.context = testContext
            try {
                withCurrentSessionContext {
                    assertEquals("exception-handling", SessionContextHolder.currentToken)
                    throw IllegalArgumentException("Test exception in withCurrentSessionContext")
                }
            } catch (e: IllegalArgumentException) {
                assertEquals("Test exception in withCurrentSessionContext", e.message)
            }
        }

        @Test
        fun `should test all CoroutineContext combination scenarios`() = runTest {
            val testContext = SessionContext.create(token = "combination-test")
            val currentContext = SessionContext.create(token = "current-context")

            SessionContextHolder.context = currentContext

            val combinedContext = (Dispatchers.IO + EmptyCoroutineContext)
                .withSessionContext(testContext)
                .withCurrentSessionContext()

            withContext(combinedContext) {
                assertEquals("current-context", SessionContextHolder.currentToken)
                assertTrue(coroutineContext[CoroutineDispatcher.Key] == Dispatchers.IO)
            }

            val anotherCombinedContext = Dispatchers.Default
                .withCurrentSessionContext()
                .withSessionContext(testContext)

            withContext(anotherCombinedContext) {
                assertEquals("combination-test", SessionContextHolder.currentToken)
                assertTrue(coroutineContext[CoroutineDispatcher.Key] == Dispatchers.Default)
            }

            val thirdContext = SessionContext.create(token = "third-context")
            val multipleSessionContext = EmptyCoroutineContext
                .withSessionContext(currentContext)
                .withSessionContext(testContext)
                .withSessionContext(thirdContext)

            withContext(multipleSessionContext) {
                assertEquals("third-context", SessionContextHolder.currentToken)
            }
        }

        @Test
        fun `should test edge cases with empty contexts`() = runTest {
            SessionContextHolder.clearContext()

            val emptyContext = SessionContext.empty()
            withSessionContext(emptyContext) {
                assertNull(SessionContextHolder.currentToken)
                assertEquals(Locale.ROOT, SessionContextHolder.currentLocale)
            }

            withCurrentSessionContext {
                assertNull(SessionContextHolder.currentToken)
                assertEquals(Locale.ROOT, SessionContextHolder.currentLocale)
            }

            val contextWithEmpty = EmptyCoroutineContext.withCurrentSessionContext()
            withContext(contextWithEmpty) {
                assertNull(SessionContextHolder.currentToken)
            }
        }

        @Test
        fun `should test return values and type consistency`() = runTest {
            val testContext = SessionContext.create(token = "return-test")

            val stringResult: String = withSessionContext(testContext) {
                "test-string"
            }
            assertEquals("test-string", stringResult)

            SessionContextHolder.context = testContext
            val intResult: Int = withCurrentSessionContext {
                42
            }
            assertEquals(42, intResult)

            data class TestResult(val token: String?, val success: Boolean)

            val complexResult: TestResult = withSessionContext(testContext) {
                TestResult(SessionContextHolder.currentToken, true)
            }
            assertEquals(TestResult("return-test", true), complexResult)
        }
    }

    @Nested
    @DisplayName("Dispatchers.IO SessionContext Methods")
    inner class DispatchersIOTests {

        @Test
        fun `should execute withSessionContextIO with correct dispatcher and context`() = runTest {
            val testContext = SessionContext.create(
                user = mockUser,
                token = "io-token",
                locale = Locale.JAPANESE,
                properties = mapOf("dispatcher" to "io")
            )

            val result = withSessionContextIO(testContext) {
                assertEquals(mockUser, SessionContextHolder.currentUser)
                assertEquals("io-token", SessionContextHolder.currentToken)
                assertEquals(Locale.JAPANESE, SessionContextHolder.currentLocale)
                assertEquals("io", SessionContextHolder.getSessionProperty("dispatcher"))

                assertTrue(coroutineContext[CoroutineDispatcher.Key] == Dispatchers.IO)

                "io-success"
            }

            assertEquals("io-success", result)
        }

        @Test
        fun `should execute withCurrentSessionContextIO with current context`() = runTest {
            val currentContext = SessionContext.create(
                token = "current-io-token",
                properties = mapOf("type" to "current-io")
            )
            SessionContextHolder.context = currentContext

            val result = withCurrentSessionContextIO {
                assertEquals("current-io-token", SessionContextHolder.currentToken)
                assertEquals("current-io", SessionContextHolder.getSessionProperty("type"))

                assertTrue(coroutineContext[CoroutineDispatcher.Key] == Dispatchers.IO)

                "current-io-success"
            }

            assertEquals("current-io-success", result)
        }

        @Test
        fun `should preserve context across suspension points in IO`() = runTest {
            val testContext = SessionContext.create(
                token = "io-suspension-token",
                properties = mapOf("test" to "suspension")
            )

            withSessionContextIO(testContext) {
                assertEquals("io-suspension-token", SessionContextHolder.currentToken)
                assertEquals("suspension", SessionContextHolder.getSessionProperty("test"))

                delay(50)

                assertEquals("io-suspension-token", SessionContextHolder.currentToken)
                assertEquals("suspension", SessionContextHolder.getSessionProperty("test"))
                assertTrue(coroutineContext[CoroutineDispatcher.Key] == Dispatchers.IO)
            }
        }

        @Test
        fun `should handle exceptions in IO context properly`() = runTest {
            val testContext = SessionContext.create(token = "io-exception-token")

            try {
                withSessionContextIO(testContext) {
                    assertEquals("io-exception-token", SessionContextHolder.currentToken)
                    assertTrue(coroutineContext[CoroutineDispatcher.Key] == Dispatchers.IO)
                    throw RuntimeException("IO test exception")
                }
            } catch (e: RuntimeException) {
                assertEquals("IO test exception", e.message)
            }
        }

        @Test
        fun `should work with concurrent IO operations`() = runTest {
            val contexts = (1..5).map { index ->
                SessionContext.create(
                    token = "io-concurrent-$index",
                    properties = mapOf("id" to index.toString())
                )
            }

            val results = contexts.mapIndexed { index, context ->
                async {
                    withSessionContextIO(context) {
                        delay(10L * (index + 1))

                        val token = SessionContextHolder.currentToken
                        val id = SessionContextHolder.getSessionProperty("id")
                        val dispatcher = coroutineContext[CoroutineDispatcher.Key]

                        Triple(token, id, dispatcher == Dispatchers.IO)
                    }
                }
            }

            val completedResults = results.awaitAll()

            completedResults.forEachIndexed { index, (token, id, isIODispatcher) ->
                assertEquals("io-concurrent-${index + 1}", token)
                assertEquals("${index + 1}", id)
                assertTrue(isIODispatcher)
            }
        }

        @Test
        fun `should work with empty current context in IO`() = runTest {
            SessionContextHolder.clearContext()

            withCurrentSessionContextIO {
                assertNull(SessionContextHolder.currentToken)
                assertEquals(Locale.ROOT, SessionContextHolder.currentLocale)
                assertTrue(coroutineContext[CoroutineDispatcher.Key] == Dispatchers.IO)
            }
        }
    }

    @Nested
    @DisplayName("Dispatchers.VT SessionContext Methods")
    inner class DispatchersVTTests {

        @Test
        fun `should execute withSessionContextVT with correct dispatcher and context`() = runTest {
            val testContext = SessionContext.create(
                user = mockUser,
                token = "vt-token",
                locale = Locale.KOREAN,
                properties = mapOf("dispatcher" to "vt")
            )

            val result = withSessionContextVT(testContext) {
                assertEquals(mockUser, SessionContextHolder.currentUser)
                assertEquals("vt-token", SessionContextHolder.currentToken)
                assertEquals(Locale.KOREAN, SessionContextHolder.currentLocale)
                assertEquals("vt", SessionContextHolder.getSessionProperty("dispatcher"))

                // Verify we're using a virtual thread dispatcher (not IO or Default)
                val dispatcher = coroutineContext[CoroutineDispatcher.Key]
                assertNotNull(dispatcher)
                assertTrue(dispatcher != Dispatchers.IO)
                assertTrue(dispatcher != Dispatchers.Default)

                "vt-success"
            }

            assertEquals("vt-success", result)
        }

        @Test
        fun `should execute withCurrentSessionContextVT with current context`() = runTest {
            val currentContext = SessionContext.create(
                token = "current-vt-token",
                properties = mapOf("type" to "current-vt")
            )
            SessionContextHolder.context = currentContext

            val result = withCurrentSessionContextVT {
                assertEquals("current-vt-token", SessionContextHolder.currentToken)
                assertEquals("current-vt", SessionContextHolder.getSessionProperty("type"))

                val dispatcher = coroutineContext[CoroutineDispatcher.Key]
                assertNotNull(dispatcher)
                assertTrue(dispatcher != Dispatchers.IO)
                assertTrue(dispatcher != Dispatchers.Default)

                "current-vt-success"
            }

            assertEquals("current-vt-success", result)
        }

        @Test
        fun `should preserve context across suspension points in VT`() = runTest {
            val testContext = SessionContext.create(
                token = "vt-suspension-token",
                properties = mapOf("test" to "vt-suspension")
            )

            withSessionContextVT(testContext) {
                assertEquals("vt-suspension-token", SessionContextHolder.currentToken)
                assertEquals("vt-suspension", SessionContextHolder.getSessionProperty("test"))

                delay(30)

                assertEquals("vt-suspension-token", SessionContextHolder.currentToken)
                assertEquals("vt-suspension", SessionContextHolder.getSessionProperty("test"))

                val dispatcher = coroutineContext[CoroutineDispatcher.Key]
                assertNotNull(dispatcher)
                assertTrue(dispatcher != Dispatchers.IO)
                assertTrue(dispatcher != Dispatchers.Default)
            }
        }

        @Test
        fun `should handle exceptions in VT context properly`() = runTest {
            val testContext = SessionContext.create(token = "vt-exception-token")

            try {
                withSessionContextVT(testContext) {
                    assertEquals("vt-exception-token", SessionContextHolder.currentToken)

                    val dispatcher = coroutineContext[CoroutineDispatcher.Key]
                    assertNotNull(dispatcher)
                    assertTrue(dispatcher != Dispatchers.IO)

                    throw IllegalStateException("VT test exception")
                }
            } catch (e: IllegalStateException) {
                assertEquals("VT test exception", e.message)
            }
        }

        @Test
        fun `should work with concurrent VT operations`() = runTest {
            val contexts = (1..3).map { index ->
                SessionContext.create(
                    token = "vt-concurrent-$index",
                    properties = mapOf("vt-id" to index.toString())
                )
            }

            val results = contexts.mapIndexed { index, context ->
                async {
                    withSessionContextVT(context) {
                        delay(15L * (index + 1))

                        val token = SessionContextHolder.currentToken
                        val id = SessionContextHolder.getSessionProperty("vt-id")
                        val dispatcher = coroutineContext[CoroutineDispatcher.Key]

                        Triple(token, id, dispatcher != Dispatchers.IO && dispatcher != Dispatchers.Default)
                    }
                }
            }

            val completedResults = results.awaitAll()

            completedResults.forEachIndexed { index, (token, id, isVTDispatcher) ->
                assertEquals("vt-concurrent-${index + 1}", token)
                assertEquals("${index + 1}", id)
                assertTrue(isVTDispatcher, "Should be using VT dispatcher")
            }
        }

        @Test
        fun `should work with empty current context in VT`() = runTest {
            SessionContextHolder.clearContext()

            withCurrentSessionContextVT {
                assertNull(SessionContextHolder.currentToken)
                assertEquals(Locale.ROOT, SessionContextHolder.currentLocale)

                val dispatcher = coroutineContext[CoroutineDispatcher.Key]
                assertNotNull(dispatcher)
                assertTrue(dispatcher != Dispatchers.IO)
                assertTrue(dispatcher != Dispatchers.Default)
            }
        }

        @Test
        fun `should handle VT dispatcher resource management`() = runTest {
            val testContext = SessionContext.create(token = "vt-resource-test")

            val result = withSessionContextVT(testContext) {
                val results = (1..5).map { index ->
                    async {
                        delay(5L)
                        "vt-result-$index"
                    }
                }

                results.awaitAll()
            }

            assertEquals(listOf("vt-result-1", "vt-result-2", "vt-result-3", "vt-result-4", "vt-result-5"), result)
        }
    }

    @Nested
    @DisplayName("Dispatcher Integration Tests")
    inner class DispatcherIntegrationTests {

        @Test
        fun `should combine IO and VT operations with different contexts`() = runTest {
            val ioContext = SessionContext.create(
                token = "io-integration",
                properties = mapOf("type" to "io")
            )

            val vtContext = SessionContext.create(
                token = "vt-integration",
                properties = mapOf("type" to "vt")
            )

            val ioResult = async {
                withSessionContextIO(ioContext) {
                    assertEquals("io-integration", SessionContextHolder.currentToken)
                    assertEquals("io", SessionContextHolder.getSessionProperty("type"))
                    "io-completed"
                }
            }

            val vtResult = async {
                withSessionContextVT(vtContext) {
                    assertEquals("vt-integration", SessionContextHolder.currentToken)
                    assertEquals("vt", SessionContextHolder.getSessionProperty("type"))
                    "vt-completed"
                }
            }

            assertEquals("io-completed", ioResult.await())
            assertEquals("vt-completed", vtResult.await())
        }

        @Test
        fun `should maintain context isolation between different dispatcher methods`() = runTest {
            val baseContext = SessionContext.create(token = "base-context")
            SessionContextHolder.context = baseContext

            withCurrentSessionContextIO {
                assertEquals("base-context", SessionContextHolder.currentToken)
                assertTrue(coroutineContext[CoroutineDispatcher.Key] == Dispatchers.IO)
            }

            withCurrentSessionContextVT {
                assertEquals("base-context", SessionContextHolder.currentToken)
                val dispatcher = coroutineContext[CoroutineDispatcher.Key]
                assertNotNull(dispatcher)
                assertTrue(dispatcher != Dispatchers.IO)
                assertTrue(dispatcher != Dispatchers.Default)
            }

            assertEquals("base-context", SessionContextHolder.currentToken)
        }

        @Test
        fun `should handle nested dispatcher context operations`() = runTest {
            val outerContext = SessionContext.create(token = "outer")
            val innerIOContext = SessionContext.create(token = "inner-io")
            val innerVTContext = SessionContext.create(token = "inner-vt")

            withSessionContext(outerContext) {
                assertEquals("outer", SessionContextHolder.currentToken)

                withSessionContextIO(innerIOContext) {
                    assertEquals("inner-io", SessionContextHolder.currentToken)
                    assertTrue(coroutineContext[CoroutineDispatcher.Key] == Dispatchers.IO)

                    withSessionContextVT(innerVTContext) {
                        assertEquals("inner-vt", SessionContextHolder.currentToken)
                        val dispatcher = coroutineContext[CoroutineDispatcher.Key]
                        assertNotNull(dispatcher)
                        assertTrue(dispatcher != Dispatchers.IO)
                        assertTrue(dispatcher != Dispatchers.Default)
                    }

                    assertEquals("inner-io", SessionContextHolder.currentToken)
                    assertTrue(coroutineContext[CoroutineDispatcher.Key] == Dispatchers.IO)
                }

                assertEquals("outer", SessionContextHolder.currentToken)
            }
        }

        @Test
        fun `should test return type consistency across all dispatcher methods`() = runTest {
            val testContext = SessionContext.create(token = "return-type-test")

            val ioStringResult: String = withSessionContextIO(testContext) { "io-string" }
            assertEquals("io-string", ioStringResult)

            val ioIntResult: Int = withCurrentSessionContextIO { 42 }
            assertEquals(42, ioIntResult)

            val vtStringResult: String = withSessionContextVT(testContext) { "vt-string" }
            assertEquals("vt-string", vtStringResult)

            val vtIntResult: Int = withCurrentSessionContextVT { 24 }
            assertEquals(24, vtIntResult)

            data class DispatcherResult(val dispatcher: String, val token: String?)

            val ioComplexResult: DispatcherResult = withSessionContextIO(testContext) {
                DispatcherResult("IO", SessionContextHolder.currentToken)
            }
            assertEquals(DispatcherResult("IO", "return-type-test"), ioComplexResult)

            val vtComplexResult: DispatcherResult = withSessionContextVT(testContext) {
                DispatcherResult("VT", SessionContextHolder.currentToken)
            }
            assertEquals(DispatcherResult("VT", "return-type-test"), vtComplexResult)
        }
    }
}
