package com.rameeza.stickynotesapp.ui.add_edit_note

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rameeza.stickynotesapp.domain.model.Note
import com.rameeza.stickynotesapp.domain.use_case.NoteUseCases
import com.rameeza.stickynotesapp.ui.theme.NoteColors
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class AddEditNoteViewModel @Inject constructor(
    private val noteUseCases: NoteUseCases
) : ViewModel() {

    private val _noteTitle = mutableStateOf("")
    val noteTitle: State<String> = _noteTitle

    private val _noteContent = mutableStateOf("")
    val noteContent: State<String> = _noteContent

    private val _noteColor = mutableIntStateOf(NoteColors.random().toArgb())
    val noteColor: State<Int> = _noteColor

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentNoteId: Int? = null

    fun loadNote(noteId: Int?) {
        if (noteId != null && noteId != -1 && currentNoteId == null) {
            viewModelScope.launch {
                noteUseCases.getNote(noteId)?.also { note ->
                    currentNoteId = note.id
                    _noteTitle.value = note.title
                    _noteContent.value = note.content
                    _noteColor.intValue = note.color
                }
            }
        }
    }

    fun onTitleChanged(title: String) {
        _noteTitle.value = title
    }

    fun onContentChanged(content: String) {
        _noteContent.value = content
    }

    fun onColorChanged(color: Int) {
        _noteColor.intValue = color
    }

    fun saveNote() {
        viewModelScope.launch {
            noteUseCases.addNote(
                Note(
                    title = noteTitle.value,
                    content = noteContent.value,
                    timestamp = System.currentTimeMillis(),
                    color = noteColor.value,
                    id = currentNoteId
                )
            )
            _eventFlow.emit(UiEvent.SaveNote)
        }
    }

    sealed class UiEvent {
        object SaveNote : UiEvent()
    }
}
