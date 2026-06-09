package com.rameeza.stickynotesapp.ui.add_edit_note

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.rameeza.stickynotesapp.ui.theme.NoteColors
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun AddEditNoteScreen(
    navController: NavController,
    noteId: Int?,
    viewModelFactory: ViewModelProvider.Factory,
    viewModel: AddEditNoteViewModel = viewModel(factory = viewModelFactory)
) {
    val titleState = viewModel.noteTitle.value
    val contentState = viewModel.noteContent.value
    val noteColor = viewModel.noteColor.value
    val scope = rememberCoroutineScope()

    val noteBackgroundAnimatable = remember {
        Animatable(
            Color(viewModel.noteColor.value)
        )
    }

    LaunchedEffect(key1 = viewModel.noteColor.value) {
        noteBackgroundAnimatable.animateTo(
            targetValue = Color(viewModel.noteColor.value),
            animationSpec = tween(
                durationMillis = 500
            )
        )
    }

    LaunchedEffect(key1 = noteId) {
        viewModel.loadNote(noteId)
        viewModel.eventFlow.collectLatest { event ->
            when(event) {
                is AddEditNoteViewModel.UiEvent.SaveNote -> {
                    navController.navigateUp()
                }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.saveNote()
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Save note")
            }
        },
        containerColor = noteBackgroundAnimatable.value
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NoteColors.forEach { color ->
                    val colorInt = color.toArgb()
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .shadow(15.dp, CircleShape)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = 3.dp,
                                color = if (viewModel.noteColor.value == colorInt) {
                                    Color.Black
                                } else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable {
                                scope.launch {
                                    noteBackgroundAnimatable.animateTo(
                                        targetValue = Color(colorInt),
                                        animationSpec = tween(
                                            durationMillis = 500
                                        )
                                    )
                                }
                                viewModel.onColorChanged(colorInt)
                            }
                            .semantics {
                                contentDescription = "Color ${color.toString()}"
                                // role = Role.Button // Role is not directly available in semantics block easily without importing, but it's okay for now
                            }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = titleState,
                onValueChange = {
                    viewModel.onTitleChanged(it)
                },
                placeholder = {
                    Text(text = "Enter title...")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Note title"
                    },
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineMedium,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = contentState,
                onValueChange = {
                    viewModel.onContentChanged(it)
                },
                placeholder = {
                    Text(text = "Enter content...")
                },
                modifier = Modifier
                    .fillMaxSize()
                    .semantics {
                        contentDescription = "Note content"
                    },
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )
        }
    }
}
