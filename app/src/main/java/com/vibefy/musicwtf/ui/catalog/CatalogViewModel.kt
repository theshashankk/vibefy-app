package com.vibefy.musicwtf.ui.catalog

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

data class CatalogUiState(
    val playlists: List<PlaylistEntry> = emptyList(),
    val visiblePlaylists: List<PlaylistEntry> = emptyList(),
    val categories: List<String> = listOf("All", "Safar", "Raat", "Bachpan", "Monsoon", "Pahadi", "Indie", "Retro", "Bhakti", "Lofi", "Hiphop", "Kshetriya"),
    val activeCategory: String = "All",
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val isOffline: Boolean = false,
)

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val repository: CatalogRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init {
        loadPlaylists()
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            repository.getPlaylists().collect { items ->
                _uiState.update { state ->
                    state.copy(
                        playlists = items,
                        visiblePlaylists = filterPlaylists(items, state.activeCategory, state.searchQuery),
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun onCategorySelect(category: String) {
        _uiState.update { state ->
            val updated = filterPlaylists(state.playlists, category, state.searchQuery)
            state.copy(activeCategory = category, visiblePlaylists = updated)
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            val updated = filterPlaylists(state.playlists, state.activeCategory, query)
            state.copy(searchQuery = query, visiblePlaylists = updated)
        }
    }

    private fun filterPlaylists(
        all: List<PlaylistEntry>,
        cat: String,
        query: String,
    ): List<PlaylistEntry> {
        return all.filter { p ->
            val matchesCategory = (cat == "All") || (p.category.equals(cat, ignoreCase = true))
            val matchesQuery = query.isBlank() || (
                p.title.contains(query, ignoreCase = true) ||
                p.owner.contains(query, ignoreCase = true) ||
                p.description.contains(query, ignoreCase = true) ||
                p.category.contains(query, ignoreCase = true)
            )
            matchesCategory && matchesQuery
        }
    }
}
