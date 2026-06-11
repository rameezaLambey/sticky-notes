package com.rameeza.stickynotesapp.ui.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.*
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.navigation.NavController
import com.rameeza.stickynotesapp.domain.model.Note
import com.rameeza.stickynotesapp.ui.util.Screen

@Composable
fun NotesScreen(
    navController: NavController,
    viewModelFactory: ViewModelProvider.Factory,
    viewModel: NotesViewModel = viewModel(factory = viewModelFactory)
) {
    val state = viewModel.state.value
    var noteToDelete by remember { mutableStateOf<Note?>(null) }

    if (noteToDelete != null) {
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("Delete Note") },
            text = { Text("Are you sure you want to delete this note?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        noteToDelete?.let { viewModel.deleteNote(it) }
                        noteToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.AddEditNoteScreen.route + "?noteId=-1")
                },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.semantics { 
                    contentDescription = "Add new note"
                }
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .semantics { 
                    contentDescription = "List of notes"
                },
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = state.notes,
                key = { it.id ?: it.hashCode() }
            ) { note ->
                val formattingInfo = buildString {
                    if (note.isBold) append("bold ")
                    if (note.isItalic) append("italic ")
                    if (note.isUnderlined) append("underlined ")
                }.trim()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                        .semantics(mergeDescendants = true) {
                            contentDescription = "Note: ${note.title}. ${if (formattingInfo.isNotEmpty()) "Style: $formattingInfo. " else ""}${note.content}"
                        }
                        .clickable(
                            onClickLabel = "Edit note"
                        ) {
                            navController.navigate(
                                Screen.AddEditNoteScreen.route + "?noteId=${note.id}"
                            )
                        },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(note.color)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = note.title,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = if (note.isBold) FontWeight.Bold else FontWeight.Normal,
                                    fontStyle = if (note.isItalic) FontStyle.Italic else FontStyle.Normal,
                                    textDecoration = if (note.isUnderlined) TextDecoration.Underline else TextDecoration.None
                                ),
                                color = Color.Black
                            )
                            IconButton(
                                onClick = {
                                    noteToDelete = note
                                },
                                modifier = Modifier.semantics { 
                                    contentDescription = "Delete note ${note.title}"
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Color.Black.copy(alpha = 0.6f)
                                )
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
                            color = Color.Black.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}
