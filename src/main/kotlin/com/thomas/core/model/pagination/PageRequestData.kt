package com.thomas.core.model.pagination

interface PageRequestData {
    val pageNumber: Long
    val pageSize: Long
    val pageSort: List<PageSort>
}
