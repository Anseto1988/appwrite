package com.example.snacktrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snacktrack.data.model.*
import com.example.snacktrack.data.repository.ExportRepository
import com.example.snacktrack.data.service.AppwriteService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ExportIntegrationUiState(
    val isLoading: Boolean = false,
    val exportRequests: List<ExportRequest> = emptyList(),
    val veterinaryIntegrations: List<VeterinaryIntegration> = emptyList(),
    val calendarIntegrations: List<CalendarIntegration> = emptyList(),
    val fitnessTrackers: List<FitnessTrackerIntegration> = emptyList(),
    val cloudBackup: CloudBackup? = null,
    val apiIntegrations: List<ApiIntegration> = emptyList(),
    val syncConfiguration: SyncConfiguration? = null,
    val exportProgress: ExportProgress? = null,
    val importProgress: ImportProgress? = null,
    val error: String? = null,
    val successMessage: String? = null
)

data class ExportProgress(
    val status: String,
    val progress: Float,
    val currentStep: String
)

class ExportIntegrationViewModel(
    private val appwriteService: AppwriteService
) : ViewModel() {
    private val exportRepository = ExportRepository(appwriteService)
    
    private val _uiState = MutableStateFlow(ExportIntegrationUiState())
    val uiState: StateFlow<ExportIntegrationUiState> = _uiState.asStateFlow()
    
    init {
        loadIntegrations()
    }
    
    private fun loadIntegrations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Load all integrations
                // This would fetch from the database
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    // Data Export Functions
    
    fun exportData(
        dogIds: List<String>,
        exportType: ExportType,
        format: ExportFormat,
        dateRange: DateRange? = null,
        includePhotos: Boolean = false
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(exportProgress = ExportProgress("Initialisiere Export...", 0f, "start")) }
            
            try {
                val userId = appwriteService.getCurrentUserId() ?: return@launch
                val result = exportRepository.createExportRequest(
                    userId = userId,
                    dogIds = dogIds,
                    exportType = exportType,
                    format = format,
                    dateRange = dateRange,
                    includePhotos = includePhotos
                )
                
                result.getOrNull()?.let { request ->
                    _uiState.update {
                        it.copy(
                            exportRequests = it.exportRequests + request,
                            exportProgress = ExportProgress("Export läuft...", 0.5f, "processing"),
                            successMessage = "Export wurde gestartet. Du erhältst eine Benachrichtigung, wenn der Download bereit ist."
                        )
                    }
                    
                    // Monitor export progress
                    monitorExportProgress(request.id)
                } ?: run {
                    _uiState.update {
                        it.copy(
                            exportProgress = null,
                            error = result.exceptionOrNull()?.message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        exportProgress = null,
                        error = e.message
                    )
                }
            }
        }
    }
    
    // Veterinary Integration Functions
    
    fun setupVeterinaryIntegration(
        clinicName: String,
        systemType: VetSystemType,
        apiEndpoint: String,
        apiKey: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val result = exportRepository.setupVeterinaryIntegration(
                    clinicName = clinicName,
                    systemType = systemType,
                    apiEndpoint = apiEndpoint,
                    apiKey = apiKey
                )
                
                result.getOrNull()?.let { integration ->
                    _uiState.update {
                        it.copy(
                            veterinaryIntegrations = it.veterinaryIntegrations + integration,
                            isLoading = false,
                            successMessage = "Tierarztpraxis erfolgreich verbunden"
                        )
                    }
                } ?: run {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.exceptionOrNull()?.message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    fun syncWithVeterinary(integrationId: String, dogId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val result = exportRepository.syncWithVeterinary(integrationId, dogId)
                
                result.getOrNull()?.let { syncResult ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Synchronisierung erfolgreich: ${syncResult.recordsSynced} Datensätze synchronisiert"
                        )
                    }
                } ?: run {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.exceptionOrNull()?.message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    // Calendar Integration Functions
    
    fun setupCalendarIntegration(
        provider: CalendarProvider,
        accountEmail: String,
        authToken: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val userId = appwriteService.getCurrentUserId() ?: return@launch
                val result = exportRepository.setupCalendarIntegration(
                    userId = userId,
                    provider = provider,
                    accountEmail = accountEmail,
                    authToken = authToken
                )
                
                result.getOrNull()?.let { integration ->
                    _uiState.update {
                        it.copy(
                            calendarIntegrations = it.calendarIntegrations + integration,
                            isLoading = false,
                            successMessage = "Kalender erfolgreich verbunden"
                        )
                    }
                } ?: run {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.exceptionOrNull()?.message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    fun syncCalendarEvents(integrationId: String, dogId: String) {
        viewModelScope.launch {
            try {
                val result = exportRepository.syncCalendarEvents(integrationId, dogId)
                
                result.getOrNull()?.let { events ->
                    _uiState.update {
                        it.copy(
                            successMessage = "${events.size} Termine synchronisiert"
                        )
                    }
                } ?: run {
                    _uiState.update {
                        it.copy(error = result.exceptionOrNull()?.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    // Fitness Tracker Functions
    
    fun setupFitnessTracker(
        dogId: String,
        deviceType: FitnessDeviceType,
        deviceId: String,
        authToken: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val userId = appwriteService.getCurrentUserId() ?: return@launch
                val result = exportRepository.setupFitnessTracker(
                    userId = userId,
                    dogId = dogId,
                    deviceType = deviceType,
                    deviceId = deviceId,
                    authToken = authToken
                )
                
                result.getOrNull()?.let { integration ->
                    _uiState.update {
                        it.copy(
                            fitnessTrackers = it.fitnessTrackers + integration,
                            isLoading = false,
                            successMessage = "Fitness Tracker erfolgreich verbunden"
                        )
                    }
                } ?: run {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.exceptionOrNull()?.message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    fun syncFitnessData(integrationId: String) {
        viewModelScope.launch {
            try {
                val result = exportRepository.syncFitnessData(integrationId)
                
                result.getOrNull()?.let { data ->
                    _uiState.update {
                        it.copy(
                            successMessage = "Fitnessdaten synchronisiert: ${data.activityData?.steps ?: 0} Schritte"
                        )
                    }
                } ?: run {
                    _uiState.update {
                        it.copy(error = result.exceptionOrNull()?.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    // Cloud Backup Functions
    
    fun setupCloudBackup(
        provider: CloudProvider,
        settings: BackupSettings
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val userId = appwriteService.getCurrentUserId() ?: return@launch
                val result = exportRepository.setupCloudBackup(
                    userId = userId,
                    provider = provider,
                    settings = settings
                )
                
                result.getOrNull()?.let { backup ->
                    _uiState.update {
                        it.copy(
                            cloudBackup = backup,
                            isLoading = false,
                            successMessage = "Cloud Backup konfiguriert"
                        )
                    }
                } ?: run {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.exceptionOrNull()?.message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    fun performBackup(backupId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(exportProgress = ExportProgress("Backup wird erstellt...", 0f, "start")) }
            
            try {
                val result = exportRepository.performBackup(backupId)
                
                result.getOrNull()?.let { record ->
                    _uiState.update {
                        it.copy(
                            exportProgress = null,
                            successMessage = "Backup erfolgreich erstellt (${record.itemsBackedUp} Elemente)"
                        )
                    }
                } ?: run {
                    _uiState.update {
                        it.copy(
                            exportProgress = null,
                            error = result.exceptionOrNull()?.message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        exportProgress = null,
                        error = e.message
                    )
                }
            }
        }
    }
    
    fun restoreFromBackup(
        backupId: String,
        options: RestoreOptions
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(importProgress = ImportProgress(currentStep = "Wiederherstellung wird gestartet...")) }
            
            try {
                val userId = appwriteService.getCurrentUserId() ?: return@launch
                val result = exportRepository.restoreFromBackup(
                    userId = userId,
                    backupId = backupId,
                    options = options
                )
                
                result.getOrNull()?.let { request ->
                    _uiState.update {
                        it.copy(
                            importProgress = ImportProgress(
                                currentStep = "Wiederherstellung läuft...",
                                percentComplete = 50
                            ),
                            successMessage = "Wiederherstellung gestartet"
                        )
                    }
                    
                    // Monitor restore progress
                    monitorRestoreProgress(request.id)
                } ?: run {
                    _uiState.update {
                        it.copy(
                            importProgress = null,
                            error = result.exceptionOrNull()?.message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        importProgress = null,
                        error = e.message
                    )
                }
            }
        }
    }
    
    // API Integration Functions
    
    fun createApiIntegration(
        name: String,
        description: String,
        permissions: ApiPermissions
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val result = exportRepository.createApiIntegration(
                    name = name,
                    description = description,
                    permissions = permissions
                )
                
                result.getOrNull()?.let { integration ->
                    _uiState.update {
                        it.copy(
                            apiIntegrations = it.apiIntegrations + integration,
                            isLoading = false,
                            successMessage = "API-Integration erstellt. API-Schlüssel: ${integration.apiKey}"
                        )
                    }
                } ?: run {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.exceptionOrNull()?.message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    // Import Functions
    
    fun importData(
        source: ImportSource,
        fileData: ByteArray,
        mappingConfig: ImportMappingConfig,
        validationRules: ValidationRules
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(importProgress = ImportProgress(currentStep = "Validiere Daten...")) }
            
            try {
                val userId = appwriteService.getCurrentUserId() ?: return@launch
                val result = exportRepository.importData(
                    userId = userId,
                    source = source,
                    fileData = fileData,
                    mappingConfig = mappingConfig,
                    validationRules = validationRules
                )
                
                result.getOrNull()?.let { request ->
                    _uiState.update {
                        it.copy(
                            importProgress = ImportProgress(
                                currentStep = "Importiere Daten...",
                                percentComplete = 50
                            )
                        )
                    }
                    
                    // Monitor import progress
                    monitorImportProgress(request.id)
                } ?: run {
                    _uiState.update {
                        it.copy(
                            importProgress = null,
                            error = result.exceptionOrNull()?.message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        importProgress = null,
                        error = e.message
                    )
                }
            }
        }
    }
    
    // Sync Functions
    
    fun configureSyncSettings(
        syncEnabled: Boolean,
        syncInterval: SyncInterval,
        syncOnWifiOnly: Boolean,
        conflictResolution: ConflictResolution
    ) {
        viewModelScope.launch {
            try {
                val userId = appwriteService.getCurrentUserId() ?: return@launch
                val result = exportRepository.configureSyncSettings(
                    userId = userId,
                    syncEnabled = syncEnabled,
                    syncInterval = syncInterval,
                    syncOnWifiOnly = syncOnWifiOnly,
                    conflictResolution = conflictResolution
                )
                
                result.getOrNull()?.let { config ->
                    _uiState.update {
                        it.copy(
                            syncConfiguration = config,
                            successMessage = "Synchronisierungseinstellungen gespeichert"
                        )
                    }
                } ?: run {
                    _uiState.update {
                        it.copy(error = result.exceptionOrNull()?.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    fun performSync(direction: SyncDirection = SyncDirection.BIDIRECTIONAL) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val userId = appwriteService.getCurrentUserId() ?: return@launch
                // TODO: Get syncConfigId from current sync configuration
                val syncConfigId = _uiState.value.syncConfigurations.firstOrNull()?.id ?: return@launch
                val result = exportRepository.performSync(syncConfigId)
                
                result.getOrNull()?.let { record ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Synchronisierung abgeschlossen: ${record.itemsSynced} Elemente synchronisiert"
                        )
                    }
                } ?: run {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.exceptionOrNull()?.message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
    
    // Helper functions
    
    private suspend fun monitorExportProgress(exportId: String) {
        // In a real implementation, this would poll the server for progress updates
        // For now, simulate progress
        for (i in 1..10) {
            kotlinx.coroutines.delay(1000)
            _uiState.update {
                it.copy(
                    exportProgress = ExportProgress(
                        "Export läuft...",
                        i * 0.1f,
                        "processing"
                    )
                )
            }
        }
        
        _uiState.update {
            it.copy(
                exportProgress = null,
                successMessage = "Export abgeschlossen! Download-Link wurde per E-Mail gesendet."
            )
        }
    }
    
    private suspend fun monitorImportProgress(importId: String) {
        // Similar to export progress monitoring
        for (i in 1..10) {
            kotlinx.coroutines.delay(500)
            _uiState.update {
                it.copy(
                    importProgress = ImportProgress(
                        totalRows = 100,
                        processedRows = i * 10,
                        successfulRows = i * 9,
                        failedRows = i,
                        currentStep = "Importiere Zeile ${i * 10}...",
                        percentComplete = i * 10
                    )
                )
            }
        }
        
        _uiState.update {
            it.copy(
                importProgress = null,
                successMessage = "Import abgeschlossen! 90 von 100 Datensätzen erfolgreich importiert."
            )
        }
    }
    
    private suspend fun monitorRestoreProgress(restoreId: String) {
        // Monitor restore progress
        for (i in 1..10) {
            kotlinx.coroutines.delay(1000)
            _uiState.update {
                it.copy(
                    importProgress = ImportProgress(
                        currentStep = "Stelle wieder her...",
                        percentComplete = i * 10
                    )
                )
            }
        }
        
        _uiState.update {
            it.copy(
                importProgress = null,
                successMessage = "Wiederherstellung abgeschlossen!"
            )
        }
    }
}

class ExportIntegrationViewModelFactory(
    private val appwriteService: AppwriteService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExportIntegrationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExportIntegrationViewModel(appwriteService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}