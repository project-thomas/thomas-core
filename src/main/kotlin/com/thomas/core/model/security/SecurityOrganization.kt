package com.thomas.core.model.security

import java.util.UUID

data class SecurityOrganization(
    val organizationId: UUID,
    val organizationName: String,
    val organizationRoles: Set<SecurityOrganizationRole>,
)
