package com.thomas.core.context

import com.thomas.core.context.SessionContextHolder.currentLocale
import com.thomas.core.context.SessionContextHolder.currentToken
import com.thomas.core.context.SessionContextHolder.currentUser
import com.thomas.core.extension.VT
import com.thomas.core.extension.withSessionContext
import com.thomas.core.extension.withSessionContextIO
import com.thomas.core.extension.withSessionContextVT
import com.thomas.core.generator.UserGenerator.generateSecurityUser
import com.thomas.core.model.entity.DeferredEntityValidationContext
import com.thomas.core.util.StringUtils.randomString
import java.util.Locale
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class CoroutineSessionContextTest {

    companion object {

        @JvmStatic
        fun dispatchers() = listOf(
            Arguments.of({ Dispatchers.VT }),
            Arguments.of({ Dispatchers.IO }),
            Arguments.of({ Dispatchers.Default }),
        )

        @JvmStatic
        fun asyncDispatchers() = listOf(
            Arguments.of(DeferredEntityValidationContext.EMPTY),
            Arguments.of(DeferredEntityValidationContext.IO),
            Arguments.of(DeferredEntityValidationContext.VT),
        )

    }

    @ParameterizedTest
    @MethodSource("dispatchers")
    fun `Coroutine without SessionContext`(
        dispatcher: () -> CoroutineDispatcher
    ) = runTest(StandardTestDispatcher()) {
        val user = generateSecurityUser()
        val token = randomString(20)
        val locale = Locale.of("pt-BR")
        currentUser = user
        currentToken = token
        currentLocale = locale
        withContext(dispatcher()) {
            assertThrows<UnauthenticatedUserException> { currentUser }
            assertNull(currentToken)
            assertEquals(Locale.ROOT, currentLocale)
        }
    }

    @ParameterizedTest
    @MethodSource("dispatchers")
    fun `Coroutine with Dispatchers IO and SessionContext`(
        dispatcher: () -> CoroutineDispatcher
    ) = runTest(StandardTestDispatcher()) {
        val user = generateSecurityUser()
        val token = randomString(20)
        val locale = Locale.of("pt-BR")
        currentUser = user
        currentToken = token
        currentLocale = locale
        withSessionContext(dispatcher()) {
            assertEquals(user, currentUser)
            assertEquals(token, currentToken)
            assertEquals(locale, currentLocale)
        }
    }

    @Test
    fun `Coroutine utils with Dispatchers IO and SessionContext`() = runTest(StandardTestDispatcher()) {
        val user = generateSecurityUser()
        val token = randomString(20)
        val locale = Locale.of("pt-BR")
        currentUser = user
        currentToken = token
        currentLocale = locale
        withSessionContextIO {
            assertEquals(user, currentUser)
            assertEquals(token, currentToken)
            assertEquals(locale, currentLocale)
        }
    }

    @Test
    fun `Coroutine utils with Dispatchers VT and SessionContext`() = runTest(StandardTestDispatcher()) {
        val user = generateSecurityUser()
        val token = randomString(20)
        val locale = Locale.of("pt-BR")
        currentUser = user
        currentToken = token
        currentLocale = locale
        withSessionContextVT {
            assertEquals(user, currentUser)
            assertEquals(token, currentToken)
            assertEquals(locale, currentLocale)
        }
    }

    @Test
    fun `Coroutine utils with default parameter and SessionContext`() = runTest(StandardTestDispatcher()) {
        val user = generateSecurityUser()
        val token = randomString(20)
        val locale = Locale.of("pt-BR")
        currentUser = user
        currentToken = token
        currentLocale = locale
        withSessionContext {
            assertEquals(user, currentUser)
            assertEquals(token, currentToken)
            assertEquals(locale, currentLocale)
        }
    }

    @Test
    fun `Coroutine coroutineScope and SessionContext`() = runTest(StandardTestDispatcher()) {
        val user = generateSecurityUser()
        val token = randomString(20)
        val locale = Locale.of("pt-BR")
        currentUser = user
        currentToken = token
        currentLocale = locale
        coroutineScope {
            assertEquals(user, currentUser)
            assertEquals(token, currentToken)
            assertEquals(locale, currentLocale)
        }
    }

    @ParameterizedTest
    @MethodSource("asyncDispatchers")
    fun `Async contexts`(
        validationContext: DeferredEntityValidationContext
    ) = runTest(StandardTestDispatcher()) {
        val user = generateSecurityUser()
        val token = randomString(20)
        val locale = Locale.of("pt-BR")
        currentUser = user
        currentToken = token
        currentLocale = locale
        validationContext.defer(this) {
            assertEquals(user, currentUser)
            assertEquals(token, currentToken)
            assertEquals(locale, currentLocale)
        }.await()
    }

}
