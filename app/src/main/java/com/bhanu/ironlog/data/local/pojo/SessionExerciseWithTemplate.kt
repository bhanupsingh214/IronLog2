package com.bhanu.ironlog.data.local.pojo

import androidx.room.Embedded
import androidx.room.Relation
import com.bhanu.ironlog.data.local.entity.ExerciseEntity
import com.bhanu.ironlog.data.local.entity.SessionExercise

data class SessionExerciseWithTemplate(
    @Embedded val sessionExercise: SessionExercise,
    @Relation(
        parentColumn = "exerciseTemplateId",
        entityColumn = "id"
    )
    val template: ExerciseEntity
)
