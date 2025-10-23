package com.facealbum.data.local.dao

import androidx.room.*
import com.facealbum.data.local.entity.PhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos ORDER BY dateAdded DESC")
    fun getAllPhotos(): Flow<List<PhotoEntity>>
    
    @Query("SELECT * FROM photos WHERE id = :photoId")
    suspend fun getPhotoById(photoId: Long): PhotoEntity?
    
    @Query("SELECT * FROM photos WHERE mediaStoreId = :mediaStoreId LIMIT 1")
    suspend fun getPhotoByMediaStoreId(mediaStoreId: Long): PhotoEntity?
    
    @Query("SELECT * FROM photos WHERE contentHash = :contentHash LIMIT 1")
    suspend fun getPhotoByContentHash(contentHash: String): PhotoEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity): Long
    
    @Update
    suspend fun updatePhoto(photo: PhotoEntity)
    
    @Delete
    suspend fun deletePhoto(photo: PhotoEntity)
    
    @Query("DELETE FROM photos WHERE id = :photoId")
    suspend fun deletePhotoById(photoId: Long)
    
    @Query("SELECT * FROM photos WHERE hasFaces = 0 AND processedAt IS NULL ORDER BY dateAdded DESC")
    suspend fun getPhotosWithoutFaces(): List<PhotoEntity>
    
    @Query("UPDATE photos SET processedAt = :timestamp WHERE id = :photoId")
    suspend fun markPhotoAsProcessed(photoId: Long, timestamp: Long)
    
    @Query("SELECT * FROM photos WHERE dateModified > :timestamp")
    suspend fun getPhotosModifiedAfter(timestamp: Long): List<PhotoEntity>
}
