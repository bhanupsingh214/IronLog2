package com.bhanu.ironlog.ui.screens.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.ironlog.data.local.entity.WorkoutSession
import com.bhanu.ironlog.data.local.pojo.SessionExerciseWithTemplate
import com.bhanu.ironlog.data.local.pojo.WorkoutCompletionSummary
import com.bhanu.ironlog.data.local.pojo.WorkoutProgress
import com.bhanu.ironlog.data.repository.ProgramRepository
import com.bhanu.ironlog.data.repository.WorkoutSessionRepository
import com.bhanu.ironlog.data.service.PersonalRecordEngine
import com.bhanu.ironlog.data.model.WorkoutSessionStatus
import com.bhanu.ironlog.data.model.workout.WorkoutSessionAggregate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionExercisesViewModel @Inject constructor(
    private val repository: ProgramRepository,
    private val sessionRepository: WorkoutSessionRepository,
    private val prEngine: PersonalRecordEngine,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val dayId: Long = savedStateHandle.get<Long>("dayId") ?: -1L
    val sessionId: Long = savedStateHandle.get<Long>("sessionId") ?: -1L

    val isArgumentValid = dayId != -1L && sessionId != -1L

    @OptIn(ExperimentalCoroutinesApi::class)
    val aggregate: StateFlow<WorkoutSessionAggregate?> = if (isArgumentValid) {
        sessionRepository.getSessionAggregate(sessionId)
            .onEach { agg ->
                if (agg?.metadata?.status == WorkoutSessionStatus.CREATED) {
                    sessionRepository.resumeSession(sessionId)
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    } else {
        MutableStateFlow(null)
    }

    // Compatibility properties for current UI implementation
    val session: StateFlow<WorkoutSession?> = aggregate.map { it?.metadata?.toEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val exercises: StateFlow<List<SessionExerciseWithTemplate>> = aggregate.map { agg ->
        agg?.exercises?.map { it.toPOJO() } ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val timerSeconds: StateFlow<Long> = aggregate.flatMapLatest { agg ->
        if (agg == null || (agg.metadata.status != WorkoutSessionStatus.IN_PROGRESS && agg.metadata.status != WorkoutSessionStatus.CREATED)) {
            flowOf(agg?.metadata?.durationSeconds ?: 0L)
        } else {
            flow {
                while (true) {
                    val duration = (System.currentTimeMillis() - agg.metadata.startTime) / 1000
                    emit(duration)
                    delay(1000)
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    @OptIn(ExperimentalCoroutinesApi::class)
    val progress: StateFlow<WorkoutProgress?> = aggregate.map { agg ->
        agg?.let {
            WorkoutProgress(
                completedExercises = it.statistics.completedExercisesCount,
                totalExercises = it.statistics.totalExercisesCount,
                completedSets = it.statistics.completedSetsCount,
                totalSets = it.statistics.totalSetsCount,
                percentage = if (it.statistics.totalExercisesCount > 0) 
                    it.statistics.completedExercisesCount.toFloat() / it.statistics.totalExercisesCount 
                else 0f
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _showBackgroundDialog = MutableStateFlow(false)
    val showBackgroundDialog = _showBackgroundDialog.asStateFlow()

    private val _completionState = MutableStateFlow(WorkoutCompletionState.ACTIVE)
    val completionState = _completionState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val completionSummary: StateFlow<WorkoutCompletionSummary?> = completionState.flatMapLatest { state ->
        if (state == WorkoutCompletionState.COMPLETED || state == WorkoutCompletionState.CONFIRMING_FINISH) {
            sessionRepository.getWorkoutCompletionSummary(sessionId)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun initiateFinish() {
        _completionState.value = WorkoutCompletionState.CONFIRMING_FINISH
    }

    fun cancelFinish() {
        _completionState.value = WorkoutCompletionState.ACTIVE
    }

    fun finishWorkout() {
        viewModelScope.launch {
            prEngine.processSessionPRs(sessionId)
            sessionRepository.finishSession(sessionId)
            _completionState.value = WorkoutCompletionState.COMPLETED
        }
    }

    fun dismissSummary() {
        _completionState.value = WorkoutCompletionState.SUMMARY_DISMISSED
        viewModelScope.launch {
            _finishSignal.emit(Unit)
        }
    }

    fun onLeaveSession() {
        viewModelScope.launch {
            val sess = aggregate.value?.metadata ?: return@launch
            if (sess.status != WorkoutSessionStatus.COMPLETED && sess.status != WorkoutSessionStatus.DISCARDED) {
                _showBackgroundDialog.value = true
            } else {
                _finishSignal.emit(Unit)
            }
        }
    }

    fun onBackgroundDialogConfirm(stay: Boolean) {
        _showBackgroundDialog.value = false
        if (!stay) {
            viewModelScope.launch {
                val agg = aggregate.value ?: return@launch
                sessionRepository.updateSession(agg.metadata.toEntity().copy(hasShownBackgroundDialog = true))
                _finishSignal.emit(Unit)
            }
        }
    }

    fun discardWorkout() {
        viewModelScope.launch {
            sessionRepository.discardSession(sessionId)
            _finishSignal.emit(Unit)
        }
    }

    fun skipExercise(exerciseId: Long) {
        viewModelScope.launch {
            sessionRepository.skipExercise(sessionId, exerciseId)
        }
    }

    fun completeExercise(exerciseId: Long) {
        viewModelScope.launch {
            sessionRepository.completeExercise(sessionId, exerciseId)
        }
    }

    fun toggleExerciseCompletion(exerciseId: Long) {
        viewModelScope.launch {
            sessionRepository.toggleExerciseCompletion(sessionId, exerciseId)
        }
    }

    private val _finishSignal = MutableSharedFlow<Unit>()
    val finishSignal = _finishSignal.asSharedFlow()

    fun onCelebrationDismissed() {
        dismissSummary()
    }
}
