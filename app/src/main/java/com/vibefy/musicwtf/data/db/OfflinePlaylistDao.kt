package com.vibefy.musicwtf.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflinePlaylistDao {

    @Query("SELECT * FROM offline_playlists ORDER BY downloadedAt DESC")
    fun getAllOfflinePlaylists(): Flow<List<OfflinePlaylistEntity>>

    @Query("SELECT * FROM offline_playlists WHERE id = :id LIMIT 1")
    suspend fun getPlaylistById(id: String): OfflinePlaylistEntity?

    @Query("SELECT * FROM offline_playlists WHERE id = :id LIMIT 1")
    fun observePlaylistById(id: String): Flow<OfflinePlaylistEntity?>

    @Query("SELECT * FROM offline_tracks WHERE playlistId = :playlistId ORDER BY position ASC")
    fun getTracksForPlaylist(playlistId: String): Flow<List<OfflineTrackEntity>>

    @Query("SELECT * FROM offline_tracks WHERE playlistId = :playlistId ORDER BY position ASC")
    suspend fun getTracksForPlaylistSync(playlistId: String): List<OfflineTrackEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: OfflinePlaylistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<OfflineTrackEntity>)

    @Query("UPDATE offline_playlists SET downloadStatus = :status, downloadProgress = :progress WHERE id = :id")
    suspend fun updateDownloadProgress(id: String, status: DownloadStatus, progress: Int)

    @Query("UPDATE offline_playlists SET updateAvailable = :hasUpdate, remoteVersionHash = :newHash WHERE id = :id")
    suspend fun markUpdateAvailable(id: String, hasUpdate: Boolean, newHash: String)

    @Query("UPDATE offline_tracks SET localAudioPath = :audioPath, localCoverPath = :coverPath, isDownloaded = 1 WHERE id = :trackId")
    suspend fun updateTrackLocalPaths(trackId: String, audioPath: String, coverPath: String)

    @Query("DELETE FROM offline_playlists WHERE id = :id")
    suspend fun deletePlaylist(id: String)
}
