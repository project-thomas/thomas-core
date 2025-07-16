package com.thomas.core.model.security

import java.util.UUID

data class SecurityGroup(
    val groupId: UUID,
    val groupName: String,
    override val securityOrganization: SecurityOrganization,
    override val securityUnits: Set<SecurityUnit>,
) : SecurityInfo()
