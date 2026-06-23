package com.rameeza.stickynotesapp.domain.use_case

import com.rameeza.stickynotesapp.domain.model.Note
import com.rameeza.stickynotesapp.domain.repository.NoteRepository

class DeleteNotes(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(notes: List<Note>) {
        repository.deleteNotes(notes)
    }
}
