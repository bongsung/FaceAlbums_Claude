package com.facealbum.data.repository

import com.facealbum.common.Result
import com.facealbum.data.local.dao.PersonDao
import com.facealbum.data.local.dao.SuggestionDao
import com.facealbum.data.local.entity.LinkPersonPhotoEntity
import com.facealbum.data.local.entity.PendingSuggestionEntity
import com.facealbum.data.local.entity.toDomain
import com.facealbum.domain.model.PendingSuggestion
import com.facealbum.domain.model.SuggestionStatus
import com.facealbum.domain.repository.SuggestionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SuggestionRepositoryImpl @Inject constructor(
    private val suggestionDao: SuggestionDao,
    private val personDao: PersonDao
) : SuggestionRepository {
    
    override fun getPendingSuggestions(): Flow<List<PendingSuggestion>> {
        return suggestionDao.getPendingSuggestions().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun createSuggestion(
        faceId: Long,
        suggestedPersonId: Long?,
        similarityScore: Float
    ): Result<Long> {
        return try {
            val entity = PendingSuggestionEntity(
                faceId = faceId,
                suggestedPersonId = suggestedPersonId,
                similarityScore = similarityScore,
                status = SuggestionStatus.PENDING.name
            )
            val id = suggestionDao.insertSuggestion(entity)
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun acceptSuggestion(suggestionId: Long): Result<Unit> {
        return try {
            val suggestion = suggestionDao.getSuggestionById(suggestionId)
            if (suggestion != null && suggestion.suggestedPersonId != null) {
                // TODO: Link face to person
                // This requires getting the photo ID from the face
                // For now, just update the status
                suggestionDao.updateSuggestionStatus(suggestionId, SuggestionStatus.ACCEPTED.name)
                Result.Success(Unit)
            } else {
                Result.Error(IllegalStateException("Invalid suggestion"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun rejectSuggestion(suggestionId: Long): Result<Unit> {
        return try {
            suggestionDao.updateSuggestionStatus(suggestionId, SuggestionStatus.REJECTED.name)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getSuggestionsForFace(faceId: Long): Result<List<PendingSuggestion>> {
        return try {
            val entities = suggestionDao.getSuggestionsForFace(faceId)
            Result.Success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun updateSuggestionStatus(
        suggestionId: Long,
        status: SuggestionStatus
    ): Result<Unit> {
        return try {
            suggestionDao.updateSuggestionStatus(suggestionId, status.name)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
