package com.thomas.core.aspect

import com.thomas.core.util.BooleanUtils.randomBoolean
import com.thomas.core.util.NumberUtils.randomBigDecimal
import com.thomas.core.util.NumberUtils.randomBigInteger
import com.thomas.core.util.NumberUtils.randomDouble
import com.thomas.core.util.NumberUtils.randomInteger
import com.thomas.core.util.NumberUtils.randomLong
import com.thomas.core.util.StringUtils.randomString
import java.util.UUID.randomUUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class AspectToStringSerializerTest {

    private val serializer = AspectToStringSerializer()

    companion object {

        @JvmStatic
        fun nonNullValues() = listOf(
            randomString().let { Arguments.of(it, it) },
            randomInteger().let { Arguments.of(it, it.toString()) },
            randomLong().let { Arguments.of(it, it.toString()) },
            randomDouble().let { Arguments.of(it, it.toString()) },
            randomBoolean().let { Arguments.of(it, it.toString()) },
            randomBigInteger().let { Arguments.of(it, it.toString()) },
            randomBigDecimal().let { Arguments.of(it, it.toString()) },
            randomUUID().let { Arguments.of(it, it.toString()) },
            TestDataClass(randomString(), randomInteger()).let { Arguments.of(it, it.toString()) },
            mapOf("key" to "value").let { Arguments.of(it, it.toString()) },
            emptyList<String>().let { Arguments.of(it, it.toString()) },
            listOf(randomString(), randomString(), randomString()).let { Arguments.of(it, it.toString()) },
        )

    }

    @Test
    fun `Serialize null value without masking`() {
        val result = serializer.serialize(value = null, masked = false)
        assertEquals("null", result)
    }

    @Test
    fun `Serialize null value with masking`() {
        val result = serializer.serialize(value = null, masked = true)
        assertEquals("null", result)
    }

    @ParameterizedTest
    @MethodSource("nonNullValues")
    fun `Serialize non-null values without masking`(value: Any, expectedString: String) {
        val result = serializer.serialize(value = value, masked = false)
        assertEquals(expectedString, result)
    }

    @ParameterizedTest
    @MethodSource("nonNullValues")
    fun `Serialize non-null values with masking`(value: Any) {
        val result = serializer.serialize(value = value, masked = true)
        assertEquals("**********", result)
    }

    private data class TestDataClass(
        val name: String,
        val value: Int
    )

}
