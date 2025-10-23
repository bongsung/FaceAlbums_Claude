package com.facealbum.domain.usecase

import com.facealbum.domain.model.Photo
import com.facealbum.domain.repository.PersonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get all photos for a specific person
 */
class GetPhotosForPersonUseCase @Inject constructor(
    private val personRepository: PersonRepository
) {
    operator fun invoke(personId: Long): Flow<List<Photo>> {
        return personRepository.getPhotosForPerson(personId)
    }
}
