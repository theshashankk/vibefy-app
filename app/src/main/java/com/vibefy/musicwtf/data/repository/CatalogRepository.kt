package com.vibefy.musicwtf.data.repository

import android.content.Context
import com.vibefy.musicwtf.data.model.PlaylistEntry
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private var cachedPlaylists: List<PlaylistEntry>? = null

    fun getPlaylists(): Flow<List<PlaylistEntry>> = flow {
        if (cachedPlaylists != null) {
            emit(cachedPlaylists!!)
            return@flow
        }
        val jsonStr = context.assets.open("playlists.json").bufferedReader().use { it.readText() }
        val parsed = json.decodeFromString<List<PlaylistEntry>>(jsonStr)
        cachedPlaylists = parsed
        emit(parsed)
    }.flowOn(Dispatchers.IO)

    suspend fun getPlaylistById(id: String): PlaylistEntry? {
        if (cachedPlaylists == null) {
            val jsonStr = context.assets.open("playlists.json").bufferedReader().use { it.readText() }
            cachedPlaylists = json.decodeFromString(jsonStr)
        }
        return cachedPlaylists?.find { it.id == id || it.numId.toString() == id }
    }
}
