package com.bhanu.ironlog.ui.screens.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.ironlog.data.local.pojo.ExerciseDetails
import com.bhanu.ironlog.data.repository.WorkoutSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ExerciseDetailsViewModel @Inject constructor(
    private val sessionRepository: WorkoutSessionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val libraryExerciseId: Long = savedStateHandle.get<Long>("exerciseId") ?: -1L
    val isArgumentValid = libraryExerciseId != -1L

    val uiState: StateFlow<ExerciseDetailsUiState> = if (isArgumentValid) {
        sessionRepository.getExerciseDetails(libraryExerciseId)
            .map { details ->
                if (details == null) ExerciseDetailsUiState.Empty
                else ExerciseDetailsUiState.Success(details)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ExerciseDetailsUiState.Loading
            )
    } else {
        MutableStateFlow(ExerciseDetailsUiState.Error("Invalid Exercise ID"))
    }
}

sealed class ExerciseDetailsUiState {
    object Loading : ExerciseDetailsUiState()
    data class Success(val details: ExerciseDetails) : ExerciseDetailsUiState()
    object Empty : ExerciseDetailsUiState()
    data class Error(val message: String) : ExerciseDetailsUiState()
}
