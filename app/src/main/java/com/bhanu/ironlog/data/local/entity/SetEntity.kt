package com.bhanu.ironlog.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercise_sets",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val exerciseId: Long,
    val sessionId: Long = 0, // 0 for template/default, >0 for specific session
    val setNumber: Int = 1,
    val weight: Double = 0.0,
    val reps: Int = 0,
    val rpe: Double? = null,
    val rir: Int? = null,
    val notes: String = "",
    val isCompleted: Boolean = false,
    val setType: String = "Working", // Warm-up, Working, Top Set, Back-off, Drop Set
    val order: Int,
    val createdAt: Long = System.currentTimeMillis()
)
