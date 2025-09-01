package com.thomas.core.model.security

import com.thomas.core.model.general.Gender
import com.thomas.core.model.general.Race
import com.thomas.core.model.general.UserType
import java.time.LocalDate
import java.util.UUID

data class SecurityUser(
    val userId: UUID,
    val firstName: String,
    val lastName: String,
    val mainEmail: String,
    val phoneNumber: String?,
    val profilePhoto: String?,
    val birthDate: LocalDate?,
    val userGender: Gender?,
    val userRace: Race?,
    val userType: UserType,
    val isActive: Boolean,
    val userGroups: Set<SecurityGroup>,
    val userRoles: Set<SecurityRole>,
) : SecurityInfo() {

    val fullName: String
        get() = "$firstName $lastName"

    val shortName: String
        get() = "$firstName ${lastName[0]}."

    val alternativeName: String
        get() = "${firstName[0]}. $lastName"

    override val securityRoles: Set<SecurityRole>
        get() = (userRoles + userGroups.map {
            it.securityRoles
        }.flatten()).distinct().toSet()

}
