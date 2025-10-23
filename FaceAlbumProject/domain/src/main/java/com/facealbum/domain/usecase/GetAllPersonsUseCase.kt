package com.facealbum.domain.usecase

import com.facealbum.domain.model.Person
import com.facealbum.domain.repository.PersonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get all persons
 */
class GetAllPersonsUseCase @Inject constructor(
    private val personRepository: PersonRepository
) {
    operator fun invoke(): Flow<List<Person>> {
        return personRepository.getAllPersons()
    }
}
