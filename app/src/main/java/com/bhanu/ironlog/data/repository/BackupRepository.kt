package com.bhanu.ironlog.data.repository

import com.bhanu.ironlog.data.local.dao.*
import com.bhanu.ironlog.data.local.backup.*
import com.bhanu.ironlog.data.local.entity.WorkoutSettingsEntity
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    private val programDao: ProgramDao,
    private val workoutSessionDao: WorkoutSessionDao,
    private val libraryDao: LibraryExerciseDao,
    private val prDao: PersonalRecordDao,
    private val settingsDao: WorkoutSettingsDao,
    private val userProfileDao: UserProfileDao
) {
    suspend fun getFullBackupPayload(appVersion: String): BackupPayload {
        val library = libraryDao.getAllExercises().map { it.toDto() }

        val programs = programDao.getAllProgramsWithStats().first().map { programWithStats ->
            val program = programWithStats.program
            val days = programDao.getDaysForProgram(program.id).map { day ->
                val exercises = programDao.getExercisesForDay(day.id).map { exercise ->
                    val sets = programDao.getSetsForExercise(exercise.id).map { it.toDto() }
                    exercise.toDto(sets)
                }
                day.toDto(exercises)
            }
            program.toDto(days)
        }

        val sessions = workoutSessionDao.getAllSessions().first().map { session ->
            val exercises = workoutSessionDao
                .getExercisesForSessionList(session.sessionId)
                .map { sessionExercise ->
                    val sets = workoutSessionDao
                        .getSetsForExerciseList(sessionExercise.sessionExerciseId)
                        .map { it.toDto() }

                    sessionExercise.toDto(sets)
                }

            session.toDto(exercises)
        }

        val records = prDao.getAllPRs().first().map { it.toDto() }

        val settings = settingsDao.getSettingsOnce()?.toDto()
            ?: WorkoutSettingsEntity().toDto()

        val profile = userProfileDao.getProfileOnce()?.toDto()
        val weightHistory = userProfileDao.getWeightHistoryOnce().map { it.toDto() }
        val waistHistory = userProfileDao.getWaistHistoryOnce().map { it.toDto() }

        val metadata = BackupMetadata(
            version = 1,
            timestamp = System.currentTimeMillis(),
            appVersion = appVersion,
            programCount = programs.size,
            sessionCount = sessions.size
        )

        return BackupPayload(
            metadata = metadata,
            library = library,
            programs = programs,
            history = sessions,
            records = records,
            settings = settings,
            profile = profile,
            weightHistory = weightHistory,
            waistHistory = waistHistory
        )
    }
}
