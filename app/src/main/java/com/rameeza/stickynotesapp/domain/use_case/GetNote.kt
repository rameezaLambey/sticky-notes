package com.rameeza.stickynotesapp.domain.use_case

import com.rameeza.stickynotesapp.domain.model.Note
import com.rameeza.stickynotesapp.domain.repository.NoteRepository

class GetNote(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(id: Int): Note? {
        return repository.getNoteById(id)
    }
}
