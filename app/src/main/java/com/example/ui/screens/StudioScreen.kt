package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.VideoProject
import com.example.domain.ThumbnailOption
import com.example.domain.VideoScene
import com.example.ui.VideoStudioViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioScreen(
    viewModel: VideoStudioViewModel,
    projectId: Int,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val projectState by viewModel.currentProject.collectAsState()
    val isGeneratingScript by viewModel.isGeneratingScript.collectAsState()
    val isGeneratingStoryboard by viewModel.isGeneratingStoryboard.collectAsState()
    val isGeneratingSeo by viewModel.isGeneratingSeo.collectAsState()
    val isRendering by viewModel.isRendering.collectAsState()
    val renderProgress by viewModel.renderProgress.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId)
    }

    val project = projectState

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = project?.topic ?: "AI Studio Editor",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = project?.title ?: "Loading...",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (project != null) {
                        StatusBadge(status = project.status)
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        if (project == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            StudioWorkspaceContent(
                project = project,
                viewModel = viewModel,
                isGeneratingScript = isGeneratingScript,
                isGeneratingStoryboard = isGeneratingStoryboard,
                isGeneratingSeo = isGeneratingSeo,
                isRendering = isRendering,
                renderProgress = renderProgress,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
fun StudioWorkspaceContent(
    project: VideoProject,
    viewModel: VideoStudioViewModel,
    isGeneratingScript: Boolean,
    isGeneratingStoryboard: Boolean,
    isGeneratingSeo: Boolean,
    isRendering: Boolean,
    renderProgress: Float, // Note: represented as Float in VM
    modifier: Modifier = Modifier
) {
    // We organize the workflow steps into a gorgeous scrolling navigation header
    var selectedWorkflowStep by remember { mutableStateOf(WorkflowStep.SCRIPT) }

    LaunchedEffect(project.status) {
        // Automatically advance steps based on project progression
        when (project.status) {
            "setup" -> selectedWorkflowStep = WorkflowStep.SCRIPT
            "script_ready" -> selectedWorkflowStep = WorkflowStep.SCRIPT
            "scenes_ready" -> selectedWorkflowStep = WorkflowStep.STORYBOARD
            "assembled" -> selectedWorkflowStep = WorkflowStep.PREVIEW
            "exported" -> selectedWorkflowStep = WorkflowStep.SEO
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Workflow Step Selector Header
        WorkflowStepSelectorRow(
            currentStep = selectedWorkflowStep,
            projectStatus = project.status,
            onStepSelected = { selectedWorkflowStep = it }
        )

        Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)

        // Main content card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            AnimatedContent(
                targetState = selectedWorkflowStep,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "workflow_step_anim"
            ) { step ->
                when (step) {
                    WorkflowStep.SCRIPT -> {
                        ScriptEditorView(
                            project = project,
                            viewModel = viewModel,
                            isGeneratingScript = isGeneratingScript,
                            isGeneratingStoryboard = isGeneratingStoryboard
                        )
                    }
                    WorkflowStep.STORYBOARD -> {
                        StoryboardEditorView(
                            project = project,
                            viewModel = viewModel,
                            isGeneratingStoryboard = isGeneratingStoryboard
                        )
                    }
                    WorkflowStep.PREVIEW -> {
                        AssemblyPreviewView(
                            project = project,
                            viewModel = viewModel,
                            isRendering = isRendering,
                            renderProgress = viewModel.renderProgress.collectAsState().value
                        )
                    }
                    WorkflowStep.SEO -> {
                        SeoAndExportView(
                            project = project,
                            viewModel = viewModel,
                            isGeneratingSeo = isGeneratingSeo
                        )
                    }
                }
            }
        }
    }
}

enum class WorkflowStep(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    SCRIPT("1. Script", Icons.Default.Edit),
    STORYBOARD("2. Storyboard", Icons.Default.List),
    PREVIEW("3. Video Play", Icons.Default.PlayArrow),
    SEO("4. SEO & Save", Icons.Default.Share)
}

@Composable
fun WorkflowStepSelectorRow(
    currentStep: WorkflowStep,
    projectStatus: String,
    onStepSelected: (WorkflowStep) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 10.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        WorkflowStep.values().forEach { step ->
            val isEnabled = when (step) {
                WorkflowStep.SCRIPT -> true
                WorkflowStep.STORYBOARD -> projectStatus != "setup"
                WorkflowStep.PREVIEW -> projectStatus == "scenes_ready" || projectStatus == "assembled" || projectStatus == "exported"
                WorkflowStep.SEO -> projectStatus == "assembled" || projectStatus == "exported"
            }

            val isSelected = currentStep == step

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = isEnabled) { onStepSelected(step) }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("step_tab_${step.name}")
            ) {
                Icon(
                    imageVector = step.icon,
                    contentDescription = step.label,
                    tint = when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        isEnabled -> MaterialTheme.colorScheme.onSurface
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                    },
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = step.label,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        isEnabled -> MaterialTheme.colorScheme.onSurface
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                    }
                )
            }
        }
    }
}

// ----------------------------------------------------
// 1. SCRIPT EDITOR VIEW
// ----------------------------------------------------
@Composable
fun ScriptEditorView(
    project: VideoProject,
    viewModel: VideoStudioViewModel,
    isGeneratingScript: Boolean,
    isGeneratingStoryboard: Boolean
) {
    var intro by remember(project.id) { mutableStateOf(project.scriptIntro) }
    var main by remember(project.id) { mutableStateOf(project.scriptMain) }
    var conclusion by remember(project.id) { mutableStateOf(project.scriptConclusion) }
    var cta by remember(project.id) { mutableStateOf(project.scriptCallToAction) }

    LaunchedEffect(project.id, project.scriptIntro) {
        intro = project.scriptIntro
        main = project.scriptMain
        conclusion = project.scriptConclusion
        cta = project.scriptCallToAction
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (project.status == "setup") {
            // First run, script not generated yet
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Script Setup Options",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Click below to trigger the Gemini AI Script generator. It will construct a structured video script including high-impact hooks, insightful educational bodies, summaries, and strong call-to-actions based on your configuration parameters.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        Text(
            text = "Video Script Content",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = "Fine-tune individual acts of your video before generating the visuals storyboard.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Intro
        Text("Hook/Introduction (Act 1)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = intro,
            onValueChange = { intro = it },
            placeholder = { Text("AI Hook intro script...") },
            modifier = Modifier.fillMaxWidth().testTag("script_intro_input"),
            maxLines = 4,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Main
        Text("Main Explainer Content (Act 2)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = main,
            onValueChange = { main = it },
            placeholder = { Text("Core educational video contents...") },
            modifier = Modifier.fillMaxWidth().height(160.dp).testTag("script_main_input"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Conclusion
        Text("Conclusion Summary (Act 3)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = conclusion,
            onValueChange = { conclusion = it },
            placeholder = { Text("Summing up points...") },
            modifier = Modifier.fillMaxWidth().testTag("script_conclusion_input"),
            maxLines = 4,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // CTA
        Text("Call To Action (Act 4)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = cta,
            onValueChange = { cta = it },
            placeholder = { Text("Subscribe, click like, visit our link...") },
            modifier = Modifier.fillMaxWidth().testTag("script_cta_input"),
            maxLines = 3,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Navigation CTA Actions
        if (isGeneratingScript) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("AI is writing your custom script...", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (project.status == "setup") {
                    Button(
                        onClick = {
                            viewModel.generateAiScript(
                                project.topic,
                                project.duration,
                                project.language,
                                project.voiceGender,
                                project.tone
                            )
                        },
                        modifier = Modifier.weight(1f).height(50.dp).testTag("generate_script_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Write Script with AI", fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            viewModel.updateProjectFields { p ->
                                p.copy(
                                    scriptIntro = intro,
                                    scriptMain = main,
                                    scriptConclusion = conclusion,
                                    scriptCallToAction = cta
                                )
                            }
                        },
                        modifier = Modifier.weight(1f).height(50.dp).testTag("save_script_changes_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Script")
                    }

                    Button(
                        onClick = {
                            // First save changes, then trigger scenes
                            viewModel.updateProjectFields { p ->
                                p.copy(
                                    scriptIntro = intro,
                                    scriptMain = main,
                                    scriptConclusion = conclusion,
                                    scriptCallToAction = cta
                                )
                            }
                            viewModel.generateStoryboardScenes()
                        },
                        modifier = Modifier.weight(1.3f).height(50.dp).testTag("proceed_storyboard_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isGeneratingStoryboard) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black)
                        } else {
                            Text("Create Storyboard", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

// ----------------------------------------------------
// 2. STORYBOARD VIEW (SCENE CARDS TIMELINE)
// ----------------------------------------------------
@Composable
fun StoryboardEditorView(
    project: VideoProject,
    viewModel: VideoStudioViewModel,
    isGeneratingStoryboard: Boolean
) {
    val coroutineScope = rememberCoroutineScope()
    var editingScene by remember { mutableStateOf<VideoScene?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isGeneratingStoryboard) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Splitting script into scenes & crafting AI visuals...", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
            ) {
                item {
                    Text(
                        text = "Storyboard & Scenes Timeline",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "AI divided your script into ${project.scenes.size} scenes. Customize layouts, narration text, and visual imagery.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(project.scenes, key = { it.sceneNumber }) { scene ->
                    SceneTimelineCard(
                        scene = scene,
                        onEdit = { editingScene = scene },
                        onPlayNarration = {
                            // High fidelity simulated voice playback indicator
                            Toast.makeText(viewModel.getApplication(), "Playing Narration Scene ${scene.sceneNumber}: speed=${project.speed}x, pitch=${project.pitch}x, tone=${project.tone}", Toast.LENGTH_SHORT).show()
                        },
                        onRegenPrompt = {
                            viewModel.regenerateSceneVisual(
                                scene.sceneNumber,
                                scene.visualPrompt + ", cinematic style, vivid lighting"
                            )
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            // Assemble video action with initial configs
                            viewModel.assembleAndSyncVideo(
                                captionsStyle = project.captionsStyle,
                                backgroundMusic = project.backgroundMusic,
                                musicVol = project.musicVolume,
                                voiceVol = project.voiceVolume
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("assemble_video_timeline_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Assemble Video & Sync Narration", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (editingScene != null) {
        val scene = editingScene!!
        EditSceneDialog(
            scene = scene,
            onDismiss = { editingScene = null },
            onSave = { onScreen, narration, transition, duration, visualUrl ->
                viewModel.updateSceneContent(
                    sceneNumber = scene.sceneNumber,
                    onScreen = onScreen,
                    narration = narration,
                    transition = transition,
                    duration = duration
                )
                if (visualUrl != scene.visualUrl) {
                    viewModel.replaceSceneVisualUrl(scene.sceneNumber, visualUrl)
                }
                editingScene = null
            }
        )
    }
}

@Composable
fun SceneTimelineCard(
    scene: VideoScene,
    onEdit: () -> Unit,
    onPlayNarration: () -> Unit,
    onRegenPrompt: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SCENE ${scene.sceneNumber}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(scene.transitionEffect, fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("${scene.durationSeconds}s", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Body
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Image view
                Box(
                    modifier = Modifier
                        .size(width = 110.dp, height = 110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .background(Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    if (scene.visualUrl.isNotEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(scene.visualUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Scene visual",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Visual placeholder")
                    }
                    // Overlay scene playhead action
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable(onClick = onPlayNarration)
                            .align(Alignment.Center),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Hear narration", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }

                // Scene Text details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Caption:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "\"${scene.onScreenText}\"",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Narration Voiceover:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = scene.narrationText,
                        fontSize = 12.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            // Prompt description banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "AI Visual prompt: ${scene.visualPrompt}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Quick Actions footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onRegenPrompt,
                    modifier = Modifier.testTag("regen_visual_${scene.sceneNumber}")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Regen", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Regen Visual", fontSize = 11.sp)
                }

                TextButton(
                    onClick = onEdit,
                    modifier = Modifier.testTag("edit_scene_${scene.sceneNumber}")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit Scene", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun EditSceneDialog(
    scene: VideoScene,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Int, String) -> Unit
) {
    var onScreen by remember { mutableStateOf(scene.onScreenText) }
    var narration by remember { mutableStateOf(scene.narrationText) }
    var transition by remember { mutableStateOf(scene.transitionEffect) }
    var duration by remember { mutableIntStateOf(scene.durationSeconds) }
    var visualUrl by remember { mutableStateOf(scene.visualUrl) }

    val transitions = listOf("Fade", "Dissolve", "Slide Left", "Zoom In")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Scene ${scene.sceneNumber}") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Caption overlay
                Text("On-Screen Caption Text", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                OutlinedTextField(
                    value = onScreen,
                    onValueChange = { onScreen = it },
                    modifier = Modifier.fillMaxWidth()
                )

                // Narration
                Text("Narration Voiceover script", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                OutlinedTextField(
                    value = narration,
                    onValueChange = { narration = it },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )

                // Transition selector
                Text("Transition effect", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                var transitionMenuExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { transitionMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(transition)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Expand")
                        }
                    }
                    DropdownMenu(expanded = transitionMenuExpanded, onDismissRequest = { transitionMenuExpanded = false }) {
                        transitions.forEach { t ->
                            DropdownMenuItem(text = { Text(t) }, onClick = { transition = t; transitionMenuExpanded = false })
                        }
                    }
                }

                // Duration Slider
                Text("Duration: ${duration}s", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Slider(
                    value = duration.toFloat(),
                    onValueChange = { duration = it.toInt() },
                    valueRange = 3f..25f,
                    steps = 22
                )

                // Manual visual URL upload
                Text("Mock Visual URL (Or Mock Upload)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                OutlinedTextField(
                    value = visualUrl,
                    onValueChange = { visualUrl = it },
                    placeholder = { Text("Paste image url...") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Support custom user image uploads by specifying URLs or trigger simulation overlays.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        },
        confirmButton = {
            Button(onClick = { onSave(onScreen, narration, transition, duration, visualUrl) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ----------------------------------------------------
// 3. ASSEMBLY & VIDEO PLAYER PREVIEW VIEW
// ----------------------------------------------------
@Composable
fun AssemblyPreviewView(
    project: VideoProject,
    viewModel: VideoStudioViewModel,
    isRendering: Boolean,
    renderProgress: Float
) {
    val isPlaying by viewModel.isPlaying.collectAsState()
    val activeSceneIndex by viewModel.activeSceneIndex.collectAsState()
    val playbackProgress by viewModel.playbackProgress.collectAsState()

    val currentScene = project.scenes.getOrNull(activeSceneIndex)

    var captionsStyle by remember { mutableStateOf(project.captionsStyle) }
    var backgroundMusic by remember { mutableStateOf(project.backgroundMusic) }
    var musicVolume by remember { mutableFloatStateOf(project.musicVolume) }
    var voiceVolume by remember { mutableFloatStateOf(project.voiceVolume) }

    val musicTracks = listOf("Cinematic Ambient", "Inspirational Corporate", "Upbeat Electric", "Lofi Sunset Beats", "None")
    val styles = listOf("Burned-In", "SRT Subtitle", "Animated Pop-up")

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Interactive Player Sync",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = "Simulate real-time synced audio-visual playback with captions.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Simulated video player canvas (Responsive aspect ratios YouTube Shorts vs standard 16:9)
        val playerHeight = if (project.duration.contains("Shorts") || project.status.contains("TikTok")) 360.dp else 210.dp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black)
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (currentScene != null && currentScene.visualUrl.isNotEmpty()) {
                // Crossfaded visual transition simulation
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(currentScene.visualUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Playback screen",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Scenes Rendered yet", color = Color.White)
                }
            }

            // Caption overlay based on captionsStyle selection
            if (currentScene != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = if (captionsStyle == "Animated Pop-up") Alignment.Center else Alignment.BottomCenter
                ) {
                    val captionBg = if (captionsStyle == "Burned-In") Color.Yellow else Color.Black.copy(alpha = 0.7f)
                    val captionColor = if (captionsStyle == "Burned-In") Color.Black else Color.White
                    val fontSize = if (captionsStyle == "Animated Pop-up") 20.sp else 14.sp
                    val fontWeight = if (captionsStyle == "Animated Pop-up") FontWeight.ExtraBold else FontWeight.Bold

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(captionBg)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = currentScene.onScreenText.uppercase(),
                            color = captionColor,
                            fontSize = fontSize,
                            fontWeight = fontWeight,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Sync visual watermark / time indicator
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "Scene ${activeSceneIndex + 1}/${project.scenes.size}",
                    color = Color.White,
                    fontSize = 10.sp
                )
            }
        }

        // Playback progress bar
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.togglePlayback() },
                modifier = Modifier.size(48.dp).testTag("play_simulation_button")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.KeyboardArrowDown else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Slider(
                value = playbackProgress,
                onValueChange = {},
                modifier = Modifier.weight(1f),
                enabled = false // Driven by VM playback loop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Timeline Scene fast selectors
        Text("Jump to Scene:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(project.scenes) { sc ->
                val isSelected = activeSceneIndex == sc.sceneNumber - 1
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .clickable { viewModel.seekToScene(sc.sceneNumber - 1) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "S${sc.sceneNumber}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Divider(color = MaterialTheme.colorScheme.surfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))

        // Custom synchronizer panel: caption configurations
        Text("AI Captions Style Overlay", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            styles.forEach { sty ->
                val isSelected = captionsStyle == sty
                OutlinedButton(
                    onClick = { captionsStyle = sty },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f).testTag("caption_style_$sty"),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(sty, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Background Music
        Text("Background Music Track", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(6.dp))
        var musicExpanded by remember { mutableStateOf(false) }
        Box {
            OutlinedButton(onClick = { musicExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(backgroundMusic)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                }
            }
            DropdownMenu(expanded = musicExpanded, onDismissRequest = { musicExpanded = false }) {
                musicTracks.forEach { track ->
                    DropdownMenuItem(
                        text = { Text(track) },
                        onClick = {
                            backgroundMusic = track
                            musicExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Volume balances
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Voice Narration vol: ${(voiceVolume * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Slider(value = voiceVolume, onValueChange = { voiceVolume = it }, valueRange = 0.0f..1.0f)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Back Music vol: ${(musicVolume * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Slider(value = musicVolume, onValueChange = { musicVolume = it }, valueRange = 0.0f..1.0f)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Assembly Confirmation CTA
        if (isRendering) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                LinearProgressIndicator(progress = renderProgress, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Rendering Video Assets: ${(renderProgress * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Button(
                onClick = {
                    viewModel.assembleAndSyncVideo(captionsStyle, backgroundMusic, musicVolume, voiceVolume)
                    Toast.makeText(context, "Voice synced! Optimization scores generated below.", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("apply_playback_settings_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Lock Settings & Generate AI Scores", fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

// ----------------------------------------------------
// 4. PUBLISH, SEO & EXPORT VIEW
// ----------------------------------------------------
@Composable
fun SeoAndExportView(
    project: VideoProject,
    viewModel: VideoStudioViewModel,
    isGeneratingSeo: Boolean
) {
    var seoTitle by remember(project.id) { mutableStateOf(project.seoTitle) }
    var seoDesc by remember(project.id) { mutableStateOf(project.seoDescription) }
    var exportFormat by remember { mutableStateOf("YouTube") }
    var exportQuality by remember { mutableStateOf("1080p") }
    var isSavingOffline by remember { mutableStateOf(false) }

    val formats = listOf("YouTube", "YouTube Shorts", "TikTok", "Instagram Reels", "Facebook Reels")
    val qualities = listOf("720p", "1080p", "4K")

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (isGeneratingSeo) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("AI is generating optimized SEO keywords, titles, tags, and thumbnails...", fontSize = 12.sp)
                }
            }
        } else {
            // QUALITY METRICS PANEL
            Text(
                text = "AI Optimization & Quality Scores",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("AI Quality Score:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${project.seoScore}/100", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    }
                    LinearProgressIndicator(
                        progress = project.seoScore / 100f,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Visual Pacing", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Text("${project.seoVisualScore}%", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Speech Flow", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Text("${project.seoSpeechScore}%", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.tertiary)
                        }
                    }

                    if (project.seoImprovements.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Actionable critiques for improvements:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        project.seoImprovements.forEach { tip ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                                Text("• ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Text(tip, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // THUMBNAIL OPTIONS SELECTOR
            Text("AI Thumbnail Generator", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
            Text("Select high-converting click-through CTR thumbnail layouts generated automatically.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(project.thumbnails.indices.toList()) { index ->
                    val thumb = project.thumbnails.getOrNull(index) ?: return@items
                    val isSelected = project.selectedThumbnailIndex == index

                    Card(
                        modifier = Modifier
                            .width(180.dp)
                            .clickable {
                                viewModel.updateProjectFields { p -> p.copy(selectedThumbnailIndex = index) }
                            }
                            .testTag("thumbnail_card_$index"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .background(Color.DarkGray)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(thumb.imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Thumbnail Option",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                // Overlay bold text mockup
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(8.dp)
                                        .background(Color.Black.copy(alpha = 0.8f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        thumb.textOverlay,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Yellow
                                    )
                                }
                            }
                            PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            Text(
                                text = "Option ${index + 1}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // METADATA PANEL
            Text("AI Video SEO Package", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            Text("Video Title (CTR Optimized)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = seoTitle,
                onValueChange = { seoTitle = it },
                modifier = Modifier.fillMaxWidth().testTag("seo_title_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Video Description (Tag-optimized)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = seoDesc,
                onValueChange = { seoDesc = it },
                modifier = Modifier.fillMaxWidth().height(120.dp).testTag("seo_desc_input")
            )

            if (project.seoTags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Suggested Hashtags", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(project.seoTags) { h ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("#$h", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(20.dp))

            // RENDER EXPORT PANEL
            Text("Final Render Configuration", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(12.dp))

            Text("Target Network Format", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))
            var formatExpanded by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(onClick = { formatExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(exportFormat)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Format drop")
                    }
                }
                DropdownMenu(expanded = formatExpanded, onDismissRequest = { formatExpanded = false }) {
                    formats.forEach { f ->
                        DropdownMenuItem(text = { Text(f) }, onClick = { exportFormat = f; formatExpanded = false })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Render Quality Resolution", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                qualities.forEach { q ->
                    val isSelected = exportQuality == q
                    OutlinedButton(
                        onClick = { exportQuality = q },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f).testTag("render_quality_$q"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(q)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // RENDER PROGRESS/TRIGGER
            val isRendering by viewModel.isRendering.collectAsState()
            val renderProg by viewModel.renderProgress.collectAsState()

            if (isRendering) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    LinearProgressIndicator(progress = renderProg, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Rendering complete video: ${(renderProg * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            isSavingOffline = true
                            viewModel.updateProjectFields { p -> p.copy(seoTitle = seoTitle, seoDescription = seoDesc) }
                            Toast.makeText(context, "Saved project state offline locally in Room!", Toast.LENGTH_SHORT).show()
                            isSavingOffline = false
                        },
                        modifier = Modifier.weight(1f).height(50.dp).testTag("save_draft_local_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Project")
                    }

                    Button(
                        onClick = {
                            // First save metadata, then render
                            viewModel.updateProjectFields { p -> p.copy(seoTitle = seoTitle, seoDescription = seoDesc) }
                            viewModel.renderAndExportVideo(exportFormat, exportQuality) {
                                Toast.makeText(context, "Render complete! Exported $exportQuality video format: $exportFormat", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.weight(1.3f).height(50.dp).testTag("render_video_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Export Video", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}
