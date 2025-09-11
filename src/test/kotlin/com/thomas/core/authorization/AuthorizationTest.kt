package com.thomas.core.authorization

import com.thomas.core.context.SessionContextHolder.clearContext
import com.thomas.core.context.SessionContextHolder.currentUser
import com.thomas.core.exception.ErrorType.UNAUTHORIZED_ACTION
import com.thomas.core.generator.UserGenerator.generateSecurityUser
import com.thomas.core.generator.UserGenerator.generateSecurityUserWithRoles
import com.thomas.core.i18n.CoreMessageI18N.contextCurrentSessionCurrentUserNotAllowed
import com.thomas.core.model.security.SecurityRole
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class AuthorizationTest {

    @AfterEach
    internal fun tearDown() {
        clearContext()
    }

    @ParameterizedTest
    @EnumSource(SecurityRole::class)
    fun `When organization role is required and user does not have it, should throws UnauthorizedUserException`(
        role: SecurityRole
    ) = runTest(StandardTestDispatcher()) {
        currentUser = generateSecurityUser()
        val exception = assertThrows<UnauthorizedUserException> {
            authorized(roles = setOf(role)) {}
        }
        assertEquals(UnauthorizedUserException().message, exception.message)
        assertEquals(UNAUTHORIZED_ACTION, exception.type)
    }

    @ParameterizedTest
    @EnumSource(SecurityRole::class)
    fun `When organization role is required and user have it, should return the block`(
        role: SecurityRole
    ) = runTest(StandardTestDispatcher()) {
        currentUser = generateSecurityUserWithRoles(
            userRoles = setOf(role),
        )
        assertDoesNotThrow {
            assertTrue(authorized(roles = setOf(role)) { true })
        }
    }

    @ParameterizedTest
    @EnumSource(SecurityRole::class)
    fun `When organization role is required and group have it, should return the block`(
        role: SecurityRole
    ) = runTest(StandardTestDispatcher()) {
        currentUser = generateSecurityUserWithRoles(
            groupRoles = setOf(role),
        )
        assertDoesNotThrow {
            assertTrue(authorized(roles = setOf(role)) { true })
        }
    }

    @Test
    fun `When roles are not specified, should return the block`() = runTest(StandardTestDispatcher()) {
        assertDoesNotThrow {
            currentUser = generateSecurityUser()
            assertTrue(authorized { true })
        }
    }

    @Test
    fun `When multiple mixed roles are required and user has partial match, should throw UnauthorizedUserException`() =
        runTest(StandardTestDispatcher()) {
            val role = SecurityRole.entries.random()
            currentUser = generateSecurityUserWithRoles(
                userRoles = setOf(role),
                groupRoles = setOf(),
            )

            val requiredRoles = SecurityRole.entries.filter { it != role }.shuffled().take(3).toSet()

            val exception = assertThrows<UnauthorizedUserException> {
                authorized(roles = requiredRoles) {}
            }
            assertEquals(contextCurrentSessionCurrentUserNotAllowed(), exception.message)
            assertEquals(UNAUTHORIZED_ACTION, exception.type)
        }

    @Test
    fun `When multiple mixed roles are required and user has at least one, should return the block`() = runTest(StandardTestDispatcher()) {
        val securityRole = SecurityRole.entries.random()

        currentUser = generateSecurityUserWithRoles(
            userRoles = setOf(securityRole),
        )

        val requiredRoles = setOf(
            securityRole,
            SecurityRole.entries.filter { it != securityRole }.random(),
        )

        assertDoesNotThrow {
            assertTrue(authorized(roles = requiredRoles) { true })
        }
    }

    @Test
    fun `When multiple mixed roles are required and group has at least one, should return the block`() = runTest(StandardTestDispatcher()) {
        val securityRole = SecurityRole.entries.random()

        currentUser = generateSecurityUserWithRoles(
            groupRoles = setOf(securityRole),
        )

        val requiredRoles = setOf(
            securityRole,
            SecurityRole.entries.filter { it != securityRole }.random(),
        )

        assertDoesNotThrow {
            assertTrue(authorized(roles = requiredRoles) { true })
        }
    }

    @Test
    fun `When exception is thrown in block execution, should propagate the exception`() = runTest(StandardTestDispatcher()) {
        currentUser = generateSecurityUserWithRoles()

        val customException = RuntimeException("Custom error")
        val exception = assertThrows<RuntimeException> {
            authorized { throw customException }
        }
        assertEquals("Custom error", exception.message)
    }

    @Test
    fun `When block returns different types, should preserve return type`() = runTest(StandardTestDispatcher()) {
        currentUser = generateSecurityUserWithRoles()

        val stringResult = authorized { "test result" }
        assertEquals("test result", stringResult)

        val intResult = authorized { 42 }
        assertEquals(42, intResult)

        val listResult = authorized { listOf(1, 2, 3) }
        assertEquals(listOf(1, 2, 3), listResult)
    }

}
