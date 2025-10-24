package com.facealbum.domain.local.dao

import androidx.room.*
import com.facealbum.domain.local.entity.WatchFolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchFolderDao {
    @Query("SELECT * FROM watch_folders ORDER BY createdAt DESC")
    fun getAllWatchFolders(): Flow<List<WatchFolderEntity>>
    
    @Query("SELECT * FROM watch_folders WHERE isEnabled = 1")
    suspend fun getEnabledWatchFolders(): List<WatchFolderEntity>
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWatchFolder(folder: WatchFolderEntity): Long
    
    @Update
    suspend fun updateWatchFolder(folder: WatchFolderEntity)
    
    @Query("UPDATE watch_folders SET isEnabled = :enabled WHERE id = :folderId")
    suspend fun toggleWatchFolder(folderId: Long, enabled: Boolean)
    
    @Query("DELETE FROM watch_folders WHERE id = :folderId")
    suspend fun deleteWatchFolder(folderId: Long)
    
    @Query("SELECT * FROM watch_folders WHERE path = :path LIMIT 1")
    suspend fun getWatchFolderByPath(path: String): WatchFolderEntity?
}
