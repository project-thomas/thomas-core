package com.thomas.core.model.security

import java.util.UUID

data class SecurityUnit(
    val unitId: UUID,
    val unitName: String,
    val unitRoles: Set<SecurityUnitRole>,
)
