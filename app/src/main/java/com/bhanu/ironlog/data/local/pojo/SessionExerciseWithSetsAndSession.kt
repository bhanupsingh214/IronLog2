package com.bhanu.ironlog.data.local.pojo

import androidx.room.Embedded
import androidx.room.Relation
import com.bhanu.ironlog.data.local.entity.SessionExercise
import com.bhanu.ironlog.data.local.entity.SessionSet
import com.bhanu.ironlog.data.local.entity.WorkoutSession

data class SessionExerciseWithSetsAndSession(
    @Embedded val sessionExercise: SessionExercise,
    @Relation(
        parentColumn = "sessionId",
        entityColumn = "sessionId"
    )
    val session: WorkoutSession,
    @Relation(
        parentColumn = "sessionExerciseId",
        entityColumn = "sessionExerciseId"
    )
    val sets: List<SessionSet>
)
