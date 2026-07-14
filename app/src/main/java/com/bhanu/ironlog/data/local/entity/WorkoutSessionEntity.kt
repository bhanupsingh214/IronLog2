package com.bhanu.ironlog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayId: Long,
    val dayName: String,
    val programName: String,
    val date: Long = System.currentTimeMillis(),
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val durationSeconds: Long = 0,
    val isCompleted: Boolean = false,
    val completedExerciseIds: String = "" // Comma separated IDs
)
