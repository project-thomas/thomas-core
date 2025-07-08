package com.thomas.core.extension

import com.thomas.core.model.entity.BaseEntity
import com.thomas.core.model.entity.DeferredEntityValidation
import com.thomas.core.model.entity.DeferredEntityValidationContext.Companion.EMPTY
import com.thomas.core.model.entity.DeferredEntityValidationContext.Companion.IO
import com.thomas.core.model.entity.DeferredEntityValidationContext.Companion.VT
import com.thomas.core.model.entity.EntityValidationException
import com.thomas.core.util.StringUtils.randomString
import java.util.UUID
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class BaseEntityExtensionTest {

    companion object {

        private const val ERROR_MESSAGE_01 = "Name is invalid"
        private const val ERROR_MESSAGE_02 = "Name is too long"
        private const val ERROR_MESSAGE_03 = "Name is too short"
        private const val ERROR_MESSAGE_04 = "Invalid e-mail"

        @JvmStatic
        fun entities() = listOf(
            Arguments.of(TestEntity(name = "    "), TestEntity::name.name, listOf(ERROR_MESSAGE_01)),
            Arguments.of(TestEntity(name = "98765432100"), TestEntity::name.name, listOf(ERROR_MESSAGE_02)),
            Arguments.of(TestEntity(name = "A"), TestEntity::name.name, listOf(ERROR_MESSAGE_03)),
            Arguments.of(TestEntity(email = "qewrty"), TestEntity::email.name, listOf(ERROR_MESSAGE_04)),
        )

    }

    private val validations: List<DeferredEntityValidation<TestEntity>> = listOf(
        DeferredEntityValidation(
            field = TestEntity::name,
            message = { ERROR_MESSAGE_01 },
            validate = { it.name.trim().isNotEmpty() },
            context = EMPTY,
        ),
        DeferredEntityValidation(
            field = TestEntity::name,
            message = { ERROR_MESSAGE_02 },
            validate = { it.name.length <= 10 },
            context = IO,
        ),
        DeferredEntityValidation(
            field = TestEntity::name,
            message = { ERROR_MESSAGE_03 },
            validate = { it.name.length > 2 },
            context = IO,
        ),
        DeferredEntityValidation(
            field = TestEntity::email,
            message = { ERROR_MESSAGE_04 },
            validate = { it.email.contains("@") },
            context = VT,
        ),
        DeferredEntityValidation(
            field = TestEntity::id,
            message = { "" },
            validate = { it.id.toString().isNotEmpty() },
        ),
    )

    @Test
    fun `Validate entity async`() = runTest(StandardTestDispatcher()) {
        assertDoesNotThrow {
            validations.validate(TestEntity(), "Entity has errors")
        }
    }

    @ParameterizedTest
    @MethodSource("entities")
    fun `Validate entity async with errors`(
        entity: TestEntity,
        attribute: String,
        errors: List<String>,
    ) = runTest(StandardTestDispatcher()) {
        val exception = assertThrows<EntityValidationException> {
            validations.validate(entity, "Entity ${entity.id} has errors")
        }

        assertEquals("Entity ${entity.id} has errors", exception.message)
        assertTrue(exception.errors.containsKey(attribute))
        assertTrue(exception.errors[attribute]!!.containsAll(errors))
    }

    @Test
    fun `Validate entity async with multiple errors`() = runTest(StandardTestDispatcher()) {
        val entity = TestEntity(name = " ", email = "qwerty")
        val exception = assertThrows<EntityValidationException> {
            validations.validate(entity, "Entity ${entity.id} has errors")
        }

        assertEquals("Entity ${entity.id} has errors", exception.message)

        assertTrue(exception.errors.containsKey(TestEntity::name.name))
        assertTrue(exception.errors.containsKey(TestEntity::email.name))

        assertTrue(exception.errors[TestEntity::name.name]!!.containsAll(listOf(ERROR_MESSAGE_01, ERROR_MESSAGE_03)))
        assertTrue(exception.errors[TestEntity::email.name]!!.contains(ERROR_MESSAGE_04))
    }

    data class TestEntity(
        override val id: UUID = UUID.randomUUID(),
        val name: String = randomString(),
        val email: String = "${randomString(5)}@email.com",
    ) : BaseEntity<TestEntity>()

}