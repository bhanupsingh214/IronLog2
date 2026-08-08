package com.bhanu.ironlog.data.repository

import com.bhanu.ironlog.data.local.dao.ProgramDao
import com.bhanu.ironlog.data.local.dao.WorkoutSessionDao
import com.bhanu.ironlog.data.local.entity.SessionExercise
import com.bhanu.ironlog.data.local.entity.SessionSet
import com.bhanu.ironlog.data.local.entity.WorkoutSession
import com.bhanu.ironlog.data.local.pojo.*
import com.bhanu.ironlog.data.model.RestTimerState
import com.bhanu.ironlog.data.model.WorkoutSessionStatus
import com.bhanu.ironlog.data.model.workout.WorkoutSessionAggregate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutSessionRepository @Inject constructor(
    private val workoutSessionDao: WorkoutSessionDao,
    private val programDao: ProgramDao,
    private val workoutSettingsDao: com.bhanu.ironlog.data.local.dao.WorkoutSettingsDao
) {
    private val repositoryScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.Main)

    val activeWorkoutSession: StateFlow<WorkoutSession?> = workoutSessionDao.getActiveSession()
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    /**
     * Exposes the active session as a domain aggregate.
     */
    val activeSessionAggregate: StateFlow<WorkoutSessionAggregate?> = workoutSessionDao.getActiveSessionAggregate()
        .map { it?.toAggregate() }
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun getWorkoutSettings(): Flow<com.bhanu.ironlog.data.local.entity.WorkoutSettingsEntity?> = 
        workoutSettingsDao.getSettings()

    suspend fun updateWorkoutSettings(settings: com.bhanu.ironlog.data.local.entity.WorkoutSettingsEntity) =
        workoutSettingsDao.updateSettings(settings)

    fun getAllSessions(): Flow<List<WorkoutSession>> = workoutSessionDao.getAllSessions()

    fun getSessionById(sessionId: Long): Flow<WorkoutSession?> = workoutSessionDao.getSessionById(sessionId)

    fun getSessionAggregate(sessionId: Long): Flow<WorkoutSessionAggregate?> = 
        workoutSessionDao.getSessionAggregate(sessionId).map { it?.toAggregate() }

    fun getActiveSessionFlow(): Flow<WorkoutSession?> = workoutSessionDao.getActiveSession()
    
    /**
     * Attempts to start a new workout session.
     * Returns the sessionId if successful, or -1 if an active session already exists.
     */
    suspend fun startWorkout(dayId: Long, programId: Long, startTime: Long = System.currentTimeMillis()): Long {
        val active = activeWorkoutSession.value
        if (active != null) return -1L
        
        return createSessionSnapshot(dayId, programId, startTime)
    }

    private suspend fun createSessionSnapshot(dayId: Long, programId: Long, startTime: Long): Long {
        val program = programDao.getProgramById(programId)
        val day = programDao.getDayById(dayId)

        val sessionId = workoutSessionDao.insertSession(
            WorkoutSession(
                programId = programId,
                workoutDayId = dayId,
                dayName = day?.name ?: "Unknown Day",
                programName = program?.name ?: "Unknown Program",
                status = WorkoutSessionStatus.CREATED,
                startTime = startTime,
                createdAt = startTime,
                lastActiveTimestamp = startTime
            )
        )

        val exercises = programDao.getEnabledExercisesForDay(dayId)
        for (exercise in exercises) {
            val sessionExerciseId = workoutSessionDao.insertSessionExercise(
                SessionExercise(
                    sessionId = sessionId,
                    exerciseTemplateId = exercise.id,
                    libraryExerciseId = exercise.libraryExerciseId,
                    exerciseName = exercise.name,
                    muscleGroup = exercise.muscleGroup,
                    equipment = exercise.equipment,
                    exerciseType = exercise.exerciseType,
                    exerciseOrder = exercise.order,
                    notes = exercise.notes,
                    status = "NOT_STARTED",
                    targetSets = exercise.targetSets,
                    targetRepMin = exercise.targetRepMin,
                    targetRepMax = exercise.targetRepMax,
                    targetRPE = exercise.targetRPE,
                    restTimerSeconds = if (exercise.useDefaultRestTimer) 90 else exercise.restTimerSeconds
                )
            )

            val templateSets = programDao.getSetsForExerciseAndSession(exercise.id, 0)
            for (set in templateSets) {
                workoutSessionDao.insertSessionSet(
                    SessionSet(
                        sessionExerciseId = sessionExerciseId,
                        setNumber = set.setNumber,
                        weight = set.weight,
                        reps = set.reps,
                        rpe = set.rpe,
                        setType = set.setType,
                        completed = false
                    )
                )
            }
        }
        
        if (exercises.isNotEmpty()) {
            workoutSessionDao.updateEngineState(sessionId, exercises.first().id, 1, 0)
        }

        return sessionId
    }

    suspend fun createHistoricalSession(dayId: Long, programId: Long, startTime: Long): Long {
        val sessionId = createSessionSnapshot(dayId, programId, startTime)
        finishSession(sessionId)
        return sessionId
    }

    suspend fun updateSessionStatus(sessionId: Long, newStatus: String) {
        val session = workoutSessionDao.getSessionByIdOnce(sessionId) ?: return
        
        // Basic state machine validation
        val isValid = when (newStatus) {
            WorkoutSessionStatus.IN_PROGRESS -> session.status in listOf(WorkoutSessionStatus.CREATED, WorkoutSessionStatus.PAUSED)
            WorkoutSessionStatus.PAUSED -> session.status == WorkoutSessionStatus.IN_PROGRESS
            WorkoutSessionStatus.COMPLETED -> session.status in listOf(WorkoutSessionStatus.IN_PROGRESS, WorkoutSessionStatus.PAUSED)
            WorkoutSessionStatus.DISCARDED -> session.status in listOf(WorkoutSessionStatus.CREATED, WorkoutSessionStatus.IN_PROGRESS, WorkoutSessionStatus.PAUSED)
            else -> false
        }
        
        if (isValid) {
            workoutSessionDao.updateSession(session.copy(
                status = newStatus,
                lastActiveTimestamp = System.currentTimeMillis()
            ))
        }
    }

    suspend fun resumeSession(sessionId: Long) = updateSessionStatus(sessionId, WorkoutSessionStatus.IN_PROGRESS)
    
    suspend fun pauseSession(sessionId: Long) = updateSessionStatus(sessionId, WorkoutSessionStatus.PAUSED)

    suspend fun discardSession(sessionId: Long) {
        val session = workoutSessionDao.getSessionByIdOnce(sessionId) ?: return
        workoutSessionDao.updateSession(session.copy(status = WorkoutSessionStatus.DISCARDED))
        workoutSessionDao.deleteSession(session)
    }

    suspend fun finishSession(sessionId: Long) {
        val session = workoutSessionDao.getSessionByIdOnce(sessionId) ?: return
        val endTime = System.currentTimeMillis()
        val isHistorical = session.startTime < endTime - 12 * 3600 * 1000
        val duration = if (isHistorical) 0L else (endTime - session.startTime) / 1000

        // Mark all unfinished exercises as SKIPPED
        val exercises = workoutSessionDao.getExercisesForSessionList(sessionId)
        for (exercise in exercises) {
            if (exercise.status != "COMPLETED" && exercise.status != "SKIPPED") {
                workoutSessionDao.updateSessionExerciseStatus(exercise.sessionExerciseId, "SKIPPED")
            }
        }

        workoutSessionDao.updateSession(session.copy(
            status = WorkoutSessionStatus.COMPLETED,
            endTime = endTime,
            durationSeconds = duration,
            timerState = "DISMISSED",
            timerStartTime = null,
            timerDurationSeconds = null,
            timerPausedRemainingSeconds = null,
            lastActiveTimestamp = endTime
        ))
    }

    fun getWorkoutCompletionSummary(sessionId: Long): Flow<WorkoutCompletionSummary?> =
        workoutSessionDao.getSessionAggregate(sessionId).map { aggregatePojo ->
            val aggregate = aggregatePojo?.toAggregate() ?: return@map null
            val stats = aggregate.statistics
            val currentSession = aggregate.metadata.toEntity()

            val achievements = if (aggregate.metadata.status == WorkoutSessionStatus.COMPLETED) {
                detectAchievements(sessionId)
            } else {
                emptyList()
            }

            val comparison = if (aggregate.metadata.status == WorkoutSessionStatus.COMPLETED) {
                getWorkoutComparison(currentSession)
            } else {
                null
            }

            WorkoutCompletionSummary(
                sessionId = sessionId,
                workoutName = aggregate.metadata.dayName,
                programName = aggregate.metadata.programName,
                durationSeconds = stats.durationSeconds,
                totalVolume = stats.totalVolume,
                exercisesCompleted = stats.completedExercisesCount,
                totalExercises = stats.totalExercisesCount,
                setsCompleted = stats.completedSetsCount,
                totalSets = stats.totalSetsCount,
                averageRPE = stats.averageRPE,
                skippedExercises = aggregate.exercises.count { it.execution.status == "SKIPPED" },
                completionPercentage = if (stats.totalExercisesCount > 0) stats.completedExercisesCount.toFloat() / stats.totalExercisesCount else 0f,
                startTime = aggregate.metadata.startTime,
                endTime = aggregate.metadata.endTime ?: System.currentTimeMillis(),
                achievements = achievements,
                comparison = comparison
            )
        }

    private suspend fun getWorkoutComparison(currentSession: WorkoutSession): WorkoutComparison? {
        val previousSession = workoutSessionDao.getLatestCompletedSessionBefore(currentSession.workoutDayId, currentSession.startTime)
        if (previousSession == null) {
            return WorkoutComparison(null, null, null, null, isFirstSession = true)
        }

        val prevAggregatePojo = workoutSessionDao.getSessionAggregate(previousSession.sessionId).first()
        val prevAggregate = prevAggregatePojo?.toAggregate() ?: return WorkoutComparison(null, null, null, null, isFirstSession = true)
            
        val stats = prevAggregate.statistics
        return WorkoutComparison(
            previousVolume = stats.totalVolume,
            previousDurationSeconds = stats.durationSeconds,
            previousSetsCompleted = stats.completedSetsCount,
            previousAverageRPE = stats.averageRPE,
            isFirstSession = false
        )
    }

    private suspend fun detectAchievements(sessionId: Long): List<PersonalRecordAchievement> {
        val achievements = mutableListOf<PersonalRecordAchievement>()
        val currentSession = workoutSessionDao.getSessionByIdOnce(sessionId) ?: return emptyList()
        val currentExercises = workoutSessionDao.getExercisesWithSetsForSessionList(sessionId)

        for (record in currentExercises) {
            val templateId = record.sessionExercise.exerciseTemplateId
            val exerciseName = record.sessionExercise.exerciseName
            
            if (record.sets.none { it.completed }) continue

            // Current Session Bests for this exercise
            val currentBestWeight = record.sets.filter { it.completed }.maxOfOrNull { it.weight } ?: 0.0
            val currentBestE1RM = record.sets.filter { it.completed }.maxOfOrNull { it.weight * (1 + it.reps / 30.0) } ?: 0.0
            val currentVolume = record.sets.filter { it.completed }.sumOf { it.weight * it.reps }

            // Previous Best values before this session
            val historicalRecords = workoutSessionDao.getCompletedExerciseRecordsBefore(templateId, currentSession.startTime)
            
            var prevBestWeight = 0.0
            var prevBestE1RM = 0.0
            var prevMaxVolume = 0.0
            
            for (hist in historicalRecords) {
                hist.sets.forEach { s ->
                    if (s.completed) {
                        if (s.weight > prevBestWeight) prevBestWeight = s.weight
                        val e1rm = s.weight * (1 + s.reps / 30.0)
                        if (e1rm > prevBestE1RM) prevBestE1RM = e1rm
                    }
                }
                val histVolume = hist.sets.filter { it.completed }.sumOf { it.weight * it.reps }
                if (histVolume > prevMaxVolume) prevMaxVolume = histVolume
            }

            // Detect PRs - only if previous best > 0 (as per the "earned through training" rule)
            if (prevBestWeight > 0 && currentBestWeight > prevBestWeight) {
                achievements.add(PersonalRecordAchievement(exerciseName, PersonalRecordType.BEST_WEIGHT, prevBestWeight, currentBestWeight))
            }
            if (prevBestE1RM > 0 && currentBestE1RM > prevBestE1RM) {
                achievements.add(PersonalRecordAchievement(exerciseName, PersonalRecordType.BEST_E1RM, prevBestE1RM, currentBestE1RM))
            }
            if (prevMaxVolume > 0 && currentVolume > prevMaxVolume) {
                achievements.add(PersonalRecordAchievement(exerciseName, PersonalRecordType.HIGHEST_VOLUME, prevMaxVolume, currentVolume))
            }
        }
        
        return achievements
    }

    fun getWorkoutProgress(sessionId: Long): Flow<WorkoutProgress> = 
        workoutSessionDao.getSessionAggregate(sessionId).map { aggregatePojo ->
            val aggregate = aggregatePojo?.toAggregate()
            val stats = aggregate?.statistics
            
            WorkoutProgress(
                completedExercises = stats?.completedExercisesCount ?: 0,
                totalExercises = stats?.totalExercisesCount ?: 0,
                completedSets = stats?.completedSetsCount ?: 0,
                totalSets = stats?.totalSetsCount ?: 0,
                percentage = if ((stats?.totalExercisesCount ?: 0) > 0) 
                    (stats?.completedExercisesCount ?: 0).toFloat() / stats!!.totalExercisesCount 
                else 0f
            )
        }

    suspend fun updateSet(
        setId: Long,
        weight: Double,
        reps: Int,
        rpe: Double?,
        notes: String?
    ) {
        val set = workoutSessionDao.getSessionSetById(setId) ?: return
        
        val validWeight = weight.coerceAtLeast(0.0)
        val validReps = reps.coerceAtLeast(0)
        val validRpe = rpe?.coerceIn(0.0, 10.0)
        
        val updatedSet = set.copy(
            weight = validWeight,
            reps = validReps,
            rpe = validRpe,
            notes = notes
        )
        workoutSessionDao.updateSessionSet(updatedSet)
        
        // Mark session as IN_PROGRESS if it was just CREATED
        val sessionId = workoutSessionDao.getSessionIdByExercise(set.sessionExerciseId)
        sessionId?.let { markSessionActive(it) }
    }
    
    suspend fun markSessionActive(sessionId: Long) {
        val session = workoutSessionDao.getSessionByIdOnce(sessionId) ?: return
        if (session.status == WorkoutSessionStatus.CREATED) {
            updateSessionStatus(sessionId, WorkoutSessionStatus.IN_PROGRESS)
        }
    }

    suspend fun toggleSetCompletion(sessionId: Long, setId: Long) {
        val set = workoutSessionDao.getSessionSetById(setId) ?: return
        val newCompletion = !set.completed
        
        workoutSessionDao.updateSessionSet(set.copy(completed = newCompletion))
        
        if (newCompletion) {
            markSessionActive(sessionId)
            if (set.setType == "Working" || set.setType == "Back-off") {
                handleAutoStartTimer(sessionId, set.sessionExerciseId)
            }
        }

        val exercises = workoutSessionDao.getExercisesWithSetsForSessionList(sessionId)
        val sessionExercise = exercises.find { it.sessionExercise.sessionExerciseId == set.sessionExerciseId } ?: return
        
        val allSetsCompleted = sessionExercise.sets.all { s -> 
            if (s.sessionSetId == setId) newCompletion else s.completed 
        }
        val anySetCompleted = sessionExercise.sets.any { s -> 
            if (s.sessionSetId == setId) newCompletion else s.completed 
        }
        
        val newStatus = when {
            allSetsCompleted -> "COMPLETED"
            anySetCompleted -> "IN_PROGRESS"
            else -> "NOT_STARTED"
        }
        
        if (sessionExercise.sessionExercise.status != newStatus) {
            workoutSessionDao.updateSessionExerciseStatus(sessionExercise.sessionExercise.sessionExerciseId, newStatus)
        }
        
        updateEngineCompletedCount(sessionId)
    }

    private suspend fun handleAutoStartTimer(sessionId: Long, sessionExerciseId: Long) {
        val settings = workoutSettingsDao.getSettingsOnce() ?: com.bhanu.ironlog.data.local.entity.WorkoutSettingsEntity()
        if (!settings.autoStartTimer) return

        val exercises = workoutSessionDao.getExercisesWithSetsForSessionList(sessionId)
        val exercise = exercises.find { it.sessionExercise.sessionExerciseId == sessionExerciseId } ?: return
        
        val duration = if (exercise.sessionExercise.restTimerSeconds > 0) {
            exercise.sessionExercise.restTimerSeconds
        } else {
            settings.defaultRestTimerSeconds
        }

        startRestTimer(sessionId, duration)
    }

    suspend fun startRestTimer(sessionId: Long, durationSeconds: Int) {
        val session = workoutSessionDao.getSessionByIdOnce(sessionId) ?: return
        workoutSessionDao.updateSession(session.copy(
            timerStartTime = System.currentTimeMillis(),
            timerDurationSeconds = durationSeconds,
            timerState = "RUNNING",
            timerPausedRemainingSeconds = null
        ))
    }

    suspend fun pauseRestTimer(sessionId: Long) {
        val session = workoutSessionDao.getSessionByIdOnce(sessionId) ?: return
        if (session.timerState != "RUNNING") return
        
        val elapsed = (System.currentTimeMillis() - (session.timerStartTime ?: 0L)) / 1000
        val remaining = (session.timerDurationSeconds ?: 0) - elapsed.toInt()
        
        workoutSessionDao.updateSession(session.copy(
            timerState = "PAUSED",
            timerPausedRemainingSeconds = remaining.coerceAtLeast(0)
        ))
    }

    suspend fun resumeRestTimer(sessionId: Long) {
        val session = workoutSessionDao.getSessionByIdOnce(sessionId) ?: return
        if (session.timerState != "PAUSED") return
        
        val remaining = session.timerPausedRemainingSeconds ?: 0
        workoutSessionDao.updateSession(session.copy(
            timerStartTime = System.currentTimeMillis() - ((session.timerDurationSeconds ?: 0) - remaining) * 1000,
            timerState = "RUNNING",
            timerPausedRemainingSeconds = null
        ))
    }

    suspend fun adjustRestTimer(sessionId: Long, addSeconds: Int) {
        val session = workoutSessionDao.getSessionByIdOnce(sessionId) ?: return
        if (session.timerState != "RUNNING" && session.timerState != "PAUSED") return
        
        val newDuration = (session.timerDurationSeconds ?: 0) + addSeconds
        workoutSessionDao.updateSession(session.copy(
            timerDurationSeconds = newDuration.coerceAtLeast(1)
        ))
    }

    suspend fun dismissRestTimer(sessionId: Long) {
        val session = workoutSessionDao.getSessionByIdOnce(sessionId) ?: return
        if (session.timerState == "IDLE" || session.timerState == "DISMISSED") return
        
        workoutSessionDao.updateSession(session.copy(
            timerState = "DISMISSED",
            timerStartTime = null,
            timerDurationSeconds = null,
            timerPausedRemainingSeconds = null
        ))
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun getActiveRestTimer(sessionId: Long): Flow<RestTimerInfo> = 
        workoutSessionDao.getSessionById(sessionId).flatMapLatest { session ->
            if (session == null || session.timerState == "IDLE" || session.timerState == "DISMISSED") {
                return@flatMapLatest flowOf(RestTimerInfo())
            }
            
            tickerFlow(1000).map {
                val now = System.currentTimeMillis()
                val startTime = session.timerStartTime ?: 0L
                val duration = session.timerDurationSeconds ?: 0
                
                when (session.timerState) {
                    "PAUSED" -> {
                        RestTimerInfo(
                            state = RestTimerState.PAUSED,
                            remainingSeconds = session.timerPausedRemainingSeconds ?: 0,
                            totalDurationSeconds = duration
                        )
                    }
                    "RUNNING", "COMPLETED" -> {
                        val elapsed = (now - startTime) / 1000
                        val remaining = duration - elapsed.toInt()
                        if (remaining > 0) {
                            RestTimerInfo(
                                state = RestTimerState.RUNNING,
                                remainingSeconds = remaining,
                                totalDurationSeconds = duration
                            )
                        } else {
                            RestTimerInfo(
                                state = RestTimerState.COMPLETED,
                                remainingSeconds = 0,
                                totalDurationSeconds = duration,
                                elapsedGraceSeconds = (-remaining).toInt()
                            )
                        }
                    }
                    else -> RestTimerInfo()
                }
            }
        }

    private fun tickerFlow(periodMillis: Long) = flow {
        while (true) {
            emit(Unit)
            delay(periodMillis)
        }
    }

    suspend fun skipExercise(sessionId: Long, exerciseTemplateId: Long) {
        val sessionExercise = workoutSessionDao.getSessionExercise(sessionId, exerciseTemplateId) ?: return
        workoutSessionDao.updateSessionExerciseStatus(sessionExercise.sessionExerciseId, "SKIPPED")
        moveToNextUnfinishedExercise(sessionId)
    }

    suspend fun completeExercise(sessionId: Long, exerciseTemplateId: Long) {
        val sessionExercise = workoutSessionDao.getSessionExercise(sessionId, exerciseTemplateId) ?: return
        workoutSessionDao.updateSessionExerciseStatus(sessionExercise.sessionExerciseId, "COMPLETED")
        moveToNextUnfinishedExercise(sessionId)
    }

    suspend fun toggleExerciseCompletion(sessionId: Long, exerciseTemplateId: Long) {
        val sessionExercise = workoutSessionDao.getSessionExercise(sessionId, exerciseTemplateId) ?: return
        val newStatus = if (sessionExercise.status == "COMPLETED") "NOT_STARTED" else "COMPLETED"
        workoutSessionDao.updateSessionExerciseStatus(sessionExercise.sessionExerciseId, newStatus)
        
        if (newStatus == "COMPLETED") {
            moveToNextUnfinishedExercise(sessionId)
        }
    }

    suspend fun moveToNextUnfinishedExercise(sessionId: Long) {
        val exercises = workoutSessionDao.getExercisesForSessionList(sessionId).sortedBy { it.exerciseOrder }
        val next = exercises.find { it.status == "NOT_STARTED" || it.status == "IN_PROGRESS" }
        
        val session = workoutSessionDao.getSessionByIdOnce(sessionId) ?: return
        workoutSessionDao.updateSession(session.copy(currentExerciseId = next?.exerciseTemplateId))
    }

    suspend fun moveToNextExercise(sessionId: Long) {
        val exercises = workoutSessionDao.getExercisesForSessionList(sessionId).sortedBy { it.exerciseOrder }
        val session = workoutSessionDao.getSessionByIdOnce(sessionId) ?: return
        val currentIndex = exercises.indexOfFirst { it.exerciseTemplateId == session.currentExerciseId }
        
        if (currentIndex < exercises.size - 1) {
            val next = exercises[currentIndex + 1]
            workoutSessionDao.updateSession(session.copy(currentExerciseId = next.exerciseTemplateId))
        }
    }

    suspend fun moveToPreviousExercise(sessionId: Long) {
        val exercises = workoutSessionDao.getExercisesForSessionList(sessionId).sortedBy { it.exerciseOrder }
        val session = workoutSessionDao.getSessionByIdOnce(sessionId) ?: return
        val currentIndex = exercises.indexOfFirst { it.exerciseTemplateId == session.currentExerciseId }
        
        if (currentIndex > 0) {
            val prev = exercises[currentIndex - 1]
            workoutSessionDao.updateSession(session.copy(currentExerciseId = prev.exerciseTemplateId))
        }
    }

    private suspend fun updateEngineCompletedCount(sessionId: Long) {
        val exercises = workoutSessionDao.getExercisesWithSetsForSessionList(sessionId)
        val totalCompletedSets = exercises.sumOf { it.sets.count { s -> s.completed } }
        val session = workoutSessionDao.getSessionByIdOnce(sessionId) ?: return
        workoutSessionDao.updateSession(session.copy(completedSetsCount = totalCompletedSets))
    }

    suspend fun updateSession(session: WorkoutSession) = workoutSessionDao.updateSession(session)

    suspend fun updateEngineState(sessionId: Long, exerciseId: Long?, setNumber: Int?, completedSets: Int) {
        workoutSessionDao.updateEngineState(sessionId, exerciseId, setNumber, completedSets)
    }

    suspend fun updateSessionExerciseStatus(sessionExerciseId: Long, status: String) {
        workoutSessionDao.updateSessionExerciseStatus(sessionExerciseId, status)
    }

    suspend fun updateSessionExerciseNotes(sessionExerciseId: Long, notes: String) {
        workoutSessionDao.updateSessionExerciseNotes(sessionExerciseId, notes)
    }

    suspend fun deleteSession(session: WorkoutSession) = workoutSessionDao.deleteSession(session)

    fun getExercisesWithTemplateForSession(sessionId: Long): Flow<List<SessionExerciseWithTemplate>> =
        workoutSessionDao.getExercisesWithTemplateForSession(sessionId)

    fun getExercisesForActiveSession(sessionId: Long): Flow<List<SessionExerciseWithTemplate>> =
        workoutSessionDao.getExercisesWithTemplateForSession(sessionId)

    fun getExercisesWithSetsForSession(sessionId: Long): Flow<List<SessionExerciseWithTemplateAndSets>> =
        workoutSessionDao.getExercisesWithSetsForSession(sessionId)

    fun getHistoricalExercisesWithSets(sessionId: Long): Flow<List<SessionExerciseWithSets>> =
        workoutSessionDao.getHistoricalExercisesWithSets(sessionId)

    fun getPreviousPerformance(exerciseTemplateId: Long, currentSessionId: Long): Flow<SessionExerciseWithSetsAndSession?> =
        workoutSessionDao.getLatestCompletedExerciseRecord(exerciseTemplateId, currentSessionId)

    fun getPreviousPerformanceByLibraryId(libraryExerciseId: Long, currentSessionId: Long): Flow<SessionExerciseWithSetsAndSession?> =
        workoutSessionDao.getLatestExecutionByLibraryId(libraryExerciseId, currentSessionId)

    fun getExerciseDetails(exerciseTemplateId: Long): Flow<ExerciseDetails?> =
        workoutSessionDao.getCompletedExerciseRecords(exerciseTemplateId).map { records ->
            if (records.isEmpty()) return@map null
            
            val mostRecent = records.first()
            val totalSessions = records.size
            var totalVolume = 0.0
            var bestWeight = 0.0
            var bestE1RM = 0.0
            val firstPerformed = records.last().session.createdAt
            val lastPerformed = mostRecent.session.createdAt
            
            val sessionHistory = records.map { record ->
                val sessionVolume = record.sets.sumOf { if (it.completed) it.weight * it.reps else 0.0 }
                totalVolume += sessionVolume
                
                record.sets.forEach { set ->
                    if (set.completed) {
                        if (set.weight > bestWeight) bestWeight = set.weight
                        val e1rm = set.weight * (1 + set.reps / 30.0)
                        if (e1rm > bestE1RM) bestE1RM = e1rm
                    }
                }
                
                ExerciseSessionRecord(
                    sessionId = record.session.sessionId,
                    date = record.session.createdAt,
                    workoutName = record.session.dayName,
                    status = record.sessionExercise.status,
                    sessionVolume = sessionVolume,
                    sets = record.sets
                )
            }
            
            ExerciseDetails(
                exerciseTemplateId = exerciseTemplateId,
                exerciseName = mostRecent.sessionExercise.exerciseName.ifBlank { mostRecent.template?.name ?: "Deleted Exercise" },
                muscleGroup = mostRecent.sessionExercise.muscleGroup.ifBlank { mostRecent.template?.muscleGroup ?: "" },
                totalSessions = totalSessions,
                totalVolume = totalVolume,
                bestWeight = bestWeight,
                estimated1RM = bestE1RM,
                firstPerformed = firstPerformed,
                lastPerformed = lastPerformed,
                sessionHistory = sessionHistory
            )
        }

    fun getSetsForExercise(sessionExerciseId: Long): Flow<List<SessionSet>> = 
        workoutSessionDao.getSetsForExercise(sessionExerciseId)

    suspend fun insertSessionSet(set: SessionSet): Long {
        val id = workoutSessionDao.insertSessionSet(set)
        renumberSets(set.sessionExerciseId)
        return id
    }

    private suspend fun renumberSets(sessionExerciseId: Long) {
        val sets = workoutSessionDao.getSetsForExerciseList(sessionExerciseId).sortedBy { it.setNumber }
        sets.forEachIndexed { index, s ->
            val updatedSet = s.copy(setNumber = index + 1)
            workoutSessionDao.updateSessionSet(updatedSet)
        }
    }

    suspend fun getSessionExercise(sessionId: Long, exerciseId: Long): SessionExercise? =
        workoutSessionDao.getSessionExercise(sessionId, exerciseId)

    suspend fun updateSessionSet(set: SessionSet) =
        workoutSessionDao.updateSessionSet(set)

    suspend fun deleteSessionSet(set: SessionSet) {
        workoutSessionDao.deleteSessionSet(set)
        renumberSets(set.sessionExerciseId)
    }

    suspend fun duplicateSessionSet(setId: Long) {
        val set = workoutSessionDao.getSessionSetById(setId) ?: return
        workoutSessionDao.insertSessionSet(
            set.copy(
                sessionSetId = 0,
                completed = false,
                createdAt = System.currentTimeMillis()
            )
        )
        renumberSets(set.sessionExerciseId)
    }

    suspend fun moveSessionSet(setId: Long, up: Boolean) {
        val set = workoutSessionDao.getSessionSetById(setId) ?: return
        val sets = workoutSessionDao.getSetsForExerciseList(set.sessionExerciseId).sortedBy { it.setNumber }
        val currentIndex = sets.indexOfFirst { it.sessionSetId == setId }
        
        val targetIndex = if (up) currentIndex - 1 else currentIndex + 1
        
        if (targetIndex in sets.indices) {
            val targetSet = sets[targetIndex]
            val currentSetNumber = set.setNumber
            val targetSetNumber = targetSet.setNumber
            
            workoutSessionDao.updateSessionSet(set.copy(setNumber = targetSetNumber))
            workoutSessionDao.updateSessionSet(targetSet.copy(setNumber = currentSetNumber))
        }
    }
}
