package com.bhanu.ironlog.ui.screens.programs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.ironlog.data.local.entity.WorkoutDayEntity
import com.bhanu.ironlog.data.local.pojo.WorkoutDayWithStats
import com.bhanu.ironlog.data.repository.ProgramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutDaysViewModel @Inject constructor(
    private val repository: ProgramRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val programId: Long = savedStateHandle.get<Long>("programId") ?: -1L

    val isArgumentValid = programId != -1L

    val workoutDays: StateFlow<List<WorkoutDayWithStats>> = if (isArgumentValid) {
        repository.getWorkoutDaysWithStats(programId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    } else {
        MutableStateFlow(emptyList())
    }

    fun addWorkoutDay(name: String, notes: String) {
        if (!isArgumentValid) return
        viewModelScope.launch {
            repository.insertWorkoutDay(programId, name, notes)
        }
    }

    fun updateWorkoutDay(day: WorkoutDayEntity) {
        viewModelScope.launch {
            repository.updateWorkoutDay(day)
        }
    }

    fun deleteWorkoutDay(day: WorkoutDayEntity) {
        viewModelScope.launch {
            repository.deleteWorkoutDay(day)
        }
    }

    fun duplicateWorkoutDay(dayId: Long) {
        viewModelScope.launch {
            repository.duplicateWorkoutDay(dayId)
        }
    }

    fun moveDayUp(dayId: Long) {
        viewModelScope.launch {
            repository.moveDay(dayId, up = true)
        }
    }

    fun moveDayDown(dayId: Long) {
        viewModelScope.launch {
            repository.moveDay(dayId, up = false)
        }
    }
}
