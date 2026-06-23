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
import com.rameeza.stickynotesapp.util.VoiceToTextParser
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class AddEditNoteViewModel @Inject constructor(
    private val noteUseCases: NoteUseCases,
    val voiceToTextParser: VoiceToTextParser
) : ViewModel() {

    private val _noteTitle = mutableStateOf("")
    val noteTitle: State<String> = _noteTitle

    private val _noteContent = mutableStateOf("")
    val noteContent: State<String> = _noteContent

    private val _isRecordingTitle = mutableStateOf(false)
    val isRecordingTitle: State<Boolean> = _isRecordingTitle

    private val _isRecordingContent = mutableStateOf(false)
    val isRecordingContent: State<Boolean> = _isRecordingContent

    private var textBeforeRecording = ""

    init {
        viewModelScope.launch {
            voiceToTextParser.state.collect { state ->
                val isTitle = _isRecordingTitle.value
                val isContent = _isRecordingContent.value
                val newText = state.spokenText.trim()
                
                if (isTitle || isContent) {
                    if (newText.isNotBlank()) {
                        if (isTitle) {
                            _noteTitle.value = if (textBeforeRecording.isBlank()) newText else "${textBeforeRecording.trim()} $newText"
                        } else {
                            _noteContent.value = if (textBeforeRecording.isBlank()) newText else "${textBeforeRecording.trim()} $newText"
                        }
                    }
                    
                    if (!state.isSpeaking) {
                        _isRecordingTitle.value = false
                        _isRecordingContent.value = false
                        textBeforeRecording = ""
                    }
                }
            }
        }
    }

    fun toggleRecordTitle() {
        if (_isRecordingTitle.value) {
            voiceToTextParser.stopListening()
        } else {
            _isRecordingContent.value = false
            _isRecordingTitle.value = true
            textBeforeRecording = _noteTitle.value
            voiceToTextParser.startListening()
        }
    }

    fun toggleRecordContent() {
        if (_isRecordingContent.value) {
            voiceToTextParser.stopListening()
        } else {
            _isRecordingTitle.value = false
            _isRecordingContent.value = true
            textBeforeRecording = _noteContent.value
            voiceToTextParser.startListening()
        }
    }

    private val _noteColor = mutableIntStateOf(NoteColors.random().toArgb())
    val noteColor: State<Int> = _noteColor

    private val _isBold = mutableStateOf(false)
    val isBold: State<Boolean> = _isBold

    private val _isItalic = mutableStateOf(false)
    val isItalic: State<Boolean> = _isItalic

    private val _isUnderlined = mutableStateOf(false)
    val isUnderlined: State<Boolean> = _isUnderlined

    private val _isChecklist = mutableStateOf(false)
    val isChecklist: State<Boolean> = _isChecklist

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
                    _isChecklist.value = note.isChecklist
                }
            }
        }
    }

    fun onTitleChanged(title: String) {
        _noteTitle.value = title
    }

    fun onContentChanged(content: String) {
        if (_isChecklist.value) {
            val oldContent = _noteContent.value
            
            // Check if user is trying to type before a checkbox
            val lines = content.split("\n")
            val oldLines = oldContent.split("\n")
            
            if (lines.size == oldLines.size) {
                for (i in lines.indices) {
                    if (lines[i] != oldLines[i] && oldLines[i].startsWith("☐ ") && !lines[i].startsWith("☐ ")) {
                        // User tried to delete or type before the checkbox, prevent it or fix it
                        _noteContent.value = oldContent
                        return
                    }
                }
            }

            if (content.length > oldContent.length) {
                // Find where the change happened
                val diffIndex = content.zip(oldContent).indexOfFirst { it.first != it.second }
                val index = if (diffIndex == -1) oldContent.length else diffIndex
                
                if (content.getOrNull(index) == '\n') {
                    val prefix = content.substring(0, index + 1)
                    val suffix = content.substring(index + 1)
                    _noteContent.value = "${prefix}☐ $suffix"
                    return
                }
            }
        }
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

    fun toggleCheckItem(lineIndex: Int) {
        val lines = _noteContent.value.split("\n").toMutableList()
        if (lineIndex in lines.indices) {
            val line = lines[lineIndex]
            lines[lineIndex] = when {
                line.startsWith("☐ ") -> line.replaceFirst("☐ ", "☑ ")
                line.startsWith("☑ ") -> line.replaceFirst("☑ ", "☐ ")
                else -> line
            }
            _noteContent.value = lines.joinToString("\n")
        }
    }

    fun toggleChecklist() {
        _isChecklist.value = !_isChecklist.value
        val currentContent = _noteContent.value
        if (_isChecklist.value) {
            if (currentContent.isBlank()) {
                _noteContent.value = "☐ "
            } else {
                val lines = currentContent.split("\n")
                val newContent = lines.joinToString("\n") { line ->
                    if (line.isNotBlank() && !line.startsWith("☐ ")) "☐ $line" else line
                }
                _noteContent.value = newContent
            }
        } else {
            _noteContent.value = currentContent.replace("☐ ", "")
        }
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
                    isUnderlined = isUnderlined.value,
                    isChecklist = isChecklist.value
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
