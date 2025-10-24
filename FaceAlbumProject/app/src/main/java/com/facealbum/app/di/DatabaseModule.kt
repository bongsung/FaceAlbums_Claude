package com.facealbum.app.di

import android.content.Context
import androidx.room.Room
import com.facealbum.common.Constants
import com.facealbum.domain.local.FaceAlbumDatabase
import com.facealbum.domain.local.dao.*
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
    fun provideFaceAlbumDatabase(
        @ApplicationContext context: Context
    ): FaceAlbumDatabase {
        return Room.databaseBuilder(
            context,
            FaceAlbumDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    fun providePersonDao(database: FaceAlbumDatabase): PersonDao {
        return database.personDao()
    }
    
    @Provides
    fun providePhotoDao(database: FaceAlbumDatabase): PhotoDao {
        return database.photoDao()
    }
    
    @Provides
    fun provideFaceDao(database: FaceAlbumDatabase): FaceDao {
        return database.faceDao()
    }
    
    @Provides
    fun provideSuggestionDao(database: FaceAlbumDatabase): SuggestionDao {
        return database.suggestionDao()
    }
    
    @Provides
    fun provideWatchFolderDao(database: FaceAlbumDatabase): WatchFolderDao {
        return database.watchFolderDao()
    }
}
