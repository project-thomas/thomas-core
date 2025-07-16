package com.thomas.core.i18n

import com.thomas.core.context.SessionContextHolder.currentLocale
import java.text.MessageFormat
import java.util.Locale
import java.util.ResourceBundle
import java.util.concurrent.ConcurrentHashMap

abstract class BundleResolver(
    private val bundleName: String
) {

    private val resources = ConcurrentHashMap<String, ResourceBundle>()

    init {
        Locale.getAvailableLocales().forEach { locale ->
            ResourceBundle.getBundle(bundleName, locale)
                .takeIf { it.locale.equals(locale) }
                ?.apply { resources[locale.toLanguageTag()] = this }
        }
    }

    private fun bundle(): ResourceBundle = resources.getOrDefault(
        currentLocale.toLanguageTag(),
        resources[Locale.ROOT.toLanguageTag()]!!
    )

    protected fun formattedMessage(
        key: String,
        vararg arguments: Any
    ): String = MessageFormat.format(bundle().getString(key), *arguments)

}
