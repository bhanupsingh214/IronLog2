package com.bhanu.ironlog.ui.screens

import androidx.lifecycle.ViewModel
import com.bhanu.ironlog.data.local.entity.WorkoutSession
import com.bhanu.ironlog.data.repository.WorkoutSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    sessionRepository: WorkoutSessionRepository
) : ViewModel() {
    val activeSession: StateFlow<WorkoutSession?> = sessionRepository.activeWorkoutSession
}
