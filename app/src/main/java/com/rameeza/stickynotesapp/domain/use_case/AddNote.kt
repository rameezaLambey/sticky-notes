package com.rameeza.stickynotesapp.domain.use_case

import com.rameeza.stickynotesapp.domain.model.Note
import com.rameeza.stickynotesapp.domain.repository.NoteRepository

class AddNote(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note) {
        repository.insertNote(note)
    }
}
