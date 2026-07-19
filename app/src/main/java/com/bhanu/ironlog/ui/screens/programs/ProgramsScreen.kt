package com.bhanu.ironlog.ui.screens.programs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import com.bhanu.ironlog.data.local.entity.ProgramEntity
import com.bhanu.ironlog.data.local.pojo.ProgramWithStats
import com.bhanu.ironlog.ui.components.SearchBar
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProgramsScreen(
    onNavigateToArchive: () -> Unit,
    onNavigateToWorkoutDays: (Long) -> Unit,
    viewModel: ProgramsViewModel = hiltViewModel(),
) {
    val programs by viewModel.programs.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()

    var showCreateDialog by remember { mutableStateOf(value = false) }
    var programToRename by remember { mutableStateOf<ProgramEntity?>(value = null) }
    var programToDelete by remember { mutableStateOf<ProgramEntity?>(value = null) }

    Scaffold(
        topBar = {
            Column {
                LargeTopAppBar(
                    title = { Text("Programs", fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = onNavigateToArchive) {
                            Icon(Icons.Default.Archive, contentDescription = "Archived")
                        }
                        SortMenu(
                            currentSort = sortOrder,
                            onSortChange = { viewModel.onSortOrderChange(it) }
                        )
                    }
                )
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = "Search programs...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Program")
            }
        }
    ) { padding ->
        if (programs.isEmpty() && searchQuery.isEmpty()) {
            EmptyProgramsState(
                onCreateClick = { showCreateDialog = true },
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
                items(
                    items = programs,
                    key = { it.program.id }
                ) { item ->
                    ProgramItem(
                        item = item,
                        onActivate = { viewModel.activateProgram(item.program.id) },
                        onArchive = { viewModel.archiveProgram(item.program) },
                        onDelete = { programToDelete = item.program },
                        onDuplicate = { viewModel.duplicateProgram(item.program.id) },
                        onRename = { programToRename = item.program },
                        onClick = { onNavigateToWorkoutDays(item.program.id) }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateProgramDialog(
            onDismiss = { showCreateDialog = false },
            onSave = { name ->
                viewModel.addProgram(name)
                showCreateDialog = false
            }
        )
    }

    programToRename?.let { program ->
        RenameDialog(
            currentName = program.name,
            onDismiss = { programToRename = null },
            onConfirm = { newName ->
                viewModel.renameProgram(program, newName)
                programToRename = null
            }
        )
    }

    programToDelete?.let { program ->
        AlertDialog(
            onDismissRequest = { programToDelete = null },
            title = { Text("Delete Program?") },
            text = { Text("Are you sure you want to delete \"${program.name}\"? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProgram(program)
                        programToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { programToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun SortMenu(
    currentSort: ProgramSortOrder,
    onSortChange: (ProgramSortOrder) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ProgramSortOrder.entries.forEach { order ->
                DropdownMenuItem(
                    text = {
                        Text(
                            when (order) {
                                ProgramSortOrder.NAME_ASC -> "Name (A-Z)"
                                ProgramSortOrder.NAME_DESC -> "Name (Z-A)"
                                ProgramSortOrder.MODIFIED_DESC -> "Recently Modified"
                                ProgramSortOrder.MODIFIED_ASC -> "Oldest First"
                                ProgramSortOrder.ACTIVE_FIRST -> "Active First"
                            }
                        )
                    },
                    onClick = {
                        onSortChange(order)
                        expanded = false
                    },
                    trailingIcon = {
                        if (currentSort == order) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun EmptyProgramsState(
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.LibraryBooks,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "No workout programs yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onCreateClick) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Create Program")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramItem(
    item: ProgramWithStats,
    onActivate: (ProgramWithStats) -> Unit,
    onArchive: (ProgramWithStats) -> Unit,
    onDelete: (ProgramWithStats) -> Unit,
    onDuplicate: (ProgramWithStats) -> Unit,
    onRename: (ProgramWithStats) -> Unit,
    onClick: (ProgramWithStats) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick(item) },
                onLongClick = { showMenu = true }
            ),
        shape = MaterialTheme.shapes.large,
        colors = if (item.program.isActive) {
            CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else CardDefaults.elevatedCardColors()
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
                            text = item.program.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (item.program.isActive) {
                            Spacer(Modifier.width(8.dp))
                            SuggestionChip(
                                onClick = { },
                                label = { Text("Active", fontSize = 10.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    labelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                border = null,
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${item.dayCount} Workout Days • ${item.exerciseCount} Exercises",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Set Active") },
                            onClick = { showMenu = false; onActivate(item) },
                            leadingIcon = { Icon(Icons.Default.CheckCircle, null) },
                            enabled = !item.program.isActive
                        )
                        DropdownMenuItem(
                            text = { Text("Rename") },
                            onClick = { showMenu = false; onRename(item) },
                            leadingIcon = { Icon(Icons.Default.Edit, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate") },
                            onClick = { showMenu = false; onDuplicate(item) },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Archive") },
                            onClick = { showMenu = false; onArchive(item) },
                            leadingIcon = { Icon(Icons.Default.Archive, null) }
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

            Spacer(Modifier.height(12.dp))
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("en", "IN"))
            Text(
                text = "Last modified: ${dateFormat.format(Date(item.program.lastModifiedAt))}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun CreateProgramDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Program") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Program Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onSave(name) },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun RenameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Program") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("New Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
