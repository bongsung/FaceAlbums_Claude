package com.facealbum.domain.usecase

import com.facealbum.domain.model.PendingSuggestion
import com.facealbum.domain.repository.SuggestionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get all pending suggestions
 */
class GetPendingSuggestionsUseCase @Inject constructor(
    private val suggestionRepository: SuggestionRepository
) {
    operator fun invoke(): Flow<List<PendingSuggestion>> {
        return suggestionRepository.getPendingSuggestions()
    }
}
