package com.bhanu.ironlog.ui.screens.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.ironlog.data.local.entity.ExerciseEntity
import com.bhanu.ironlog.data.local.entity.WorkoutSessionEntity
import com.bhanu.ironlog.data.repository.ProgramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class SessionExercisesViewModel @Inject constructor(
    private val repository: ProgramRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val dayId: Long = savedStateHandle.get<Long>("dayId") ?: -1L
    val sessionId: Long = savedStateHandle.get<Long>("sessionId") ?: -1L

    val isArgumentValid = dayId != -1L && sessionId != -1L

    @OptIn(ExperimentalCoroutinesApi::class)
    val session: StateFlow<WorkoutSessionEntity?> = if (isArgumentValid) {
        repository.getSession(sessionId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    } else {
        MutableStateFlow(null)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val exercises: StateFlow<List<ExerciseEntity>> = session.flatMapLatest { session ->
        if (session != null) {
            repository.getExercisesForDay(session.dayId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _timerSeconds = MutableStateFlow(0L)
    val timerSeconds = _timerSeconds.asStateFlow()

    private var timerJob: Job? = null

    init {
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1.seconds)
                _timerSeconds.value++
            }
        }
    }

    fun toggleExerciseCompletion(exerciseId: Long) {
        viewModelScope.launch {
            val currentSession = session.value ?: return@launch
            val completedIds = currentSession.completedExerciseIds.split(",")
                .filter { it.isNotBlank() }
                .toMutableSet()
            
            val idStr = exerciseId.toString()
            if (completedIds.contains(idStr)) {
                completedIds.remove(idStr)
            } else {
                completedIds.add(idStr)
            }
            
            repository.updateWorkoutSession(currentSession.copy(
                completedExerciseIds = completedIds.joinToString(",")
            ))
        }
    }

    fun finishWorkout() {
        viewModelScope.launch {
            repository.finishWorkoutSession(sessionId, _timerSeconds.value)
        }
    }
}
