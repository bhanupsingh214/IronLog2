package com.bhanu.ironlog.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.bhanu.ironlog.ui.screens.dashboard.DashboardScreen
import com.bhanu.ironlog.ui.screens.insights.InsightsScreen
import com.bhanu.ironlog.ui.screens.profile.ProfileScreen
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
                onNavigateToRecentSession = { dayId, sessionId ->
                    navController.navigate(Screen.SessionExercises.passSession(dayId, sessionId))
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
                onNavigateToLogging = { exerciseId, sessionId ->
                    navController.navigate(Screen.WorkoutLogging.passLogging(exerciseId, sessionId))
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
            WorkoutLoggingScreen(onBack = { navController.popBackStack() })
        }
        composable(route = Screen.ArchivedPrograms.route) {
            ArchivedProgramsScreen(onBack = { navController.popBackStack() })
        }
        composable(route = Screen.Insights.route) {
            InsightsScreen()
        }
        composable(route = Screen.Profile.route) {
            ProfileScreen()
        }
    }
}
