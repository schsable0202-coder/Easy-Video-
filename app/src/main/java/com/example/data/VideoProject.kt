package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.ThumbnailOption
import com.example.domain.VideoScene

@Entity(tableName = "video_projects")
data class VideoProject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val topic: String,
    val duration: String,
    val language: String,
    val voiceGender: String,
    val speed: Float = 1.0f,
    val pitch: Float = 1.0f,
    val tone: String = "Professional",
    
    // Script components
    val scriptIntro: String = "",
    val scriptMain: String = "",
    val scriptConclusion: String = "",
    val scriptCallToAction: String = "",
    
    // Storyboard scenes
    val scenes: List<VideoScene> = emptyList(),
    
    // SEO & Optimization features
    val seoTitle: String = "",
    val seoDescription: String = "",
    val seoTags: List<String> = emptyList(),
    val seoKeywords: List<String> = emptyList(),
    val seoScore: Int = 85,
    val seoVisualScore: Int = 90,
    val seoSpeechScore: Int = 80,
    val seoImprovements: List<String> = emptyList(),
    
    // Visual Options
    val selectedThumbnailIndex: Int = 0,
    val thumbnails: List<ThumbnailOption> = emptyList(),
    
    // Formatting & Editing details
    val captionsStyle: String = "Animated Pop-up",
    val backgroundMusic: String = "Cinematic Ambient",
    val musicVolume: Float = 0.3f,
    val voiceVolume: Float = 0.8f,
    
    val status: String = "setup", // setup, script_ready, scenes_ready, assembled, exported
    val createdAt: Long = System.currentTimeMillis()
)
