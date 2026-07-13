package com.bhanu.ironlog.ui.screens.programs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.ironlog.data.local.entity.ExerciseEntity
import com.bhanu.ironlog.data.local.entity.WorkoutDayEntity
import com.bhanu.ironlog.data.repository.ProgramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExercisesViewModel @Inject constructor(
    private val repository: ProgramRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val dayId: Long = checkNotNull(savedStateHandle["dayId"])

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val workoutDay: StateFlow<WorkoutDayEntity?> = repository.getWorkoutDay(dayId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val exercises: StateFlow<List<ExerciseEntity>> = combine(
        repository.getExercisesForDay(dayId),
        _searchQuery
    ) { exercises, query ->
        if (query.isBlank()) {
            exercises
        } else {
            exercises.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.muscleGroup.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun addExercise(
        name: String,
        muscleGroup: String,
        equipment: String,
        exerciseType: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.insertExercise(
                ExerciseEntity(
                    dayId = dayId,
                    name = name,
                    muscleGroup = muscleGroup,
                    equipment = equipment,
                    exerciseType = exerciseType,
                    notes = notes,
                    order = 0 // Repository will handle actual order
                )
            )
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
}
