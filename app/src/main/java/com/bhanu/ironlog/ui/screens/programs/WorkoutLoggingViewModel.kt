package com.bhanu.ironlog.ui.screens.programs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.ironlog.data.local.entity.ExerciseEntity
import com.bhanu.ironlog.data.local.entity.SessionExercise
import com.bhanu.ironlog.data.local.entity.SessionSet
import com.bhanu.ironlog.data.local.entity.SetEntity
import com.bhanu.ironlog.data.local.entity.WorkoutSession
import com.bhanu.ironlog.data.repository.ProgramRepository
import com.bhanu.ironlog.data.repository.WorkoutSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutLoggingViewModel @Inject constructor(
    private val repository: ProgramRepository,
    private val sessionRepository: WorkoutSessionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val exerciseId: Long = savedStateHandle.get<Long>("exerciseId") ?: -1L
    val sessionId: Long = savedStateHandle.get<Long>("sessionId") ?: -1L

    val isArgumentValid = exerciseId != -1L && sessionId != -1L

    val exercise: StateFlow<ExerciseEntity?> = if (isArgumentValid) {
        repository.getExercise(exerciseId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    } else {
        MutableStateFlow(null)
    }

    val sessionExercise: StateFlow<SessionExercise?> = if (isArgumentValid && sessionId > 0) {
        flow {
            val se = sessionRepository.getSessionExercise(sessionId, exerciseId)
            emit(se)
            // In a more complex app, I'd want this to be a Flow from DAO
            // For now, I'll just fetch it once or use a state flow that I update manually
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    } else {
        MutableStateFlow(null)
    }

    val sets: StateFlow<List<WorkoutSetUiModel>> = if (isArgumentValid) {
        if (sessionId == 0L) {
            repository.getSetsForExercise(exerciseId, 0L).map { list ->
                list.map { it.toUiModel() }
            }
        } else {
            // Need to find sessionExerciseId
            flow {
                val sessionExercise = sessionRepository.getSessionExercise(sessionId, exerciseId)
                if (sessionExercise != null) {
                    sessionRepository.getSetsForExercise(sessionExercise.sessionExerciseId).map { list ->
                        list.map { it.toUiModel() }
                    }.collect { emit(it) }
                } else {
                    emit(emptyList())
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    } else {
        MutableStateFlow(emptyList())
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val session: StateFlow<WorkoutSession?> = if (isArgumentValid) {
        if (sessionId == 0L) {
            MutableStateFlow(null)
        } else {
            sessionRepository.getSessionById(sessionId)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = null
                )
        }
    } else {
        MutableStateFlow(null)
    }

    val previousSets: StateFlow<List<WorkoutSetUiModel>> = if (isArgumentValid) {
        repository.getPreviousSets(exerciseId, sessionId).map { list ->
            list.map { it.toUiModel() }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    } else {
        MutableStateFlow(emptyList())
    }

    val previousPerformance: StateFlow<com.bhanu.ironlog.data.local.pojo.SessionExerciseWithSetsAndSession?> = if (isArgumentValid && sessionId > 0) {
        sessionRepository.getPreviousPerformance(exerciseId, sessionId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    } else {
        MutableStateFlow(null)
    }

    private val _navigationEvent = MutableSharedFlow<LoggingNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    init {
        updateEngineState()
        setExerciseInProgress()
    }

    private fun setExerciseInProgress() {
        if (sessionId > 0) {
            viewModelScope.launch {
                val sessionExercise = sessionRepository.getSessionExercise(sessionId, exerciseId)
                if (sessionExercise != null && sessionExercise.status == "NOT_STARTED") {
                    sessionRepository.updateSessionExerciseStatus(sessionExercise.sessionExerciseId, "IN_PROGRESS")
                }
            }
        }
    }

    fun addSet(setType: String = "Working") {
        if (!isArgumentValid) return
        viewModelScope.launch {
            if (sessionId == 0L) {
                repository.insertSet(
                    SetEntity(
                        exerciseId = exerciseId,
                        sessionId = sessionId,
                        setType = setType,
                        order = 0
                    )
                )
            } else {
                val sessionExercise = sessionRepository.getSessionExercise(sessionId, exerciseId)
                if (sessionExercise != null) {
                    val currentSets = sessionRepository.getSetsForExercise(sessionExercise.sessionExerciseId).first()
                    sessionRepository.insertSessionSet(
                        SessionSet(
                            sessionExerciseId = sessionExercise.sessionExerciseId,
                            setNumber = currentSets.size + 1,
                            weight = 0.0,
                            reps = 0,
                            rpe = null,
                            setType = setType,
                            completed = false
                        )
                    )
                    updateEngineState(currentSets.size + 1)
                    viewModelScope.launch {
                        sessionRepository.dismissRestTimer(sessionId)
                    }
                }
            }
        }
    }

    fun updateExerciseNotes(newNotes: String) {
        if (sessionId > 0) {
            viewModelScope.launch {
                val sessionExercise = sessionRepository.getSessionExercise(sessionId, exerciseId)
                if (sessionExercise != null) {
                    sessionRepository.updateSessionExerciseNotes(sessionExercise.sessionExerciseId, newNotes)
                }
            }
        }
    }

    private fun updateEngineState(setNumber: Int? = null) {
        if (sessionId > 0) {
            viewModelScope.launch {
                val currentSets = sessionRepository.getSetsForExercise(
                    sessionRepository.getSessionExercise(sessionId, exerciseId)?.sessionExerciseId ?: return@launch
                ).first()
                val completedCount = currentSets.count { it.completed }
                sessionRepository.updateEngineState(sessionId, exerciseId, setNumber, completedCount)
            }
        }
    }

    fun copyPreviousSet() {
        if (!isArgumentValid) return
        viewModelScope.launch {
            if (sessionId == 0L) {
                val currentSets = repository.getSetsForExercise(exerciseId, 0L).first()
                if (currentSets.isNotEmpty()) {
                    val lastSet = currentSets.last()
                    repository.insertSet(
                        lastSet.copy(
                            id = 0,
                            isCompleted = false,
                            createdAt = System.currentTimeMillis()
                        )
                    )
                } else {
                    addSet()
                }
            } else {
                val sessionExercise = sessionRepository.getSessionExercise(sessionId, exerciseId)
                if (sessionExercise != null) {
                    val currentSets = sessionRepository.getSetsForExercise(sessionExercise.sessionExerciseId).first()
                    if (currentSets.isNotEmpty()) {
                        val lastSet = currentSets.last()
                        sessionRepository.insertSessionSet(
                            lastSet.copy(
                                sessionSetId = 0,
                                setNumber = currentSets.size + 1,
                                completed = false,
                                createdAt = System.currentTimeMillis()
                            )
                        )
                        viewModelScope.launch {
                            sessionRepository.dismissRestTimer(sessionId)
                        }
                    } else {
                        addSet()
                    }
                }
            }
        }
    }

    fun updateSet(uiModel: WorkoutSetUiModel) {
        viewModelScope.launch {
            if (sessionId == 0L) {
                val original = repository.getSetsForExercise(exerciseId, 0L).first().find { it.id == uiModel.id }
                original?.let {
                    repository.updateSet(it.copy(
                        weight = uiModel.weight.coerceAtLeast(0.0),
                        reps = uiModel.reps.coerceAtLeast(0),
                        rpe = uiModel.rpe?.coerceIn(0.0, 10.0),
                        notes = uiModel.notes,
                        isCompleted = uiModel.isCompleted
                    ))
                }
            } else {
                sessionRepository.updateSet(
                    setId = uiModel.id,
                    weight = uiModel.weight,
                    reps = uiModel.reps,
                    rpe = uiModel.rpe,
                    notes = uiModel.notes
                )
                
                val original = sessionRepository.getSetsForExercise(
                    sessionRepository.getSessionExercise(sessionId, exerciseId)?.sessionExerciseId ?: return@launch
                ).first().find { it.sessionSetId == uiModel.id }
                
                if (original != null && original.completed != uiModel.isCompleted) {
                    sessionRepository.toggleSetCompletion(sessionId, uiModel.id)
                }
            }
        }
    }

    fun deleteSet(uiModel: WorkoutSetUiModel) {
        viewModelScope.launch {
            if (sessionId == 0L) {
                val original = repository.getSetsForExercise(exerciseId, 0L).first().find { it.id == uiModel.id }
                original?.let { repository.deleteSet(it) }
            } else {
                val sessionExercise = sessionRepository.getSessionExercise(sessionId, exerciseId)
                if (sessionExercise != null) {
                    val original = sessionRepository.getSetsForExercise(sessionExercise.sessionExerciseId).first().find { it.sessionSetId == uiModel.id }
                    original?.let { 
                        sessionRepository.deleteSessionSet(it)
                        updateEngineState()
                    }
                }
            }
        }
    }

    fun duplicateSet(setId: Long) {
        viewModelScope.launch {
            if (sessionId == 0L) {
                repository.duplicateSet(setId)
            } else {
                sessionRepository.duplicateSessionSet(setId)
            }
        }
    }

    fun moveSetUp(setId: Long) {
        viewModelScope.launch {
            if (sessionId == 0L) {
                repository.moveSet(setId, up = true)
            } else {
                sessionRepository.moveSessionSet(setId, up = true)
            }
        }
    }

    fun moveSetDown(setId: Long) {
        viewModelScope.launch {
            if (sessionId == 0L) {
                repository.moveSet(setId, up = false)
            } else {
                sessionRepository.moveSessionSet(setId, up = false)
            }
        }
    }

    fun moveToNextExercise() {
        if (sessionId > 0) {
            viewModelScope.launch {
                sessionRepository.moveToNextExercise(sessionId)
                val sess = sessionRepository.getSessionById(sessionId).first()
                sess?.currentExerciseId?.let { nextId ->
                    if (nextId != exerciseId) {
                        _navigationEvent.emit(LoggingNavigationEvent.NavigateToExercise(nextId))
                    }
                }
            }
        }
    }

    fun moveToPreviousExercise() {
        if (sessionId > 0) {
            viewModelScope.launch {
                sessionRepository.moveToPreviousExercise(sessionId)
                val sess = sessionRepository.getSessionById(sessionId).first()
                sess?.currentExerciseId?.let { prevId ->
                    if (prevId != exerciseId) {
                        _navigationEvent.emit(LoggingNavigationEvent.NavigateToExercise(prevId))
                    }
                }
            }
        }
    }

    fun skipExercise() {
        if (sessionId > 0) {
            viewModelScope.launch {
                sessionRepository.skipExercise(sessionId, exerciseId)
                handleAdvance()
            }
        }
    }

    fun completeExercise() {
        if (sessionId > 0) {
            viewModelScope.launch {
                sessionRepository.completeExercise(sessionId, exerciseId)
                handleAdvance()
            }
        }
    }

    private suspend fun handleAdvance() {
        val sess = sessionRepository.getSessionById(sessionId).first()
        val nextId = sess?.currentExerciseId
        if (nextId != null) {
            _navigationEvent.emit(LoggingNavigationEvent.NavigateToExercise(nextId))
        } else {
            _navigationEvent.emit(LoggingNavigationEvent.WorkoutFinished)
        }
    }
}

sealed class LoggingNavigationEvent {
    data class NavigateToExercise(val exerciseId: Long) : LoggingNavigationEvent()
    object WorkoutFinished : LoggingNavigationEvent()
}

private fun SetEntity.toUiModel() = WorkoutSetUiModel(
    id = id,
    setNumber = setNumber,
    weight = weight,
    reps = reps,
    rpe = rpe,
    notes = notes,
    isCompleted = isCompleted,
    setType = setType
)

private fun SessionSet.toUiModel() = WorkoutSetUiModel(
    id = sessionSetId,
    setNumber = setNumber,
    weight = weight,
    reps = reps,
    rpe = rpe,
    notes = notes ?: "",
    isCompleted = completed,
    setType = setType
)
