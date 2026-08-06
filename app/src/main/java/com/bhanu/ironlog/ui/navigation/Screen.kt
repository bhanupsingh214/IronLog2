package com.bhanu.ironlog.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Programs : Screen("programs", "Programs", Icons.AutoMirrored.Filled.List)
    object Workout : Screen("workout_session_days", "Workout", Icons.Default.PlayArrow)
    object SessionExercises : Screen("session_exercises/{dayId}/{sessionId}", "Exercises", Icons.Default.FitnessCenter) {
        fun passSession(dayId: Long, sessionId: Long) = "session_exercises/$dayId/$sessionId"
    }
    object Progress : Screen("progress", "Progress", Icons.Default.BarChart)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object History : Screen("history", "History", Icons.Default.History)
    object Records : Screen("records", "Records", Icons.Default.EmojiEvents)
    object RecordDetail : Screen("record_detail/{exerciseId}", "Record Detail", Icons.Default.Info) {
        fun passExerciseId(id: Long) = "record_detail/$id"
    }
    object WorkoutDetails : Screen("workout_details/{sessionId}", "Details", Icons.Default.Info) {
        fun passSessionId(id: Long) = "workout_details/$id"
    }
    object ArchivedPrograms : Screen("archived_programs", "Archived", Icons.Default.Archive)
    object WorkoutDays : Screen("workout_days/{programId}", "Workout Days", Icons.Default.CalendarToday) {
        fun passProgramId(id: Long) = "workout_days/$id"
    }
    object Exercises : Screen("exercises/{dayId}", "Exercises", Icons.Default.FitnessCenter) {
        fun passDayId(id: Long) = "exercises/$id"
    }
    object WorkoutLogging : Screen("workout_logging/{exerciseId}/{sessionId}", "Logging", Icons.Default.Edit) {
        fun passLogging(exerciseId: Long, sessionId: Long) = "workout_logging/$exerciseId/$sessionId"
    }
    object ExerciseDetails : Screen("exercise_details/{exerciseId}", "Exercise Details", Icons.Default.Info) {
        fun passExerciseId(id: Long) = "exercise_details/$id"
    }
    object ExerciseLibrary : Screen("exercise_library", "Exercise Library", Icons.AutoMirrored.Filled.LibraryBooks)
}
