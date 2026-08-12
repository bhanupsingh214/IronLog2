package com.bhanu.ironlog.data.local.pojo

import androidx.room.Embedded
import androidx.room.Relation
import com.bhanu.ironlog.data.local.entity.ExerciseEntity
import com.bhanu.ironlog.data.local.entity.LibraryExerciseEntity
import com.bhanu.ironlog.data.local.entity.PersonalRecordEntity
import com.bhanu.ironlog.data.local.entity.SessionExercise

data class PRWithExerciseName(
    @Embedded val pr: PersonalRecordEntity,
    @Relation(
        parentColumn = "exerciseTemplateId",
        entityColumn = "id"
    )
    val exercise: ExerciseEntity?,
    @Relation(
        parentColumn = "libraryExerciseId",
        entityColumn = "id"
    )
    val libraryExercise: LibraryExerciseEntity?,
    @Relation(
        parentColumn = "weightPRSessionId",
        entityColumn = "sessionId"
    )
    private val sessions: List<SessionExercise>
) {
    val snapshotName: String? by lazy {
        libraryExercise?.name ?:
        sessions.find {
            if (pr.libraryExerciseId > 0) it.libraryExerciseId == pr.libraryExerciseId
            else it.exerciseTemplateId == pr.exerciseTemplateId
        }?.exerciseName ?:
        exercise?.name
    }

    val snapshotMuscle: String? by lazy {
        libraryExercise?.muscleGroup ?:
        sessions.find {
            if (pr.libraryExerciseId > 0) it.libraryExerciseId == pr.libraryExerciseId
            else it.exerciseTemplateId == pr.exerciseTemplateId
        }?.muscleGroup ?:
        exercise?.muscleGroup
    }
}
