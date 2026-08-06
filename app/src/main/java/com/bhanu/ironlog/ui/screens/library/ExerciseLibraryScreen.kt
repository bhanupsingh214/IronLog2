package com.bhanu.ironlog.ui.screens.library

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bhanu.ironlog.data.local.entity.LibraryExerciseEntity
import com.bhanu.ironlog.data.repository.SaveExerciseResult
import com.bhanu.ironlog.ui.components.SearchBar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseLibraryScreen(
    onBack: () -> Unit,
    viewModel: ExerciseLibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var exerciseToEdit by remember { mutableStateOf<LibraryExerciseEntity?>(null) }
    var exerciseToArchive by remember { mutableStateOf<LibraryExerciseEntity?>(null) }
    
    // For handling repository save flow results
    var resultToShow by remember { mutableStateOf<SaveExerciseResult?>(null) }
    var pendingExercise by remember { mutableStateOf<LibraryExerciseEntity?>(null) }

    LaunchedEffect(Unit) {
        viewModel.saveResult.collect { result ->
            when (result) {
                is SaveExerciseResult.Success -> {
                    showAddDialog = false
                    exerciseToEdit = null
                    resultToShow = null
                    pendingExercise = null
                }
                else -> {
                    resultToShow = result
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text("Exercise Library", fontWeight = FontWeight.Bold)
                            if (uiState is ExerciseLibraryUiState.Success) {
                                Text(
                                    text = "${(uiState as ExerciseLibraryUiState.Success).totalActiveCount} Active Exercises",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    },
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
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Exercise")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is ExerciseLibraryUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ExerciseLibraryUiState.Success -> {
                    if (state.exercises.isEmpty()) {
                        EmptyLibraryState(
                            query = searchQuery,
                            onCreateClick = { 
                                pendingExercise = LibraryExerciseEntity(name = searchQuery, muscleGroup = "Chest", normalizedName = "")
                                showAddDialog = true 
                            }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.exercises, key = { it.id }) { exercise ->
                                ExerciseLibraryCard(
                                    exercise = exercise,
                                    onEdit = { exerciseToEdit = it },
                                    onArchive = { exerciseToArchive = it }
                                )
                            }
                            item { Spacer(Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog || exerciseToEdit != null) {
        val exercise = exerciseToEdit ?: pendingExercise ?: LibraryExerciseEntity(name = "", muscleGroup = "Chest", normalizedName = "")
        ExerciseEditDialog(
            exercise = exercise,
            muscleGroups = viewModel.muscleGroups,
            equipmentOptions = viewModel.equipmentOptions,
            exerciseTypes = viewModel.exerciseTypes,
            onDismiss = { 
                showAddDialog = false
                exerciseToEdit = null
                pendingExercise = null
            },
            onSave = { updated ->
                if (exerciseToEdit != null) {
                    viewModel.updateExercise(updated)
                    exerciseToEdit = null
                } else {
                    pendingExercise = updated
                    viewModel.saveExercise(updated)
                }
            }
        )
    }

    if (exerciseToArchive != null) {
        ArchiveConfirmationDialog(
            exerciseName = exerciseToArchive!!.name,
            onDismiss = { exerciseToArchive = null },
            onConfirm = {
                viewModel.archiveExercise(exerciseToArchive!!.id)
                exerciseToArchive = null
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
                        Button(onClick = { resultToShow = null; showAddDialog = false }) {
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
                        Button(onClick = { resultToShow = null; showAddDialog = false }) {
                            Text("Use Existing")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { 
                            pendingExercise?.let { viewModel.saveExercise(it, ignoreSimilarity = true) }
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
fun ExerciseLibraryCard(
    exercise: LibraryExerciseEntity,
    onEdit: (LibraryExerciseEntity) -> Unit,
    onArchive: (LibraryExerciseEntity) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${exercise.muscleGroup} • ${exercise.equipment} • ${exercise.exerciseType}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = { 
                            showMenu = false
                            onEdit(exercise)
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Archive") },
                        onClick = { 
                            showMenu = false
                            onArchive(exercise)
                        },
                        leadingIcon = { Icon(Icons.Default.Archive, null) }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyLibraryState(
    query: String,
    onCreateClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(16.dp))
            Text("No exercises found", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (query.isNotBlank()) {
                TextButton(onClick = onCreateClick) {
                    Text("Create \"$query\"?")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseEditDialog(
    exercise: LibraryExerciseEntity,
    muscleGroups: List<String>,
    equipmentOptions: List<String>,
    exerciseTypes: List<String>,
    onDismiss: () -> Unit,
    onSave: (LibraryExerciseEntity) -> Unit
) {
    var name by remember { mutableStateOf(exercise.name) }
    var muscle by remember { mutableStateOf(exercise.muscleGroup) }
    var equipment by remember { mutableStateOf(exercise.equipment) }
    var type by remember { mutableStateOf(exercise.exerciseType) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (exercise.id == 0L) "Add Exercise" else "Edit Exercise") },
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
                onClick = { onSave(exercise.copy(name = name, muscleGroup = muscle, equipment = equipment, exerciseType = type)) },
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
            modifier = Modifier.menuAnchor().fillMaxWidth()
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
fun ArchiveConfirmationDialog(
    exerciseName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Archive Exercise?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Archive \"$exerciseName\"?")
                Text(
                    "This exercise will no longer appear in your active Exercise Library. Existing workout programs and history will remain unchanged.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Archive")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
