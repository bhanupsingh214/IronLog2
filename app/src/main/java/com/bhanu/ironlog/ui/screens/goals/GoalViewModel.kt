package com.bhanu.ironlog.ui.screens.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.ironlog.data.local.entity.GoalEntity
import com.bhanu.ironlog.data.local.pojo.TrackableExercise
import com.bhanu.ironlog.data.local.entity.PersonalRecordEntity
import com.bhanu.ironlog.data.model.goals.GoalProgress
import com.bhanu.ironlog.data.model.goals.GoalTrendPoint
import com.bhanu.ironlog.data.model.goals.GoalType
import com.bhanu.ironlog.data.repository.AnalyticsRepository
import com.bhanu.ironlog.data.repository.BodyProgressRepository
import com.bhanu.ironlog.data.repository.GoalRepository
import com.bhanu.ironlog.data.repository.HistoryRepository
import com.bhanu.ironlog.data.repository.PersonalRecordRepository
import com.bhanu.ironlog.data.util.GoalCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class GoalViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val bodyProgressRepository: BodyProgressRepository,
    private val personalRecordRepository: PersonalRecordRepository,
    private val historyRepository: HistoryRepository,
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {
    private val weightHistory = bodyProgressRepository.getWeightHistory()
    private val waistHistory = bodyProgressRepository.getWaistHistory()
    private val personalRecords = personalRecordRepository.getAllPRs()
    private val completedSessions = historyRepository.getCompletedSessions()

    val latestWeight: StateFlow<Double?> = bodyProgressRepository.getLatestWeight().map { it?.weightKg }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val latestWaist: StateFlow<Double?> = bodyProgressRepository.getLatestWaist().map { it?.circumferenceCm }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val currentPersonalRecords: StateFlow<List<PersonalRecordEntity>> = personalRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val trackableExercises: StateFlow<List<TrackableExercise>> = analyticsRepository.getTrackableExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val goals: StateFlow<List<GoalProgress>> = goalRepository.getGoals()
        .flatMapLatest { goals -> if (goals.isEmpty()) flowOf(emptyList()) else combine(goals.map { goal -> progressFlow(goal) }) { it.toList() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun createGoal(type: GoalType, targetValue: Double, startingValue: Double, libraryExerciseId: Long? = null, frequencyCount: Int? = null, frequencyPeriod: String? = null, deadline: Long? = null) {
        viewModelScope.launch {
            if (type == GoalType.WORKOUT_FREQUENCY) {
                if (!GoalCalculator.isValidFrequencyTarget(targetValue)) return@launch
            }
            val baseline = if (type == GoalType.EXERCISE_PR && libraryExerciseId != null) {
                personalRecordRepository.getPRForExercise(libraryExerciseId, 0L)?.weightPR ?: 0.0
            } else startingValue
            goalRepository.createGoal(GoalEntity(type = type.key, targetValue = targetValue, startingValue = baseline, libraryExerciseId = libraryExerciseId, frequencyCount = if (type == GoalType.WORKOUT_FREQUENCY) targetValue.toInt() else frequencyCount, frequencyPeriod = frequencyPeriod, startDate = System.currentTimeMillis(), deadline = deadline))
        }
    }

    fun updateGoal(goal: GoalEntity, targetValue: Double, deadline: Long?) {
        viewModelScope.launch {
            val type = GoalType.entries.firstOrNull { it.key == goal.type }
            if (type == GoalType.WORKOUT_FREQUENCY) {
                if (!GoalCalculator.isValidFrequencyTarget(targetValue)) return@launch
            }
            goalRepository.updateGoal(goal.copy(targetValue = targetValue, frequencyCount = if (type == GoalType.WORKOUT_FREQUENCY) targetValue.toInt() else goal.frequencyCount, deadline = deadline))
        }
    }

    fun deleteGoal(goal: GoalEntity) { viewModelScope.launch { goalRepository.deleteGoal(goal) } }

    private fun progressFlow(goal: GoalEntity): Flow<GoalProgress> {
        val clock = flow { while (true) { emit(System.currentTimeMillis()); delay(60_000) } }
        return when (goal.type) {
            GoalType.WEIGHT.key -> combine(weightHistory, clock) { history, now -> GoalCalculator.calculate(goal, history.firstOrNull()?.weightKg, history.map { GoalTrendPoint(it.timestamp, it.weightKg) }, now = now) }
            GoalType.WAIST.key -> combine(waistHistory, clock) { history, now -> GoalCalculator.calculate(goal, history.firstOrNull()?.circumferenceCm, history.map { GoalTrendPoint(it.timestamp, it.circumferenceCm) }, now = now) }
            GoalType.WORKOUT_FREQUENCY.key -> combine(completedSessions, clock) { sessions, now -> GoalCalculator.calculate(goal, null, emptyList(), sessions.count { it.createdAt in currentCalendarWindow(goal.frequencyPeriod) }, now) }
            GoalType.EXERCISE_PR.key -> {
                val libraryId = goal.libraryExerciseId ?: 0L
                combine(personalRecords, analyticsRepository.getExerciseStrengthHistory(libraryId, 0L), clock) { prs, history, now ->
                    val current = prs.firstOrNull { it.libraryExerciseId == libraryId && it.exerciseTemplateId == 0L }?.weightPR
                    GoalCalculator.calculate(goal, current, history.map { GoalTrendPoint(it.date, it.maxWeight) }, now = now)
                }
            }
            else -> flowOf(GoalCalculator.calculate(goal, null, emptyList()))
        }
    }

    private fun currentCalendarWindow(period: String?): LongRange {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        return if (period == "MONTHLY") {
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val start = calendar.timeInMillis
            calendar.add(Calendar.MONTH, 1)
            start until calendar.timeInMillis
        } else {
            calendar.firstDayOfWeek = Calendar.MONDAY
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            val start = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, 7)
            start until calendar.timeInMillis
        }
    }
}
