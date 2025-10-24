package com.facealbum.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facealbum.domain.model.Face
import com.facealbum.domain.model.Person
import com.facealbum.domain.model.WatchFolder
import com.facealbum.domain.repository.FaceRepository
import com.facealbum.domain.repository.PersonRepository
import com.facealbum.domain.repository.PhotoRepository
import com.facealbum.domain.repository.SettingsRepository
import com.facealbum.domain.repository.SuggestionRepository
import com.facealbum.domain.usecase.*
import com.facealbum.work.FolderScanWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllPersonsUseCase: GetAllPersonsUseCase,
    private val getPendingSuggestionsUseCase: GetPendingSuggestionsUseCase,
    private val createPersonUseCase: CreatePersonUseCase,
    private val personRepository: PersonRepository,
    private val faceRepository: FaceRepository,
    private val photoRepository: PhotoRepository,
    private val settingsRepository: SettingsRepository,
    private val suggestionRepository: SuggestionRepository
) : ViewModel() {

    // UI State
    private val _persons = MutableStateFlow<List<Person>>(emptyList())
    val persons: StateFlow<List<Person>> = _persons.asStateFlow()

    private val _suggestionsCount = MutableStateFlow(0)
    val suggestionsCount: StateFlow<Int> = _suggestionsCount.asStateFlow()

    private val _watchFolders = MutableStateFlow<List<WatchFolder>>(emptyList())
    val watchFolders: StateFlow<List<WatchFolder>> = _watchFolders.asStateFlow()

    private val _unassignedFaces = MutableStateFlow<List<Face>>(emptyList())
    val unassignedFaces: StateFlow<List<Face>> = _unassignedFaces.asStateFlow()

    val newFacesCount: StateFlow<Int> = _unassignedFaces.map { it.size }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    private val _faceToAdd = MutableStateFlow<Face?>(null)
    val faceToAdd: StateFlow<Face?> = _faceToAdd.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    init {
        observePersons()
        observeSuggestions()
        observeWatchFolders()
        observeUnassignedFaces()
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

    private fun observeWatchFolders() {
        viewModelScope.launch {
            settingsRepository.getWatchFolders().collect { folders ->
                _watchFolders.value = folders
            }
        }
    }

    private fun observeUnassignedFaces() {
        viewModelScope.launch {
            try {
                val result = faceRepository.getUnassignedFaces()
                result.getOrNull()?.let { faces ->
                    _unassignedFaces.value = faces
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addWatchFolder(folderPath: String) {
        viewModelScope.launch {
            try {
                val result = settingsRepository.addWatchFolder(folderPath)
                if (result.isSuccess) {
                    // Start scanning the folder immediately
                    val folderId = result.getOrNull()
                    if (folderId != null) {
                        scanFolder(folderId)
                    }
                }
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }

    fun removeWatchFolder(folderId: Long) {
        viewModelScope.launch {
            try {
                settingsRepository.removeWatchFolder(folderId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun scanFolder(folderId: Long) {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                // Trigger folder scanning worker
                // This would queue a WorkManager job to scan the folder
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun showAddFaceDialog(face: Face) {
        _faceToAdd.value = face
    }

    fun dismissAddFaceDialog() {
        _faceToAdd.value = null
    }

    fun addFaceAsPerson(face: Face, name: String) {
        viewModelScope.launch {
            try {
                // Check if person exists
                val existingPersons = personRepository.searchPersons(name)

                val personId = if (existingPersons.isSuccess &&
                    existingPersons.getOrNull()?.isNotEmpty() == true) {
                    // Add to existing person
                    existingPersons.getOrNull()?.first()?.id
                } else {
                    // Create new person
                    val result = createPersonUseCase(name)
                    result.getOrNull()?.id
                }

                personId?.let { id ->
                    // Link the face's photo to this person
                    val photoResult = photoRepository.getPhotoById(face.photoId)
                    if (photoResult.isSuccess) {
                        personRepository.linkPhotoToPerson(id, face.photoId)

                        // Create a suggestion and auto-accept it
                        val suggestionResult = suggestionRepository.createSuggestion(
                            faceId = face.id,
                            suggestedPersonId = id,
                            similarityScore = 1.0f
                        )

                        suggestionResult.getOrNull()?.let { suggestionId ->
                            suggestionRepository.acceptSuggestion(suggestionId)
                        }
                    }
                }

                _faceToAdd.value = null
            } catch (e: Exception) {
                e.printStackTrace()
                _faceToAdd.value = null
            }
        }
    }

    fun createNewPerson() {
        viewModelScope.launch {
            // This is now handled through the new faces tab
            // Users create persons by adding faces
        }
    }
}