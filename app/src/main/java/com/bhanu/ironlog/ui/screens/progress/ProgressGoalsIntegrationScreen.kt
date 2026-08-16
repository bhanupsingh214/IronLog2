package com.bhanu.ironlog.ui.screens.progress

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bhanu.ironlog.data.model.goals.GoalProgress
import com.bhanu.ironlog.data.model.goals.GoalStatus
import com.bhanu.ironlog.data.model.goals.GoalTrend
import com.bhanu.ironlog.data.model.goals.GoalType
import com.bhanu.ironlog.ui.screens.goals.GoalViewModel

@Composable
fun ProgressGoalsIntegrationScreen(
    onNavigateToRecords: () -> Unit,
    onNavigateToGoals: () -> Unit,
    viewModel: GoalViewModel = hiltViewModel()
) {
    val goals by viewModel.goals.collectAsState()
    val activeGoals = goals.filter { it.status != GoalStatus.COMPLETED }

    ProgressScreen(
        onNavigateToRecords = onNavigateToRecords,
        goalsContent = if (activeGoals.isNotEmpty()) {
            {
                ActiveGoalsSection(
                    activeGoals = activeGoals,
                    onNavigateToGoals = onNavigateToGoals
                )
            }
        } else null
    )
}

@Composable
private fun ActiveGoalsSection(
    activeGoals: List<GoalProgress>,
    onNavigateToGoals: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Active Goals", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onNavigateToGoals) { Text("Manage") }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(activeGoals.take(3), key = { it.goal.goalId }) { progress ->
                    GoalStatusCard(progress)
                }
            }
        }
    }
}

@Composable
private fun GoalStatusCard(progress: GoalProgress) {
    val type = GoalType.entries.firstOrNull { it.key == progress.goal.type }
    Card(modifier = Modifier.width(180.dp)) {
        Column(Modifier.padding(10.dp)) {
            Text(type?.label ?: progress.goal.type, style = MaterialTheme.typography.labelLarge)
            progress.progress?.let {
                Text("${formatPercent(it)}%", style = MaterialTheme.typography.titleMedium)
                LinearProgressIndicator(progress = { it.toFloat() }, modifier = Modifier.fillMaxWidth())
            } ?: Text("Insufficient data", style = MaterialTheme.typography.bodySmall)
            val trend = when (progress.trend) {
                GoalTrend.IMPROVING -> "Improving"
                GoalTrend.STABLE -> "Stable"
                GoalTrend.MOVING_AWAY -> "Moving away"
                GoalTrend.INSUFFICIENT_DATA -> "Insufficient data"
                null -> null
            }
            trend?.let { Text(it, style = MaterialTheme.typography.labelSmall) }
            progress.status?.let { Text(statusLabel(it), style = MaterialTheme.typography.labelSmall) }
        }
    }
}

private fun statusLabel(status: GoalStatus): String = when (status) {
    GoalStatus.ON_TRACK -> "On Track"
    GoalStatus.BEHIND -> "Behind"
    GoalStatus.OVERDUE -> "Overdue"
    GoalStatus.COMPLETED -> "Completed"
}

private fun formatPercent(value: Double): String = String.format("%.0f", value * 100.0)
