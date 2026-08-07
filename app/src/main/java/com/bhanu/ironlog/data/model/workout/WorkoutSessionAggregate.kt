package com.bhanu.ironlog.data.model.workout

import com.bhanu.ironlog.data.local.entity.SessionExercise
import com.bhanu.ironlog.data.local.entity.SessionSet
import com.bhanu.ironlog.data.local.entity.WorkoutSession
import com.bhanu.ironlog.data.local.pojo.SessionExerciseWithTemplate
import com.bhanu.ironlog.data.local.pojo.SessionExerciseWithTemplateAndSets

/**
 * WorkoutSessionAggregate is the aggregate root for a training session.
 * It encapsulates Metadata, Snapshots, Execution state, and Statistics.
 */
data class WorkoutSessionAggregate(
    val metadata: SessionMetadata,
    val exercises: List<ExerciseAggregate>
) {
    /**
     * Statistics are derived from the current state of exercises and sets.
     */
    val statistics: SessionStatistics by lazy {
        val completedSets = exercises.flatMap { it.sets }.filter { it.completed }
        val completedExercises = exercises.filter { it.execution.status == "COMPLETED" }
        
        SessionStatistics(
            totalVolume = completedSets.sumOf { it.weight * it.reps },
            completedExercisesCount = completedExercises.size,
            totalExercisesCount = exercises.size,
            completedSetsCount = completedSets.size,
            totalSetsCount = exercises.sumOf { it.sets.size },
            averageRPE = completedSets.mapNotNull { it.rpe }.average().takeIf { !it.isNaN() },
            durationSeconds = metadata.durationSeconds
        )
    }
}

/**
 * SessionMetadata defines the identity and lifecycle state of the session.
 */
data class SessionMetadata(
    val sessionId: Long,
    val programId: Long,
    val workoutDayId: Long,
    val dayName: String,
    val programName: String,
    val status: String,
    val startTime: Long,
    val endTime: Long?,
    val durationSeconds: Long,
    val lastActiveTimestamp: Long,
    val currentExerciseId: Long?,
    val currentSetNumber: Int?
) {
    fun toEntity(): WorkoutSession {
        return WorkoutSession(
            sessionId = sessionId,
            programId = programId,
            workoutDayId = workoutDayId,
            dayName = dayName,
            programName = programName,
            startTime = startTime,
            endTime = endTime,
            status = status,
            durationSeconds = durationSeconds,
            lastActiveTimestamp = lastActiveTimestamp,
            currentExerciseId = currentExerciseId,
            currentSetNumber = currentSetNumber
        )
    }
}

/**
 * ExerciseAggregate represents a single exercise within a session.
 * It strictly separates the immutable Snapshot (Prescription) from the mutable Execution.
 */
data class ExerciseAggregate(
    val snapshot: ExerciseSnapshot,
    val execution: ExerciseExecution,
    val sets: List<SessionSet>
) {
    fun toPOJO(): SessionExerciseWithTemplate {
        return SessionExerciseWithTemplate(
            sessionExercise = SessionExercise(
                sessionExerciseId = snapshot.sessionExerciseId,
                sessionId = 0,
                exerciseTemplateId = snapshot.templateId,
                exerciseName = snapshot.name,
                muscleGroup = snapshot.muscleGroup,
                equipment = snapshot.equipment,
                exerciseType = snapshot.type,
                targetSets = snapshot.targetSets,
                targetRepMin = snapshot.targetRepMin,
                targetRepMax = snapshot.targetRepMax,
                targetRPE = snapshot.targetRPE,
                restTimerSeconds = snapshot.restTimerSeconds,
                exerciseOrder = snapshot.order,
                status = execution.status,
                notes = execution.notes,
                isSwapped = execution.isSwapped,
                originalExerciseId = execution.originalExerciseId
            ),
            template = null
        )
    }

    fun toPOJOWithSets(): SessionExerciseWithTemplateAndSets {
        val pojo = toPOJO()
        return SessionExerciseWithTemplateAndSets(
            sessionExercise = pojo.sessionExercise,
            template = null,
            sets = sets
        )
    }
}

/**
 * ExerciseSnapshot is the immutable record of what was planned (Identity + Prescription).
 */
data class ExerciseSnapshot(
    val sessionExerciseId: Long,
    val templateId: Long,
    val name: String,
    val muscleGroup: String,
    val equipment: String,
    val type: String,
    val targetSets: Int,
    val targetRepMin: Int,
    val targetRepMax: Int,
    val targetRPE: Double?,
    val restTimerSeconds: Int,
    val order: Int
)

/**
 * ExerciseExecution is the mutable record of what is happening during the workout.
 */
data class ExerciseExecution(
    val status: String,
    val notes: String,
    val isSwapped: Boolean,
    val originalExerciseId: Long?
)

/**
 * SessionStatistics contains metrics derived from session execution.
 */
data class SessionStatistics(
    val totalVolume: Double,
    val completedExercisesCount: Int,
    val totalExercisesCount: Int,
    val completedSetsCount: Int,
    val totalSetsCount: Int,
    val averageRPE: Double?,
    val durationSeconds: Long
)
