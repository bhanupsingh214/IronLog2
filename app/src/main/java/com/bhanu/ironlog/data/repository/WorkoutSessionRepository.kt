package com.bhanu.ironlog.data.repository

import com.bhanu.ironlog.data.local.dao.ProgramDao
import com.bhanu.ironlog.data.local.dao.WorkoutSessionDao
import com.bhanu.ironlog.data.local.entity.SessionExercise
import com.bhanu.ironlog.data.local.entity.SessionSet
import com.bhanu.ironlog.data.local.entity.WorkoutSession
import com.bhanu.ironlog.data.local.pojo.SessionExerciseWithTemplate
import com.bhanu.ironlog.data.local.pojo.SessionExerciseWithTemplateAndSets
import com.bhanu.ironlog.data.local.pojo.WorkoutSessionWithVolume
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutSessionRepository @Inject constructor(
    private val workoutSessionDao: WorkoutSessionDao,
    private val programDao: ProgramDao
) {
    fun getAllSessions(): Flow<List<WorkoutSession>> = workoutSessionDao.getAllSessions()

    fun getCompletedSessions(): Flow<List<WorkoutSession>> = workoutSessionDao.getCompletedSessions()

    fun getCompletedSessionsWithVolume(): Flow<List<WorkoutSessionWithVolume>> = 
        workoutSessionDao.getCompletedSessionsWithVolume()

    fun getSessionById(sessionId: Long): Flow<WorkoutSession?> = workoutSessionDao.getSessionById(sessionId)

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
            // Hardening: Verify if this session actually has exercises.
            // If it's an empty "zombie" session from a previous bug, we should populate it.
            val sessionExercises = workoutSessionDao.getExercisesForSessionList(existingSessionId)
            if (sessionExercises.isNotEmpty()) {
                return existingSessionId
            }
            // If empty, we proceed to treat it as a new session or at least perform the copy.
            // For simplicity and safety, we'll use this ID but trigger the copy logic below.
        }

        // Get Names for Session
        val program = programDao.getProgramById(programId)
        val day = programDao.getDayById(dayId)

        // Create or reuse session ID
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

        // Copy Exercises from Template to Session
        val exercises = programDao.getEnabledExercisesForDay(dayId)
        for (exercise in exercises) {
            val sessionExerciseId = workoutSessionDao.insertSessionExercise(
                SessionExercise(
                    sessionId = sessionId,
                    exerciseTemplateId = exercise.id,
                    exerciseOrder = exercise.order
                )
            )

            // Copy Sets from Template to Session
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

        return sessionId
    }

    suspend fun insertSession(session: WorkoutSession): Long = workoutSessionDao.insertSession(session)

    suspend fun updateSession(session: WorkoutSession) = workoutSessionDao.updateSession(session)

    suspend fun finishSession(sessionId: Long) {
        val session = workoutSessionDao.getSessionByIdOnce(sessionId)
        session?.let {
            val endTime = System.currentTimeMillis()
            // If the workout was started more than 12 hours ago, it's likely a historical log
            // or a forgotten session. In these cases, we don't want a massive duration.
            val isHistorical = it.startTime < endTime - 12 * 3600 * 1000
            val duration = if (isHistorical) 0L else (endTime - it.startTime) / 1000

            workoutSessionDao.updateSession(it.copy(
                status = "COMPLETED",
                endTime = endTime,
                durationSeconds = duration
            ))
        }
    }

    suspend fun deleteSession(session: WorkoutSession) = workoutSessionDao.deleteSession(session)

    fun getExercisesForSession(sessionId: Long): Flow<List<SessionExercise>> = 
        workoutSessionDao.getExercisesForSession(sessionId)

    fun getExercisesWithTemplateForSession(sessionId: Long): Flow<List<SessionExerciseWithTemplate>> =
        workoutSessionDao.getExercisesWithTemplateForSession(sessionId)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getExercisesForActiveSession(sessionId: Long, dayId: Long): Flow<List<SessionExerciseWithTemplate>> =
        programDao.getEnabledExercisesForDayFlow(dayId).flatMapLatest { templateExercises ->
            flow {
                val sessionExercises = workoutSessionDao.getExercisesForSessionList(sessionId)
                val templateIds = templateExercises.map { it.id }.toSet()
                val sessionTemplateIds = sessionExercises.map { it.exerciseTemplateId }.toSet()

                // 1. Add missing exercises
                val missingExercises = templateExercises.filter { it.id !in sessionTemplateIds }
                for (exercise in missingExercises) {
                    val sessionExerciseId = workoutSessionDao.insertSessionExercise(
                        SessionExercise(
                            sessionId = sessionId,
                            exerciseTemplateId = exercise.id,
                            exerciseOrder = exercise.order
                        )
                    )
                    // Copy Sets
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

                // 2. Remove exercises no longer in template
                val removedSessionExercises = sessionExercises.filter { it.exerciseTemplateId !in templateIds }
                for (sessionExercise in removedSessionExercises) {
                    // This will also delete session_sets due to ForeignKey CASCADE
                    workoutSessionDao.deleteSessionExercise(sessionExercise)
                }
                
                // 3. Update orders if changed (Optional, but good for consistency)
                for (exercise in templateExercises) {
                    val existing = sessionExercises.find { it.exerciseTemplateId == exercise.id }
                    if (existing != null && existing.exerciseOrder != exercise.order) {
                        workoutSessionDao.insertSessionExercise(existing.copy(exerciseOrder = exercise.order))
                    }
                }

                emitAll(workoutSessionDao.getExercisesWithTemplateForSession(sessionId))
            }
        }

    fun getExercisesWithSetsForSession(sessionId: Long): Flow<List<SessionExerciseWithTemplateAndSets>> =
        workoutSessionDao.getExercisesWithSetsForSession(sessionId)

    suspend fun insertSessionExercise(exercise: SessionExercise): Long = 
        workoutSessionDao.insertSessionExercise(exercise)

    fun getSetsForExercise(sessionExerciseId: Long): Flow<List<SessionSet>> = 
        workoutSessionDao.getSetsForExercise(sessionExerciseId)

    suspend fun insertSessionSet(set: SessionSet): Long {
        val id = workoutSessionDao.insertSessionSet(set)
        renumberSets(set.sessionExerciseId)
        return id
    }

    private suspend fun renumberSets(sessionExerciseId: Long) {
        val sets = workoutSessionDao.getSetsForExerciseList(sessionExerciseId).sortedBy { it.setNumber }
        sets.forEachIndexed { index, set ->
            val updatedSet = set.copy(setNumber = index + 1)
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
