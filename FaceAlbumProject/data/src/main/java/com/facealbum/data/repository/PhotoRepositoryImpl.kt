package com.facealbum.data.repository

import com.facealbum.common.Result
import com.facealbum.data.local.dao.FaceDao
import com.facealbum.data.local.dao.PhotoDao
import com.facealbum.data.local.entity.toDomain
import com.facealbum.data.local.entity.toEntity
import com.facealbum.domain.model.Face
import com.facealbum.domain.model.Photo
import com.facealbum.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PhotoRepositoryImpl @Inject constructor(
    private val photoDao: PhotoDao,
    private val faceDao: FaceDao
) : PhotoRepository {
    
    override fun getAllPhotos(): Flow<List<Photo>> {
        return photoDao.getAllPhotos().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getPhotoById(photoId: Long): Result<Photo> {
        return try {
            val entity = photoDao.getPhotoById(photoId)
            if (entity != null) {
                Result.Success(entity.toDomain())
            } else {
                Result.Error(NoSuchElementException("Photo not found"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getPhotoByMediaStoreIdOrHash(
        mediaStoreId: Long,
        contentHash: String
    ): Result<Photo?> {
        return try {
            // First try by MediaStore ID (fastest)
            var entity = photoDao.getPhotoByMediaStoreId(mediaStoreId)
            
            // If not found, try by content hash (handles renames/moves)
            if (entity == null) {
                entity = photoDao.getPhotoByContentHash(contentHash)
            }
            
            Result.Success(entity?.toDomain())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun upsertPhoto(photo: Photo): Result<Long> {
        return try {
            val id = photoDao.insertPhoto(photo.toEntity())
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun deletePhoto(photoId: Long): Result<Unit> {
        return try {
            photoDao.deletePhotoById(photoId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getFacesForPhoto(photoId: Long): Result<List<Face>> {
        return try {
            val entities = faceDao.getFacesForPhoto(photoId)
            Result.Success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getPhotosWithoutFaces(): Result<List<Photo>> {
        return try {
            val entities = photoDao.getPhotosWithoutFaces()
            Result.Success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun markPhotoAsProcessed(photoId: Long): Result<Unit> {
        return try {
            photoDao.markPhotoAsProcessed(photoId, System.currentTimeMillis())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
