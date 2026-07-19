package com.bhanu.ironlog.ui.screens.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.ironlog.data.local.entity.WorkoutSession
import com.bhanu.ironlog.data.local.entity.PersonalRecordEntity
import com.bhanu.ironlog.data.local.pojo.SessionExerciseWithTemplateAndSets
import com.bhanu.ironlog.data.repository.PersonalRecordRepository
import com.bhanu.ironlog.data.repository.WorkoutSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class WorkoutDetailsViewModel @Inject constructor(
    private val sessionRepository: WorkoutSessionRepository,
    private val prRepository: PersonalRecordRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val sessionId: Long = savedStateHandle.get<Long>("sessionId") ?: -1L
    val isArgumentValid = sessionId != -1L

    val session: StateFlow<WorkoutSession?> = if (isArgumentValid) {
        sessionRepository.getSessionById(sessionId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    } else {
        MutableStateFlow(null)
    }

    val exercises: StateFlow<List<SessionExerciseWithTemplateAndSets>> = if (isArgumentValid) {
        sessionRepository.getExercisesWithSetsForSession(sessionId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    } else {
        MutableStateFlow(emptyList())
    }

    val personalRecords: StateFlow<List<PersonalRecordEntity>> = if (isArgumentValid) {
        prRepository.getAllPRs().map { list ->
            list.filter { it.weightPRSessionId == sessionId || it.estimated1RMSessionId == sessionId }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    } else {
        MutableStateFlow(emptyList())
    }

    val workoutStats: StateFlow<WorkoutStats?> = combine(exercises, personalRecords) { exercises, prs ->
        if (exercises.isEmpty()) return@combine null
        
        val totalVolume = exercises.sumOf { ex -> ex.sets.sumOf { it.weight * it.reps } }
        val totalSets = exercises.sumOf { it.sets.size }
        val totalReps = exercises.sumOf { ex -> ex.sets.sumOf { it.reps } }
        val prCount = prs.size
        
        val avgIntensity = if (totalReps > 0) totalVolume / totalReps else 0.0
        
        WorkoutStats(totalVolume, totalSets, exercises.size, prCount, avgIntensity)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}

data class WorkoutStats(
    val totalVolume: Double,
    val totalSets: Int,
    val exerciseCount: Int,
    val prCount: Int,
    val averageIntensity: Double
)
