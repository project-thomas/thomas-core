package com.thomas.core.model.security

import com.thomas.core.context.SessionContextHolder.clearContext
import com.thomas.core.generator.UserGenerator.generateSecurityUser
import com.thomas.core.generator.UserGenerator.generateSecurityUserWithRoles
import com.thomas.core.model.security.SecurityRole.ADMINISTRATOR_USER
import com.thomas.core.model.security.SecurityRole.MASTER_USER
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SecurityUserTest {

    @BeforeEach
    internal fun setUp() {
        clearContext()
    }

    @Test
    fun `Security User full name`() {
        val user = generateSecurityUser()
        assertEquals("${user.firstName} ${user.lastName}", user.fullName)
    }

    @Test
    fun `Security User short name`() {
        val user = generateSecurityUser()
        assertEquals("${user.firstName} ${user.lastName[0]}.", user.shortName)
    }

    @Test
    fun `Security User alternative name`() {
        val user = generateSecurityUser()
        assertEquals("${user.firstName[0]}. ${user.lastName}", user.alternativeName)
    }

    @Test
    fun `Security User is master`() {
        generateSecurityUserWithRoles(
            userRoles = setOf(MASTER_USER),
            groupRoles = setOf()
        ).apply {
            assertTrue(this.isMaster)
            assertTrue(this.isAdministrator)
        }
    }

    @Test
    fun `Security User is administrator`() {
        generateSecurityUserWithRoles(
            userRoles = setOf(ADMINISTRATOR_USER),
            groupRoles = setOf()
        ).apply {
            assertFalse(this.isMaster)
            assertTrue(this.isAdministrator)
        }
    }

    @Test
    fun `Security User is master by group`() {
        generateSecurityUserWithRoles(
            userRoles = setOf(),
            groupRoles = setOf(MASTER_USER)
        ).apply {
            assertTrue(this.isMaster)
            assertTrue(this.isAdministrator)
        }
    }

    @Test
    fun `Security User is administrator by group`() {
        generateSecurityUserWithRoles(
            userRoles = setOf(),
            groupRoles = setOf(ADMINISTRATOR_USER)
        ).apply {
            assertFalse(this.isMaster)
            assertTrue(this.isAdministrator)
        }
    }

}
