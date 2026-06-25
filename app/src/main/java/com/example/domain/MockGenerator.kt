package com.example.domain

import java.util.UUID

object MockGenerator {
    fun generateMockScript(
        topic: String,
        duration: String,
        language: String,
        voiceGender: String,
        tone: String
    ): ScriptContent {
        val normalized = topic.lowercase()
        return when {
            normalized.contains("vitamin b12") || normalized.contains("vitamin") -> {
                ScriptContent(
                    introduction = "Did you know that a simple vitamin deficiency could be the secret reason why you feel tired all day? Let's talk about the incredible benefits of Vitamin B12!",
                    mainContent = "Vitamin B12 is crucial for your body. It plays a vital role in red blood cell formation, cell metabolism, nerve function, and the production of DNA.\n" +
                            "Since our body doesn't produce it, we must get it from our diet—like beef, fish, eggs, dairy, or fortified cereals.\n" +
                            "A deficiency can lead to fatigue, anemia, and even neurological changes. Ensuring adequate intake keeps your red blood cells healthy and boosts your daily energy levels!",
                    conclusion = "In summary, keeping your B12 levels optimal is key to maintaining high energy, sharp focus, and healthy cellular renewal.",
                    callToAction = "If you found this helpful, click the subscribe button, leave a thumbs up, and comment below with your favorite energy-boosting foods!"
                )
            }
            normalized.contains("artificial intelligence") || normalized.contains("ai") || normalized.contains("history") -> {
                ScriptContent(
                    introduction = "From science fiction dreams to daily reality, the history of Artificial Intelligence is a thrilling ride through human ingenuity. Let's trace how it all started!",
                    mainContent = "The journey began in 1950 when Alan Turing proposed the Turing Test, asking: 'Can machines think?'\n" +
                            "In 1956, the term 'Artificial Intelligence' was coined at the Dartmouth Conference, initiating decades of research.\n" +
                            "We survived 'AI winters' of low funding and high skepticism, until deep learning and massive compute capabilities triggered the modern revolution, giving rise to generative neural networks.",
                    conclusion = "From simple logic circuits to digital minds capable of writing code and painting art, AI has rewritten our relationship with technology.",
                    callToAction = "What do you think is next for AI? Subscribe for more tech history, and drop your theories in the comments section below!"
                )
            }
            normalized.contains("blood sugar") || normalized.contains("glucose") -> {
                ScriptContent(
                    introduction = "Tired of energy crashes and constant sugar cravings? Here is how to naturally support healthy, stable blood sugar levels starting today!",
                    mainContent = "First, prioritize fiber-rich foods like oats, beans, and leafy greens. Fiber slows carb digestion, preventing sharp glucose spikes.\n" +
                            "Second, don't underestimate walking. Just a ten-minute walk after meals helps muscles absorb excess glucose directly from the bloodstream.\n" +
                            "Third, focus on hydration and sound sleep. Dehydration concentrates blood sugar, while poor sleep increases cortisol, raising insulin resistance.",
                    conclusion = "Combining high fiber, post-meal walking, and proper hydration is a natural, sustainable way to keep your metabolism thriving.",
                    callToAction = "Subscribe for simple, evidence-based wellness tips. Let us know in the comments: which of these tips will you try first?"
                )
            }
            normalized.contains("pune") || normalized.contains("travel") -> {
                ScriptContent(
                    introduction = "Welcome to Pune, the cultural capital of Maharashtra! Today, we're counting down the top 10 places you absolutely must visit in this historical city.",
                    mainContent = "We start our journey at Shaniwar Wada, the majestic fortification built by the Peshwas.\n" +
                            "Next, climb up to Sinhagad Fort for breathtaking panoramic views of the Western Ghats, especially during the monsoon!\n" +
                            "Don't miss the serene Aga Khan Palace, a monument of national importance where Mahatma Gandhi was closely interned, or food crawls through Camp for world-famous Shrewsbury biscuits.",
                    conclusion = "With its rich Peshwa legacy, stunning hill forts, and mouthwatering culinary spots, Pune blends heritage and modern student life perfectly.",
                    callToAction = "Have you visited Pune before? Subscribe to our channel, share this video with your travel buddy, and list your favorite Pune spots below!"
                )
            }
            else -> {
                ScriptContent(
                    introduction = "Are you curious about $topic? Today, we are diving deep into everything you need to know about this fascinating subject, so stick around!",
                    mainContent = "When exploring $topic, the most important aspect to understand is its core impact on our daily life.\n" +
                            "Historically, it has evolved significantly, driven by innovation, community interest, and global trends.\n" +
                            "Today, applying these concepts can help solve common challenges, make your workflow more efficient, and unlock new creative potentials in an ever-changing landscape.",
                    conclusion = "Understanding the foundations of $topic gives you a powerful perspective to master it and share valuable insights with others.",
                    callToAction = "If you enjoyed this deep-dive, don't forget to hit that subscribe button, ring the bell, and like this video to support our channel!"
                )
            }
        }
    }

    fun generateMockScenes(topic: String, script: ScriptContent): List<VideoScene> {
        val normalized = topic.lowercase()
        val style = "Cinematic 3D render, vibrant color grade, soft volumetric lighting, high dynamic range"
        
        return listOf(
            VideoScene(
                sceneNumber = 1,
                onScreenText = if (normalized.contains("vitamin")) "Unlocking Daily Energy" else "Introduction: $topic",
                narrationText = script.introduction,
                visualPrompt = "Close-up shot of a glowing translucent vitamin pill reflecting light on a dark sleek background, photorealistic, 4k",
                sceneDescription = "A striking, high-contrast intro visual setting the context of the presentation.",
                transitionEffect = "Fade",
                visualUrl = "https://picsum.photos/seed/${UUID.randomUUID()}/800/450",
                durationSeconds = 8
            ),
            VideoScene(
                sceneNumber = 2,
                onScreenText = "The Deep Science",
                narrationText = script.mainContent.substringBefore("\n"),
                visualPrompt = "Scientific representation of neural connections pulsing with golden energy, digital art style, volumetric glow, high-end CGI",
                sceneDescription = "An educational graphic or metaphorical visualization explaining the science or core details.",
                transitionEffect = "Dissolve",
                visualUrl = "https://picsum.photos/seed/${UUID.randomUUID()}/800/450",
                durationSeconds = 12
            ),
            VideoScene(
                sceneNumber = 3,
                onScreenText = "Actionable Insights",
                narrationText = script.mainContent.substringAfter("\n").substringBefore("\n"),
                visualPrompt = "A person happily preparing organic healthy greens and meals in a bright modern kitchen, sunset warm light streaming in, cozy editorial style",
                sceneDescription = "Practical, relatable visuals demonstrating application or real-world evidence.",
                transitionEffect = "Slide Left",
                visualUrl = "https://picsum.photos/seed/${UUID.randomUUID()}/800/450",
                durationSeconds = 14
            ),
            VideoScene(
                sceneNumber = 4,
                onScreenText = "Join the Community!",
                narrationText = script.callToAction,
                visualPrompt = "Minimalist flat illustration of a bell icon popping with tiny red notification circles on a clean modern gradient background, vector art, 3D clay style",
                sceneDescription = "A high-retention call-to-action visual prompting likes, comments, and subscriptions.",
                transitionEffect = "Zoom In",
                visualUrl = "https://picsum.photos/seed/${UUID.randomUUID()}/800/450",
                durationSeconds = 10
            )
        )
    }

    fun generateMockSeo(topic: String): VideoSeo {
        return VideoSeo(
            title = "Secrets of $topic Revealed! (Ultimate Guide)",
            description = "Discover everything you need to know about $topic in this detailed guide. We cover history, practical tips, key benefits, and actionable takeaways you can apply today.\n\nTimestamps:\n0:00 Introduction\n1:15 Core Principles\n3:00 Practical Application\n4:15 Final CTA",
            hashtags = listOf(topic.replace(" ", ""), "educational", "viralvideo", "tutorial", "learnfast"),
            keywords = listOf(topic, "tutorial", "guide", "benefits", "how to", "explainer", "animated", "narration"),
            qualityScore = 94,
            visualPacingScore = 91,
            speechNaturalnessScore = 95,
            improvements = listOf(
                "Shorten the intro text by 1.5 seconds to maximize immediate viewer retention.",
                "Incorporate more visual contrast in Scene 2 to emphasize the scientific keywords.",
                "Ensure your call to action lines up with on-screen graphical subscription stickers."
            )
        )
    }

    fun generateMockThumbnails(topic: String): List<ThumbnailOption> {
        val topicSlug = topic.replace(" ", "_").lowercase()
        return listOf(
            ThumbnailOption(
                imageUrl = "https://picsum.photos/seed/${topicSlug}_1/400/300",
                textOverlay = "STOP IGNORED!",
                isSuggested = true
            ),
            ThumbnailOption(
                imageUrl = "https://picsum.photos/seed/${topicSlug}_2/400/300",
                textOverlay = "100% NATURALLY",
                isSuggested = true
            ),
            ThumbnailOption(
                imageUrl = "https://picsum.photos/seed/${topicSlug}_3/400/300",
                textOverlay = "SECRET REVEALED",
                isSuggested = true
            )
        )
    }
}
