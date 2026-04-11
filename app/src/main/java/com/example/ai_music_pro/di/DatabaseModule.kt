package com.example.ai_music_pro.di

import android.content.Context
import androidx.room.Room
import com.example.ai_music_pro.data.local.AppDatabase
import com.example.ai_music_pro.data.local.dao.MusicDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ai_music_pro_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideMusicDao(database: AppDatabase): MusicDao {
        return database.musicDao()
    }
}
