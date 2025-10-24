package com.facealbum.domain.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.facealbum.domain.model.WatchFolder

@Entity(
    tableName = "watch_folders",
    indices = [Index(value = ["path"], unique = true)]
)
data class WatchFolderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val path: String,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

fun WatchFolderEntity.toDomain(): WatchFolder {
    return WatchFolder(
        id = id,
        path = path,
        isEnabled = isEnabled,
        createdAt = createdAt
    )
}

fun WatchFolder.toEntity(): WatchFolderEntity {
    return WatchFolderEntity(
        id = id,
        path = path,
        isEnabled = isEnabled,
        createdAt = createdAt
    )
}
