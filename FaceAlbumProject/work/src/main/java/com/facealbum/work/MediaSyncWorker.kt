package com.facealbum.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.facealbum.domain.repository.PhotoRepository
import com.facealbum.media.MediaStoreScanner
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker to sync MediaStore changes to local database
 */
@HiltWorker
class MediaSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val mediaStoreScanner: MediaStoreScanner,
    private val photoRepository: PhotoRepository
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting media sync")
        
        return try {
            val mediaStoreId = inputData.getLong(KEY_MEDIA_STORE_ID, -1L)
            
            if (mediaStoreId != -1L) {
                // Sync specific item
                syncSingleItem(mediaStoreId)
            } else {
                // Full scan
                syncAllItems()
            }
            
            Log.d(TAG, "Media sync completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Media sync failed", e)
            if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
    
    private suspend fun syncSingleItem(mediaStoreId: Long) {
        val photo = mediaStoreScanner.getPhotoDetails(mediaStoreId)
        
        if (photo != null) {
            // Check if photo already exists (by MediaStore ID or content hash)
            val existingPhoto = photoRepository.getPhotoByMediaStoreIdOrHash(
                mediaStoreId = photo.mediaStoreId,
                contentHash = photo.contentHash
            )
            
            when {
                existingPhoto.isSuccess && existingPhoto.getOrNull() != null -> {
                    // Photo exists, update if needed
                    val existing = existingPhoto.getOrNull()!!
                    if (existing.dateModified != photo.dateModified) {
                        photoRepository.upsertPhoto(photo.copy(id = existing.id))
                        Log.d(TAG, "Updated photo: ${photo.displayName}")
                    }
                }
                else -> {
                    // New photo, insert
                    photoRepository.upsertPhoto(photo)
                    Log.d(TAG, "Inserted new photo: ${photo.displayName}")
                    
                    // Queue face detection for this photo
                    FaceDetectionWorker.enqueue(applicationContext, photo.mediaStoreId)
                }
            }
        }
    }
    
    private suspend fun syncAllItems() {
        val photos = mediaStoreScanner.scanAllImages()
        var newCount = 0
        var updatedCount = 0
        
        for (photo in photos) {
            val existingPhoto = photoRepository.getPhotoByMediaStoreIdOrHash(
                mediaStoreId = photo.mediaStoreId,
                contentHash = photo.contentHash
            )
            
            when {
                existingPhoto.isSuccess && existingPhoto.getOrNull() != null -> {
                    val existing = existingPhoto.getOrNull()!!
                    if (existing.dateModified != photo.dateModified) {
                        photoRepository.upsertPhoto(photo.copy(id = existing.id))
                        updatedCount++
                    }
                }
                else -> {
                    photoRepository.upsertPhoto(photo)
                    newCount++
                    
                    // Queue face detection
                    FaceDetectionWorker.enqueue(applicationContext, photo.mediaStoreId)
                }
            }
        }
        
        Log.d(TAG, "Full sync: $newCount new, $updatedCount updated")
    }
    
    companion object {
        private const val TAG = "MediaSyncWorker"
        private const val MAX_RETRY_ATTEMPTS = 3
        const val KEY_MEDIA_STORE_ID = "media_store_id"
    }
}
