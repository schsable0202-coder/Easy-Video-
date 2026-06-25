package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.AppDatabase
import com.example.data.ProjectRepository
import com.example.data.VideoProject
import com.example.domain.GeminiClient
import com.example.domain.MockGenerator
import com.example.domain.ScriptContent
import com.example.domain.ThumbnailOption
import com.example.domain.VideoScene
import com.example.domain.VideoSeo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VideoStudioViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ProjectRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ProjectRepository(database.videoProjectDao())
    }

    // Projects list feed
    val projects: StateFlow<List<VideoProject>> = repository.allProjects
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current active project in studio workspace
    private val _currentProject = MutableStateFlow<VideoProject?>(null)
    val currentProject: StateFlow<VideoProject?> = _currentProject.asStateFlow()

    // Loading states
    private val _isGeneratingScript = MutableStateFlow(false)
    val isGeneratingScript = _isGeneratingScript.asStateFlow()

    private val _isGeneratingStoryboard = MutableStateFlow(false)
    val isGeneratingStoryboard = _isGeneratingStoryboard.asStateFlow()

    private val _isGeneratingSeo = MutableStateFlow(false)
    val isGeneratingSeo = _isGeneratingSeo.asStateFlow()

    private val _isRendering = MutableStateFlow(false)
    val isRendering = _isRendering.asStateFlow()

    private val _renderProgress = MutableStateFlow(0f)
    val renderProgress = _renderProgress.asStateFlow()

    // Simulation & playback states for the video player
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _activeSceneIndex = MutableStateFlow(0)
    val activeSceneIndex = _activeSceneIndex.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f) // 0.0 to 1.0
    val playbackProgress = _playbackProgress.asStateFlow()

    private val _isApiKeySet = MutableStateFlow(true)
    val isApiKeySet = _isApiKeySet.asStateFlow()

    init {
        checkApiKey()
    }

    private fun checkApiKey() {
        val key = BuildConfig.GEMINI_API_KEY
        _isApiKeySet.value = key.isNotEmpty() && key != "MY_GEMINI_API_KEY"
    }

    // Load active project
    fun loadProject(projectId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val project = repository.getProjectById(projectId)
            _currentProject.value = project
            // Reset playback
            _isPlaying.value = false
            _activeSceneIndex.value = 0
            _playbackProgress.value = 0f
        }
    }

    fun selectProject(project: VideoProject) {
        _currentProject.value = project
        _isPlaying.value = false
        _activeSceneIndex.value = 0
        _playbackProgress.value = 0f
    }

    // Create a new blank project and navigate to it
    fun createNewProject(topic: String, duration: String, language: String, voiceGender: String, tone: String, onCreated: (Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val newProj = VideoProject(
                title = "Video on $topic",
                topic = topic,
                duration = duration,
                language = language,
                voiceGender = voiceGender,
                tone = tone,
                status = "setup"
            )
            val newId = repository.insertProject(newProj)
            loadProject(newId)
            launch(Dispatchers.Main) {
                onCreated(newId)
            }
        }
    }

    // Delete a project
    fun deleteProject(projectId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteProjectById(projectId)
            if (_currentProject.value?.id == projectId) {
                _currentProject.value = null
            }
        }
    }

    // Update current project fields directly
    fun updateProjectFields(update: (VideoProject) -> VideoProject) {
        val current = _currentProject.value ?: return
        val updated = update(current)
        _currentProject.value = updated
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateProject(updated)
        }
    }

    // AI WORKFLOW - STEP 1: GENERATE AI SCRIPT
    fun generateAiScript(topic: String, duration: String, language: String, voiceGender: String, tone: String) {
        val current = _currentProject.value ?: return
        _isGeneratingScript.value = true
        checkApiKey()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                var script = if (_isApiKeySet.value) {
                    GeminiClient.generateScript(topic, duration, language, voiceGender, tone)
                } else {
                    null
                }

                // Fallback to simulation mock
                if (script == null) {
                    delay(1500) // Aesthetic delay for simulator feel
                    script = MockGenerator.generateMockScript(topic, duration, language, voiceGender, tone)
                }

                updateProjectFields { proj ->
                    proj.copy(
                        title = "Video on $topic",
                        topic = topic,
                        duration = duration,
                        language = language,
                        voiceGender = voiceGender,
                        tone = tone,
                        scriptIntro = script.introduction,
                        scriptMain = script.mainContent,
                        scriptConclusion = script.conclusion,
                        scriptCallToAction = script.callToAction,
                        status = "script_ready"
                    )
                }
            } catch (e: Exception) {
                Log.e("VideoStudioViewModel", "Script generation failed", e)
            } finally {
                _isGeneratingScript.value = false
            }
        }
    }

    // AI WORKFLOW - STEP 2: GENERATE STORYBOARD SCENES & AUDIO TIMING
    fun generateStoryboardScenes() {
        val current = _currentProject.value ?: return
        _isGeneratingStoryboard.value = true
        checkApiKey()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                var scenes = if (_isApiKeySet.value) {
                    GeminiClient.generateScenes(
                        current.topic,
                        current.scriptIntro,
                        current.scriptMain,
                        current.scriptConclusion,
                        current.scriptCallToAction
                    )
                } else {
                    emptyList()
                }

                // Fallback to simulation mock
                if (scenes.isEmpty()) {
                    delay(2000) // Aesthetic loading experience
                    scenes = MockGenerator.generateMockScenes(
                        current.topic,
                        ScriptContent(
                            current.scriptIntro,
                            current.scriptMain,
                            current.scriptConclusion,
                            current.scriptCallToAction
                        )
                    )
                }

                // Generate default mock thumbnail list based on scenes
                val thumbOptions = if (_isApiKeySet.value) {
                    GeminiClient.generateThumbnails(current.topic, scenes)
                } else {
                    emptyList()
                }
                
                val finalThumbs = if (thumbOptions.isEmpty()) {
                    MockGenerator.generateMockThumbnails(current.topic)
                } else {
                    thumbOptions
                }

                updateProjectFields { proj ->
                    proj.copy(
                        scenes = scenes,
                        thumbnails = finalThumbs,
                        selectedThumbnailIndex = 0,
                        status = "scenes_ready"
                    )
                }
            } catch (e: Exception) {
                Log.e("VideoStudioViewModel", "Scenes generation failed", e)
            } finally {
                _isGeneratingStoryboard.value = false
            }
        }
    }

    // Regenerate a single scene's visual or prompts
    fun regenerateSceneVisual(sceneNumber: Int, customPrompt: String) {
        val current = _currentProject.value ?: return
        viewModelScope.launch(Dispatchers.Default) {
            val updatedScenes = current.scenes.map { scene ->
                if (scene.sceneNumber == sceneNumber) {
                    scene.copy(
                        visualPrompt = customPrompt,
                        visualUrl = "https://picsum.photos/seed/regen_${System.currentTimeMillis()}/800/450"
                    )
                } else {
                    scene
                }
            }
            updateProjectFields { proj ->
                proj.copy(scenes = updatedScenes)
            }
        }
    }

    // Save manual modifications of a scene
    fun updateSceneContent(sceneNumber: Int, onScreen: String, narration: String, transition: String, duration: Int) {
        val current = _currentProject.value ?: return
        val updatedScenes = current.scenes.map { scene ->
            if (scene.sceneNumber == sceneNumber) {
                scene.copy(
                    onScreenText = onScreen,
                    narrationText = narration,
                    transitionEffect = transition,
                    durationSeconds = duration
                )
            } else {
                scene
            }
        }
        updateProjectFields { proj ->
            proj.copy(scenes = updatedScenes)
        }
    }

    // Replace visuals manually (Mock local upload or custom url)
    fun replaceSceneVisualUrl(sceneNumber: Int, customUrl: String) {
        val current = _currentProject.value ?: return
        val updatedScenes = current.scenes.map { scene ->
            if (scene.sceneNumber == sceneNumber) {
                scene.copy(visualUrl = customUrl)
            } else {
                scene
            }
        }
        updateProjectFields { proj ->
            proj.copy(scenes = updatedScenes)
        }
    }

    // AI WORKFLOW - STEP 3: ANALYZE SEO & SCORE QUALITY PRE-EXPORT
    fun generateSeoAndOptimization() {
        val current = _currentProject.value ?: return
        _isGeneratingSeo.value = true
        checkApiKey()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                var seo = if (_isApiKeySet.value) {
                    GeminiClient.generateSeoAndQuality(
                        current.topic,
                        current.scriptIntro,
                        current.scriptMain,
                        current.scriptConclusion,
                        current.scriptCallToAction
                    )
                } else {
                    null
                }

                // Fallback to simulation mock
                if (seo == null) {
                    delay(1500)
                    seo = MockGenerator.generateMockSeo(current.topic)
                }

                updateProjectFields { proj ->
                    proj.copy(
                        seoTitle = seo.title,
                        seoDescription = seo.description,
                        seoTags = seo.hashtags,
                        seoKeywords = seo.keywords,
                        seoScore = seo.qualityScore,
                        seoVisualScore = seo.visualPacingScore,
                        seoSpeechScore = seo.speechNaturalnessScore,
                        seoImprovements = seo.improvements,
                        status = "assembled"
                    )
                }
            } catch (e: Exception) {
                Log.e("VideoStudioViewModel", "SEO generation failed", e)
            } finally {
                _isGeneratingSeo.value = false
            }
        }
    }

    // AI WORKFLOW - STEP 4: TRIGGER ASSEMBLY AND TIMING SYNC
    fun assembleAndSyncVideo(captionsStyle: String, backgroundMusic: String, musicVol: Float, voiceVol: Float) {
        updateProjectFields { proj ->
            proj.copy(
                captionsStyle = captionsStyle,
                backgroundMusic = backgroundMusic,
                musicVolume = musicVol,
                voiceVolume = voiceVol,
                status = "assembled"
            )
        }
        // Trigger automatic SEO and Quality scoring alongside assembly
        generateSeoAndOptimization()
    }

    // Video Player Playback loop
    fun togglePlayback() {
        _isPlaying.value = !_isPlaying.value
        if (_isPlaying.value) {
            runPlaybackLoop()
        }
    }

    private fun runPlaybackLoop() {
        viewModelScope.launch(Dispatchers.Main) {
            val project = _currentProject.value ?: return@launch
            val scenes = project.scenes
            if (scenes.isEmpty()) return@launch

            // If we are at the end, reset first
            if (_activeSceneIndex.value >= scenes.size - 1 && _playbackProgress.value >= 1.0f) {
                _activeSceneIndex.value = 0
                _playbackProgress.value = 0f
            }

            while (_isPlaying.value) {
                val currentIndex = _activeSceneIndex.value
                val activeScene = scenes.getOrNull(currentIndex) ?: break
                val sceneDurMs = (activeScene.durationSeconds * 1000).toLong()
                val stepMs = 100L // 10 steps per second for smooth slider
                val stepsCount = sceneDurMs / stepMs

                var currentStep = (_playbackProgress.value * stepsCount).toInt()

                while (currentStep < stepsCount && _isPlaying.value) {
                    delay(stepMs)
                    currentStep++
                    _playbackProgress.value = currentStep.toFloat() / stepsCount
                }

                if (_isPlaying.value) {
                    // Next scene
                    if (currentIndex < scenes.size - 1) {
                        _activeSceneIndex.value = currentIndex + 1
                        _playbackProgress.value = 0f
                    } else {
                        // Reached the end
                        _isPlaying.value = false
                        _playbackProgress.value = 1.0f
                    }
                }
            }
        }
    }

    fun seekToScene(sceneIndex: Int) {
        val current = _currentProject.value ?: return
        if (sceneIndex in current.scenes.indices) {
            _activeSceneIndex.value = sceneIndex
            _playbackProgress.value = 0f
        }
    }

    // RENDER & EXPORT VIDEO SIMULATOR
    fun renderAndExportVideo(format: String, quality: String, onFinished: () -> Unit) {
        _isRendering.value = true
        _renderProgress.value = 0f
        viewModelScope.launch(Dispatchers.Default) {
            for (i in 1..100) {
                delay(40) // total ~4 seconds render progress bar
                _renderProgress.value = i / 100f
            }
            _isRendering.value = false
            updateProjectFields { proj ->
                proj.copy(status = "exported")
            }
            launch(Dispatchers.Main) {
                onFinished()
            }
        }
    }
}
