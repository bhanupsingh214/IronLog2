package com.bhanu.ironlog.ui.screens.programs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.ironlog.data.local.entity.WorkoutDayEntity
import com.bhanu.ironlog.data.local.pojo.WorkoutDayWithStats
import com.bhanu.ironlog.data.repository.ProgramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

    val programId: Long = checkNotNull(savedStateHandle["programId"])

    val workoutDays: StateFlow<List<WorkoutDayWithStats>> = repository.getWorkoutDaysWithStats(programId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addWorkoutDay(name: String, notes: String) {
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
