package com.rameeza.stickynotesapp.ui.add_edit_note

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.rameeza.stickynotesapp.R
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
    val textFieldValue = remember {
        mutableStateOf(
            TextFieldValue(
                text = contentState,
                selection = TextRange(contentState.length)
            )
        )
    }
    
    // Keep textFieldValue in sync with contentState when it changes externally (like loading or voice input)
    LaunchedEffect(contentState) {
        if (contentState != textFieldValue.value.text) {
            textFieldValue.value = textFieldValue.value.copy(
                text = contentState,
                selection = TextRange(contentState.length)
            )
        }
    }
    val isBold = viewModel.isBold.value
    val isItalic = viewModel.isItalic.value
    val isUnderlined = viewModel.isUnderlined.value
    val isChecklist = viewModel.isChecklist.value
    
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
        NoteColors[0] to stringResource(R.string.color_yellow),
        NoteColors[1] to stringResource(R.string.color_pink),
        NoteColors[2] to stringResource(R.string.color_blue),
        NoteColors[3] to stringResource(R.string.color_green),
        NoteColors[4] to stringResource(R.string.color_orange)
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
            title = { Text(stringResource(R.string.empty_note_title)) },
            text = { Text(stringResource(R.string.empty_note_message)) },
            confirmButton = {
                TextButton(onClick = { showEmptyDialog = false }) {
                    Text(stringResource(R.string.add_contents))
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showEmptyDialog = false
                    navController.navigateUp()
                }) {
                    Text(stringResource(R.string.discard))
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
                    Icon(imageVector = Icons.Default.Check, contentDescription = stringResource(R.string.save_note))
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
                                    val colorName = colorNames[color] ?: context.getString(R.string.color_unknown)
                                    contentDescription = context.getString(R.string.color_description, colorName)
                                    stateDescription = if (viewModel.noteColor.value == colorInt) {
                                        context.getString(R.string.custom_selected)
                                    } else {
                                        context.getString(R.string.custom_not_selected)
                                    }
                                }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
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
                            Text(text = stringResource(R.string.enter_title), style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .semantics {
                                contentDescription = context.getString(R.string.note_title)
                            },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
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
                            contentDescription = stringResource(R.string.voice_to_text_title),
                            tint = if (viewModel.isRecordingTitle.value) Color.Red else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val activeStr = stringResource(R.string.active)
                    val inactiveStr = stringResource(R.string.inactive)
                    IconButton(
                        onClick = { viewModel.toggleBold() },
                        modifier = Modifier.semantics {
                            stateDescription = if (isBold) activeStr else inactiveStr
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (isBold) Color.Black.copy(alpha = 0.1f) else Color.Transparent
                        )
                    ) {
                        Icon(Icons.Default.FormatBold, contentDescription = stringResource(R.string.bold_text))
                    }
                    IconButton(
                        onClick = { viewModel.toggleItalic() },
                        modifier = Modifier.semantics {
                            stateDescription = if (isItalic) activeStr else inactiveStr
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (isItalic) Color.Black.copy(alpha = 0.1f) else Color.Transparent
                        )
                    ) {
                        Icon(Icons.Default.FormatItalic, contentDescription = stringResource(R.string.italic_text))
                    }
                    IconButton(
                        onClick = { viewModel.toggleUnderline() },
                        modifier = Modifier.semantics {
                            stateDescription = if (isUnderlined) activeStr else inactiveStr
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (isUnderlined) Color.Black.copy(alpha = 0.1f) else Color.Transparent
                        )
                    ) {
                        Icon(Icons.Default.FormatUnderlined, contentDescription = stringResource(R.string.underline_text))
                    }
                    
                    VerticalDivider(
                        modifier = Modifier
                            .height(24.dp)
                            .padding(horizontal = 8.dp),
                        color = Color.Black.copy(alpha = 0.2f)
                    )

                    IconButton(
                        onClick = { viewModel.toggleChecklist() },
                        modifier = Modifier.semantics {
                            stateDescription = if (isChecklist) activeStr else inactiveStr
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (isChecklist) Color.Black.copy(alpha = 0.1f) else Color.Transparent
                        )
                    ) {
                        Icon(
                            imageVector = if (isChecklist) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                            contentDescription = stringResource(R.string.toggle_checklist)
                        )
                    }
                }
                
                val textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                    fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                    textDecoration = if (isUnderlined) TextDecoration.Underline else TextDecoration.None
                )

                Spacer(modifier = Modifier.height(16.dp))
                
                Box(modifier = Modifier.weight(1f)) {
                    TextField(
                        value = textFieldValue.value,
                        onValueChange = { newValue ->
                            val oldText = textFieldValue.value.text
                            val newText = newValue.text
                            
                            // Prevent typing before checkboxes
                            if (isChecklist && newText.length >= oldText.length) {
                                val lines = newText.split("\n")
                                val oldLines = oldText.split("\n")
                                if (lines.size == oldLines.size) {
                                    var blocked = false
                                    for (i in lines.indices) {
                                        val oldLine = if (i < oldLines.size) oldLines[i] else ""
                                        val newLine = lines[i]
                                        if ((oldLine.startsWith("☐ ") && !newLine.startsWith("☐ ")) ||
                                            (oldLine.startsWith("☑ ") && !newLine.startsWith("☑ "))
                                        ) {
                                            blocked = true
                                            break
                                        }
                                    }
                                    if (blocked) {
                                        textFieldValue.value = textFieldValue.value.copy(selection = newValue.selection)
                                        return@TextField
                                    }
                                }
                            }

                            textFieldValue.value = newValue
                            if (newText != contentState) {
                                viewModel.onContentChanged(newText)
                            }
                        },
                        placeholder = {
                            Text(text = stringResource(R.string.enter_content), style = textStyle)
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .semantics {
                                contentDescription = context.getString(R.string.note_content)
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
                    
                    // Clickable overlay for checkboxes
                    if (isChecklist) {
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 16.dp, start = 16.dp)
                        ) {
                            val lines = contentState.split("\n")
                            lines.forEachIndexed { index, line ->
                                if (line.startsWith("☐ ") || line.startsWith("☑ ")) {
                                    Box(
                                        modifier = Modifier
                                            .offset(y = (index * 24).dp) // Estimate line height
                                            .size(24.dp)
                                            .clickable(
                                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                viewModel.toggleCheckItem(index)
                                            }
                                    )
                                }
                            }
                        }
                    }
                    
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
                            contentDescription = stringResource(R.string.voice_to_text_content),
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
                        text = stringResource(R.string.listening),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (spokenText.isBlank()) stringResource(R.string.start_speaking) else spokenText,
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
                        Text(stringResource(R.string.stop_recording))
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
