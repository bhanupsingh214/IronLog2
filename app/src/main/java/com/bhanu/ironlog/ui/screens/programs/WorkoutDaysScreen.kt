package com.bhanu.ironlog.ui.screens.programs

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bhanu.ironlog.data.local.entity.WorkoutDayEntity
import com.bhanu.ironlog.data.local.pojo.WorkoutDayWithStats
import com.bhanu.ironlog.ui.components.ErrorScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDaysScreen(
    onBack: () -> Unit,
    onNavigateToExercises: (Long) -> Unit,
    viewModel: WorkoutDaysViewModel = hiltViewModel(),
) {
    if (!viewModel.isArgumentValid) {
        ErrorScreen(onBack = onBack, message = "Invalid Program data")
        return
    }

    val workoutDays by viewModel.workoutDays.collectAsState()
    var showAddDialog by remember { mutableStateOf(value = false) }
    var dayToEdit by remember { mutableStateOf<WorkoutDayEntity?>(value = null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout Days", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Workout Day")
            }
        }
    ) { padding ->
        if (workoutDays.isEmpty()) {
            EmptyWorkoutDaysState(
                onCreateClick = { showAddDialog = true },
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
                itemsIndexed(
                    items = workoutDays,
                    key = { _, item -> item.day.id }
                ) { index, item ->
                    WorkoutDayItem(
                        item = item,
                        isFirst = index == 0,
                        isLast = (index == workoutDays.size - 1),
                        onEdit = { dayToEdit = item.day },
                        onDelete = { viewModel.deleteWorkoutDay(item.day) },
                        onDuplicate = { viewModel.duplicateWorkoutDay(item.day.id) },
                        onMoveUp = { viewModel.moveDayUp(item.day.id) },
                        onMoveDown = { viewModel.moveDayDown(item.day.id) },
                        onToggleEnabled = { enabled ->
                            viewModel.updateWorkoutDay(item.day.copy(isEnabled = enabled))
                        },
                        onClick = { onNavigateToExercises(item.day.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        WorkoutDayDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, notes ->
                viewModel.addWorkoutDay(name, notes)
                showAddDialog = false
            }
        )
    }

    dayToEdit?.let { day ->
        WorkoutDayDialog(
            initialName = day.name,
            initialNotes = day.notes,
            onDismiss = { dayToEdit = null },
            onSave = { name, notes ->
                viewModel.updateWorkoutDay(day.copy(name = name, notes = notes))
                dayToEdit = null
            }
        )
    }
}

@Composable
fun WorkoutDayItem(
    item: WorkoutDayWithStats,
    isFirst: Boolean,
    isLast: Boolean,
    onEdit: (WorkoutDayWithStats) -> Unit,
    onDelete: (WorkoutDayWithStats) -> Unit,
    onDuplicate: (WorkoutDayWithStats) -> Unit,
    onMoveUp: (WorkoutDayWithStats) -> Unit,
    onMoveDown: (WorkoutDayWithStats) -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onClick: (WorkoutDayWithStats) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(item) },
        shape = MaterialTheme.shapes.large,
        colors = if (item.day.isEnabled) CardDefaults.elevatedCardColors()
        else CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.day.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (item.day.isEnabled) MaterialTheme.colorScheme.onSurface 
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Switch(
                            checked = item.day.isEnabled,
                            onCheckedChange = onToggleEnabled,
                            modifier = Modifier.scale(0.7f)
                        )
                    }
                    if (item.day.notes.isNotBlank()) {
                        Text(
                            text = item.day.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${item.day.estimatedDurationMinutes}m",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(Modifier.width(12.dp))
                        Icon(Icons.Default.FitnessCenter, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${item.exerciseCount} Exercises",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = { showMenu = false; onEdit(item) },
                            leadingIcon = { Icon(Icons.Default.Edit, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate") },
                            onClick = { showMenu = false; onDuplicate(item) },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Move Up") },
                            onClick = { showMenu = false; onMoveUp(item) },
                            leadingIcon = { Icon(Icons.Default.ArrowUpward, null) },
                            enabled = !isFirst
                        )
                        DropdownMenuItem(
                            text = { Text("Move Down") },
                            onClick = { showMenu = false; onMoveDown(item) },
                            leadingIcon = { Icon(Icons.Default.ArrowDownward, null) },
                            enabled = !isLast
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = { showMenu = false; onDelete(item) },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyWorkoutDaysState(
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "No workout days added",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onCreateClick) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Add Workout Day")
            }
        }
    }
}

@Composable
fun WorkoutDayDialog(
    initialName: String = "",
    initialNotes: String = "",
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var notes by remember { mutableStateOf(initialNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialName.isEmpty()) "New Workout Day" else "Edit Workout Day") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Day Name") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. Push A, Monday") }
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, notes) }, enabled = name.isNotBlank()) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
