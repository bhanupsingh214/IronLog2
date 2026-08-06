package com.bhanu.ironlog.data.local.pojo

import com.bhanu.ironlog.data.local.entity.WorkoutSession

data class WorkoutDetails(
    val session: WorkoutSession,
    val summary: WorkoutCompletionSummary,
    val exercises: List<SessionExerciseWithTemplateAndSets>
)
