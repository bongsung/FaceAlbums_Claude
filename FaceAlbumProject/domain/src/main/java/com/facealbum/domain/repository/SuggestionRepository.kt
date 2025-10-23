package com.facealbum.domain.repository

import com.facealbum.common.Result
import com.facealbum.domain.model.PendingSuggestion
import com.facealbum.domain.model.SuggestionStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Suggestion operations
 */
interface SuggestionRepository {
    /**
     * Get all pending suggestions as Flow
     */
    fun getPendingSuggestions(): Flow<List<PendingSuggestion>>
    
    /**
     * Create a new suggestion
     */
    suspend fun createSuggestion(
        faceId: Long,
        suggestedPersonId: Long?,
        similarityScore: Float
    ): Result<Long>
    
    /**
     * Accept a suggestion (link face to person)
     */
    suspend fun acceptSuggestion(suggestionId: Long): Result<Unit>
    
    /**
     * Reject a suggestion
     */
    suspend fun rejectSuggestion(suggestionId: Long): Result<Unit>
    
    /**
     * Get suggestions for a specific face
     */
    suspend fun getSuggestionsForFace(faceId: Long): Result<List<PendingSuggestion>>
    
    /**
     * Update suggestion status
     */
    suspend fun updateSuggestionStatus(
        suggestionId: Long,
        status: SuggestionStatus
    ): Result<Unit>
}
