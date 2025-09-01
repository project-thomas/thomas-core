package com.thomas.core.extension

import com.thomas.core.context.SessionContext
import com.thomas.core.context.SessionContextHolder
import com.thomas.core.generator.UserGenerator.generateSecurityUser
import com.thomas.core.model.entity.BaseEntity
import com.thomas.core.model.entity.DeferredEntityValidation
import com.thomas.core.model.entity.EntityValidationException
import com.thomas.core.model.entity.EntityValidationScope
import com.thomas.core.util.StringUtils.randomString
import java.time.LocalDateTime
import java.util.Locale
import java.util.UUID
import java.util.UUID.randomUUID
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

internal class BaseEntityExtensionTest {

    private lateinit var testEntity: TestEntity
    private lateinit var sessionContext: SessionContext
    private lateinit var userName: String

    @BeforeEach
    internal fun setUp() {
        testEntity = TestEntity(
            id = randomUUID(),
            name = "Test Entity",
            email = "test@example.com",
            age = 25
        )

        userName = randomString(numbers = false, spaces = false)

        sessionContext = SessionContext.create(
            user = generateSecurityUser().copy(firstName = userName),
            locale = Locale.forLanguageTag("en-US")
        )

        SessionContextHolder.context = sessionContext
    }

    @AfterEach
    internal fun tearDown() {
        SessionContextHolder.clearContext()
    }

    @Test
    internal fun `should not validate when validation list is empty`() = runTest {
        val validations = emptyList<DeferredEntityValidation<TestEntity>>()
        val errorMessage = "Validation failed"

        // Should not throw any exception
        validations.validate(testEntity, errorMessage)
    }

    @Test
    internal fun `should validate successfully when all validations pass`() = runTest {
        val validations = listOf(
            createValidation(
                field = TestEntity::name,
                message = { "Name cannot be empty" },
                validate = { it.name.isNotEmpty() }
            ),
            createValidation(
                field = TestEntity::email,
                message = { "Email must contain @" },
                validate = { it.email.contains("@") }
            ),
            createValidation(
                field = TestEntity::age,
                message = { "Age must be positive" },
                validate = { it.age > 0 }
            )
        )

        // Should not throw any exception since all validations pass
        validations.validate(testEntity, "Validation failed")
    }

    @Test
    internal fun `should throw EntityValidationException when validations fail`() = runTest {
        val entityWithInvalidData = TestEntity(
            id = randomUUID(),
            name = "",
            email = "invalid-email",
            age = -1
        )

        val validations = listOf(
            createValidation(
                field = TestEntity::name,
                message = { "Name cannot be empty" },
                validate = { it.name.isNotEmpty() }
            ),
            createValidation(
                field = TestEntity::email,
                message = { "Email must contain @" },
                validate = { it.email.contains("@") }
            ),
            createValidation(
                field = TestEntity::age,
                message = { "Age must be positive" },
                validate = { it.age > 0 }
            )
        )

        try {
            validations.validate(entityWithInvalidData, "Entity validation failed")
            fail { "Validation should have failed" }
        } catch (e: EntityValidationException) {
            assertEquals("Entity validation failed", e.message)
            assertEquals(3, e.errors.size)
            assertTrue(e.errors.containsKey("name"))
            assertTrue(e.errors.containsKey("email"))
            assertTrue(e.errors.containsKey("age"))
            assertEquals("Name cannot be empty", e.errors["name"]?.first())
            assertEquals("Email must contain @", e.errors["email"]?.first())
            assertEquals("Age must be positive", e.errors["age"]?.first())
        }
    }

    @Test
    internal fun `should handle multiple errors for same field`() = runTest {
        val validations = listOf(
            createValidation(
                field = TestEntity::name,
                message = { "Name cannot be empty" },
                validate = { it.name.isNotEmpty() }
            ),
            createValidation(
                field = TestEntity::name,
                message = { "Name must be at least 3 characters" },
                validate = { it.name.length >= 3 }
            )
        )

        val entityWithEmptyName = TestEntity(id = randomUUID(), name = "", email = "test@test.com", age = 25)

        try {
            validations.validate(entityWithEmptyName, "Multiple name errors")
            fail { "Validation should have failed" }
        } catch (e: EntityValidationException) {
            assertEquals(1, e.errors.size)
            assertTrue(e.errors.containsKey("name"))
            assertEquals(2, e.errors["name"]?.size)
            assertTrue(e.errors["name"]?.contains("Name cannot be empty") == true)
            assertTrue(e.errors["name"]?.contains("Name must be at least 3 characters") == true)
        }
    }

    @Test
    internal fun `should work with different validation scopes`() = runTest {
        val validationsWithIO = listOf(
            createValidation(
                field = TestEntity::name,
                message = { "Name validation with IO" },
                validate = {
                    delay(10) // Simulate IO operation
                    it.name.isNotEmpty()
                },
                scope = EntityValidationScope.IO
            )
        )

        val validationsWithVT = listOf(
            createValidation(
                field = TestEntity::email,
                message = { "Email validation with VT" },
                validate = {
                    delay(10) // Simulate virtual thread operation
                    it.email.contains("@")
                },
                scope = EntityValidationScope.VT
            )
        )

        // Both should pass without exceptions
        validationsWithIO.validate(testEntity, "IO validation failed")
        validationsWithVT.validate(testEntity, "VT validation failed")
    }

    @Test
    internal fun `should handle async validations correctly`() = runTest {
        val validations = listOf(
            createValidation(
                field = TestEntity::name,
                message = { "Async name validation" },
                validate = { entity ->
                    delay(50) // Simulate async operation
                    entity.name.isNotEmpty()
                }
            ),
            createValidation(
                field = TestEntity::email,
                message = { "Async email validation" },
                validate = { entity ->
                    delay(30) // Different delay
                    entity.email.contains("@")
                }
            )
        )

        // Should complete successfully with async validations
        validations.validate(testEntity, "Async validation failed")
    }

    @Test
    internal fun `should convert field names to snake_case`() = runTest {
        val validations = listOf(
            createValidation(
                field = TestEntity::firstName, // camelCase field
                message = { "First name validation" },
                validate = { false } // Always fail to test field name conversion
            )
        )

        try {
            validations.validate(testEntity, "Field name conversion test")
            fail { "Validation should have failed" }
        } catch (e: EntityValidationException) {
            assertTrue(e.errors.containsKey("first_name"))
            assertFalse(e.errors.containsKey("firstName"))
        }
    }

    @Test
    internal fun `addError should add error to concurrent hash map`() {
        val errorMap = ConcurrentHashMap<String, MutableList<String>>()

        errorMap.addError("field1", "Error message 1")
        errorMap.addError("field1", "Error message 2")
        errorMap.addError("field2", "Error message 3")

        assertEquals(2, errorMap.size)
        assertEquals(2, errorMap["field1"]?.size)
        assertEquals(1, errorMap["field2"]?.size)
        assertTrue(errorMap["field1"]?.contains("Error message 1") == true)
        assertTrue(errorMap["field1"]?.contains("Error message 2") == true)
        assertTrue(errorMap["field2"]?.contains("Error message 3") == true)
    }

    @Test
    internal fun `throwsOnError should not throw when map is empty`() {
        val emptyMap = ConcurrentHashMap<String, MutableList<String>>()

        // Should not throw any exception
        emptyMap.throwsOnError("Should not be thrown")
    }

    @Test
    internal fun `throwsOnError should throw EntityValidationException when map has errors`() {
        val errorMap = ConcurrentHashMap<String, MutableList<String>>()
        errorMap.addError("field1", "Error 1")
        errorMap.addError("field2", "Error 2")

        val exception = assertThrows(EntityValidationException::class.java) {
            errorMap.throwsOnError("Test error message")
        }

        assertEquals("Test error message", exception.message)
        assertEquals(2, exception.errors.size)
        assertTrue(exception.errors.containsKey("field1"))
        assertTrue(exception.errors.containsKey("field2"))
    }

    @Test
    internal fun `should handle concurrent validation execution`() = runTest {
        val validations = (1..10).map { index ->
            createValidation(
                field = TestEntity::name,
                message = { "Concurrent validation $index" },
                validate = { entity ->
                    delay((1..20).random().toLong()) // Random delay
                    entity.name.isNotEmpty()
                }
            )
        }

        // Should complete successfully with concurrent validations
        validations.validate(testEntity, "Concurrent validation failed")
    }

    @Test
    internal fun `should maintain session context during validation`() = runTest {
        val validations = listOf(
            createValidation(
                field = TestEntity::name,
                message = { entity ->
                    // Access session context during validation
                    val user = SessionContextHolder.currentUser
                    "Validation by ${user.firstName} for ${entity.name}"
                },
                validate = {
                    // Verify session context is available
                    val currentUser = SessionContextHolder.currentUser
                    assertEquals(userName, currentUser.firstName)
                    false // Always fail to test message generation
                }
            )
        )

        try {
            validations.validate(testEntity, "Session context test")
            fail { "Validation should have failed" }
        } catch (e: EntityValidationException) {
            val errorMessage = e.errors["name"]?.first()
            assertTrue(errorMessage?.contains(userName) == true)
            assertTrue(errorMessage?.contains("Test Entity") == true)
        }

    }

    private fun createValidation(
        field: kotlin.reflect.KProperty1<TestEntity, *>,
        message: suspend (TestEntity) -> String,
        validate: suspend (TestEntity) -> Boolean,
        scope: EntityValidationScope = EntityValidationScope.EMPTY
    ) = DeferredEntityValidation(
        field = field,
        message = message,
        validate = validate,
        scope = scope
    )

    private data class TestEntity(
        override var id: UUID = UUID.randomUUID(),
        var createdAt: LocalDateTime? = null,
        var updatedAt: LocalDateTime? = null,
        val name: String = "",
        val email: String = "",
        val age: Int = 0,
        val firstName: String = "First" // For snake_case testing
    ) : BaseEntity<TestEntity>()
}
