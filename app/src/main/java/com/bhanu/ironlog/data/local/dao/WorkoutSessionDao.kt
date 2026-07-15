package com.bhanu.ironlog.data.local.dao

import androidx.room.*
import com.bhanu.ironlog.data.local.entity.SessionExercise
import com.bhanu.ironlog.data.local.entity.SessionSet
import com.bhanu.ironlog.data.local.entity.WorkoutSession
import com.bhanu.ironlog.data.local.pojo.SessionExerciseWithTemplate
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSession): Long

    @Update
    suspend fun updateSession(session: WorkoutSession)

    @Query("SELECT * FROM workout_session_logs WHERE sessionId = :sessionId")
    fun getSessionById(sessionId: Long): Flow<WorkoutSession?>

    @Query("SELECT * FROM workout_session_logs WHERE sessionId = :sessionId")
    suspend fun getSessionByIdOnce(sessionId: Long): WorkoutSession?

    @Query("SELECT * FROM workout_session_logs WHERE workoutDayId = :dayId AND status = 'ACTIVE' LIMIT 1")
    suspend fun getActiveSessionByDay(dayId: Long): WorkoutSession?

    @Query("SELECT * FROM workout_session_logs ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<WorkoutSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionExercise(exercise: SessionExercise): Long

    @Transaction
    @Query("SELECT * FROM session_exercises WHERE sessionId = :sessionId ORDER BY exerciseOrder ASC")
    fun getExercisesWithTemplateForSession(sessionId: Long): Flow<List<SessionExerciseWithTemplate>>

    @Query("SELECT * FROM session_exercises WHERE sessionId = :sessionId ORDER BY exerciseOrder ASC")
    fun getExercisesForSession(sessionId: Long): Flow<List<SessionExercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionSet(set: SessionSet): Long

    @Query("SELECT * FROM session_sets WHERE sessionExerciseId = :sessionExerciseId ORDER BY setNumber ASC")
    fun getSetsForExercise(sessionExerciseId: Long): Flow<List<SessionSet>>

    @Query("SELECT * FROM session_sets WHERE sessionExerciseId = :sessionExerciseId ORDER BY setNumber ASC")
    suspend fun getSetsForExerciseList(sessionExerciseId: Long): List<SessionSet>

    @Query("SELECT * FROM session_sets WHERE sessionSetId = :id")
    suspend fun getSessionSetById(id: Long): SessionSet?

    @Query("SELECT * FROM session_exercises WHERE sessionId = :sessionId AND exerciseTemplateId = :exerciseId LIMIT 1")
    suspend fun getSessionExercise(sessionId: Long, exerciseId: Long): SessionExercise?

    @Update
    suspend fun updateSessionSet(set: SessionSet)

    @Delete
    suspend fun deleteSessionSet(set: SessionSet)

    @Query("SELECT * FROM workout_session_logs WHERE status = 'COMPLETED' ORDER BY createdAt DESC")
    fun getCompletedSessions(): Flow<List<WorkoutSession>>

    @Query("""
        SELECT SUM(s.weight * s.reps) 
        FROM session_sets s 
        JOIN session_exercises e ON s.sessionExerciseId = e.sessionExerciseId 
        JOIN workout_session_logs sess ON e.sessionId = sess.sessionId 
        WHERE sess.status = 'COMPLETED' AND sess.createdAt >= :since
    """)
    fun getVolumeSince(since: Long): Flow<Double?>

    @Delete
    suspend fun deleteSession(session: WorkoutSession)
}
