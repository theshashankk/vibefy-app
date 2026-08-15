package com.vibefy.musicwtf.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun fromDownloadStatus(status: DownloadStatus): String = status.name

    @TypeConverter
    fun toDownloadStatus(value: String): DownloadStatus =
        try { DownloadStatus.valueOf(value) } catch (e: Exception) { DownloadStatus.NOT_DOWNLOADED }
}

@Database(
    entities = [OfflinePlaylistEntity::class, OfflineTrackEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun offlinePlaylistDao(): OfflinePlaylistDao
}
