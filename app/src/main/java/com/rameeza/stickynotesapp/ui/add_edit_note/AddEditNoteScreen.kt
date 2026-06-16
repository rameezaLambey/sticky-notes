package com.rameeza.stickynotesapp.ui.add_edit_note

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
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
    val context = LocalContext.current
    
    val voiceState by viewModel.voiceToTextParser.state.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                if (viewModel.isRecordingTitle.value) viewModel.toggleRecordTitle()
                else if (viewModel.isRecordingContent.value) viewModel.toggleRecordContent()
            }
        }
    )

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

    Box(modifier = Modifier.fillMaxSize()) {
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = titleState,
                        onValueChange = {
                            viewModel.onTitleChanged(it)
                        },
                        placeholder = {
                            Text(text = "Enter title...", style = MaterialTheme.typography.headlineMedium)
                        },
                        modifier = Modifier
                            .weight(1f)
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
                    IconButton(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                viewModel.toggleRecordTitle()
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier.padding(start = 8.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (viewModel.isRecordingTitle.value) Color.Red.copy(alpha = 0.1f) else Color.Transparent
                        )
                    ) {
                        Icon(
                            imageVector = if (viewModel.isRecordingTitle.value) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Voice to text for title",
                            tint = if (viewModel.isRecordingTitle.value) Color.Red else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Box(modifier = Modifier.weight(1f)) {
                    TextField(
                        value = contentState,
                        onValueChange = {
                            viewModel.onContentChanged(it)
                        },
                        placeholder = {
                            Text(text = "Enter content...", style = textStyle)
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
                    
                    IconButton(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                viewModel.toggleRecordContent()
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 4.dp, end = 4.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (viewModel.isRecordingContent.value) Color.Red.copy(alpha = 0.1f) else Color.Transparent
                        )
                    ) {
                        Icon(
                            imageVector = if (viewModel.isRecordingContent.value) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Voice to text for content",
                            tint = if (viewModel.isRecordingContent.value) Color.Red else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        VoiceRecordingOverlay(
            visible = voiceState.isSpeaking,
            rmsDb = voiceState.rmsDb,
            spokenText = voiceState.spokenText,
            onStop = {
                viewModel.voiceToTextParser.stopListening()
            }
        )
    }
}

@Composable
fun VoiceRecordingOverlay(
    visible: Boolean,
    rmsDb: Float,
    spokenText: String,
    onStop: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Listening...",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (spokenText.isBlank()) "Start speaking..." else spokenText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    AudioVisualizer(rmsDb = rmsDb)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = onStop,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Stop Recording")
                    }
                }
            }
        }
    }
}

@Composable
fun AudioVisualizer(rmsDb: Float) {
    val barCount = 5
    val infiniteTransition = rememberInfiniteTransition(label = "audio_bars")
    
    Row(
        modifier = Modifier.height(60.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val baseScale = (rmsDb + 2f).coerceIn(1f, 10f) / 5f
            val animationScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, delayMillis = i * 100),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$i"
            )
            
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .fillMaxHeight(fraction = (0.2f + 0.15f * i).coerceAtMost(1f))
                    .graphicsLayer {
                        scaleY = baseScale * animationScale
                    }
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}
