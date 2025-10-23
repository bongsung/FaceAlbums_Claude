package com.facealbum.domain.model

/**
 * Represents a folder being watched for new/modified photos
 */
data class WatchFolder(
    val id: Long = 0,
    val path: String,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
