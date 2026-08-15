package com.bhanu.ironlog.data.model.analytics

data class PeriodRecap(
    val periodName: String, // e.g., "August 2026" or "2026"
    val workoutCount: Int,
    val totalVolume: Double,
    val totalDurationSeconds: Long,
    val totalSets: Int,
    val prCount: Int,
    val topMuscleGroups: List<MuscleGroupCount>,
    val workoutConsistency: Float, // percentage of days with workouts in the period
    val averageWorkoutDurationMinutes: Int
)

data class MuscleGroupCount(
    val muscleGroup: String,
    val count: Int
)

data class ProgressSummary(
    val totalWorkouts: Int,
    val totalVolume: Double,
    val weeklyFrequency: Float, // average workouts per week
    val monthlyVolumeTrend: List<Double>,
    val muscleGroupDistribution: List<MuscleGroupCount>
)
