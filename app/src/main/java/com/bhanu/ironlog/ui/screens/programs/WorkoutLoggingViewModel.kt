package com.bhanu.ironlog.ui.screens.programs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.ironlog.data.local.entity.ExerciseEntity
import com.bhanu.ironlog.data.local.entity.SetEntity
import com.bhanu.ironlog.data.local.entity.WorkoutSessionEntity
import com.bhanu.ironlog.data.repository.ProgramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutLoggingViewModel @Inject constructor(
    private val repository: ProgramRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val exerciseId: Long = savedStateHandle.get<Long>("exerciseId") ?: -1L
    val sessionId: Long = savedStateHandle.get<Long>("sessionId") ?: -1L

    val isArgumentValid = exerciseId != -1L && sessionId != -1L

    val exercise: StateFlow<ExerciseEntity?> = if (isArgumentValid) {
        repository.getExercise(exerciseId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    } else {
        MutableStateFlow(null)
    }

    val sets: StateFlow<List<SetEntity>> = if (isArgumentValid) {
        repository.getSetsForExercise(exerciseId, sessionId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    } else {
        MutableStateFlow(emptyList())
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val session: StateFlow<WorkoutSessionEntity?> = if (isArgumentValid) {
        repository.getActiveSession()
            .mapLatest { active ->
                if (sessionId != 0L) {
                    // If we're looking for a specific session, find it
                    repository.getCompletedSessions().first().find { it.id == sessionId } ?: active
                } else {
                    null
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    } else {
        MutableStateFlow(null)
    }

    val previousSets: StateFlow<List<SetEntity>> = if (isArgumentValid) {
        repository.getPreviousSets(exerciseId, sessionId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    } else {
        MutableStateFlow(emptyList())
    }

    fun addSet(setType: String = "Working") {
        if (!isArgumentValid) return
        viewModelScope.launch {
            repository.insertSet(
                SetEntity(
                    exerciseId = exerciseId,
                    sessionId = sessionId,
                    setType = setType,
                    order = 0
                )
            )
        }
    }

    fun copyPreviousSet() {
        if (!isArgumentValid) return
        viewModelScope.launch {
            val currentSets = sets.value
            if (currentSets.isNotEmpty()) {
                val lastSet = currentSets.last()
                repository.insertSet(
                    lastSet.copy(
                        id = 0,
                        isCompleted = false,
                        createdAt = System.currentTimeMillis()
                    )
                )
            } else {
                addSet()
            }
        }
    }

    fun updateSet(set: SetEntity) {
        viewModelScope.launch {
            repository.updateSet(set)
        }
    }

    fun deleteSet(set: SetEntity) {
        viewModelScope.launch {
            repository.deleteSet(set)
        }
    }

    fun duplicateSet(setId: Long) {
        viewModelScope.launch {
            repository.duplicateSet(setId)
        }
    }

    fun moveSetUp(setId: Long) {
        viewModelScope.launch {
            repository.moveSet(setId, up = true)
        }
    }

    fun moveSetDown(setId: Long) {
        viewModelScope.launch {
            repository.moveSet(setId, up = false)
        }
    }
}
