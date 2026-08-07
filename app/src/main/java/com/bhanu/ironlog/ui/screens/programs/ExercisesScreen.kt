package com.bhanu.ironlog.ui.screens.programs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.bhanu.ironlog.data.local.entity.ExerciseEntity
import com.bhanu.ironlog.data.local.entity.LibraryExerciseEntity
import com.bhanu.ironlog.data.local.pojo.ProgramExerciseWithLibrary
import com.bhanu.ironlog.data.repository.SaveExerciseResult
import com.bhanu.ironlog.ui.components.ErrorScreen
import com.bhanu.ironlog.ui.components.SearchBar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(
    onBack: () -> Unit,
    onNavigateToLogging: (Long) -> Unit, // This will be removed from use but kept in signature for compatibility if needed elsewhere
    viewModel: ExercisesViewModel = hiltViewModel(),
) {
    if (!viewModel.isArgumentValid) {
        ErrorScreen(onBack = onBack, message = "Invalid Workout Day data")
        return
    }

    val workoutDay by viewModel.workoutDay.collectAsState()
    val exercises by viewModel.exercises.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val libraryExercises by viewModel.libraryExercises.collectAsState()
    val librarySearchQuery by viewModel.librarySearchQuery.collectAsState()

    var showPicker by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var exerciseToEdit by remember { mutableStateOf<ProgramExerciseWithLibrary?>(value = null) }
    var exerciseToDelete by remember { mutableStateOf<ProgramExerciseWithLibrary?>(value = null) }
    
    // For creating new exercise from picker
    var resultToShow by remember { mutableStateOf<SaveExerciseResult?>(null) }
    var pendingExercise by remember { mutableStateOf<LibraryExerciseEntity?>(null) }

    LaunchedEffect(Unit) {
        viewModel.saveResult.collect { result ->
            if (result is SaveExerciseResult.Success) {
                showAddDialog = false
                showPicker = false
                resultToShow = null
                pendingExercise = null
            } else {
                resultToShow = result
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(workoutDay?.name ?: "Exercises", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = "Search added exercises...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showPicker = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Exercise")
            }
        }
    ) { padding ->
        if (exercises.isEmpty() && searchQuery.isEmpty()) {
            EmptyExercisesState(
                onAddClick = { showPicker = true },
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
                itemsIndexed(exercises, key = { _, e -> e.programExercise.id }) { index, exercise ->
                    ExerciseItem(
                        exercise = exercise,
                        isFirst = index == 0,
                        isLast = (index == exercises.size - 1),
                        onEdit = { exerciseToEdit = exercise },
                        onDelete = { exerciseToDelete = exercise },
                        onDuplicate = { viewModel.duplicateExercise(exercise.programExercise.id) },
                        onMoveUp = { viewModel.moveExerciseUp(exercise.programExercise.id) },
                        onMoveDown = { viewModel.moveExerciseDown(exercise.programExercise.id) },
                        onToggleEnabled = { enabled ->
                            viewModel.updateExercise(exercise.programExercise.copy(enabled = enabled))
                        },
                        onClick = { exerciseToEdit = exercise }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    if (showPicker) {
        LibraryPickerSheet(
            query = librarySearchQuery,
            onQueryChange = { viewModel.onLibrarySearchQueryChange(it) },
            exercises = libraryExercises,
            addedExerciseIds = exercises.map { it.programExercise.libraryExerciseId }.toSet(),
            onSelect = { 
                viewModel.addExerciseFromLibrary(it)
                showPicker = false
            },
            onCreateNew = { name ->
                pendingExercise = LibraryExerciseEntity(name = name, muscleGroup = "Chest", normalizedName = "")
                showAddDialog = true
            },
            onDismiss = { showPicker = false }
        )
    }

    if (showAddDialog) {
        LibraryExerciseDialog(
            initialExercise = pendingExercise ?: LibraryExerciseEntity(name = "", muscleGroup = "Chest", normalizedName = ""),
            muscleGroups = viewModel.muscleGroups,
            equipmentOptions = viewModel.equipmentOptions,
            exerciseTypes = viewModel.exerciseTypes,
            onDismiss = { showAddDialog = false },
            onSave = { updated ->
                pendingExercise = updated
                viewModel.saveAndAddExercise(updated)
            }
        )
    }

    exerciseToEdit?.let { exercise ->
        ExercisePrescriptionDialog(
            initialExercise = exercise,
            onDismiss = { exerciseToEdit = null },
            onSave = { sets, minReps, maxReps, targetRpe, restTimer, useDefault, notes ->
                viewModel.updateExercise(
                    exercise.programExercise.copy(
                        targetSets = sets,
                        targetRepMin = minReps,
                        targetRepMax = maxReps,
                        targetRPE = targetRpe,
                        restTimerSeconds = restTimer,
                        useDefaultRestTimer = useDefault,
                        notes = notes
                    )
                )
                exerciseToEdit = null
            }
        )
    }

    exerciseToDelete?.let { exercise ->
        AlertDialog(
            onDismissRequest = { exerciseToDelete = null },
            title = { Text("Delete Exercise?") },
            text = { Text("Are you sure you want to remove \"${exercise.exerciseName}\" from this workout day?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteExercise(exercise.programExercise)
                        exerciseToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { exerciseToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // Result dialogs (Duplicate/Similarity)
    resultToShow?.let { result ->
        when (result) {
            is SaveExerciseResult.ExactDuplicate -> {
                AlertDialog(
                    onDismissRequest = { resultToShow = null },
                    title = { Text("Exercise already exists") },
                    text = { Text(result.existing.name) },
                    confirmButton = {
                        Button(onClick = { 
                            viewModel.addExerciseFromLibrary(result.existing)
                            resultToShow = null 
                            showAddDialog = false
                            showPicker = false
                        }) {
                            Text("Use Existing")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { resultToShow = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }
            is SaveExerciseResult.SimilarFound -> {
                AlertDialog(
                    onDismissRequest = { resultToShow = null },
                    title = { Text("Similar exercise found") },
                    text = {
                        Column {
                            Text("The following similar exercises exist:")
                            result.matches.forEach { 
                                Text("• ${it.name}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 2.dp))
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = { resultToShow = null; showAddDialog = false; showPicker = true }) {
                            Text("Use Existing")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { 
                            pendingExercise?.let { viewModel.saveAndAddExercise(it, ignoreSimilarity = true) }
                            resultToShow = null
                        }) {
                            Text("Create New")
                        }
                    }
                )
            }
            is SaveExerciseResult.Error -> {
                AlertDialog(
                    onDismissRequest = { resultToShow = null },
                    title = { Text("Error") },
                    text = { Text(result.message) },
                    confirmButton = {
                        Button(onClick = { resultToShow = null }) { Text("OK") }
                    }
                )
            }
            else -> {}
        }
    }
}

@Composable
fun ExerciseItem(
    exercise: ProgramExerciseWithLibrary,
    isFirst: Boolean,
    isLast: Boolean,
    onEdit: (ProgramExerciseWithLibrary) -> Unit,
    onDelete: (ProgramExerciseWithLibrary) -> Unit,
    onDuplicate: (ProgramExerciseWithLibrary) -> Unit,
    onMoveUp: (ProgramExerciseWithLibrary) -> Unit,
    onMoveDown: (ProgramExerciseWithLibrary) -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onClick: (ProgramExerciseWithLibrary) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(exercise) },
        shape = MaterialTheme.shapes.large,
        colors = if (exercise.programExercise.enabled) CardDefaults.elevatedCardColors()
        else CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.exerciseName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (exercise.programExercise.enabled) MaterialTheme.colorScheme.onSurface 
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrescriptionBadge(text = "${exercise.programExercise.targetSets} Sets")
                    PrescriptionBadge(text = "${exercise.programExercise.targetRepMin}-${exercise.programExercise.targetRepMax} Reps")
                    if (exercise.programExercise.targetRPE != null) {
                        PrescriptionBadge(text = "RPE ${exercise.programExercise.targetRPE}")
                    }
                    val rest = if (exercise.programExercise.useDefaultRestTimer) "90s" else "${exercise.programExercise.restTimerSeconds}s"
                    PrescriptionBadge(text = "${rest} Rest")
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Edit Prescription") },
                        onClick = { showMenu = false; onEdit(exercise) },
                        leadingIcon = { Icon(Icons.Default.Edit, null) }
                    )
                    DropdownMenuItem(
                        text = { Text(if (exercise.programExercise.enabled) "Disable" else "Enable") },
                        onClick = { showMenu = false; onToggleEnabled(!exercise.programExercise.enabled) },
                        leadingIcon = { Icon(if (exercise.programExercise.enabled) Icons.Default.Block else Icons.Default.CheckCircle, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Duplicate") },
                        onClick = { showMenu = false; onDuplicate(exercise) },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Move Up") },
                        onClick = { showMenu = false; onMoveUp(exercise) },
                        leadingIcon = { Icon(Icons.Default.ArrowUpward, null) },
                        enabled = !isFirst
                    )
                    DropdownMenuItem(
                        text = { Text("Move Down") },
                        onClick = { showMenu = false; onMoveDown(exercise) },
                        leadingIcon = { Icon(Icons.Default.ArrowDownward, null) },
                        enabled = !isLast
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        onClick = { showMenu = false; onDelete(exercise) },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                    )
                }
            }
        }
    }
}

@Composable
fun PrescriptionBadge(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryPickerSheet(
    query: String,
    onQueryChange: (String) -> Unit,
    exercises: List<LibraryExerciseEntity>,
    addedExerciseIds: Set<Long>,
    onSelect: (LibraryExerciseEntity) -> Unit,
    onCreateNew: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.fillMaxHeight(0.8f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                "Add Exercise",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            SearchBar(
                query = query,
                onQueryChange = onQueryChange,
                placeholder = "Search library...",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            
            if (exercises.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No matches in library", color = MaterialTheme.colorScheme.outline)
                        if (query.isNotBlank()) {
                            TextButton(onClick = { onCreateNew(query) }) {
                                Text("Create \"$query\"?")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(exercises, key = { it.id }) { exercise ->
                        val alreadyAdded = addedExerciseIds.contains(exercise.id)
                        LibraryExercisePickerItem(
                            exercise = exercise,
                            alreadyAdded = alreadyAdded,
                            onClick = { if (!alreadyAdded) onSelect(exercise) }
                        )
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun LibraryExercisePickerItem(
    exercise: LibraryExerciseEntity,
    alreadyAdded: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !alreadyAdded, onClick = onClick),
        colors = if (alreadyAdded) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                 else CardDefaults.cardColors()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(exercise.name, fontWeight = FontWeight.Bold)
                Text("${exercise.muscleGroup} • ${exercise.equipment}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
            if (alreadyAdded) {
                Text("Added", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun ExercisePrescriptionDialog(
    initialExercise: ProgramExerciseWithLibrary,
    onDismiss: () -> Unit,
    onSave: (Int, Int, Int, Double?, Int, Boolean, String) -> Unit
) {
    var sets by remember { mutableIntStateOf(initialExercise.programExercise.targetSets) }
    var minReps by remember { mutableIntStateOf(initialExercise.programExercise.targetRepMin) }
    var maxReps by remember { mutableIntStateOf(initialExercise.programExercise.targetRepMax) }
    var targetRpe by remember { mutableStateOf(initialExercise.programExercise.targetRPE?.toString() ?: "") }
    var restTimer by remember { mutableIntStateOf(initialExercise.programExercise.restTimerSeconds) }
    var useDefault by remember { mutableStateOf(initialExercise.programExercise.useDefaultRestTimer) }
    var notes by remember { mutableStateOf(initialExercise.programExercise.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(initialExercise.exerciseName) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Prescription", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = if (sets == 0) "" else sets.toString(),
                        onValueChange = { sets = it.toIntOrNull() ?: 0 },
                        label = { Text("Sets") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = targetRpe,
                        onValueChange = { targetRpe = it },
                        label = { Text("Target RPE") },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("e.g. 8.5") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = if (minReps == 0) "" else minReps.toString(),
                        onValueChange = { minReps = it.toIntOrNull() ?: 0 },
                        label = { Text("Min Reps") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = if (maxReps == 0) "" else maxReps.toString(),
                        onValueChange = { maxReps = it.toIntOrNull() ?: 0 },
                        label = { Text("Max Reps") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider()
                
                Text("Rest Timer", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = useDefault, onClick = { useDefault = true })
                    Text("Use Global Default (90s)", style = MaterialTheme.typography.bodyMedium)
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = !useDefault, onClick = { useDefault = false })
                    Text("Custom Timer", style = MaterialTheme.typography.bodyMedium)
                }
                
                if (!useDefault) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = if (restTimer == 0) "" else restTimer.toString(),
                            onValueChange = { restTimer = it.toIntOrNull() ?: 0 },
                            label = { Text("Seconds") },
                            modifier = Modifier.width(100.dp),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                        )
                        Text("sec", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onSave(sets, minReps, maxReps, targetRpe.toDoubleOrNull(), restTimer, useDefault, notes) 
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryExerciseDialog(
    initialExercise: LibraryExerciseEntity,
    muscleGroups: List<String>,
    equipmentOptions: List<String>,
    exerciseTypes: List<String>,
    onDismiss: () -> Unit,
    onSave: (LibraryExerciseEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialExercise.name) }
    var muscle by remember { mutableStateOf(initialExercise.muscleGroup) }
    var equipment by remember { mutableStateOf(initialExercise.equipment) }
    var type by remember { mutableStateOf(initialExercise.exerciseType) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Library Exercise") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exercise Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                DropdownField(label = "Muscle Group", selected = muscle, options = muscleGroups) { muscle = it }
                DropdownField(label = "Equipment", selected = equipment, options = equipmentOptions) { equipment = it }
                DropdownField(label = "Exercise Type", selected = type, options = exerciseTypes) { type = it }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(initialExercise.copy(name = name, muscleGroup = muscle, equipment = equipment, exerciseType = type)) },
                enabled = name.isNotBlank()
            ) {
                Text("Create & Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun EmptyExercisesState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "No exercises added",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onAddClick) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Add from Library")
            }
        }
    }
}
