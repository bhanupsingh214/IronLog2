package com.bhanu.ironlog.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutDayEntity::class,
            parentColumns = ["id"],
            childColumns = ["dayId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LibraryExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["libraryExerciseId"],
            onDelete = ForeignKey.RESTRICT
        )
    ]
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayId: Long,
    val libraryExerciseId: Long = 0, // Reference to Exercise Library
    
    // Prescription Data (Blueprint)
    val order: Int,
    val enabled: Boolean = true,
    val notes: String = "",
    val restTimerSeconds: Int = 90,
    val useDefaultRestTimer: Boolean = true,
    val targetSets: Int = 3,
    val targetRepMin: Int = 8,
    val targetRepMax: Int = 12,
    val targetRPE: Double? = null,
    
    // Legacy fields - kept for migration safety and history snapshots
    // These will be removed/deprecated in future PRs after full migration
    val name: String = "",
    val muscleGroup: String = "",
    val equipment: String = "",
    val exerciseType: String = "Compound",

    val createdAt: Long = System.currentTimeMillis()
)
