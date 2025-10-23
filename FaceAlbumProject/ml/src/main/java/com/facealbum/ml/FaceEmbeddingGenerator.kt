package com.facealbum.ml

import android.graphics.Bitmap
import com.facealbum.common.Result

/**
 * Interface for generating face embeddings using TensorFlow Lite
 */
interface FaceEmbeddingGenerator {
    /**
     * Generate embedding vector for a face image
     * @param faceBitmap Cropped face image
     * @return FloatArray representing the face embedding
     */
    suspend fun generateEmbedding(faceBitmap: Bitmap): Result<FloatArray>
    
    /**
     * Calculate cosine similarity between two embeddings
     * @return Similarity score between 0 and 1 (1 = identical)
     */
    fun calculateSimilarity(embedding1: FloatArray, embedding2: FloatArray): Float
    
    /**
     * Release resources
     */
    fun close()
}
