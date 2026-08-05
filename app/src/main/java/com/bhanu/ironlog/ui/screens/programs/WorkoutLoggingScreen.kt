package com.bhanu.ironlog.ui.screens.programs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bhanu.ironlog.ui.components.ErrorScreen
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutLoggingScreen(
    onBack: () -> Unit,
    onNavigateToExercise: (Long) -> Unit = {},
    viewModel: WorkoutLoggingViewModel = hiltViewModel(),
) {
    if (!viewModel.isArgumentValid) {
        ErrorScreen(onBack = onBack, message = "Invalid Exercise or Session data")
        return
    }

    val exercise by viewModel.exercise.collectAsState()
    val sessionExercise by viewModel.sessionExercise.collectAsState()
    val sets by viewModel.sets.collectAsState()
    val previousSets by viewModel.previousSets.collectAsState()
    val session by viewModel.session.collectAsState()
    val sessionId = viewModel.sessionId

    val isReadOnly = sessionId != 0L && session?.status == "COMPLETED"
    
    var isProcessingAction by remember { mutableStateOf(false) }

    // Listen for current exercise changes to trigger navigation
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            isProcessingAction = false
            when (event) {
                is LoggingNavigationEvent.NavigateToExercise -> {
                    onNavigateToExercise(event.exerciseId)
                }
                LoggingNavigationEvent.WorkoutFinished -> {
                    onBack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(exercise?.name ?: "Workout Logging", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (sessionId > 0 && !isReadOnly) {
                        IconButton(
                            onClick = { 
                                isProcessingAction = true
                                viewModel.moveToPreviousExercise() 
                            },
                            enabled = !isProcessingAction
                        ) {
                            Icon(Icons.Default.ChevronLeft, "Previous")
                        }
                        IconButton(
                            onClick = { 
                                isProcessingAction = true
                                viewModel.moveToNextExercise() 
                            },
                            enabled = !isProcessingAction
                        ) {
                            Icon(Icons.Default.ChevronRight, "Next")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (sessionId > 0 && !isReadOnly) {
                LoggingBottomBar(
                    onAddWorking = { viewModel.addSet("Working") },
                    onAddWarmup = { viewModel.addSet("Warm-up") },
                    onAddBackoff = { viewModel.addSet("Back-off") },
                    onCopyPrevious = { viewModel.copyPreviousSet() },
                    onSkipExercise = { 
                        isProcessingAction = true
                        viewModel.skipExercise() 
                    },
                    onCompleteExercise = { 
                        isProcessingAction = true
                        viewModel.completeExercise() 
                    },
                    enabled = !isProcessingAction
                )
            }
        },
        floatingActionButton = {
            if (sessionId == 0L) {
                FloatingActionButton(
                    onClick = { viewModel.addSet("Working") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Set")
                }
            }
        }
    ) { padding ->
        if (sets.isEmpty()) {
            EmptySetsState(
                onAddClick = { viewModel.addSet() },
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (sessionId > 0 && sessionExercise != null) {
                    item {
                        ExerciseNotesEditor(
                            notes = sessionExercise!!.notes,
                            onNotesChange = { viewModel.updateExerciseNotes(it) }
                        )
                    }
                }

                itemsIndexed(sets, key = { _, s -> s.id }) { index, set ->
                    val prevSet = previousSets.getOrNull(index)
                    SetItem(
                        set = set,
                        previousSet = prevSet,
                        isFirst = index == 0,
                        isLast = index == sets.size - 1,
                        isReadOnly = isReadOnly,
                        onUpdate = { viewModel.updateSet(it) },
                        onDelete = { viewModel.deleteSet(it) },
                        onDuplicate = { viewModel.duplicateSet(set.id) },
                        onMoveUp = { viewModel.moveSetUp(set.id) },
                        onMoveDown = { viewModel.moveSetDown(set.id) }
                    )
                }
                item {
                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun ExerciseNotesEditor(
    notes: String,
    onNotesChange: (String) -> Unit
) {
    var text by remember(notes) { mutableStateOf(notes) }
    
    OutlinedTextField(
        value = text,
        onValueChange = { 
            text = it
            onNotesChange(it)
        },
        label = { Text("Exercise Notes") },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Add instructions or thoughts...") },
        maxLines = 3,
        shape = MaterialTheme.shapes.large
    )
}

@Composable
fun SetItem(
    set: WorkoutSetUiModel,
    previousSet: WorkoutSetUiModel?,
    isFirst: Boolean,
    isLast: Boolean,
    isReadOnly: Boolean,
    onUpdate: (WorkoutSetUiModel) -> Unit,
    onDelete: (WorkoutSetUiModel) -> Unit,
    onDuplicate: (Long) -> Unit,
    onMoveUp: (Long) -> Unit,
    onMoveDown: (Long) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    // Local state to prevent cursor jumping while typing
    var localWeight by remember(set.id) { mutableStateOf(if (set.weight == 0.0) "" else set.weight.toString()) }
    var localReps by remember(set.id) { mutableStateOf(if (set.reps == 0) "" else set.reps.toString()) }
    var localRpe by remember(set.id) { mutableStateOf(set.rpe?.toString() ?: "") }
    
    // Using TextFieldValue to preserve cursor position for notes
    var notesFieldValue by remember(set.id) { 
        mutableStateOf(TextFieldValue(text = set.notes, selection = TextRange(set.notes.length))) 
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SET ${set.setNumber}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    SuggestionChip(
                        onClick = { },
                        label = { Text(set.setType, fontSize = 10.sp) },
                        modifier = Modifier.height(24.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isReadOnly) {
                        Checkbox(
                            checked = set.isCompleted,
                            onCheckedChange = { onUpdate(set.copy(isCompleted = it)) }
                        )
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Duplicate") },
                                onClick = { showMenu = false; onDuplicate(set.id) },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Move Up") },
                                onClick = { showMenu = false; onMoveUp(set.id) },
                                leadingIcon = { Icon(Icons.Default.ArrowUpward, null) },
                                enabled = !isFirst
                            )
                            DropdownMenuItem(
                                text = { Text("Move Down") },
                                onClick = { showMenu = false; onMoveDown(set.id) },
                                leadingIcon = { Icon(Icons.Default.ArrowDownward, null) },
                                enabled = !isLast
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                onClick = { showMenu = false; onDelete(set) },
                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                            )
                        }
                    }
                }
            }

            if (previousSet != null) {
                Text(
                    text = "Previous: ${previousSet.weight}kg x ${previousSet.reps} @ RPE ${previousSet.rpe ?: "-"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LoggingTextField(
                    value = localWeight,
                    onValueChange = { 
                        localWeight = it
                        val weight = it.toDoubleOrNull() ?: 0.0
                        onUpdate(set.copy(weight = weight))
                    },
                    label = "Weight",
                    modifier = Modifier.weight(1f),
                    suffix = "kg",
                    enabled = !isReadOnly
                )
                LoggingTextField(
                    value = localReps,
                    onValueChange = { 
                        localReps = it
                        val reps = it.toIntOrNull() ?: 0
                        onUpdate(set.copy(reps = reps))
                    },
                    label = "Reps",
                    modifier = Modifier.weight(1f),
                    enabled = !isReadOnly,
                    isInteger = true
                )
                LoggingTextField(
                    value = localRpe,
                    onValueChange = { 
                        localRpe = it
                        val rpe = it.toDoubleOrNull()
                        if (rpe == null || rpe in 0.0..10.0) {
                            onUpdate(set.copy(rpe = rpe))
                        }
                    },
                    label = "RPE",
                    modifier = Modifier.weight(1f),
                    enabled = !isReadOnly
                )
            }
            
            if (set.isCompleted || isReadOnly) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = notesFieldValue,
                    onValueChange = { 
                        notesFieldValue = it
                        onUpdate(set.copy(notes = it.text))
                    },
                    label = { Text("Notes", style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodySmall,
                    singleLine = true,
                    enabled = !isReadOnly
                )
            }
        }
    }
}

@Composable
fun LoggingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    suffix: String? = null,
    enabled: Boolean = true,
    isInteger: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            if (isInteger) {
                if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                    onValueChange(newValue)
                }
            } else {
                // Allow only one decimal point
                if (newValue.isEmpty() || newValue.count { it == '.' } <= 1 && newValue.all { it.isDigit() || it == '.' }) {
                    onValueChange(newValue)
                }
            }
        },
        label = { Text(label, fontSize = 10.sp) },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = if (isInteger) KeyboardType.Number else KeyboardType.Decimal),
        singleLine = true,
        suffix = suffix?.let { { Text(it, fontSize = 10.sp) } },
        enabled = enabled
    )
}

@Composable
fun LoggingBottomBar(
    onAddWorking: () -> Unit,
    onAddWarmup: () -> Unit,
    onAddBackoff: () -> Unit,
    onCopyPrevious: () -> Unit,
    onSkipExercise: () -> Unit = {},
    onCompleteExercise: () -> Unit = {},
    enabled: Boolean = true
) {
    Surface(
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSkipExercise,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    enabled = enabled
                ) {
                    Text("Skip", fontSize = 10.sp)
                }
                Button(
                    onClick = onCompleteExercise,
                    modifier = Modifier.weight(1f),
                    enabled = enabled
                ) {
                    Text("Done", fontSize = 10.sp)
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onAddWarmup,
                    modifier = Modifier.weight(1f),
                    enabled = enabled
                ) {
                    Text("Warmup", fontSize = 10.sp)
                }
                Button(
                    onClick = onAddWorking,
                    modifier = Modifier.weight(1.2f),
                    enabled = enabled
                ) {
                    Text("Working", fontSize = 10.sp)
                }
                OutlinedButton(
                    onClick = onAddBackoff,
                    modifier = Modifier.weight(1f),
                    enabled = enabled
                ) {
                    Text("Backoff", fontSize = 10.sp)
                }
                IconButton(
                    onClick = onCopyPrevious,
                    colors = IconButtonDefaults.filledIconButtonColors(),
                    enabled = enabled
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Previous")
                }
            }
        }
    }
}

@Composable
fun EmptySetsState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "No sets logged today",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onAddClick) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Start First Set")
            }
        }
    }
}
