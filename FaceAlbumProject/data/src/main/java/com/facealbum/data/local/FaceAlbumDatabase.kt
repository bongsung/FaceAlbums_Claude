package com.facealbum.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.facealbum.data.local.dao.*
import com.facealbum.data.local.entity.*

@Database(
    entities = [
        PersonEntity::class,
        PhotoEntity::class,
        FaceEntity::class,
        LinkPersonPhotoEntity::class,
        PendingSuggestionEntity::class,
        WatchFolderEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class FaceAlbumDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun photoDao(): PhotoDao
    abstract fun faceDao(): FaceDao
    abstract fun suggestionDao(): SuggestionDao
    abstract fun watchFolderDao(): WatchFolderDao
}
