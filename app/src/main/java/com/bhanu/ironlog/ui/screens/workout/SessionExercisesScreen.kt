package com.bhanu.ironlog.ui.screens.workout

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bhanu.ironlog.data.local.entity.ExerciseEntity
import com.bhanu.ironlog.data.local.pojo.SessionExerciseWithTemplate
import com.bhanu.ironlog.data.service.Achievement
import com.bhanu.ironlog.data.service.AchievementType
import com.bhanu.ironlog.ui.components.ErrorScreen
import com.bhanu.ironlog.ui.components.ExerciseSessionItem
import com.bhanu.ironlog.ui.components.WorkoutProgress
import com.bhanu.ironlog.ui.components.formatTimer
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionExercisesScreen(
    onBack: () -> Unit,
    onFinish: () -> Unit,
    onNavigateToLogging: (Long, Long) -> Unit,
    viewModel: SessionExercisesViewModel = hiltViewModel()
) {
    if (!viewModel.isArgumentValid) {
        ErrorScreen(onBack = onBack, message = "Invalid Session data")
        return
    }

    val context = LocalContext.current
    val session by viewModel.session.collectAsState()
    val exercises by viewModel.exercises.collectAsState()
    val timerSeconds by viewModel.timerSeconds.collectAsState()
    val progress by viewModel.progress.collectAsState()
    
    val achievements by viewModel.achievements.collectAsState()
    val showCelebration by viewModel.showCelebration.collectAsState()
    val showBackgroundDialog by viewModel.showBackgroundDialog.collectAsState()
    val finishSummary by viewModel.finishSummary.collectAsState()
    
    var showFinishConfirmation by remember { mutableStateOf(false) }
    var showDiscardConfirmation by remember { mutableStateOf(false) }

    // Intercept Back Navigation
    androidx.activity.compose.BackHandler {
        viewModel.onLeaveSession()
    }

    // Handle Finish Navigation
    LaunchedEffect(Unit) {
        viewModel.finishSignal.collect {
            Toast.makeText(context, "Workout Saved", Toast.LENGTH_SHORT).show()
            onFinish()
        }
    }

    // We can use a derived state to track if we've attempted to load and failed
    var loadingTimedOut by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(2000) // Reduced to 2s for better UX
        if (session == null) loadingTimedOut = true
    }

    if (loadingTimedOut && session == null) {
        ErrorScreen(onBack = onBack, message = "Workout Session not found")
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(session?.dayName ?: "Workout", style = MaterialTheme.typography.titleMedium)
                        Text(session?.programName ?: "", style = MaterialTheme.typography.bodySmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onLeaveSession() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Text(
                        text = formatTimer(timerSeconds),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDiscardConfirmation = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Discard")
                    }
                    Button(
                        onClick = { showFinishConfirmation = true },
                        modifier = Modifier.weight(2f)
                    ) {
                        Text("Finish Workout")
                    }
                }
            }
        }
    ) { padding ->
        when {
            session == null && !loadingTimedOut -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            session == null && loadingTimedOut -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Session not found", color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = onBack) { Text("Go Back") }
                    }
                }
            }
            exercises.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FitnessCenter, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.height(16.dp))
                        Text("No exercises in this session", color = MaterialTheme.colorScheme.outline)
                        Text("Add exercises to your program template first", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        progress?.let {
                            WorkoutProgress(
                                completedExercises = it.completedExercises,
                                totalExercises = it.totalExercises,
                                completedSets = it.completedSets,
                                totalSets = it.totalSets
                            )
                        }
                    }

                    items(exercises, key = { it.sessionExercise.sessionExerciseId }) { item ->
                    val isCompleted = item.sessionExercise.status == "COMPLETED"
                    ExerciseSessionItem(
                        exercise = item.template,
                        isCompleted = isCompleted,
                        onToggleComplete = { viewModel.toggleExerciseCompletion(item.template.id) },
                        onClick = { onNavigateToLogging(item.template.id, session?.sessionId ?: 0L) },
                        notes = item.sessionExercise.notes
                    )
                }
                }
            }
        }
    }

    if (showFinishConfirmation && finishSummary != null) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        AlertDialog(
            onDismissRequest = { showFinishConfirmation = false },
            title = { Text("Finish Workout?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ready to wrap up your session?")
                    HorizontalDivider(thickness = 0.5.dp)
                    
                    SummaryRowItem("Duration", formatTimer(finishSummary!!.durationSeconds))
                    SummaryRowItem("Completed", "${finishSummary!!.completedExercises} exercises")
                    SummaryRowItem("Total Sets", "${finishSummary!!.totalSets} sets")
                    SummaryRowItem("Total Volume", String.format(Locale.getDefault(), "%,.0f kg", finishSummary!!.totalVolume))
                    SummaryRowItem("Started", timeFormat.format(Date(finishSummary!!.startTime)))
                    SummaryRowItem("Finished", timeFormat.format(Date(finishSummary!!.endTime)))
                }
            },
            confirmButton = {
                Button(onClick = { 
                    viewModel.finishWorkout()
                    showFinishConfirmation = false
                }) {
                    Text("Finish")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDiscardConfirmation) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirmation = false },
            title = { Text("Discard Workout?") },
            text = { Text("Workout progress will be lost. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = { 
                        viewModel.discardWorkout()
                        showDiscardConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showBackgroundDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onBackgroundDialogConfirm(stay = true) },
            title = { Text("Workout continues in the background") },
            text = { Text("Your workout remains active until you finish or discard it.") },
            confirmButton = {
                TextButton(onClick = { viewModel.onBackgroundDialogConfirm(stay = false) }) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onBackgroundDialogConfirm(stay = true) }) {
                    Text("Stay in Workout")
                }
            }
        )
    }

    if (showCelebration) {
        PRCelebrationDialog(
            achievements = achievements,
            onDismiss = { viewModel.onCelebrationDismissed() }
        )
    }
}

@Composable
fun PRCelebrationDialog(
    achievements: List<Achievement>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                Text("New Personal Records!", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                achievements.forEach { achievement ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(achievement.exerciseName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            val typeLabel = if (achievement.type == AchievementType.WEIGHT_PR) "New Max Weight" else "New Est. 1RM"
                            Text(typeLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                        Text(
                            String.format(Locale.getDefault(), "%.1f kg", achievement.value),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Awesome!")
            }
        }
    )
}

@Composable
fun SummaryRowItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
