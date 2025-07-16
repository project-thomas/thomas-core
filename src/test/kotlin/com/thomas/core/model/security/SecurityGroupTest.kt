package com.thomas.core.model.security

import com.thomas.core.context.SessionContextHolder.currentUnit
import com.thomas.core.generator.OrganizationUnitGenerator.generateSecurityOrganization
import com.thomas.core.generator.OrganizationUnitGenerator.generateSecurityUnit
import com.thomas.core.model.security.SecurityOrganizationRole.MASTER_ROLE
import com.thomas.core.model.security.SecurityOrganizationRole.ORGANIZATION_ALL
import com.thomas.core.model.security.SecurityOrganizationRole.UNIT_CREATE
import com.thomas.core.model.security.SecurityUnitRole.UNIT_ALL
import com.thomas.core.util.StringUtils.randomString
import java.util.UUID
import java.util.UUID.randomUUID
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource

class SecurityGroupTest {

    companion object {

        @JvmStatic
        fun unitAdministratorRoles() = listOf(
            Arguments.of(setOf<SecurityOrganizationRole>(), setOf(UNIT_ALL)),
            Arguments.of(setOf(MASTER_ROLE), setOf<SecurityUnitRole>()),
            Arguments.of(setOf(ORGANIZATION_ALL), setOf<SecurityUnitRole>()),
        )

    }

    @Test
    fun `SecurityGroup is master`() {
        SecurityGroup(
            groupId = randomUUID(),
            groupName = randomString(),
            securityOrganization = generateSecurityOrganization().copy(
                organizationRoles = setOf(MASTER_ROLE)
            ),
            securityUnits = setOf(),
        ).apply {
            assertTrue(this.isMaster)
        }
    }

    @ParameterizedTest
    @EnumSource(SecurityOrganizationRole::class, names = ["MASTER_ROLE", "ORGANIZATION_ALL"])
    fun `SecurityGroup is administrator`(role: SecurityOrganizationRole) {
        SecurityGroup(
            groupId = randomUUID(),
            groupName = randomString(),
            securityOrganization = generateSecurityOrganization().copy(
                organizationRoles = setOf(role)
            ),
            securityUnits = setOf(),
        ).apply {
            assertTrue(this.isAdministrator)
        }
    }

    @ParameterizedTest
    @MethodSource("unitAdministratorRoles")
    fun `SecurityGroup is unit administrator`(
        organizationRoles: Set<SecurityOrganizationRole>,
        unitRoles: Set<SecurityUnitRole>
    ) {
        val unitId = randomUUID()
        currentUnit = unitId
        SecurityGroup(
            groupId = randomUUID(),
            groupName = randomString(),
            securityOrganization = generateSecurityOrganization().copy(
                organizationRoles = organizationRoles
            ),
            securityUnits = setOf(
                generateSecurityUnit().copy(
                    unitId = unitId,
                    unitRoles = unitRoles
                ),
            ),
        ).apply {
            assertTrue(this.isUnitAdministrator)
        }
    }

    @Test
    fun `SecurityGroup does not have master role`() {
        SecurityGroup(
            groupId = randomUUID(),
            groupName = randomString(),
            securityOrganization = generateSecurityOrganization().copy(
                organizationRoles = setOf(UNIT_CREATE)
            ),
            securityUnits = setOf(),
        ).apply {
            assertFalse(this.isMaster)
        }
    }

}
