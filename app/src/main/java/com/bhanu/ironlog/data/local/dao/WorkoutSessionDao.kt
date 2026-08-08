package com.bhanu.ironlog.data.local.dao

import androidx.room.*
import com.bhanu.ironlog.data.local.entity.SessionExercise
import com.bhanu.ironlog.data.local.entity.SessionSet
import com.bhanu.ironlog.data.local.entity.WorkoutSession
import com.bhanu.ironlog.data.local.pojo.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSession): Long

    @Update
    suspend fun updateSession(session: WorkoutSession)

    @Query("SELECT * FROM workout_session_logs WHERE sessionId = :sessionId")
    fun getSessionById(sessionId: Long): Flow<WorkoutSession?>

    @Query("SELECT * FROM workout_session_logs WHERE status IN ('CREATED', 'IN_PROGRESS', 'PAUSED') LIMIT 1")
    fun getActiveSession(): Flow<WorkoutSession?>

    @Query("SELECT * FROM workout_session_logs WHERE sessionId = :sessionId")
    suspend fun getSessionByIdOnce(sessionId: Long): WorkoutSession?

    @Transaction
    @Query("SELECT * FROM workout_session_logs WHERE sessionId = :sessionId")
    fun getSessionAggregate(sessionId: Long): Flow<WorkoutSessionWithExercises?>

    @Transaction
    @Query("SELECT * FROM workout_session_logs WHERE status IN ('CREATED', 'IN_PROGRESS', 'PAUSED') LIMIT 1")
    fun getActiveSessionAggregate(): Flow<WorkoutSessionWithExercises?>

    @Query("UPDATE workout_session_logs SET currentExerciseId = :exerciseId, currentSetNumber = :setNumber, completedSetsCount = :completedSets WHERE sessionId = :sessionId")
    suspend fun updateEngineState(sessionId: Long, exerciseId: Long?, setNumber: Int?, completedSets: Int)

    @Query("SELECT sessionId FROM session_exercises WHERE sessionExerciseId = :sessionExerciseId LIMIT 1")
    suspend fun getSessionIdByExercise(sessionExerciseId: Long): Long?

    @Query("UPDATE session_exercises SET status = :status WHERE sessionExerciseId = :sessionExerciseId")
    suspend fun updateSessionExerciseStatus(sessionExerciseId: Long, status: String)

    @Query("UPDATE session_exercises SET notes = :notes WHERE sessionExerciseId = :sessionExerciseId")
    suspend fun updateSessionExerciseNotes(sessionExerciseId: Long, notes: String)

    @Query("SELECT * FROM workout_session_logs WHERE workoutDayId = :dayId AND status = 'ACTIVE' LIMIT 1")
    suspend fun getActiveSessionByDay(dayId: Long): WorkoutSession?

    @Query("SELECT * FROM workout_session_logs WHERE workoutDayId = :dayId AND createdAt BETWEEN :start AND :end LIMIT 1")
    suspend fun getSessionByDayAndDate(dayId: Long, start: Long, end: Long): WorkoutSession?

    @Query("SELECT * FROM workout_session_logs WHERE workoutDayId = :dayId AND status = 'COMPLETED' AND startTime < :beforeStartTime ORDER BY startTime DESC LIMIT 1")
    suspend fun getLatestCompletedSessionBefore(dayId: Long, beforeStartTime: Long): WorkoutSession?

    @Query("SELECT * FROM workout_session_logs ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<WorkoutSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionExercise(exercise: SessionExercise): Long

    @Transaction
    @Query("""
        SELECT se.* FROM session_exercises se
        JOIN workout_session_logs wsl ON se.sessionId = wsl.sessionId
        WHERE se.exerciseTemplateId = :exerciseTemplateId 
        AND wsl.status = 'COMPLETED'
        AND se.sessionId != :currentSessionId
        ORDER BY wsl.startTime DESC
        LIMIT 1
    """)
    fun getLatestCompletedExerciseRecord(exerciseTemplateId: Long, currentSessionId: Long): Flow<SessionExerciseWithSetsAndSession?>

    @Transaction
    @Query("""
        SELECT se.* FROM session_exercises se
        JOIN workout_session_logs wsl ON se.sessionId = wsl.sessionId
        WHERE se.libraryExerciseId = :libraryExerciseId 
        AND wsl.status = 'COMPLETED'
        AND se.sessionId != :currentSessionId
        ORDER BY wsl.startTime DESC
        LIMIT 1
    """)
    fun getLatestExecutionByLibraryId(libraryExerciseId: Long, currentSessionId: Long): Flow<SessionExerciseWithSetsAndSession?>

    @Transaction
    @Query("SELECT * FROM session_exercises WHERE sessionId = :sessionId ORDER BY exerciseOrder ASC")
    fun getExercisesWithTemplateForSession(sessionId: Long): Flow<List<SessionExerciseWithTemplate>>

    @Transaction
    @Query("SELECT * FROM session_exercises WHERE sessionId = :sessionId ORDER BY exerciseOrder ASC")
    fun getExercisesWithSetsForSession(sessionId: Long): Flow<List<SessionExerciseWithTemplateAndSets>>

    @Transaction
    @Query("SELECT * FROM session_exercises WHERE sessionId = :sessionId ORDER BY exerciseOrder ASC")
    fun getHistoricalExercisesWithSets(sessionId: Long): Flow<List<SessionExerciseWithSets>>

    @Transaction
    @Query("""
        SELECT se.* FROM session_exercises se
        JOIN workout_session_logs wsl ON se.sessionId = wsl.sessionId
        WHERE se.exerciseTemplateId = :exerciseTemplateId 
        AND wsl.status = 'COMPLETED'
        ORDER BY wsl.startTime DESC
    """)
    fun getCompletedExerciseRecords(exerciseTemplateId: Long): Flow<List<SessionExerciseWithSetsAndSession>>

    @Transaction
    @Query("""
        SELECT * FROM session_exercises 
        WHERE exerciseTemplateId = :exerciseTemplateId 
        AND sessionId IN (SELECT sessionId FROM workout_session_logs WHERE status = 'COMPLETED' AND startTime < :startTime)
    """)
    suspend fun getCompletedExerciseRecordsBefore(exerciseTemplateId: Long, startTime: Long): List<SessionExerciseWithSets>

    @Transaction
    @Query("SELECT * FROM session_exercises WHERE sessionId = :sessionId ORDER BY exerciseOrder ASC")
    suspend fun getExercisesWithSetsForSessionList(sessionId: Long): List<SessionExerciseWithTemplateAndSets>

    @Query("SELECT * FROM session_exercises WHERE sessionId = :sessionId ORDER BY exerciseOrder ASC")
    fun getExercisesForSession(sessionId: Long): Flow<List<SessionExercise>>

    @Query("SELECT * FROM session_exercises WHERE sessionId = :sessionId")
    suspend fun getExercisesForSessionList(sessionId: Long): List<SessionExercise>

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

    @Delete
    suspend fun deleteSessionExercise(exercise: SessionExercise)

    @Query("SELECT * FROM workout_session_logs WHERE status = 'COMPLETED' ORDER BY createdAt DESC")
    fun getCompletedSessions(): Flow<List<WorkoutSession>>

    @Query("""
        SELECT sess.*, IFNULL(SUM(s.weight * s.reps), 0) as totalVolume
        FROM workout_session_logs sess
        LEFT JOIN session_exercises e ON sess.sessionId = e.sessionId
        LEFT JOIN session_sets s ON e.sessionExerciseId = s.sessionExerciseId
        WHERE sess.status = 'COMPLETED'
        GROUP BY sess.sessionId
        ORDER BY sess.createdAt DESC
    """)
    fun getCompletedSessionsWithVolume(): Flow<List<WorkoutSessionWithVolume>>

    @Query("""
        SELECT sess.*, 
               IFNULL(SUM(s.weight * s.reps), 0) as totalVolume,
               (SELECT COUNT(*) FROM session_exercises WHERE sessionId = sess.sessionId) as exerciseCount,
               (SELECT COUNT(*) FROM session_sets ss JOIN session_exercises ex ON ss.sessionExerciseId = ex.sessionExerciseId WHERE ex.sessionId = sess.sessionId) as setCount,
               (SELECT COUNT(*) FROM personal_records WHERE weightPRSessionId = sess.sessionId OR estimated1RMSessionId = sess.sessionId) as prCount,
               IFNULL((SELECT GROUP_CONCAT(exerciseName, ', ') FROM session_exercises WHERE sessionId = sess.sessionId), '') as exerciseNames
        FROM workout_session_logs sess
        LEFT JOIN session_exercises e ON sess.sessionId = e.sessionId
        LEFT JOIN session_sets s ON e.sessionExerciseId = s.sessionExerciseId
        WHERE sess.status = 'COMPLETED'
        GROUP BY sess.sessionId
        ORDER BY sess.createdAt DESC
    """)
    fun getCompletedSessionsWithStats(): Flow<List<WorkoutSessionWithStats>>

    @Query("""
        SELECT SUM(s.weight * s.reps) 
        FROM session_sets s 
        JOIN session_exercises e ON s.sessionExerciseId = e.sessionExerciseId 
        JOIN workout_session_logs sess ON e.sessionId = sess.sessionId 
        WHERE sess.status = 'COMPLETED' AND sess.createdAt >= :since
    """)
    fun getVolumeSince(since: Long): Flow<Double?>

    @Query("""
        SELECT SUM(s.weight * s.reps) 
        FROM session_sets s 
        JOIN session_exercises e ON s.sessionExerciseId = e.sessionExerciseId 
        JOIN workout_session_logs sess ON e.sessionId = sess.sessionId 
        WHERE sess.status = 'COMPLETED'
    """)
    fun getTotalVolume(): Flow<Double?>

    @Query("""
        SELECT sess.createdAt as date, SUM(s.weight * s.reps) as volume
        FROM session_sets s 
        JOIN session_exercises e ON s.sessionExerciseId = e.sessionExerciseId 
        JOIN workout_session_logs sess ON e.sessionId = sess.sessionId 
        WHERE sess.status = 'COMPLETED' AND sess.createdAt >= :since
        GROUP BY CAST(sess.createdAt / 86400000 AS INTEGER)
        ORDER BY sess.createdAt ASC
    """)
    fun getDailyVolumeHistory(since: Long): Flow<List<DailyVolume>>

    @Query("""
        SELECT sess.createdAt as date, MAX(s.weight) as maxWeight, MAX(s.weight * (1 + s.reps / 30.0)) as maxE1RM
        FROM session_sets s
        JOIN session_exercises e ON s.sessionExerciseId = e.sessionExerciseId
        JOIN workout_session_logs sess ON e.sessionId = sess.sessionId
        WHERE sess.status = 'COMPLETED' AND e.exerciseTemplateId = :exerciseId
        GROUP BY sess.sessionId
        ORDER BY sess.createdAt ASC
    """)
    fun getExerciseStrengthHistory(exerciseId: Long): Flow<List<ExerciseStrengthHistory>>

    @Delete
    suspend fun deleteSession(session: WorkoutSession)
}
