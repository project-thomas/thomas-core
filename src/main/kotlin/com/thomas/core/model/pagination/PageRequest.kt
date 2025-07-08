package com.thomas.core.model.pagination

data class PageRequest(
    override val pageNumber: Long = 1,
    override val pageSize: Long = 10,
    override val pageSort: List<PageSort> = listOf()
) : PageRequestData
