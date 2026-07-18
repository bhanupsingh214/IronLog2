package com.bhanu.ironlog.ui.screens.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bhanu.ironlog.data.local.entity.WorkoutSession
import com.bhanu.ironlog.data.local.pojo.ProgramWithStats
import com.bhanu.ironlog.data.local.pojo.WorkoutDayWithStats
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    onNavigateToWorkout: () -> Unit,
    onNavigateToCurrentProgram: (Long) -> Unit,
    onNavigateToRecentSession: (Long, Long) -> Unit,
    onNavigateToSessionExercises: (Long, Long) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val activeProgram by viewModel.activeProgram.collectAsState()
    val todayWorkout by viewModel.todayWorkout.collectAsState()
    val recentHistory by viewModel.recentHistory.collectAsState()
    val weeklyVolume by viewModel.weeklyVolume.collectAsState()
    val personalRecords by viewModel.personalRecords.collectAsState()
    
    var showAddLogDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        launch {
            viewModel.navigateToWorkout.collectLatest {
                onNavigateToWorkout()
            }
        }
        launch {
            viewModel.navigateToSession.collectLatest { pair ->
                if (pair.first != 0L && pair.second != 0L) {
                    showAddLogDialog = false
                    onNavigateToSessionExercises(pair.first, pair.second)
                    viewModel.onNavigationHandled()
                }
            }
        }
    }

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
            QuickActions(
                onStartWorkout = { viewModel.onStartWorkout() },
                onAddLog = { showAddLogDialog = true },
                enabled = activeProgram != null
            )
        }

        item {
            TodayWorkoutCard(
                todayWorkout = todayWorkout,
                onClick = {
                    viewModel.onStartWorkout()
                }
            )
        }

        item {
            CurrentProgramCard(
                activeProgram = activeProgram,
                onClick = { programId ->
                    onNavigateToCurrentProgram(programId)
                }
            )
        }

        item {
            WeeklyVolumeCard(weeklyVolume)
        }

        item {
            RecentHistoryCard(
                history = recentHistory,
                onViewClick = { dayId, sessionId ->
                    onNavigateToRecentSession(dayId, sessionId)
                }
            )
        }

        item {
            PersonalRecordsCard(personalRecords)
        }
    }

    if (showAddLogDialog) {
        AddLogDialog(
            onDismiss = { showAddLogDialog = false },
            activeProgram = activeProgram,
            viewModel = viewModel
        )
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
fun QuickActions(
    onStartWorkout: () -> Unit,
    onAddLog: () -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onStartWorkout,
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.medium,
            enabled = enabled
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Start Workout")
        }
        OutlinedButton(
            onClick = onAddLog,
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.medium,
            enabled = enabled
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
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
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
fun TodayWorkoutCard(todayWorkout: WorkoutDayWithStats?, onClick: () -> Unit) {
    DashboardCard(
        title = "Today's Workout", 
        icon = Icons.Default.FitnessCenter,
        modifier = Modifier.clickable(enabled = todayWorkout != null) { onClick() }
    ) {
        if (todayWorkout != null) {
            Text(
                text = todayWorkout.day.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${todayWorkout.exerciseCount} exercises • Est. ${todayWorkout.day.estimatedDurationMinutes} mins",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = "No workout scheduled",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun CurrentProgramCard(activeProgram: ProgramWithStats?, onClick: (Long) -> Unit) {
    DashboardCard(
        title = "Current Program", 
        icon = Icons.AutoMirrored.Filled.EventNote,
        modifier = Modifier.clickable(enabled = activeProgram != null) { 
            activeProgram?.let { onClick(it.program.id) }
        }
    ) {
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
fun WeeklyVolumeCard(volume: Double) {
    DashboardCard(title = "Weekly Volume", icon = Icons.Default.BarChart) {
        Text(
            text = String.format(Locale.getDefault(), "%.1f kg", volume),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Total volume logged this week",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun RecentHistoryCard(
    history: List<WorkoutSession>,
    onViewClick: (Long, Long) -> Unit
) {
    DashboardCard(title = "Recent History", icon = Icons.Default.History) {
        if (history.isEmpty()) {
            Text(
                text = "No workouts logged yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                history.forEach { session ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onViewClick(session.workoutDayId, session.sessionId) },
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = session.dayName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = dateFormat.format(Date(session.createdAt)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Text(
                            text = "${session.durationSeconds / 60}m",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PersonalRecordsCard(records: List<Pair<String, Double>>) {
    DashboardCard(title = "Personal Records", icon = Icons.Default.EmojiEvents) {
        if (records.isEmpty()) {
            Text(
                text = "No PRs logged yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(records.size) { index ->
                    val (name, weight) = records[index]
                    SuggestionChip(
                        onClick = { },
                        label = {
                            Text(text = "$name: ${weight}kg")
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLogDialog(
    onDismiss: () -> Unit,
    activeProgram: ProgramWithStats?,
    viewModel: DashboardViewModel
) {
    val workoutDays by viewModel.activeProgramDays.collectAsState()
    var selectedDayId by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (!showDatePicker) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Select Workout Day") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pick a day from ${activeProgram?.program?.name}")
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(workoutDays.size) { index ->
                            val item = workoutDays[index]
                            OutlinedButton(
                                onClick = {
                                    selectedDayId = item.day.id
                                    showDatePicker = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(item.day.name)
                            }
                        }
                    }
                }
            },
            confirmButton = { },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        )
    } else {
        val datePickerState = rememberDatePickerState(
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= System.currentTimeMillis()
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val date = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                        selectedDayId?.let { dayId ->
                            viewModel.onAddHistoricalSession(dayId, date)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Back")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
