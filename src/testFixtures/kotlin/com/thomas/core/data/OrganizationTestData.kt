package com.thomas.core.data

import java.util.UUID

data class OrganizationTestData(
    val id: UUID,
    val organizationName: String,
    val organizationDescription: String?,
    val isActive: Boolean,
)
