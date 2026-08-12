package com.bhanu.ironlog.data.repository

import android.util.Log
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
        Log.d("IronLogImportDebug", "9. RestoreRepository.restoreBackup() entered")
        database.withTransaction {
            // 1. Clear existing data
            Log.d("IronLogImportDebug", "10. clearAllUserData() started")
            database.clearAllUserData()

            // 2. Restore Library (Physical Identities)
            Log.d("IronLogImportDebug", "11. Restore phase: library. Count: ${payload.library.size}")
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
                    Log.e("IronLogImportDebug", "Failed to resolve library identity for: ${dto.name}")
                }
            }

            // 3. Restore Programs Tree
            Log.d("IronLogImportDebug", "11. Restore phase: programs/days/exercises/sets")
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
                        val newLibId = getOrResolveLibraryId(
                            oldId = exerciseDto.libraryExerciseId,
                            name = exerciseDto.name,
                            muscle = exerciseDto.muscleGroup,
                            equipment = exerciseDto.equipment,
                            type = exerciseDto.exerciseType,
                            idMap = libraryIdMap
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
            Log.d("IronLogImportDebug", "11. Restore phase: sessions/session_exercises/session_sets")
            val sessionIdMap = mutableMapOf<Long, Long>()
            payload.history.forEach { sessionDto ->
                val newProgramId = programIdMap[sessionDto.programId] ?: 0L
                val newDayId = dayIdMap[sessionDto.workoutDayId] ?: 0L

                // Map currentExerciseId if it existed (autosave)
                val newCurrentExerciseId = sessionDto.currentExerciseId?.let { templateExerciseIdMap[it] }

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
                        .mapNotNull { templateExerciseIdMap[it.toLongOrNull()] }
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
                    val newLibId = getOrResolveLibraryId(
                        oldId = seDto.libraryExerciseId,
                        name = seDto.exerciseName,
                        muscle = seDto.muscleGroup,
                        equipment = seDto.equipment,
                        type = seDto.exerciseType,
                        idMap = libraryIdMap
                    )

                    val newTemplateId = templateExerciseIdMap[seDto.exerciseTemplateId] ?: 0L
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
                        originalExerciseId = seDto.originalExerciseId?.let { templateExerciseIdMap[it] },
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
            Log.d("IronLogImportDebug", "11. Restore phase: personal_records")
            payload.records.forEach { prDto ->
                val newLibId = libraryIdMap[prDto.libraryExerciseId] ?: 0L
                val newTemplateId = if (prDto.libraryExerciseId > 0) 0L else (templateExerciseIdMap[prDto.exerciseTemplateId] ?: 0L)

                if (newLibId > 0 || newTemplateId > 0) {
                    prDao.insertOrUpdatePR(PersonalRecordEntity(
                        libraryExerciseId = newLibId,
                        exerciseTemplateId = newTemplateId,
                        weightPR = prDto.weightPR,
                        weightPRDate = prDto.weightPRDate,
                        weightPRSessionId = sessionIdMap[prDto.weightPRSessionId] ?: 0L,
                        estimated1RM = prDto.estimated1RM,
                        estimated1RMDate = prDto.estimated1RMDate,
                        estimated1RMSessionId = sessionIdMap[prDto.estimated1RMSessionId] ?: 0L,
                        createdAt = prDto.createdAt,
                        updatedAt = prDto.updatedAt
                    ))
                }
            }

            // 6. Restore Settings
            Log.d("IronLogImportDebug", "11. Restore phase: settings")
            settingsDao.updateSettings(WorkoutSettingsEntity(
                defaultRestTimerSeconds = payload.settings.defaultRestTimerSeconds,
                autoStartTimer = payload.settings.autoStartTimer,
                hapticFeedback = payload.settings.hapticFeedback,
                soundAlert = payload.settings.soundAlert
            ))
            Log.d("IronLogImportDebug", "12. restoreBackup transaction completed")
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
        idMap: Map<Long, Long>
    ): Long {
        // a. If oldId > 0 and idMap contains a valid positive ID, use it.
        idMap[oldId]?.let { if (it > 0) return it }

        // b & c. Resolve by canonical identity (SystemKey then NormalizedName)
        val normalized = ExerciseNormalizationUtil.normalize(name)

        // System Lookup (By normalized name as proxy for key if key is null in current context)
        libraryDao.findByNormalizedName(normalized)?.id?.let { return it }

        // d. Create exactly one new library record if no match exists
        Log.d("IronLogImportDebug", "Creating on-the-fly library identity for: $name")
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
            libraryDao.findByNormalizedName(normalized)?.id ?: throw IllegalStateException("Failed to create or find library identity for $name")
        } else newId
    }
}
