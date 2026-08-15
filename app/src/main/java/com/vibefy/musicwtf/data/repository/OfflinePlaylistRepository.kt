package com.vibefy.musicwtf.data.repository

import android.content.Context
import androidx.work.*
import com.vibefy.musicwtf.data.db.DownloadStatus
import com.vibefy.musicwtf.data.db.OfflinePlaylistDao
import com.vibefy.musicwtf.data.db.OfflinePlaylistEntity
import com.vibefy.musicwtf.data.db.OfflineTrackEntity
import com.vibefy.musicwtf.data.worker.PlaylistDownloadWorker
import com.vibefy.musicwtf.data.worker.PlaylistSyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflinePlaylistRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: OfflinePlaylistDao,
    private val workManager: WorkManager,
) {

    fun getOfflinePlaylists(): Flow<List<OfflinePlaylistEntity>> =
        dao.getAllOfflinePlaylists()

    fun getPlaylist(id: String): Flow<OfflinePlaylistEntity?> =
        dao.observePlaylistById(id)

    fun getTracks(playlistId: String): Flow<List<OfflineTrackEntity>> =
        dao.getTracksForPlaylist(playlistId)

    /**
     * Start downloading a full playlist (songs, covers, metadata).
     */
    suspend fun startDownload(
        playlist: OfflinePlaylistEntity,
        tracks: List<OfflineTrackEntity>,
    ) {
        // Save initial metadata in DB
        dao.insertPlaylist(playlist.copy(downloadStatus = DownloadStatus.DOWNLOADING, downloadProgress = 0))
        dao.insertTracks(tracks)

        // Enqueue WorkManager download task
        val data = workDataOf(PlaylistDownloadWorker.KEY_PLAYLIST_ID to playlist.id)
        val downloadRequest = OneTimeWorkRequestBuilder<PlaylistDownloadWorker>()
            .setInputData(data)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            "download_${playlist.id}",
            ExistingWorkPolicy.REPLACE,
            downloadRequest,
        )
    }

    /**
     * Re-download / update playlist when online update is available.
     */
    suspend fun syncUpdate(playlistId: String) {
        val playlist = dao.getPlaylistById(playlistId) ?: return
        val tracks = dao.getTracksForPlaylistSync(playlistId)
        startDownload(playlist.copy(versionHash = playlist.remoteVersionHash), tracks)
    }

    /**
     * Delete an offline playlist and clean up storage.
     */
    suspend fun removeOffline(playlistId: String) {
        workManager.cancelUniqueWork("download_$playlistId")
        dao.deletePlaylist(playlistId)
    }

    /**
     * Schedule periodic background sync worker to check for remote playlist changes.
     */
    fun schedulePeriodicSync() {
        val syncRequest = PeriodicWorkRequestBuilder<PlaylistSyncWorker>(12, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            "playlist_periodic_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest,
        )
    }
}
