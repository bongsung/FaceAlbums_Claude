package com.facealbum.domain.repository

import com.facealbum.common.Result
import com.facealbum.domain.model.Face
import com.facealbum.domain.model.Photo
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Photo operations
 */
interface PhotoRepository {
    /**
     * Get all photos as Flow
     */
    fun getAllPhotos(): Flow<List<Photo>>
    
    /**
     * Get photo by ID
     */
    suspend fun getPhotoById(photoId: Long): Result<Photo>
    
    /**
     * Get photo by MediaStore ID and hash (for tracking renames/moves)
     */
    suspend fun getPhotoByMediaStoreIdOrHash(
        mediaStoreId: Long,
        contentHash: String
    ): Result<Photo?>
    
    /**
     * Insert or update photo
     */
    suspend fun upsertPhoto(photo: Photo): Result<Long>
    
    /**
     * Delete photo
     */
    suspend fun deletePhoto(photoId: Long): Result<Unit>
    
    /**
     * Get faces for a photo
     */
    suspend fun getFacesForPhoto(photoId: Long): Result<List<Face>>
    
    /**
     * Get photos without faces detected
     */
    suspend fun getPhotosWithoutFaces(): Result<List<Photo>>
    
    /**
     * Mark photo as processed
     */
    suspend fun markPhotoAsProcessed(photoId: Long): Result<Unit>
}
