package com.bhanu.ironlog.ui.screens.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bhanu.ironlog.data.local.pojo.SessionExerciseWithTemplateAndSets
import com.bhanu.ironlog.ui.components.ErrorScreen
import com.bhanu.ironlog.ui.components.formatTimer
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailsScreen(
    onBack: () -> Unit,
    viewModel: WorkoutDetailsViewModel = hiltViewModel()
) {
    if (!viewModel.isArgumentValid) {
        ErrorScreen(onBack = onBack, message = "Invalid Session data")
        return
    }

    val session by viewModel.session.collectAsState()
    val exercises by viewModel.exercises.collectAsState()
    val stats by viewModel.workoutStats.collectAsState()
    val prs by viewModel.personalRecords.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(session?.dayName ?: "Workout Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (session == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val currentSession = session ?: return@Scaffold
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SessionSummaryHeader(
                        dayName = currentSession.dayName,
                        programName = currentSession.programName,
                        date = currentSession.createdAt,
                        duration = currentSession.durationSeconds,
                        stats = stats
                    )
                }

                items(exercises, key = { it.sessionExercise.sessionExerciseId }) { exercise ->
                    val exercisePRs = prs.filter { it.exerciseTemplateId == exercise.template.id }
                    ExerciseDetailItem(exercise, exercisePRs, currentSession.sessionId)
                }
                
                item {
                    BottomSummarySection(stats)
                }
                
                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun SessionSummaryHeader(
    dayName: String,
    programName: String,
    date: Long,
    duration: Long,
    stats: WorkoutStats?
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("en", "IN"))
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(text = dayName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = programName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryStatItem("Date", dateFormat.format(Date(date)))
                SummaryStatItem("Duration", formatTimer(duration))
            }
            
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp)
            Spacer(Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryStatItem("Volume", String.format(Locale.getDefault(), "%,.0f kg", stats?.totalVolume ?: 0.0))
                SummaryStatItem("PRs", (stats?.prCount ?: 0).toString())
            }
        }
    }
}

@Composable
fun SummaryStatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ExerciseDetailItem(
    item: SessionExerciseWithTemplateAndSets,
    prs: List<com.bhanu.ironlog.data.local.entity.PersonalRecordEntity>,
    sessionId: Long
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = item.template.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${item.template.muscleGroup} • ${item.template.equipment}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(Modifier.padding(8.dp)) {
                item.sets.forEach { set ->
                    val isWeightPR = prs.any { it.weightPRSessionId == sessionId && it.weightPR == set.weight }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Set ${set.setNumber}",
                            modifier = Modifier.width(60.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${set.weight}kg x ${set.reps}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (isWeightPR) {
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = MaterialTheme.shapes.extraSmall
                                ) {
                                    Text(
                                        "PR",
                                        modifier = Modifier.padding(horizontal = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        if (set.rpe != null) {
                            Text(
                                text = "@ RPE ${set.rpe}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomSummarySection(stats: WorkoutStats?) {
    if (stats == null) return
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Total Volume", style = MaterialTheme.typography.labelSmall)
                Text(String.format(Locale.getDefault(), "%,.0f kg", stats.totalVolume), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Total Sets", style = MaterialTheme.typography.labelSmall)
                Text("${stats.totalSets} sets", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("Avg. Intensity", style = MaterialTheme.typography.labelSmall)
                Text(String.format(Locale.getDefault(), "%,.1f kg/rep", stats.averageIntensity), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}
