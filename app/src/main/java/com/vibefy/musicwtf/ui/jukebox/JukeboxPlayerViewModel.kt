package com.vibefy.musicwtf.ui.jukebox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibefy.musicwtf.data.model.JukeboxPlaylist
import com.vibefy.musicwtf.data.model.JukeboxTrack
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JukeboxPlayerState(
    val playlist: JukeboxPlaylist? = null,
    val tracks: List<JukeboxTrack> = emptyList(),
    val currentIndex: Int = 0,
    val isPlaying: Boolean = false,
    val progressFraction: Float = 0.25f,
    val currentSec: Int = 45,
    val durationSec: Int = 214,
    val isShuffle: Boolean = false,
    val isLooping: Boolean = false,
    val showQueue: Boolean = false,
) {
    val currentTrack: JukeboxTrack?
        get() = tracks.getOrNull(currentIndex)
}

@HiltViewModel
class JukeboxPlayerViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(JukeboxPlayerState())
    val uiState: StateFlow<JukeboxPlayerState> = _uiState.asStateFlow()

    fun load(jukeboxId: String) {
        val samplePlaylist = JukeboxPlaylist(
            id = jukeboxId,
            slug = "vintage-lounge",
            title = "Vintage Lounge 70s",
            description = "Original analog studio masters from 1974",
            coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600",
            accentColor = "#f59e0b",
            versionHash = "v1.0.4",
            tracks = listOf(
                JukeboxTrack("t-1", jukeboxId, "Safar Express", "R.D. Vibe Band", 214, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600", 1),
                JukeboxTrack("t-2", jukeboxId, "Monsoon Rain Blues", "Anand Brothers", 188, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600", 2),
                JukeboxTrack("t-3", jukeboxId, "Midnight Taxi Radio", "Ghazal Ensemble", 245, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600", 3),
            )
        )
        _uiState.update {
            it.copy(
                playlist = samplePlaylist,
                tracks = samplePlaylist.tracks,
                currentIndex = 0,
                durationSec = samplePlaylist.tracks.firstOrNull()?.durationSec ?: 214,
            )
        }
    }

    fun togglePlayPause() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    fun seekTo(fraction: Float) {
        _uiState.update { state ->
            val sec = (fraction * state.durationSec).toInt()
            state.copy(progressFraction = fraction, currentSec = sec)
        }
    }

    fun next() {
        _uiState.update { state ->
            val nextIdx = (state.currentIndex + 1) % state.tracks.size.coerceAtLeast(1)
            state.copy(currentIndex = nextIdx, progressFraction = 0f, currentSec = 0)
        }
    }

    fun prev() {
        _uiState.update { state ->
            val prevIdx = if (state.currentIndex > 0) state.currentIndex - 1 else state.tracks.size - 1
            state.copy(currentIndex = prevIdx.coerceAtLeast(0), progressFraction = 0f, currentSec = 0)
        }
    }

    fun rewind10() {
        _uiState.update { state ->
            val newSec = (state.currentSec - 10).coerceAtLeast(0)
            state.copy(currentSec = newSec, progressFraction = newSec.toFloat() / state.durationSec)
        }
    }

    fun forward10() {
        _uiState.update { state ->
            val newSec = (state.currentSec + 10).coerceAtMost(state.durationSec)
            state.copy(currentSec = newSec, progressFraction = newSec.toFloat() / state.durationSec)
        }
    }

    fun toggleShuffle() {
        _uiState.update { it.copy(isShuffle = !it.isShuffle) }
    }

    fun toggleLoop() {
        _uiState.update { it.copy(isLooping = !it.isLooping) }
    }

    fun toggleQueue() {
        _uiState.update { it.copy(showQueue = !it.showQueue) }
    }

    fun jumpTo(index: Int) {
        _uiState.update { it.copy(currentIndex = index, progressFraction = 0f, currentSec = 0) }
    }
}
