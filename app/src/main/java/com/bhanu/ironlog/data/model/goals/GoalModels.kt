package com.bhanu.ironlog.data.model.goals

import com.bhanu.ironlog.data.local.entity.GoalEntity

enum class GoalType(val key: String, val label: String, val unit: String) {
    WEIGHT("WEIGHT", "Weight", "kg"),
    WAIST("WAIST", "Waist", "cm"),
    EXERCISE_PR("EXERCISE_PR", "Exercise PR", "kg"),
    WORKOUT_FREQUENCY("WORKOUT_FREQUENCY", "Workout Frequency", "sessions")
}

enum class GoalTrend {
    IMPROVING,
    STABLE,
    MOVING_AWAY,
    INSUFFICIENT_DATA
}

enum class GoalStatus {
    ON_TRACK,
    BEHIND,
    OVERDUE,
    COMPLETED
}

data class GoalTrendPoint(
    val timestamp: Long,
    val value: Double
)

data class GoalProgress(
    val goal: GoalEntity,
    val currentValue: Double?,
    val progress: Double?,
    val trend: GoalTrend?,
    val trendRatePerDay: Double?,
    val status: GoalStatus?,
    val expectedProgress: Double?
)
