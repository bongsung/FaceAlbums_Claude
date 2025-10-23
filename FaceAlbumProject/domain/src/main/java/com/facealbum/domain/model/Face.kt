package com.facealbum.domain.model

/**
 * Represents a detected face in a photo with its embedding
 */
data class Face(
    val id: Long = 0,
    val photoId: Long,
    val boundingBox: BoundingBox,
    val embedding: FloatArray,
    val confidence: Float,
    val detectedAt: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Face

        if (id != other.id) return false
        if (photoId != other.photoId) return false
        if (boundingBox != other.boundingBox) return false
        if (!embedding.contentEquals(other.embedding)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + photoId.hashCode()
        result = 31 * result + boundingBox.hashCode()
        result = 31 * result + embedding.contentHashCode()
        return result
    }
}

/**
 * Bounding box coordinates (normalized 0-1)
 */
data class BoundingBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)
