package com.bhanu.ironlog.data.local.pojo

import androidx.room.Embedded
import com.bhanu.ironlog.data.local.entity.WorkoutDayEntity

data class WorkoutDayWithStats(
    @Embedded val day: WorkoutDayEntity,
    val exerciseCount: Int
)
