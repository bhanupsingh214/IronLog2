package com.bhanu.ironlog.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bhanu.ironlog.ui.navigation.Screen
import com.bhanu.ironlog.ui.navigation.SetupNavGraph

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val activeSession by viewModel.activeSession.collectAsState()

    val screens = listOf(
        Screen.Dashboard,
        Screen.Programs,
        Screen.Workout,
        Screen.Progress,
        Screen.Profile,
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val currentRoute = currentDestination?.route

                screens.forEach { screen ->
                    // Root vs Child identification for highlighting
                    val isSelected = when (screen) {
                        Screen.Dashboard -> currentRoute == Screen.Dashboard.route ||
                                         currentRoute == Screen.History.route ||
                                         currentRoute?.startsWith("workout_details") == true
                        Screen.Programs -> currentRoute == Screen.Programs.route ||
                                         currentRoute?.startsWith("workout_days") == true ||
                                         currentRoute?.startsWith("exercises") == true ||
                                         currentRoute?.startsWith("archived_programs") == true ||
                                         (currentRoute?.startsWith("workout_logging") == true && navBackStackEntry?.arguments?.getLong("sessionId") == 0L)
                        Screen.Workout -> currentRoute == Screen.Workout.route ||
                                        currentRoute?.startsWith("session_exercises") == true ||
                                        (currentRoute?.startsWith("workout_logging") == true && (navBackStackEntry?.arguments?.getLong("sessionId") ?: 0L) > 0L)
                        Screen.Progress -> currentRoute == Screen.Progress.route ||
                                         currentRoute?.startsWith("records") == true ||
                                         currentRoute?.startsWith("record_detail") == true
                        else -> currentRoute == screen.route
                    }

                    NavigationBarItem(
                        label = { Text(screen.title) },
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        selected = isSelected,
                        onClick = {
                            if (currentRoute == screen.route && screen != Screen.Workout) {
                                // Already at root, do nothing
                            } else {
                                val targetRoute = if (screen == Screen.Workout && activeSession != null) {
                                    Screen.SessionExercises.passSession(activeSession!!.workoutDayId, activeSession!!.sessionId)
                                } else {
                                    screen.route
                                }

                                navController.navigate(targetRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = false 
                                }
                                
                                if (isSelected) {
                                    navController.popBackStack(targetRoute, inclusive = false)
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxSize()
        ) {
            SetupNavGraph(navController = navController)
        }
    }
}
