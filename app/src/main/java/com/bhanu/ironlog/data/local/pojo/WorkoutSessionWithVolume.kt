package com.bhanu.ironlog.data.local.pojo

import androidx.room.Embedded
import com.bhanu.ironlog.data.local.entity.WorkoutSession

data class WorkoutSessionWithVolume(
    @Embedded val session: WorkoutSession,
    val totalVolume: Double
)
