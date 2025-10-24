package com.facealbum.domain.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.facealbum.domain.model.BoundingBox
import com.facealbum.domain.model.Face

@Entity(
    tableName = "faces",
    foreignKeys = [
        ForeignKey(
            entity = PhotoEntity::class,
            parentColumns = ["id"],
            childColumns = ["photoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["photoId"])]
)
data class FaceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val photoId: Long,
    val boundingBoxLeft: Float,
    val boundingBoxTop: Float,
    val boundingBoxRight: Float,
    val boundingBoxBottom: Float,
    val embedding: String, // Stored as comma-separated string
    val confidence: Float,
    val detectedAt: Long = System.currentTimeMillis()
)

fun FaceEntity.toDomain(): Face {
    val embeddingArray = embedding.split(",").map { it.toFloat() }.toFloatArray()
    return Face(
        id = id,
        photoId = photoId,
        boundingBox = BoundingBox(
            left = boundingBoxLeft,
            top = boundingBoxTop,
            right = boundingBoxRight,
            bottom = boundingBoxBottom
        ),
        embedding = embeddingArray,
        confidence = confidence,
        detectedAt = detectedAt
    )
}

fun Face.toEntity(): FaceEntity {
    val embeddingString = embedding.joinToString(",")
    return FaceEntity(
        id = id,
        photoId = photoId,
        boundingBoxLeft = boundingBox.left,
        boundingBoxTop = boundingBox.top,
        boundingBoxRight = boundingBox.right,
        boundingBoxBottom = boundingBox.bottom,
        embedding = embeddingString,
        confidence = confidence,
        detectedAt = detectedAt
    )
}
