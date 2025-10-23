package com.facealbum.domain.model

/**
 * Represents a virtual person/album
 */
data class Person(
    val id: Long = 0,
    val name: String,
    val coverPhotoUri: String? = null,
    val photoCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
