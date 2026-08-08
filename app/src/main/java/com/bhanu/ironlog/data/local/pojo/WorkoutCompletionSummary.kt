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
    val averageRPE: Double?,
    val skippedExercises: Int,
    val completionPercentage: Float,
    val startTime: Long,
    val endTime: Long,
    val achievements: List<PersonalRecordAchievement> = emptyList(),
    val comparison: WorkoutComparison? = null
)

data class WorkoutComparison(
    val previousVolume: Double?,
    val previousDurationSeconds: Long?,
    val previousSetsCompleted: Int?,
    val previousAverageRPE: Double?,
    val isFirstSession: Boolean
)
