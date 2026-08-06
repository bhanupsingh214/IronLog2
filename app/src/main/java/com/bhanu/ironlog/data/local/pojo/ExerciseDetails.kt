package com.bhanu.ironlog.data.local.pojo

data class ExerciseDetails(
    val exerciseTemplateId: Long,
    val exerciseName: String,
    val muscleGroup: String,
    val totalSessions: Int,
    val totalVolume: Double,
    val bestWeight: Double,
    val estimated1RM: Double,
    val firstPerformed: Long,
    val lastPerformed: Long,
    val sessionHistory: List<ExerciseSessionRecord>
)

data class ExerciseSessionRecord(
    val sessionId: Long,
    val date: Long,
    val workoutName: String,
    val status: String,
    val sessionVolume: Double,
    val sets: List<com.bhanu.ironlog.data.local.entity.SessionSet>
)
