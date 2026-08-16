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
    private val settingsDao: WorkoutSettingsDao,
    private val userProfileDao: UserProfileDao,
    private val goalDao: GoalDao
) {
    suspend fun restoreBackup(payload: BackupPayload) {
        database.withTransaction {
            database.clearAllUserData()

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
                    if (dto.systemKey != null) libraryDao.findBySystemKey(dto.systemKey)?.id
                    else libraryDao.findByNormalizedName(dto.normalizedName)?.id
                } else newId
                if (finalId != null && finalId > 0) libraryIdMap[dto.id] = finalId
                else error("Failed to resolve library identity for: ${dto.name}")
            }

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
                        val systemKey = if (exerciseDto.libraryExerciseId > 0) payload.library.find { it.id == exerciseDto.libraryExerciseId }?.systemKey else null
                        val newLibId = getOrResolveLibraryId(exerciseDto.libraryExerciseId, exerciseDto.name, exerciseDto.muscleGroup, exerciseDto.equipment, exerciseDto.exerciseType, libraryIdMap, systemKey)
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
                                sessionId = 0,
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

            val sessionIdMap = mutableMapOf<Long, Long>()
            payload.history.forEach { sessionDto ->
                val newProgramId = programIdMap[sessionDto.programId]
                    ?: error("Missing program mapping for session ${sessionDto.sessionId} (Program ID: ${sessionDto.programId})")
                val newDayId = dayIdMap[sessionDto.workoutDayId]
                    ?: error("Missing workout day mapping for session ${sessionDto.sessionId} (Day ID: ${sessionDto.workoutDayId})")
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
                    completedExerciseIds = sessionDto.completedExerciseIds.split(",").filter { it.isNotBlank() }.mapNotNull {
                        val oldId = it.toLongOrNull() ?: return@mapNotNull null
                        templateExerciseIdMap[oldId] ?: error("Missing template mapping for completedExerciseId $oldId in session ${sessionDto.sessionId}")
                    }.joinToString(","),
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
                    val systemKey = if (seDto.libraryExerciseId > 0) payload.library.find { it.id == seDto.libraryExerciseId }?.systemKey else null
                    val newLibId = getOrResolveLibraryId(seDto.libraryExerciseId, seDto.exerciseName, seDto.muscleGroup, seDto.equipment, seDto.exerciseType, libraryIdMap, systemKey)
                    val newTemplateId = if (seDto.exerciseTemplateId > 0) templateExerciseIdMap[seDto.exerciseTemplateId]
                        ?: error("Missing template mapping for session exercise ${seDto.sessionExerciseId}") else 0L

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

            payload.records.forEach { prDto ->
                val newLibId = if (prDto.libraryExerciseId > 0) libraryIdMap[prDto.libraryExerciseId]
                    ?: error("Missing library mapping for physical PR") else 0L
                val newTemplateId = if (prDto.libraryExerciseId > 0) 0L
                else templateExerciseIdMap[prDto.exerciseTemplateId] ?: error("Missing template mapping for custom PR")
                if (newLibId > 0 || newTemplateId > 0) {
                    prDao.insertOrUpdatePR(PersonalRecordEntity(
                        libraryExerciseId = newLibId,
                        exerciseTemplateId = newTemplateId,
                        weightPR = prDto.weightPR,
                        weightPRDate = prDto.weightPRDate,
                        weightPRSessionId = if (prDto.weightPRSessionId > 0L) sessionIdMap[prDto.weightPRSessionId]
                            ?: error("Missing session mapping for weight PR in record lib:${prDto.libraryExerciseId}/temp:${prDto.exerciseTemplateId}") else 0L,
                        estimated1RM = prDto.estimated1RM,
                        estimated1RMDate = prDto.estimated1RMDate,
                        estimated1RMSessionId = if (prDto.estimated1RMSessionId > 0L) sessionIdMap[prDto.estimated1RMSessionId]
                            ?: error("Missing session mapping for estimated 1RM PR in record lib:${prDto.libraryExerciseId}/temp:${prDto.exerciseTemplateId}") else 0L,
                        createdAt = prDto.createdAt,
                        updatedAt = prDto.updatedAt
                    ))
                }
            }

            settingsDao.updateSettings(WorkoutSettingsEntity(
                defaultRestTimerSeconds = payload.settings.defaultRestTimerSeconds,
                autoStartTimer = payload.settings.autoStartTimer,
                hapticFeedback = payload.settings.hapticFeedback,
                soundAlert = payload.settings.soundAlert
            ))

            payload.profile?.let { dto ->
                userProfileDao.insertOrUpdateProfile(UserProfileEntity(1, dto.sex, dto.dateOfBirth, dto.heightCm, dto.createdAt, dto.updatedAt))
            }
            payload.weightHistory.forEach { dto -> userProfileDao.insertWeightEntry(BodyWeightEntry(weightKg = dto.weightKg, timestamp = dto.timestamp, notes = dto.notes)) }
            payload.waistHistory.forEach { dto -> userProfileDao.insertWaistEntry(WaistEntry(circumferenceCm = dto.circumferenceCm, timestamp = dto.timestamp, notes = dto.notes)) }

            // Goals are restored inside the same Room transaction. Old v1 backups have an empty goal list.
            payload.goals.forEach { dto ->
                val remappedLibraryId = dto.libraryExerciseId?.let { oldId ->
                    libraryIdMap[oldId] ?: error("Missing library mapping for goal $oldId")
                }
                goalDao.insertGoal(GoalEntity(
                    goalId = 0,
                    type = dto.type,
                    targetValue = dto.targetValue,
                    startingValue = dto.startingValue,
                    libraryExerciseId = remappedLibraryId,
                    frequencyCount = dto.frequencyCount,
                    frequencyPeriod = dto.frequencyPeriod,
                    startDate = dto.startDate,
                    deadline = dto.deadline
                ))
            }
        }
    }

    private suspend fun getOrResolveLibraryId(
        oldId: Long,
        name: String,
        muscle: String,
        equipment: String,
        type: String,
        idMap: Map<Long, Long>,
        systemKey: String?
    ): Long {
        idMap[oldId]?.let { if (it > 0) return it }
        if (systemKey != null) libraryDao.findBySystemKey(systemKey)?.id?.let { return it }
        val normalized = ExerciseNormalizationUtil.normalize(name)
        libraryDao.findByNormalizedName(normalized)?.id?.let { return it }
        val newId = libraryDao.insert(LibraryExerciseEntity(
            name = name,
            normalizedName = normalized,
            muscleGroup = muscle,
            equipment = equipment,
            exerciseType = type,
            createdBy = "User"
        ))
        return if (newId == -1L) {
            libraryDao.findByNormalizedName(normalized)?.id ?: error("Failed to create or find library identity for $name")
        } else newId
    }
}