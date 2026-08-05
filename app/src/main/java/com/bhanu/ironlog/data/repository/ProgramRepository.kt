package com.bhanu.ironlog.data.repository

import com.bhanu.ironlog.data.local.dao.ProgramDao
import com.bhanu.ironlog.data.local.dao.SessionDao
import com.bhanu.ironlog.data.local.entity.ExerciseEntity
import com.bhanu.ironlog.data.local.entity.ProgramEntity
import com.bhanu.ironlog.data.local.entity.SetEntity
import com.bhanu.ironlog.data.local.entity.WorkoutDayEntity
import com.bhanu.ironlog.data.local.entity.WorkoutSessionEntity
import com.bhanu.ironlog.data.local.pojo.ProgramWithStats
import com.bhanu.ironlog.data.local.pojo.WorkoutDayWithStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgramRepository @Inject constructor(
    private val programDao: ProgramDao,
    private val sessionDao: SessionDao
) {
    fun getAllProgramsWithStats(): Flow<List<ProgramWithStats>> = programDao.getAllProgramsWithStats()
    
    fun getArchivedProgramsWithStats(): Flow<List<ProgramWithStats>> = programDao.getArchivedProgramsWithStats()

    fun getActiveProgram(): Flow<ProgramWithStats?> = programDao.getActiveProgramWithStats()

    suspend fun insertProgram(program: ProgramEntity) = programDao.insertProgram(program)

    suspend fun updateProgram(program: ProgramEntity) = programDao.updateProgram(program.copy(lastModifiedAt = System.currentTimeMillis()))

    suspend fun deleteProgram(program: ProgramEntity) = programDao.deleteProgram(program)

    suspend fun activateProgram(programId: Long) = programDao.activateProgram(programId)

    suspend fun archiveProgram(program: ProgramEntity) {
        programDao.updateProgram(program.copy(isArchived = true, isActive = false, lastModifiedAt = System.currentTimeMillis()))
    }

    suspend fun restoreProgram(program: ProgramEntity) {
        programDao.updateProgram(program.copy(isArchived = false, lastModifiedAt = System.currentTimeMillis()))
    }

    suspend fun duplicateProgram(programId: Long) {
        val originalProgram = programDao.getProgramById(programId) ?: return
        
        // 1. Duplicate Program
        val newProgramId = programDao.insertProgram(
            originalProgram.copy(
                id = 0,
                name = "${originalProgram.name} (Copy)",
                isActive = false,
                isArchived = false,
                createdAt = System.currentTimeMillis(),
                lastModifiedAt = System.currentTimeMillis()
            )
        )

        // 2. Duplicate Workout Days
        val days = programDao.getDaysForProgram(programId)
        for (day in days) {
            val newDayId = programDao.insertDay(day.copy(id = 0, programId = newProgramId))
            
            // 3. Duplicate Exercises
            val exercises = programDao.getExercisesForDay(day.id)
            for (exercise in exercises) {
                val newExerciseId = programDao.insertExercise(
                    exercise.copy(
                        id = 0, 
                        dayId = newDayId,
                        restTimerSeconds = exercise.restTimerSeconds,
                        useDefaultRestTimer = exercise.useDefaultRestTimer
                    )
                )
                
                // 4. Duplicate Sets
                val sets = programDao.getSetsForExercise(exercise.id)
                for (set in sets) {
                    programDao.insertSet(set.copy(id = 0, exerciseId = newExerciseId))
                }
            }
        }
    }

    // Workout Day Management
    fun getWorkoutDaysWithStats(programId: Long): Flow<List<WorkoutDayWithStats>> = 
        programDao.getWorkoutDaysWithStats(programId)

    fun getExercisesForDay(dayId: Long): Flow<List<ExerciseEntity>> =
        programDao.getExercisesForDayFlow(dayId)

    fun getExercise(exerciseId: Long): Flow<ExerciseEntity?> =
        programDao.getExerciseFlow(exerciseId)

    fun getAllExercises(): Flow<List<ExerciseEntity>> =
        programDao.getAllExercisesFlow()

    fun getSetsForExercise(exerciseId: Long, sessionId: Long = 0): Flow<List<SetEntity>> =
        programDao.getSetsForExerciseAndSessionFlow(exerciseId, sessionId)

    fun getPreviousSets(exerciseId: Long, currentSessionId: Long): Flow<List<SetEntity>> =
        programDao.getPreviousSessionSets(exerciseId, currentSessionId)

    suspend fun insertSet(set: SetEntity) {
        val maxOrder = programDao.getMaxOrderForExerciseInSession(set.exerciseId, set.sessionId) ?: -1
        programDao.insertSet(set.copy(order = maxOrder + 1))
        renumberSets(set.exerciseId, set.sessionId)
    }

    suspend fun updateSet(set: SetEntity) = programDao.updateSet(set)

    suspend fun deleteSet(set: SetEntity) {
        programDao.deleteSet(set)
        renumberSets(set.exerciseId, set.sessionId)
    }

    private suspend fun renumberSets(exerciseId: Long, sessionId: Long) {
        val sets = programDao.getSetsForExerciseAndSession(exerciseId, sessionId).sortedBy { it.order }
        sets.forEachIndexed { index, set ->
            val updatedSet = set.copy(setNumber = index + 1, order = index)
            programDao.updateSet(updatedSet)
        }
    }

    suspend fun moveSet(setId: Long, up: Boolean) {
        val set = programDao.getSetById(setId) ?: return
        val sets = programDao.getSetsForExerciseAndSession(set.exerciseId, set.sessionId).sortedBy { it.order }
        val currentIndex = sets.indexOfFirst { it.id == setId }
        
        val targetIndex = if (up) currentIndex - 1 else currentIndex + 1
        
        if (targetIndex in sets.indices) {
            val targetSet = sets[targetIndex]
            val newOrderForCurrent = targetSet.order
            val newOrderForTarget = set.order
            
            programDao.updateSet(set.copy(order = newOrderForCurrent, setNumber = targetIndex + 1))
            programDao.updateSet(targetSet.copy(order = newOrderForTarget, setNumber = currentIndex + 1))
        }
    }

    suspend fun duplicateSet(setId: Long) {
        val set = programDao.getSetById(setId) ?: return
        val maxOrder = programDao.getMaxOrderForExerciseInSession(set.exerciseId, set.sessionId) ?: 0
        programDao.insertSet(
            set.copy(
                id = 0,
                order = maxOrder + 1,
                isCompleted = false,
                createdAt = System.currentTimeMillis()
            )
        )
        renumberSets(set.exerciseId, set.sessionId)
    }

    fun getWorkoutDay(dayId: Long): Flow<WorkoutDayEntity?> =
        programDao.getWorkoutDayFlow(dayId)

    suspend fun insertExercise(exercise: ExerciseEntity) {
        val maxOrder = programDao.getMaxOrderForDay(exercise.dayId) ?: -1
        programDao.insertExercise(exercise.copy(order = maxOrder + 1))
    }

    suspend fun updateExercise(exercise: ExerciseEntity) = programDao.updateExercise(exercise)

    suspend fun deleteExercise(exercise: ExerciseEntity) = programDao.deleteExercise(exercise)

    suspend fun moveExercise(exerciseId: Long, up: Boolean) {
        val exercise = programDao.getExerciseById(exerciseId) ?: return
        val exercises = programDao.getExercisesForDay(exercise.dayId).sortedBy { it.order }
        val currentIndex = exercises.indexOfFirst { it.id == exerciseId }
        
        val targetIndex = if (up) currentIndex - 1 else currentIndex + 1
        
        if (targetIndex in exercises.indices) {
            val targetExercise = exercises[targetIndex]
            val newOrderForCurrent = targetExercise.order
            val newOrderForTarget = exercise.order
            
            programDao.updateExercise(exercise.copy(order = newOrderForCurrent))
            programDao.updateExercise(targetExercise.copy(order = newOrderForTarget))
        }
    }

    suspend fun duplicateExercise(exerciseId: Long) {
        val exercise = programDao.getExerciseById(exerciseId) ?: return
        val maxOrder = programDao.getMaxOrderForDay(exercise.dayId) ?: 0
        val newExerciseId = programDao.insertExercise(
            exercise.copy(
                id = 0,
                name = "${exercise.name} (Copy)",
                order = maxOrder + 1,
                restTimerSeconds = exercise.restTimerSeconds,
                useDefaultRestTimer = exercise.useDefaultRestTimer,
                createdAt = System.currentTimeMillis()
            )
        )

        // Duplicate Sets
        val sets = programDao.getSetsForExercise(exerciseId)
        for (set in sets) {
            programDao.insertSet(set.copy(id = 0, exerciseId = newExerciseId))
        }
    }

    suspend fun insertWorkoutDay(programId: Long, name: String, notes: String) {
        val maxOrder = programDao.getMaxOrderForProgram(programId) ?: -1
        programDao.insertDay(
            WorkoutDayEntity(
                programId = programId,
                name = name,
                notes = notes,
                order = maxOrder + 1
            )
        )
    }

    // Session Management
    fun getCompletedSessions(): Flow<List<WorkoutSessionEntity>> = sessionDao.getCompletedSessions()
    
    fun getActiveSession(): Flow<WorkoutSessionEntity?> = sessionDao.getActiveSession()
    
    fun getSession(sessionId: Long): Flow<WorkoutSessionEntity?> = sessionDao.getSessionByIdFlow(sessionId)
    
    fun getWeeklyVolume(): Flow<Double?> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        return sessionDao.getVolumeSince(calendar.timeInMillis)
    }

    fun getTopPersonalRecords(): Flow<List<Pair<String, Double>>> = 
        sessionDao.getTopPersonalRecords().map { list ->
            list.map { it.exerciseName to it.maxWeight }
        }

    suspend fun startWorkoutSession(dayId: Long, dayName: String, programName: String): Long {
        val sessionId = sessionDao.insertSession(
            WorkoutSessionEntity(
                dayId = dayId,
                dayName = dayName,
                programName = programName
            )
        )
        
        // Copy sets from template (sessionId = 0) to new session
        val exercises = programDao.getExercisesForDay(dayId)
        for (exercise in exercises) {
            val templateSets = programDao.getSetsForExerciseAndSession(exercise.id, 0)
            for (set in templateSets) {
                programDao.insertSet(set.copy(
                    id = 0,
                    sessionId = sessionId,
                    isCompleted = false,
                    createdAt = System.currentTimeMillis()
                ))
            }
        }
        
        return sessionId
    }

    suspend fun updateWorkoutSession(session: WorkoutSessionEntity) = sessionDao.updateSession(session)

    suspend fun finishWorkoutSession(sessionId: Long, durationSeconds: Long) {
        val session = sessionDao.getSessionById(sessionId)
        session?.let {
            sessionDao.updateSession(it.copy(
                isCompleted = true,
                endTime = System.currentTimeMillis(),
                durationSeconds = durationSeconds
            ))
        }
    }

    suspend fun deleteSession(sessionId: Long) = sessionDao.deleteSessionById(sessionId)

    suspend fun updateWorkoutDay(day: WorkoutDayEntity) = programDao.updateDay(day)

    suspend fun deleteWorkoutDay(day: WorkoutDayEntity) = programDao.deleteDay(day)

    suspend fun moveDay(dayId: Long, up: Boolean) {
        // Simple swap logic
        val day = programDao.getDayById(dayId) ?: return
        val days = programDao.getDaysForProgram(day.programId).sortedBy { it.order }
        val currentIndex = days.indexOfFirst { it.id == dayId }
        
        val targetIndex = if (up) currentIndex - 1 else currentIndex + 1
        
        if (targetIndex in days.indices) {
            val targetDay = days[targetIndex]
            val newOrderForCurrent = targetDay.order
            val newOrderForTarget = day.order
            
            programDao.updateDay(day.copy(order = newOrderForCurrent))
            programDao.updateDay(targetDay.copy(order = newOrderForTarget))
        }
    }

    suspend fun duplicateWorkoutDay(dayId: Long) {
        val day = programDao.getDayById(dayId) ?: return
        val maxOrder = programDao.getMaxOrderForProgram(day.programId) ?: 0
        val newDayId = programDao.insertDay(
            day.copy(
                id = 0,
                name = "${day.name} (Copy)",
                order = maxOrder + 1
            )
        )

        // Duplicate Exercises and Sets
        val exercises = programDao.getExercisesForDay(dayId)
        for (exercise in exercises) {
            val newExerciseId = programDao.insertExercise(exercise.copy(id = 0, dayId = newDayId))
            val sets = programDao.getSetsForExercise(exercise.id)
            for (set in sets) {
                programDao.insertSet(set.copy(id = 0, exerciseId = newExerciseId))
            }
        }
    }
}
