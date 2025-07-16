package com.thomas.core.model.security

import com.thomas.core.context.SessionContextHolder.currentLocale
import java.util.Locale
import java.util.Locale.ROOT
import java.util.Properties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class SecurityRoleGroupCategoryTest {

    companion object {

        @JvmStatic
        fun strings() = listOf(
            Arguments.of(ROOT, {
                Properties().apply {
                    this.load(ClassLoader.getSystemClassLoader().getResourceAsStream("strings/core-roles-categories.properties"))
                }
            }),
            Arguments.of(Locale.forLanguageTag("en-US"), {
                Properties().apply {
                    this.load(ClassLoader.getSystemClassLoader().getResourceAsStream("strings/core-roles-categories_en_US.properties"))
                }
            }),
        )

    }

    @BeforeEach
    internal fun setUp() {
        currentLocale = ROOT
    }

    @ParameterizedTest
    @MethodSource("strings")
    fun `Security Role Group test`(
        locale: Locale,
        props: () -> Properties,
    ) {
        currentLocale = locale
        val properties = props()
        SecurityRoleGroupCategory.entries.forEach { category ->
            assertEquals(properties.getProperty("security.role-category.${category.name.lowercase()}.name"), category.categoryName)
            assertEquals(properties.getProperty("security.role-category.${category.name.lowercase()}.description"), category.categoryDescription)
        }
    }

}
