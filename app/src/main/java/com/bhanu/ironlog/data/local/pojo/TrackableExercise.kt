package com.bhanu.ironlog.data.local.pojo

import androidx.room.Embedded
import androidx.room.Relation
import com.bhanu.ironlog.data.local.entity.ExerciseEntity
import com.bhanu.ironlog.data.local.entity.LibraryExerciseEntity

data class TrackableExercise(
    val libraryExerciseId: Long,
    val exerciseTemplateId: Long,
    // Metadata for resolution
    val snapshotName: String,
    val snapshotMuscle: String,

    @Relation(
        parentColumn = "libraryExerciseId",
        entityColumn = "id"
    )
    val libraryExercise: LibraryExerciseEntity?,

    @Relation(
        parentColumn = "exerciseTemplateId",
        entityColumn = "id"
    )
    val templateExercise: ExerciseEntity?
) {
    val displayName: String
        get() = libraryExercise?.name ?: templateExercise?.name ?: snapshotName

    val displayMuscle: String
        get() = libraryExercise?.muscleGroup ?: templateExercise?.muscleGroup ?: snapshotMuscle
}