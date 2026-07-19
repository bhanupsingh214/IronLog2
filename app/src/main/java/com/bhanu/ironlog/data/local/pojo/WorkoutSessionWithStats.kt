package com.bhanu.ironlog.data.local.pojo

import androidx.room.Embedded
import com.bhanu.ironlog.data.local.entity.WorkoutSession

data class WorkoutSessionWithStats(
    @Embedded val session: WorkoutSession,
    val totalVolume: Double,
    val exerciseCount: Int,
    val setCount: Int,
    val prCount: Int,
    val exerciseNames: String // Concatenated names for searching
)
