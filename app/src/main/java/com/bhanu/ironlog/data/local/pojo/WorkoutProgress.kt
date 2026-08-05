package com.bhanu.ironlog.data.local.pojo

data class WorkoutProgress(
    val completedExercises: Int,
    val totalExercises: Int,
    val completedSets: Int,
    val totalSets: Int,
    val percentage: Float
)
