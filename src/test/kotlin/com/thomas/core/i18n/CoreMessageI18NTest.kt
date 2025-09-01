package com.thomas.core.i18n

import com.thomas.core.context.SessionContextHolder
import com.thomas.core.context.SessionContextHolder.clearContext
import java.util.Locale
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

internal class CoreMessageI18NTest {

    @AfterEach
    internal fun tearDown() {
        clearContext()
    }

    @Test
    internal fun `should return formatted message for pt-BR locale`() {
        SessionContextHolder.currentLocale = Locale.forLanguageTag("pt-BR")

        val notLoggedMessage = CoreMessageI18N.contextCurrentSessionCurrentUserNotLogged()
        val notAllowedMessage = CoreMessageI18N.contextCurrentSessionCurrentUserNotAllowed()
        val exceptionMessage = CoreMessageI18N.exceptionDetailedExceptionMessageDefault()
        val validationMessage = CoreMessageI18N.validationEntityValidationInvalidErrorMessage()

        assertNotNull(notLoggedMessage)
        assertNotNull(notAllowedMessage)
        assertNotNull(exceptionMessage)
        assertNotNull(validationMessage)
        assertFalse(notLoggedMessage.isBlank())
        assertFalse(notAllowedMessage.isBlank())
        assertFalse(exceptionMessage.isBlank())
        assertFalse(validationMessage.isBlank())
    }

    @Test
    internal fun `should return formatted message for en-US locale`() {
        SessionContextHolder.currentLocale = Locale.forLanguageTag("en-US")

        val notLoggedMessage = CoreMessageI18N.contextCurrentSessionCurrentUserNotLogged()
        val notAllowedMessage = CoreMessageI18N.contextCurrentSessionCurrentUserNotAllowed()
        val exceptionMessage = CoreMessageI18N.exceptionDetailedExceptionMessageDefault()
        val validationMessage = CoreMessageI18N.validationEntityValidationInvalidErrorMessage()

        assertNotNull(notLoggedMessage)
        assertNotNull(notAllowedMessage)
        assertNotNull(exceptionMessage)
        assertNotNull(validationMessage)
    }

    @Test
    internal fun `should return formatted message for ROOT locale`() {
        SessionContextHolder.currentLocale = Locale.ROOT

        val notLoggedMessage = CoreMessageI18N.contextCurrentSessionCurrentUserNotLogged()
        val notAllowedMessage = CoreMessageI18N.contextCurrentSessionCurrentUserNotAllowed()
        val exceptionMessage = CoreMessageI18N.exceptionDetailedExceptionMessageDefault()
        val validationMessage = CoreMessageI18N.validationEntityValidationInvalidErrorMessage()

        assertNotNull(notLoggedMessage)
        assertNotNull(notAllowedMessage)
        assertNotNull(exceptionMessage)
        assertNotNull(validationMessage)
    }

    @Test
    internal fun `should handle different locales consistently`() {
        val locales = listOf(
            Locale.ROOT,
            Locale.forLanguageTag("pt-BR"),
            Locale.forLanguageTag("en-US"),
            Locale.forLanguageTag("es-ES"),
            Locale.forLanguageTag("fr-FR")
        )

        locales.forEach { locale ->
            SessionContextHolder.currentLocale = locale

            val notLoggedMessage = CoreMessageI18N.contextCurrentSessionCurrentUserNotLogged()
            val notAllowedMessage = CoreMessageI18N.contextCurrentSessionCurrentUserNotAllowed()
            val exceptionMessage = CoreMessageI18N.exceptionDetailedExceptionMessageDefault()
            val validationMessage = CoreMessageI18N.validationEntityValidationInvalidErrorMessage()

            // All messages should be non-null and non-empty
            assertNotNull(notLoggedMessage)
            assertNotNull(notAllowedMessage)
            assertNotNull(exceptionMessage)
            assertNotNull(validationMessage)
            assertFalse(notLoggedMessage.isBlank())
            assertFalse(notAllowedMessage.isBlank())
            assertFalse(exceptionMessage.isBlank())
            assertFalse(validationMessage.isBlank())
        }
    }

    @Test
    internal fun `should be thread safe in concurrent access`() {
        val threads = mutableListOf<Thread>()
        val results = mutableListOf<List<String>>()
        val lock = Any()

        repeat(50) { index ->
            val thread = Thread {
                SessionContextHolder.currentLocale = when (index % 4) {
                    0 -> Locale.forLanguageTag("pt-BR")
                    1 -> Locale.forLanguageTag("en-US")
                    2 -> Locale.ROOT
                    else -> Locale.forLanguageTag("es-ES")
                }

                val messages = listOf(
                    CoreMessageI18N.contextCurrentSessionCurrentUserNotLogged(),
                    CoreMessageI18N.contextCurrentSessionCurrentUserNotAllowed(),
                    CoreMessageI18N.exceptionDetailedExceptionMessageDefault(),
                    CoreMessageI18N.validationEntityValidationInvalidErrorMessage()
                )

                synchronized(lock) {
                    results.add(messages)
                }
            }
            threads.add(thread)
            thread.start()
        }

        threads.forEach { it.join() }

        // Verify all threads completed successfully
        assertNotNull(results)
        results.forEach { messageList ->
            messageList.forEach { message ->
                assertNotNull(message)
                assertFalse(message.isBlank())
            }
        }
    }

    @Test
    internal fun `should handle rapid locale switching without errors`() {
        val locales = listOf(
            Locale.ROOT,
            Locale.forLanguageTag("pt-BR"),
            Locale.forLanguageTag("en-US")
        )

        repeat(1000) { index ->
            SessionContextHolder.currentLocale = locales[index % locales.size]

            val message = CoreMessageI18N.contextCurrentSessionCurrentUserNotLogged()

            assertNotNull(message)
            assertFalse(message.isBlank())
        }
    }

    @Test
    internal fun `should maintain consistency across all methods`() {
        val methods = listOf(
            CoreMessageI18N::contextCurrentSessionCurrentUserNotLogged,
            CoreMessageI18N::contextCurrentSessionCurrentUserNotAllowed,
            CoreMessageI18N::exceptionDetailedExceptionMessageDefault,
            CoreMessageI18N::validationEntityValidationInvalidErrorMessage
        )

        methods.forEach { method ->
            val result = method.invoke()
            assertNotNull(result)
            assertFalse(result.isBlank())
        }
    }
}
