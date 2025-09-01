package com.thomas.core.i18n

import com.thomas.core.context.SessionContextHolder
import com.thomas.core.context.SessionContextHolder.clearContext
import java.util.Locale
import java.util.MissingResourceException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class BundleResolverTest {

    @AfterEach
    internal fun tearDown() {
        clearContext()
    }

    @Test
    internal fun `should format message with current locale using existing CoreMessageI18N`() {
        SessionContextHolder.currentLocale = Locale.forLanguageTag("pt-BR")

        val result = CoreMessageI18N.contextCurrentSessionCurrentUserNotLogged()

        assertNotNull(result)
    }

    @Test
    internal fun `should fallback to ROOT locale when current locale not available`() {
        SessionContextHolder.currentLocale = Locale.forLanguageTag("fr-FR")

        val result = CoreMessageI18N.contextCurrentSessionCurrentUserNotLogged()

        assertNotNull(result)
    }

    @Test
    internal fun `should handle different locales consistently`() {
        val locales = listOf(
            Locale.ROOT,
            Locale.forLanguageTag("pt-BR"),
            Locale.forLanguageTag("en-US")
        )

        locales.forEach { locale ->
            SessionContextHolder.currentLocale = locale

            val notLoggedMessage = CoreMessageI18N.contextCurrentSessionCurrentUserNotLogged()
            val notAllowedMessage = CoreMessageI18N.contextCurrentSessionCurrentUserNotAllowed()
            val defaultMessage = CoreMessageI18N.exceptionDetailedExceptionMessageDefault()
            val validationMessage = CoreMessageI18N.validationEntityValidationInvalidErrorMessage()

            assertNotNull(notLoggedMessage)
            assertNotNull(notAllowedMessage)
            assertNotNull(defaultMessage)
            assertNotNull(validationMessage)
        }
    }

    @Test
    internal fun `should handle concurrent access to bundle resources`() {
        val threads = mutableListOf<Thread>()
        val results = mutableListOf<String>()
        val lock = Any()

        repeat(20) { index ->
            val thread = Thread {
                SessionContextHolder.currentLocale = if (index % 2 == 0) {
                    Locale.forLanguageTag("pt-BR")
                } else {
                    Locale.forLanguageTag("en-US")
                }

                val message = CoreMessageI18N.contextCurrentSessionCurrentUserNotLogged()

                synchronized(lock) {
                    results.add(message)
                }
            }
            threads.add(thread)
            thread.start()
        }

        threads.forEach { it.join() }

        assertEquals(20, results.size)
        results.forEach { message ->
            assertNotNull(message)
        }
    }

    @Test
    internal fun `should throw exception when bundle not found during initialization`() {
        assertThrows(MissingResourceException::class.java) {
            TestBundleResolver("non-existent-bundle")
        }
    }

    @Test
    internal fun `should maintain thread safety during locale switching`() {
        val switchCount = 100
        var successCount = 0
        val lock = Any()

        repeat(switchCount) { index ->
            val thread = Thread {
                val locale = when (index % 3) {
                    0 -> Locale.forLanguageTag("pt-BR")
                    1 -> Locale.forLanguageTag("en-US")
                    else -> Locale.ROOT
                }

                SessionContextHolder.currentLocale = locale
                val message = CoreMessageI18N.contextCurrentSessionCurrentUserNotLogged()

                synchronized(lock) {
                    if (message.isNotEmpty()) {
                        successCount++
                    }
                }
            }
            thread.start()
            thread.join()
        }

        assertEquals(switchCount, successCount)
    }

    @Test
    internal fun `should handle all CoreMessageI18N methods without exceptions`() {
        val methods = listOf(
            { CoreMessageI18N.contextCurrentSessionCurrentUserNotLogged() },
            { CoreMessageI18N.contextCurrentSessionCurrentUserNotAllowed() },
            { CoreMessageI18N.exceptionDetailedExceptionMessageDefault() },
            { CoreMessageI18N.validationEntityValidationInvalidErrorMessage() }
        )

        methods.forEach { method ->
            val result = method.invoke()
            assertNotNull(result)
        }
    }

    private class TestBundleResolver(bundleName: String) : BundleResolver(bundleName) {

        fun getFormattedMessage(key: String, vararg arguments: Any): String {
            return formattedMessage(key, *arguments)
        }
    }
}
