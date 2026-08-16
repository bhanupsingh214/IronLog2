package com.bhanu.ironlog.data.util

import com.bhanu.ironlog.data.local.entity.GoalEntity
import com.bhanu.ironlog.data.model.goals.GoalProgress
import com.bhanu.ironlog.data.model.goals.GoalStatus
import com.bhanu.ironlog.data.model.goals.GoalTrend
import com.bhanu.ironlog.data.model.goals.GoalTrendPoint
import com.bhanu.ironlog.data.model.goals.GoalType
import kotlin.math.abs
import kotlin.math.max

object GoalCalculator {
    const val WEIGHT_STABLE_TOLERANCE_KG = 0.2
    const val WAIST_STABLE_TOLERANCE_CM = 0.5
    private const val MILLIS_PER_DAY = 86_400_000.0

    fun calculate(
        goal: GoalEntity,
        currentValue: Double?,
        trendPoints: List<GoalTrendPoint>,
        frequencyCompleted: Int = 0,
        now: Long = System.currentTimeMillis()
    ): GoalProgress {
        val type = GoalType.entries.firstOrNull { it.key == goal.type }
        val effectiveCurrent = if (type == GoalType.WORKOUT_FREQUENCY) frequencyCompleted.toDouble() else currentValue
        val progress = calculateProgress(goal, effectiveCurrent)
        val trend = if (type == GoalType.WORKOUT_FREQUENCY) {
            null
        } else {
            val tolerance = when (type) {
                GoalType.WEIGHT -> WEIGHT_STABLE_TOLERANCE_KG
                GoalType.WAIST -> WAIST_STABLE_TOLERANCE_CM
                else -> 0.0
            }
            calculateDirectionalTrend(goal.targetValue, trendPoints, tolerance)
        }
        val trendRate = calculateTrendRate(trendPoints)
        val expected = calculateExpectedProgress(goal.startDate, goal.deadline, now)
        val status = calculateStatus(progress, expected, goal.deadline, now)

        return GoalProgress(
            goal = goal,
            currentValue = effectiveCurrent,
            progress = progress,
            trend = trend,
            trendRatePerDay = trendRate,
            status = status,
            expectedProgress = expected
        )
    }

    fun calculateProgress(goal: GoalEntity, currentValue: Double?): Double? {
        if (goal.type == GoalType.WORKOUT_FREQUENCY.key) {
            val target = goal.frequencyCount ?: return null
            if (target <= 0) return null
            return ((currentValue ?: 0.0) / target.toDouble()).coerceIn(0.0, 1.0)
        }

        val current = currentValue ?: return null
        val start = goal.startingValue
        val target = goal.targetValue
        if (target == start) return 1.0

        val raw = if (target > start) {
            (current - start) / (target - start)
        } else {
            (start - current) / (start - target)
        }
        return raw.coerceIn(0.0, 1.0)
    }

    fun calculateDirectionalTrend(
        targetValue: Double,
        points: List<GoalTrendPoint>,
        tolerance: Double = 0.0
    ): GoalTrend {
        if (points.size < 3) return GoalTrend.INSUFFICIENT_DATA
        val recent = points.sortedBy { it.timestamp }.takeLast(3)
        val p1 = recent[2]
        val p2 = recent[1]
        if (tolerance > 0.0 && abs(p1.value - p2.value) <= tolerance) return GoalTrend.STABLE

        val p1Distance = abs(targetValue - p1.value)
        val p2Distance = abs(targetValue - p2.value)
        return when {
            p1Distance < p2Distance -> GoalTrend.IMPROVING
            p1Distance > p2Distance -> GoalTrend.MOVING_AWAY
            else -> GoalTrend.STABLE
        }
    }

    fun calculateTrendRate(points: List<GoalTrendPoint>): Double? {
        if (points.size < 2) return null
        val recent = points.sortedBy { it.timestamp }.takeLast(2)
        val days = max((recent[1].timestamp - recent[0].timestamp) / MILLIS_PER_DAY, 0.0)
        return if (days > 0.0) (recent[1].value - recent[0].value) / days else null
    }

    fun calculateExpectedProgress(startDate: Long, deadline: Long?, now: Long): Double? {
        if (deadline == null || deadline <= startDate) return null
        return ((now - startDate).toDouble() / (deadline - startDate).toDouble()).coerceIn(0.0, 1.0)
    }

    fun calculateStatus(
        actualProgress: Double?,
        expectedProgress: Double?,
        deadline: Long?,
        now: Long
    ): GoalStatus? {
        if (actualProgress == null || deadline == null) return null
        if (actualProgress >= 1.0) return GoalStatus.COMPLETED
        if (now > deadline) return GoalStatus.OVERDUE
        if (expectedProgress == null) return null
        return if (actualProgress >= expectedProgress) GoalStatus.ON_TRACK else GoalStatus.BEHIND
    }
}
