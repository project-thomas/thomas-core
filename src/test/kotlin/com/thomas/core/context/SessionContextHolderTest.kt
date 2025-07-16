package com.thomas.core.context

import com.thomas.core.context.SessionContextHolder.clearContext
import com.thomas.core.generator.UserGenerator.generateSecurityUser
import com.thomas.core.model.security.SecurityUser
import java.util.Locale
import java.util.Locale.CHINA
import java.util.Locale.FRENCH
import java.util.Locale.GERMAN
import java.util.Locale.ITALY
import java.util.Locale.KOREA
import java.util.Locale.ROOT
import java.util.UUID
import java.util.UUID.randomUUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class SessionContextHolderTest {

    private val threads = 5
    private val executors = Executors.newFixedThreadPool(threads)
    private val latch = CountDownLatch(threads)

    private val locales: MutableMap<Int, Locale> = (1..threads).associateWith {
        FRENCH
    }.toMutableMap()

    private val users: MutableMap<Int, SecurityUser?> = (1..threads).associateWith {
        null
    }.toMutableMap()

    private val units: MutableMap<Int, UUID?> = (1..threads).associateWith {
        null
    }.toMutableMap()

    private fun ExecutorService.submitTest(
        session: SessionData
    ) = this.submit {
        session.locale?.apply { SessionContextHolder.context.currentLocale = this }
        SessionContextHolder.context.currentUser = generateSecurityUser().copy(
            userId = session.id,
            firstName = "User ${session.number}",
            lastName = "Last Name ${session.number}",
            mainEmail = "user${session.number}@test.com",
        )
        runBlocking { delay(session.delay) }
        locales[session.number] = SessionContextHolder.context.currentLocale
        users[session.number] = SessionContextHolder.context.currentUser
        units[session.number] = session.unit
        latch.countDown()
    }

    private data class SessionData(
        val id: UUID = randomUUID(),
        val locale: Locale?,
        val unit: UUID?,
        val number: Int,
        val delay: Long,
    )

    @BeforeEach
    internal fun setUp() {
        clearContext()
    }

    @AfterEach
    internal fun tearDown() {
        clearContext()
    }

    @Test
    fun `Given different threads, they should be treated separately`() {
        val sessions = listOf(
            SessionData(locale = CHINA, unit = randomUUID(), number = 1, delay = 1500),
            SessionData(locale = GERMAN, unit = null, number = 2, delay = 500),
            SessionData(locale = ITALY, unit = randomUUID(), number = 3, delay = 1500),
            SessionData(locale = null, unit = randomUUID(), number = 4, delay = 3500),
            SessionData(locale = KOREA, unit = randomUUID(), number = 5, delay = 800),
        )

        sessions.forEach { executors.submitTest(it) }

        latch.await(10, TimeUnit.SECONDS)

        sessions.forEach {
            assertEquals(it.locale ?: ROOT, locales[it.number])
            assertNotNull(users[it.number])
            val user = users[it.number]!!

            assertEquals(it.id, user.userId)
            assertEquals("User ${it.number}", user.firstName)
            assertEquals("Last Name ${it.number}", user.lastName)
            assertEquals("user${it.number}@test.com", user.mainEmail)
            assertEquals(it.unit, units[it.number])
        }
    }

    @Test
    fun `When there is no user logged in, should throws exception`() {
        assertThrows<UnauthenticatedUserException> { SessionContextHolder.currentUser }
    }

    @Test
    fun `When a custom property does not exists and is set, should return the respective value`() {
        val uuid = randomUUID().toString()
        val property = "custom"
        SessionContextHolder.setSessionProperty(property, uuid)
        assertEquals(uuid, SessionContextHolder.getSessionProperty(property))
    }

    @Test
    fun `When create a session, current token should be null`() {
        SessionContextHolder.context = SessionContext()
        assertNull(SessionContextHolder.currentToken)
    }

    @Test
    fun `When token is set in a session, current token should be returned`() {
        val uuid = randomUUID().toString()
        SessionContextHolder.currentToken = uuid
        assertEquals(uuid, SessionContextHolder.currentToken)
    }

    @Test
    fun `When unit is set in a session, current unit should be returned`() {
        val uuid = randomUUID()
        SessionContextHolder.currentUnit = uuid
        assertEquals(uuid, SessionContextHolder.currentUnit)
    }

}
