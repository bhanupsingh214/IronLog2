package com.bhanu.ironlog.ui.screens.records

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bhanu.ironlog.data.local.pojo.ExerciseStrengthHistory
import com.bhanu.ironlog.ui.components.ErrorScreen
import com.bhanu.ironlog.ui.components.StrengthProgressionChart
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.compose.m3.style.m3ChartStyle
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDetailScreen(
    onBack: () -> Unit,
    viewModel: RecordDetailViewModel = hiltViewModel()
) {
    if (!viewModel.isArgumentValid) {
        ErrorScreen(onBack = onBack, message = "Invalid Exercise data")
        return
    }

    val exercise by viewModel.exercise.collectAsState()
    val history by viewModel.history.collectAsState()
    val isE1RM by viewModel.isE1RMToggle.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(exercise?.name ?: "Record Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is RecordDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is RecordDetailUiState.Success -> {
                    if (state.current == null) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No history found for this exercise")
                        }
                    } else {
                        DetailContent(
                            exerciseName = exercise?.name ?: "",
                            current = state.current,
                            previous = state.previous,
                            summary = state.summary,
                            history = history,
                            isE1RM = isE1RM,
                            onToggleE1RM = { viewModel.toggleE1RM(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailContent(
    exerciseName: String,
    current: ExerciseStrengthHistory,
    previous: ExerciseStrengthHistory?,
    summary: RecordSummary?,
    history: List<ExerciseStrengthHistory>,
    isE1RM: Boolean,
    onToggleE1RM: (Boolean) -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ComparisonSection(current, previous, isE1RM, onToggleE1RM)
        }

        item {
            ProgressionChartCard(history, isE1RM)
        }

        if (summary != null) {
            item {
                SummarySection(summary)
            }
        }

        item {
            Text("Record History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(history.sortedByDescending { it.date }) { entry ->
            HistoryItem(entry)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparisonSection(
    current: ExerciseStrengthHistory,
    previous: ExerciseStrengthHistory?,
    isE1RM: Boolean,
    onToggleE1RM: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = !isE1RM,
                    onClick = { onToggleE1RM(false) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("Weight", style = MaterialTheme.typography.labelSmall)
                }
                SegmentedButton(
                    selected = isE1RM,
                    onClick = { onToggleE1RM(true) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("Est. 1RM", style = MaterialTheme.typography.labelSmall)
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val currentValue = if (isE1RM) current.maxE1RM else current.maxWeight
                val prevValue = if (isE1RM) previous?.maxE1RM else previous?.maxWeight
                
                Column {
                    Text("Current PR", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        String.format(Locale.getDefault(), "%,.1f kg", currentValue),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (prevValue != null && prevValue > 0) {
                    val improvement = currentValue - prevValue
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Previous PR", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text(
                            String.format(Locale.getDefault(), "%,.1f kg", prevValue),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            String.format(Locale.getDefault(), "(+%,.1f kg)", improvement),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            val dateFormat = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
            Text(
                text = "Achieved on ${dateFormat.format(Date(current.date))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun ProgressionChartCard(
    history: List<ExerciseStrengthHistory>,
    isE1RM: Boolean
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Progression", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            
            StrengthProgressionChart(
                history = history,
                isE1RM = isE1RM,
                modifier = Modifier.height(200.dp).fillMaxWidth()
            )
        }
    }
}

@Composable
fun SummarySection(summary: RecordSummary) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Summary", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryStat("Total PRs", summary.totalPRs.toString())
                SummaryStat("Biggest Jump", String.format(Locale.getDefault(), "%,.1f kg", summary.biggestWeightImprovement))
            }
            
            Spacer(Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryStat("First Achieved", dateFormat.format(Date(summary.firstPRDate)))
                SummaryStat("Latest Achieved", dateFormat.format(Date(summary.latestPRDate)))
            }
        }
    }
}

@Composable
fun SummaryStat(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HistoryItem(entry: ExerciseStrengthHistory) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(dateFormat.format(Date(entry.date)), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text("Strength Record", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    String.format(Locale.getDefault(), "%,.1f kg", entry.maxWeight),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    String.format(Locale.getDefault(), "e1RM: %,.1f kg", entry.maxE1RM),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
