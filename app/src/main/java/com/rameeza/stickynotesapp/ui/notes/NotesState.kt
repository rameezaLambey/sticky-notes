package com.rameeza.stickynotesapp.ui.notes

import com.rameeza.stickynotesapp.domain.model.Note

data class NotesState(
    val notes: List<Note> = emptyList()
)
