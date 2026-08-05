package com.bhanu.ironlog.data.local.pojo

data class WorkoutCompletionSummary(
    val sessionId: Long,
    val workoutName: String,
    val programName: String,
    val durationSeconds: Long,
    val totalVolume: Double,
    val exercisesCompleted: Int,
    val totalExercises: Int,
    val setsCompleted: Int,
    val totalSets: Int,
    val skippedExercises: Int,
    val completionPercentage: Float,
    val startTime: Long,
    val endTime: Long
)
