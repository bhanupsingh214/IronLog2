package com.bhanu.ironlog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "personal_records",
    primaryKeys = ["libraryExerciseId", "exerciseTemplateId"]
)
data class PersonalRecordEntity(
    val libraryExerciseId: Long, // Link to canonical physical exercise
    val exerciseTemplateId: Long, // Fallback/Isolated link for custom exercises
    
    // Weight PR
    val weightPR: Double = 0.0,
    val weightPRDate: Long = 0L,
    val weightPRSessionId: Long = 0L,
    
    // Estimated 1RM PR
    val estimated1RM: Double = 0.0,
    val estimated1RMDate: Long = 0L,
    val estimated1RMSessionId: Long = 0L,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
