package com.facealbum.domain.repository

import com.facealbum.common.Result
import com.facealbum.domain.local.dao.WatchFolderDao
import com.facealbum.domain.local.entity.WatchFolderEntity
import com.facealbum.domain.local.entity.toDomain
import com.facealbum.domain.model.WatchFolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val watchFolderDao: WatchFolderDao
) : SettingsRepository {

    override fun getWatchFolders(): Flow<List<WatchFolder>> {
        return watchFolderDao.getAllWatchFolders().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addWatchFolder(path: String): Result<Long> {
        return try {
            val entity = WatchFolderEntity(path = path, isEnabled = true)
            val id = watchFolderDao.insertWatchFolder(entity)
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun removeWatchFolder(folderId: Long): Result<Unit> {
        return try {
            watchFolderDao.deleteWatchFolder(folderId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun toggleWatchFolder(folderId: Long, enabled: Boolean): Result<Unit> {
        return try {
            watchFolderDao.toggleWatchFolder(folderId, enabled)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // TODO: SharedPreferences나 DataStore로 구현 필요
    override fun getExportToExternalGallery(): Flow<Boolean> {
        return flow { emit(false) }
    }

    override suspend fun setExportToExternalGallery(enabled: Boolean): Result<Unit> {
        return Result.Success(Unit)
    }

    override fun getAutoClusterEnabled(): Flow<Boolean> {
        return flow { emit(true) }
    }

    override suspend fun setAutoClusterEnabled(enabled: Boolean): Result<Unit> {
        return Result.Success(Unit)
    }
}