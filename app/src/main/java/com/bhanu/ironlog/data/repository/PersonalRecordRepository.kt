package com.bhanu.ironlog.data.repository

import com.bhanu.ironlog.data.local.dao.PersonalRecordDao
import com.bhanu.ironlog.data.local.entity.PersonalRecordEntity
import com.bhanu.ironlog.data.local.pojo.PRWithExerciseName
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonalRecordRepository @Inject constructor(
    private val personalRecordDao: PersonalRecordDao
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
}
