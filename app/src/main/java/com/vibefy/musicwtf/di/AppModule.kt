package com.vibefy.musicwtf.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.vibefy.musicwtf.data.db.AppDatabase
import com.vibefy.musicwtf.data.db.OfflinePlaylistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "musicwtf_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideOfflinePlaylistDao(db: AppDatabase): OfflinePlaylistDao {
        return db.offlinePlaylistDao()
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
}
