package com.bhanu.ironlog.data.repository

import com.bhanu.ironlog.data.local.dao.LibraryExerciseDao
import com.bhanu.ironlog.data.local.entity.LibraryExerciseEntity
import com.bhanu.ironlog.util.ExerciseNormalizationUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseLibraryRepository @Inject constructor(
    private val libraryDao: LibraryExerciseDao
) {
    val allActiveExercises: Flow<List<LibraryExerciseEntity>> = libraryDao.getAllActiveExercises()

    fun searchExercises(query: String): Flow<List<LibraryExerciseEntity>> = libraryDao.searchExercises(query)

    suspend fun getExerciseByNormalizedName(name: String): LibraryExerciseEntity? {
        return libraryDao.findByNormalizedName(ExerciseNormalizationUtil.normalize(name))
    }

    /**
     * Handles the full save flow for an exercise.
     * Performs normalization, exact duplicate detection, and similarity detection.
     */
    suspend fun validateAndSaveExercise(
        exercise: LibraryExerciseEntity,
        ignoreSimilarity: Boolean = false
    ): SaveExerciseResult {
        val normalized = ExerciseNormalizationUtil.normalize(exercise.name)
        
        // 1. Exact Duplicate Detection
        val existingExact = libraryDao.findByNormalizedName(normalized)
        if (existingExact != null) {
            return SaveExerciseResult.ExactDuplicate(existingExact)
        }

        // 2. Similarity Detection (if not explicitly ignored by user)
        if (!ignoreSimilarity) {
            val similar = findSimilarExercisesInternal(normalized)
            if (similar.isNotEmpty()) {
                return SaveExerciseResult.SimilarFound(similar)
            }
        }

        // 3. Create Exercise
        val id = libraryDao.insert(exercise.copy(normalizedName = normalized, updatedAt = System.currentTimeMillis()))
        return SaveExerciseResult.Success(id)
    }

    suspend fun updateExercise(exercise: LibraryExerciseEntity) {
        val normalized = ExerciseNormalizationUtil.normalize(exercise.name)
        libraryDao.update(exercise.copy(
            normalizedName = normalized,
            updatedAt = System.currentTimeMillis()
        ))
    }

    suspend fun archiveExercise(id: Long) {
        libraryDao.softDelete(id)
    }

    /**
     * Internal helper for similarity detection during save flow.
     */
    private suspend fun findSimilarExercisesInternal(normalized: String): List<LibraryExerciseEntity> {
        if (normalized.length < 3) return emptyList()
        val all = libraryDao.getAllActiveExercises().first()
        return all.filter { existing ->
            existing.normalizedName.contains(normalized) || normalized.contains(existing.normalizedName)
        }.take(5)
    }

    /**
     * Public API for UI to check similarity while typing (optional/future)
     */
    suspend fun findSimilarExercises(name: String): List<LibraryExerciseEntity> {
        return findSimilarExercisesInternal(ExerciseNormalizationUtil.normalize(name))
    }

    suspend fun seedLibraryIfNeeded() {
        if (libraryDao.count() > 0) return

        val builtInExercises = listOf(
            // Chest
            createSystemExercise("Bench Press (Barbell)", "Chest", "Barbell", "Compound"),
            createSystemExercise("Incline Bench Press (Barbell)", "Chest", "Barbell", "Compound"),
            createSystemExercise("Decline Bench Press (Barbell)", "Chest", "Barbell", "Compound"),
            createSystemExercise("Dumbbell Press", "Chest", "Dumbbell", "Compound"),
            createSystemExercise("Incline Dumbbell Press", "Chest", "Dumbbell", "Compound"),
            createSystemExercise("Chest Fly", "Chest", "Dumbbell", "Isolation"),
            createSystemExercise("Push Up", "Chest", "Bodyweight", "Compound"),
            createSystemExercise("Cable Fly", "Chest", "Cable", "Isolation"),

            // Back
            createSystemExercise("Deadlift (Barbell)", "Back", "Barbell", "Compound"),
            createSystemExercise("Bent Over Row (Barbell)", "Back", "Barbell", "Compound"),
            createSystemExercise("Pull Up", "Back", "Bodyweight", "Compound"),
            createSystemExercise("Lat Pulldown (Cable)", "Back", "Cable", "Isolation"),
            createSystemExercise("Seated Cable Row", "Back", "Cable", "Compound"),
            createSystemExercise("One Arm Dumbbell Row", "Back", "Dumbbell", "Compound"),
            createSystemExercise("Face Pull", "Back", "Cable", "Isolation"),
            createSystemExercise("T-Bar Row", "Back", "Barbell", "Compound"),

            // Shoulders
            createSystemExercise("Overhead Press (Barbell)", "Shoulders", "Barbell", "Compound"),
            createSystemExercise("Dumbbell Shoulder Press", "Shoulders", "Dumbbell", "Compound"),
            createSystemExercise("Lateral Raise (Dumbbell)", "Shoulders", "Dumbbell", "Isolation"),
            createSystemExercise("Front Raise (Dumbbell)", "Shoulders", "Dumbbell", "Isolation"),
            createSystemExercise("Rear Delt Fly (Dumbbell)", "Shoulders", "Dumbbell", "Isolation"),
            createSystemExercise("Arnold Press", "Shoulders", "Dumbbell", "Compound"),
            createSystemExercise("Upright Row (Barbell)", "Shoulders", "Barbell", "Compound"),

            // Legs
            createSystemExercise("Squat (Barbell)", "Legs", "Barbell", "Compound"),
            createSystemExercise("Front Squat (Barbell)", "Legs", "Barbell", "Compound"),
            createSystemExercise("Leg Press", "Legs", "Machine", "Compound"),
            createSystemExercise("Leg Extension", "Legs", "Machine", "Isolation"),
            createSystemExercise("Leg Curl", "Legs", "Machine", "Isolation"),
            createSystemExercise("Lunge (Dumbbell)", "Legs", "Dumbbell", "Compound"),
            createSystemExercise("Romanian Deadlift (Barbell)", "Legs", "Barbell", "Compound"),
            createSystemExercise("Calf Raise (Standing)", "Legs", "Machine", "Isolation"),
            createSystemExercise("Bulgarian Split Squat", "Legs", "Dumbbell", "Compound"),

            // Arms - Biceps
            createSystemExercise("Bicep Curl (Barbell)", "Arms", "Barbell", "Isolation"),
            createSystemExercise("Bicep Curl (Dumbbell)", "Arms", "Dumbbell", "Isolation"),
            createSystemExercise("Hammer Curl", "Arms", "Dumbbell", "Isolation"),
            createSystemExercise("Preacher Curl", "Arms", "Barbell", "Isolation"),
            createSystemExercise("Concentration Curl", "Arms", "Dumbbell", "Isolation"),

            // Arms - Triceps
            createSystemExercise("Tricep Pushdown (Cable)", "Arms", "Cable", "Isolation"),
            createSystemExercise("Skull Crusher", "Arms", "Barbell", "Isolation"),
            createSystemExercise("Tricep Extension (Dumbbell)", "Arms", "Dumbbell", "Isolation"),
            createSystemExercise("Dip", "Arms", "Bodyweight", "Compound"),
            createSystemExercise("Close Grip Bench Press", "Arms", "Barbell", "Compound"),

            // Core
            createSystemExercise("Plank", "Core", "Bodyweight", "Isolation"),
            createSystemExercise("Crunch", "Core", "Bodyweight", "Isolation"),
            createSystemExercise("Leg Raise", "Core", "Bodyweight", "Isolation"),
            createSystemExercise("Russian Twist", "Core", "Bodyweight", "Isolation"),
            createSystemExercise("Hanging Leg Raise", "Core", "Bodyweight", "Isolation"),

            // Cardio
            createSystemExercise("Running", "Cardio", "None", "Cardio"),
            createSystemExercise("Cycling", "Cardio", "None", "Cardio"),
            createSystemExercise("Swimming", "Cardio", "None", "Cardio"),
            createSystemExercise("Jump Rope", "Cardio", "None", "Cardio"),
            createSystemExercise("Walking", "Cardio", "None", "Cardio")
        )

        builtInExercises.forEach { libraryDao.insert(it) }
    }

    private fun createSystemExercise(
        name: String,
        muscle: String,
        equipment: String,
        type: String
    ): LibraryExerciseEntity {
        val normalized = ExerciseNormalizationUtil.normalize(name)
        return LibraryExerciseEntity(
            systemKey = normalized,
            name = name,
            normalizedName = normalized,
            muscleGroup = muscle,
            equipment = equipment,
            exerciseType = type,
            createdBy = "System"
        )
    }
}
