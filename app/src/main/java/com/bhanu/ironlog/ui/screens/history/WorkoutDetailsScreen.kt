package com.bhanu.ironlog.ui.screens.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bhanu.ironlog.data.local.pojo.WorkoutDetails
import com.bhanu.ironlog.ui.components.ErrorScreen
import com.bhanu.ironlog.ui.components.formatTimer
import com.bhanu.ironlog.ui.components.formatWorkoutDate
import com.bhanu.ironlog.ui.components.formatWorkoutTime
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailsScreen(
    onBack: () -> Unit,
    onNavigateToExerciseDetails: (Long) -> Unit,
    viewModel: WorkoutDetailsViewModel = hiltViewModel()
) {
    if (!viewModel.isArgumentValid) {
        ErrorScreen(onBack = onBack, message = "Invalid Session data")
        return
    }

    val workoutDetails by viewModel.workoutDetails.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (workoutDetails == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val details = workoutDetails!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    WorkoutDetailsHeader(details)
                }

                item {
                    WorkoutDetailsSummaryCard(details)
                }

                item {
                    Text(
                        text = "Exercises",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(details.exercises, key = { it.sessionExercise.sessionExerciseId }) { exercise ->
                    HistoricalExerciseItem(
                        item = exercise,
                        onClick = { onNavigateToExerciseDetails(exercise.sessionExercise.libraryExerciseId) }
                    )
                }
                
                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun WorkoutDetailsHeader(details: WorkoutDetails) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = details.session.dayName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = details.session.programName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.size(4.dp)
            ) {}
            Spacer(Modifier.width(8.dp))
            Text(
                text = details.session.dayName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            DetailStat("Date", formatWorkoutDate(details.session.createdAt))
            DetailStat("Duration", formatTimer(details.session.durationSeconds))
        }
        
        Spacer(Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            DetailStat("Start Time", formatWorkoutTime(details.session.startTime))
            details.session.endTime?.let {
                DetailStat("End Time", formatWorkoutTime(it))
            }
        }
    }
}

@Composable
fun DetailStat(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun WorkoutDetailsSummaryCard(details: WorkoutDetails) {
    val summary = details.summary
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryStat(
                    label = "Exercises",
                    value = "${summary.exercisesCompleted}/${summary.totalExercises}",
                    modifier = Modifier.weight(1f)
                )
                SummaryStat(
                    label = "Sets",
                    value = "${summary.setsCompleted}/${summary.totalSets}",
                    modifier = Modifier.weight(1f)
                )
            }
            
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryStat(
                    label = "Volume",
                    value = String.format(Locale.getDefault(), "%,.0f kg", summary.totalVolume),
                    modifier = Modifier.weight(1f)
                )
                SummaryStat(
                    label = "Completion",
                    value = "${(summary.completionPercentage * 100).toInt()}%",
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
        }
    }
}

@Composable
fun SummaryStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HistoricalExerciseItem(
    item: com.bhanu.ironlog.data.local.pojo.SessionExerciseWithTemplateAndSets,
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val exercise = item.sessionExercise
    
    val displayName = exercise.exerciseName.ifBlank { item.template?.name ?: "Deleted Exercise" }
    val displayMuscle = exercise.muscleGroup.ifBlank { item.template?.muscleGroup ?: "" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = if (exercise.status == "SKIPPED") 
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (exercise.status == "SKIPPED") null 
                 else CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (displayMuscle.isNotBlank()) "$displayMuscle • ${exercise.status}" else exercise.status,
                        style = MaterialTheme.typography.bodySmall,
                        color = when (exercise.status) {
                            "COMPLETED" -> MaterialTheme.colorScheme.primary
                            "SKIPPED" -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.outline
                        }
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.History, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Exercise History", style = MaterialTheme.typography.labelMedium)
                }
            }
            
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (exercise.notes.isNotBlank()) {
                        Text(
                            text = "Notes: ${exercise.notes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    item.sets.forEach { set ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Set ${set.setNumber}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.width(60.dp)
                            )
                            
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                SetDataPoint(label = "kg", value = String.format(Locale.getDefault(), "%.1f", set.weight))
                                SetDataPoint(label = "reps", value = set.reps.toString())
                                set.rpe?.let {
                                    SetDataPoint(label = "RPE", value = it.toString())
                                }
                            }
                            
                            if (set.completed) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Completed",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        if (!set.notes.isNullOrBlank()) {
                            Text(
                                text = set.notes,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(start = 64.dp, bottom = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SetDataPoint(label: String, value: String) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(2.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}
