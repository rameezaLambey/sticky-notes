package com.rameeza.stickynotesapp.data.mapper

import com.rameeza.stickynotesapp.data.local.NoteEntity
import com.rameeza.stickynotesapp.domain.model.Note

fun NoteEntity.toNote(): Note {
    return Note(
        id = id,
        title = title,
        content = content,
        timestamp = timestamp,
        color = color
    )
}

fun Note.toNoteEntity(): NoteEntity {
    return NoteEntity(
        id = id ?: 0,
        title = title,
        content = content,
        timestamp = timestamp,
        color = color
    )
}
