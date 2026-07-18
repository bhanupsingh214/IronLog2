package com.bhanu.ironlog.ui.screens.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.ironlog.data.local.entity.WorkoutSession
import com.bhanu.ironlog.data.local.pojo.SessionExerciseWithTemplate
import com.bhanu.ironlog.data.repository.ProgramRepository
import com.bhanu.ironlog.data.repository.WorkoutSessionRepository
import com.bhanu.ironlog.data.service.Achievement
import com.bhanu.ironlog.data.service.PersonalRecordEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

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
            if (session.status == "ACTIVE") {
                sessionRepository.getExercisesForActiveSession(session.sessionId, dayId)
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

    private val _timerSeconds = MutableStateFlow(0L)
    val timerSeconds = _timerSeconds.asStateFlow()

    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements = _achievements.asStateFlow()

    private val _showCelebration = MutableStateFlow(false)
    val showCelebration = _showCelebration.asStateFlow()

    private var timerJob: Job? = null

    init {
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1.seconds)
                _timerSeconds.value++
            }
        }
    }

    fun toggleExerciseCompletion(exerciseId: Long) {
        viewModelScope.launch {
            val currentSession = session.value ?: return@launch
            val completedIds = currentSession.completedExerciseIds.split(",")
                .filter { it.isNotBlank() }
                .toMutableSet()
            
            val idStr = exerciseId.toString()
            if (completedIds.contains(idStr)) {
                completedIds.remove(idStr)
            } else {
                completedIds.add(idStr)
            }
            
            sessionRepository.updateSession(currentSession.copy(
                completedExerciseIds = completedIds.joinToString(",")
            ))
        }
    }

    fun finishWorkout() {
        timerJob?.cancel()
        viewModelScope.launch {
            // 1. Process PRs before closing session
            val newAchievements = prEngine.processSessionPRs(sessionId)
            
            // 2. Finish Session
            sessionRepository.finishSession(sessionId)
            
            // 3. Show celebration if needed
            if (newAchievements.isNotEmpty()) {
                _achievements.value = newAchievements
                _showCelebration.value = true
            } else {
                // If no PRs, we'll signal to navigate away
                _finishSignal.emit(Unit)
            }
        }
    }

    private val _finishSignal = MutableSharedFlow<Unit>()
    val finishSignal = _finishSignal.asSharedFlow()

    fun onCelebrationDismissed() {
        _showCelebration.value = false
        viewModelScope.launch {
            _finishSignal.emit(Unit)
        }
    }
}
