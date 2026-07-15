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
    val exerciseOrder: Int,
    val isSwapped: Boolean = false,
    val originalExerciseId: Long? = null
)
