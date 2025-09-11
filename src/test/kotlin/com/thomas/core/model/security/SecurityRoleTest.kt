package com.thomas.core.model.security

import com.thomas.core.context.SessionContextHolder.currentLocale
import com.thomas.core.model.security.SecurityRole.ADMINISTRATOR_USER
import com.thomas.core.model.security.SecurityRole.MASTER_USER
import java.util.Locale
import java.util.Locale.ROOT
import java.util.Properties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class SecurityRoleTest {

    companion object {

        @JvmStatic
        fun strings() = listOf(
            Arguments.of(ROOT, {
                Properties().apply {
                    this.load(ClassLoader.getSystemClassLoader().getResourceAsStream("strings/core-roles-groups.properties"))
                    this.load(ClassLoader.getSystemClassLoader().getResourceAsStream("strings/core-roles-categories.properties"))
                    this.load(ClassLoader.getSystemClassLoader().getResourceAsStream("strings/core-roles.properties"))
                }
            }),
            Arguments.of(Locale.forLanguageTag("en-US"), {
                Properties().apply {
                    this.load(ClassLoader.getSystemClassLoader().getResourceAsStream("strings/core-roles-groups_en_US.properties"))
                    this.load(ClassLoader.getSystemClassLoader().getResourceAsStream("strings/core-roles-categories_en_US.properties"))
                    this.load(ClassLoader.getSystemClassLoader().getResourceAsStream("strings/core-roles_en_US.properties"))
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
        SecurityRoleGroup.entries.map { it.groupOrder }.forEach { order ->
            val group = SecurityRoleGroup.entries.firstOrNull { it.groupOrder == order }
            assertNotNull(group)
            assertEquals(properties.getProperty("security.role-group.${group!!.name.lowercase()}.name"), group.groupName)
            assertEquals(properties.getProperty("security.role-group.${group.name.lowercase()}.description"), group.groupDescription)
            assertTrue(SecurityRoleCategory.entries.filter { it.subgroupGroup == group }.containsAll(group.subgroups))
        }
    }


    @ParameterizedTest
    @MethodSource("strings")
    fun `Security Role Subgroup test`(
        locale: Locale,
        props: () -> Properties,
    ) {
        currentLocale = locale
        val properties = props()
        SecurityRoleCategory.entries.map { it.subgroupOrder }.forEach { order ->
            val subgroup = SecurityRoleCategory.entries.firstOrNull { it.subgroupOrder == order }
            assertNotNull(subgroup)
            assertEquals(properties.getProperty("security.role-category.${subgroup!!.name.lowercase()}.name"), subgroup.subgroupName)
            assertEquals(
                properties.getProperty("security.role-category.${subgroup.name.lowercase()}.description"),
                subgroup.subgroupDescription
            )
            assertTrue(SecurityRole.entries.filter { it.roleSubgroup == subgroup }.containsAll(subgroup.roles))
        }
    }

    @ParameterizedTest
    @MethodSource("strings")
    fun `Security Role test`(
        locale: Locale,
        props: () -> Properties,
    ) {
        currentLocale = locale
        val properties = props()
        SecurityRole.entries.map { it.roleCode to it.roleOrder }.forEach { order ->
            val role = SecurityRole.entries.firstOrNull {
                it.roleCode == order.first && it.roleOrder == order.second
            }
            assertNotNull(role)
            assertEquals(properties.getProperty("security.role.${role!!.name.lowercase()}.name"), role.roleName)
            assertEquals(properties.getProperty("security.role.${role.name.lowercase()}.description"), role.roleDescription)
        }
    }

    @Test
    fun `Security Role by Code`() {
        assertEquals(MASTER_USER, SecurityRole.byCode(0))
    }

    @Test
    fun `Security Role by Code not found`() {
        assertNull(SecurityRole.byCode(987654321))
    }

    @Test
    fun `Security Role displayable`() {
        assertFalse(MASTER_USER.roleDisplayable)
        assertTrue(ADMINISTRATOR_USER.roleDisplayable)
    }

    @Test
    fun `Subgroups List`() {
        SecurityRoleGroup.entries.forEach { group ->
            val subgroups = SecurityRoleCategory.entries.filter { it.subgroupGroup == group }
            assertEquals(subgroups.size, group.subgroups.size)
            subgroups.forEach { subgroup ->
                assertTrue(group.subgroups.contains(subgroup))
            }
        }
    }

    @Test
    fun `Roles List`() {
        SecurityRoleCategory.entries.forEach { subgroup ->
            val roles = SecurityRole.entries.filter { it.roleSubgroup == subgroup }
            assertEquals(roles.size, subgroup.roles.size)
            roles.forEach { role ->
                assertTrue(subgroup.roles.contains(role))
            }
        }
    }

}
