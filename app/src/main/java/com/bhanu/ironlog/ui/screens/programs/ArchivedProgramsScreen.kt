package com.bhanu.ironlog.ui.screens.programs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bhanu.ironlog.data.local.pojo.ProgramWithStats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedProgramsScreen(
    onBack: () -> Unit,
    viewModel: ProgramsViewModel = hiltViewModel()
) {
    val archivedPrograms by viewModel.archivedPrograms.collectAsState()
    var programToDelete by remember { mutableStateOf<ProgramWithStats?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Archived Programs") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (archivedPrograms.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No archived programs", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(archivedPrograms, key = { it.program.id }) { item ->
                    ArchivedProgramItem(
                        item = item,
                        onRestore = { viewModel.restoreProgram(it.program) },
                        onDelete = { programToDelete = it }
                    )
                }
            }
        }
    }

    programToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { programToDelete = null },
            title = { Text("Delete Program?") },
            text = { Text("This will permanently delete \"${item.program.name}\" and all its workouts. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProgram(item.program)
                        programToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { programToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun ArchivedProgramItem(
    item: ProgramWithStats,
    onRestore: (ProgramWithStats) -> Unit,
    onDelete: (ProgramWithStats) -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.program.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${item.dayCount} days • ${item.exerciseCount} exercises", style = MaterialTheme.typography.bodySmall)
            }
            Row {
                IconButton(onClick = { onRestore(item) }) {
                    Icon(Icons.Default.Restore, contentDescription = "Restore", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { onDelete(item) }) {
                    Icon(Icons.Default.DeleteForever, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
