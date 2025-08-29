package com.thomas.core.extension

import com.thomas.core.util.BooleanUtils.randomBoolean
import com.thomas.core.util.NumberUtils.randomBigDecimal
import com.thomas.core.util.NumberUtils.randomInteger
import com.thomas.core.util.StringUtils.randomString
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType
import java.math.BigDecimal
import java.util.Optional
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class TypeExtensionTest {

    companion object {

        @JvmStatic
        fun classTypes() = listOf(
            Arguments.of(String::class.java, "String"),
            Arguments.of(Int::class.java, "int"),
            Arguments.of(Integer::class.java, "Integer"),
            Arguments.of(Boolean::class.java, "boolean"),
            Arguments.of(java.lang.Boolean::class.java, "Boolean"),
            Arguments.of(BigDecimal::class.java, "BigDecimal"),
            Arguments.of(List::class.java, "List"),
            Arguments.of(Map::class.java, "Map"),
            Arguments.of(Set::class.java, "Set"),
            Arguments.of(UUID::class.java, "UUID"),
            Arguments.of(TestDataClass::class.java, "TestDataClass"),
            Arguments.of(Array<String>::class.java, "String[]"),
        )

        @JvmStatic
        fun parameterizedTypes() = listOf(
            Arguments.of(getParameterizedType<List<String>>(), "List<String>"),
            Arguments.of(getParameterizedType<Map<String, Int>>(), "Map<String, Integer>"),
            Arguments.of(getParameterizedType<Set<Boolean>>(), "Set<Boolean>"),
            Arguments.of(getParameterizedType<List<Map<String, Any>>>(), "List<Map<String, Any>>"),
            Arguments.of(getParameterizedType<Map<String, List<Int>>>(), "Map<String, List<Integer>>"),
            Arguments.of(getParameterizedType<Pair<String, TestDataClass>>(), "Pair<String, TestDataClass>"),
        )

        private inline fun <reified T> getParameterizedType(): Type {
            return object : TypeReference<T>() {}.type
        }

        private abstract class TypeReference<T> {
            val type: Type = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
        }

    }

    @ParameterizedTest
    @MethodSource("classTypes")
    fun `Simple typed name for Class types`(type: Type, expectedName: String) {
        val result = type.simpleTypedName()
        assertEquals(expectedName, result)
    }

    @ParameterizedTest
    @MethodSource("parameterizedTypes")
    fun `Simple typed name for ParameterizedType`(type: Type, expectedName: String) {
        val result = type.simpleTypedName()
        assertEquals(expectedName, result)
    }

    @Test
    fun `Simple typed name for nested ParameterizedType`() {
        val type = getParameterizedType<Map<String, List<Map<Int, Boolean>>>>()
        val result = type.simpleTypedName()
        assertEquals("Map<String, List<Map<Integer, Boolean>>>", result)
    }

    @Test
    fun `Simple typed name for complex generic type`() {
        val type = getParameterizedType<Triple<List<String>, Map<Int, Boolean>, Set<TestDataClass>>>()
        val result = type.simpleTypedName()
        assertEquals("Triple<List<String>, Map<Integer, Boolean>, Set<TestDataClass>>", result)
    }

    @Test
    fun `Simple typed name for TypeVariable`() {
        val method = GenericTestClass::class.java.getDeclaredMethod("genericMethod")
        val typeVariable = method.genericReturnType as TypeVariable<*>

        val result = typeVariable.simpleTypedName()
        // TypeVariable geralmente retorna o nome da variável ou toString
        assertEquals("T", result)
    }

    @Test
    fun `Simple typed name for WildcardType with upper bound`() {
        val method = GenericTestClass::class.java.methods.first { it.name == "wildcardMethod" }
        val parameterizedType = method.genericParameterTypes[0] as ParameterizedType
        val wildcardType = parameterizedType.actualTypeArguments[0] as WildcardType

        val result = wildcardType.simpleTypedName()
        assertEquals("Number", result)
    }

    @Test
    fun `Simple typed name for WildcardType with lower bound`() {
        val method = GenericTestClass::class.java.methods.first { it.name == "wildcardLowerMethod" }
        val parameterizedType = method.genericParameterTypes[0] as ParameterizedType
        val wildcardType = parameterizedType.actualTypeArguments[0] as WildcardType

        val result = wildcardType.simpleTypedName()
        // WildcardType com lower bound
        assertEquals("Integer", result)
    }

    @Test
    fun `Simple typed name for unbounded WildcardType`() {
        val method = GenericTestClass::class.java.methods.first { it.name == "unboundedWildcardMethod" }
        val parameterizedType = method.genericParameterTypes[0] as ParameterizedType
        val wildcardType = parameterizedType.actualTypeArguments[0] as WildcardType

        val result = wildcardType.simpleTypedName()
        // WildcardType sem bounds específicos
        assertEquals("Any", result)
    }

    @Test
    fun `Simple typed name for GenericArrayType`() {
        val method = GenericTestClass::class.java.methods.first { it.name == "genericArrayMethod" }
        val genericArrayType = method.genericParameterTypes[0] as GenericArrayType

        val result = genericArrayType.simpleTypedName()
        assertEquals("T[]", result)
    }

    @Test
    fun `Simple typed name for primitive array`() {
        val intArrayType = Array<Int>::class.java
        val result = intArrayType.simpleTypedName()
        assertEquals("Integer[]", result)
    }

    @Test
    fun `Simple typed name for custom type with toString fallback`() {
        val customType = object : Type {
            override fun toString(): String = "com.example.custom.CustomType"
        }

        val result = customType.simpleTypedName()
        assertEquals("CustomType", result)
    }

    @Test
    fun `Simple typed name for custom type with simple toString`() {
        val customType = object : Type {
            override fun toString(): String = "SimpleType"
        }

        val result = customType.simpleTypedName()
        assertEquals("SimpleType", result)
    }

    @Test
    fun `Simple typed name for empty ParameterizedType`() {
        // Simulando um ParameterizedType sem argumentos de tipo
        val mockParameterizedType = object : ParameterizedType {
            override fun getActualTypeArguments(): Array<Type> = emptyArray()
            override fun getRawType(): Type = List::class.java
            override fun getOwnerType(): Type? = null
            override fun toString(): String = "List"
        }

        val result = mockParameterizedType.simpleTypedName()
        assertEquals("List", result)
    }

    @Test
    fun `Simple typed name for single type argument ParameterizedType`() {
        val type = getParameterizedType<Optional<String>>()
        val result = type.simpleTypedName()
        assertEquals("Optional<String>", result)
    }

    @Test
    fun `Simple typed name for void type`() {
        val voidType = Void.TYPE
        val result = voidType.simpleTypedName()
        assertEquals("void", result)
    }

    @Test
    fun `Simple typed name for Void wrapper type`() {
        val voidWrapperType = Void::class.java
        val result = voidWrapperType.simpleTypedName()
        assertEquals("Void", result)
    }

    private data class TestDataClass(
        val name: String = randomString(),
        val value: Int = randomInteger(),
        val active: Boolean = randomBoolean(),
        val amount: BigDecimal = randomBigDecimal()
    )

    @Suppress("unused")
    private class GenericTestClass<T> {

        fun genericMethod(): T = TODO()

        fun wildcardMethod(list: List<out Number>): Unit = TODO()

        fun wildcardLowerMethod(consumer: Consumer<in Integer>): Unit = TODO()

        fun unboundedWildcardMethod(list: List<*>): Unit = TODO()

        fun genericArrayMethod(array: Array<T>): Unit = TODO()
    }

    private interface Consumer<in T>
}
