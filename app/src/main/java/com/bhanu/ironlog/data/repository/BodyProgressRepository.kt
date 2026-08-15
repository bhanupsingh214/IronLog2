package com.bhanu.ironlog.data.repository

import com.bhanu.ironlog.data.local.dao.UserProfileDao
import com.bhanu.ironlog.data.local.entity.BodyWeightEntry
import com.bhanu.ironlog.data.local.entity.UserProfileEntity
import com.bhanu.ironlog.data.local.entity.WaistEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BodyProgressRepository @Inject constructor(
    private val userProfileDao: UserProfileDao
) {
    fun getProfile(): Flow<UserProfileEntity?> = userProfileDao.getProfileFlow()

    suspend fun getProfileOnce(): UserProfileEntity? = userProfileDao.getProfileOnce()

    suspend fun saveProfile(profile: UserProfileEntity) {
        userProfileDao.insertOrUpdateProfile(profile.copy(updatedAt = System.currentTimeMillis()))
    }

    // Weight
    fun getWeightHistory(): Flow<List<BodyWeightEntry>> = userProfileDao.getWeightHistoryFlow()

    fun getLatestWeight(): Flow<BodyWeightEntry?> = userProfileDao.getLatestWeightFlow()

    suspend fun addWeightEntry(weightKg: Double, timestamp: Long, notes: String = ""): Long {
        return userProfileDao.insertWeightEntry(BodyWeightEntry(weightKg = weightKg, timestamp = timestamp, notes = notes))
    }

    suspend fun updateWeightEntry(entry: BodyWeightEntry) {
        userProfileDao.updateWeightEntry(entry)
    }

    suspend fun deleteWeightEntry(entry: BodyWeightEntry) {
        userProfileDao.deleteWeightEntry(entry)
    }

    // Waist
    fun getWaistHistory(): Flow<List<WaistEntry>> = userProfileDao.getWaistHistoryFlow()

    fun getLatestWaist(): Flow<WaistEntry?> = userProfileDao.getLatestWaistFlow()

    suspend fun addWaistEntry(circumferenceCm: Double, timestamp: Long, notes: String = ""): Long {
        return userProfileDao.insertWaistEntry(WaistEntry(circumferenceCm = circumferenceCm, timestamp = timestamp, notes = notes))
    }

    suspend fun updateWaistEntry(entry: WaistEntry) {
        userProfileDao.updateWaistEntry(entry)
    }

    suspend fun deleteWaistEntry(entry: WaistEntry) {
        userProfileDao.deleteWaistEntry(entry)
    }
}
