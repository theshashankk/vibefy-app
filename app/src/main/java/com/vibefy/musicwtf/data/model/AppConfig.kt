package com.vibefy.musicwtf.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    val aboutTitle: String = "Gaane WTF!",
    val aboutVersion: String = "Version 1.0.0 (Independence Build)",
    val aboutDescription: String = "A platform where each playlist is a fully art-directed micro-experience built by its creator. Every creator's player UI ships intact — never replaced with generic templates.",
    val githubUrl: String = "https://github.com/theshashankk/Vibefy",
    val twitterUrl: String = "https://x.com/theshashankk",
    val announcementMessage: String? = null,
    val announcementLink: String? = null,
)
