package com.bhanu.ironlog.data.repository

import androidx.room.withTransaction
import com.bhanu.ironlog.data.local.AppDatabase
import com.bhanu.ironlog.data.local.backup.*
import com.bhanu.ironlog.data.local.entity.*
import com.bhanu.ironlog.data.local.dao.*
import com.bhanu.ironlog.util.ExerciseNormalizationUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestoreRepository @Inject constructor(
    private val database: AppDatabase,
    private val programDao: ProgramDao,
    private val workoutSessionDao: WorkoutSessionDao,
    private val libraryDao: LibraryExerciseDao,
    private val prDao: PersonalRecordDao,
    private val settingsDao: WorkoutSettingsDao
) {
    /**
     * Performs a full "Clear & Restore" of the provided payload.
     * Uses remapping to prevent ID collisions.
     */
    suspend fun restoreBackup(payload: BackupPayload) {
        database.withTransaction {
            // 1. Clear existing data
            database.clearAllUserData()

            // 2. Restore Library (Physical Identities)
            val libraryIdMap = mutableMapOf<Long, Long>()
            payload.library.forEach { dto ->
                val entity = LibraryExerciseEntity(
                    systemKey = dto.systemKey,
                    name = dto.name,
                    normalizedName = dto.normalizedName,
                    muscleGroup = dto.muscleGroup,
                    equipment = dto.equipment,
                    exerciseType = dto.exerciseType,
                    createdBy = dto.createdBy,
                    isActive = dto.isActive,
                    createdAt = dto.createdAt,
                    updatedAt = dto.updatedAt
                )
                val newId = libraryDao.insert(entity)
                val finalId = if (newId == -1L) {
                    // Conflict resolution: find existing
                    if (dto.systemKey != null) {
                        libraryDao.findBySystemKey(dto.systemKey)?.id
                    } else {
                        libraryDao.findByNormalizedName(dto.normalizedName)?.id
                    }
                } else newId

                if (finalId != null && finalId > 0) {
                    libraryIdMap[dto.id] = finalId
                } else {
                    error("Failed to resolve library identity for: ${dto.name}")
                }
            }

            // 3. Restore Programs Tree
            val programIdMap = mutableMapOf<Long, Long>()
            val dayIdMap = mutableMapOf<Long, Long>()
            val templateExerciseIdMap = mutableMapOf<Long, Long>()

            payload.programs.forEach { programDto ->
                val programId = programDao.insertProgram(ProgramEntity(
                    name = programDto.name,
                    createdAt = programDto.createdAt,
                    lastModifiedAt = programDto.lastModifiedAt,
                    isActive = programDto.isActive,
                    isArchived = programDto.isArchived
                ))
                programIdMap[programDto.id] = programId

                programDto.days.forEach { dayDto ->
                    val dayId = programDao.insertDay(WorkoutDayEntity(
                        programId = programId,
                        name = dayDto.name,
                        order = dayDto.order,
                        notes = dayDto.notes,
                        isEnabled = dayDto.isEnabled,
                        estimatedDurationMinutes = dayDto.estimatedDurationMinutes
                    ))
                    dayIdMap[dayDto.id] = dayId

                    dayDto.exercises.forEach { exerciseDto ->
                        val systemKey = if (exerciseDto.libraryExerciseId > 0) {
                            payload.library.find { it.id == exerciseDto.libraryExerciseId }?.systemKey
                        } else null

                        val newLibId = getOrResolveLibraryId(
                            oldId = exerciseDto.libraryExerciseId,
                            name = exerciseDto.name,
                            muscle = exerciseDto.muscleGroup,
                            equipment = exerciseDto.equipment,
                            type = exerciseDto.exerciseType,
                            idMap = libraryIdMap,
                            systemKey = systemKey
                        )

                        val exerciseId = programDao.insertExercise(ExerciseEntity(
                            dayId = dayId,
                            libraryExerciseId = newLibId,
                            order = exerciseDto.order,
                            enabled = exerciseDto.enabled,
                            notes = exerciseDto.notes,
                            restTimerSeconds = exerciseDto.restTimerSeconds,
                            useDefaultRestTimer = exerciseDto.useDefaultRestTimer,
                            targetSets = exerciseDto.targetSets,
                            targetRepMin = exerciseDto.targetRepMin,
                            targetRepMax = exerciseDto.targetRepMax,
                            targetRPE = exerciseDto.targetRPE,
                            name = exerciseDto.name,
                            muscleGroup = exerciseDto.muscleGroup,
                            equipment = exerciseDto.equipment,
                            exerciseType = exerciseDto.exerciseType,
                            createdAt = exerciseDto.createdAt
                        ))
                        templateExerciseIdMap[exerciseDto.id] = exerciseId

                        exerciseDto.sets.forEach { setDto ->
                            programDao.insertSet(SetEntity(
                                exerciseId = exerciseId,
                                sessionId = 0, // Blueprints always use 0
                                setNumber = setDto.setNumber,
                                weight = setDto.weight,
                                reps = setDto.reps,
                                rpe = setDto.rpe,
                                rir = setDto.rir,
                                notes = setDto.notes,
                                isCompleted = setDto.isCompleted,
                                setType = setDto.setType,
                                order = setDto.order,
                                createdAt = setDto.createdAt
                            ))
                        }
                    }
                }
            }

            // 4. Restore Workout History
            val sessionIdMap = mutableMapOf<Long, Long>()
            payload.history.forEach { sessionDto ->
                val newProgramId = programIdMap[sessionDto.programId]
                    ?: error("Missing program mapping for session ${sessionDto.sessionId} (Program ID: ${sessionDto.programId})")
                val newDayId = dayIdMap[sessionDto.workoutDayId]
                    ?: error("Missing workout day mapping for session ${sessionDto.sessionId} (Day ID: ${sessionDto.workoutDayId})")

                // Map currentExerciseId if it existed (autosave)
                val newCurrentExerciseId = sessionDto.currentExerciseId?.let {
                    templateExerciseIdMap[it] ?: error("Missing template mapping for currentExerciseId in session ${sessionDto.sessionId}")
                }

                val sessionId = workoutSessionDao.insertSession(WorkoutSession(
                    programId = newProgramId,
                    workoutDayId = newDayId,
                    dayName = sessionDto.dayName,
                    programName = sessionDto.programName,
                    startTime = sessionDto.startTime,
                    endTime = sessionDto.endTime,
                    status = sessionDto.status,
                    notes = sessionDto.notes,
                    createdAt = sessionDto.createdAt,
                    completedExerciseIds = sessionDto.completedExerciseIds.split(",")
                        .filter { it.isNotBlank() }
                        .mapNotNull {
                            val oldId = it.toLongOrNull() ?: return@mapNotNull null
                            templateExerciseIdMap[oldId] ?: error("Missing template mapping for completedExerciseId $oldId in session ${sessionDto.sessionId}")
                        }
                        .joinToString(","),
                    durationSeconds = sessionDto.durationSeconds,
                    currentExerciseId = newCurrentExerciseId,
                    currentSetNumber = sessionDto.currentSetNumber,
                    completedSetsCount = sessionDto.completedSetsCount,
                    lastActiveTimestamp = sessionDto.lastActiveTimestamp,
                    hasShownBackgroundDialog = sessionDto.hasShownBackgroundDialog,
                    timerStartTime = sessionDto.timerStartTime,
                    timerDurationSeconds = sessionDto.timerDurationSeconds,
                    timerState = sessionDto.timerState,
                    timerPausedRemainingSeconds = sessionDto.timerPausedRemainingSeconds
                ))
                sessionIdMap[sessionDto.sessionId] = sessionId

                sessionDto.exercises.forEach { seDto ->
                    val systemKey = if (seDto.libraryExerciseId > 0) {
                        payload.library.find { it.id == seDto.libraryExerciseId }?.systemKey
                    } else null

                    val newLibId = getOrResolveLibraryId(
                        oldId = seDto.libraryExerciseId,
                        name = seDto.exerciseName,
                        muscle = seDto.muscleGroup,
                        equipment = seDto.equipment,
                        type = seDto.exerciseType,
                        idMap = libraryIdMap,
                        systemKey = systemKey
                    )

                    val newTemplateId = if (seDto.exerciseTemplateId > 0) {
                        templateExerciseIdMap[seDto.exerciseTemplateId] ?: error("Missing template mapping for session exercise ${seDto.sessionExerciseId}")
                    } else 0L

                    val sessionExerciseId = workoutSessionDao.insertSessionExercise(SessionExercise(
                        sessionId = sessionId,
                        exerciseTemplateId = newTemplateId,
                        libraryExerciseId = newLibId,
                        exerciseName = seDto.exerciseName,
                        muscleGroup = seDto.muscleGroup,
                        equipment = seDto.equipment,
                        exerciseType = seDto.exerciseType,
                        targetSets = seDto.targetSets,
                        targetRepMin = seDto.targetRepMin,
                        targetRepMax = seDto.targetRepMax,
                        targetRPE = seDto.targetRPE,
                        restTimerSeconds = seDto.restTimerSeconds,
                        exerciseOrder = seDto.exerciseOrder,
                        isSwapped = seDto.isSwapped,
                        originalExerciseId = seDto.originalExerciseId?.let {
                            templateExerciseIdMap[it] ?: error("Missing template mapping for originalExerciseId in session exercise ${seDto.sessionExerciseId}")
                        },
                        status = seDto.status,
                        notes = seDto.notes
                    ))

                    seDto.sets.forEach { ssDto ->
                        workoutSessionDao.insertSessionSet(SessionSet(
                            sessionExerciseId = sessionExerciseId,
                            setNumber = ssDto.setNumber,
                            weight = ssDto.weight,
                            reps = ssDto.reps,
                            rpe = ssDto.rpe,
                            completed = ssDto.completed,
                            setType = ssDto.setType,
                            notes = ssDto.notes,
                            createdAt = ssDto.createdAt
                        ))
                    }
                }
            }

            // 5. Restore Personal Records
            payload.records.forEach { prDto ->
                val newLibId = if (prDto.libraryExerciseId > 0) {
                    libraryIdMap[prDto.libraryExerciseId] ?: error("Missing library mapping for physical PR")
                } else 0L

                val newTemplateId = if (prDto.libraryExerciseId > 0) 0L else {
                    templateExerciseIdMap[prDto.exerciseTemplateId] ?: error("Missing template mapping for custom PR")
                }

                if (newLibId > 0 || newTemplateId > 0) {
                    prDao.insertOrUpdatePR(PersonalRecordEntity(
                        libraryExerciseId = newLibId,
                        exerciseTemplateId = newTemplateId,
                        weightPR = prDto.weightPR,
                        weightPRDate = prDto.weightPRDate,
                        weightPRSessionId = if (prDto.weightPRSessionId > 0L) {
                            sessionIdMap[prDto.weightPRSessionId]
                                ?: error("Missing session mapping for weight PR in record lib:${prDto.libraryExerciseId}/temp:${prDto.exerciseTemplateId}")
                        } else 0L,
                        estimated1RM = prDto.estimated1RM,
                        estimated1RMDate = prDto.estimated1RMDate,
                        estimated1RMSessionId = if (prDto.estimated1RMSessionId > 0L) {
                            sessionIdMap[prDto.estimated1RMSessionId]
                                ?: error("Missing session mapping for estimated 1RM PR in record lib:${prDto.libraryExerciseId}/temp:${prDto.exerciseTemplateId}")
                        } else 0L,
                        createdAt = prDto.createdAt,
                        updatedAt = prDto.updatedAt
                    ))
                }
            }

            // 6. Restore Settings
            settingsDao.updateSettings(WorkoutSettingsEntity(
                defaultRestTimerSeconds = payload.settings.defaultRestTimerSeconds,
                autoStartTimer = payload.settings.autoStartTimer,
                hapticFeedback = payload.settings.hapticFeedback,
                soundAlert = payload.settings.soundAlert
            ))
        }
    }

    /**
     * Resolves a libraryExerciseId during restoration based on canonical identity rules.
     */
    private suspend fun getOrResolveLibraryId(
        oldId: Long,
        name: String,
        muscle: String,
        equipment: String,
        type: String,
        idMap: Map<Long, Long>,
        systemKey: String?
    ): Long {
        // a. If oldId > 0 and idMap contains a valid positive ID, use it.
        idMap[oldId]?.let { if (it > 0) return it }

        // b. System exercise with systemKey -> find by systemKey
        if (systemKey != null) {
            libraryDao.findBySystemKey(systemKey)?.id?.let { return it }
        }

        // c. User/custom exercise -> find by normalizedName
        val normalized = ExerciseNormalizationUtil.normalize(name)
        libraryDao.findByNormalizedName(normalized)?.id?.let { return it }

        // d. If genuinely unresolved -> create exactly one new library record
        val newId = libraryDao.insert(LibraryExerciseEntity(
            name = name,
            normalizedName = normalized,
            muscleGroup = muscle,
            equipment = equipment,
            exerciseType = type,
            createdBy = "User"
        ))

        // If insert failed due to concurrent race (unlikely in transaction but safe), fetch existing
        return if (newId == -1L) {
            libraryDao.findByNormalizedName(normalized)?.id ?: error("Failed to create or find library identity for $name")
        } else newId
    }
}
