package com.bhanu.ironlog.ui.screens.programs

data class WorkoutSetUiModel(
    val id: Long,
    val setNumber: Int,
    val weight: Double,
    val reps: Int,
    val rpe: Double? = null,
    val notes: String = "",
    val isCompleted: Boolean = false,
    val setType: String = "Working"
)
