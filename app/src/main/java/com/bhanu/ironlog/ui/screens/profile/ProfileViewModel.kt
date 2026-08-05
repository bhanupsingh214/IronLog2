package com.bhanu.ironlog.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.ironlog.data.local.entity.WorkoutSettingsEntity
import com.bhanu.ironlog.data.repository.WorkoutSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: WorkoutSessionRepository
) : ViewModel() {

    val settings: StateFlow<WorkoutSettingsEntity?> = repository.getWorkoutSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateSettings(newSettings: WorkoutSettingsEntity) {
        viewModelScope.launch {
            repository.updateWorkoutSettings(newSettings)
        }
    }
}
