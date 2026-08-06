package com.bhanu.ironlog.data.repository

import com.bhanu.ironlog.data.local.dao.ProgramDao
import com.bhanu.ironlog.data.local.dao.WorkoutSessionDao
import com.bhanu.ironlog.data.local.entity.SessionExercise
import com.bhanu.ironlog.data.local.entity.SessionSet
import com.bhanu.ironlog.data.local.entity.WorkoutSession
import com.bhanu.ironlog.data.local.pojo.*
import com.bhanu.ironlog.data.model.RestTimerState
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

    fun getWorkoutSettings(): Flow<com.bhanu.ironlog.data.local.entity.WorkoutSettingsEntity?> = 
        workoutSettingsDao.getSettings()

    suspend fun updateWorkoutSettings(settings: com.bhanu.ironlog.data.local.entity.WorkoutSettingsEntity) =
        workoutSettingsDao.updateSettings(settings)

    fun getAllSessions(): Flow<List<WorkoutSession>> = workoutSessionDao.getAllSessions()

    fun getCompletedSessions(): Flow<List<WorkoutSession>> = workoutSessionDao.getCompletedSessions()

    fun getCompletedSessionsWithStats(): Flow<List<WorkoutSessionWithStats>> = 
        workoutSessionDao.getCompletedSessionsWithStats()

    fun getCompletedSessionsWithVolume(): Flow<List<WorkoutSessionWithVolume>> = 
        workoutSessionDao.getCompletedSessionsWithVolume()

    fun getSessionById(sessionId: Long): Flow<WorkoutSession?> = workoutSessionDao.getSessionById(sessionId)

    fun getActiveSessionFlow(): Flow<WorkoutSession?> = workoutSessionDao.getActiveSession()

    fun getWorkoutCompletionSummary(sessionId: Long): Flow<WorkoutCompletionSummary?> =
        workoutSessionDao.getExercisesWithSetsForSession(sessionId).map { list ->
            val session = workoutSessionDao.getSessionByIdOnce(sessionId) ?: return@map null

            val totalExercises = list.size
            val completedExercises = list.count { it.sessionExercise.status == "COMPLETED" }
            val skippedExercises = list.count { it.sessionExercise.status == "SKIPPED" }
            val totalSets = list.sumOf { it.sets.size }
            val completedSets = list.sumOf { it.sets.count { s -> s.completed } }
            val totalVolume = list.sumOf { ex -> ex.sets.sumOf { s -> if (s.completed) s.weight * s.reps else 0.0 } }
            
            val percentage = if (totalExercises > 0) completedExercises.toFloat() / totalExercises else 0f
            
            val duration = if (session.status == "COMPLETED") {
                session.durationSeconds
            } else {
                (System.currentTimeMillis() - session.startTime) / 1000
            }

            val achievements = if (session.status == "COMPLETED") {
                detectAchievements(sessionId)
            } else {
                emptyList()
            }

            WorkoutCompletionSummary(
                sessionId = sessionId,
                workoutName = session.dayName,
                programName = session.programName,
                durationSeconds = duration,
                totalVolume = totalVolume,
                exercisesCompleted = completedExercises,
                totalExercises = totalExercises,
                setsCompleted = completedSets,
                totalSets = totalSets,
                skippedExercises = skippedExercises,
                completionPercentage = percentage,
                startTime = session.startTime,
                endTime = session.endTime ?: System.currentTimeMillis(),
                achievements = achievements
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
        workoutSessionDao.getExercisesWithSetsForSession(sessionId).map { list ->
            val totalExercises = list.size
            val completedExercises = list.count { it.sessionExercise.status == "COMPLETED" || it.sessionExercise.status == "SKIPPED" }
            val totalSets = list.sumOf { it.sets.size }
            val completedSets = list.sumOf { it.sets.count { s -> s.completed } }
            
            val percentage = if (totalExercises > 0) completedExercises.toFloat() / totalExercises else 0f
            
            WorkoutProgress(
                completedExercises = completedExercises,
                totalExercises = totalExercises,
                completedSets = completedSets,
                totalSets = totalSets,
                percentage = percentage
            )
        }

    fun getWeeklyVolume(): Flow<Double?> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return workoutSessionDao.getVolumeSince(calendar.timeInMillis)
    }

    fun getVolumeSince(since: Long): Flow<Double?> = 
        workoutSessionDao.getVolumeSince(since)

    fun getTotalVolume(): Flow<Double?> = 
        workoutSessionDao.getTotalVolume()

    fun getDailyVolumeHistory(since: Long): Flow<List<DailyVolume>> =
        workoutSessionDao.getDailyVolumeHistory(since)

    fun getExerciseStrengthHistory(exerciseId: Long): Flow<List<ExerciseStrengthHistory>> =
        workoutSessionDao.getExerciseStrengthHistory(exerciseId)

    suspend fun getOrCreateSession(dayId: Long, programId: Long, startTime: Long = System.currentTimeMillis()): Long {
        val activeSession = workoutSessionDao.getActiveSessionByDay(dayId)
        val isHistorical = startTime < System.currentTimeMillis() - 60000
        
        var existingSessionId: Long? = null
        
        if (!isHistorical && activeSession != null) {
            existingSessionId = activeSession.sessionId
        } else if (isHistorical) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = startTime
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val dayStart = calendar.timeInMillis
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val dayEnd = calendar.timeInMillis
            
            val existing = workoutSessionDao.getSessionByDayAndDate(dayId, dayStart, dayEnd)
            if (existing != null) {
                existingSessionId = existing.sessionId
            }
        }

        if (existingSessionId != null) {
            val sessionExercises = workoutSessionDao.getExercisesForSessionList(existingSessionId)
            if (sessionExercises.isNotEmpty()) {
                return existingSessionId
            }
        }

        val program = programDao.getProgramById(programId)
        val day = programDao.getDayById(dayId)

        val sessionId = existingSessionId ?: workoutSessionDao.insertSession(
            WorkoutSession(
                programId = programId,
                workoutDayId = dayId,
                dayName = day?.name ?: "Unknown Day",
                programName = program?.name ?: "Unknown Program",
                status = "ACTIVE",
                startTime = startTime,
                createdAt = startTime
            )
        )

        val exercises = programDao.getEnabledExercisesForDay(dayId)
        for (exercise in exercises) {
            val sessionExerciseId = workoutSessionDao.insertSessionExercise(
                SessionExercise(
                    sessionId = sessionId,
                    exerciseTemplateId = exercise.id,
                    exerciseName = exercise.name,
                    muscleGroup = exercise.muscleGroup,
                    exerciseOrder = exercise.order,
                    notes = exercise.notes,
                    status = "NOT_STARTED",
                    restTimerSeconds = if (exercise.useDefaultRestTimer) 0 else exercise.restTimerSeconds
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
    }

    suspend fun toggleSetCompletion(sessionId: Long, setId: Long) {
        val set = workoutSessionDao.getSessionSetById(setId) ?: return
        val newCompletion = !set.completed
        
        workoutSessionDao.updateSessionSet(set.copy(completed = newCompletion))
        
        if (newCompletion && (set.setType == "Working" || set.setType == "Back-off")) {
            handleAutoStartTimer(sessionId, set.sessionExerciseId)
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
        
        // If completing, maybe move to next unfinished
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

    suspend fun finishSession(sessionId: Long) {
        val session = workoutSessionDao.getSessionByIdOnce(sessionId)
        session?.let {
            val endTime = System.currentTimeMillis()
            val isHistorical = it.startTime < endTime - 12 * 3600 * 1000
            val duration = if (isHistorical) 0L else (endTime - it.startTime) / 1000

            // Issue 1 Fix: Mark all unfinished exercises as SKIPPED
            val exercises = workoutSessionDao.getExercisesForSessionList(sessionId)
            for (exercise in exercises) {
                if (exercise.status != "COMPLETED" && exercise.status != "SKIPPED") {
                    workoutSessionDao.updateSessionExerciseStatus(exercise.sessionExerciseId, "SKIPPED")
                }
            }

            workoutSessionDao.updateSession(it.copy(
                status = "COMPLETED",
                endTime = endTime,
                durationSeconds = duration,
                timerState = "DISMISSED",
                timerStartTime = null,
                timerDurationSeconds = null,
                timerPausedRemainingSeconds = null
            ))
        }
    }

    suspend fun discardSession(sessionId: Long) {
        val session = workoutSessionDao.getSessionByIdOnce(sessionId)
        session?.let {
            workoutSessionDao.deleteSession(it)
        }
    }

    suspend fun updateEngineState(sessionId: Long, exerciseId: Long?, setNumber: Int?, completedSets: Int) {
        workoutSessionDao.updateEngineState(sessionId, exerciseId, setNumber, completedSets)
    }

    suspend fun updateSessionExerciseStatus(sessionExerciseId: Long, status: String) {
        workoutSessionDao.updateSessionExerciseStatus(sessionExerciseId, status)
    }

    suspend fun updateSessionExerciseNotes(sessionExerciseId: Long, notes: String) {
        workoutSessionDao.updateSessionExerciseNotes(sessionExerciseId, notes)
    }

    suspend fun updateSessionExerciseRestTimer(sessionExerciseId: Long, seconds: Int) {
        workoutSessionDao.updateSessionExerciseRestTimer(sessionExerciseId, seconds)
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
