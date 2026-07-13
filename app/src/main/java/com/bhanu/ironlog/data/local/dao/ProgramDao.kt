package com.bhanu.ironlog.data.local.dao

import androidx.room.*
import com.bhanu.ironlog.data.local.entity.*
import com.bhanu.ironlog.data.local.pojo.ProgramWithStats
import com.bhanu.ironlog.data.local.pojo.WorkoutDayWithStats
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramDao {
    @Query("""
        SELECT p.*, 
        (SELECT COUNT(*) FROM workout_days WHERE programId = p.id) as dayCount,
        (SELECT COUNT(e.id) FROM workout_days wd LEFT JOIN exercises e ON wd.id = e.dayId WHERE wd.programId = p.id) as exerciseCount
        FROM programs p
        WHERE p.isArchived = 0
    """)
    fun getAllProgramsWithStats(): Flow<List<ProgramWithStats>>

    @Query("""
        SELECT p.*, 
        (SELECT COUNT(*) FROM workout_days WHERE programId = p.id) as dayCount,
        (SELECT COUNT(e.id) FROM workout_days wd LEFT JOIN exercises e ON wd.id = e.dayId WHERE wd.programId = p.id) as exerciseCount
        FROM programs p
        WHERE p.isArchived = 1
        ORDER BY lastModifiedAt DESC
    """)
    fun getArchivedProgramsWithStats(): Flow<List<ProgramWithStats>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgram(program: ProgramEntity): Long

    @Update
    suspend fun updateProgram(program: ProgramEntity)

    @Delete
    suspend fun deleteProgram(program: ProgramEntity)

    @Transaction
    suspend fun activateProgram(programId: Long) {
        deactivateAllPrograms()
        setProgramActive(programId)
    }

    @Query("UPDATE programs SET isActive = 0")
    suspend fun deactivateAllPrograms()

    @Query("UPDATE programs SET isActive = 1 WHERE id = :programId")
    suspend fun setProgramActive(programId: Long)

    @Query("SELECT * FROM programs WHERE id = :id")
    suspend fun getProgramById(id: Long): ProgramEntity?

    @Query("""
        SELECT p.*, 
        (SELECT COUNT(*) FROM workout_days WHERE programId = p.id) as dayCount,
        (SELECT COUNT(e.id) FROM workout_days wd LEFT JOIN exercises e ON wd.id = e.dayId WHERE wd.programId = p.id) as exerciseCount
        FROM programs p
        WHERE p.isActive = 1 AND p.isArchived = 0
        LIMIT 1
    """)
    fun getActiveProgramWithStats(): Flow<ProgramWithStats?>

    // Support for duplication
    @Query("SELECT * FROM workout_days WHERE programId = :programId")
    suspend fun getDaysForProgram(programId: Long): List<WorkoutDayEntity>

    @Query("SELECT * FROM exercises WHERE dayId = :dayId ORDER BY `order` ASC")
    fun getExercisesForDayFlow(dayId: Long): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE dayId = :dayId")
    suspend fun getExercisesForDay(dayId: Long): List<ExerciseEntity>

    @Query("SELECT * FROM exercise_sets WHERE exerciseId = :exerciseId")
    suspend fun getSetsForExercise(exerciseId: Long): List<SetEntity>

    @Insert
    suspend fun insertDay(day: WorkoutDayEntity): Long

    @Insert
    suspend fun insertExercise(exercise: ExerciseEntity): Long

    @Insert
    suspend fun insertSet(set: SetEntity): Long

    // Exercise Management
    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

    @Delete
    suspend fun deleteExercise(exercise: ExerciseEntity)

    @Query("SELECT MAX(`order`) FROM exercises WHERE dayId = :dayId")
    suspend fun getMaxOrderForDay(dayId: Long): Int?

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExerciseById(id: Long): ExerciseEntity?

    @Query("SELECT * FROM workout_days WHERE id = :id")
    fun getWorkoutDayFlow(id: Long): Flow<WorkoutDayEntity?>

    // Workout Day Management
    @Query("""
        SELECT wd.*, 
        (SELECT COUNT(*) FROM exercises WHERE dayId = wd.id) as exerciseCount
        FROM workout_days wd
        WHERE wd.programId = :programId
        ORDER BY wd.`order` ASC
    """)
    fun getWorkoutDaysWithStats(programId: Long): Flow<List<WorkoutDayWithStats>>

    @Update
    suspend fun updateDay(day: WorkoutDayEntity)

    @Delete
    suspend fun deleteDay(day: WorkoutDayEntity)

    @Query("SELECT MAX(`order`) FROM workout_days WHERE programId = :programId")
    suspend fun getMaxOrderForProgram(programId: Long): Int?

    @Query("SELECT * FROM workout_days WHERE id = :id")
    suspend fun getDayById(id: Long): WorkoutDayEntity?
}
