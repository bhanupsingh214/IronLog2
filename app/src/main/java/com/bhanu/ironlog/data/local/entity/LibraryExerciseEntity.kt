package com.bhanu.ironlog.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercise_library",
    indices = [
        Index(value = ["normalizedName"], unique = true),
        Index(value = ["systemKey"], unique = true)
    ]
)
data class LibraryExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val systemKey: String? = null, // e.g. "bench_press_barbell"
    val name: String,
    val normalizedName: String, // e.g. "benchpressbarbell"
    val muscleGroup: String,
    val equipment: String = "None",
    val exerciseType: String = "Compound", // Compound, Isolation, etc.
    val createdBy: String = "System", // System, User
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
