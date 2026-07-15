package com.bhanu.ironlog.data.local.dao

import androidx.room.*
import com.bhanu.ironlog.data.local.entity.SessionExercise
import com.bhanu.ironlog.data.local.entity.SessionSet
import com.bhanu.ironlog.data.local.entity.WorkoutSession
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSession): Long

    @Update
    suspend fun updateSession(session: WorkoutSession)

    @Query("SELECT * FROM workout_session_logs WHERE sessionId = :sessionId")
    fun getSessionById(sessionId: Long): Flow<WorkoutSession?>

    @Query("SELECT * FROM workout_session_logs ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<WorkoutSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionExercise(exercise: SessionExercise): Long

    @Query("SELECT * FROM session_exercises WHERE sessionId = :sessionId ORDER BY exerciseOrder ASC")
    fun getExercisesForSession(sessionId: Long): Flow<List<SessionExercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionSet(set: SessionSet): Long

    @Query("SELECT * FROM session_sets WHERE sessionExerciseId = :sessionExerciseId ORDER BY setNumber ASC")
    fun getSetsForExercise(sessionExerciseId: Long): Flow<List<SessionSet>>

    @Delete
    suspend fun deleteSession(session: WorkoutSession)
}
