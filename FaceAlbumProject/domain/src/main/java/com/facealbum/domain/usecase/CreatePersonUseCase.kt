package com.facealbum.domain.usecase

import com.facealbum.common.Result
import com.facealbum.domain.model.Person
import com.facealbum.domain.repository.PersonRepository
import javax.inject.Inject

/**
 * Use case to create a new person
 */
class CreatePersonUseCase @Inject constructor(
    private val personRepository: PersonRepository
) {
    suspend operator fun invoke(name: String, coverPhotoUri: String? = null): Result<Person> {
        if (name.isBlank()) {
            return Result.Error(IllegalArgumentException("Person name cannot be empty"))
        }
        return personRepository.createPerson(name, coverPhotoUri)
    }
}
