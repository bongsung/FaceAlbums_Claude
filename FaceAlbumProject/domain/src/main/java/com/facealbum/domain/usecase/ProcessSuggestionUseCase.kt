package com.facealbum.domain.usecase

import com.facealbum.common.Result
import com.facealbum.domain.repository.SuggestionRepository
import javax.inject.Inject

/**
 * Use case to accept or reject a suggestion
 */
class ProcessSuggestionUseCase @Inject constructor(
    private val suggestionRepository: SuggestionRepository
) {
    suspend fun accept(suggestionId: Long): Result<Unit> {
        return suggestionRepository.acceptSuggestion(suggestionId)
    }
    
    suspend fun reject(suggestionId: Long): Result<Unit> {
        return suggestionRepository.rejectSuggestion(suggestionId)
    }
}
