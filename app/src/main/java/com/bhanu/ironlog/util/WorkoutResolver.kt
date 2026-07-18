package com.bhanu.ironlog.util

import com.bhanu.ironlog.data.local.pojo.WorkoutDayWithStats
import java.util.Calendar
import java.util.Locale

object WorkoutResolver {
    private val WEEKDAYS = listOf(
        "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    )

    /**
     * Returns today's weekday name in English (e.g., "Monday").
     */
    fun getTodayWeekday(): String {
        val calendar = Calendar.getInstance()
        return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US) ?: ""
    }

    /**
     * Resolves which workout day from the given list should be performed today.
     * Uses a multi-step matching strategy to handle localization and naming variations.
     */
    fun resolveTodayWorkout(days: List<WorkoutDayWithStats>): WorkoutDayWithStats? {
        if (days.isEmpty()) return null
        
        val today = getTodayWeekday()
        val calendar = Calendar.getInstance()
        val localizedToday = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()) ?: ""
        val shortToday = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()) ?: ""

        // 1. Try exact match against English standard (reliable source of truth)
        val standardMatch = days.find { 
            it.day.isEnabled && it.day.name.equals(today, ignoreCase = true) 
        }
        if (standardMatch != null) return standardMatch

        // 2. Try exact match against localized long/short names
        val localizedMatch = days.find { 
            it.day.isEnabled && (
                it.day.name.equals(localizedToday, ignoreCase = true) ||
                it.day.name.equals(shortToday, ignoreCase = true)
            )
        }
        if (localizedMatch != null) return localizedMatch

        // 3. Try partial matches (e.g. "Monday Upper Body")
        val partialMatch = days.find { 
            it.day.isEnabled && (
                it.day.name.contains(today, ignoreCase = true) ||
                it.day.name.contains(localizedToday, ignoreCase = true)
            )
        }
        if (partialMatch != null) return partialMatch

        // 4. Fallback Logic:
        // Check if the program uses weekday names at all.
        val programUsesWeekdays = days.any { d ->
            WEEKDAYS.any { weekday -> d.day.name.contains(weekday, ignoreCase = true) }
        }

        return if (programUsesWeekdays) {
            // If it's a weekday-based program and no match was found for today,
            // then today is most likely a Rest Day.
            null
        } else {
            // If it's a non-weekday program (e.g. "Push/Pull/Legs"),
            // fallback to the first enabled day in the list.
            days.firstOrNull { it.day.isEnabled }
        }
    }
}
