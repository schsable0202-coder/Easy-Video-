package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VideoProject
import com.example.ui.VideoStudioViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    viewModel: VideoStudioViewModel,
    onNavigateToStudio: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val projects by viewModel.projects.collectAsState()
    val isApiKeySet by viewModel.isApiKeySet.collectAsState()
    var showCreateSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Logo",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "AI Video Gen Studio",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Black,
                modifier = Modifier
                    .testTag("create_new_project_fab")
                    .padding(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Video")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // API Key Check Alert Banner
            if (!isApiKeySet) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Warning",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Running in Simulator Mode",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "No API key configured. We have enabled offline simulation fallbacks so you can explore all features immediately!",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Text(
                text = "Your Projects",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            if (projects.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Empty projects",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Video Projects Yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap the '+' button below to convert any topic, prompt, or idea into a fully-formed video script, visual scenes, narration, and SEO package.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Try these ideas:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val suggestions = listOf(
                            "Benefits of Vitamin B12",
                            "History of Artificial Intelligence",
                            "How to Reduce Blood Sugar Naturally",
                            "Top 10 Places to Visit in Pune"
                        )
                        suggestions.forEach { suggestion ->
                            SuggestionChip(
                                onClick = {
                                    viewModel.createNewProject(
                                        topic = suggestion,
                                        duration = "1 minute",
                                        language = "English",
                                        voiceGender = "Professional Narrator",
                                        tone = "Professional"
                                    ) { newId ->
                                        onNavigateToStudio(newId)
                                    }
                                },
                                label = { Text(suggestion, fontSize = 12.sp) },
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(projects, key = { it.id }) { project ->
                        ProjectItemCard(
                            project = project,
                            onClick = { onNavigateToStudio(project.id) },
                            onDelete = { viewModel.deleteProject(project.id) }
                        )
                    }
                }
            }
        }

        if (showCreateSheet) {
            CreateProjectBottomSheet(
                onDismiss = { showCreateSheet = false },
                onCreate = { topic, duration, language, voice, tone, speed, pitch ->
                    showCreateSheet = false
                    viewModel.createNewProject(
                        topic = topic,
                        duration = duration,
                        language = language,
                        voiceGender = voice,
                        tone = tone
                    ) { newId ->
                        // Pre-set customized sliders
                        viewModel.updateProjectFields { p ->
                            p.copy(speed = speed, pitch = pitch)
                        }
                        onNavigateToStudio(newId)
                    }
                }
            )
        }
    }
}

@Composable
fun ProjectItemCard(
    project: VideoProject,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("project_card_${project.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.topic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Topic: ${project.title}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                StatusBadge(status = project.status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info rows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoChip(label = project.duration)
                InfoChip(label = project.language)
                InfoChip(label = project.voiceGender)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
                val dateStr = dateFormat.format(Date(project.createdAt))
                Text(
                    text = "Created: $dateStr",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("delete_project_button_${project.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (text, color) = when (status) {
        "setup" -> "Setup" to MaterialTheme.colorScheme.secondary
        "script_ready" -> "Script Ready" to MaterialTheme.colorScheme.primary
        "scenes_ready" -> "Storyboard Done" to MaterialTheme.colorScheme.tertiary
        "assembled" -> "Assembled" to Color(0xFFFF9800)
        "exported" -> "Exported" to Color(0xFF4CAF50)
        else -> status to MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun InfoChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectBottomSheet(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String, String, Float, Float) -> Unit
) {
    var topic by remember { mutableStateOf("") }
    var selectedDuration by remember { mutableStateOf("1 minute") }
    var selectedLanguage by remember { mutableStateOf("English") }
    var selectedVoice by remember { mutableStateOf("Professional Narrator") }
    var selectedTone by remember { mutableStateOf("Professional") }
    var voiceSpeed by remember { mutableFloatStateOf(1.0f) }
    var voicePitch by remember { mutableFloatStateOf(1.0f) }

    val durations = listOf("30 seconds", "1 minute", "2 minutes", "3 minutes", "5 minutes")
    val languages = listOf("English", "Hindi", "Marathi", "Spanish", "French", "German", "Japanese")
    val voices = listOf("Male", "Female", "Child", "Professional Narrator")
    val tones = listOf("Professional", "Energetic", "Calm", "Dramatic", "Informative", "Joyful")

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val keyboardController = LocalSoftwareKeyboardController.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "New Video Project",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Configure your AI scripts, scenes, and narration voice metrics.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Topic Input
            Text(
                text = "Enter a Topic, Prompt, or Idea",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = topic,
                onValueChange = { topic = it },
                placeholder = { Text("e.g. Benefits of Vitamin B12") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("new_project_topic_input"),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Duration
            Text(
                text = "Video Duration",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                durations.take(3).forEach { dur ->
                    val isSelected = selectedDuration == dur
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedDuration = dur },
                        label = { Text(dur, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                durations.drop(3).forEach { dur ->
                    val isSelected = selectedDuration == dur
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedDuration = dur },
                        label = { Text(dur, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dropdowns row (Language and Voice)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Language",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    var langMenuExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = langMenuExpanded,
                        onExpandedChange = { langMenuExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedLanguage,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = langMenuExpanded) },
                            modifier = Modifier.menuAnchor(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = langMenuExpanded,
                            onDismissRequest = { langMenuExpanded = false }
                        ) {
                            languages.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang) },
                                    onClick = {
                                        selectedLanguage = lang
                                        langMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Voice narrator",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    var voiceMenuExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = voiceMenuExpanded,
                        onExpandedChange = { voiceMenuExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedVoice,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = voiceMenuExpanded) },
                            modifier = Modifier.menuAnchor(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = voiceMenuExpanded,
                            onDismissRequest = { voiceMenuExpanded = false }
                        ) {
                            voices.forEach { voice ->
                                DropdownMenuItem(
                                    text = { Text(voice) },
                                    onClick = {
                                        selectedVoice = voice
                                        voiceMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tone Dropdown
            Text(
                text = "AI Voice Narration Tone",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            var toneMenuExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = toneMenuExpanded,
                onExpandedChange = { toneMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedTone,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toneMenuExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = toneMenuExpanded,
                    onDismissRequest = { toneMenuExpanded = false }
                ) {
                    tones.forEach { tone ->
                        DropdownMenuItem(
                            text = { Text(tone) },
                            onClick = {
                                selectedTone = tone
                                toneMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sliders for Pitch and Speed
            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Voice speed: ${"%.1f".format(voiceSpeed)}x",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Slider(
                value = voiceSpeed,
                onValueChange = { voiceSpeed = it },
                valueRange = 0.5f..2.0f,
                steps = 14
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Voice pitch: ${"%.1f".format(voicePitch)}x",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Slider(
                value = voicePitch,
                onValueChange = { voicePitch = it },
                valueRange = 0.5f..2.0f,
                steps = 14
            )

            Spacer(modifier = Modifier.height(24.dp))

            // CTA Submit Button
            Button(
                onClick = {
                    if (topic.isNotBlank()) {
                        onCreate(topic, selectedDuration, selectedLanguage, selectedVoice, selectedTone, voiceSpeed, voicePitch)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_create_project"),
                enabled = topic.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "Generate AI Video Script",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
