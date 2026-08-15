package com.bhanu.ironlog.data.local.dao

import androidx.room.*
import com.bhanu.ironlog.data.local.entity.BodyWeightEntry
import com.bhanu.ironlog.data.local.entity.UserProfileEntity
import com.bhanu.ironlog.data.local.entity.WaistEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getProfileFlow(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getProfileOnce(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

    // Weight
    @Query("SELECT * FROM body_weight_history ORDER BY timestamp DESC")
    fun getWeightHistoryFlow(): Flow<List<BodyWeightEntry>>

    @Query("SELECT * FROM body_weight_history ORDER BY timestamp DESC")
    suspend fun getWeightHistoryOnce(): List<BodyWeightEntry>

    @Query("SELECT * FROM body_weight_history ORDER BY timestamp DESC LIMIT 1")
    fun getLatestWeightFlow(): Flow<BodyWeightEntry?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightEntry(entry: BodyWeightEntry): Long

    @Update
    suspend fun updateWeightEntry(entry: BodyWeightEntry)

    @Delete
    suspend fun deleteWeightEntry(entry: BodyWeightEntry)

    // Waist
    @Query("SELECT * FROM waist_history ORDER BY timestamp DESC")
    fun getWaistHistoryFlow(): Flow<List<WaistEntry>>

    @Query("SELECT * FROM waist_history ORDER BY timestamp DESC")
    suspend fun getWaistHistoryOnce(): List<WaistEntry>

    @Query("SELECT * FROM waist_history ORDER BY timestamp DESC LIMIT 1")
    fun getLatestWaistFlow(): Flow<WaistEntry?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaistEntry(entry: WaistEntry): Long

    @Update
    suspend fun updateWaistEntry(entry: WaistEntry)

    @Delete
    suspend fun deleteWaistEntry(entry: WaistEntry)
}
