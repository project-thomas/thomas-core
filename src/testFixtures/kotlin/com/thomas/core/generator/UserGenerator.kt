package com.thomas.core.generator

import com.thomas.core.data.securityRoles
import com.thomas.core.generator.GroupGenerator.generateSecurityGroup
import com.thomas.core.generator.PersonGenerator.generatePerson
import com.thomas.core.model.general.UserType
import com.thomas.core.model.general.UserType.MASTER
import com.thomas.core.model.security.SecurityRole
import com.thomas.core.model.security.SecurityUser

object UserGenerator {

    fun generateSecurityUser(
        userType: UserType = MASTER,
    ): SecurityUser = generatePerson().let {
        SecurityUser(
            userId = it.id,
            firstName = it.firstName,
            lastName = it.lastName,
            mainEmail = it.mainEmail,
            phoneNumber = it.phoneNumber,
            profilePhoto = null,
            birthDate = it.birthDate,
            userGender = it.userGender,
            userRace = it.userRace,
            userType = userType,
            isActive = listOf(true, false).random(),
            userRoles = setOf(),
            userGroups = setOf(),
        )
    }

    fun generateSecurityUserWithRoles(
        userType: UserType = MASTER,
        userRoles: Set<SecurityRole> = securityRoles,
        groupRoles: Set<SecurityRole> = securityRoles,
    ): SecurityUser = generateSecurityUser().copy(
        userType = userType,
        userRoles = userRoles,
        userGroups = setOf(generateSecurityGroup(groupRoles)),
    )

}
