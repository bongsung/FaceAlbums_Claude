package com.facealbum.domain.repository

import com.facealbum.common.Result
import com.facealbum.domain.model.Person
import com.facealbum.domain.model.Photo
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Person operations
 */
interface PersonRepository {
    /**
     * Get all persons as Flow
     */
    fun getAllPersons(): Flow<List<Person>>
    
    /**
     * Get person by ID
     */
    suspend fun getPersonById(personId: Long): Result<Person>
    
    /**
     * Create a new person
     */
    suspend fun createPerson(name: String, coverPhotoUri: String? = null): Result<Person>
    
    /**
     * Update person details
     */
    suspend fun updatePerson(person: Person): Result<Unit>
    
    /**
     * Delete person (and unlink all photos)
     */
    suspend fun deletePerson(personId: Long): Result<Unit>
    
    /**
     * Link a photo to a person
     */
    suspend fun linkPhotoToPerson(personId: Long, photoId: Long): Result<Unit>
    
    /**
     * Unlink a photo from a person
     */
    suspend fun unlinkPhotoFromPerson(personId: Long, photoId: Long): Result<Unit>
    
    /**
     * Get all photos for a person
     */
    fun getPhotosForPerson(personId: Long): Flow<List<Photo>>
    
    /**
     * Search persons by name
     */
    suspend fun searchPersons(query: String): Result<List<Person>>
}
