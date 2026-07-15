package com.bhanu.ironlog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_session_logs")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true)
    val sessionId: Long = 0,
    val programId: Long,
    val workoutDayId: Long,
    val dayName: String,
    val programName: String,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val status: String = "ACTIVE", // ACTIVE / COMPLETED
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedExerciseIds: String = "", // Comma separated IDs
    val durationSeconds: Long = 0
)
