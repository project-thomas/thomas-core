package com.thomas.core.data

import com.thomas.core.model.general.Gender
import com.thomas.core.model.general.Race
import com.thomas.core.model.general.UserType
import com.thomas.core.model.security.SecurityGroup
import com.thomas.core.model.security.SecurityOrganization
import com.thomas.core.model.security.SecurityOrganizationRole
import com.thomas.core.model.security.SecurityOrganizationRole.MASTER_ROLE
import com.thomas.core.model.security.SecurityUnit
import com.thomas.core.model.security.SecurityUnitRole
import com.thomas.core.model.security.SecurityUser
import com.thomas.core.util.NumberUtils.randomInteger
import com.thomas.core.util.StringUtils.randomEmail
import com.thomas.core.util.StringUtils.randomPhone
import com.thomas.core.util.StringUtils.randomString
import java.time.LocalDate
import java.util.UUID.randomUUID

val securityOrganization: SecurityOrganization
    get() = SecurityOrganization(
        organizationId = randomUUID(),
        organizationName = randomString(),
        organizationRoles = setOf(),
    )

val securityOrganizationRoles: SecurityOrganization
    get() = SecurityOrganization(
        organizationId = randomUUID(),
        organizationName = randomString(),
        organizationRoles = SecurityOrganizationRole.entries.let {
            it.shuffled().take(randomInteger(1, it.size)).toSet()
        },
    )

val securityUnitRoles: SecurityUnit
    get() = SecurityUnit(
        unitId = randomUUID(),
        unitName = randomString(numbers = false),
        unitRoles = SecurityUnitRole.entries.let {
            it.shuffled().take(randomInteger(1, it.size)).toSet()
        },
    )

val securityUserMaster: SecurityUser
    get() = securityUser.copy(
        userType = UserType.MASTER,
        securityOrganization = securityOrganization.copy(
            organizationRoles = setOf(MASTER_ROLE)
        ),
        isActive = true,
    )

val securityUser: SecurityUser
    get() = SecurityUser(
        userId = randomUUID(),
        firstName = randomString(numbers = false),
        lastName = randomString(numbers = false),
        mainEmail = randomEmail(),
        phoneNumber = randomPhone(),
        profilePhoto = null,
        birthDate = LocalDate.now(),
        userGender = Gender.entries.random(),
        userRace = Race.entries.random(),
        userType = UserType.entries.random(),
        isActive = true,
        securityOrganization = securityOrganization,
        userGroups = setOf(),
        securityUnits = setOf(),
    )

val securityUserRoles: SecurityUser
    get() = securityOrganizationRoles.let { organization ->
        securityUser.copy(
            securityOrganization = organization,
            userGroups = (1..3).map {
                securityGroupRoles.copy(
                    securityOrganization = organization.copy(
                        organizationRoles = SecurityOrganizationRole.entries.let {
                            it.shuffled().take(randomInteger(1, it.size)).toSet()
                        }
                    )
                )
            }.toSet(),
            securityUnits = (1..3).map { securityUnitRoles }.toSet(),
        )
    }

val securityGroupRoles: SecurityGroup
    get() = SecurityGroup(
        groupId = randomUUID(),
        groupName = randomString(numbers = false),
        securityOrganization = securityOrganization,
        securityUnits = (1..3).map { securityUnitRoles }.toSet(),
    )


