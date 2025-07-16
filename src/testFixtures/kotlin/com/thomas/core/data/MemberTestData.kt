package com.thomas.core.data

import java.util.UUID

data class UnitTestData(
    val id: UUID,
    val unitName: String,
    val unitDescription: String?,
    val isActive: Boolean,
)
