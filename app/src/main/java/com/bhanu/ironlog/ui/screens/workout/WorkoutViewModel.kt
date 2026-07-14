package com.bhanu.ironlog.ui.screens.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.ironlog.data.local.entity.ExerciseEntity
import com.bhanu.ironlog.data.local.entity.WorkoutDayEntity
import com.bhanu.ironlog.data.local.pojo.ProgramWithStats
import com.bhanu.ironlog.data.local.pojo.WorkoutDayWithStats
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
class WorkoutViewModel @Inject constructor(
    private val repository: ProgramRepository
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

    fun pauseTimer() {
        timerJob?.cancel()
    }

    fun resumeTimer() {
        startTimer()
    }

    fun startSession(day: WorkoutDayEntity, onSessionStarted: (Long) -> Unit) {
        viewModelScope.launch {
            val program = activeProgram.value ?: return@launch
            val sessionId = repository.startWorkoutSession(
                dayId = day.id,
                dayName = day.name,
                programName = program.program.name
            )
            onSessionStarted(sessionId)
        }
    }
}
