package com.bhanu.ironlog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val goalId: Long = 0,
    val type: String,
    val targetValue: Double,
    val startingValue: Double,
    val libraryExerciseId: Long? = null,
    val frequencyCount: Int? = null,
    val frequencyPeriod: String? = null,
    val startDate: Long,
    val deadline: Long? = null
)