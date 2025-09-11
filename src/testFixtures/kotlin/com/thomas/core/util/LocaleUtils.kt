package com.thomas.core.util

import java.util.Locale

object LocaleUtils {

    fun randomLocale(): Locale = listOf(
        Locale.ENGLISH,
        Locale.FRENCH,
        Locale.CANADA,
        Locale.ITALY,
        Locale.forLanguageTag("pt-BR"),
    ).random()

}
