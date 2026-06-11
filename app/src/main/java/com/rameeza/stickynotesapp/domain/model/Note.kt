package com.rameeza.stickynotesapp.domain.model

data class Note(
    val id: Int? = null,
    val title: String,
    val content: String,
    val timestamp: Long,
    val color: Int,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderlined: Boolean = false
)
