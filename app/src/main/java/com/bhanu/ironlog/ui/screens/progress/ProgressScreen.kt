package com.bhanu.ironlog.ui.screens.progress

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bhanu.ironlog.data.local.pojo.PRWithExerciseName
import com.bhanu.ironlog.data.local.pojo.TrackableExercise
import com.bhanu.ironlog.data.model.analytics.MuscleGroupCount
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
fun ProgressScreen(
    onNavigateToRecords: () -> Unit,
    goalsContent: @Composable (() -> Unit)? = null,
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val allExercises by viewModel.allExercises.collectAsState()
    val selectedExercise by viewModel.selectedExercise.collectAsState()
    val isE1RM by viewModel.isE1RMToggle.collectAsState()
    val strengthHistory by viewModel.strengthHistory.collectAsState()
    val volumeHistory by viewModel.volumeHistory.collectAsState()
    val volumeFilter by viewModel.volumeTimeFilter.collectAsState()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Progress", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is ProgressUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ProgressUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { /* ViewModel could expose a refresh function if needed */ }) {
                            Text("Retry")
                        }
                    }
                }
                is ProgressUiState.Success -> {
                    if (state.totalWorkouts == 0) {
                        EmptyProgressState()
                    } else {
                        ProgressContent(
                            state = state,
                            exercises = allExercises,
                            selectedExercise = selectedExercise,
                            isE1RM = isE1RM,
                            strengthHistory = strengthHistory,
                            volumeHistory = volumeHistory,
                            volumeFilter = volumeFilter,
                            goalsContent = goalsContent,
                            onExerciseSelected = { libId, tempId -> viewModel.onExerciseSelected(libId, tempId) },
                            onToggleE1RM = { viewModel.toggleE1RM(it) },
                            onVolumeFilterSelected = { viewModel.onVolumeFilterSelected(it) },
                            onNavigateToRecords = onNavigateToRecords
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressContent(
    state: ProgressUiState.Success,
    exercises: List<TrackableExercise>,
    selectedExercise: SelectedExerciseIdentity?,
    isE1RM: Boolean,
    strengthHistory: List<com.bhanu.ironlog.data.local.pojo.ExerciseStrengthHistory>,
    volumeHistory: List<com.bhanu.ironlog.data.local.pojo.DailyVolume>,
    volumeFilter: TimeFilter,
    goalsContent: @Composable (() -> Unit)? = null,
    onExerciseSelected: (Long, Long) -> Unit,
    onToggleE1RM: (Boolean) -> Unit,
    onVolumeFilterSelected: (TimeFilter) -> Unit,
    onNavigateToRecords: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Workouts",
                    value = String.format(Locale.getDefault(), "%,d", state.totalWorkouts),
                    icon = Icons.Default.History,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Total Volume",
                    value = String.format(Locale.getDefault(), "%,.0fkg", state.totalVolume),
                    icon = Icons.Default.FitnessCenter,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Weight PRs",
                    value = state.weightPRCount.toString(),
                    icon = Icons.Default.EmojiEvents,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "e1RM PRs",
                    value = state.e1rmPRCount.toString(),
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Frequency", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text(String.format(Locale.getDefault(), "%.1f sessions/week", state.summary.weeklyFrequency), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Icon(Icons.Default.Speed, null, tint = MaterialTheme.colorScheme.secondary)
                }
            }
        }

        if (goalsContent != null) {
            item {
                goalsContent()
            }
        }

        item {
            Text("Strength Progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            StrengthChartSection(
                exercises = exercises,
                selectedExercise = selectedExercise,
                isE1RM = isE1RM,
                history = strengthHistory,
                onExerciseSelected = onExerciseSelected,
                onToggleE1RM = onToggleE1RM
            )
        }

        item {
            Text("Volume Trend", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            VolumeTrendSection(
                history = volumeHistory,
                currentFilter = volumeFilter,
                onFilterSelected = onVolumeFilterSelected
            )
        }

        item {
            Text("Volume Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            VolumeSummaryCard(state)
        }

        if (state.muscleDistribution.isNotEmpty()) {
            item {
                Text("Training Focus", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            item {
                MuscleDistributionCard(state.muscleDistribution)
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent Personal Records", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = onNavigateToRecords) {
                    Text("View All")
                }
            }
        }

        if (state.latestPRs.isEmpty()) {
            item {
                Text("No PRs yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
            }
        } else {
            items(state.latestPRs) { pr ->
                PRItem(pr)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrengthChartSection(
    exercises: List<TrackableExercise>,
    selectedExercise: SelectedExerciseIdentity?,
    isE1RM: Boolean,
    history: List<com.bhanu.ironlog.data.local.pojo.ExerciseStrengthHistory>,
    onExerciseSelected: (Long, Long) -> Unit,
    onToggleE1RM: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val currentExercise = exercises.find {
        it.libraryExerciseId == selectedExercise?.libraryId &&
        it.exerciseTemplateId == selectedExercise?.templateId
    }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = currentExercise?.displayName ?: "Select Exercise",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    exercises.forEach { exercise ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(exercise.displayName)
                                    Text(exercise.displayMuscle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                }
                            },
                            onClick = {
                                onExerciseSelected(exercise.libraryExerciseId, exercise.exerciseTemplateId)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

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

            StrengthProgressionChart(
                history = history,
                isE1RM = isE1RM,
                modifier = Modifier.height(200.dp).fillMaxWidth()
            )
        }
    }
}

@Composable
fun VolumeTrendSection(
    history: List<com.bhanu.ironlog.data.local.pojo.DailyVolume>,
    currentFilter: TimeFilter,
    onFilterSelected: (TimeFilter) -> Unit
) {
    ProvideChartStyle(m3ChartStyle()) {
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimeFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = currentFilter == filter,
                            onClick = { onFilterSelected(filter) },
                            label = { Text(filter.label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (history.size < 2) {
                    Box(Modifier.height(200.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Train more to unlock your progress graph", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    val entries = history.mapIndexed { index, item ->
                        entryOf(index.toFloat(), item.volume.toFloat())
                    }

                    val model = entryModelOf(entries)

                    Chart(
                        chart = lineChart(),
                        model = model,
                        startAxis = rememberStartAxis(
                            valueFormatter = { value, _ -> String.format(Locale.getDefault(), "%,.0f", value) }
                        ),
                        bottomAxis = rememberBottomAxis(
                            valueFormatter = { value, _ ->
                                val index = value.toInt()
                                if (index in history.indices) {
                                    SimpleDateFormat("dd/MM", Locale("en", "IN")).format(Date(history[index].date))
                                } else ""
                            }
                        ),
                        chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = true),
                        isZoomEnabled = true,
                        modifier = Modifier.height(200.dp).fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun VolumeSummaryCard(state: ProgressUiState.Success) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Weekly Volume", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                    Text(String.format(Locale.getDefault(), "%,.1f kg", state.weeklyVolume), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Monthly Volume", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                    Text(String.format(Locale.getDefault(), "%,.1f kg", state.monthlyVolume), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }

            val trend = state.summary.monthlyVolumeTrend
            if (trend.size >= 2) {
                val last = trend.last()
                val prev = trend[trend.size - 2]
                if (prev > 0) {
                    val percent = ((last - prev) / prev) * 100
                    val color = if (percent >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    val arrow = if (percent >= 0) "↑" else "↓"
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = String.format(Locale.getDefault(), "%s %.1f%% vs last month", arrow, kotlin.math.abs(percent)),
                        style = MaterialTheme.typography.labelSmall,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun MuscleDistributionCard(distribution: List<MuscleGroupCount>) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val maxCount = distribution.maxOf { it.count }
            distribution.take(6).forEach { muscle ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(muscle.muscleGroup, style = MaterialTheme.typography.bodyMedium)
                        Text("${muscle.count} ex.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                    LinearProgressIndicator(
                        progress = { muscle.count.toFloat() / maxCount },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
fun PRItem(item: PRWithExerciseName) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(item.snapshotName ?: "Deleted Exercise", fontWeight = FontWeight.Bold)
                Text("Weight PR", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
            Text(
                String.format(Locale.getDefault(), "%,.1f kg", item.pr.weightPR),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EmptyProgressState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(16.dp))
            Text("No workout data yet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Complete your first workout to start tracking progress", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
