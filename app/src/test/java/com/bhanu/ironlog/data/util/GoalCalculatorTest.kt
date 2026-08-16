package com.bhanu.ironlog.data.util

import com.bhanu.ironlog.data.local.entity.GoalEntity
import com.bhanu.ironlog.data.model.goals.GoalStatus
import com.bhanu.ironlog.data.model.goals.GoalTrend
import com.bhanu.ironlog.data.model.goals.GoalTrendPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GoalCalculatorTest {
    @Test
    fun equalStartAndTargetCompletesBeforeFormula() {
        val goal = GoalEntity(type = "WEIGHT", targetValue = 80.0, startingValue = 80.0, startDate = 1_000L)
        val result = GoalCalculator.calculate(goal, 80.0, emptyList(), now = 2_000L)
        assertEquals(1.0, result.progress!!, 0.0001)
    }

    @Test
    fun increasingProgressIsDirectionAwareAndClamped() {
        val goal = GoalEntity(type = "WEIGHT", targetValue = 90.0, startingValue = 80.0, startDate = 1_000L)
        assertEquals(0.5, GoalCalculator.calculateProgress(goal, 85.0)!!, 0.0001)
        assertEquals(1.0, GoalCalculator.calculateProgress(goal, 100.0)!!, 0.0001)
    }

    @Test
    fun decreasingProgressIsDirectionAwareAndClamped() {
        val goal = GoalEntity(type = "WAIST", targetValue = 80.0, startingValue = 90.0, startDate = 1_000L)
        assertEquals(0.5, GoalCalculator.calculateProgress(goal, 85.0)!!, 0.0001)
        assertEquals(1.0, GoalCalculator.calculateProgress(goal, 70.0)!!, 0.0001)
    }

    @Test
    fun trendUsesThreeMostRecentPointsAndTargetDistance() {
        val points = listOf(
            GoalTrendPoint(1_000L, 90.0),
            GoalTrendPoint(2_000L, 88.0),
            GoalTrendPoint(3_000L, 85.0),
            GoalTrendPoint(4_000L, 84.0)
        )
        assertEquals(GoalTrend.IMPROVING, GoalCalculator.calculateDirectionalTrend(80.0, points))
    }

    @Test
    fun stableWinsWhenLatestDeltaIsWithinTolerance() {
        val points = listOf(
            GoalTrendPoint(1_000L, 90.0),
            GoalTrendPoint(2_000L, 88.0),
            GoalTrendPoint(3_000L, 88.1)
        )
        assertEquals(GoalTrend.STABLE, GoalCalculator.calculateDirectionalTrend(80.0, points, 0.2))
    }

    @Test
    fun fewerThanThreePointsIsInsufficient() {
        val points = listOf(GoalTrendPoint(1_000L, 90.0), GoalTrendPoint(2_000L, 88.0))
        assertEquals(GoalTrend.INSUFFICIENT_DATA, GoalCalculator.calculateDirectionalTrend(80.0, points))
    }

    @Test
    fun deadlineStatusUsesCompletionThenOverdueThenAdherence() {
        val deadline = 11_000L
        assertEquals(GoalStatus.COMPLETED, GoalCalculator.calculateStatus(1.0, 0.5, deadline, 10_000L))
        assertEquals(GoalStatus.OVERDUE, GoalCalculator.calculateStatus(0.5, 1.0, deadline, 12_000L))
        assertEquals(GoalStatus.BEHIND, GoalCalculator.calculateStatus(0.2, 0.5, deadline, 5_000L))
        assertEquals(GoalStatus.ON_TRACK, GoalCalculator.calculateStatus(0.6, 0.5, deadline, 5_000L))
        assertNull(GoalCalculator.calculateStatus(0.2, null, null, 5_000L))
    }

    @Test
    fun frequencyProgressUsesCalendarTargetCount() {
        val goal = GoalEntity(type = "WORKOUT_FREQUENCY", targetValue = 4.0, startingValue = 0.0, frequencyCount = 4, frequencyPeriod = "WEEKLY", startDate = 1_000L)
        assertEquals(0.5, GoalCalculator.calculateProgress(goal, 2.0)!!, 0.0001)
        assertEquals(1.0, GoalCalculator.calculateProgress(goal, 8.0)!!, 0.0001)
    }
}
