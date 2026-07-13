package com.bhanu.ironlog.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Programs : Screen("programs", "Programs", Icons.AutoMirrored.Filled.List)
    object Workout : Screen("workout", "Workout", Icons.Default.PlayArrow)
    object Insights : Screen("insights", "Insights", Icons.Default.Info)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object ArchivedPrograms : Screen("archived_programs", "Archived", Icons.Default.Archive)
    object WorkoutDays : Screen("workout_days/{programId}", "Workout Days", Icons.Default.CalendarToday) {
        fun passProgramId(id: Long) = "workout_days/$id"
    }
    object Exercises : Screen("exercises/{dayId}", "Exercises", Icons.Default.FitnessCenter) {
        fun passDayId(id: Long) = "exercises/$id"
    }
}
