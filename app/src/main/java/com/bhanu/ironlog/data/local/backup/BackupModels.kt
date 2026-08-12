package com.bhanu.ironlog.data.local.backup

import kotlinx.serialization.Serializable
import com.bhanu.ironlog.data.local.entity.*

@Serializable
data class BackupPayload(
    val metadata: BackupMetadata,
    val library: List<LibraryExerciseDto>,
    val programs: List<ProgramDto>,
    val history: List<WorkoutSessionDto>,
    val records: List<PersonalRecordDto>,
    val settings: WorkoutSettingsDto
)

@Serializable
data class BackupMetadata(
    val version: Int,
    val timestamp: Long,
    val appVersion: String,
    val programCount: Int,
    val sessionCount: Int,
    val checksum: String = ""
)

@Serializable
data class LibraryExerciseDto(
    val id: Long,
    val systemKey: String?,
    val name: String,
    val normalizedName: String,
    val muscleGroup: String,
    val equipment: String,
    val exerciseType: String,
    val createdBy: String,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

fun LibraryExerciseEntity.toDto() = LibraryExerciseDto(
    id, systemKey, name, normalizedName, muscleGroup, equipment, exerciseType, createdBy, isActive, createdAt, updatedAt
)

@Serializable
data class ProgramDto(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val lastModifiedAt: Long,
    val isActive: Boolean,
    val isArchived: Boolean,
    val days: List<WorkoutDayDto>
)

fun ProgramEntity.toDto(days: List<WorkoutDayDto>) = ProgramDto(
    id, name, createdAt, lastModifiedAt, isActive, isArchived, days
)

@Serializable
data class WorkoutDayDto(
    val id: Long,
    val programId: Long,
    val name: String,
    val notes: String,
    val order: Int,
    val exercises: List<ExerciseDto>
)

fun WorkoutDayEntity.toDto(exercises: List<ExerciseDto>) = WorkoutDayDto(
    id, programId, name, notes, order, exercises
)

@Serializable
data class ExerciseDto(
    val id: Long,
    val dayId: Long,
    val libraryExerciseId: Long,
    val order: Int,
    val enabled: Boolean,
    val notes: String,
    val restTimerSeconds: Int,
    val useDefaultRestTimer: Boolean,
    val targetSets: Int,
    val targetRepMin: Int,
    val targetRepMax: Int,
    val targetRPE: Double?,
    val name: String,
    val muscleGroup: String,
    val equipment: String,
    val exerciseType: String,
    val createdAt: Long,
    val sets: List<SetDto>
)

fun ExerciseEntity.toDto(sets: List<SetDto>) = ExerciseDto(
    id, dayId, libraryExerciseId, order, enabled, notes, restTimerSeconds, useDefaultRestTimer, targetSets, targetRepMin, targetRepMax, targetRPE, name, muscleGroup, equipment, exerciseType, createdAt, sets
)

@Serializable
data class SetDto(
    val id: Long,
    val exerciseId: Long,
    val sessionId: Long,
    val setNumber: Int,
    val weight: Double,
    val reps: Int,
    val rpe: Double?,
    val rir: Int?,
    val notes: String,
    val isCompleted: Boolean,
    val setType: String,
    val order: Int,
    val createdAt: Long
)

fun SetEntity.toDto() = SetDto(
    id, exerciseId, sessionId, setNumber, weight, reps, rpe, rir, notes, isCompleted, setType, order, createdAt
)

@Serializable
data class WorkoutSessionDto(
    val sessionId: Long,
    val programId: Long,
    val workoutDayId: Long,
    val dayName: String,
    val programName: String,
    val startTime: Long,
    val endTime: Long?,
    val status: String,
    val notes: String?,
    val createdAt: Long,
    val completedExerciseIds: String,
    val durationSeconds: Long,
    val currentExerciseId: Long?,
    val currentSetNumber: Int?,
    val completedSetsCount: Int,
    val lastActiveTimestamp: Long,
    val hasShownBackgroundDialog: Boolean,
    val timerStartTime: Long?,
    val timerDurationSeconds: Int?,
    val timerState: String,
    val timerPausedRemainingSeconds: Int?,
    val exercises: List<SessionExerciseDto>
)

fun WorkoutSession.toDto(exercises: List<SessionExerciseDto>) = WorkoutSessionDto(
    sessionId, programId, workoutDayId, dayName, programName, startTime, endTime, status, notes, createdAt, completedExerciseIds, durationSeconds, currentExerciseId, currentSetNumber, completedSetsCount, lastActiveTimestamp, hasShownBackgroundDialog, timerStartTime, timerDurationSeconds, timerState, timerPausedRemainingSeconds, exercises
)

@Serializable
data class SessionExerciseDto(
    val sessionExerciseId: Long,
    val sessionId: Long,
    val exerciseTemplateId: Long,
    val libraryExerciseId: Long,
    val exerciseName: String,
    val muscleGroup: String,
    val equipment: String,
    val exerciseType: String,
    val targetSets: Int,
    val targetRepMin: Int,
    val targetRepMax: Int,
    val targetRPE: Double?,
    val restTimerSeconds: Int,
    val exerciseOrder: Int,
    val isSwapped: Boolean,
    val originalExerciseId: Long?,
    val status: String,
    val notes: String,
    val sets: List<SessionSetDto>
)

fun SessionExercise.toDto(sets: List<SessionSetDto>) = SessionExerciseDto(
    sessionExerciseId, sessionId, exerciseTemplateId, libraryExerciseId, exerciseName, muscleGroup, equipment, exerciseType, targetSets, targetRepMin, targetRepMax, targetRPE, restTimerSeconds, exerciseOrder, isSwapped, originalExerciseId, status, notes, sets
)

@Serializable
data class SessionSetDto(
    val sessionSetId: Long,
    val sessionExerciseId: Long,
    val setNumber: Int,
    val weight: Double,
    val reps: Int,
    val rpe: Double?,
    val completed: Boolean,
    val setType: String,
    val notes: String?,
    val createdAt: Long
)

fun SessionSet.toDto() = SessionSetDto(
    sessionSetId, sessionExerciseId, setNumber, weight, reps, rpe, completed, setType, notes, createdAt
)

@Serializable
data class PersonalRecordDto(
    val libraryExerciseId: Long,
    val exerciseTemplateId: Long,
    val weightPR: Double,
    val weightPRDate: Long,
    val weightPRSessionId: Long,
    val estimated1RM: Double,
    val estimated1RMDate: Long,
    val estimated1RMSessionId: Long,
    val createdAt: Long,
    val updatedAt: Long
)

fun PersonalRecordEntity.toDto() = PersonalRecordDto(
    libraryExerciseId, exerciseTemplateId, weightPR, weightPRDate, weightPRSessionId, estimated1RM, estimated1RMDate, estimated1RMSessionId, createdAt, updatedAt
)

@Serializable
data class WorkoutSettingsDto(
    val id: Int,
    val defaultRestTimerSeconds: Int,
    val autoStartTimer: Boolean,
    val hapticFeedback: Boolean,
    val soundAlert: Boolean
)

fun WorkoutSettingsEntity.toDto() = WorkoutSettingsDto(
    id, defaultRestTimerSeconds, autoStartTimer, hapticFeedback, soundAlert
)
