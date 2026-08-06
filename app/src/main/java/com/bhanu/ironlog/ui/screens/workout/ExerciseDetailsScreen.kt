package com.bhanu.ironlog.ui.screens.workout

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bhanu.ironlog.data.local.pojo.ExerciseDetails
import com.bhanu.ironlog.data.local.pojo.ExerciseSessionRecord
import com.bhanu.ironlog.ui.components.ErrorScreen
import com.bhanu.ironlog.ui.components.formatTimer
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailsScreen(
    onBack: () -> Unit,
    onNavigateToSession: (Long) -> Unit,
    viewModel: ExerciseDetailsViewModel = hiltViewModel()
) {
    if (!viewModel.isArgumentValid) {
        ErrorScreen(onBack = onBack, message = "Invalid Exercise data")
        return
    }

    val details by viewModel.exerciseDetails.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exercise Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (details == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            ExerciseDetailsContent(
                details = details!!,
                onSessionClick = onNavigateToSession,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
fun ExerciseDetailsContent(
    details: ExerciseDetails,
    onSessionClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ExerciseHeader(details)
        }

        item {
            LifetimeSummary(details)
        }

        item {
            Text(
                text = "History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(details.sessionHistory, key = { it.sessionId }) { record ->
            ExerciseSessionCard(
                record = record,
                onClick = { onSessionClick(record.sessionId) }
            )
        }
        
        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
fun ExerciseHeader(details: ExerciseDetails) {
    Column {
        Text(
            text = details.exerciseName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = details.muscleGroup,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun LifetimeSummary(details: ExerciseDetails) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("en", "IN"))
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryItem(label = "Sessions", value = details.totalSessions.toString(), modifier = Modifier.weight(1f))
                SummaryItem(label = "Total Volume", value = String.format(Locale.getDefault(), "%,.0f kg", details.totalVolume), modifier = Modifier.weight(1f))
            }
            
            HorizontalDivider(thickness = 0.5.dp)
            
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryItem(label = "Best Weight", value = String.format(Locale.getDefault(), "%.1f kg", details.bestWeight), modifier = Modifier.weight(1f))
                SummaryItem(label = "Est. 1RM", value = String.format(Locale.getDefault(), "%.1f kg", details.estimated1RM), modifier = Modifier.weight(1f))
            }

            HorizontalDivider(thickness = 0.5.dp)

            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryItem(label = "First Done", value = dateFormat.format(Date(details.firstPerformed)), modifier = Modifier.weight(1f))
                SummaryItem(label = "Last Done", value = dateFormat.format(Date(details.lastPerformed)), modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ExerciseSessionCard(
    record: ExerciseSessionRecord,
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("en", "IN"))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dateFormat.format(Date(record.date)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = record.workoutName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 8.dp)) {
                        Text(
                            text = String.format(Locale.getDefault(), "%,.0f kg", record.sessionVolume),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${record.sets.size} sets",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(onClick = onClick) {
                        Icon(Icons.Default.OpenInNew, "View Session", modifier = Modifier.size(20.dp))
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    record.sets.forEach { set ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Set ${set.setNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.width(48.dp)
                            )
                            
                            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.Center) {
                                Text(
                                    text = "${String.format(Locale.getDefault(), "%.1f", set.weight)} kg × ${set.reps}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            
                            if (set.rpe != null) {
                                Text(
                                    text = "RPE ${set.rpe}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
