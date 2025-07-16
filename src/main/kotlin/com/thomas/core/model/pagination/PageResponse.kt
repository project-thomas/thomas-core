package com.thomas.core.model.pagination

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

data class PageResponse<T>(
    val contentList: List<T>,
    val totalItems: Long,
    val totalPages: Long,
    val pageNumber: Long,
    val pageSize: Long,
    val firstPage: Boolean,
    val lastPage: Boolean,
    val sortedBy: List<PageSort>
) {

    companion object {
        fun <T> of(contentList: List<T>, pageable: PageRequestData, totalItems: Long): PageResponse<T> {
            val addPage = (totalItems % pageable.pageSize).takeIf { it == 0L } ?: 1L
            val totalPages = (totalItems / pageable.pageSize) + addPage

            return PageResponse(
                contentList,
                totalItems,
                totalPages,
                pageable.pageNumber,
                pageable.pageSize,
                pageable.pageNumber == 1L,
                (pageable.pageNumber == totalPages) || (totalPages == 0L),
                pageable.pageSort
            )
        }
    }

    suspend fun <E> map(map: suspend (T) -> E) = coroutineScope {
        PageResponse(
            contentList.map { async { map(it) } }.awaitAll(),
            totalItems,
            totalPages,
            pageNumber,
            pageSize,
            firstPage,
            lastPage,
            sortedBy
        )
    }

}
