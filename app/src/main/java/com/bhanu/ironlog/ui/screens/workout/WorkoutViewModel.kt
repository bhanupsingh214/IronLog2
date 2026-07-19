package com.bhanu.ironlog.ui.screens.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.ironlog.data.local.entity.ExerciseEntity
import com.bhanu.ironlog.data.local.entity.WorkoutDayEntity
import com.bhanu.ironlog.data.local.entity.WorkoutSession
import com.bhanu.ironlog.data.local.pojo.ProgramWithStats
import com.bhanu.ironlog.data.local.pojo.WorkoutDayWithStats
import com.bhanu.ironlog.data.repository.ProgramRepository
import com.bhanu.ironlog.data.repository.WorkoutSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val repository: ProgramRepository,
    private val sessionRepository: WorkoutSessionRepository
) : ViewModel() {

    val activeProgram: StateFlow<ProgramWithStats?> = repository.getActiveProgram()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val workoutDays: StateFlow<List<WorkoutDayWithStats>> = activeProgram.flatMapLatest { program ->
        if (program != null) {
            repository.getWorkoutDaysWithStats(program.program.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val activeSession: StateFlow<WorkoutSession?> = sessionRepository.activeWorkoutSession

    fun discardSession(sessionId: Long) {
        viewModelScope.launch {
            sessionRepository.discardSession(sessionId)
        }
    }

    fun startSession(day: WorkoutDayEntity, onSessionStarted: (Long) -> Unit) {
        viewModelScope.launch {
            val program = activeProgram.value ?: return@launch
            val sessionId = sessionRepository.getOrCreateSession(
                dayId = day.id,
                programId = program.program.id
            )
            onSessionStarted(sessionId)
        }
    }
}
