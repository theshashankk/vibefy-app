package com.vibefy.musicwtf.ui.jukebox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibefy.musicwtf.data.db.OfflinePlaylistEntity
import com.vibefy.musicwtf.data.db.OfflineTrackEntity
import com.vibefy.musicwtf.data.model.JukeboxPlaylist
import com.vibefy.musicwtf.data.model.JukeboxTrack
import com.vibefy.musicwtf.data.repository.OfflinePlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JukeboxViewModel @Inject constructor(
    private val offlineRepository: OfflinePlaylistRepository,
) : ViewModel() {

    private val samplePlaylists = listOf(
        JukeboxPlaylist(
            id = "jk-1",
            slug = "vintage-lounge",
            title = "Vintage Lounge 70s",
            description = "Original analog studio masters from 1974",
            coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600",
            accentColor = "#f59e0b",
            versionHash = "v1.0.4",
            tracks = listOf(
                JukeboxTrack("t-1", "jk-1", "Safar Express", "R.D. Vibe Band", 214, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600", 1),
                JukeboxTrack("t-2", "jk-1", "Monsoon Rain Blues", "Anand Brothers", 188, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600", 2),
                JukeboxTrack("t-3", "jk-1", "Midnight Taxi Radio", "Ghazal Ensemble", 245, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600", 3),
            )
        ),
        JukeboxPlaylist(
            id = "jk-2",
            slug = "sufi-acoustics",
            title = "Sufi Acoustic Sessions",
            description = "Unplugged original recordings in 24-bit audio",
            coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600",
            accentColor = "#10b981",
            versionHash = "v1.1.0",
            tracks = listOf(
                JukeboxTrack("t-4", "jk-2", "Dariya Unplugged", "Nizam Folk Studio", 260, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3", "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600", 1),
                JukeboxTrack("t-5", "jk-2", "Noor-E-Subah", "Soul Sufi Duo", 210, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3", "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600", 2),
            )
        )
    )

    private val _playlists = MutableStateFlow(samplePlaylists)
    val playlists: StateFlow<List<JukeboxPlaylist>> = _playlists.asStateFlow()

    fun downloadPlaylist(playlist: JukeboxPlaylist) {
        viewModelScope.launch {
            val entity = OfflinePlaylistEntity(
                id = playlist.id,
                slug = playlist.slug,
                title = playlist.title,
                description = playlist.description,
                coverUrl = playlist.coverUrl,
                accentColor = playlist.accentColor,
                versionHash = playlist.versionHash,
            )
            val trackEntities = playlist.tracks.map { t ->
                OfflineTrackEntity(
                    id = t.id,
                    playlistId = t.playlistId,
                    title = t.title,
                    artist = t.artist,
                    durationSec = t.durationSec,
                    remoteAudioUrl = t.remoteAudioUrl,
                    remoteCoverUrl = t.coverUrl,
                    position = t.position,
                )
            }
            offlineRepository.startDownload(entity, trackEntities)
        }
    }
}
