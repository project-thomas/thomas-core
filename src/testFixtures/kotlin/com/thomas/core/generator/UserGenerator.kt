package com.thomas.core.generator

import com.thomas.core.generator.GroupGenerator.generateSecurityGroupSet
import com.thomas.core.generator.OrganizationUnitGenerator.generateSecurityOrganization
import com.thomas.core.generator.OrganizationUnitGenerator.generateSecurityUnitSet
import com.thomas.core.generator.PersonGenerator.generatePerson
import com.thomas.core.generator.RoleGenerator.generateOrganizationRoles
import com.thomas.core.model.general.UserType
import com.thomas.core.model.general.UserType.MASTER
import com.thomas.core.model.security.SecurityOrganizationRole
import com.thomas.core.model.security.SecurityUnitRole
import com.thomas.core.model.security.SecurityUser

object UserGenerator {

    fun generateSecurityUser(
        userType: UserType = MASTER,
    ): SecurityUser = generatePerson().let {
        val organization = generateSecurityOrganization()
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
            securityOrganization = organization,
            userGroups = generateSecurityGroupSet().map { group ->
                group.copy(
                    securityOrganization = organization.copy(
                        organizationRoles = generateOrganizationRoles()
                    )
                )
            }.toSet(),
            securityUnits = generateSecurityUnitSet(),
        )
    }

    fun generateSecurityUserWithRoles(
        userOrganizationRoles: Set<SecurityOrganizationRole> = setOf(),
        userUnitRoles: Set<SecurityUnitRole> = setOf(),
        groupOrganizationRoles: Set<SecurityOrganizationRole> = setOf(),
        groupUnitRoles: Set<SecurityUnitRole> = setOf(),
    ): SecurityUser = generateSecurityUser().let { user ->
        user.copy(
            securityOrganization = user.securityOrganization.copy(
                organizationRoles = userOrganizationRoles
            ),
            securityUnits = user.securityUnits.map { unit ->
                unit.copy(
                    unitRoles = userUnitRoles
                )
            }.toSet(),
            userGroups = user.userGroups.map { group ->
                group.copy(
                    securityOrganization = group.securityOrganization.copy(
                        organizationRoles = groupOrganizationRoles
                    ),
                    securityUnits = group.securityUnits.map { unit ->
                        unit.copy(
                            unitRoles = groupUnitRoles
                        )
                    }.toSet()
                )
            }.toSet(),
        )
    }

}