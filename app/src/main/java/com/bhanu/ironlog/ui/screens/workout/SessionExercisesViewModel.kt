package com.bhanu.ironlog.ui.screens.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.ironlog.data.local.entity.WorkoutSession
import com.bhanu.ironlog.data.local.pojo.SessionExerciseWithTemplate
import com.bhanu.ironlog.data.local.pojo.WorkoutProgress
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
        if (sess == null || sess.status != "ACTIVE") {
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
        if (sess != null && sess.status == "ACTIVE") {
            sessionRepository.getWorkoutProgress(sess.sessionId)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements = _achievements.asStateFlow()

    private val _showCelebration = MutableStateFlow(false)
    val showCelebration = _showCelebration.asStateFlow()

    private val _showBackgroundDialog = MutableStateFlow(false)
    val showBackgroundDialog = _showBackgroundDialog.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val finishSummary: StateFlow<WorkoutSummary?> = session.flatMapLatest { sess ->
        if (sess == null) return@flatMapLatest flowOf(null)
        
        sessionRepository.getExercisesWithSetsForSession(sess.sessionId).map { exerciseList ->
            val totalVolume = exerciseList.sumOf { ex -> ex.sets.sumOf { it.weight * it.reps } }
            val totalSets = exerciseList.sumOf { it.sets.size }
            val completedExercises = sess.completedExerciseIds.split(",").filter { it.isNotBlank() }.size
            
            WorkoutSummary(
                durationSeconds = (System.currentTimeMillis() - sess.startTime) / 1000,
                completedExercises = completedExercises,
                totalSets = totalSets,
                totalVolume = totalVolume,
                startTime = sess.startTime,
                endTime = System.currentTimeMillis()
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun onLeaveSession() {
        viewModelScope.launch {
            val sess = session.value ?: return@launch
            if (sess.status == "ACTIVE" && !sess.hasShownBackgroundDialog) {
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
            val currentSession = session.value ?: return@launch
            val completedIds = currentSession.completedExerciseIds.split(",")
                .filter { it.isNotBlank() }
                .toMutableSet()
            
            val idStr = exerciseId.toString()
            val newStatus: String
            if (completedIds.contains(idStr)) {
                completedIds.remove(idStr)
                newStatus = "PLANNED"
            } else {
                completedIds.add(idStr)
                newStatus = "COMPLETED"
            }
            
            // 1. Update Session metadata
            sessionRepository.updateSession(currentSession.copy(
                completedExerciseIds = completedIds.joinToString(",")
            ))

            // 2. Update individual exercise snapshot
            val sessionExercise = sessionRepository.getSessionExercise(sessionId, exerciseId)
            if (sessionExercise != null) {
                sessionRepository.updateSessionExerciseStatus(sessionExercise.sessionExerciseId, newStatus)
            }
        }
    }

    fun finishWorkout() {
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

data class WorkoutSummary(
    val durationSeconds: Long,
    val completedExercises: Int,
    val totalSets: Int,
    val totalVolume: Double,
    val startTime: Long,
    val endTime: Long
)
