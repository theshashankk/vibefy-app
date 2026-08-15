package com.vibefy.musicwtf.data.repository

import android.content.Context
import com.vibefy.musicwtf.BuildConfig
import com.vibefy.musicwtf.data.model.PlaylistEntry
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private var cachedPlaylists: List<PlaylistEntry>? = null

    fun getPlaylists(): Flow<List<PlaylistEntry>> = flow {
        if (cachedPlaylists != null) {
            emit(cachedPlaylists!!)
            return@flow
        }

        // 1. Try fetching live online playlists.json from Vercel
        try {
            val request = Request.Builder()
                .url("${BuildConfig.BASE_URL}/playlists.json")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonStr = response.body?.string()
                if (!jsonStr.isNullOrBlank()) {
                    val parsed = json.decodeFromString<List<PlaylistEntry>>(jsonStr).distinctBy { it.id }
                    cachedPlaylists = parsed
                    emit(parsed)
                    return@flow
                }
            }
        } catch (e: Exception) {
            // Suppress network error to fallback to local offline asset
        }

        // 2. Fallback to local bundled playlists.json asset
        val jsonStr = context.assets.open("playlists.json").bufferedReader().use { it.readText() }
        val parsed = json.decodeFromString<List<PlaylistEntry>>(jsonStr).distinctBy { it.id }
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
