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
    val status: String = "ACTIVE", // ACTIVE / COMPLETED / DISCARDED
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedExerciseIds: String = "", // Comma separated IDs
    val durationSeconds: Long = 0,
    
    // Engine State
    val currentExerciseId: Long? = null,
    val currentSetNumber: Int? = null,
    val completedSetsCount: Int = 0,
    val hasShownBackgroundDialog: Boolean = false,
    
    // Rest Timer State
    val timerStartTime: Long? = null,
    val timerDurationSeconds: Int? = null,
    val timerState: String = "IDLE", // IDLE, RUNNING, PAUSED, COMPLETED, DISMISSED
    val timerPausedRemainingSeconds: Int? = null
)
