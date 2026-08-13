package com.bhanu.ironlog.ui.screens.profile

import android.content.Context
import android.content.IntentSender
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.ironlog.data.local.PreferenceStorage
import com.bhanu.ironlog.data.local.backup.BackupPayload
import com.bhanu.ironlog.data.local.entity.WorkoutSettingsEntity
import com.bhanu.ironlog.data.model.cloud.CloudResult
import com.bhanu.ironlog.data.repository.*
import com.bhanu.ironlog.data.service.CloudStorageService
import com.bhanu.ironlog.data.service.ExportService
import com.bhanu.ironlog.data.service.ImportService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: WorkoutSessionRepository,
    private val backupRepository: BackupRepository,
    private val restoreRepository: RestoreRepository,
    private val exportService: ExportService,
    private val importService: ImportService,
    private val accountRepository: AccountRepository,
    private val cloudStorageService: CloudStorageService,
    private val preferenceStorage: PreferenceStorage
) : ViewModel() {

    val settings: StateFlow<WorkoutSettingsEntity?> = repository.getWorkoutSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val accountState = accountRepository.accountState

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    private val _cloudState = MutableStateFlow<CloudBackupState>(CloudBackupState.Idle)
    val cloudState: StateFlow<CloudBackupState> = _cloudState.asStateFlow()

    private val _exportEvent = MutableSharedFlow<ExportEvent>()
    val exportEvent: SharedFlow<ExportEvent> = _exportEvent.asSharedFlow()

    private val _importEvent = MutableSharedFlow<ImportEvent>()
    val importEvent: SharedFlow<ImportEvent> = _importEvent.asSharedFlow()

    private val _authEvent = MutableSharedFlow<AuthEvent>()
    val authEvent: SharedFlow<AuthEvent> = _authEvent.asSharedFlow()

    val lastCloudBackup = preferenceStorage.lastCloudBackupTimestamp
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    private val _accountError = MutableStateFlow<String?>(null)
    val accountError: StateFlow<String?> = _accountError.asStateFlow()

    fun updateSettings(newSettings: WorkoutSettingsEntity) {
        viewModelScope.launch {
            repository.updateWorkoutSettings(newSettings)
        }
    }

    fun startSignIn(activityContext: Context) {
        viewModelScope.launch {
            _accountError.value = null
            val result = accountRepository.signIn(activityContext)
            if (result is CloudResult.Error) {
                _accountError.value = result.message
            }
        }
    }

    fun startDriveAuthorization() {
        viewModelScope.launch {
            _accountError.value = null
            val result = accountRepository.authorizeDrive()
            if (result is CloudResult.Success && result.data.pendingIntent != null) {
                _authEvent.emit(AuthEvent.LaunchResolution(result.data.pendingIntent!!.intentSender))
            } else if (result is CloudResult.Error) {
                _accountError.value = result.message
            }
        }
    }

    fun onAuthorizationResult(intent: android.content.Intent?) {
        viewModelScope.launch {
            _accountError.value = null
            val result = accountRepository.processAuthorizationResult(intent)
            if (result is CloudResult.Error) {
                _accountError.value = result.message
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            accountRepository.signOut()
        }
    }

    fun refreshAuth() {
        viewModelScope.launch {
            accountRepository.refreshAuth()
        }
    }

    fun startExport(appVersion: String) {
        if (_exportState.value is ExportState.Loading) return

        viewModelScope.launch {
            _exportState.value = ExportState.Loading
            try {
                val payload = backupRepository.getFullBackupPayload(appVersion)
                val backupFile = exportService.createBackupZip(payload)
                _exportState.value = ExportState.Success(backupFile)
                _exportEvent.emit(ExportEvent.RequestSave(backupFile))
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(e.message ?: "Unknown export error")
            }
        }
    }

    fun startCloudBackup(appVersion: String) {
        if (_cloudState.value is CloudBackupState.Loading) return

        viewModelScope.launch {
            _cloudState.value = CloudBackupState.Loading
            try {
                val payload = backupRepository.getFullBackupPayload(appVersion)
                val backupFile = exportService.createBackupZip(payload)
                val result = cloudStorageService.uploadBackup(backupFile)
                when (result) {
                    is CloudResult.Success -> {
                        _cloudState.value = CloudBackupState.Success(System.currentTimeMillis())
                    }
                    is CloudResult.Error -> {
                        _cloudState.value = CloudBackupState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _cloudState.value = CloudBackupState.Error(e.message ?: "Cloud backup failed")
            }
        }
    }

    fun startImport(uri: Uri) {
        if (_importState.value is ImportState.Loading) return

        viewModelScope.launch {
            _importState.value = ImportState.Loading
            try {
                val payload = importService.parseBackup(uri)
                _importState.value = ImportState.Ready(payload)
            } catch (e: Exception) {
                _importState.value = ImportState.Error(e.message ?: "Unable to read backup")
            }
        }
    }

    fun confirmImport() {
        val state = _importState.value
        if (state !is ImportState.Ready) return

        viewModelScope.launch {
            _importState.value = ImportState.Loading
            try {
                restoreRepository.restoreBackup(state.payload)
                _importState.value = ImportState.Success
                _importEvent.emit(ImportEvent.RestoreComplete)
            } catch (e: Exception) {
                _importState.value = ImportState.Error(e.message ?: "Unable to restore backup")
            }
        }
    }

    fun cancelImport() {
        _importState.value = ImportState.Idle
    }

    fun onExportHandled() {
        _exportState.value = ExportState.Idle
    }

    fun onImportHandled() {
        _importState.value = ImportState.Idle
    }
}

sealed class ExportState {
    object Idle : ExportState()
    object Loading : ExportState()
    data class Success(val file: File) : ExportState()
    data class Error(val message: String) : ExportState()
}

sealed class ImportState {
    object Idle : ImportState()
    object Loading : ImportState()
    data class Ready(val payload: BackupPayload) : ImportState()
    object Success : ImportState()
    data class Error(val message: String) : ImportState()
}

sealed class ExportEvent {
    data class RequestSave(val file: File) : ExportEvent()
}

sealed class ImportEvent {
    object RestoreComplete : ImportEvent()
}

sealed class AuthEvent {
    data class LaunchResolution(val intentSender: IntentSender) : AuthEvent()
}

sealed class CloudBackupState {
    object Idle : CloudBackupState()
    object Loading : CloudBackupState()
    data class Success(val timestamp: Long) : CloudBackupState()
    data class Error(val message: String) : CloudBackupState()
}
