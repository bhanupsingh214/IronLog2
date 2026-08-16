package com.bhanu.ironlog.ui.screens.goals

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bhanu.ironlog.data.local.entity.GoalEntity
import com.bhanu.ironlog.data.local.pojo.TrackableExercise
import com.bhanu.ironlog.data.model.goals.GoalProgress
import com.bhanu.ironlog.data.model.goals.GoalStatus
import com.bhanu.ironlog.data.model.goals.GoalTrend
import com.bhanu.ironlog.data.model.goals.GoalType
import com.bhanu.ironlog.data.util.GoalCalculator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    onBack: () -> Unit = {},
    viewModel: GoalViewModel = hiltViewModel()
) {
    val goals by viewModel.goals.collectAsState()
    val exercises by viewModel.trackableExercises.collectAsState()
    val latestWeight by viewModel.latestWeight.collectAsState()
    val latestWaist by viewModel.latestWaist.collectAsState()
    var showCreate by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<GoalProgress?>(null) }
    var deletingGoal by remember { mutableStateOf<GoalProgress?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Goals", fontWeight = FontWeight.Bold) },
                navigationIcon = {},
                actions = {
                    IconButton(onClick = { showCreate = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Create goal")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreate = true }) {
                Icon(Icons.Default.Add, contentDescription = "Create goal")
            }
        }
    ) { padding ->
        if (goals.isEmpty()) {
            EmptyGoalsState(
                modifier = Modifier.padding(padding).fillMaxSize(),
                onCreate = { showCreate = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Goals are calculated from your saved IronLog data.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                items(goals, key = { it.goal.goalId }) { progress ->
                    GoalCard(
                        progress = progress,
                        exerciseName = exercises.firstOrNull { it.libraryExerciseId == progress.goal.libraryExerciseId }?.displayName,
                        onEdit = { editingGoal = progress },
                        onDelete = { deletingGoal = progress }
                    )
                }
            }
        }
    }

    if (showCreate) {
        GoalEditorDialog(
            goal = null,
            exercises = exercises,
            latestWeight = latestWeight,
            latestWaist = latestWaist,
            onDismiss = { showCreate = false },
            onSave = { type, target, baseline, exerciseId, frequencyCount, frequencyPeriod, deadline ->
                viewModel.createGoal(type, target, baseline, exerciseId, frequencyCount, frequencyPeriod, deadline)
                showCreate = false
            }
        )
    }

    editingGoal?.let { progress ->
        GoalEditorDialog(
            goal = progress.goal,
            exercises = exercises,
            latestWeight = latestWeight,
            latestWaist = latestWaist,
            onDismiss = { editingGoal = null },
            onSave = { _, target, _, _, _, _, deadline ->
                viewModel.updateGoal(progress.goal, target, deadline)
                editingGoal = null
            }
        )
    }

    deletingGoal?.let { progress ->
        AlertDialog(
            onDismissRequest = { deletingGoal = null },
            title = { Text("Delete goal?") },
            text = { Text("This removes the persisted goal. Completed goals remain until you choose to delete them.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteGoal(progress.goal)
                    deletingGoal = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deletingGoal = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun EmptyGoalsState(modifier: Modifier, onCreate: () -> Unit) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(12.dp))
        Text("No goals yet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Create a target and IronLog will calculate progress, trend, and deadline status locally.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onCreate) { Text("Create Goal") }
    }
}

@Composable
private fun GoalCard(
    progress: GoalProgress,
    exerciseName: String?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val goal = progress.goal
    val type = GoalType.entries.firstOrNull { it.key == goal.type }
    val targetText = if (type == GoalType.WORKOUT_FREQUENCY) {
        "${goal.frequencyCount ?: goal.targetValue.toInt()} / ${goal.frequencyPeriod?.lowercase(Locale.getDefault()) ?: "period"}"
    } else {
        "${formatNumber(goal.targetValue)} ${type?.unit.orEmpty()}"
    }
    val currentText = progress.currentValue?.let { "${formatNumber(it)} ${type?.unit.orEmpty()}" } ?: "Insufficient data"

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(type?.label ?: goal.type, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (type == GoalType.EXERCISE_PR && exerciseName != null) {
                        Text(exerciseName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit goal") }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete goal") }
            }

            Spacer(Modifier.height(8.dp))
            Text("Current: $currentText")
            Text("Target: $targetText")
            Spacer(Modifier.height(8.dp))

            progress.progress?.let {
                LinearProgressIndicator(progress = { it.toFloat() }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(4.dp))
                Text("Progress: ${formatNumber(it * 100.0)}%", style = MaterialTheme.typography.labelMedium)
            }

            if (type != GoalType.WORKOUT_FREQUENCY) {
                val trendText = when (progress.trend) {
                    GoalTrend.IMPROVING -> "Improving"
                    GoalTrend.STABLE -> "Stable"
                    GoalTrend.MOVING_AWAY -> "Moving away"
                    GoalTrend.INSUFFICIENT_DATA -> "Insufficient data"
                    null -> null
                }
                trendText?.let {
                    Spacer(Modifier.height(4.dp))
                    Text("Trend: $it", style = MaterialTheme.typography.labelMedium)
                }
            }

            progress.status?.let {
                Spacer(Modifier.height(4.dp))
                Text(statusLabel(it), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }

            goal.deadline?.let {
                Spacer(Modifier.height(4.dp))
                Text("Deadline: ${formatDate(it)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalEditorDialog(
    goal: GoalEntity?,
    exercises: List<TrackableExercise>,
    latestWeight: Double?,
    latestWaist: Double?,
    onDismiss: () -> Unit,
    onSave: (GoalType, Double, Double, Long?, Int?, String?, Long?) -> Unit
) {
    val context = LocalContext.current
    val isEditing = goal != null
    var type by remember { mutableStateOf(GoalType.entries.firstOrNull { it.key == goal?.type } ?: GoalType.WEIGHT) }
    var targetText by remember { mutableStateOf(goal?.targetValue?.let(::formatNumber) ?: "") }
    var baselineText by remember {
        mutableStateOf(
            when (type) {
                GoalType.WEIGHT -> latestWeight?.let(::formatNumber)
                GoalType.WAIST -> latestWaist?.let(::formatNumber)
                else -> goal?.startingValue?.let(::formatNumber)
            } ?: ""
        )
    }
    var selectedExerciseId by remember { mutableStateOf(goal?.libraryExerciseId) }
    var frequencyPeriod by remember { mutableStateOf(goal?.frequencyPeriod ?: "WEEKLY") }
    var deadline by remember { mutableStateOf(goal?.deadline) }
    var typeMenu by remember { mutableStateOf(false) }
    var exerciseMenu by remember { mutableStateOf(false) }
    var periodMenu by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(type) {
        if (!isEditing) {
            baselineText = when (type) {
                GoalType.WEIGHT -> latestWeight?.let(::formatNumber) ?: ""
                GoalType.WAIST -> latestWaist?.let(::formatNumber) ?: ""
                else -> "0"
            }
            if (type != GoalType.EXERCISE_PR) selectedExerciseId = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Edit Goal" else "Create Goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!isEditing) {
                    Box {
                        OutlinedButton(onClick = { typeMenu = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(type.label)
                        }
                        DropdownMenu(expanded = typeMenu, onDismissRequest = { typeMenu = false }) {
                            GoalType.entries.forEach { option ->
                                DropdownMenuItem(text = { Text(option.label) }, onClick = {
                                    type = option
                                    typeMenu = false
                                })
                            }
                        }
                    }

                    if (type == GoalType.EXERCISE_PR) {
                        val available = exercises.filter { it.libraryExerciseId > 0 }
                        Box {
                            OutlinedButton(onClick = { exerciseMenu = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(available.firstOrNull { it.libraryExerciseId == selectedExerciseId }?.displayName ?: "Select exercise")
                            }
                            DropdownMenu(expanded = exerciseMenu, onDismissRequest = { exerciseMenu = false }) {
                                available.forEach { exercise ->
                                    DropdownMenuItem(text = { Text(exercise.displayName) }, onClick = {
                                        selectedExerciseId = exercise.libraryExerciseId
                                        exerciseMenu = false
                                    })
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text(if (type == GoalType.WORKOUT_FREQUENCY) "Target sessions" else "Target ${type.unit}") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (!isEditing && type != GoalType.WORKOUT_FREQUENCY && type != GoalType.EXERCISE_PR) {
                    OutlinedTextField(
                        value = baselineText,
                        onValueChange = { baselineText = it },
                        label = { Text("Starting ${type.unit} (baseline)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (!isEditing && type == GoalType.EXERCISE_PR) {
                    Text("Starting PR: baseline is the current saved PR; 0 if none exists.", style = MaterialTheme.typography.bodySmall)
                }

                if (type == GoalType.WORKOUT_FREQUENCY) {
                    Box {
                        OutlinedButton(
                            onClick = { periodMenu = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isEditing
                        ) {
                            Text(if (frequencyPeriod == "MONTHLY") "Monthly" else "Weekly")
                        }
                        DropdownMenu(expanded = periodMenu, onDismissRequest = { periodMenu = false }) {
                            DropdownMenuItem(text = { Text("Weekly (Monday-Sunday)") }, onClick = {
                                frequencyPeriod = "WEEKLY"
                                periodMenu = false
                            })
                            DropdownMenuItem(text = { Text("Monthly (calendar month)") }, onClick = {
                                frequencyPeriod = "MONTHLY"
                                periodMenu = false
                            })
                        }
                    }
                }

                OutlinedButton(onClick = {
                    val initial = Calendar.getInstance().apply { deadline?.let { timeInMillis = it } }
                    DatePickerDialog(context, { _, year, month, day ->
                        deadline = Calendar.getInstance().apply {
                            set(year, month, day, 23, 59, 59)
                            set(Calendar.MILLISECOND, 999)
                        }.timeInMillis
                    }, initial.get(Calendar.YEAR), initial.get(Calendar.MONTH), initial.get(Calendar.DAY_OF_MONTH)).show()
                }, modifier = Modifier.fillMaxWidth()) {
                    Text(deadline?.let { "Deadline: ${formatDate(it)}" } ?: "No deadline")
                }
                if (deadline != null) {
                    TextButton(onClick = { deadline = null }) { Text("Remove deadline") }
                }

                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val target = targetText.toDoubleOrNull()
                val baseline = baselineText.toDoubleOrNull()
                val count = if (type == GoalType.WORKOUT_FREQUENCY) target?.toInt() else null
                val valid = when {
                    target == null || target <= 0.0 -> "Enter a positive target."
                    type == GoalType.WORKOUT_FREQUENCY && !GoalCalculator.isValidFrequencyTarget(target) -> "Enter a whole number for sessions."
                    !isEditing && type != GoalType.WORKOUT_FREQUENCY && type != GoalType.EXERCISE_PR && (baseline == null || baseline <= 0.0) -> "A valid baseline is required."
                    !isEditing && type == GoalType.EXERCISE_PR && selectedExerciseId == null -> "Select an exercise."
                    type == GoalType.WORKOUT_FREQUENCY && (count == null || count <= 0) -> "Enter a positive session count."
                    deadline != null && deadline!! <= System.currentTimeMillis() -> "Deadline must be in the future."
                    else -> null
                }
                if (valid != null) {
                    error = valid
                } else {
                    val start = when {
                        isEditing -> goal!!.startingValue
                        type == GoalType.WORKOUT_FREQUENCY -> 0.0
                        type == GoalType.EXERCISE_PR -> 0.0
                        else -> baseline!!
                    }
                    onSave(type, target!!, start, selectedExerciseId, count, if (type == GoalType.WORKOUT_FREQUENCY) frequencyPeriod else null, deadline)
                }
            }) { Text(if (isEditing) "Save" else "Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun statusLabel(status: GoalStatus): String = when (status) {
    GoalStatus.ON_TRACK -> "On Track"
    GoalStatus.BEHIND -> "Behind"
    GoalStatus.OVERDUE -> "Overdue"
    GoalStatus.COMPLETED -> "Completed"
}

private fun formatNumber(value: Double): String = String.format(Locale.getDefault(), "%.2f", value).trimEnd('0').trimEnd('.')

private fun formatDate(timestamp: Long): String = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(timestamp)
