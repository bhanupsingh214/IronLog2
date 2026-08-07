package com.bhanu.ironlog.ui.screens.workout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bhanu.ironlog.data.local.entity.ExerciseEntity
import com.bhanu.ironlog.data.local.entity.WorkoutDayEntity
import com.bhanu.ironlog.data.local.pojo.WorkoutDayWithStats
import com.bhanu.ironlog.ui.components.formatTimer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    onBack: () -> Unit,
    onNavigateToSessionExercises: (Long, Long) -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val activeProgram by viewModel.activeProgram.collectAsState()
    val workoutDays by viewModel.workoutDays.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()

    var dayToStart by remember { mutableStateOf<WorkoutDayEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Workout Session", style = MaterialTheme.typography.titleMedium)
                        Text(activeProgram?.program?.name ?: "Active Program", style = MaterialTheme.typography.bodySmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (workoutDays.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No workout days in active program", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(workoutDays, key = { it.day.id }) { item ->
                    WorkoutDayItemForSession(
                        item = item,
                        onClick = { 
                            if (activeSession != null) {
                                dayToStart = item.day
                            } else {
                                viewModel.startSession(item.day) { sessionId ->
                                    onNavigateToSessionExercises(item.day.id, sessionId)
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    if (dayToStart != null && activeSession != null) {
        AlertDialog(
            onDismissRequest = { dayToStart = null },
            title = { Text("Active Workout") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(activeSession!!.dayName, fontWeight = FontWeight.Bold)
                    Text("Started: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(activeSession!!.startTime))}")
                    val elapsed = (System.currentTimeMillis() - activeSession!!.startTime) / 1000
                    Text("Elapsed: ${formatTimer(elapsed)}")
                    Spacer(Modifier.height(8.dp))
                    Text("Starting a new workout will discard your current progress.")
                }
            },
            confirmButton = {
                Button(onClick = {
                    onNavigateToSessionExercises(activeSession!!.workoutDayId, activeSession!!.sessionId)
                    dayToStart = null
                }) {
                    Text("Resume")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        viewModel.discardSession(activeSession!!.sessionId)
                        viewModel.startSession(dayToStart!!) { sessionId ->
                            onNavigateToSessionExercises(dayToStart!!.id, sessionId)
                        }
                        dayToStart = null
                    }) {
                        Text("Discard", color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(onClick = { dayToStart = null }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

@Composable
fun WorkoutDayItemForSession(
    item: WorkoutDayWithStats,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.day.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${item.exerciseCount} Exercises",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}
