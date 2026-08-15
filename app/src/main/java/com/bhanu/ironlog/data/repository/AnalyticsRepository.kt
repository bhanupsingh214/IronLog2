package com.bhanu.ironlog.data.repository

import com.bhanu.ironlog.data.local.entity.ExerciseEntity
import com.bhanu.ironlog.data.local.pojo.*
import com.bhanu.ironlog.data.model.analytics.*
import kotlinx.coroutines.flow.*
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsRepository @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val programRepository: ProgramRepository,
    private val prRepository: PersonalRecordRepository,
    private val workoutSessionDao: com.bhanu.ironlog.data.local.dao.WorkoutSessionDao
) {
    fun getAllExercises(): Flow<List<ExerciseEntity>> =
        programRepository.getAllExercises()

    fun getTotalWorkoutsCount(): Flow<Int> =
        historyRepository.getCompletedSessions().map { it.size }

    fun getTotalVolume(): Flow<Double> =
        historyRepository.getTotalVolume().map { it ?: 0.0 }

    fun getWeightPRCount(): Flow<Int> =
        prRepository.getAllPRs().map { list ->
            list.count { it.weightPR > 0.0 }
        }

    fun getEstimated1RMPRCount(): Flow<Int> =
        prRepository.getAllPRs().map { list ->
            list.count { it.estimated1RM > 0.0 }
        }

    fun getWeeklyVolume(): Flow<Double> =
        historyRepository.getWeeklyVolume().map { it ?: 0.0 }

    fun getMonthlyVolume(): Flow<Double> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return historyRepository.getVolumeSince(calendar.timeInMillis).map { it ?: 0.0 }
    }

    fun getLatestPRs(limit: Int = 3): Flow<List<PRWithExerciseName>> =
        prRepository.getAllPRsWithExerciseName().map { list ->
            list.sortedByDescending { it.pr.updatedAt }.take(limit)
        }

    fun getTrackableExercises(): Flow<List<TrackableExercise>> =
        historyRepository.getTrackableExercises()

    fun getDailyVolumeHistory(since: Long): Flow<List<DailyVolume>> =
        historyRepository.getDailyVolumeHistory(since)

    fun getExerciseStrengthHistory(libraryId: Long, templateId: Long): Flow<List<ExerciseStrengthHistory>> =
        historyRepository.getExerciseStrengthHistoryCanonical(libraryId, templateId)

    fun getMonthlyRecap(year: Int, month: Int): Flow<PeriodRecap?> = flow {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        val end = calendar.timeInMillis

        historyRepository.getCompletedSessionsWithStats().collect { allSessions ->
            val periodSessions = allSessions.filter { it.session.createdAt in start until end }
            if (periodSessions.isEmpty()) {
                emit(null)
                return@collect
            }

            val exercises = workoutSessionDao.getCompletedExercisesSince(start)
                .filter { it.sessionId in periodSessions.map { s -> s.session.sessionId } }

            val muscleGroups = exercises.groupBy { it.muscleGroup }
                .map { MuscleGroupCount(it.key, it.value.size) }
                .sortedByDescending { it.count }

            val totalDuration = periodSessions.sumOf { it.session.durationSeconds }

            val recap = PeriodRecap(
                periodName = java.text.DateFormat.getDateInstance().format(Date(start)), // Placeholder format
                workoutCount = periodSessions.size,
                totalVolume = periodSessions.sumOf { it.totalVolume },
                totalDurationSeconds = totalDuration,
                totalSets = periodSessions.sumOf { it.setCount },
                prCount = periodSessions.sumOf { it.prCount },
                topMuscleGroups = muscleGroups,
                workoutConsistency = periodSessions.size.toFloat() / calendar.getActualMaximum(Calendar.DAY_OF_MONTH),
                averageWorkoutDurationMinutes = if (periodSessions.isNotEmpty()) (totalDuration / periodSessions.size / 60).toInt() else 0
            )
            emit(recap)
        }
    }

    fun getYearlyRecap(year: Int): Flow<PeriodRecap?> = flow {
        val calendar = Calendar.getInstance()
        calendar.set(year, 0, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis

        calendar.add(Calendar.YEAR, 1)
        val end = calendar.timeInMillis

        historyRepository.getCompletedSessionsWithStats().collect { allSessions ->
            val periodSessions = allSessions.filter { it.session.createdAt in start until end }
            if (periodSessions.isEmpty()) {
                emit(null)
                return@collect
            }

            val exercises = workoutSessionDao.getCompletedExercisesSince(start)
                .filter { it.sessionId in periodSessions.map { s -> s.session.sessionId } }

            val muscleGroups = exercises.groupBy { it.muscleGroup }
                .map { MuscleGroupCount(it.key, it.value.size) }
                .sortedByDescending { it.count }

            val totalDuration = periodSessions.sumOf { it.session.durationSeconds }

            val recap = PeriodRecap(
                periodName = "$year",
                workoutCount = periodSessions.size,
                totalVolume = periodSessions.sumOf { it.totalVolume },
                totalDurationSeconds = totalDuration,
                totalSets = periodSessions.sumOf { it.setCount },
                prCount = periodSessions.sumOf { it.prCount },
                topMuscleGroups = muscleGroups,
                workoutConsistency = periodSessions.size.toFloat() / 365f, // simplified
                averageWorkoutDurationMinutes = if (periodSessions.isNotEmpty()) (totalDuration / periodSessions.size / 60).toInt() else 0
            )
            emit(recap)
        }
    }

    fun getProgressSummary(): Flow<ProgressSummary> = combine(
        getTotalWorkoutsCount(),
        getTotalVolume(),
        historyRepository.getCompletedSessions(),
        historyRepository.getCompletedSessionsWithVolume()
    ) { workouts, totalVolume, sessions, sessionsWithVolume ->
        // Calculate weekly frequency (last 30 days)
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        val recentSessions = sessions.filter { it.createdAt >= thirtyDaysAgo }
        val frequency = recentSessions.size / 4.28f // sessions per week over ~30 days

        // Real volume trend (last 6 months)
        val trend = mutableListOf<Double>()
        for (i in 5 downTo 0) {
            val monthCal = Calendar.getInstance()
            monthCal.add(Calendar.MONTH, -i)
            monthCal.set(Calendar.DAY_OF_MONTH, 1)
            monthCal.set(Calendar.HOUR_OF_DAY, 0)
            monthCal.set(Calendar.MINUTE, 0)
            monthCal.set(Calendar.SECOND, 0)
            monthCal.set(Calendar.MILLISECOND, 0)
            val start = monthCal.timeInMillis

            monthCal.add(Calendar.MONTH, 1)
            val end = monthCal.timeInMillis

            val monthVolume = sessionsWithVolume
                .filter { it.session.createdAt in start until end }
                .sumOf { it.totalVolume }
            trend.add(monthVolume)
        }

        ProgressSummary(
            totalWorkouts = workouts,
            totalVolume = totalVolume,
            weeklyFrequency = frequency,
            monthlyVolumeTrend = trend,
            muscleGroupDistribution = emptyList()
        )
    }

    fun getMuscleGroupDistribution(since: Long): Flow<List<MuscleGroupCount>> = flow {
        val exercises = workoutSessionDao.getCompletedExercisesSince(since)
        val distribution = exercises.groupBy { it.muscleGroup }
            .map { MuscleGroupCount(it.key, it.value.size) }
            .sortedByDescending { it.count }
        emit(distribution)
    }
}
