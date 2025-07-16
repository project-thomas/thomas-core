package com.thomas.core.model.pagination

import java.time.OffsetDateTime

data class PageRequestPeriod(
    val createdStart: OffsetDateTime? = null,
    val createdEnd: OffsetDateTime? = null,
    val updatedStart: OffsetDateTime? = null,
    val updatedEnd: OffsetDateTime? = null,
    override val pageNumber: Long = 1,
    override val pageSize: Long = 10,
    override val pageSort: List<PageSort> = listOf()
) : PageRequestData
