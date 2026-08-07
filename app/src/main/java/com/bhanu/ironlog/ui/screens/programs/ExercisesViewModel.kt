package com.bhanu.ironlog.ui.screens.programs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.ironlog.data.local.entity.ExerciseEntity
import com.bhanu.ironlog.data.local.entity.LibraryExerciseEntity
import com.bhanu.ironlog.data.local.entity.WorkoutDayEntity
import com.bhanu.ironlog.data.local.pojo.ProgramExerciseWithLibrary
import com.bhanu.ironlog.data.repository.ExerciseLibraryRepository
import com.bhanu.ironlog.data.repository.ProgramRepository
import com.bhanu.ironlog.data.repository.SaveExerciseResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExercisesViewModel @Inject constructor(
    private val repository: ProgramRepository,
    private val libraryRepository: ExerciseLibraryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val dayId: Long = savedStateHandle.get<Long>("dayId") ?: -1L

    val isArgumentValid = dayId != -1L

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _librarySearchQuery = MutableStateFlow("")
    val librarySearchQuery = _librarySearchQuery.asStateFlow()

    val workoutDay: StateFlow<WorkoutDayEntity?> = if (isArgumentValid) {
        repository.getWorkoutDay(dayId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    } else {
        MutableStateFlow(null)
    }

    val exercises: StateFlow<List<ProgramExerciseWithLibrary>> = if (isArgumentValid) {
        combine(
            repository.getExercisesWithLibraryForDay(dayId),
            _searchQuery
        ) { exercises, query ->
            if (query.isBlank()) {
                exercises
            } else {
                exercises.filter {
                    it.exerciseName.contains(query, ignoreCase = true) ||
                    it.muscleGroup.contains(query, ignoreCase = true)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    } else {
        MutableStateFlow(emptyList())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val libraryExercises: StateFlow<List<LibraryExerciseEntity>> = _librarySearchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                libraryRepository.allActiveExercises
            } else {
                libraryRepository.searchExercises(query)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _saveResult = MutableSharedFlow<SaveExerciseResult>()
    val saveResult = _saveResult.asSharedFlow()

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onLibrarySearchQueryChange(query: String) {
        _librarySearchQuery.value = query
    }

    fun addExerciseFromLibrary(libraryExercise: LibraryExerciseEntity) {
        viewModelScope.launch {
            // Check if already added
            val current = exercises.value
            if (current.any { it.programExercise.libraryExerciseId == libraryExercise.id }) {
                return@launch
            }

            repository.insertExercise(
                ExerciseEntity(
                    dayId = dayId,
                    libraryExerciseId = libraryExercise.id,
                    name = libraryExercise.name,
                    muscleGroup = libraryExercise.muscleGroup,
                    equipment = libraryExercise.equipment,
                    exerciseType = libraryExercise.exerciseType,
                    order = 0
                )
            )
        }
    }

    fun saveAndAddExercise(exercise: LibraryExerciseEntity, ignoreSimilarity: Boolean = false) {
        viewModelScope.launch {
            val result = libraryRepository.validateAndSaveExercise(exercise, ignoreSimilarity)
            if (result is SaveExerciseResult.Success) {
                // Fetch the newly created exercise to get the full identity
                val newExercise = libraryRepository.allActiveExercises.first().find { it.id == result.id }
                if (newExercise != null) {
                    addExerciseFromLibrary(newExercise)
                }
            }
            _saveResult.emit(result)
        }
    }

    fun updateExercise(exercise: ExerciseEntity) {
        viewModelScope.launch {
            repository.updateExercise(exercise)
        }
    }

    fun deleteExercise(exercise: ExerciseEntity) {
        viewModelScope.launch {
            repository.deleteExercise(exercise)
        }
    }

    fun duplicateExercise(exerciseId: Long) {
        viewModelScope.launch {
            repository.duplicateExercise(exerciseId)
        }
    }

    fun moveExerciseUp(exerciseId: Long) {
        viewModelScope.launch {
            repository.moveExercise(exerciseId, up = true)
        }
    }

    fun moveExerciseDown(exerciseId: Long) {
        viewModelScope.launch {
            repository.moveExercise(exerciseId, up = false)
        }
    }

    // Static data for dropdowns
    val muscleGroups = listOf("Chest", "Back", "Shoulders", "Legs", "Arms", "Biceps", "Triceps", "Core", "Cardio", "Full Body")
    val equipmentOptions = listOf("None", "Barbell", "Dumbbell", "Machine", "Cable", "Kettlebell", "Band", "Plate")
    val exerciseTypes = listOf("Compound", "Isolation", "Cardio")
}
