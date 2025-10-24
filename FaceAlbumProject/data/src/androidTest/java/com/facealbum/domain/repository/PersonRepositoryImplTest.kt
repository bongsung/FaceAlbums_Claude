package com.facealbum.domain.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.facealbum.domain.local.FaceAlbumDatabase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersonRepositoryImplTest {
    
    private lateinit var database: FaceAlbumDatabase
    private lateinit var repository: PersonRepository
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            FaceAlbumDatabase::class.java
        ).build()
        
        repository = PersonRepositoryImpl(database.personDao())
    }
    
    @After
    fun tearDown() {
        database.close()
    }
    
    @Test
    fun insertPerson_shouldReturnPersonWithId() = runTest {
        // Given
        val personName = "Test Person"
        
        // When
        val result = repository.createPerson(personName)
        
        // Then
        assertThat(result.isSuccess).isTrue()
        val person = result.getOrNull()!!
        assertThat(person.name).isEqualTo(personName)
        assertThat(person.id).isGreaterThan(0)
    }
    
    @Test
    fun getAllPersons_shouldReturnEmptyListInitially() = runTest {
        // When
        val persons = repository.getAllPersons().first()
        
        // Then
        assertThat(persons).isEmpty()
    }
    
    @Test
    fun getAllPersons_shouldReturnInsertedPersons() = runTest {
        // Given
        repository.createPerson("Person 1")
        repository.createPerson("Person 2")
        
        // When
        val persons = repository.getAllPersons().first()
        
        // Then
        assertThat(persons).hasSize(2)
        assertThat(persons.map { it.name }).containsExactly("Person 1", "Person 2")
    }
    
    @Test
    fun deletePerson_shouldRemovePerson() = runTest {
        // Given
        val createResult = repository.createPerson("Test Person")
        val personId = createResult.getOrNull()!!.id
        
        // When
        repository.deletePerson(personId)
        
        // Then
        val persons = repository.getAllPersons().first()
        assertThat(persons).isEmpty()
    }
}
