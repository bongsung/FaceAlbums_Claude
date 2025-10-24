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
        // KEY 이름 변경: PHOTO_ID (DB ID)
        val photoId = inputData.getLong(KEY_PHOTO_ID, -1L)

        if (photoId == -1L) {
            Log.e(TAG, "Invalid photo ID")
            return Result.failure()
        }

        Log.d(TAG, "Starting face detection for photo ID: $photoId")

        return try {
            // Get photo from repository
            val photoResult = photoRepository.getPhotoById(photoId)
            if (!photoResult.isSuccess) {
                Log.e(TAG, "Photo not found: $photoId")
                return Result.failure()
            }

            val photo = photoResult.getOrNull()!!
            Log.d(TAG, "Processing photo: ${photo.displayName}")

            // Load bitmap
            val bitmap = applicationContext.contentResolver.openInputStream(Uri.parse(photo.uri))?.use {
                BitmapFactory.decodeStream(it)
            }

            if (bitmap == null) {
                Log.e(TAG, "Failed to load bitmap for: ${photo.displayName}")
                return Result.failure()
            }

            Log.d(TAG, "Bitmap loaded: ${bitmap.width}x${bitmap.height}")

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
                Log.d(TAG, "No faces found, marked as processed")
                return Result.success()
            }

            // Generate embeddings for each face
            val faces = mutableListOf<Face>()
            for ((index, detectedFace) in detectedFaces.withIndex()) {
                val faceBitmap = detectedFace.faceBitmap

                if (faceBitmap == null) {
                    Log.w(TAG, "Face $index has no bitmap, skipping")
                    continue
                }

                Log.d(TAG, "Generating embedding for face $index")
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
                    Log.d(TAG, "Embedding generated for face $index")
                } else {
                    Log.e(TAG, "Failed to generate embedding for face $index",
                        embeddingResult.exceptionOrNull())
                }
            }

            // Save faces to database
            if (faces.isNotEmpty()) {
                val insertResult = faceRepository.insertFaces(faces)
                if (insertResult.isSuccess) {
                    Log.d(TAG, "Saved ${faces.size} faces to database")

                    // Mark photo as having faces
                    photoRepository.markPhotoAsProcessed(photo.id)

                    // Queue clustering/suggestion generation
                    ClusteringWorker.enqueue(applicationContext)
                    Log.d(TAG, "Queued clustering worker")
                } else {
                    Log.e(TAG, "Failed to save faces", insertResult.exceptionOrNull())
                }
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
        const val KEY_PHOTO_ID = "photo_id"  // ← 이름 변경!

        /**
         * Enqueue face detection work for a specific photo
         * @param photoId Database photo ID (not mediaStoreId!)
         */
        fun enqueue(context: Context, photoId: Long) {
            Log.d(TAG, "Enqueueing face detection for photo ID: $photoId")

            val workRequest = OneTimeWorkRequestBuilder<FaceDetectionWorker>()
                .setInputData(
                    workDataOf(KEY_PHOTO_ID to photoId)
                )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(false)  // 즉시 실행
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