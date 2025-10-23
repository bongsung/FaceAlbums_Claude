package com.facealbum.data.local.dao

import androidx.room.*
import com.facealbum.data.local.entity.LinkPersonPhotoEntity
import com.facealbum.data.local.entity.PersonEntity
import com.facealbum.data.local.entity.PhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Query("SELECT * FROM persons ORDER BY updatedAt DESC")
    fun getAllPersons(): Flow<List<PersonEntity>>
    
    @Query("SELECT * FROM persons WHERE id = :personId")
    suspend fun getPersonById(personId: Long): PersonEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: PersonEntity): Long
    
    @Update
    suspend fun updatePerson(person: PersonEntity)
    
    @Delete
    suspend fun deletePerson(person: PersonEntity)
    
    @Query("DELETE FROM persons WHERE id = :personId")
    suspend fun deletePersonById(personId: Long)
    
    @Query("SELECT * FROM persons WHERE name LIKE '%' || :query || '%'")
    suspend fun searchPersons(query: String): List<PersonEntity>
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun linkPhotoToPerson(link: LinkPersonPhotoEntity)
    
    @Query("DELETE FROM link_person_photo WHERE personId = :personId AND photoId = :photoId")
    suspend fun unlinkPhotoFromPerson(personId: Long, photoId: Long)
    
    @Query("""
        SELECT photos.* FROM photos
        INNER JOIN link_person_photo ON photos.id = link_person_photo.photoId
        WHERE link_person_photo.personId = :personId
        ORDER BY photos.dateAdded DESC
    """)
    fun getPhotosForPerson(personId: Long): Flow<List<PhotoEntity>>
    
    @Query("SELECT COUNT(*) FROM link_person_photo WHERE personId = :personId")
    suspend fun getPhotoCountForPerson(personId: Long): Int
}
