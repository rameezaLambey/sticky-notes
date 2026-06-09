package com.rameeza.stickynotesapp.data.repository

import com.rameeza.stickynotesapp.data.local.NoteDao
import com.rameeza.stickynotesapp.data.mapper.toNote
import com.rameeza.stickynotesapp.data.mapper.toNoteEntity
import com.rameeza.stickynotesapp.domain.model.Note
import com.rameeza.stickynotesapp.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NoteRepositoryImpl(
    private val dao: NoteDao
) : NoteRepository {
    override fun getNotes(): Flow<List<Note>> {
        return dao.getAllNotes().map { entities ->
            entities.map { it.toNote() }
        }
    }

    override suspend fun getNoteById(id: Int): Note? {
        return dao.getNoteById(id)?.toNote()
    }

    override suspend fun insertNote(note: Note) {
        dao.insertNote(note.toNoteEntity())
    }

    override suspend fun deleteNote(note: Note) {
        dao.deleteNote(note.toNoteEntity())
    }
}
