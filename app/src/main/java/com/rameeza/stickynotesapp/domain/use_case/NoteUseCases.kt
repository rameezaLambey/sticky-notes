package com.rameeza.stickynotesapp.domain.use_case

data class NoteUseCases(
    val getNotes: GetNotes,
    val deleteNote: DeleteNote,
    val deleteNotes: DeleteNotes,
    val addNote: AddNote,
    val getNote: GetNote
)
