package com.vibefy.musicwtf.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibefy.musicwtf.data.model.PlaylistEntry
import com.vibefy.musicwtf.data.repository.CatalogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val playlist: PlaylistEntry? = null,
    val isLoaded: Boolean = false,
    val loadProgress: Int = 0,
    val statusText: String = "Tuning frequencies…",
    val isDead: Boolean = false,
    val userNavigatedInside: Boolean = false,
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val catalogRepository: CatalogRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    fun loadPlaylist(id: String) {
        viewModelScope.launch {
            val playlist = catalogRepository.getPlaylistById(id)
            _uiState.update { it.copy(playlist = playlist, isDead = playlist?.dead == true) }
        }
    }

    fun onPageStarted() {
        _uiState.update { it.copy(isLoaded = false, statusText = "Warming vacuum tubes…") }
    }

    fun onProgressChanged(progress: Int) {
        _uiState.update { state ->
            val text = when {
                progress < 35 -> "Tuning frequencies…"
                progress < 70 -> "Warming analog vacuum tubes…"
                progress < 90 -> "Connecting to ${state.playlist?.title ?: "vibe-site"}…"
                else -> "Signal locked!"
            }
            state.copy(loadProgress = progress, statusText = text)
        }
    }

    fun onPageFinished() {
        _uiState.update { it.copy(isLoaded = true, loadProgress = 100, statusText = "Signal locked!") }
    }

    fun flagBroken() {
        _uiState.update { it.copy(isDead = true) }
    }

    fun retry() {
        _uiState.update { it.copy(isDead = false, isLoaded = false, loadProgress = 0) }
    }
}
