package com.bhanu.ironlog.ui.screens.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.ironlog.data.local.pojo.SessionExerciseWithSets
import com.bhanu.ironlog.data.local.pojo.WorkoutCompletionSummary
import com.bhanu.ironlog.data.local.pojo.WorkoutDetails
import com.bhanu.ironlog.data.repository.WorkoutSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class WorkoutDetailsViewModel @Inject constructor(
    private val sessionRepository: WorkoutSessionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val sessionId: Long = savedStateHandle.get<Long>("sessionId") ?: -1L
    val isArgumentValid = sessionId != -1L

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val workoutDetails: StateFlow<WorkoutDetails?> = if (isArgumentValid) {
        combine(
            sessionRepository.getSessionById(sessionId).filterNotNull(),
            sessionRepository.getWorkoutCompletionSummary(sessionId).filterNotNull(),
            sessionRepository.getHistoricalExercisesWithSets(sessionId)
        ) { session, summary, exercises ->
            WorkoutDetails(session, summary, exercises)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    } else {
        MutableStateFlow(null)
    }
}
