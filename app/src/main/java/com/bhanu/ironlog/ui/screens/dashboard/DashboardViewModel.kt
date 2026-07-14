package com.bhanu.ironlog.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.ironlog.data.local.entity.WorkoutSessionEntity
import com.bhanu.ironlog.data.local.pojo.ProgramWithStats
import com.bhanu.ironlog.data.local.pojo.WorkoutDayWithStats
import com.bhanu.ironlog.data.repository.ProgramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val programRepository: ProgramRepository
) : ViewModel() {

    val activeProgram: StateFlow<ProgramWithStats?> = programRepository.getActiveProgram()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val todayWorkout: StateFlow<WorkoutDayWithStats?> = activeProgram.flatMapLatest { program ->
        if (program != null) {
            programRepository.getWorkoutDaysWithStats(program.program.id).map { days ->
                days.firstOrNull { it.day.isEnabled }
            }
        } else {
            flowOf(null)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeProgramDays: StateFlow<List<WorkoutDayWithStats>> = activeProgram.flatMapLatest { program ->
        if (program != null) {
            programRepository.getWorkoutDaysWithStats(program.program.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val recentHistory: StateFlow<List<WorkoutSessionEntity>> = programRepository.getCompletedSessions()
        .map { it.take(5) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val weeklyVolume: StateFlow<Double> = programRepository.getWeeklyVolume()
        .map { it ?: 0.0 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val personalRecords: StateFlow<List<Pair<String, Double>>> = programRepository.getTopPersonalRecords()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _navigateToWorkout = MutableSharedFlow<Unit>()
    val navigateToWorkout = _navigateToWorkout.asSharedFlow()

    fun onStartWorkout() {
        viewModelScope.launch {
            _navigateToWorkout.emit(Unit)
        }
    }

    fun onAddHistoricalSession(dayId: Long, date: Long) {
        viewModelScope.launch {
            val program = activeProgram.value ?: return@launch
            
            // Wait for days to load if they are empty
            val days = if (activeProgramDays.value.isEmpty()) {
                programRepository.getWorkoutDaysWithStats(program.program.id).first()
            } else {
                activeProgramDays.value
            }

            val day = days.find { it.day.id == dayId } ?: return@launch
            
            val sessionId = programRepository.startWorkoutSession(
                dayId = dayId,
                dayName = day.day.name,
                programName = program.program.name
            )
            
            _navigateToSession.emit(dayId to sessionId) 
        }
    }

    fun onNavigationHandled() {
        viewModelScope.launch {
            _navigateToSession.emit(0L to 0L) // Reset or handle otherwise
        }
    }

    private val _navigateToSession = MutableSharedFlow<Pair<Long, Long>>(replay = 1)
    val navigateToSession = _navigateToSession.asSharedFlow()
}
