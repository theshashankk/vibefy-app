package com.vibefy.musicwtf.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistEntry(
    val id: String,
    val numId: Int = 0,
    val title: String,
    val category: String = "Safar",
    val cover: String = "",
    val playlistUrl: String = "",
    val originalSiteUrl: String = "",
    val mirroredSiteUrl: String? = null,
    val description: String = "",
    val brandLabel: String? = null,
    val owner: String = "",
    val ownerTwitterUrl: String = "",
    val accentColor: String = "#d97706",
    val listeners: Int = 0,
    val dead: Boolean? = false,
)

@Serializable
data class JukeboxPlaylist(
    val id: String,
    val slug: String,
    val title: String,
    val description: String,
    val coverUrl: String,
    val accentColor: String = "#f59e0b",
    val versionHash: String = "",
    val tracks: List<JukeboxTrack> = emptyList(),
)

@Serializable
data class JukeboxTrack(
    val id: String,
    val playlistId: String,
    val title: String,
    val artist: String,
    val durationSec: Int,
    val remoteAudioUrl: String,
    val coverUrl: String,
    val position: Int,
) {
    val durationStr: String
        get() {
            val m = durationSec / 60
            val s = durationSec % 60
            return "%d:%02d".format(m, s)
        }
}
