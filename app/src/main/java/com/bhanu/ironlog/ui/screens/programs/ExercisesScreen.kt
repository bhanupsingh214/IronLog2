package com.bhanu.ironlog.ui.screens.programs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bhanu.ironlog.data.local.entity.ExerciseEntity
import com.bhanu.ironlog.ui.components.SearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(
    onBack: () -> Unit,
    viewModel: ExercisesViewModel = hiltViewModel(),
) {
    val workoutDay by viewModel.workoutDay.collectAsState()
    val exercises by viewModel.exercises.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showAddDialog by remember { mutableStateOf(value = false) }
    var exerciseToEdit by remember { mutableStateOf<ExerciseEntity?>(value = null) }
    var exerciseToDelete by remember { mutableStateOf<ExerciseEntity?>(value = null) }

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
                    placeholder = "Search exercises...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Exercise")
            }
        }
    ) { padding ->
        if (exercises.isEmpty() && searchQuery.isEmpty()) {
            EmptyExercisesState(
                onAddClick = { showAddDialog = true },
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
                itemsIndexed(exercises, key = { _, e -> e.id }) { index, exercise ->
                    ExerciseItem(
                        exercise = exercise,
                        isFirst = index == 0,
                        isLast = (index == exercises.size - 1),
                        onEdit = { exerciseToEdit = it },
                        onDelete = { exerciseToDelete = it },
                        onDuplicate = { viewModel.duplicateExercise(it.id) },
                        onMoveUp = { viewModel.moveExerciseUp(it.id) },
                        onMoveDown = { viewModel.moveExerciseDown(it.id) },
                        onToggleEnabled = { enabled ->
                            viewModel.updateExercise(exercise.copy(enabled = enabled))
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        ExerciseDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, muscle, equipment, type, notes ->
                viewModel.addExercise(name, muscle, equipment, type, notes)
                showAddDialog = false
            }
        )
    }

    exerciseToEdit?.let { exercise ->
        ExerciseDialog(
            initialExercise = exercise,
            onDismiss = { exerciseToEdit = null },
            onSave = { name, muscle, equipment, type, notes ->
                viewModel.updateExercise(
                    exercise.copy(
                        name = name,
                        muscleGroup = muscle,
                        equipment = equipment,
                        exerciseType = type,
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
            text = { Text("Are you sure you want to delete \"${exercise.name}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteExercise(exercise)
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
}

@Composable
fun ExerciseItem(
    exercise: ExerciseEntity,
    isFirst: Boolean,
    isLast: Boolean,
    onEdit: (ExerciseEntity) -> Unit,
    onDelete: (ExerciseEntity) -> Unit,
    onDuplicate: (ExerciseEntity) -> Unit,
    onMoveUp: (ExerciseEntity) -> Unit,
    onMoveDown: (ExerciseEntity) -> Unit,
    onToggleEnabled: (Boolean) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = if (exercise.enabled) CardDefaults.elevatedCardColors()
        else CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (exercise.enabled) MaterialTheme.colorScheme.onSurface 
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = { },
                        label = { Text(exercise.muscleGroup, fontSize = 10.sp) },
                        modifier = Modifier.height(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = exercise.equipment,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Rename/Edit") },
                        onClick = { showMenu = false; onEdit(exercise) },
                        leadingIcon = { Icon(Icons.Default.Edit, null) }
                    )
                    DropdownMenuItem(
                        text = { Text(if (exercise.enabled) "Disable" else "Enable") },
                        onClick = { showMenu = false; onToggleEnabled(!exercise.enabled) },
                        leadingIcon = { Icon(if (exercise.enabled) Icons.Default.Block else Icons.Default.CheckCircle, null) }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDialog(
    initialExercise: ExerciseEntity? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialExercise?.name ?: "") }
    var muscle by remember { mutableStateOf(initialExercise?.muscleGroup ?: "") }
    var equipment by remember { mutableStateOf(initialExercise?.equipment ?: "") }
    var type by remember { mutableStateOf(initialExercise?.exerciseType ?: "Compound") }
    var notes by remember { mutableStateOf(initialExercise?.notes ?: "") }

    var expandedType by remember { mutableStateOf(false) }
    val types = listOf("Compound", "Isolation", "Cardio")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialExercise == null) "Add Exercise" else "Edit Exercise") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exercise Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = muscle,
                    onValueChange = { muscle = it },
                    label = { Text("Muscle Group") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. Chest, Quads") }
                )
                OutlinedTextField(
                    value = equipment,
                    onValueChange = { equipment = it },
                    label = { Text("Equipment") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. Barbell, Dumbbell") }
                )
                
                ExposedDropdownMenuBox(
                    expanded = expandedType,
                    onExpandedChange = { expandedType = it }
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Exercise Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedType,
                        onDismissRequest = { expandedType = false }
                    ) {
                        types.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t) },
                                onClick = {
                                    type = t
                                    expandedType = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, muscle, equipment, type, notes) },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
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
                Text("Add Exercise")
            }
        }
    }
}
