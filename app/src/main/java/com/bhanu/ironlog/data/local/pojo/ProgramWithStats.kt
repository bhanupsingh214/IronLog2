package com.bhanu.ironlog.data.local.pojo

import androidx.room.Embedded
import androidx.room.Relation
import com.bhanu.ironlog.data.local.entity.ProgramEntity
import com.bhanu.ironlog.data.local.entity.WorkoutDayEntity

data class ProgramWithStats(
    @Embedded val program: ProgramEntity,
    val dayCount: Int,
    val exerciseCount: Int
)
