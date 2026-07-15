package com.bhanu.ironlog.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "session_sets",
    foreignKeys = [
        ForeignKey(
            entity = SessionExercise::class,
            parentColumns = ["sessionExerciseId"],
            childColumns = ["sessionExerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionExerciseId"])]
)
data class SessionSet(
    @PrimaryKey(autoGenerate = true)
    val sessionSetId: Long = 0,
    val sessionExerciseId: Long,
    val setNumber: Int,
    val weight: Double,
    val reps: Int,
    val rpe: Double? = null,
    val completed: Boolean = false,
    val setType: String = "Working", // Warm-up, Working, Top Set, Back-off, Drop Set
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
