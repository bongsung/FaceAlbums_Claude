package com.facealbum.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.facealbum.domain.model.PendingSuggestion
import com.facealbum.domain.model.SuggestionStatus

@Entity(
    tableName = "pending_suggestions",
    foreignKeys = [
        ForeignKey(
            entity = FaceEntity::class,
            parentColumns = ["id"],
            childColumns = ["faceId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["suggestedPersonId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["faceId"]),
        Index(value = ["suggestedPersonId"]),
        Index(value = ["status"])
    ]
)
data class PendingSuggestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val faceId: Long,
    val suggestedPersonId: Long?,
    val similarityScore: Float,
    val status: String = "PENDING", // Store enum as string
    val createdAt: Long = System.currentTimeMillis()
)

fun PendingSuggestionEntity.toDomain(): PendingSuggestion {
    return PendingSuggestion(
        id = id,
        faceId = faceId,
        suggestedPersonId = suggestedPersonId,
        similarityScore = similarityScore,
        status = SuggestionStatus.valueOf(status),
        createdAt = createdAt
    )
}

fun PendingSuggestion.toEntity(): PendingSuggestionEntity {
    return PendingSuggestionEntity(
        id = id,
        faceId = faceId,
        suggestedPersonId = suggestedPersonId,
        similarityScore = similarityScore,
        status = status.name,
        createdAt = createdAt
    )
}
