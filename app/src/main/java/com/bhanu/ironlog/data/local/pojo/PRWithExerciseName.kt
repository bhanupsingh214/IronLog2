package com.bhanu.ironlog.data.local.pojo

import androidx.room.Embedded
import androidx.room.Relation
import com.bhanu.ironlog.data.local.entity.ExerciseEntity
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
        parentColumn = "weightPRSessionId",
        entityColumn = "sessionId"
    )
    private val sessions: List<SessionExercise>
) {
    val snapshotName: String? by lazy {
        sessions.find { it.exerciseTemplateId == pr.exerciseTemplateId }?.exerciseName
    }
    
    val snapshotMuscle: String? by lazy {
        sessions.find { it.exerciseTemplateId == pr.exerciseTemplateId }?.muscleGroup
    }
}
