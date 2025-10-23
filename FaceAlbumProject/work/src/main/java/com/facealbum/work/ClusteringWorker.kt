package com.facealbum.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.facealbum.common.Constants
import com.facealbum.domain.repository.FaceRepository
import com.facealbum.domain.repository.SuggestionRepository
import com.facealbum.ml.FaceEmbeddingGenerator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Worker to cluster unassigned faces and generate suggestions
 */
@HiltWorker
class ClusteringWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val faceRepository: FaceRepository,
    private val suggestionRepository: SuggestionRepository,
    private val embeddingGenerator: FaceEmbeddingGenerator
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting face clustering")
        
        return try {
            // Get all unassigned faces
            val unassignedResult = faceRepository.getUnassignedFaces()
            if (!unassignedResult.isSuccess) {
                Log.e(TAG, "Failed to get unassigned faces")
                return Result.failure()
            }
            
            val unassignedFaces = unassignedResult.getOrNull()!!
            if (unassignedFaces.isEmpty()) {
                Log.d(TAG, "No unassigned faces to cluster")
                return Result.success()
            }
            
            Log.d(TAG, "Clustering ${unassignedFaces.size} unassigned faces")
            
            var suggestionsCreated = 0
            
            // For each unassigned face, find similar faces
            for (face in unassignedFaces) {
                val similarFacesResult = faceRepository.findSimilarFaces(
                    embedding = face.embedding,
                    threshold = Constants.SIMILARITY_THRESHOLD,
                    limit = 5
                )
                
                if (similarFacesResult.isSuccess) {
                    val similarFaces = similarFacesResult.getOrNull()!!
                    
                    if (similarFaces.isNotEmpty()) {
                        // Get the most similar face and its person
                        val (mostSimilar, similarity) = similarFaces.first()
                        
                        // TODO: Get person ID for the most similar face
                        // For now, create suggestion with null person (new person)
                        suggestionRepository.createSuggestion(
                            faceId = face.id,
                            suggestedPersonId = null,
                            similarityScore = similarity
                        )
                        
                        suggestionsCreated++
                    }
                }
            }
            
            Log.d(TAG, "Clustering completed: $suggestionsCreated suggestions created")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Clustering worker failed", e)
            if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
    
    companion object {
        private const val TAG = "ClusteringWorker"
        private const val MAX_RETRY_ATTEMPTS = 3
        
        /**
         * Enqueue clustering work
         */
        fun enqueue(context: Context) {
            val workRequest = OneTimeWorkRequestBuilder<ClusteringWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30,
                    TimeUnit.SECONDS
                )
                .build()
            
            // Use unique work to avoid duplicate clustering
            WorkManager.getInstance(context).enqueueUniqueWork(
                "clustering_work",
                ExistingWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
