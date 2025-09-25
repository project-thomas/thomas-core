package com.thomas.core.model.stream

import java.time.ZonedDateTime

data class StreamData<K, T>(
    val eventType: StreamType,
    val eventTimestamp: ZonedDateTime,
    val eventKey: K,
    val eventData: T,
)
