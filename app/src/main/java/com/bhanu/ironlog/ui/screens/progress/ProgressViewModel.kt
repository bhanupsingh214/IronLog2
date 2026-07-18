package com.bhanu.ironlog.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.ironlog.data.local.pojo.PRWithExerciseName
import com.bhanu.ironlog.data.repository.AnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    val uiState: StateFlow<ProgressUiState> = combine(
        analyticsRepository.getTotalWorkoutsCount(),
        analyticsRepository.getTotalVolume(),
        analyticsRepository.getWeightPRCount(),
        analyticsRepository.getEstimated1RMPRCount(),
        analyticsRepository.getWeeklyVolume(),
        analyticsRepository.getMonthlyVolume(),
        analyticsRepository.getLatestPRs()
    ) { args: Array<Any> ->
        ProgressUiState.Success(
            totalWorkouts = args[0] as Int,
            totalVolume = args[1] as Double,
            weightPRCount = args[2] as Int,
            e1rmPRCount = args[3] as Int,
            weeklyVolume = args[4] as Double,
            monthlyVolume = args[5] as Double,
            latestPRs = args[6] as List<PRWithExerciseName>
        ) as ProgressUiState
    }.catch { e ->
        emit(ProgressUiState.Error(e.message ?: "Unknown Error"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProgressUiState.Loading
    )
}

sealed class ProgressUiState {
    object Loading : ProgressUiState()
    data class Success(
        val totalWorkouts: Int,
        val totalVolume: Double,
        val weightPRCount: Int,
        val e1rmPRCount: Int,
        val weeklyVolume: Double,
        val monthlyVolume: Double,
        val latestPRs: List<PRWithExerciseName>
    ) : ProgressUiState()
    data class Error(val message: String) : ProgressUiState()
}
