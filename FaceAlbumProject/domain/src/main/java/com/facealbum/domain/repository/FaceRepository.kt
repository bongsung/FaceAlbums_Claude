package com.facealbum.domain.repository

import com.facealbum.common.Result
import com.facealbum.domain.model.Face

/**
 * Repository interface for Face operations
 */
interface FaceRepository {
    /**
     * Insert faces for a photo
     */
    suspend fun insertFaces(faces: List<Face>): Result<List<Long>>
    
    /**
     * Get face by ID
     */
    suspend fun getFaceById(faceId: Long): Result<Face>
    
    /**
     * Get all faces without assigned person
     */
    suspend fun getUnassignedFaces(): Result<List<Face>>
    
    /**
     * Get faces for a specific person
     */
    suspend fun getFacesForPerson(personId: Long): Result<List<Face>>
    
    /**
     * Find similar faces based on embedding
     */
    suspend fun findSimilarFaces(
        embedding: FloatArray,
        threshold: Float,
        limit: Int = 10
    ): Result<List<Pair<Face, Float>>> // Face with similarity score
    
    /**
     * Delete faces for a photo
     */
    suspend fun deleteFacesForPhoto(photoId: Long): Result<Unit>
}
