package com.facealbum.data.repository

import com.facealbum.common.Result
import com.facealbum.data.local.dao.FaceDao
import com.facealbum.data.local.entity.toDomain
import com.facealbum.data.local.entity.toEntity
import com.facealbum.domain.model.Face
import com.facealbum.domain.repository.FaceRepository
import com.facealbum.ml.FaceEmbeddingGenerator
import javax.inject.Inject
import kotlin.math.sqrt

class FaceRepositoryImpl @Inject constructor(
    private val faceDao: FaceDao,
    private val embeddingGenerator: FaceEmbeddingGenerator
) : FaceRepository {
    
    override suspend fun insertFaces(faces: List<Face>): Result<List<Long>> {
        return try {
            val entities = faces.map { it.toEntity() }
            val ids = faceDao.insertFaces(entities)
            Result.Success(ids)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getFaceById(faceId: Long): Result<Face> {
        return try {
            val entity = faceDao.getFaceById(faceId)
            if (entity != null) {
                Result.Success(entity.toDomain())
            } else {
                Result.Error(NoSuchElementException("Face not found"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getUnassignedFaces(): Result<List<Face>> {
        return try {
            val entities = faceDao.getUnassignedFaces()
            Result.Success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getFacesForPerson(personId: Long): Result<List<Face>> {
        return try {
            val entities = faceDao.getFacesForPerson(personId)
            Result.Success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun findSimilarFaces(
        embedding: FloatArray,
        threshold: Float,
        limit: Int
    ): Result<List<Pair<Face, Float>>> {
        return try {
            // Get all faces and compute similarities
            // Note: This is a simple implementation. For better performance,
            // consider using a vector database or indexing solution
            val allFaces = faceDao.getAllFaces().map { it.toDomain() }
            
            val similarities = allFaces.mapNotNull { face ->
                val similarity = embeddingGenerator.calculateSimilarity(embedding, face.embedding)
                if (similarity >= threshold) {
                    face to similarity
                } else {
                    null
                }
            }
            
            // Sort by similarity (descending) and take top results
            val topSimilar = similarities
                .sortedByDescending { it.second }
                .take(limit)
            
            Result.Success(topSimilar)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun deleteFacesForPhoto(photoId: Long): Result<Unit> {
        return try {
            faceDao.deleteFacesForPhoto(photoId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
