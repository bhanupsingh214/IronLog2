package com.bhanu.ironlog.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.bhanu.ironlog.ui.screens.dashboard.DashboardScreen
import com.bhanu.ironlog.ui.screens.progress.ProgressScreen
import com.bhanu.ironlog.ui.screens.profile.ProfileScreen
import com.bhanu.ironlog.ui.screens.history.HistoryScreen
import com.bhanu.ironlog.ui.screens.history.WorkoutDetailsScreen
import com.bhanu.ironlog.ui.screens.records.RecordDetailScreen
import com.bhanu.ironlog.ui.screens.records.RecordsScreen
import com.bhanu.ironlog.ui.screens.programs.ArchivedProgramsScreen
import com.bhanu.ironlog.ui.screens.programs.ExercisesScreen
import com.bhanu.ironlog.ui.screens.programs.ProgramsScreen
import com.bhanu.ironlog.ui.screens.programs.WorkoutDaysScreen
import com.bhanu.ironlog.ui.screens.programs.WorkoutLoggingScreen
import com.bhanu.ironlog.ui.screens.workout.SessionExercisesScreen
import com.bhanu.ironlog.ui.screens.workout.WorkoutScreen

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(route = Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToWorkout = { 
                    navController.navigate(Screen.Workout.route) 
                },
                onNavigateToCurrentProgram = { programId ->
                    navController.navigate(Screen.WorkoutDays.passProgramId(programId))
                },
                onNavigateToRecentSession = { _, sessionId ->
                    navController.navigate(Screen.WorkoutDetails.passSessionId(sessionId))
                },
                onNavigateToSessionExercises = { dayId, sessionId ->
                    navController.navigate(Screen.SessionExercises.passSession(dayId, sessionId))
                },
                onNavigateToRecords = {
                    navController.navigate(Screen.Records.route)
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                }
            )
        }
        composable(route = Screen.Workout.route) {
            WorkoutScreen(
                onBack = { navController.popBackStack() },
                onNavigateToSessionExercises = { dayId, sessionId ->
                    navController.navigate(Screen.SessionExercises.passSession(dayId, sessionId))
                }
            )
        }
        composable(
            route = Screen.SessionExercises.route,
            arguments = listOf(
                navArgument("dayId") { type = NavType.LongType },
                navArgument("sessionId") { type = NavType.LongType }
            )
        ) {
            SessionExercisesScreen(
                onBack = { navController.popBackStack() },
                onFinish = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                },
                onNavigateToLogging = { exerciseId, sessionId ->
                    navController.navigate(Screen.WorkoutLogging.passLogging(exerciseId, sessionId))
                },
                onNavigateToDetails = { sessionId ->
                    navController.navigate(Screen.WorkoutDetails.passSessionId(sessionId)) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = false
                        }
                    }
                }
            )
        }
        composable(route = Screen.Programs.route) {
            ProgramsScreen(
                onNavigateToArchive = { navController.navigate(Screen.ArchivedPrograms.route) },
                onNavigateToWorkoutDays = { programId ->
                    navController.navigate(Screen.WorkoutDays.passProgramId(programId))
                }
            )
        }
        composable(
            route = Screen.WorkoutDays.route,
            arguments = listOf(navArgument("programId") { type = NavType.LongType })
        ) {
            WorkoutDaysScreen(
                onBack = { navController.popBackStack() },
                onNavigateToExercises = { dayId ->
                    navController.navigate(Screen.Exercises.passDayId(dayId))
                }
            )
        }
        composable(
            route = Screen.Exercises.route,
            arguments = listOf(navArgument("dayId") { type = NavType.LongType })
        ) {
            ExercisesScreen(
                onBack = { navController.popBackStack() },
                onNavigateToLogging = { exerciseId ->
                    // This is for editing the Program Template
                    navController.navigate(Screen.WorkoutLogging.passLogging(exerciseId, 0))
                }
            )
        }
        composable(
            route = Screen.WorkoutLogging.route,
            arguments = listOf(
                navArgument("exerciseId") { type = NavType.LongType },
                navArgument("sessionId") { type = NavType.LongType }
            )
        ) {
            WorkoutLoggingScreen(
                onBack = { navController.popBackStack() },
                onNavigateToExercise = { exerciseId ->
                    val sessionId = it.arguments?.getLong("sessionId") ?: 0L
                    navController.navigate(Screen.WorkoutLogging.passLogging(exerciseId, sessionId)) {
                        // Replace current logging screen in backstack to prevent deep nesting
                        popUpTo(Screen.WorkoutLogging.route) { inclusive = true }
                    }
                }
            )
        }
        composable(route = Screen.ArchivedPrograms.route) {
            ArchivedProgramsScreen(onBack = { navController.popBackStack() })
        }
        composable(route = Screen.History.route) {
            HistoryScreen(
                onBack = { navController.popBackStack() },
                onNavigateToDetails = { sessionId ->
                    navController.navigate(Screen.WorkoutDetails.passSessionId(sessionId))
                }
            )
        }
        composable(route = Screen.Records.route) {
            RecordsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToDetail = { exerciseId ->
                    navController.navigate(Screen.RecordDetail.passExerciseId(exerciseId))
                }
            )
        }
        composable(
            route = Screen.RecordDetail.route,
            arguments = listOf(navArgument("exerciseId") { type = NavType.LongType })
        ) {
            RecordDetailScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.WorkoutDetails.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) {
            WorkoutDetailsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToExerciseDetails = { exerciseId ->
                    navController.navigate(Screen.ExerciseDetails.passExerciseId(exerciseId))
                }
            )
        }
        composable(
            route = Screen.ExerciseDetails.route,
            arguments = listOf(navArgument("exerciseId") { type = NavType.LongType })
        ) {
            com.bhanu.ironlog.ui.screens.workout.ExerciseDetailsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToSession = { sessionId ->
                    navController.navigate(Screen.WorkoutDetails.passSessionId(sessionId))
                }
            )
        }
        composable(route = Screen.Progress.route) {
            ProgressScreen(
                onNavigateToRecords = {
                    navController.navigate(Screen.Records.route)
                }
            )
        }
        composable(route = Screen.Profile.route) {
            ProfileScreen()
        }
    }
}
