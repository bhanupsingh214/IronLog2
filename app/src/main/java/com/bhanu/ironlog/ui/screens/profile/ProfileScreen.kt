package com.bhanu.ironlog.ui.screens.profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun ProfileScreen(
    onNavigateToLibrary: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val exportState by viewModel.exportState.collectAsState()
    val importState by viewModel.importState.collectAsState()

    var showImportConfirmation by remember { mutableStateOf(false) }

    val appVersion = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
    } catch (e: Exception) { "1.0.0" }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let { destination ->
            if (exportState is ExportState.Success) {
                val file = (exportState as ExportState.Success).file
                try {
                    context.contentResolver.openOutputStream(destination)?.use { output ->
                        file.inputStream().use { input ->
                            input.copyTo(output)
                        }
                    }
                    viewModel.onExportHandled()
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.startImport(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.exportEvent.collect { event ->
            when (event) {
                is ExportEvent.RequestSave -> {
                    val timestamp = System.currentTimeMillis()
                    createDocumentLauncher.launch("ironlog_backup_$timestamp.ironlog")
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.importEvent.collect { event ->
            when (event) {
                is ImportEvent.RestoreComplete -> {
                    Toast.makeText(context, "Backup restored successfully!", Toast.LENGTH_LONG).show()
                    viewModel.onImportHandled()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Profile & Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        SettingsSection(title = "General") {
            SettingsClickItem(
                title = "Exercise Library",
                subtitle = "Manage your training vocabulary",
                onClick = onNavigateToLibrary
            )
        }

        SettingsSection(title = "Backup & Data") {
            SettingsClickItem(
                title = "Export Backup",
                subtitle = "Save your training data to a file",
                onClick = { viewModel.startExport(appVersion) },
                icon = Icons.Default.Download,
                loading = exportState is ExportState.Loading
            )

            SettingsClickItem(
                title = "Import Backup",
                subtitle = "Restore data from an .ironlog file",
                onClick = { showImportConfirmation = true },
                icon = Icons.Default.Upload,
                loading = importState is ImportState.Loading
            )

            if (exportState is ExportState.Error) {
                ErrorMessage((exportState as ExportState.Error).message)
            }

            if (importState is ImportState.Error) {
                ErrorMessage((importState as ImportState.Error).message)
            }

            if (importState is ImportState.Success) {
                Text(
                    text = "Backup restored successfully!",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        settings?.let { s ->
            SettingsSection(title = "Workout Settings") {
                SettingsToggleItem(
                    title = "Auto Start Rest Timer",
                    subtitle = "Timer starts after completing a working set",
                    checked = s.autoStartTimer,
                    onCheckedChange = { viewModel.updateSettings(s.copy(autoStartTimer = it)) }
                )
                
                SettingsSliderItem(
                    title = "Default Rest Duration",
                    value = s.defaultRestTimerSeconds,
                    onValueChange = { viewModel.updateSettings(s.copy(defaultRestTimerSeconds = it)) },
                    valueRange = 30f..300f,
                    steps = 17 // Every 15 seconds: (300-30)/15 - 1 = 17 steps
                )

                SettingsToggleItem(
                    title = "Haptic Feedback",
                    subtitle = "Vibrate when timer completes",
                    checked = s.hapticFeedback,
                    onCheckedChange = { viewModel.updateSettings(s.copy(hapticFeedback = it)) }
                )

                SettingsToggleItem(
                    title = "Sound Alert",
                    subtitle = "Play chime when timer completes",
                    checked = s.soundAlert,
                    onCheckedChange = { viewModel.updateSettings(s.copy(soundAlert = it)) }
                )
            }
        }
    }

    if (showImportConfirmation) {
        AlertDialog(
            onDismissRequest = { showImportConfirmation = false },
            title = { Text("Restore Backup?") },
            text = { Text("This will permanently replace all your current programs and workout history. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showImportConfirmation = false
                        openDocumentLauncher.launch(arrayOf("application/octet-stream", "*/*"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirm Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ErrorMessage(message: String) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(8.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SettingsToggleItem(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingsClickItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    icon: ImageVector = Icons.Default.ChevronRight,
    loading: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !loading, onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        } else {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun SettingsSliderItem(title: String, value: Int, onValueChange: (Int) -> Unit, valueRange: ClosedFloatingPointRange<Float>, steps: Int) {
    Column(modifier = Modifier.padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = "$value sec", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = valueRange,
            steps = steps
        )
    }
}
