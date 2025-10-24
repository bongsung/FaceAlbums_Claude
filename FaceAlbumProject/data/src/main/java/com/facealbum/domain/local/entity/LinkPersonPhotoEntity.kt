package com.facealbum.domain.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "link_person_photo",
    primaryKeys = ["personId", "photoId"],
    foreignKeys = [
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PhotoEntity::class,
            parentColumns = ["id"],
            childColumns = ["photoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["personId"]),
        Index(value = ["photoId"])
    ]
)
data class LinkPersonPhotoEntity(
    val personId: Long,
    val photoId: Long,
    val linkedAt: Long = System.currentTimeMillis()
)
