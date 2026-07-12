package com.bhanu.ironlog.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            DashboardHeader()
        }

        item {
            QuickActions()
        }

        item {
            TodayWorkoutCard()
        }

        item {
            CurrentProgramCard()
        }

        item {
            WeeklyVolumeCard()
        }

        item {
            RecentHistoryCard()
        }

        item {
            PersonalRecordsCard()
        }
        
        // Debug info from earlier
        item {
            Text(
                text = "System Status: ${items.size} logs synced",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun DashboardHeader() {
    Column {
        Text(
            text = "Welcome back, Champ!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Monday, Oct 27", // Placeholder date
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun QuickActions() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { /* TODO */ },
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Start Workout")
        }
        OutlinedButton(
            onClick = { /* TODO */ },
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Default.History, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Add Log")
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            content()
        }
    }
}

@Composable
fun TodayWorkoutCard() {
    DashboardCard(title = "Today's Workout", icon = Icons.Default.FitnessCenter) {
        Text(
            text = "Push Day - Hypertrophy",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "6 exercises • Est. 75 mins",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CurrentProgramCard() {
    DashboardCard(title = "Current Program", icon = Icons.AutoMirrored.Filled.EventNote) {
        Text(
            text = "PPL Foundation (4-Day Split)",
            style = MaterialTheme.typography.bodyLarge
        )
        LinearProgressIndicator(
            progress = { 0.65f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        Text(
            text = "Week 6 of 12 • 65% Complete",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun WeeklyVolumeCard() {
    DashboardCard(title = "Weekly Volume", icon = Icons.Default.BarChart) {
        // Placeholder for a graph
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Volume Chart Placeholder",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
        Text(
            text = "Total Volume: 42,500 kg (+12% vs last week)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun RecentHistoryCard() {
    DashboardCard(title = "Recent History", icon = Icons.Default.History) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(2) { index ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (index == 0) "Yesterday: Pull Day" else "Saturday: Leg Day",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "View Details",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
fun PersonalRecordsCard() {
    DashboardCard(title = "Personal Records", icon = Icons.Default.EmojiEvents) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(3) { index ->
                SuggestionChip(
                    onClick = { },
                    label = {
                        Text(
                            text = when(index) {
                                0 -> "Deadlift: 180kg"
                                1 -> "Bench: 110kg"
                                else -> "Squat: 145kg"
                            }
                        )
                    }
                )
            }
        }
    }
}
