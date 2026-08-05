package com.bhanu.ironlog.data.local.pojo

import androidx.room.Embedded
import androidx.room.Relation
import com.bhanu.ironlog.data.local.entity.SessionExercise
import com.bhanu.ironlog.data.local.entity.SessionSet

data class SessionExerciseWithSets(
    @Embedded val sessionExercise: SessionExercise,
    @Relation(
        parentColumn = "sessionExerciseId",
        entityColumn = "sessionExerciseId"
    )
    val sets: List<SessionSet>
)
