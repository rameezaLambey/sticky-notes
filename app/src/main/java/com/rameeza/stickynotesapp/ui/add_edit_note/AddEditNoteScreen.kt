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
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
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
    val isBold = viewModel.isBold.value
    val isItalic = viewModel.isItalic.value
    val isUnderlined = viewModel.isUnderlined.value
    
    val scope = rememberCoroutineScope()
    var showEmptyDialog by remember { mutableStateOf(false) }

    val colorNames = mapOf(
        NoteColors[0] to "Yellow",
        NoteColors[1] to "Pink",
        NoteColors[2] to "Blue",
        NoteColors[3] to "Green",
        NoteColors[4] to "Orange"
    )

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
                is AddEditNoteViewModel.UiEvent.ShowEmptyNoteDialog -> {
                    showEmptyDialog = true
                }
            }
        }
    }

    if (showEmptyDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyDialog = false },
            title = { Text("Empty Note") },
            text = { Text("You haven't added any content. Notes without text won't be saved. Would you like to add some contents?") },
            confirmButton = {
                TextButton(onClick = { showEmptyDialog = false }) {
                    Text("Add Contents")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showEmptyDialog = false
                    navController.navigateUp()
                }) {
                    Text("Discard")
                }
            }
        )
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
                                val colorName = colorNames[color] ?: "Unknown"
                                contentDescription = "$colorName color"
                                stateDescription = if (viewModel.noteColor.value == colorInt) "Selected" else "Not selected"
                            }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = { viewModel.toggleBold() },
                    modifier = Modifier.semantics {
                        stateDescription = if (isBold) "Active" else "Inactive"
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isBold) Color.Black.copy(alpha = 0.1f) else Color.Transparent
                    )
                ) {
                    Icon(Icons.Default.FormatBold, contentDescription = "Bold text")
                }
                IconButton(
                    onClick = { viewModel.toggleItalic() },
                    modifier = Modifier.semantics {
                        stateDescription = if (isItalic) "Active" else "Inactive"
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isItalic) Color.Black.copy(alpha = 0.1f) else Color.Transparent
                    )
                ) {
                    Icon(Icons.Default.FormatItalic, contentDescription = "Italic text")
                }
                IconButton(
                    onClick = { viewModel.toggleUnderline() },
                    modifier = Modifier.semantics {
                        stateDescription = if (isUnderlined) "Active" else "Inactive"
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isUnderlined) Color.Black.copy(alpha = 0.1f) else Color.Transparent
                    )
                ) {
                    Icon(Icons.Default.FormatUnderlined, contentDescription = "Underline text")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            val textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                textDecoration = if (isUnderlined) TextDecoration.Underline else TextDecoration.None
            )

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
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                    fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                    textDecoration = if (isUnderlined) TextDecoration.Underline else TextDecoration.None
                ),
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
                textStyle = textStyle,
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
