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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.navigation.NavController
import com.rameeza.stickynotesapp.ui.util.Screen

@Composable
fun NotesScreen(
    navController: NavController,
    viewModelFactory: ViewModelProvider.Factory,
    viewModel: NotesViewModel = viewModel(factory = viewModelFactory)
) {
    val state = viewModel.state.value

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
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                        .clickable(
                            onClickLabel = "Edit note ${note.title}"
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
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.Black,
                                modifier = Modifier.semantics { 
                                    contentDescription = "Note title: ${note.title}"
                                }
                            )
                            IconButton(
                                onClick = {
                                    viewModel.deleteNote(note)
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
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black.copy(alpha = 0.8f),
                            modifier = Modifier.semantics { 
                                contentDescription = "Note content: ${note.content}"
                            }
                        )
                    }
                }
            }
        }
    }
}
