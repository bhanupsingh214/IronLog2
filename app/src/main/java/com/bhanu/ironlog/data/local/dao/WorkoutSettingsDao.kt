package com.bhanu.ironlog.data.local.dao

import androidx.room.*
import com.bhanu.ironlog.data.local.entity.WorkoutSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSettingsDao {
    @Query("SELECT * FROM workout_settings WHERE id = 1")
    fun getSettings(): Flow<WorkoutSettingsEntity?>

    @Query("SELECT * FROM workout_settings WHERE id = 1")
    suspend fun getSettingsOnce(): WorkoutSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSettings(settings: WorkoutSettingsEntity)
}
