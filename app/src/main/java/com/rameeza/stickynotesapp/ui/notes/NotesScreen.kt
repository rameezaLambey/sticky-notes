package com.rameeza.stickynotesapp.ui.notes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.rameeza.stickynotesapp.R
import com.rameeza.stickynotesapp.domain.model.Note
import com.rameeza.stickynotesapp.ui.util.Screen

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    navController: NavController,
    viewModelFactory: ViewModelProvider.Factory,
    viewModel: NotesViewModel = viewModel(factory = viewModelFactory)
) {
    val context = LocalContext.current
    val state = viewModel.state.value
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var showBatchDeleteDialog by remember { mutableStateOf(false) }

    if (noteToDelete != null) {
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text(stringResource(R.string.delete_note_title)) },
            text = { Text(stringResource(R.string.delete_note_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        noteToDelete?.let { viewModel.deleteNote(it) }
                        noteToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showBatchDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showBatchDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_notes_title)) },
            text = { Text(stringResource(R.string.delete_notes_message, state.selectedNoteIds.size)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSelectedNotes()
                        showBatchDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (state.isSelectionMode) {
                        Text(stringResource(R.string.items_selected, state.selectedNoteIds.size))
                    } else {
                        Text(stringResource(R.string.app_name))
                    }
                },
                navigationIcon = {
                    if (state.isSelectionMode) {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel_selection))
                        }
                    }
                },
                actions = {
                    if (state.isSelectionMode) {
                        IconButton(onClick = { showBatchDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_selected))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (state.isSelectionMode) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (!state.isSelectionMode) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Screen.AddEditNoteScreen.route + "?noteId=-1")
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.semantics { 
                        contentDescription = context.getString(R.string.add_new_note)
                    }
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .semantics { 
                    contentDescription = context.getString(R.string.list_of_notes)
                },
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = state.notes,
                key = { it.id ?: it.hashCode() }
            ) { note ->
                val isSelected = note.id in state.selectedNoteIds
                val formattingInfo = buildString {
                    if (note.isBold) append("bold ")
                    if (note.isItalic) append("italic ")
                    if (note.isUnderlined) append("underlined ")
                }.trim()

                val styleStr = if (formattingInfo.isNotEmpty()) {
                    stringResource(R.string.style_formatting, formattingInfo)
                } else ""

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                        .semantics(mergeDescendants = true) {
                            contentDescription = context.getString(
                                R.string.note_content_description,
                                note.title,
                                styleStr,
                                note.content
                            )
                        }
                        .combinedClickable(
                            onClick = {
                                if (state.isSelectionMode) {
                                    note.id?.let { viewModel.toggleSelection(it) }
                                } else {
                                    navController.navigate(
                                        Screen.AddEditNoteScreen.route + "?noteId=${note.id}"
                                    )
                                }
                            },
                            onLongClick = {
                                note.id?.let { viewModel.toggleSelection(it) }
                            },
                            onClickLabel = stringResource(if (state.isSelectionMode) R.string.custom_selected else R.string.edit_note)
                        ),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(note.color)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Box {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = note.title,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                if (!state.isSelectionMode) {
                                    IconButton(
                                        onClick = {
                                            noteToDelete = note
                                        },
                                        modifier = Modifier.semantics { 
                                            contentDescription = context.getString(R.string.delete_note_with_title, note.title)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = Color.Black.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = note.content,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (note.isBold) FontWeight.Bold else FontWeight.Normal,
                                    fontStyle = if (note.isItalic) FontStyle.Italic else FontStyle.Normal,
                                    textDecoration = if (note.isUnderlined) TextDecoration.Underline else TextDecoration.None
                                ),
                                color = Color.Black.copy(alpha = 0.8f),
                                maxLines = 10,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (state.isSelectionMode) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { note.id?.let { viewModel.toggleSelection(it) } },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
