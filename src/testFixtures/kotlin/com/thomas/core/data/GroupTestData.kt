package com.thomas.core.data

import java.util.UUID

data class GroupTestData(
    val id: UUID,
    val groupName: String,
    val groupDescription: String?,
    val isActive: Boolean,
)
