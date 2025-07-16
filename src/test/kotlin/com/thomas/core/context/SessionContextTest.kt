package com.thomas.core.context

import com.thomas.core.context.SessionContextHolder.clearContext
import com.thomas.core.context.SessionContextHolder.context
import com.thomas.core.context.SessionContextHolder.currentLocale
import com.thomas.core.context.SessionContextHolder.currentOrganization
import com.thomas.core.context.SessionContextHolder.currentUnit
import com.thomas.core.context.SessionContextHolder.currentUser
import com.thomas.core.context.SessionContextHolder.getSessionProperty
import com.thomas.core.context.SessionContextHolder.setSessionProperty
import com.thomas.core.generator.UserGenerator.generateSecurityUser
import java.util.Locale.ENGLISH
import java.util.Locale.FRENCH
import java.util.Locale.ROOT
import java.util.UUID.randomUUID
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class SessionContextTest {

    private val user = generateSecurityUser()

    @AfterEach
    internal fun tearDown() {
        clearContext()
    }

    @Test
    fun `Clear session context`() {
        val unitId = randomUUID()
        val propOne = "prop_one"
        val propTwo = "prop_two"
        val propValue = "value_two"

        currentUser = user
        currentLocale = FRENCH
        currentUnit = unitId

        setSessionProperty(propOne, null)
        setSessionProperty(propTwo, propValue)

        assertEquals(user, context.currentUser)
        assertEquals(FRENCH, context.currentLocale)
        assertEquals(unitId, context.currentUnit)
        assertEquals(user.securityOrganization.organizationId, context.currentOrganization)
        assertNull(getSessionProperty(propOne))
        assertEquals(propValue, getSessionProperty(propTwo))

        clearContext()

        assertThrows<UnauthenticatedUserException> { currentUser }
        assertThrows<UnresolvedOrganizationException> { currentOrganization }
        assertEquals(ROOT, currentLocale)
        assertNull(currentUnit)
        assertNull(getSessionProperty(propOne))
        assertNull(getSessionProperty(propTwo))
    }


    @Test
    fun `When no locale is informed, ROOT should be default`() {
        assertEquals(ROOT, SessionContext().currentLocale)
    }

    @Test
    fun `When a locale is informed, the informed locale should be set`() {
        val session = SessionContext(mutableMapOf()).apply {
            this.currentLocale = ENGLISH
        }
        assertEquals(ENGLISH, session.currentLocale)
    }

    @Test
    fun `When no user is set, should throws exception`() {
        assertThrows(UnauthenticatedUserException::class.java) {
            SessionContext().currentUser
        }
    }

    @Test
    fun `When user is set, should returns the user`() {
        assertDoesNotThrow {
            SessionContext().apply {
                currentUser = user
            }.currentUser
        }
    }

    @Test
    fun `When no user is set, organization should throws exception`() {
        assertThrows(UnresolvedOrganizationException::class.java) {
            SessionContext().currentOrganization
        }
    }

    @Test
    fun `When user is set, organization should returns the organization id`() {
        assertDoesNotThrow {
            SessionContext().apply {
                currentUser = user
            }.currentOrganization
        }
    }

    @Test
    fun `When a custom property exists, should return the respective value`() {
        val uuid = randomUUID().toString()
        val property = "custom"
        val session = SessionContext(mutableMapOf(property to uuid))
        assertEquals(uuid, session.getProperty(property))
    }

    @Test
    fun `When a custom property does not exists and is set, should return the respective value`() {
        val uuid = randomUUID().toString()
        val property = "custom"
        val session = SessionContext(mutableMapOf())
        session.setProperty(property, uuid)
        assertEquals(uuid, session.getProperty(property))
    }

    @Test
    fun `When a custom property exists and is set, should return the new value`() {
        val uuid = randomUUID().toString()
        val property = "custom"
        val session = SessionContext(mutableMapOf(property to "ad95cb8c-1f60-4b9b-8a8e-5c25dacb7a7b"))
        session.setProperty(property, uuid)
        assertEquals(uuid, session.getProperty(property))
    }

    @Test
    fun `When create a session, current token should be null`() {
        val session = SessionContext(mutableMapOf())
        assertNull(session.currentToken)
    }

    @Test
    fun `When token is set in a session, current token should be returned`() {
        val uuid = randomUUID().toString()
        val session = SessionContext(mutableMapOf()).apply {
            currentToken = uuid
        }
        assertEquals(uuid, session.currentToken)
    }

}
