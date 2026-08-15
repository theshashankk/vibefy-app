package com.vibefy.musicwtf.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Status of an offline playlist download */
enum class DownloadStatus {
    NOT_DOWNLOADED,
    DOWNLOADING,
    DOWNLOADED,
    FAILED,
}

@Entity(tableName = "offline_playlists")
data class OfflinePlaylistEntity(
    @PrimaryKey val id: String,
    val slug: String,
    val title: String,
    val description: String,
    val coverUrl: String,
    val localCoverPath: String? = null,
    val accentColor: String = "#f59e0b",
    val versionHash: String = "",           // Remote hash used to check for online updates
    val downloadStatus: DownloadStatus = DownloadStatus.NOT_DOWNLOADED,
    val downloadProgress: Int = 0,          // 0 to 100 percentage
    val downloadedAt: Long = 0L,            // Timestamp of completion
    val updateAvailable: Boolean = false,   // Set to true when online check finds remote change
    val remoteVersionHash: String = "",     // Latest remote version found during sync
)

@Entity(
    tableName = "offline_tracks",
    foreignKeys = [
        ForeignKey(
            entity = OfflinePlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("playlistId")]
)
data class OfflineTrackEntity(
    @PrimaryKey val id: String,
    val playlistId: String,
    val title: String,
    val artist: String,
    val durationSec: Int,
    val remoteAudioUrl: String,
    val remoteCoverUrl: String,
    val localAudioPath: String? = null,
    val localCoverPath: String? = null,
    val position: Int,
    val isDownloaded: Boolean = false,
)
