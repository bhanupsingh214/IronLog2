package com.bhanu.ironlog.ui.screens.profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bhanu.ironlog.data.repository.AccountState
import java.text.DateFormat
import java.util.Date

@Composable
fun ProfileScreen(
    onNavigateToLibrary: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val accountState by viewModel.accountState.collectAsState()
    val accountError by viewModel.accountError.collectAsState()
    val cloudState by viewModel.cloudState.collectAsState()
    val exportState by viewModel.exportState.collectAsState()
    val importState by viewModel.importState.collectAsState()

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

    val authLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            viewModel.onAuthorizationResult(result.data)
        }
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
        viewModel.authEvent.collect { event ->
            when (event) {
                is AuthEvent.LaunchResolution -> {
                    authLauncher.launch(IntentSenderRequest.Builder(event.intentSender).build())
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

        // Account Section
        SettingsSection(title = "Account") {
            when (val state = accountState) {
                is AccountState.SignedIn -> {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = state.displayName ?: "User", style = MaterialTheme.typography.bodyLarge)
                                Text(text = state.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            TextButton(onClick = { viewModel.signOut() }) {
                                Text("Sign Out")
                            }
                        }

                        if (!state.isDriveAuthorized) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                            SettingsClickItem(
                                title = "Authorize Google Drive",
                                subtitle = "Grant permission to save backups",
                                onClick = { viewModel.startDriveAuthorization() },
                                icon = Icons.Default.AddModerator
                            )
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Google Drive Authorized",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                is AccountState.SignedOut -> {
                    SettingsClickItem(
                        title = "Sign in with Google",
                        subtitle = "Enable cloud features",
                        onClick = { viewModel.startSignIn(context) },
                        icon = Icons.Default.Login
                    )
                }
                AccountState.Loading -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
            accountError?.let { ErrorMessage(it) }
        }

        SettingsSection(title = "General") {
            SettingsClickItem(
                title = "Exercise Library",
                subtitle = "Manage your training vocabulary",
                onClick = onNavigateToLibrary
            )
        }

        SettingsSection(title = "Backup & Data") {
            SettingsClickItem(
                title = "Local Export",
                subtitle = "Save training data to a file",
                onClick = { viewModel.startExport(appVersion) },
                icon = Icons.Default.Save,
                loading = exportState is ExportState.Loading
            )

            SettingsClickItem(
                title = "Local Import",
                subtitle = "Restore from an .ironlog file",
                onClick = {
                    openDocumentLauncher.launch(arrayOf("application/octet-stream", "*/*"))
                },
                icon = Icons.Default.FileUpload,
                loading = importState is ImportState.Loading
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Cloud Backup Item
            val isCloudAuthorized = (accountState as? AccountState.SignedIn)?.isDriveAuthorized == true
            val currentLastBackup by viewModel.lastCloudBackup.collectAsState()
            val lastBackupLabel = if (currentLastBackup > 0) {
                "Last: " + DateFormat.getDateTimeInstance().format(Date(currentLastBackup))
            } else {
                "No cloud backups yet"
            }

            SettingsClickItem(
                title = "Back up to Google Drive",
                subtitle = if (isCloudAuthorized) lastBackupLabel else "Authorize Drive to enable cloud backup",
                onClick = { viewModel.startCloudBackup(appVersion) },
                icon = Icons.Default.CloudUpload,
                loading = cloudState is CloudBackupState.Loading,
                enabled = isCloudAuthorized
            )

            if (exportState is ExportState.Error) {
                ErrorMessage((exportState as ExportState.Error).message)
            }

            if (importState is ExportState.Error) {
                ErrorMessage((importState as ImportState.Error).message)
            }

            if (cloudState is CloudBackupState.Error) {
                ErrorMessage((cloudState as CloudBackupState.Error).message)
            }

            if (importState is ImportState.Success) {
                Text(
                    text = "Backup restored successfully!",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            if (cloudState is CloudBackupState.Success) {
                Text(
                    text = "Cloud backup completed!",
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

    val readyImport = importState as? ImportState.Ready

    if (readyImport != null) {
        val metadata = readyImport.payload.metadata
        val backupTimestamp = remember(metadata.timestamp) {
            DateFormat.getDateTimeInstance(
                DateFormat.MEDIUM,
                DateFormat.SHORT
            ).format(Date(metadata.timestamp))
        }

        AlertDialog(
            onDismissRequest = { viewModel.cancelImport() },
            title = { Text("Restore Backup?") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Backup information",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text("Created: $backupTimestamp")
                    Text("App version: ${metadata.appVersion}")
                    Text("Programs: ${metadata.programCount}")
                    Text("Workout sessions: ${metadata.sessionCount}")
                    Text("Backup version: ${metadata.version}")
                    HorizontalDivider()
                    Text(
                        text = "This will permanently replace your current programs and workout history. This action cannot be undone."
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmImport() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelImport() }) {
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
    loading: Boolean = false,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !loading && enabled, onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            )
        }
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outline.copy(alpha = 0.38f)
            )
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
