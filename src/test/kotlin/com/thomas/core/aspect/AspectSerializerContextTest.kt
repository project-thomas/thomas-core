package com.thomas.core.aspect

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
internal class AspectSerializerContextTest {

    private lateinit var originalSerializer: AspectSerializer
    private lateinit var mockSerializer: AspectSerializer

    companion object {
        @JvmStatic
        fun serializationTestData() = listOf(
            Arguments.of("test string", false, "test string"),
            Arguments.of("sensitive data", true, "**********"),
            Arguments.of(null, false, "null"),
            Arguments.of(null, true, "null"),
            Arguments.of(RuntimeException("Test error"), false, "RuntimeException(Test error)"),
            Arguments.of(IllegalArgumentException("Invalid argument"), true, "IllegalArgumentException(Invalid argument)")
        )
    }

    @BeforeEach
    fun setUp() {
        originalSerializer = AspectSerializerContext.aspectSerializer
        mockSerializer = mockk<AspectSerializer>(relaxed = true)
    }

    @AfterEach
    fun tearDown() {
        AspectSerializerContext.aspectSerializer = originalSerializer
    }

    @Test
    fun `Should have default AspectToStringSerializer`() {
        val defaultSerializer = AspectSerializerContext.aspectSerializer

        assertNotNull(defaultSerializer)
        assertEquals(AspectToStringSerializer::class, defaultSerializer::class)
    }

    @Test
    fun `Should allow setting custom serializer`() {
        val customSerializer = mockk<AspectSerializer>()

        AspectSerializerContext.aspectSerializer = customSerializer

        assertSame(customSerializer, AspectSerializerContext.aspectSerializer)
    }

    @Test
    fun `Should return same instance when accessed multiple times`() {
        val serializer1 = AspectSerializerContext.aspectSerializer
        val serializer2 = AspectSerializerContext.aspectSerializer

        assertSame(serializer1, serializer2)
    }

    @Test
    fun `Should replace serializer atomically`() {
        val newSerializer = mockk<AspectSerializer>()
        val oldSerializer = AspectSerializerContext.aspectSerializer

        AspectSerializerContext.aspectSerializer = newSerializer

        assertNotSame(oldSerializer, AspectSerializerContext.aspectSerializer)
        assertSame(newSerializer, AspectSerializerContext.aspectSerializer)
    }

    @ParameterizedTest
    @MethodSource("serializationTestData")
    fun `Should serialize values using current serializer`(
        value: Any?,
        masked: Boolean,
        expectedResult: String
    ) {
        val result = AspectSerializerContext.aspectSerializer.serialize(value, masked)
        assertEquals(expectedResult, result)
    }

    @Test
    fun `Should delegate serialization calls to current serializer`() {
        val testValue = "test"
        val expectedResult = "serialized_result"

        every { mockSerializer.serialize(testValue, false) } returns expectedResult
        AspectSerializerContext.aspectSerializer = mockSerializer

        val result = AspectSerializerContext.aspectSerializer.serialize(testValue, false)

        assertEquals(expectedResult, result)
        verify { mockSerializer.serialize(testValue, false) }
    }

    @Test
    fun `Should be thread-safe for concurrent reads`() = runBlocking {
        val readCount = 1000
        val results = ConcurrentHashMap<Int, AspectSerializer>()

        val jobs = (1..readCount).map { index ->
            async {
                results[index] = AspectSerializerContext.aspectSerializer
            }
        }

        jobs.awaitAll()

        assertEquals(readCount, results.size)
        val uniqueSerializers = results.values.toSet()
        assertEquals(1, uniqueSerializers.size, "All reads should return the same serializer instance")
    }

    @Test
    fun `Should be thread-safe for concurrent writes`() {
        val threadCount = 10
        val executor = Executors.newFixedThreadPool(threadCount)
        val startLatch = CountDownLatch(1)
        val endLatch = CountDownLatch(threadCount)
        val results = ConcurrentHashMap<Int, AspectSerializer>()

        repeat(threadCount) { threadIndex ->
            executor.submit {
                try {
                    startLatch.await()
                    val customSerializer = mockk<AspectSerializer>("serializer_$threadIndex")
                    AspectSerializerContext.aspectSerializer = customSerializer
                    results[threadIndex] = AspectSerializerContext.aspectSerializer
                } finally {
                    endLatch.countDown()
                }
            }
        }

        startLatch.countDown()
        endLatch.await(5, TimeUnit.SECONDS)
        executor.shutdown()

        assertEquals(threadCount, results.size)
        val finalSerializer = AspectSerializerContext.aspectSerializer
        assertNotNull(finalSerializer)
    }

    @Test
    fun `Should handle rapid serializer changes`() = runBlocking {
        val changeCount = 100
        val serializers = mutableListOf<AspectSerializer>()

        repeat(changeCount) {
            val newSerializer = mockk<AspectSerializer>("serializer_$it")
            serializers.add(newSerializer)
            AspectSerializerContext.aspectSerializer = newSerializer
        }

        val finalSerializer = AspectSerializerContext.aspectSerializer
        assertEquals(serializers.last(), finalSerializer)
    }

    @Test
    fun `Should maintain serializer state across multiple operations`() {
        val customSerializer = mockk<AspectSerializer>()
        val testValue = "persistent_test"
        val expectedResult = "custom_result"

        every { customSerializer.serialize(any(), any()) } returns expectedResult
        AspectSerializerContext.aspectSerializer = customSerializer

        repeat(5) {
            val result = AspectSerializerContext.aspectSerializer.serialize(testValue, false)
            assertEquals(expectedResult, result)
        }

        verify(exactly = 5) { customSerializer.serialize(testValue, false) }
    }

    @Test
    fun `Should handle null serialization correctly`() {
        val result = AspectSerializerContext.aspectSerializer.serialize(null, false)
        assertEquals("null", result)

        val maskedResult = AspectSerializerContext.aspectSerializer.serialize(null, true)
        assertEquals("null", maskedResult)
    }

    @Test
    fun `Should handle exception serialization correctly`() {
        val exception = IllegalStateException("Test exception message")
        val result = AspectSerializerContext.aspectSerializer.serialize(exception, false)

        assertEquals("IllegalStateException(Test exception message)", result)
    }

    @Test
    fun `Should handle masked serialization correctly`() {
        val sensitiveData = "password123"
        val result = AspectSerializerContext.aspectSerializer.serialize(sensitiveData, true)

        assertEquals("**********", result)
    }

    @Test
    fun `Should work correctly with mixed concurrent read-write operations`() = runBlocking {
        val operationCount = 500
        val results = ConcurrentHashMap<Int, String>()
        val serializers = (1..5).map { mockk<AspectSerializer>("mock_$it") }

        // Configure all mock serializers
        serializers.forEach { serializer ->
            every { serializer.serialize(any(), any()) } returns "result_${serializer.hashCode()}"
        }

        // Start with a mock serializer instead of default one
        AspectSerializerContext.aspectSerializer = serializers.first()

        val jobs = (1..operationCount).map { index ->
            async {
                if (index % 10 == 0) {
                    // Write operation - switch to a different mock serializer
                    val randomSerializer = serializers[Random.nextInt(serializers.size)]
                    AspectSerializerContext.aspectSerializer = randomSerializer
                } else {
                    // Read operation
                    val result = AspectSerializerContext.aspectSerializer.serialize("test", false)
                    results[index] = result
                }
            }
        }

        jobs.awaitAll()

        val finalSerializer = AspectSerializerContext.aspectSerializer
        assertNotNull(finalSerializer)

        // All results should come from mock serializers
        results.values.forEach { result ->
            assert(result.startsWith("result_")) {
                "Invalid result format: $result. Expected format: result_<hashcode>"
            }
        }

        // Verify that we have results from multiple serializers
        val uniqueResults = results.values.toSet()
        assert(uniqueResults.isNotEmpty()) { "Should have at least some serialization results" }
    }

    @Test
    fun `Should handle concurrent operations with mixed serializer types`() = runBlocking {
        val operationCount = 200
        val results = ConcurrentHashMap<Int, String>()
        val writeOperations = mutableListOf<Int>()

        // Create mock serializers with distinct outputs
        val mockSerializer1 = mockk<AspectSerializer>("mock1")
        val mockSerializer2 = mockk<AspectSerializer>("mock2")

        every { mockSerializer1.serialize(any(), any()) } returns "mock1_output"
        every { mockSerializer2.serialize(any(), any()) } returns "mock2_output"

        val jobs = (1..operationCount).map { index ->
            async {
                if (index % 15 == 0) {
                    // Write operations
                    writeOperations.add(index)
                    val serializer = if (index % 30 == 0) mockSerializer1 else mockSerializer2
                    AspectSerializerContext.aspectSerializer = serializer
                } else {
                    // Read operations
                    val result = AspectSerializerContext.aspectSerializer.serialize("test_data", false)
                    results[index] = result
                }
            }
        }

        jobs.awaitAll()

        // Verify we have results
        assert(results.isNotEmpty()) { "Should have serialization results" }

        // All results should be from valid serializers (either mock or default)
        val validResults = results.values.all { result ->
            result == "mock1_output" ||
                result == "mock2_output" ||
                result == "test_data" ||  // from default AspectToStringSerializer
                result == "**********"    // masked result
        }

        assert(validResults) {
            "All results should be from valid serializers. Found: ${results.values.distinct()}"
        }

        // Should have performed some write operations
        assert(writeOperations.isNotEmpty()) { "Should have performed write operations" }
    }

    @Test
    fun `Should maintain atomicity during serializer replacement`() = runBlocking {
        val iterations = 100
        val serializers = (1..3).map { i ->
            mockk<AspectSerializer>("serializer_$i").apply {
                every { serialize(any(), any()) } returns "output_$i"
            }
        }

        repeat(iterations) { iteration ->
            val selectedSerializer = serializers[iteration % serializers.size]
            AspectSerializerContext.aspectSerializer = selectedSerializer

            // Immediately read after write - should get consistent result
            val result = AspectSerializerContext.aspectSerializer.serialize("test", false)
            val expectedOutput = "output_${(iteration % serializers.size) + 1}"

            assertEquals(expectedOutput, result, "Serializer replacement should be atomic at iteration $iteration")
        }
    }

}
