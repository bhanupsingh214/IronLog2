package com.bhanu.ironlog.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.ironlog.data.local.entity.LibraryExerciseEntity
import com.bhanu.ironlog.data.repository.ExerciseLibraryRepository
import com.bhanu.ironlog.data.repository.SaveExerciseResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseLibraryViewModel @Inject constructor(
    private val repository: ExerciseLibraryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val uiState: StateFlow<ExerciseLibraryUiState> = combine(
        repository.allActiveExercises,
        _searchQuery
    ) { allExercises, query ->
        val filtered = if (query.isBlank()) {
            allExercises
        } else {
            allExercises.filter { 
                it.name.contains(query, ignoreCase = true) || 
                it.muscleGroup.contains(query, ignoreCase = true)
            }
        }
        ExerciseLibraryUiState.Success(
            exercises = filtered,
            totalActiveCount = allExercises.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExerciseLibraryUiState.Loading
    )

    private val _saveResult = MutableSharedFlow<SaveExerciseResult>()
    val saveResult = _saveResult.asSharedFlow()

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun saveExercise(exercise: LibraryExerciseEntity, ignoreSimilarity: Boolean = false) {
        viewModelScope.launch {
            val result = repository.validateAndSaveExercise(exercise, ignoreSimilarity)
            _saveResult.emit(result)
        }
    }

    fun updateExercise(exercise: LibraryExerciseEntity) {
        viewModelScope.launch {
            repository.updateExercise(exercise)
        }
    }

    fun archiveExercise(id: Long) {
        viewModelScope.launch {
            repository.archiveExercise(id)
        }
    }

    // Static data for dropdowns
    val muscleGroups = listOf("Chest", "Back", "Shoulders", "Legs", "Arms", "Biceps", "Triceps", "Core", "Cardio", "Full Body")
    val equipmentOptions = listOf("None", "Barbell", "Dumbbell", "Machine", "Cable", "Kettlebell", "Band", "Plate")
    val exerciseTypes = listOf("Compound", "Isolation", "Cardio")
}

sealed interface ExerciseLibraryUiState {
    object Loading : ExerciseLibraryUiState
    data class Success(
        val exercises: List<LibraryExerciseEntity>,
        val totalActiveCount: Int
    ) : ExerciseLibraryUiState
}
