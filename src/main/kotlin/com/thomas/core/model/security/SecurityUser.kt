package com.thomas.core.model.security

import com.thomas.core.extension.merge
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
    override val securityOrganization: SecurityOrganization,
    val userGroups: Set<SecurityGroup>,
    override val securityUnits: Set<SecurityUnit>,
) : SecurityInfo() {

    val fullName: String
        get() = "$firstName $lastName"

    val shortName: String
        get() = "$firstName ${lastName[0]}."

    val alternativeName: String
        get() = "${firstName[0]}. $lastName"

    override val organizationRoles: Set<SecurityOrganizationRole>
        get() = (super.organizationRoles + userGroups.organizationRoles()).distinct().toSet()

    override val unitRoles: Set<SecurityUnitRole>
        get() = (super.unitRoles + userGroups.map { it.unitRoles }.flatten()).distinct().toSet()

    override val unitsRoles: Map<UUID, Set<SecurityUnitRole>>
        get() = (userGroups.map { it.unitsRoles } + super.unitsRoles).reduce { acc, map -> acc.merge(map) }

    private fun Set<SecurityGroup>.organizationRoles() = this.map { it.organizationRoles }.flatten()

}
