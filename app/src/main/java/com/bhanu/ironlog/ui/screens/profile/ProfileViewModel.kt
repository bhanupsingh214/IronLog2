package com.bhanu.ironlog.ui.screens.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.ironlog.data.local.entity.WorkoutSettingsEntity
import com.bhanu.ironlog.data.repository.BackupRepository
import com.bhanu.ironlog.data.repository.RestoreRepository
import com.bhanu.ironlog.data.repository.WorkoutSessionRepository
import com.bhanu.ironlog.data.service.ExportService
import com.bhanu.ironlog.data.service.ImportService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: WorkoutSessionRepository,
    private val backupRepository: BackupRepository,
    private val restoreRepository: RestoreRepository,
    private val exportService: ExportService,
    private val importService: ImportService
) : ViewModel() {

    val settings: StateFlow<WorkoutSettingsEntity?> = repository.getWorkoutSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    private val _exportEvent = MutableSharedFlow<ExportEvent>()
    val exportEvent: SharedFlow<ExportEvent> = _exportEvent.asSharedFlow()

    private val _importEvent = MutableSharedFlow<ImportEvent>()
    val importEvent: SharedFlow<ImportEvent> = _importEvent.asSharedFlow()

    fun updateSettings(newSettings: WorkoutSettingsEntity) {
        viewModelScope.launch {
            repository.updateWorkoutSettings(newSettings)
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

    fun startImport(uri: Uri) {
        if (_importState.value is ImportState.Loading) return

        viewModelScope.launch {
            _importState.value = ImportState.Loading
            try {
                val payload = importService.parseBackup(uri)
                restoreRepository.restoreBackup(payload)
                _importState.value = ImportState.Success
                _importEvent.emit(ImportEvent.RestoreComplete)
            } catch (e: Exception) {
                _importState.value = ImportState.Error(e.message ?: "Unknown import error")
            }
        }
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
    object Success : ImportState()
    data class Error(val message: String) : ImportState()
}

sealed class ExportEvent {
    data class RequestSave(val file: File) : ExportEvent()
}

sealed class ImportEvent {
    object RestoreComplete : ImportEvent()
}
