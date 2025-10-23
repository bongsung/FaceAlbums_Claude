package com.facealbum.domain.model

/**
 * Represents a photo from MediaStore
 */
data class Photo(
    val id: Long = 0,
    val mediaStoreId: Long,
    val uri: String,
    val displayName: String,
    val dateAdded: Long,
    val dateModified: Long,
    val size: Long,
    val mimeType: String,
    val contentHash: String, // SHA-256 hash
    val width: Int = 0,
    val height: Int = 0,
    val hasFaces: Boolean = false,
    val processedAt: Long? = null
)
