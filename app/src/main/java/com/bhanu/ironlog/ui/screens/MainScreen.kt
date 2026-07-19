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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bhanu.ironlog.ui.navigation.Screen
import com.bhanu.ironlog.ui.navigation.SetupNavGraph

@Composable
fun MainScreen() {
    val navController = rememberNavController()
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
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    // on the back stack as users select items
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination when
                                    // reselecting the same item
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    // Rule: "Dashboard must NEVER redirect back into Workout History"
                                    // and "ALWAYS navigate directly to the selected ROOT"
                                    // We'll disable restoreState to ensure we always hit the root
                                    restoreState = false 
                                }
                            } else {
                                // Already at root, but check if we are on a child screen of this root
                                // If so, pop back to the root
                                navController.popBackStack(screen.route, inclusive = false)
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
