package com.facealbum.work

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.facealbum.domain.model.Face
import com.facealbum.domain.repository.FaceRepository
import com.facealbum.domain.repository.PhotoRepository
import com.facealbum.domain.repository.SettingsRepository
import com.facealbum.media.FolderAwareMediaScanner
import com.facealbum.ml.FaceDetector
import com.facealbum.ml.FaceEmbeddingGenerator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Worker to scan specific folders for photos and detect faces
 */
@HiltWorker
class FolderScanWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val folderScanner: FolderAwareMediaScanner,
    private val photoRepository: PhotoRepository,
    private val faceRepository: FaceRepository,
    private val settingsRepository: SettingsRepository,
    private val faceDetector: FaceDetector,
    private val embeddingGenerator: FaceEmbeddingGenerator
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        val folderId = inputData.getLong(KEY_FOLDER_ID, -1L)
        val folderPath = inputData.getString(KEY_FOLDER_PATH)
        
        if (folderId == -1L && folderPath == null) {
            Log.e(TAG, "No folder specified for scanning")
            return Result.failure()
        }
        
        Log.d(TAG, "Starting folder scan: $folderPath")
        setProgress(workDataOf("status" to "Scanning folder..."))
        
        return try {
            // Get folder path if only ID provided
            val pathToScan = folderPath ?: getFolderPath(folderId)
            if (pathToScan == null) {
                Log.e(TAG, "Folder not found: $folderId")
                return Result.failure()
            }
            
            // Scan folder for photos
            val photos = folderScanner.scanFolder(pathToScan)
            Log.d(TAG, "Found ${photos.size} photos in $pathToScan")
            
            var processedCount = 0
            var newFacesCount = 0
            
            for (photo in photos) {
                setProgress(workDataOf(
                    "status" to "Processing photos...",
                    "progress" to processedCount,
                    "total" to photos.size
                ))
                
                // Check if photo already exists
                val existingPhoto = photoRepository.getPhotoByMediaStoreIdOrHash(
                    mediaStoreId = photo.mediaStoreId,
                    contentHash = photo.contentHash
                )
                
                val photoId = when {
                    existingPhoto.isSuccess && existingPhoto.getOrNull() != null -> {
                        // Photo exists, check if already processed
                        val existing = existingPhoto.getOrNull()!!
                        if (existing.processedAt != null) {
                            // Already processed, skip
                            processedCount++
                            continue
                        }
                        existing.id
                    }
                    else -> {
                        // New photo, insert it
                        val insertResult = photoRepository.upsertPhoto(photo)
                        insertResult.getOrNull() ?: continue
                    }
                }
                
                // Detect faces in the photo
                val facesDetected = detectAndSaveFaces(photo.copy(id = photoId))
                newFacesCount += facesDetected
                
                // Mark photo as processed
                photoRepository.markPhotoAsProcessed(photoId)
                processedCount++
            }
            
            setProgress(workDataOf(
                "status" to "Complete",
                "processed" to processedCount,
                "newFaces" to newFacesCount
            ))
            
            Log.d(TAG, "Folder scan complete: $processedCount photos, $newFacesCount new faces")
            
            // If new faces were found, trigger clustering
            if (newFacesCount > 0) {
                ClusteringWorker.enqueue(applicationContext)
            }
            
            Result.success(workDataOf(
                "processedPhotos" to processedCount,
                "newFaces" to newFacesCount
            ))
        } catch (e: Exception) {
            Log.e(TAG, "Folder scan failed", e)
            if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
    
    private suspend fun getFolderPath(folderId: Long): String? {
        // Get folder path from settings repository
        val folders = settingsRepository.getWatchFolders()
        // This would need to be a suspend function to get the current value
        return null // Placeholder
    }
    
    private suspend fun detectAndSaveFaces(photo: com.facealbum.domain.model.Photo): Int = withContext(Dispatchers.IO) {
        try {
            // Load bitmap
            val bitmap = applicationContext.contentResolver.openInputStream(Uri.parse(photo.uri))?.use {
                BitmapFactory.decodeStream(it)
            } ?: return@withContext 0
            
            // Detect faces
            val detectionResult = faceDetector.detectFaces(bitmap)
            if (!detectionResult.isSuccess) {
                return@withContext 0
            }
            
            val detectedFaces = detectionResult.getOrNull() ?: return@withContext 0
            
            // Generate embeddings for each face
            val faces = mutableListOf<Face>()
            for (detectedFace in detectedFaces) {
                val faceBitmap = detectedFace.faceBitmap ?: continue
                
                val embeddingResult = embeddingGenerator.generateEmbedding(faceBitmap)
                if (embeddingResult.isSuccess) {
                    val embedding = embeddingResult.getOrNull()!!
                    
                    faces.add(
                        Face(
                            photoId = photo.id,
                            boundingBox = detectedFace.boundingBox,
                            embedding = embedding,
                            confidence = detectedFace.confidence
                        )
                    )
                }
                
                // Clean up bitmap
                faceBitmap.recycle()
            }
            
            // Save faces to database
            if (faces.isNotEmpty()) {
                faceRepository.insertFaces(faces)
            }
            
            faces.size
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting faces in ${photo.displayName}", e)
            0
        }
    }
    
    companion object {
        private const val TAG = "FolderScanWorker"
        private const val MAX_RETRY_ATTEMPTS = 3
        const val KEY_FOLDER_ID = "folder_id"
        const val KEY_FOLDER_PATH = "folder_path"
        const val WORK_NAME = "folder_scan"
        
        /**
         * Enqueue folder scanning work
         */
        fun enqueue(context: Context, folderId: Long? = null, folderPath: String? = null) {
            val inputData = workDataOf(
                KEY_FOLDER_ID to (folderId ?: -1L),
                KEY_FOLDER_PATH to folderPath
            )
            
            val workRequest = OneTimeWorkRequestBuilder<FolderScanWorker>()
                .setInputData(inputData)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .addTag("folder_scan")
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30,
                    TimeUnit.SECONDS
                )
                .build()
            
            // Use unique work to avoid duplicate scans of same folder
            val uniqueName = if (folderId != null) {
                "${WORK_NAME}_$folderId"
            } else {
                "${WORK_NAME}_${folderPath?.hashCode()}"
            }
            
            WorkManager.getInstance(context).enqueueUniqueWork(
                uniqueName,
                ExistingWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
