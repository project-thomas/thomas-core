package com.thomas.core.data

import com.thomas.core.model.general.Gender
import com.thomas.core.model.general.Race
import com.thomas.core.model.general.UserType
import com.thomas.core.model.general.UserType.ADMINISTRATOR
import com.thomas.core.model.general.UserType.MASTER
import com.thomas.core.model.security.SecurityGroup
import com.thomas.core.model.security.SecurityRole
import com.thomas.core.model.security.SecurityRole.ADMINISTRATOR_USER
import com.thomas.core.model.security.SecurityRole.MASTER_USER
import com.thomas.core.model.security.SecurityUser
import com.thomas.core.util.NumberUtils.randomInteger
import com.thomas.core.util.StringUtils.randomEmail
import com.thomas.core.util.StringUtils.randomPhone
import com.thomas.core.util.StringUtils.randomString
import java.time.LocalDate
import java.util.UUID.randomUUID

val securityRoles: Set<SecurityRole>
    get() = SecurityRole.entries.let {
        it.shuffled().take(randomInteger(1, it.size)).toSet()
    }

val securityUserMaster: SecurityUser
    get() = securityUser.copy(
        userType = MASTER,
        userRoles = setOf(MASTER_USER),
        isActive = true,
    )

val securityUserAdministrator: SecurityUser
    get() = securityUser.copy(
        userType = ADMINISTRATOR,
        userRoles = setOf(ADMINISTRATOR_USER),
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
        userGroups = setOf(),
        userRoles = setOf(),
    )

val securityUserRoles: SecurityUser
    get() = securityUser.copy(
        userRoles = securityRoles,
    )

val securityUserGroupRoles: SecurityUser
    get() = securityUser.copy(
        userGroups = (1..3).map {
            securityGroupRoles.copy(
                securityRoles = securityRoles
            )
        }.toSet(),
    )

val securityUserFull: SecurityUser
    get() = securityUser.copy(
        userRoles = securityRoles,
        userGroups = (1..3).map {
            securityGroupRoles.copy(
                securityRoles = securityRoles
            )
        }.toSet(),
    )

val securityGroup: SecurityGroup
    get() = SecurityGroup(
        groupId = randomUUID(),
        groupName = randomString(numbers = false),
        securityRoles = setOf(),
    )

val securityGroupRoles: SecurityGroup
    get() = securityGroup.copy(
        securityRoles = securityRoles,
    )


