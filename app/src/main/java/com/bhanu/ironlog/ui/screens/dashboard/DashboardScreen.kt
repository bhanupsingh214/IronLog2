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
import kotlinx.coroutines.delay
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
    onNavigateToRecords: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val activeProgram by viewModel.activeProgram.collectAsState()
    val todayWorkout by viewModel.todayWorkout.collectAsState()
    val recentHistory by viewModel.recentHistory.collectAsState()
    val weeklyVolume by viewModel.weeklyVolume.collectAsState()
    val personalRecords by viewModel.personalRecords.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val currentExerciseName by viewModel.currentExerciseName.collectAsState()
    
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
            if (activeSession != null) {
                ActiveWorkoutCard(
                    session = activeSession!!,
                    currentExerciseName = currentExerciseName,
                    onResume = {
                        onNavigateToSessionExercises(activeSession!!.workoutDayId, activeSession!!.sessionId)
                    }
                )
            } else {
                TodayWorkoutCard(
                    todayWorkout = todayWorkout,
                    onClick = {
                        viewModel.onStartWorkout()
                    }
                )
            }
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
                },
                onViewAllClick = onNavigateToHistory
            )
        }

        item {
            PersonalRecordsCard(
                records = personalRecords,
                onClick = onNavigateToRecords
            )
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
    val dateFormat = SimpleDateFormat("EEEE, dd/MM/yyyy", Locale("en", "IN"))
    
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
    onHeaderClick: (() -> Unit)? = null,
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
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                if (onHeaderClick != null) {
                    TextButton(onClick = onHeaderClick) {
                        Text("See History")
                    }
                }
            }
            content()
        }
    }
}

@Composable
fun ActiveWorkoutCard(
    session: WorkoutSession,
    currentExerciseName: String?,
    onResume: () -> Unit
) {
    val startedTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    var duration by remember { mutableLongStateOf((System.currentTimeMillis() - session.startTime) / 1000) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            duration = (System.currentTimeMillis() - session.startTime) / 1000
        }
    }

    DashboardCard(
        title = "Active Workout", 
        icon = Icons.Default.PlayCircle,
        modifier = Modifier.clickable { onResume() }
    ) {
        Column {
            Text(
                text = session.dayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${session.programName} • Started ${startedTimeFormat.format(Date(session.startTime))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            
            Spacer(Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Duration", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = com.bhanu.ironlog.ui.components.formatTimer(duration),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Completed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    val completedCount = session.completedExerciseIds.split(",").filter { it.isNotBlank() }.size
                    Text(
                        text = "$completedCount exercises",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (currentExerciseName != null) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(thickness = 0.5.dp)
                Spacer(Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Current Exercise", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text(currentExerciseName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    if (session.currentSetNumber != null) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Current Set", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text("Set ${session.currentSetNumber}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Button(
                onClick = onResume,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.PlayArrow, null)
                Spacer(Modifier.width(8.dp))
                Text("Resume Workout")
            }
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
    onViewClick: (Long, Long) -> Unit,
    onViewAllClick: () -> Unit
) {
    DashboardCard(
        title = "Recent History", 
        icon = Icons.Default.History,
        onHeaderClick = onViewAllClick
    ) {
        if (history.isEmpty()) {
            Text(
                text = "No workouts logged yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("en", "IN"))
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
fun PersonalRecordsCard(
    records: List<Pair<String, Double>>,
    onClick: () -> Unit
) {
    DashboardCard(
        title = "Personal Records", 
        icon = Icons.Default.EmojiEvents,
        modifier = Modifier.clickable { onClick() }
    ) {
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
            DatePicker(
                state = datePickerState,
                dateFormatter = remember { 
                    DatePickerDefaults.dateFormatter(
                        selectedDateSkeleton = "ddMMyyyy"
                    ) 
                }
            )
        }
    }
}
