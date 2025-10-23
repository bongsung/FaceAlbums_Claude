package com.facealbum.work

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.facealbum.common.Constants
import com.facealbum.domain.model.Face
import com.facealbum.domain.repository.FaceRepository
import com.facealbum.domain.repository.PhotoRepository
import com.facealbum.ml.FaceDetector
import com.facealbum.ml.FaceEmbeddingGenerator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Worker to detect faces in photos and generate embeddings
 */
@HiltWorker
class FaceDetectionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val photoRepository: PhotoRepository,
    private val faceRepository: FaceRepository,
    private val faceDetector: FaceDetector,
    private val embeddingGenerator: FaceEmbeddingGenerator
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        val mediaStoreId = inputData.getLong(KEY_MEDIA_STORE_ID, -1L)
        
        if (mediaStoreId == -1L) {
            Log.e(TAG, "Invalid media store ID")
            return Result.failure()
        }
        
        Log.d(TAG, "Starting face detection for media ID: $mediaStoreId")
        
        return try {
            // Get photo from repository
            val photoResult = photoRepository.getPhotoById(mediaStoreId)
            if (!photoResult.isSuccess) {
                Log.e(TAG, "Photo not found: $mediaStoreId")
                return Result.failure()
            }
            
            val photo = photoResult.getOrNull()!!
            
            // Load bitmap
            val bitmap = applicationContext.contentResolver.openInputStream(Uri.parse(photo.uri))?.use {
                BitmapFactory.decodeStream(it)
            }
            
            if (bitmap == null) {
                Log.e(TAG, "Failed to load bitmap for: ${photo.displayName}")
                return Result.failure()
            }
            
            // Detect faces
            val detectionResult = faceDetector.detectFaces(bitmap)
            if (!detectionResult.isSuccess) {
                Log.e(TAG, "Face detection failed", detectionResult.exceptionOrNull())
                return Result.retry()
            }
            
            val detectedFaces = detectionResult.getOrNull()!!
            Log.d(TAG, "Detected ${detectedFaces.size} faces in ${photo.displayName}")
            
            if (detectedFaces.isEmpty()) {
                // Mark as processed even if no faces found
                photoRepository.markPhotoAsProcessed(photo.id)
                return Result.success()
            }
            
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
            }
            
            // Save faces to database
            if (faces.isNotEmpty()) {
                faceRepository.insertFaces(faces)
                
                // Mark photo as having faces
                photoRepository.markPhotoAsProcessed(photo.id)
                
                // Queue clustering/suggestion generation
                ClusteringWorker.enqueue(applicationContext)
            }
            
            Log.d(TAG, "Face detection completed for ${photo.displayName}: ${faces.size} faces saved")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Face detection worker failed", e)
            if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
    
    companion object {
        private const val TAG = "FaceDetectionWorker"
        private const val MAX_RETRY_ATTEMPTS = 3
        const val KEY_MEDIA_STORE_ID = "media_store_id"
        
        /**
         * Enqueue face detection work for a specific photo
         */
        fun enqueue(context: Context, mediaStoreId: Long) {
            val workRequest = OneTimeWorkRequestBuilder<FaceDetectionWorker>()
                .setInputData(
                    workDataOf(KEY_MEDIA_STORE_ID to mediaStoreId)
                )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .addTag(Constants.WORK_TAG_FACE_DETECTION)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    10,
                    TimeUnit.SECONDS
                )
                .build()
            
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}
