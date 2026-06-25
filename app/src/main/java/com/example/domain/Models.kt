package com.example.domain

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VideoScene(
    val sceneNumber: Int,
    val onScreenText: String,
    val narrationText: String,
    val visualPrompt: String,
    val sceneDescription: String,
    val transitionEffect: String = "Fade",
    val visualUrl: String = "",
    val durationSeconds: Int = 10
)

@JsonClass(generateAdapter = true)
data class ThumbnailOption(
    val imageUrl: String,
    val textOverlay: String,
    val isSuggested: Boolean = true
)

@JsonClass(generateAdapter = true)
data class ScriptContent(
    val introduction: String,
    val mainContent: String,
    val conclusion: String,
    val callToAction: String
)

@JsonClass(generateAdapter = true)
data class VideoSeo(
    val title: String = "",
    val description: String = "",
    val hashtags: List<String> = emptyList(),
    val keywords: List<String> = emptyList(),
    val qualityScore: Int = 85,
    val visualPacingScore: Int = 90,
    val speechNaturalnessScore: Int = 80,
    val improvements: List<String> = emptyList()
)
