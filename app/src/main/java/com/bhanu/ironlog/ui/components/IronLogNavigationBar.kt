package com.bhanu.ironlog.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.bhanu.ironlog.ui.navigation.Screen
import com.bhanu.ironlog.ui.theme.IronLogPrimary
import com.bhanu.ironlog.ui.theme.IronLogPrimaryLight
import com.bhanu.ironlog.ui.theme.IronLogSurface1
import com.bhanu.ironlog.ui.theme.IronLogTextSecondary

private val RootDestinations = listOf(
    Screen.Dashboard,
    Screen.Programs,
    Screen.Progress,
    Screen.Goals,
    Screen.Profile
)

@Composable
fun IronLogNavigationBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = modifier,
        containerColor = IronLogSurface1,
        tonalElevation = 0.dp
    ) {
        RootDestinations.forEach { screen ->
            val selected = when (screen) {
                Screen.Dashboard -> currentRoute == Screen.Dashboard.route ||
                    currentRoute == Screen.History.route ||
                    currentRoute?.startsWith("workout_details") == true

                Screen.Programs -> currentRoute == Screen.Programs.route ||
                    currentRoute?.startsWith("workout_days") == true ||
                    currentRoute?.startsWith("exercises") == true ||
                    currentRoute?.startsWith("archived_programs") == true ||
                    currentRoute?.startsWith("exercise_library") == true ||
                    (currentRoute?.startsWith("workout_logging") == true &&
                        (navBackStackEntry?.arguments?.getLong("sessionId") ?: 0L) == 0L)

                Screen.Progress -> currentRoute == Screen.Progress.route ||
                    currentRoute?.startsWith("records") == true ||
                    currentRoute?.startsWith("record_detail") == true

                Screen.Goals -> currentRoute == Screen.Goals.route
                Screen.Profile -> currentRoute == Screen.Profile.route
                else -> false
            }

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected || currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title
                    )
                },
                label = { Text(screen.title) },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = IronLogPrimaryLight,
                    selectedTextColor = IronLogPrimaryLight,
                    indicatorColor = IronLogPrimary.copy(alpha = 0.18f),
                    unselectedIconColor = IronLogTextSecondary,
                    unselectedTextColor = IronLogTextSecondary
                )
            )
        }
    }
}
