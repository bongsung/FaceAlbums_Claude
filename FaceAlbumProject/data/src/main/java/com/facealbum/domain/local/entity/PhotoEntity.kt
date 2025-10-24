package com.facealbum.domain.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.facealbum.domain.model.Photo

@Entity(
    tableName = "photos",
    indices = [
        Index(value = ["mediaStoreId"], unique = true),
        Index(value = ["contentHash"]),
        Index(value = ["processedAt"])
    ]
)
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mediaStoreId: Long,
    val uri: String,
    val displayName: String,
    val dateAdded: Long,
    val dateModified: Long,
    val size: Long,
    val mimeType: String,
    val contentHash: String, // SHA-256 hash for tracking renames/moves
    val width: Int = 0,
    val height: Int = 0,
    val hasFaces: Boolean = false,
    val processedAt: Long? = null
)

fun PhotoEntity.toDomain(): Photo {
    return Photo(
        id = id,
        mediaStoreId = mediaStoreId,
        uri = uri,
        displayName = displayName,
        dateAdded = dateAdded,
        dateModified = dateModified,
        size = size,
        mimeType = mimeType,
        contentHash = contentHash,
        width = width,
        height = height,
        hasFaces = hasFaces,
        processedAt = processedAt
    )
}

fun Photo.toEntity(): PhotoEntity {
    return PhotoEntity(
        id = id,
        mediaStoreId = mediaStoreId,
        uri = uri,
        displayName = displayName,
        dateAdded = dateAdded,
        dateModified = dateModified,
        size = size,
        mimeType = mimeType,
        contentHash = contentHash,
        width = width,
        height = height,
        hasFaces = hasFaces,
        processedAt = processedAt
    )
}
