package com.bhanu.ironlog.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.ironlog.data.local.entity.WorkoutSession
import com.bhanu.ironlog.data.local.pojo.RestTimerInfo
import com.bhanu.ironlog.data.repository.WorkoutSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionRepository: WorkoutSessionRepository
) : ViewModel() {
    val activeSession: StateFlow<WorkoutSession?> = sessionRepository.activeWorkoutSession

    @OptIn(ExperimentalCoroutinesApi::class)
    val restTimer: StateFlow<RestTimerInfo?> = activeSession.flatMapLatest { session ->
        if (session != null) {
            sessionRepository.getActiveRestTimer(session.sessionId)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val workoutSettings = sessionRepository.getWorkoutSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun pauseTimer() {
        activeSession.value?.let {
            viewModelScope.launch { sessionRepository.pauseRestTimer(it.sessionId) }
        }
    }

    fun resumeTimer() {
        activeSession.value?.let {
            viewModelScope.launch { sessionRepository.resumeRestTimer(it.sessionId) }
        }
    }

    fun adjustTimer(seconds: Int) {
        activeSession.value?.let {
            viewModelScope.launch { sessionRepository.adjustRestTimer(it.sessionId, seconds) }
        }
    }

    fun skipTimer() {
        activeSession.value?.let {
            viewModelScope.launch { sessionRepository.dismissRestTimer(it.sessionId) }
        }
    }

    fun dismissTimer() {
        activeSession.value?.let {
            viewModelScope.launch { sessionRepository.dismissRestTimer(it.sessionId) }
        }
    }
}
