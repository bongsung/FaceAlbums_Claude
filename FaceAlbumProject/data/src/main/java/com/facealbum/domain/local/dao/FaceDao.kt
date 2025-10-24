package com.facealbum.domain.local.dao

import androidx.room.*
import com.facealbum.domain.local.entity.FaceEntity

@Dao
interface FaceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaces(faces: List<FaceEntity>): List<Long>
    
    @Query("SELECT * FROM faces WHERE id = :faceId")
    suspend fun getFaceById(faceId: Long): FaceEntity?
    
    @Query("SELECT * FROM faces WHERE photoId = :photoId")
    suspend fun getFacesForPhoto(photoId: Long): List<FaceEntity>
    
    @Query("""
        SELECT faces.* FROM faces
        LEFT JOIN link_person_photo ON faces.photoId = link_person_photo.photoId
        WHERE link_person_photo.personId IS NULL
    """)
    suspend fun getUnassignedFaces(): List<FaceEntity>
    
    @Query("""
        SELECT faces.* FROM faces
        INNER JOIN link_person_photo ON faces.photoId = link_person_photo.photoId
        WHERE link_person_photo.personId = :personId
    """)
    suspend fun getFacesForPerson(personId: Long): List<FaceEntity>
    
    @Query("SELECT * FROM faces")
    suspend fun getAllFaces(): List<FaceEntity>
    
    @Query("DELETE FROM faces WHERE photoId = :photoId")
    suspend fun deleteFacesForPhoto(photoId: Long)
    
    @Delete
    suspend fun deleteFace(face: FaceEntity)
}
