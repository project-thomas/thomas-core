package com.thomas.core.extension

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode

@Execution(ExecutionMode.CONCURRENT)
internal class UUIDExtensionTest {

    @Test
    fun `randomUUIDv7 should generate valid UUID`() {
        val uuid = randomUUIDv7()
        assertTrue(uuid.toString().matches(Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")))
    }

    @Test
    fun `randomUUIDv7 should generate UUID version 7`() {
        val uuid = randomUUIDv7()
        val version = (uuid.mostSignificantBits shr 12) and 0xF
        assertEquals(7, version, "UUID should be version 7")
    }

    @Test
    fun `randomUUIDv7 should have correct variant bits`() {
        val uuid = randomUUIDv7()
        val variant = (uuid.leastSignificantBits shr 62) and 0x3
        assertEquals(2, variant, "UUID should have variant bits set to 10 (binary)")
    }

    @Test
    fun `randomUUIDv7 should embed current timestamp in first 48 bits`() {
        val beforeTimestamp = System.currentTimeMillis()
        val uuid = randomUUIDv7()
        val afterTimestamp = System.currentTimeMillis()
        val embeddedTimestamp = uuid.mostSignificantBits shr 16

        assertTrue(embeddedTimestamp >= beforeTimestamp, "Embedded timestamp should be >= generation time")
        assertTrue(embeddedTimestamp <= afterTimestamp, "Embedded timestamp should be <= generation time")
    }

    @RepeatedTest(10)
    fun `randomUUIDv7 should generate unique UUIDs`() {
        val uuid1 = randomUUIDv7()
        val uuid2 = randomUUIDv7()
        assertNotEquals(uuid1, uuid2, "Each generated UUID should be unique")
    }

    @Test
    fun `randomUUIDv7 should maintain temporal ordering`() {
        val uuids = mutableListOf<UUID>()
        repeat(10) {
            uuids.add(randomUUIDv7())
            Thread.sleep(2) // 2ms delay to ensure different timestamps
        }
        val timestamps = uuids.map { it.mostSignificantBits shr 16 }
        val sortedTimestamps = timestamps.sorted()
        assertEquals(sortedTimestamps, timestamps, "UUIDs should maintain temporal ordering based on embedded timestamps")
    }

    @Test
    fun `randomUUIDv7 should be thread-safe under concurrent access`() {
        val threadCount = 50
        val uuidsPerThread = 100
        val allUUIDs = ConcurrentHashMap.newKeySet<UUID>()
        val latch = CountDownLatch(threadCount)
        val executor = Executors.newFixedThreadPool(threadCount)
        val collisionCount = AtomicInteger(0)

        repeat(threadCount) {
            executor.submit {
                try {
                    val threadUUIDs = mutableSetOf<UUID>()
                    repeat(uuidsPerThread) {
                        val uuid = randomUUIDv7()
                        if (!threadUUIDs.add(uuid)) {
                            collisionCount.incrementAndGet()
                        }
                        if (!allUUIDs.add(uuid)) {
                            collisionCount.incrementAndGet()
                        }
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS), "All threads should complete within timeout")
        executor.shutdown()

        val totalExpectedUUIDs = threadCount * uuidsPerThread
        assertEquals(totalExpectedUUIDs, allUUIDs.size, "All generated UUIDs should be unique (no collisions)")
        assertEquals(0, collisionCount.get(), "No UUID collisions should occur in concurrent generation")
    }

    @Test
    fun `randomUUIDv7 should have proper bit field distribution`() {
        val uuid = randomUUIDv7()
        val msb = uuid.mostSignificantBits
        val lsb = uuid.leastSignificantBits

        val timestamp = msb shr 16
        assertTrue(timestamp > 0, "Timestamp field should not be zero")

        val version = (msb shr 12) and 0xF
        assertEquals(7, version, "Version field should be 7")

        val variant = (lsb shr 62) and 0x3
        assertEquals(2, variant, "Variant field should be 10 binary")

        val counterAndRandA = msb and 0xFFF
        val randB = lsb and 0x3FFFFFFFFFFFFFFFL

        assertTrue(counterAndRandA != 0L || randB != 0L, "Random fields should contain some entropy")
    }

}
