package com.facealbum.app.ui.screens.suggestions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facealbum.domain.model.PendingSuggestion
import com.facealbum.domain.usecase.GetPendingSuggestionsUseCase
import com.facealbum.domain.usecase.ProcessSuggestionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SuggestionsViewModel @Inject constructor(
    private val getPendingSuggestionsUseCase: GetPendingSuggestionsUseCase,
    private val processSuggestionUseCase: ProcessSuggestionUseCase
) : ViewModel() {
    
    private val _suggestions = MutableStateFlow<List<PendingSuggestion>>(emptyList())
    val suggestions: StateFlow<List<PendingSuggestion>> = _suggestions.asStateFlow()
    
    init {
        observeSuggestions()
    }
    
    private fun observeSuggestions() {
        viewModelScope.launch {
            getPendingSuggestionsUseCase().collect { suggestionList ->
                _suggestions.value = suggestionList
            }
        }
    }
    
    fun acceptSuggestion(suggestionId: Long) {
        viewModelScope.launch {
            processSuggestionUseCase.accept(suggestionId)
        }
    }
    
    fun rejectSuggestion(suggestionId: Long) {
        viewModelScope.launch {
            processSuggestionUseCase.reject(suggestionId)
        }
    }
}
