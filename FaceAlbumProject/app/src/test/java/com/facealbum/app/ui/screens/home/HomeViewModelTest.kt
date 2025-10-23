package com.facealbum.app.ui.screens.home

import app.cash.turbine.test
import com.facealbum.domain.model.Person
import com.facealbum.domain.usecase.CreatePersonUseCase
import com.facealbum.domain.usecase.GetAllPersonsUseCase
import com.facealbum.domain.usecase.GetPendingSuggestionsUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    
    private val testDispatcher = UnconfinedTestDispatcher()
    
    private lateinit var getAllPersonsUseCase: GetAllPersonsUseCase
    private lateinit var getPendingSuggestionsUseCase: GetPendingSuggestionsUseCase
    private lateinit var createPersonUseCase: CreatePersonUseCase
    private lateinit var viewModel: HomeViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        getAllPersonsUseCase = mockk()
        getPendingSuggestionsUseCase = mockk()
        createPersonUseCase = mockk()
        
        // Setup default mocks
        coEvery { getAllPersonsUseCase() } returns flowOf(emptyList())
        coEvery { getPendingSuggestionsUseCase() } returns flowOf(emptyList())
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `when persons are available, state should be updated`() = runTest {
        // Given
        val persons = listOf(
            Person(id = 1, name = "John Doe", photoCount = 5),
            Person(id = 2, name = "Jane Smith", photoCount = 3)
        )
        coEvery { getAllPersonsUseCase() } returns flowOf(persons)
        
        // When
        viewModel = HomeViewModel(
            getAllPersonsUseCase,
            getPendingSuggestionsUseCase,
            createPersonUseCase
        )
        
        // Then
        viewModel.persons.test {
            assertThat(awaitItem()).isEqualTo(persons)
        }
    }
    
    @Test
    fun `createNewPerson should call use case`() = runTest {
        // Given
        val person = Person(id = 1, name = "Person 1")
        coEvery { createPersonUseCase(any(), any()) } returns com.facealbum.common.Result.Success(person)
        
        viewModel = HomeViewModel(
            getAllPersonsUseCase,
            getPendingSuggestionsUseCase,
            createPersonUseCase
        )
        
        // When
        viewModel.createNewPerson()
        
        // Then
        coVerify { createPersonUseCase(any(), any()) }
    }
}
