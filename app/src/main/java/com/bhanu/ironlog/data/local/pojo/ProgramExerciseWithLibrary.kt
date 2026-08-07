package com.bhanu.ironlog.data.local.pojo

import androidx.room.Embedded
import androidx.room.Relation
import com.bhanu.ironlog.data.local.entity.ExerciseEntity
import com.bhanu.ironlog.data.local.entity.LibraryExerciseEntity
import com.bhanu.ironlog.data.local.entity.SetEntity

data class ProgramExerciseWithLibrary(
    @Embedded val programExercise: ExerciseEntity,
    @Relation(
        parentColumn = "libraryExerciseId",
        entityColumn = "id"
    )
    val libraryExercise: LibraryExerciseEntity?,
    @Relation(
        parentColumn = "id",
        entityColumn = "exerciseId"
    )
    val templateSets: List<SetEntity>
) {
    // Helper to get name regardless of whether it's from library or legacy
    val exerciseName: String
        get() = libraryExercise?.name ?: programExercise.name

    val muscleGroup: String
        get() = libraryExercise?.muscleGroup ?: programExercise.muscleGroup

    val equipment: String
        get() = libraryExercise?.equipment ?: programExercise.equipment

    val exerciseType: String
        get() = libraryExercise?.exerciseType ?: programExercise.exerciseType
}
