package com.rameeza.stickynotesapp.ui.notes

import com.rameeza.stickynotesapp.domain.model.Note

data class NotesState(
    val notes: List<Note> = emptyList(),
    val selectedNoteIds: Set<Int> = emptySet(),
    val isSelectionMode: Boolean = false
)
