package com.facealbum.domain.repository

import com.facealbum.common.Result
import com.facealbum.domain.model.WatchFolder
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Settings and configuration
 */
interface SettingsRepository {
    /**
     * Get all watch folders as Flow
     */
    fun getWatchFolders(): Flow<List<WatchFolder>>
    
    /**
     * Add a watch folder
     */
    suspend fun addWatchFolder(path: String): Result<Long>
    
    /**
     * Remove a watch folder
     */
    suspend fun removeWatchFolder(folderId: Long): Result<Unit>
    
    /**
     * Toggle watch folder enabled state
     */
    suspend fun toggleWatchFolder(folderId: Long, enabled: Boolean): Result<Unit>
    
    /**
     * Get export to external gallery setting
     */
    fun getExportToExternalGallery(): Flow<Boolean>
    
    /**
     * Set export to external gallery setting
     */
    suspend fun setExportToExternalGallery(enabled: Boolean): Result<Unit>
    
    /**
     * Get auto-clustering enabled setting
     */
    fun getAutoClusterEnabled(): Flow<Boolean>
    
    /**
     * Set auto-clustering enabled setting
     */
    suspend fun setAutoClusterEnabled(enabled: Boolean): Result<Unit>
}
