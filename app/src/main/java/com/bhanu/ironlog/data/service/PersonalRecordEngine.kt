package com.bhanu.ironlog.data.service

import com.bhanu.ironlog.data.local.entity.PersonalRecordEntity
import com.bhanu.ironlog.data.local.entity.SessionSet
import com.bhanu.ironlog.data.repository.PersonalRecordRepository
import com.bhanu.ironlog.data.repository.WorkoutSessionRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonalRecordEngine @Inject constructor(
    private val prRepository: PersonalRecordRepository,
    private val sessionRepository: WorkoutSessionRepository
) {

    /**
     * Calculates Estimated 1RM using Epley Formula: 1RM = weight * (1 + reps / 30.0)
     */
    fun calculateE1RM(weight: Double, reps: Int): Double {
        if (reps <= 0) return 0.0
        if (reps == 1) return weight
        return weight * (1.0 + reps / 30.0)
    }

    /**
     * Processes a completed session to detect and store new PRs.
     * Returns a list of strings describing the new PRs achieved.
     */
    suspend fun processSessionPRs(sessionId: Long): List<Achievement> {
        val achievements = mutableListOf<Achievement>()
        val sessionExercises = sessionRepository.getExercisesWithTemplateForSession(sessionId).first()
        
        for (sessionExercise in sessionExercises) {
            val templateId = sessionExercise.sessionExercise.exerciseTemplateId
            val exerciseName = sessionExercise.sessionExercise.exerciseName.ifBlank { sessionExercise.template?.name ?: "Unknown" }
            val sets = sessionRepository.getSetsForExercise(sessionExercise.sessionExercise.sessionExerciseId).first()
            
            if (sets.isEmpty()) continue
            
            val currentPR = prRepository.getPRForExercise(templateId) ?: PersonalRecordEntity(exerciseTemplateId = templateId)
            var updatedPR = currentPR
            var weightImproved = false
            var e1RMImproved = false
            
            var bestWeight = currentPR.weightPR
            var bestE1RM = currentPR.estimated1RM
            
            for (set in sets) {
                if (!set.completed) continue
                
                // Check Weight PR
                if (set.weight > bestWeight) {
                    bestWeight = set.weight
                    weightImproved = true
                }
                
                // Check e1RM PR
                val currentE1RM = calculateE1RM(set.weight, set.reps)
                if (currentE1RM > bestE1RM) {
                    bestE1RM = currentE1RM
                    e1RMImproved = true
                }
            }
            
            if (weightImproved || e1RMImproved) {
                if (weightImproved) {
                    updatedPR = updatedPR.copy(
                        weightPR = bestWeight,
                        weightPRDate = System.currentTimeMillis(),
                        weightPRSessionId = sessionId
                    )
                    achievements.add(Achievement(exerciseName, AchievementType.WEIGHT_PR, bestWeight))
                }
                if (e1RMImproved) {
                    updatedPR = updatedPR.copy(
                        estimated1RM = bestE1RM,
                        estimated1RMDate = System.currentTimeMillis(),
                        estimated1RMSessionId = sessionId
                    )
                    achievements.add(Achievement(exerciseName, AchievementType.E1RM_PR, bestE1RM))
                }
                
                prRepository.updatePR(updatedPR.copy(updatedAt = System.currentTimeMillis()))
            }
        }
        
        return achievements
    }
}

data class Achievement(
    val exerciseName: String,
    val type: AchievementType,
    val value: Double
)

enum class AchievementType {
    WEIGHT_PR,
    E1RM_PR
}
