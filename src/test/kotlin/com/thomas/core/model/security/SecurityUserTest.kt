package com.thomas.core.model.security

import com.thomas.core.context.SessionContextHolder.clearContext
import com.thomas.core.context.SessionContextHolder.currentUnit
import com.thomas.core.generator.GroupGenerator.generateSecurityGroup
import com.thomas.core.generator.OrganizationUnitGenerator.generateSecurityOrganization
import com.thomas.core.generator.OrganizationUnitGenerator.generateSecurityUnit
import com.thomas.core.generator.UserGenerator.generateSecurityUser
import com.thomas.core.model.security.SecurityOrganizationRole.GROUP_CREATE
import com.thomas.core.model.security.SecurityOrganizationRole.GROUP_READ
import com.thomas.core.model.security.SecurityOrganizationRole.GROUP_UPDATE
import com.thomas.core.model.security.SecurityOrganizationRole.MASTER_ROLE
import com.thomas.core.model.security.SecurityOrganizationRole.ORGANIZATION_ALL
import com.thomas.core.model.security.SecurityOrganizationRole.UNIT_READ
import com.thomas.core.model.security.SecurityOrganizationRole.USER_CREATE
import com.thomas.core.model.security.SecurityOrganizationRole.USER_READ
import com.thomas.core.model.security.SecurityOrganizationRole.USER_UPDATE
import com.thomas.core.model.security.SecurityUnitRole.COA_CREATE
import com.thomas.core.model.security.SecurityUnitRole.COA_READ
import com.thomas.core.model.security.SecurityUnitRole.COA_UPDATE
import com.thomas.core.model.security.SecurityUnitRole.UNIT_ALL
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource

class SecurityUserTest {

    companion object {

        @JvmStatic
        fun unitAdministratorRoles() = listOf(
            Arguments.of(setOf<SecurityOrganizationRole>(), setOf(UNIT_ALL)),
            Arguments.of(setOf(MASTER_ROLE), setOf<SecurityUnitRole>()),
            Arguments.of(setOf(ORGANIZATION_ALL), setOf<SecurityUnitRole>()),
        )

    }

    @BeforeEach
    internal fun setUp() {
        clearContext()
    }

    @Test
    fun `Security User full name`() {
        val user = generateSecurityUser()
        assertEquals("${user.firstName} ${user.lastName}", user.fullName)
    }

    @Test
    fun `Security User short name`() {
        val user = generateSecurityUser()
        assertEquals("${user.firstName} ${user.lastName[0]}.", user.shortName)
    }

    @Test
    fun `Security User alternative name`() {
        val user = generateSecurityUser()
        assertEquals("${user.firstName[0]}. ${user.lastName}", user.alternativeName)
    }

    @Test
    fun `Security User is master`() {
        val user = generateSecurityUser().copy(
            securityOrganization = generateSecurityOrganization().copy(
                organizationRoles = setOf(MASTER_ROLE)
            ),
            userGroups = setOf()
        )

        assertTrue(user.isMaster)
    }

    @ParameterizedTest
    @EnumSource(SecurityOrganizationRole::class, names = ["MASTER_ROLE", "ORGANIZATION_ALL"])
    fun `Security User is administrator`(role: SecurityOrganizationRole) {
        val user = generateSecurityUser().copy(
            securityOrganization = generateSecurityOrganization().copy(
                organizationRoles = setOf(role)
            ),
            userGroups = setOf()
        )

        assertTrue(user.isAdministrator)
    }

    @ParameterizedTest
    @MethodSource("unitAdministratorRoles")
    fun `Security User is unit administrator`(
        organizationRoles: Set<SecurityOrganizationRole>,
        unitRoles: Set<SecurityUnitRole>
    ) {
        val unitId = UUID.randomUUID()
        currentUnit = unitId
        val user = generateSecurityUser().copy(
            securityOrganization = generateSecurityOrganization().copy(
                organizationRoles = organizationRoles
            ),
            securityUnits = setOf(
                generateSecurityUnit().copy(
                    unitId = unitId,
                    unitRoles = unitRoles
                ),
            ),
            userGroups = setOf()
        )

        assertTrue(user.isUnitAdministrator)
    }

    @Test
    fun `Security User is master by group`() {
        val user = generateSecurityUser().copy(
            securityOrganization = generateSecurityOrganization().copy(
                organizationRoles = setOf()
            ),
            userGroups = setOf(
                generateSecurityGroup().copy(
                    securityOrganization = generateSecurityOrganization().copy(
                        organizationRoles = setOf(MASTER_ROLE)
                    )
                )
            )
        )

        assertTrue(user.isMaster)
    }

    @ParameterizedTest
    @EnumSource(SecurityOrganizationRole::class, names = ["MASTER_ROLE", "ORGANIZATION_ALL"])
    fun `Security User is administrator by group`(role: SecurityOrganizationRole) {
        val user = generateSecurityUser().copy(
            securityOrganization = generateSecurityOrganization().copy(
                organizationRoles = setOf()
            ),
            userGroups = setOf(
                generateSecurityGroup().copy(
                    securityOrganization = generateSecurityOrganization().copy(
                        organizationRoles = setOf(role)
                    )
                )
            )
        )

        assertTrue(user.isAdministrator)
    }

    @ParameterizedTest
    @MethodSource("unitAdministratorRoles")
    fun `Security User is unit administrator by group`(
        organizationRoles: Set<SecurityOrganizationRole>,
        unitRoles: Set<SecurityUnitRole>
    ) {
        val unitId = UUID.randomUUID()
        currentUnit = unitId
        val user = generateSecurityUser().copy(
            securityOrganization = generateSecurityOrganization().copy(
                organizationRoles = setOf()
            ),
            securityUnits = setOf(),
            userGroups = setOf(
                generateSecurityGroup().copy(
                    securityOrganization = generateSecurityOrganization().copy(
                        organizationRoles = organizationRoles
                    ),
                    securityUnits = setOf(
                        generateSecurityUnit().copy(
                            unitId = unitId,
                            unitRoles = unitRoles
                        ),
                    ),
                ),
            )
        )

        assertTrue(user.isUnitAdministrator)
    }

    @Test
    fun `Security User Organization roles`() {
        val user = generateSecurityUser().copy(
            securityOrganization = generateSecurityOrganization().copy(
                organizationRoles = setOf(USER_READ, USER_CREATE, USER_UPDATE)
            ),
            userGroups = setOf(
                generateSecurityGroup().copy(
                    securityOrganization = generateSecurityOrganization().copy(
                        organizationRoles = setOf(GROUP_READ, GROUP_CREATE, GROUP_UPDATE)
                    )
                ),
                generateSecurityGroup().copy(
                    securityOrganization = generateSecurityOrganization().copy(
                        organizationRoles = setOf(USER_READ, GROUP_READ, UNIT_READ)
                    )
                ),
            )
        )

        assertEquals(7, user.organizationRoles.size)
        assertTrue(
            user.organizationRoles.containsAll(
                setOf(
                    USER_READ, USER_CREATE, USER_UPDATE,
                    GROUP_READ, GROUP_CREATE, GROUP_UPDATE,
                    UNIT_READ
                )
            )
        )
    }

    @Test
    fun `Security User Unit roles without current Unit`() {
        val user = generateSecurityUser()
        assertTrue(user.unitRoles.isEmpty())
    }

    @Test
    fun `Security User Unit roles`() {
        val unitId = UUID.randomUUID()
        currentUnit = unitId
        val user = generateSecurityUser().copy(
            securityUnits = setOf(
                generateSecurityUnit().copy(
                    unitRoles = setOf()
                ),
                generateSecurityUnit().copy(
                    unitId = unitId,
                    unitRoles = setOf(COA_READ)
                ),
            ),
            userGroups = setOf(
                generateSecurityGroup().copy(
                    securityUnits = setOf()
                ),
                generateSecurityGroup().copy(
                    securityUnits = setOf(
                        generateSecurityUnit().copy(
                            unitId = unitId,
                            unitRoles = setOf(COA_READ)
                        ),
                    )
                ),
                generateSecurityGroup().copy(
                    securityUnits = setOf(
                        generateSecurityUnit().copy(
                            unitRoles = setOf(COA_READ)
                        ),
                        generateSecurityUnit().copy(
                            unitId = unitId,
                            unitRoles = setOf(COA_CREATE)
                        ),
                    )
                ),
                generateSecurityGroup().copy(
                    securityUnits = setOf(
                        generateSecurityUnit().copy(
                            unitRoles = setOf(COA_UPDATE)
                        ),
                        generateSecurityUnit().copy(
                            unitId = unitId,
                            unitRoles = setOf(COA_CREATE)
                        ),
                    )
                ),
            )
        )

        assertEquals(2, user.unitRoles.size)
        assertTrue(user.unitRoles.containsAll(setOf(COA_READ, COA_CREATE)))
    }

    @Test
    fun `Security User Unit roles map`() {
        val unitId = UUID.randomUUID()
        currentUnit = unitId
        val user = generateSecurityUser().copy(
            securityUnits = setOf(
                generateSecurityUnit().copy(
                    unitRoles = setOf()
                ),
                generateSecurityUnit().copy(
                    unitId = unitId,
                    unitRoles = setOf(COA_READ)
                ),
            ),
            userGroups = setOf(
                generateSecurityGroup().copy(
                    securityUnits = setOf()
                ),
                generateSecurityGroup().copy(
                    securityUnits = setOf(
                        generateSecurityUnit().copy(
                            unitId = unitId,
                            unitRoles = setOf(COA_READ)
                        ),
                    )
                ),
                generateSecurityGroup().copy(
                    securityUnits = setOf(
                        generateSecurityUnit().copy(
                            unitRoles = setOf(COA_READ)
                        ),
                        generateSecurityUnit().copy(
                            unitId = unitId,
                            unitRoles = setOf(COA_CREATE)
                        ),
                    )
                ),
                generateSecurityGroup().copy(
                    securityUnits = setOf(
                        generateSecurityUnit().copy(
                            unitRoles = setOf(COA_UPDATE)
                        ),
                        generateSecurityUnit().copy(
                            unitId = unitId,
                            unitRoles = setOf(COA_CREATE)
                        ),
                    )
                ),
            )
        )

        assertEquals(4, user.unitsRoles.size)
    }

    @Test
    fun `Security User current roles`() {
        val unitId = UUID.randomUUID()
        currentUnit = unitId
        val user = generateSecurityUser().copy(
            securityOrganization = generateSecurityOrganization().copy(
                organizationRoles = setOf(USER_READ, USER_CREATE, USER_UPDATE)
            ),
            securityUnits = setOf(
                generateSecurityUnit().copy(
                    unitRoles = setOf()
                ),
                generateSecurityUnit().copy(
                    unitId = unitId,
                    unitRoles = setOf(COA_READ)
                ),
            ),
            userGroups = setOf(
                generateSecurityGroup().copy(
                    securityOrganization = generateSecurityOrganization().copy(
                        organizationRoles = setOf(GROUP_READ, GROUP_CREATE, GROUP_UPDATE)
                    ),
                    securityUnits = setOf()
                ),
                generateSecurityGroup().copy(
                    securityOrganization = generateSecurityOrganization().copy(
                        organizationRoles = setOf()
                    ),
                    securityUnits = setOf(
                        generateSecurityUnit().copy(
                            unitId = unitId,
                            unitRoles = setOf(COA_READ)
                        ),
                    )
                ),
                generateSecurityGroup().copy(
                    securityOrganization = generateSecurityOrganization().copy(
                        organizationRoles = setOf(USER_READ, GROUP_READ, UNIT_READ)
                    ),
                    securityUnits = setOf(
                        generateSecurityUnit().copy(
                            unitRoles = setOf(COA_READ)
                        ),
                        generateSecurityUnit().copy(
                            unitId = unitId,
                            unitRoles = setOf(COA_CREATE)
                        ),
                    )
                ),
                generateSecurityGroup().copy(
                    securityOrganization = generateSecurityOrganization().copy(
                        organizationRoles = setOf(USER_READ, GROUP_READ, UNIT_READ)
                    ),
                    securityUnits = setOf(
                        generateSecurityUnit().copy(
                            unitRoles = setOf(COA_UPDATE)
                        ),
                        generateSecurityUnit().copy(
                            unitId = unitId,
                            unitRoles = setOf(COA_CREATE)
                        ),
                    )
                ),
            )
        )

        assertEquals(9, user.currentRoles.size)
        assertTrue(
            user.currentRoles.containsAll(
                setOf(
                    USER_READ, USER_CREATE, USER_UPDATE,
                    GROUP_READ, GROUP_CREATE, GROUP_UPDATE,
                    UNIT_READ,
                    COA_READ, COA_CREATE
                )
            )
        )
    }

}