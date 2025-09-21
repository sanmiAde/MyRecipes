package com.sanmiade.myrecipes.utils

import org.springframework.data.domain.Page

data class PagedResponse<T>(
    val content: List<T>,
    val prevPage: Int?,
    val currentPage: Int,
    val nextPage: Int?,
    val totalPages: Int,
    val totalElements: Long
)

fun <T> fromSpringPage(page: Page<T>): PagedResponse<T> {
    val current = page.number
    val totalPages = page.totalPages
    val totalElements = page.totalElements

    return PagedResponse(
        content = page.content,
        prevPage = if (current > 0) current - 1 else null,
        currentPage = current,
        nextPage = if (current < totalPages - 1) current + 1 else null,
        totalPages = totalPages,
        totalElements = totalElements
    )
}
