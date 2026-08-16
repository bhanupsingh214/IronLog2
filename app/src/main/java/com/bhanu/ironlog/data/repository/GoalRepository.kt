package com.bhanu.ironlog.data.repository

import com.bhanu.ironlog.data.local.dao.GoalDao
import com.bhanu.ironlog.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepository @Inject constructor(
    private val goalDao: GoalDao
) {
    fun getGoals(): Flow<List<GoalEntity>> = goalDao.getAllGoals()

    suspend fun createGoal(goal: GoalEntity): Long = goalDao.insertGoal(goal)

    suspend fun updateGoal(goal: GoalEntity) = goalDao.updateGoal(goal)

    suspend fun deleteGoal(goal: GoalEntity) = goalDao.deleteGoal(goal)

    suspend fun deleteAllGoals() = goalDao.deleteAllGoals()
}