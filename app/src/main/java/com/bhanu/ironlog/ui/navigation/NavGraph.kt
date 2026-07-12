package com.bhanu.ironlog.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bhanu.ironlog.ui.screens.dashboard.DashboardScreen
import com.bhanu.ironlog.ui.screens.insights.InsightsScreen
import com.bhanu.ironlog.ui.screens.profile.ProfileScreen
import com.bhanu.ironlog.ui.screens.programs.ProgramsScreen
import com.bhanu.ironlog.ui.screens.workout.WorkoutScreen

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(route = Screen.Dashboard.route) {
            DashboardScreen()
        }
        composable(route = Screen.Programs.route) {
            ProgramsScreen()
        }
        composable(route = Screen.Workout.route) {
            WorkoutScreen()
        }
        composable(route = Screen.Insights.route) {
            InsightsScreen()
        }
        composable(route = Screen.Profile.route) {
            ProfileScreen()
        }
    }
}
