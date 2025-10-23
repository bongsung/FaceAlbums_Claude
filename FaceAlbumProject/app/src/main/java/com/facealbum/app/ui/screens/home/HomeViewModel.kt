package com.facealbum.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facealbum.domain.model.Person
import com.facealbum.domain.usecase.CreatePersonUseCase
import com.facealbum.domain.usecase.GetAllPersonsUseCase
import com.facealbum.domain.usecase.GetPendingSuggestionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllPersonsUseCase: GetAllPersonsUseCase,
    private val getPendingSuggestionsUseCase: GetPendingSuggestionsUseCase,
    private val createPersonUseCase: CreatePersonUseCase
) : ViewModel() {
    
    private val _persons = MutableStateFlow<List<Person>>(emptyList())
    val persons: StateFlow<List<Person>> = _persons.asStateFlow()
    
    private val _suggestionsCount = MutableStateFlow(0)
    val suggestionsCount: StateFlow<Int> = _suggestionsCount.asStateFlow()
    
    init {
        observePersons()
        observeSuggestions()
    }
    
    private fun observePersons() {
        viewModelScope.launch {
            getAllPersonsUseCase().collect { personList ->
                _persons.value = personList
            }
        }
    }
    
    private fun observeSuggestions() {
        viewModelScope.launch {
            getPendingSuggestionsUseCase().collect { suggestions ->
                _suggestionsCount.value = suggestions.size
            }
        }
    }
    
    fun createNewPerson() {
        viewModelScope.launch {
            // TODO: Show dialog to enter person name
            val defaultName = "Person ${_persons.value.size + 1}"
            createPersonUseCase(defaultName)
        }
    }
}
