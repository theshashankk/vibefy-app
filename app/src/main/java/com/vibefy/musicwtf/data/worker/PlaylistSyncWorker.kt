package com.vibefy.musicwtf.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vibefy.musicwtf.data.db.DownloadStatus
import com.vibefy.musicwtf.data.db.OfflinePlaylistDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * Background WorkManager worker triggered when network is online:
 * Checks for playlist updates against the remote API.
 * If something changed on server (songs added/removed/cover changed), it:
 *   1. Updates `updateAvailable = true` in Room DB
 *   2. Displays an Android system notification to the user: "Playlist Update Available"
 */
@HiltWorker
class PlaylistSyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val dao: OfflinePlaylistDao,
    private val okHttpClient: OkHttpClient,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val downloadedPlaylists = dao.getAllOfflinePlaylists().first()
            .filter { it.downloadStatus == DownloadStatus.DOWNLOADED }

        if (downloadedPlaylists.isEmpty()) return Result.success()

        for (playlist in downloadedPlaylists) {
            try {
                // Fetch remote metadata/version check
                val request = Request.Builder()
                    .url("https://music-wtf.vercel.app/api/jukebox/${playlist.id}/version")
                    .build()

                val response = okHttpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val jsonStr = response.body?.string() ?: continue
                    val jsonObj = JSONObject(jsonStr)
                    val remoteHash = jsonObj.optString("versionHash", "")

                    if (remoteHash.isNotEmpty() && remoteHash != playlist.versionHash) {
                        // Mark update available in DB
                        dao.markUpdateAvailable(playlist.id, hasUpdate = true, newHash = remoteHash)

                        // Notify user
                        sendUpdateNotification(playlist.title, playlist.id)
                    }
                }
            } catch (e: Exception) {
                // Suppress network check errors gracefully when offline
            }
        }

        return Result.success()
    }

    private fun sendUpdateNotification(playlistTitle: String, playlistId: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "musicwtf_playlist_updates"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Playlist Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications when downloaded playlists have updates"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("Playlist Update Available")
            .setContentText("\"$playlistTitle\" has new songs or updates available.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(playlistId.hashCode(), notification)
    }
}
