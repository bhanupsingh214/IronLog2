package com.bhanu.ironlog.ui.screens.records

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.ironlog.data.local.entity.ExerciseEntity
import com.bhanu.ironlog.data.local.pojo.ExerciseStrengthHistory
import com.bhanu.ironlog.data.repository.PersonalRecordRepository
import com.bhanu.ironlog.data.repository.ProgramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class RecordDetailViewModel @Inject constructor(
    private val prRepository: PersonalRecordRepository,
    private val programRepository: ProgramRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val exerciseId: Long = savedStateHandle.get<Long>("exerciseId") ?: -1L
    val isArgumentValid = exerciseId != -1L

    private val _isE1RMToggle = MutableStateFlow(false)
    val isE1RMToggle = _isE1RMToggle.asStateFlow()

    val exercise: StateFlow<ExerciseEntity?> = if (isArgumentValid) {
        programRepository.getExercise(exerciseId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    } else {
        MutableStateFlow(null)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val history: StateFlow<List<ExerciseStrengthHistory>> = _isE1RMToggle.flatMapLatest { isE1RM ->
        if (isArgumentValid) {
            prRepository.getPRProgression(exerciseId, isE1RM)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<RecordDetailUiState> = history.map { prList ->
        if (prList.isEmpty()) {
            RecordDetailUiState.Success(null, null, null)
        } else {
            val sorted = prList.sortedByDescending { it.date }
            val current = sorted.first()
            val previous = sorted.getOrNull(1)
            
            val summary = RecordSummary(
                totalPRs = prList.size,
                firstPRDate = prList.minOf { it.date },
                latestPRDate = prList.maxOf { it.date },
                biggestWeightImprovement = if (prList.size >= 2) {
                    prList.maxOf { it.maxWeight } - prList.minOf { it.maxWeight }
                } else 0.0
            )
            
            RecordDetailUiState.Success(current, previous, summary) as RecordDetailUiState
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RecordDetailUiState.Loading)

    fun toggleE1RM(enabled: Boolean) {
        _isE1RMToggle.value = enabled
    }
}

data class RecordSummary(
    val totalPRs: Int,
    val firstPRDate: Long,
    val latestPRDate: Long,
    val biggestWeightImprovement: Double
)

sealed class RecordDetailUiState {
    object Loading : RecordDetailUiState()
    data class Success(
        val current: ExerciseStrengthHistory?,
        val previous: ExerciseStrengthHistory?,
        val summary: RecordSummary?
    ) : RecordDetailUiState()
}
