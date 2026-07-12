package com.bhanu.ironlog.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Programs : Screen("programs", "Programs", Icons.AutoMirrored.Filled.List)
    object Workout : Screen("workout", "Workout", Icons.Default.PlayArrow)
    object Insights : Screen("insights", "Insights", Icons.Default.Info)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}
