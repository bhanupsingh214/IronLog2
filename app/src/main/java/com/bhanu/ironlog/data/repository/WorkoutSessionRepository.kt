package com.bhanu.ironlog.data.repository

import com.bhanu.ironlog.data.local.dao.WorkoutSessionDao
import com.bhanu.ironlog.data.local.entity.SessionExercise
import com.bhanu.ironlog.data.local.entity.SessionSet
import com.bhanu.ironlog.data.local.entity.WorkoutSession
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutSessionRepository @Inject constructor(
    private val workoutSessionDao: WorkoutSessionDao
) {
    fun getAllSessions(): Flow<List<WorkoutSession>> = workoutSessionDao.getAllSessions()

    fun getSessionById(sessionId: Long): Flow<WorkoutSession?> = workoutSessionDao.getSessionById(sessionId)

    suspend fun insertSession(session: WorkoutSession): Long = workoutSessionDao.insertSession(session)

    suspend fun updateSession(session: WorkoutSession) = workoutSessionDao.updateSession(session)

    suspend fun deleteSession(session: WorkoutSession) = workoutSessionDao.deleteSession(session)

    fun getExercisesForSession(sessionId: Long): Flow<List<SessionExercise>> = 
        workoutSessionDao.getExercisesForSession(sessionId)

    suspend fun insertSessionExercise(exercise: SessionExercise): Long = 
        workoutSessionDao.insertSessionExercise(exercise)

    fun getSetsForExercise(sessionExerciseId: Long): Flow<List<SessionSet>> = 
        workoutSessionDao.getSetsForExercise(sessionExerciseId)

    suspend fun insertSessionSet(set: SessionSet): Long = 
        workoutSessionDao.insertSessionSet(set)
}
