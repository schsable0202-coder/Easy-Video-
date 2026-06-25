package com.example.domain

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val api: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApi::class.java)
    }

    private fun cleanJsonResponse(rawResponse: String): String {
        var clean = rawResponse.trim()
        if (clean.startsWith("```")) {
            // Remove code block wrappers like ```json or ```
            clean = clean.replace(Regex("^```(?:json)?\\s*"), "")
            clean = clean.replace(Regex("\\s*```$"), "")
        }
        return clean.trim()
    }

    suspend fun callGemini(prompt: String, isJson: Boolean = false): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e("GeminiClient", "API Key is missing or default placeholder!")
            return@withContext ""
        }

        val config = if (isJson) GeminiConfig(responseMimeType = "application/json", temperature = 0.4f) else null
        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            generationConfig = config
        )

        try {
            val response = api.generateContent(apiKey, request)
            val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            if (isJson) cleanJsonResponse(rawText) else rawText
        } catch (e: Exception) {
            Log.e("GeminiClient", "API call failed", e)
            ""
        }
    }

    // Custom generators that parse JSON response

    suspend fun generateScript(
        topic: String,
        duration: String,
        language: String,
        voiceGender: String,
        tone: String
    ): ScriptContent? {
        val prompt = """
            Create a complete, engaging video script on the topic: "$topic".
            Target Duration: $duration.
            Language: $language.
            Narration voice gender target: $voiceGender.
            Overall tone: $tone.
            
            Return the output strictly in JSON format matching the following keys:
            {
              "introduction": "An eye-catching, attention-grabbing opening line (max 2-3 sentences)",
              "mainContent": "The core educational or entertaining body of the video with deep insights, formatted cleanly with linebreaks",
              "conclusion": "A concise summary wrapping up the key ideas",
              "callToAction": "An action directive (e.g., Subscribe, hit the like button, visit website, or try this today)"
            }
            Do not include any extra text, only valid JSON.
        """.trimIndent()

        val responseJson = callGemini(prompt, isJson = true)
        if (responseJson.isEmpty()) return null

        return try {
            val adapter = moshi.adapter(ScriptContent::class.java)
            adapter.fromJson(responseJson)
        } catch (e: Exception) {
            Log.e("GeminiClient", "Failed to parse script JSON: $responseJson", e)
            null
        }
    }

    suspend fun generateScenes(
        topic: String,
        scriptIntro: String,
        scriptMain: String,
        scriptConclusion: String,
        scriptCta: String
    ): List<VideoScene> {
        val prompt = """
            Divide the following video script about "$topic" into 4 distinct, engaging chronologically ordered scenes.
            
            Script Details:
            - Introduction: $scriptIntro
            - Main Body: $scriptMain
            - Conclusion: $scriptConclusion
            - Call to Action: $scriptCta
            
            For each scene, generate:
            1. sceneNumber: (integer, starting at 1)
            2. onScreenText: (Overlay caption to burn into the video for this scene)
            3. narrationText: (Exact spoken word script to be voiced over during this scene)
            4. visualPrompt: (Highly detailed prompt for an AI image generator representing this scene, including artistic style, e.g. photorealistic, watercolor, isometric)
            5. sceneDescription: (Overview description of what appears in the scene)
            6. transitionEffect: (Choose from: "Fade", "Dissolve", "Slide Left", "Zoom In")
            7. durationSeconds: (Suggested scene duration in seconds, total should align with pacing)
            
            Return the output strictly as a JSON list matching this structure:
            [
              {
                "sceneNumber": 1,
                "onScreenText": "Headline text",
                "narrationText": "Narrator voice text...",
                "visualPrompt": "Detailed visual style and subject...",
                "sceneDescription": "Description of visuals...",
                "transitionEffect": "Fade",
                "durationSeconds": 8
              },
              ...
            ]
            Only return the JSON list. Do not include markdown wraps or anything except the JSON string itself.
        """.trimIndent()

        val responseJson = callGemini(prompt, isJson = true)
        if (responseJson.isEmpty()) return emptyList()

        return try {
            val listType = Types.newParameterizedType(List::class.java, VideoScene::class.java)
            val adapter = moshi.adapter<List<VideoScene>>(listType)
            adapter.fromJson(responseJson) ?: emptyList()
        } catch (e: Exception) {
            Log.e("GeminiClient", "Failed to parse scenes JSON: $responseJson", e)
            emptyList()
        }
    }

    suspend fun generateSeoAndQuality(
        topic: String,
        scriptIntro: String,
        scriptMain: String,
        scriptConclusion: String,
        scriptCta: String
    ): VideoSeo? {
        val prompt = """
            Analyze the following video concept about "$topic" and generate optimized SEO assets and a Quality Score analysis.
            
            Script:
            - Intro: $scriptIntro
            - Body: $scriptMain
            - Ending: $scriptConclusion / $scriptCta
            
            Provide:
            1. A highly clickable, search-friendly Video Title (SEO Title)
            2. An engaging Description full of search keywords
            3. A list of 5-8 hot trending Hashtags (without '#' symbol in array)
            4. A list of 8-12 search Keywords / tags
            5. A Quality Score (integer 0-100) assessing audience retention potential
            6. A Visual Pacing Score (integer 0-100) assessing visual transitions and flow
            7. A Speech Naturalness Score (integer 0-100) based on phrasing lengths
            8. A list of 3 actionable improvement suggestions or critiques for the creator before rendering.
            
            Return strictly in JSON format:
            {
              "title": "Title Here",
              "description": "Description Text...",
              "hashtags": ["tag1", "tag2"],
              "keywords": ["keyword1", "keyword2"],
              "qualityScore": 92,
              "visualPacingScore": 88,
              "speechNaturalnessScore": 94,
              "improvements": ["Improvement tip 1", "Improvement tip 2"]
            }
            Do not include any extra text.
        """.trimIndent()

        val responseJson = callGemini(prompt, isJson = true)
        if (responseJson.isEmpty()) return null

        return try {
            val adapter = moshi.adapter(VideoSeo::class.java)
            adapter.fromJson(responseJson)
        } catch (e: Exception) {
            Log.e("GeminiClient", "Failed to parse SEO JSON: $responseJson", e)
            null
        }
    }

    suspend fun generateThumbnails(topic: String, scenes: List<VideoScene>): List<ThumbnailOption> {
        val sceneSummary = scenes.joinToString("\n") { "Scene ${it.sceneNumber}: ${it.visualPrompt}" }
        val prompt = """
            Create 3 distinct YouTube/Shorts high-CTR clickable thumbnail style options for a video about "$topic".
            The video has these scene concepts:
            $sceneSummary
            
            For each thumbnail, provide:
            1. imageUrl: (Choose an descriptive aesthetic style prompt representing the thumbnail layout, e.g. "https://picsum.photos/seed/thumb1/400/300")
            2. textOverlay: (Bold, high-impact, short clickable text overlay on the thumbnail, max 4 words)
            3. isSuggested: (true)
            
            Return the output strictly in a JSON list format:
            [
              {
                "imageUrl": "https://picsum.photos/seed/thumb1/400/300",
                "textOverlay": "Clickable Thumbnail Text"
              },
              ...
            ]
            Ensure imageUrl is a valid mock URL (you can use https://picsum.photos/seed/<random>/400/300 to provide visual variety).
        """.trimIndent()

        val responseJson = callGemini(prompt, isJson = true)
        if (responseJson.isEmpty()) return emptyList()

        return try {
            val listType = Types.newParameterizedType(List::class.java, ThumbnailOption::class.java)
            val adapter = moshi.adapter<List<ThumbnailOption>>(listType)
            adapter.fromJson(responseJson) ?: emptyList()
        } catch (e: Exception) {
            Log.e("GeminiClient", "Failed to parse thumbnail options JSON: $responseJson", e)
            emptyList()
        }
    }
}
