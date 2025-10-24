package com.facealbum.domain.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.facealbum.domain.model.Person

@Entity(tableName = "persons")
data class PersonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val coverPhotoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

fun PersonEntity.toDomain(photoCount: Int = 0): Person {
    return Person(
        id = id,
        name = name,
        coverPhotoUri = coverPhotoUri,
        photoCount = photoCount,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Person.toEntity(): PersonEntity {
    return PersonEntity(
        id = id,
        name = name,
        coverPhotoUri = coverPhotoUri,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
