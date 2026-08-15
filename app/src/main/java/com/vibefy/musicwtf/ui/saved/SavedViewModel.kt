package com.vibefy.musicwtf.ui.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibefy.musicwtf.data.db.OfflinePlaylistEntity
import com.vibefy.musicwtf.data.repository.OfflinePlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedViewModel @Inject constructor(
    private val repository: OfflinePlaylistRepository,
) : ViewModel() {

    val offlinePlaylists: Flow<List<OfflinePlaylistEntity>> =
        repository.getOfflinePlaylists()

    init {
        // Schedule periodic background sync to check online update hashes
        repository.schedulePeriodicSync()
    }

    fun updatePlaylist(id: String) {
        viewModelScope.launch {
            repository.syncUpdate(id)
        }
    }

    fun deletePlaylist(id: String) {
        viewModelScope.launch {
            repository.removeOffline(id)
        }
    }
}
