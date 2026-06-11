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

    private val _isBold = mutableStateOf(false)
    val isBold: State<Boolean> = _isBold

    private val _isItalic = mutableStateOf(false)
    val isItalic: State<Boolean> = _isItalic

    private val _isUnderlined = mutableStateOf(false)
    val isUnderlined: State<Boolean> = _isUnderlined

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
                    _isBold.value = note.isBold
                    _isItalic.value = note.isItalic
                    _isUnderlined.value = note.isUnderlined
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

    fun toggleBold() {
        _isBold.value = !_isBold.value
    }

    fun toggleItalic() {
        _isItalic.value = !_isItalic.value
    }

    fun toggleUnderline() {
        _isUnderlined.value = !_isUnderlined.value
    }

    fun saveNote() {
        if (noteTitle.value.isBlank() && noteContent.value.isBlank()) {
            viewModelScope.launch {
                _eventFlow.emit(UiEvent.ShowEmptyNoteDialog)
            }
            return
        }
        viewModelScope.launch {
            noteUseCases.addNote(
                Note(
                    title = noteTitle.value,
                    content = noteContent.value,
                    timestamp = System.currentTimeMillis(),
                    color = noteColor.value,
                    id = currentNoteId,
                    isBold = isBold.value,
                    isItalic = isItalic.value,
                    isUnderlined = isUnderlined.value
                )
            )
            _eventFlow.emit(UiEvent.SaveNote)
        }
    }

    sealed class UiEvent {
        object SaveNote : UiEvent()
        object ShowEmptyNoteDialog : UiEvent()
    }
}
