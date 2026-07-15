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
        Screen.Insights,
        Screen.Profile,
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val currentRoute = currentDestination?.route
                val sessionId = navBackStackEntry?.arguments?.getLong("sessionId") ?: -1L

                screens.forEach { screen ->
                    NavigationBarItem(
                        label = { Text(screen.title) },
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        selected = when (screen) {
                            Screen.Dashboard -> currentRoute == Screen.Dashboard.route
                            Screen.Programs -> currentRoute == Screen.Programs.route || 
                                             currentRoute?.startsWith("workout_days") == true || 
                                             currentRoute?.startsWith("exercises") == true ||
                                             (currentRoute?.startsWith("workout_logging") == true && sessionId == 0L)
                            Screen.Workout -> currentRoute == Screen.Workout.route || 
                                            currentRoute?.startsWith("session_exercises") == true ||
                                            (currentRoute?.startsWith("workout_logging") == true && sessionId > 0L)
                            else -> currentRoute == screen.route
                        },
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
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
