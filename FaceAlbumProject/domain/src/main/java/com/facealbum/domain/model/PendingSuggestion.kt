package com.facealbum.domain.model

/**
 * Represents a pending suggestion for user to accept/reject
 */
data class PendingSuggestion(
    val id: Long = 0,
    val faceId: Long,
    val suggestedPersonId: Long?,
    val similarityScore: Float,
    val status: SuggestionStatus = SuggestionStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)

enum class SuggestionStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}
