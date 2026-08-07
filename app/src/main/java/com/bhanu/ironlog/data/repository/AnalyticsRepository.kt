package com.bhanu.ironlog.data.repository

import com.bhanu.ironlog.data.local.entity.ExerciseEntity
import com.bhanu.ironlog.data.local.pojo.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsRepository @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val programRepository: ProgramRepository,
    private val prRepository: PersonalRecordRepository
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

    fun getDailyVolumeHistory(since: Long): Flow<List<DailyVolume>> =
        historyRepository.getDailyVolumeHistory(since)

    fun getExerciseStrengthHistory(exerciseId: Long): Flow<List<ExerciseStrengthHistory>> =
        historyRepository.getExerciseStrengthHistory(exerciseId)
}
