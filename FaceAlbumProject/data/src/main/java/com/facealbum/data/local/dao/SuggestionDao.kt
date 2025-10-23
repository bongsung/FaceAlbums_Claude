package com.facealbum.data.local.dao

import androidx.room.*
import com.facealbum.data.local.entity.PendingSuggestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SuggestionDao {
    @Query("SELECT * FROM pending_suggestions WHERE status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingSuggestions(): Flow<List<PendingSuggestionEntity>>
    
    @Query("SELECT * FROM pending_suggestions WHERE id = :suggestionId")
    suspend fun getSuggestionById(suggestionId: Long): PendingSuggestionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuggestion(suggestion: PendingSuggestionEntity): Long
    
    @Update
    suspend fun updateSuggestion(suggestion: PendingSuggestionEntity)
    
    @Query("UPDATE pending_suggestions SET status = :status WHERE id = :suggestionId")
    suspend fun updateSuggestionStatus(suggestionId: Long, status: String)
    
    @Query("SELECT * FROM pending_suggestions WHERE faceId = :faceId")
    suspend fun getSuggestionsForFace(faceId: Long): List<PendingSuggestionEntity>
    
    @Delete
    suspend fun deleteSuggestion(suggestion: PendingSuggestionEntity)
    
    @Query("DELETE FROM pending_suggestions WHERE status IN ('ACCEPTED', 'REJECTED') AND createdAt < :timestamp")
    suspend fun deleteOldProcessedSuggestions(timestamp: Long)
}
