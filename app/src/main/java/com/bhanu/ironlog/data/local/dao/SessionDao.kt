package com.bhanu.ironlog.data.local.dao

import androidx.room.*
import com.bhanu.ironlog.data.local.entity.SetEntity
import com.bhanu.ironlog.data.local.entity.WorkoutSessionEntity
import com.bhanu.ironlog.data.local.pojo.PRResult
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Update
    suspend fun updateSession(session: WorkoutSessionEntity)

    @Query("SELECT * FROM workout_sessions WHERE isCompleted = 1 ORDER BY date DESC")
    fun getCompletedSessions(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE isCompleted = 0 LIMIT 1")
    fun getActiveSession(): Flow<WorkoutSessionEntity?>

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    fun getSessionByIdFlow(id: Long): Flow<WorkoutSessionEntity?>

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): WorkoutSessionEntity?

    @Query("DELETE FROM workout_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("SELECT SUM(weight * reps) FROM exercise_sets WHERE sessionId > 0 AND createdAt >= :since")
    fun getVolumeSince(since: Long): Flow<Double?>

    @Query("""
        SELECT * FROM exercise_sets 
        WHERE sessionId > 0 
        AND weight = (SELECT MAX(weight) FROM exercise_sets WHERE exerciseId = :exerciseId AND sessionId > 0)
        LIMIT 1
    """)
    fun getPersonalRecord(exerciseId: Long): Flow<SetEntity?>

    @Query("""
        SELECT name as exerciseName, MAX(weight) as maxWeight FROM exercise_sets es 
        JOIN exercises e ON es.exerciseId = e.id 
        WHERE es.sessionId > 0 
        GROUP BY exerciseId 
        ORDER BY maxWeight DESC
        LIMIT 5
    """)
    fun getTopPersonalRecords(): Flow<List<PRResult>>
}
