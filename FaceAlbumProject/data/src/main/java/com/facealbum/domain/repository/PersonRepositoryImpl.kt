package com.facealbum.domain.repository

import com.facealbum.common.Result
import com.facealbum.domain.local.dao.PersonDao
import com.facealbum.domain.local.entity.*
import com.facealbum.domain.model.Person
import com.facealbum.domain.model.Photo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PersonRepositoryImpl @Inject constructor(
    private val personDao: PersonDao
) : PersonRepository {
    
    override fun getAllPersons(): Flow<List<Person>> {
        return personDao.getAllPersons().map { entities ->
            entities.map { entity ->
                val photoCount = personDao.getPhotoCountForPerson(entity.id)
                entity.toDomain(photoCount)
            }
        }
    }
    
    override suspend fun getPersonById(personId: Long): Result<Person> {
        return try {
            val entity = personDao.getPersonById(personId)
            if (entity != null) {
                val photoCount = personDao.getPhotoCountForPerson(personId)
                Result.Success(entity.toDomain(photoCount))
            } else {
                Result.Error(NoSuchElementException("Person not found"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun createPerson(name: String, coverPhotoUri: String?): Result<Person> {
        return try {
            val entity = PersonEntity(
                name = name,
                coverPhotoUri = coverPhotoUri
            )
            val id = personDao.insertPerson(entity)
            Result.Success(entity.copy(id = id).toDomain())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun updatePerson(person: Person): Result<Unit> {
        return try {
            personDao.updatePerson(person.toEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun deletePerson(personId: Long): Result<Unit> {
        return try {
            personDao.deletePersonById(personId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun linkPhotoToPerson(personId: Long, photoId: Long): Result<Unit> {
        return try {
            val link = LinkPersonPhotoEntity(personId = personId, photoId = photoId)
            personDao.linkPhotoToPerson(link)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun unlinkPhotoFromPerson(personId: Long, photoId: Long): Result<Unit> {
        return try {
            personDao.unlinkPhotoFromPerson(personId, photoId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override fun getPhotosForPerson(personId: Long): Flow<List<Photo>> {
        return personDao.getPhotosForPerson(personId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun searchPersons(query: String): Result<List<Person>> {
        return try {
            val entities = personDao.searchPersons(query)
            val persons = entities.map { entity ->
                val photoCount = personDao.getPhotoCountForPerson(entity.id)
                entity.toDomain(photoCount)
            }
            Result.Success(persons)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
