package com.vibefy.musicwtf.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vibefy.musicwtf.data.db.DownloadStatus
import com.vibefy.musicwtf.data.db.OfflinePlaylistDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

/**
 * Background WorkManager worker that downloads the entire playlist:
 * - Cover / background images
 * - Audio files for all tracks
 * Saves locally to app internal storage (`filesDir/offline/{playlistId}/`)
 * Updates progress in Room DB so UI displays 0% -> 100% progress.
 */
@HiltWorker
class PlaylistDownloadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val dao: OfflinePlaylistDao,
    private val okHttpClient: OkHttpClient,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val playlistId = inputData.getString(KEY_PLAYLIST_ID) ?: return Result.failure()

        val playlist = dao.getPlaylistById(playlistId) ?: return Result.failure()
        val tracks = dao.getTracksForPlaylistSync(playlistId)

        if (tracks.isEmpty()) return Result.failure()

        dao.updateDownloadProgress(playlistId, DownloadStatus.DOWNLOADING, 0)

        val outputDir = File(context.filesDir, "offline/$playlistId").apply { mkdirs() }

        try {
            // 1. Download main cover / background image
            val coverFile = File(outputDir, "cover.jpg")
            downloadFile(playlist.coverUrl, coverFile)

            var downloadedCount = 0
            val totalItems = tracks.size

            // 2. Download each track audio + cover
            for (track in tracks) {
                val audioFile = File(outputDir, "track_${track.id}.mp3")
                val trackCoverFile = File(outputDir, "cover_${track.id}.jpg")

                downloadFile(track.remoteAudioUrl, audioFile)
                downloadFile(track.remoteCoverUrl, trackCoverFile)

                dao.updateTrackLocalPaths(
                    trackId = track.id,
                    audioPath = audioFile.absolutePath,
                    coverPath = trackCoverFile.absolutePath,
                )

                downloadedCount++
                val progress = ((downloadedCount.toFloat() / totalItems) * 100).toInt()
                dao.updateDownloadProgress(playlistId, DownloadStatus.DOWNLOADING, progress)
            }

            // Mark completion
            dao.updateDownloadProgress(playlistId, DownloadStatus.DOWNLOADED, 100)
            dao.markUpdateAvailable(playlistId, hasUpdate = false, newHash = playlist.versionHash)

            return Result.success()

        } catch (e: Exception) {
            e.printStackTrace()
            dao.updateDownloadProgress(playlistId, DownloadStatus.FAILED, 0)
            return Result.retry()
        }
    }

    private fun downloadFile(url: String, targetFile: File) {
        if (targetFile.exists() && targetFile.length() > 0) return // Skip if already downloaded
        val request = Request.Builder().url(url).build()
        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) throw Exception("Failed to download $url")

        response.body?.byteStream()?.use { input ->
            FileOutputStream(targetFile).use { output ->
                input.copyTo(output)
            }
        }
    }

    companion object {
        const val KEY_PLAYLIST_ID = "key_playlist_id"
    }
}
