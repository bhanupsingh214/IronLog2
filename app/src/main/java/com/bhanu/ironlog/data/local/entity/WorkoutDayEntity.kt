package com.bhanu.ironlog.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_days",
    foreignKeys = [
        ForeignKey(
            entity = ProgramEntity::class,
            parentColumns = ["id"],
            childColumns = ["programId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WorkoutDayEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val programId: Long,
    val name: String,
    val order: Int,
    val notes: String = "",
    val isEnabled: Boolean = true,
    val estimatedDurationMinutes: Int = 0
)
