package com.thomas.core.model.security

import com.thomas.core.model.security.SecurityRole.ADMINISTRATOR_USER
import com.thomas.core.model.security.SecurityRole.MASTER_USER
import com.thomas.core.util.StringUtils.randomString
import java.util.UUID.randomUUID
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SecurityGroupTest {

    @Test
    fun `SecurityGroup is master`() {
        SecurityGroup(
            groupId = randomUUID(),
            groupName = randomString(),
            securityRoles = setOf(MASTER_USER),
        ).apply {
            assertTrue(this.isMaster)
            assertTrue(this.isAdministrator)
        }
    }

    @Test
    fun `SecurityGroup is administrator`() {
        SecurityGroup(
            groupId = randomUUID(),
            groupName = randomString(),
            securityRoles = setOf(ADMINISTRATOR_USER),
        ).apply {
            assertFalse(this.isMaster)
            assertTrue(this.isAdministrator)
        }
    }

    @Test
    fun `SecurityGroup does not have master nor administrator role`() {
        SecurityGroup(
            groupId = randomUUID(),
            groupName = randomString(),
            securityRoles = SecurityRole.entries.filter { it != MASTER_USER && it != ADMINISTRATOR_USER }.toSet(),
        ).apply {
            assertFalse(this.isMaster)
            assertFalse(this.isAdministrator)
        }
    }

}
