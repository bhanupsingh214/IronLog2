package com.bhanu.ironlog.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "session_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSession::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class SessionExercise(
    @PrimaryKey(autoGenerate = true)
    val sessionExerciseId: Long = 0,
    val sessionId: Long,
    val exerciseTemplateId: Long,
    val libraryExerciseId: Long = 0, // Identity Reference
    
    // Identity Snapshot (Independent of Library)
    val exerciseName: String = "",
    val muscleGroup: String = "",
    val equipment: String = "",
    val exerciseType: String = "Compound",
    
    // Prescription Snapshot (Independent of Program)
    val targetSets: Int = 3,
    val targetRepMin: Int = 8,
    val targetRepMax: Int = 12,
    val targetRPE: Double? = null,
    val restTimerSeconds: Int = 90,
    
    val exerciseOrder: Int,
    val isSwapped: Boolean = false,
    val originalExerciseId: Long? = null,
    val status: String = "NOT_STARTED", // NOT_STARTED, IN_PROGRESS, COMPLETED, SKIPPED
    val notes: String = ""
)
