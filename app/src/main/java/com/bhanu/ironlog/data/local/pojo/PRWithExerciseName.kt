package com.bhanu.ironlog.data.local.pojo

import androidx.room.Embedded
import androidx.room.Relation
import com.bhanu.ironlog.data.local.entity.ExerciseEntity
import com.bhanu.ironlog.data.local.entity.PersonalRecordEntity

data class PRWithExerciseName(
    @Embedded val pr: PersonalRecordEntity,
    @Relation(
        parentColumn = "exerciseTemplateId",
        entityColumn = "id"
    )
    val exercise: ExerciseEntity?
)
