package com.bhanu.ironlog.ui.screens.workout

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bhanu.ironlog.data.local.entity.ExerciseEntity
import com.bhanu.ironlog.data.local.pojo.SessionExerciseWithTemplate
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
    onNavigateToDetails: (Long) -> Unit,
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
    
    val showBackgroundDialog by viewModel.showBackgroundDialog.collectAsState()
    
    val completionState by viewModel.completionState.collectAsState()
    val completionSummary by viewModel.completionSummary.collectAsState()
    
    var showDiscardConfirmation by remember { mutableStateOf(false) }

    // Intercept Back Navigation
    androidx.activity.compose.BackHandler {
        if (completionState == WorkoutCompletionState.ACTIVE) {
            viewModel.onLeaveSession()
        } else if (completionState == WorkoutCompletionState.COMPLETED) {
            viewModel.dismissSummary()
        }
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

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = completionState,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "WorkoutCompletionTransition"
        ) { state ->
            if (state == WorkoutCompletionState.COMPLETED && completionSummary != null) {
                WorkoutCompleteScreen(
                    summary = completionSummary!!,
                    onDone = { viewModel.dismissSummary() },
                    onViewDetails = { onNavigateToDetails(completionSummary!!.sessionId) }
                )
            } else {
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
                                    onClick = { viewModel.initiateFinish() },
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
                                        exerciseName = item.sessionExercise.exerciseName.ifBlank { item.template?.name ?: "Deleted Exercise" },
                                        muscleGroup = item.sessionExercise.muscleGroup.ifBlank { item.template?.muscleGroup ?: "" },
                                        exerciseType = item.template?.exerciseType ?: "Exercise",
                                        isCompleted = isCompleted,
                                        onToggleComplete = { viewModel.toggleExerciseCompletion(item.sessionExercise.exerciseTemplateId) },
                                        onClick = { onNavigateToLogging(item.sessionExercise.exerciseTemplateId, session?.sessionId ?: 0L) },
                                        notes = item.sessionExercise.notes
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (completionState == WorkoutCompletionState.CONFIRMING_FINISH && completionSummary != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelFinish() },
            title = { Text("Finish Workout?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(completionSummary!!.workoutName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    HorizontalDivider(thickness = 0.5.dp)
                    SummaryRowItem("Exercises", "${completionSummary!!.exercisesCompleted}")
                    SummaryRowItem("Sets", "${completionSummary!!.setsCompleted}")
                    SummaryRowItem("Duration", formatTimer(completionSummary!!.durationSeconds))
                    SummaryRowItem("Volume", String.format(Locale.getDefault(), "%,.0f kg", completionSummary!!.totalVolume))
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.finishWorkout() }) {
                    Text("Finish Workout")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelFinish() }) {
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutCompleteScreen(
    summary: com.bhanu.ironlog.data.local.pojo.WorkoutCompletionSummary,
    onDone: () -> Unit,
    onViewDetails: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Summary") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = "Workout Complete!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = summary.workoutName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(Modifier.height(32.dp))
            
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        StatItem(
                            label = "Duration",
                            value = formatTimer(summary.durationSeconds),
                            modifier = Modifier.weight(1f)
                        )
                        StatItem(
                            label = "Volume",
                            value = String.format(Locale.getDefault(), "%,.0f kg", summary.totalVolume),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    HorizontalDivider(thickness = 0.5.dp)
                    
                    Row(modifier = Modifier.fillMaxWidth()) {
                        StatItem(
                            label = "Duration",
                            value = formatTimer(summary.durationSeconds),
                            modifier = Modifier.weight(1f)
                        )
                        StatItem(
                            label = "Volume",
                            value = String.format(Locale.getDefault(), "%,.0f kg", summary.totalVolume),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    HorizontalDivider(thickness = 0.5.dp)
                    
                    Row(modifier = Modifier.fillMaxWidth()) {
                        StatItem(
                            label = "Exercises",
                            value = "${summary.exercisesCompleted}/${summary.totalExercises}",
                            modifier = Modifier.weight(1f)
                        )
                        StatItem(
                            label = "Sets",
                            value = "${summary.setsCompleted}/${summary.totalSets}",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(thickness = 0.5.dp)

                    Row(modifier = Modifier.fillMaxWidth()) {
                        StatItem(
                            label = "Avg. RPE",
                            value = summary.averageRPE?.let { String.format(Locale.getDefault(), "%.1f", it) } ?: "-",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (summary.skippedExercises > 0) {
                        Text(
                            text = "${summary.skippedExercises} exercises skipped",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Completion",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        LinearProgressIndicator(
                            progress = { summary.completionPercentage },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }

                    if (summary.achievements.isNotEmpty()) {
                        HorizontalDivider(thickness = 0.5.dp)
                        PRAchievementsSection(summary.achievements)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            summary.comparison?.let { comp ->
                WorkoutComparisonSection(comp, summary)
            }
            
            Spacer(Modifier.height(48.dp))
            
            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Text("Done")
            }
            
            TextButton(
                onClick = onViewDetails,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Workout Details")
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
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

@Composable
fun PRAchievementsSection(achievements: List<com.bhanu.ironlog.data.local.pojo.PersonalRecordAchievement>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.EmojiEvents, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Personal Records Achieved", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(12.dp))
        
        val grouped = achievements.groupBy { it.exerciseName }
        grouped.forEach { (exercise, exerciseAchievements) ->
            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                Text(text = exercise, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                exerciseAchievements.forEach { pr ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = pr.type.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        Text(
                            text = "${formatPRValue(pr.previousValue)} → ${formatPRValue(pr.newValue)} ${pr.unit}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

private fun formatPRValue(value: Double): String {
    return if (value % 1 == 0.0) value.toInt().toString() else String.format(Locale.getDefault(), "%.1f", value)
}

@Composable
fun WorkoutComparisonSection(
    comparison: com.bhanu.ironlog.data.local.pojo.WorkoutComparison,
    currentSummary: com.bhanu.ironlog.data.local.pojo.WorkoutCompletionSummary
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Performance Comparison",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        if (comparison.isFirstSession) {
            Text(
                text = "This is your first completed workout for this routine. Future sessions will be compared here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        } else {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    comparison.previousVolume?.let { prevVolume ->
                        ComparisonRow(
                            label = "Total Volume",
                            current = currentSummary.totalVolume,
                            previous = prevVolume,
                            formatter = { String.format(Locale.getDefault(), "%,.0f kg", it) }
                        )
                    }
                    comparison.previousDurationSeconds?.let { prevDuration ->
                        ComparisonRow(
                            label = "Duration",
                            current = currentSummary.durationSeconds.toDouble(),
                            previous = prevDuration.toDouble(),
                            formatter = { formatTimer(it.toLong()) },
                            isLowerBetter = true
                        )
                    }
                    comparison.previousSetsCompleted?.let { prevSets ->
                        ComparisonRow(
                            label = "Completed Sets",
                            current = currentSummary.setsCompleted.toDouble(),
                            previous = prevSets.toDouble(),
                            formatter = { it.toInt().toString() }
                        )
                    }
                    if (currentSummary.averageRPE != null && comparison.previousAverageRPE != null) {
                        ComparisonRow(
                            label = "Average RPE",
                            current = currentSummary.averageRPE,
                            previous = comparison.previousAverageRPE,
                            formatter = { String.format(Locale.getDefault(), "%.1f", it) },
                            isLowerBetter = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ComparisonRow(
    label: String,
    current: Double,
    previous: Double,
    formatter: (Double) -> String,
    isLowerBetter: Boolean = false
) {
    val diff = current - previous
    val isPositive = diff > 0
    val color = if (isPositive) {
        if (isLowerBetter) MaterialTheme.colorScheme.error else Color(0xFF4CAF50)
    } else if (diff < 0) {
        if (isLowerBetter) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.outline
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = formatter(current), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            if (diff != 0.0) {
                Spacer(Modifier.width(8.dp))
                val icon = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
