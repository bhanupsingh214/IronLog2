package com.bhanu.ironlog.data.repository

import com.bhanu.ironlog.data.local.dao.ProgramDao
import com.bhanu.ironlog.data.local.dao.WorkoutSessionDao
import com.bhanu.ironlog.data.local.entity.SessionExercise
import com.bhanu.ironlog.data.local.entity.SessionSet
import com.bhanu.ironlog.data.local.entity.WorkoutSession
import com.bhanu.ironlog.data.local.pojo.SessionExerciseWithTemplate
import com.bhanu.ironlog.data.local.pojo.SessionExerciseWithTemplateAndSets
import com.bhanu.ironlog.data.local.pojo.WorkoutSessionWithVolume
import kotlinx.coroutines.flow.Flow
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
        return workoutSessionDao.getVolumeSince(calendar.timeInMillis)
    }

    suspend fun getOrCreateSession(dayId: Long, programId: Long): Long {
        val activeSession = workoutSessionDao.getActiveSessionByDay(dayId)
        if (activeSession != null) {
            return activeSession.sessionId
        }

        // Get Names for Session
        val program = programDao.getProgramById(programId)
        val day = programDao.getDayById(dayId)

        // Create new session
        val sessionId = workoutSessionDao.insertSession(
            WorkoutSession(
                programId = programId,
                workoutDayId = dayId,
                dayName = day?.name ?: "Unknown Day",
                programName = program?.name ?: "Unknown Program",
                status = "ACTIVE"
            )
        )

        // Copy Exercises from Template to Session
        val exercises = programDao.getExercisesForDay(dayId)
        for (exercise in exercises) {
            val sessionExerciseId = workoutSessionDao.insertSessionExercise(
                SessionExercise(
                    sessionId = sessionId,
                    exerciseTemplateId = exercise.id,
                    exerciseOrder = exercise.order
                )
            )

            // Copy Sets from Template to Session
            // Template sets are stored in exercise_sets with sessionId = 0
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
            val duration = (endTime - it.startTime) / 1000
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
