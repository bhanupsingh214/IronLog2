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
    val session: StateFlow<WorkoutSession?> = if (isArgumentValid) {
        sessionRepository.getSessionById(sessionId)
            .onEach { sess ->
                // Ensure session is IN_PROGRESS if we are on this screen and it was CREATED
                if (sess?.status == WorkoutSessionStatus.CREATED) {
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

    @OptIn(ExperimentalCoroutinesApi::class)
    val exercises: StateFlow<List<SessionExerciseWithTemplate>> = session.flatMapLatest { session ->
        if (session != null) {
            if (session.status != WorkoutSessionStatus.COMPLETED && session.status != WorkoutSessionStatus.DISCARDED) {
                sessionRepository.getExercisesForActiveSession(session.sessionId)
            } else {
                sessionRepository.getExercisesWithTemplateForSession(session.sessionId)
            }
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val timerSeconds: StateFlow<Long> = session.flatMapLatest { sess ->
        if (sess == null || (sess.status != WorkoutSessionStatus.IN_PROGRESS && sess.status != WorkoutSessionStatus.CREATED)) {
            flowOf(sess?.durationSeconds ?: 0L)
        } else {
            flow {
                while (true) {
                    val duration = (System.currentTimeMillis() - sess.startTime) / 1000
                    emit(duration)
                    delay(1000)
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    @OptIn(ExperimentalCoroutinesApi::class)
    val progress: StateFlow<WorkoutProgress?> = session.flatMapLatest { sess ->
        if (sess != null && sess.status != WorkoutSessionStatus.COMPLETED && sess.status != WorkoutSessionStatus.DISCARDED) {
            sessionRepository.getWorkoutProgress(sess.sessionId)
        } else {
            flowOf(null)
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
            // 1. Process PRs (existing engine - internal cache/records)
            prEngine.processSessionPRs(sessionId)
            
            // 2. Finish Session in DB
            sessionRepository.finishSession(sessionId)
            
            // 3. Move to completed state
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
            val sess = session.value ?: return@launch
            if (sess.status != WorkoutSessionStatus.COMPLETED && sess.status != WorkoutSessionStatus.DISCARDED && !sess.hasShownBackgroundDialog) {
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
                val sess = session.value ?: return@launch
                sessionRepository.updateSession(sess.copy(hasShownBackgroundDialog = true))
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
