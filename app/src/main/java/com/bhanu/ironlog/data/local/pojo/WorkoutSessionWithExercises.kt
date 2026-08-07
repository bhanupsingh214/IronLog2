package com.bhanu.ironlog.data.local.pojo

import androidx.room.Embedded
import androidx.room.Relation
import com.bhanu.ironlog.data.local.entity.SessionExercise
import com.bhanu.ironlog.data.local.entity.WorkoutSession
import com.bhanu.ironlog.data.model.workout.*

data class WorkoutSessionWithExercises(
    @Embedded val session: WorkoutSession,
    @Relation(
        entity = SessionExercise::class,
        parentColumn = "sessionId",
        entityColumn = "sessionId"
    )
    val exercises: List<SessionExerciseWithTemplateAndSets>
) {
    /**
     * Maps the Room POJO to the Domain Aggregate.
     */
    fun toAggregate(): WorkoutSessionAggregate {
        return WorkoutSessionAggregate(
            metadata = SessionMetadata(
                sessionId = session.sessionId,
                programId = session.programId,
                workoutDayId = session.workoutDayId,
                dayName = session.dayName,
                programName = session.programName,
                status = session.status,
                startTime = session.startTime,
                endTime = session.endTime,
                durationSeconds = session.durationSeconds,
                lastActiveTimestamp = session.lastActiveTimestamp,
                currentExerciseId = session.currentExerciseId,
                currentSetNumber = session.currentSetNumber
            ),
            exercises = exercises.map { exerciseWithSets ->
                val se = exerciseWithSets.sessionExercise
                ExerciseAggregate(
                    snapshot = ExerciseSnapshot(
                        sessionExerciseId = se.sessionExerciseId,
                        templateId = se.exerciseTemplateId,
                        name = se.exerciseName,
                        muscleGroup = se.muscleGroup,
                        equipment = se.equipment,
                        type = se.exerciseType,
                        targetSets = se.targetSets,
                        targetRepMin = se.targetRepMin,
                        targetRepMax = se.targetRepMax,
                        targetRPE = se.targetRPE,
                        restTimerSeconds = se.restTimerSeconds,
                        order = se.exerciseOrder
                    ),
                    execution = ExerciseExecution(
                        status = se.status,
                        notes = se.notes,
                        isSwapped = se.isSwapped,
                        originalExerciseId = se.originalExerciseId
                    ),
                    sets = exerciseWithSets.sets
                )
            }
        )
    }
}
