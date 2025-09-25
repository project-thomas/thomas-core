package com.thomas.core.model.stream

import com.thomas.core.util.DateUtils.randomZonedDateTime
import com.thomas.core.util.NumberUtils.randomInteger
import com.thomas.core.util.StringUtils.randomString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class StreamDataTest {

    @ParameterizedTest
    @EnumSource(StreamType::class)
    fun `properties, destructuring and toString`(type: StreamType) {
        val ts = randomZonedDateTime()
        val key = randomString()
        val data = listOf(randomInteger(), randomInteger(), randomInteger())
        val stream = StreamData(
            eventType = type,
            eventTimestamp = ts,
            eventKey = key,
            eventData = data,
        )

        assertEquals(type, stream.eventType)
        assertEquals(ts, stream.eventTimestamp)
        assertEquals(key, stream.eventKey)
        assertEquals(data, stream.eventData)

        val (streamType, time, streamKey, eventData) = stream
        assertEquals(type, streamType)
        assertEquals(ts, time)
        assertEquals(key, streamKey)
        assertEquals(data, eventData)

        val expected = "StreamData(eventType=$type, eventTimestamp=$ts, eventKey=$key, eventData=$data)"
        assertEquals(expected, stream.toString())
    }

    @ParameterizedTest
    @EnumSource(StreamType::class)
    fun `copy and equals contract`(type: StreamType) {
        val ts = randomZonedDateTime()
        val key = randomInteger()
        val data = CustomData(randomString(), randomInteger())
        val base = StreamData(
            eventType = type,
            eventTimestamp = ts,
            eventKey = key,
            eventData = data,
        )

        val same = base.copy()
        assertEquals(base, same)
        assertEquals(base.hashCode(), same.hashCode())

        assertFalse(base.equals(null))
        assertFalse(base.equals("not a StreamData"))

        val diffType = base.copy(eventType = StreamType.entries.shuffled().first { it != type })
        assertNotEquals(base, diffType)

        val diffTimestamp = base.copy(eventTimestamp = ts.plusSeconds(1))
        assertNotEquals(base, diffTimestamp)

        val diffKey = base.copy(eventKey = key + 1)
        assertNotEquals(base, diffKey)

        val diffData = base.copy(eventData = CustomData(randomString(), randomInteger()))
        assertNotEquals(base, diffData)
    }

    data class CustomData(val name: String, val value: Int)
}
