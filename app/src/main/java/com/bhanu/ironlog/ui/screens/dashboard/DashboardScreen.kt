package com.bhanu.ironlog.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import com.bhanu.ironlog.data.local.pojo.ProgramWithStats
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val activeProgram by viewModel.activeProgram.collectAsState()

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
            CurrentProgramCard(activeProgram)
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
    }
}

@Composable
fun DashboardHeader() {
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
    
    Column {
        Text(
            text = "Welcome back, Champ!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = dateFormat.format(calendar.time),
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
fun CurrentProgramCard(activeProgram: ProgramWithStats?) {
    DashboardCard(title = "Current Program", icon = Icons.AutoMirrored.Filled.EventNote) {
        if (activeProgram != null) {
            Text(
                text = activeProgram.program.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${activeProgram.dayCount} days • ${activeProgram.exerciseCount} exercises",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = "No active program",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun WeeklyVolumeCard() {
    DashboardCard(title = "Weekly Volume", icon = Icons.Default.BarChart) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Chart data will appear here",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
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
                        text = "View",
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
