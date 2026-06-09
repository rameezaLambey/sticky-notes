package com.rameeza.stickynotesapp.domain.use_case

import com.rameeza.stickynotesapp.domain.model.Note
import com.rameeza.stickynotesapp.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow

class GetNotes(
    private val repository: NoteRepository
) {
    operator fun invoke(): Flow<List<Note>> {
        return repository.getNotes()
    }
}
