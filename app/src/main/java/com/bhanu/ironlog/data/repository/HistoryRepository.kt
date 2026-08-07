package com.bhanu.ironlog.data.repository

import com.bhanu.ironlog.data.local.dao.WorkoutSessionDao
import com.bhanu.ironlog.data.local.entity.WorkoutSession
import com.bhanu.ironlog.data.local.pojo.*
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepository @Inject constructor(
    private val workoutSessionDao: WorkoutSessionDao
) {
    fun getCompletedSessions(): Flow<List<WorkoutSession>> = workoutSessionDao.getCompletedSessions()

    fun getCompletedSessionsWithStats(): Flow<List<WorkoutSessionWithStats>> = 
        workoutSessionDao.getCompletedSessionsWithStats()

    fun getCompletedSessionsWithVolume(): Flow<List<WorkoutSessionWithVolume>> = 
        workoutSessionDao.getCompletedSessionsWithVolume()

    fun getSessionById(sessionId: Long): Flow<WorkoutSession?> = workoutSessionDao.getSessionById(sessionId)

    fun getTotalVolume(): Flow<Double?> = workoutSessionDao.getTotalVolume()

    fun getWeeklyVolume(): Flow<Double?> = workoutSessionDao.getVolumeSince(getStartOfWeek())

    fun getVolumeSince(since: Long): Flow<Double?> = workoutSessionDao.getVolumeSince(since)

    fun getDailyVolumeHistory(since: Long): Flow<List<DailyVolume>> =
        workoutSessionDao.getDailyVolumeHistory(since)

    fun getExerciseStrengthHistory(exerciseId: Long): Flow<List<ExerciseStrengthHistory>> =
        workoutSessionDao.getExerciseStrengthHistory(exerciseId)

    private fun getStartOfWeek(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
