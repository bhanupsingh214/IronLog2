package com.bhanu.ironlog.data.local.dao

import androidx.room.*
import com.bhanu.ironlog.data.local.entity.PersonalRecordEntity
import com.bhanu.ironlog.data.local.pojo.PRWithExerciseName
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalRecordDao {
    @Query("SELECT * FROM personal_records WHERE exerciseTemplateId = :exerciseId")
    suspend fun getPRForExercise(exerciseId: Long): PersonalRecordEntity?

    @Query("SELECT * FROM personal_records WHERE exerciseTemplateId = :exerciseId")
    fun getPRForExerciseFlow(exerciseId: Long): Flow<PersonalRecordEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePR(pr: PersonalRecordEntity)

    @Query("SELECT * FROM personal_records")
    fun getAllPRs(): Flow<List<PersonalRecordEntity>>

    @Transaction
    @Query("SELECT * FROM personal_records")
    fun getAllPRsWithExerciseName(): Flow<List<PRWithExerciseName>>
}
