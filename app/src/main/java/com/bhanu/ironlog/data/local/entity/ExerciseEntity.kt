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
        )
    ]
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayId: Long,
    val name: String,
    val muscleGroup: String = "",
    val equipment: String = "",
    val exerciseType: String = "Compound",
    val order: Int,
    val enabled: Boolean = true,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
