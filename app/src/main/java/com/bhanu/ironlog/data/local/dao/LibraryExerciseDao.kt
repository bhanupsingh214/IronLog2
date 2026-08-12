package com.bhanu.ironlog.data.local.dao

import androidx.room.*
import com.bhanu.ironlog.data.local.entity.LibraryExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryExerciseDao {
    @Query("SELECT * FROM exercise_library WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveExercises(): Flow<List<LibraryExerciseEntity>>

    @Query("SELECT * FROM exercise_library ORDER BY name ASC")
    suspend fun getAllExercises(): List<LibraryExerciseEntity>

    @Query("SELECT * FROM exercise_library WHERE isActive = 1 AND (name LIKE '%' || :query || '%' OR muscleGroup LIKE '%' || :query || '%') ORDER BY name ASC")
    fun searchExercises(query: String): Flow<List<LibraryExerciseEntity>>

    @Query("SELECT * FROM exercise_library WHERE normalizedName = :normalizedName LIMIT 1")
    suspend fun findByNormalizedName(normalizedName: String): LibraryExerciseEntity?

    @Query("SELECT * FROM exercise_library WHERE systemKey = :systemKey LIMIT 1")
    suspend fun findBySystemKey(systemKey: String): LibraryExerciseEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(exercise: LibraryExerciseEntity): Long

    @Update
    suspend fun update(exercise: LibraryExerciseEntity)

    @Query("UPDATE exercise_library SET isActive = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDelete(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE exercise_library SET isActive = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun restore(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM exercise_library")
    suspend fun count(): Int
}
