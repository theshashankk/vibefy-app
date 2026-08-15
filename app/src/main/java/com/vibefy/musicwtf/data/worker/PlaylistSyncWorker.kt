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
import com.vibefy.musicwtf.data.model.PlaylistEntry
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Background WorkManager worker triggered when network is online:
 * Fetches static https://music-wtf.vercel.app/playlists.json and checks for playlist updates.
 * If something changed on server (version hash or tracks updated), it:
 *   1. Updates `updateAvailable = true` in Room DB
 *   2. Displays an Android system notification: "Playlist Update Available"
 */
@HiltWorker
class PlaylistSyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val dao: OfflinePlaylistDao,
    private val okHttpClient: OkHttpClient,
) : CoroutineWorker(context, params) {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun doWork(): Result {
        val downloadedPlaylists = dao.getAllOfflinePlaylists().first()
            .filter { it.downloadStatus == DownloadStatus.DOWNLOADED }

        if (downloadedPlaylists.isEmpty()) return Result.success()

        try {
            // Fetch public static playlists.json from Vercel
            val request = Request.Builder()
                .url("${com.vibefy.musicwtf.BuildConfig.BASE_URL}/playlists.json")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonStr = response.body?.string() ?: return Result.success()
                val remotePlaylists = json.decodeFromString<List<PlaylistEntry>>(jsonStr)

                for (localPlaylist in downloadedPlaylists) {
                    val remoteMatch = remotePlaylists.find { it.id == localPlaylist.id }
                    if (remoteMatch != null) {
                        // Compare version hash / metadata
                        val remoteHash = remoteMatch.accentColor + "_" + remoteMatch.title
                        if (localPlaylist.versionHash.isNotEmpty() && remoteHash != localPlaylist.versionHash) {
                            dao.markUpdateAvailable(localPlaylist.id, hasUpdate = true, newHash = remoteHash)
                            sendUpdateNotification(localPlaylist.title, localPlaylist.id)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Suppress network check errors gracefully when offline
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
            .setContentText("\"$playlistTitle\" has updates available.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(playlistId.hashCode(), notification)
    }
}
