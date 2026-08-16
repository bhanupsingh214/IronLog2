package com.bhanu.ironlog.ui.screens.profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.bhanu.ironlog.data.local.entity.BodyWeightEntry
import com.bhanu.ironlog.data.local.entity.UserProfileEntity
import com.bhanu.ironlog.data.local.entity.WaistEntry
import com.bhanu.ironlog.data.repository.AccountState
import com.bhanu.ironlog.data.util.BodyMetricsCalculator
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    onNavigateToLibrary: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val latestWeight by viewModel.latestWeight.collectAsState()
    val latestWaist by viewModel.latestWaist.collectAsState()
    val weightHistory by viewModel.weightHistory.collectAsState()
    val waistHistory by viewModel.waistHistory.collectAsState()

    val accountState by viewModel.accountState.collectAsState()
    val accountError by viewModel.accountError.collectAsState()
    val cloudState by viewModel.cloudState.collectAsState()
    val cloudRestoreState by viewModel.cloudRestoreState.collectAsState()
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

    var showDobPicker by remember { mutableStateOf(false) }
    var showWeightDialog by remember { mutableStateOf(false) }
    var showWaistDialog by remember { mutableStateOf(false) }
    var showHeightDialog by remember { mutableStateOf(false) }
    var showSexDialog by remember { mutableStateOf(false) }

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

        // About You Section
        SettingsSection(title = "About You") {
            SettingsClickItem(
                title = "Sex",
                subtitle = profile?.sex ?: "Not specified",
                onClick = { showSexDialog = true },
                icon = Icons.Default.Person
            )

            val dobLabel = profile?.dateOfBirth?.let {
                DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(it))
            } ?: "Not specified"

            val ageLabel = profile?.dateOfBirth?.let {
                "(${BodyMetricsCalculator.calculateAge(it)} years old)"
            } ?: ""

            SettingsClickItem(
                title = "Date of Birth",
                subtitle = "$dobLabel $ageLabel",
                onClick = { showDobPicker = true },
                icon = Icons.Default.Cake
            )

            val heightLabel = profile?.heightCm?.let {
                val ftIn = BodyMetricsCalculator.cmToFtIn(it)
                "${ftIn.first}ft ${ftIn.second}in (${String.format(Locale.getDefault(), "%.1f", it)} cm)"
            } ?: "Not specified"

            SettingsClickItem(
                title = "Height",
                subtitle = heightLabel,
                onClick = { showHeightDialog = true },
                icon = Icons.Default.Straighten
            )
        }

        // Body & Progress Section
        SettingsSection(title = "Body & Progress") {
            val weightLabel = latestWeight?.let {
                String.format(Locale.getDefault(), "%.1f kg", it.weightKg)
            } ?: "No data"

            SettingsClickItem(
                title = "Body Weight",
                subtitle = weightLabel,
                onClick = { showWeightDialog = true },
                icon = Icons.Default.MonitorWeight
            )

            if (latestWeight != null && profile?.heightCm != null) {
                val bmi = BodyMetricsCalculator.calculateBMI(latestWeight!!.weightKg, profile!!.heightCm!!)
                val isAdult = profile?.dateOfBirth?.let { BodyMetricsCalculator.isAdult(it) } ?: true

                val interpretation = if (isAdult) {
                    BodyMetricsCalculator.interpretAdultBMI(bmi)
                } else {
                    "Pediatric (Refer to age charts)"
                }

                SettingsClickItem(
                    title = "BMI",
                    subtitle = "${String.format(Locale.getDefault(), "%.1f", bmi)} - $interpretation",
                    onClick = { /* BMI is read-only */ },
                    icon = Icons.Default.Info,
                    enabled = false,
                    showDisabledAlpha = false
                )
            }

            val waistLabel = latestWaist?.let {
                "${String.format(Locale.getDefault(), "%.1f", it.circumferenceCm)} cm"
            } ?: "No data"

            SettingsClickItem(
                title = "Waist Circumference",
                subtitle = waistLabel,
                onClick = { showWaistDialog = true },
                icon = Icons.Default.Straighten
            )
        }

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

            SettingsClickItem(
                title = "Restore from Google Drive",
                subtitle = if (isCloudAuthorized) "Download and restore latest backup" else "Authorize Drive to enable cloud restore",
                onClick = { viewModel.startCloudRestore(context) },
                icon = Icons.Default.CloudDownload,
                loading = cloudRestoreState is CloudRestoreState.Loading,
                enabled = isCloudAuthorized
            )

            if (exportState is ExportState.Error) {
                ErrorMessage((exportState as ExportState.Error).message)
            }

            if (importState is ImportState.Error) {
                ErrorMessage((importState as ImportState.Error).message)
            }

            if (cloudState is CloudBackupState.Error) {
                ErrorMessage((cloudState as CloudBackupState.Error).message)
            }

            if (cloudRestoreState is CloudRestoreState.Error) {
                ErrorMessage((cloudRestoreState as CloudRestoreState.Error).message)
            }

            if (cloudRestoreState is CloudRestoreState.NoBackup) {
                ErrorMessage("No cloud backup found")
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

    if (showSexDialog) {
        AlertDialog(
            onDismissRequest = { showSexDialog = false },
            title = { Text("Select Sex") },
            text = {
                Column {
                    listOf("Male", "Female", "Prefer not to say").forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateProfile(
                                        sex = if (option == "Prefer not to say") null else option,
                                        dob = profile?.dateOfBirth,
                                        heightCm = profile?.heightCm
                                    )
                                    showSexDialog = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (profile?.sex == option) || (profile?.sex == null && option == "Prefer not to say"),
                                onClick = null
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(option)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSexDialog = false }) { Text("Close") }
            }
        )
    }

    if (showDobPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = profile?.dateOfBirth ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDobPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateProfile(
                        sex = profile?.sex,
                        dob = datePickerState.selectedDateMillis,
                        heightCm = profile?.heightCm
                    )
                    showDobPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDobPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showHeightDialog) {
        var feet by remember { mutableIntStateOf(BodyMetricsCalculator.cmToFtIn(profile?.heightCm ?: 170.0).first) }
        var inches by remember { mutableIntStateOf(BodyMetricsCalculator.cmToFtIn(profile?.heightCm ?: 170.0).second) }

        AlertDialog(
            onDismissRequest = { showHeightDialog = false },
            title = { Text("Set Height") },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Feet", style = MaterialTheme.typography.labelSmall)
                        NumberPicker(
                            value = feet,
                            onValueChange = { feet = it },
                            range = 1..8
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Inches", style = MaterialTheme.typography.labelSmall)
                        NumberPicker(
                            value = inches,
                            onValueChange = { inches = it },
                            range = 0..11
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val cm = BodyMetricsCalculator.ftInToCm(feet, inches)
                    viewModel.updateProfile(
                        sex = profile?.sex,
                        dob = profile?.dateOfBirth,
                        heightCm = cm
                    )
                    showHeightDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showHeightDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showWeightDialog) {
        WeightHistoryDialog(
            history = weightHistory,
            onAdd = { weight, date -> viewModel.addWeightEntry(weight, date) },
            onDelete = { viewModel.deleteWeightEntry(it) },
            onDismiss = { showWeightDialog = false }
        )
    }

    if (showWaistDialog) {
        WaistHistoryDialog(
            history = waistHistory,
            onAdd = { cm, date -> viewModel.addWaistEntry(cm, date) },
            onDelete = { viewModel.deleteWaistEntry(it) },
            onDismiss = { showWaistDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightHistoryDialog(
    history: List<BodyWeightEntry>,
    onAdd: (Double, Long) -> Unit,
    onDelete: (BodyWeightEntry) -> Unit,
    onDismiss: () -> Unit
) {
    var showAddWeight by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Weight History", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                IconButton(onClick = { showAddWeight = true }) {
                    Icon(Icons.Default.Add, null)
                }
            }

            Spacer(Modifier.height(16.dp))

            if (history.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No weight records yet", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f, false).heightIn(max = 400.dp)) {
                    items(history) { entry ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("${entry.weightKg} kg", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                Text(DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(entry.timestamp)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            }
                            IconButton(onClick = { onDelete(entry) }) {
                                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddWeight) {
        var weightStr by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddWeight = false },
            title = { Text("Add Weight") },
            text = {
                OutlinedTextField(
                    value = weightStr,
                    onValueChange = { weightStr = it },
                    label = { Text("Weight (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                )
            },
            confirmButton = {
                Button(onClick = {
                    weightStr.toDoubleOrNull()?.let {
                        onAdd(it, System.currentTimeMillis())
                        showAddWeight = false
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddWeight = false }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaistHistoryDialog(
    history: List<WaistEntry>,
    onAdd: (Double, Long) -> Unit,
    onDelete: (WaistEntry) -> Unit,
    onDismiss: () -> Unit
) {
    var showAddWaist by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Waist History", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                IconButton(onClick = { showAddWaist = true }) {
                    Icon(Icons.Default.Add, null)
                }
            }

            Spacer(Modifier.height(16.dp))

            if (history.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No waist records yet", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f, false).heightIn(max = 400.dp)) {
                    items(history) { entry ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("${entry.circumferenceCm} cm", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                Text(DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(entry.timestamp)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            }
                            IconButton(onClick = { onDelete(entry) }) {
                                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddWaist) {
        var waistStr by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddWaist = false },
            title = { Text("Add Waist Circumference") },
            text = {
                OutlinedTextField(
                    value = waistStr,
                    onValueChange = { waistStr = it },
                    label = { Text("Waist (cm)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                )
            },
            confirmButton = {
                Button(onClick = {
                    waistStr.toDoubleOrNull()?.let {
                        onAdd(it, System.currentTimeMillis())
                        showAddWaist = false
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddWaist = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun NumberPicker(value: Int, onValueChange: (Int) -> Unit, range: IntRange) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = { if (value < range.last) onValueChange(value + 1) }) {
            Icon(Icons.Default.KeyboardArrowUp, null)
        }
        Text(text = value.toString(), style = MaterialTheme.typography.headlineMedium)
        IconButton(onClick = { if (value > range.first) onValueChange(value - 1) }) {
            Icon(Icons.Default.KeyboardArrowDown, null)
        }
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
    enabled: Boolean = true,
    showDisabledAlpha: Boolean = !enabled
) {
    val alpha = if (showDisabledAlpha) 0.38f else 1.0f
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
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
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
                tint = MaterialTheme.colorScheme.outline.copy(alpha = alpha)
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
