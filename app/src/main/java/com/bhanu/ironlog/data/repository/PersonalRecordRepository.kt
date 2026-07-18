package com.bhanu.ironlog.data.repository

import com.bhanu.ironlog.data.local.dao.PersonalRecordDao
import com.bhanu.ironlog.data.local.dao.WorkoutSessionDao
import com.bhanu.ironlog.data.local.entity.PersonalRecordEntity
import com.bhanu.ironlog.data.local.pojo.ExerciseStrengthHistory
import com.bhanu.ironlog.data.local.pojo.PRWithExerciseName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonalRecordRepository @Inject constructor(
    private val personalRecordDao: PersonalRecordDao,
    private val sessionDao: WorkoutSessionDao
) {
    suspend fun getPRForExercise(exerciseId: Long): PersonalRecordEntity? =
        personalRecordDao.getPRForExercise(exerciseId)

    fun getPRForExerciseFlow(exerciseId: Long): Flow<PersonalRecordEntity?> =
        personalRecordDao.getPRForExerciseFlow(exerciseId)

    suspend fun updatePR(pr: PersonalRecordEntity) =
        personalRecordDao.insertOrUpdatePR(pr)

    fun getAllPRs(): Flow<List<PersonalRecordEntity>> =
        personalRecordDao.getAllPRs()

    fun getAllPRsWithExerciseName(): Flow<List<PRWithExerciseName>> =
        personalRecordDao.getAllPRsWithExerciseName()

    /**
     * Returns the complete PR progression for an exercise.
     * Points are filtered to be monotonically increasing.
     */
    fun getPRProgression(exerciseId: Long, isE1RM: Boolean): Flow<List<ExerciseStrengthHistory>> =
        sessionDao.getExerciseStrengthHistory(exerciseId).map { history ->
            val prPoints = mutableListOf<ExerciseStrengthHistory>()
            var currentMax = 0.0
            
            history.forEach { point ->
                val value = if (isE1RM) point.maxE1RM else point.maxWeight
                if (value > currentMax) {
                    currentMax = value
                    prPoints.add(point)
                }
            }
            prPoints
        }
}
