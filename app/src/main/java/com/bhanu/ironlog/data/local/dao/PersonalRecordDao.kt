package com.bhanu.ironlog.data.local.dao

import androidx.room.*
import com.bhanu.ironlog.data.local.entity.PersonalRecordEntity
import com.bhanu.ironlog.data.local.pojo.PRWithExerciseName
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalRecordDao {
    @Query("SELECT * FROM personal_records WHERE libraryExerciseId = :libraryId AND exerciseTemplateId = :templateId")
    suspend fun getPRByIds(libraryId: Long, templateId: Long): PersonalRecordEntity?

    @Query("SELECT * FROM personal_records WHERE libraryExerciseId = :libraryId AND exerciseTemplateId = :templateId")
    fun getPRByIdsFlow(libraryId: Long, templateId: Long): Flow<PersonalRecordEntity?>

    @Query("SELECT * FROM personal_records WHERE exerciseTemplateId = :exerciseId AND libraryExerciseId = 0")
    suspend fun getPRForExercise(exerciseId: Long): PersonalRecordEntity?

    @Query("SELECT * FROM personal_records WHERE exerciseTemplateId = :exerciseId AND libraryExerciseId = 0")
    fun getPRForExerciseFlow(exerciseId: Long): Flow<PersonalRecordEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePR(pr: PersonalRecordEntity)

    @Query("SELECT * FROM personal_records")
    fun getAllPRs(): Flow<List<PersonalRecordEntity>>

    @Transaction
    @Query("SELECT * FROM personal_records")
    fun getAllPRsWithExerciseName(): Flow<List<PRWithExerciseName>>
}
