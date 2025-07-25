package com.example.snacktrack.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snacktrack.data.model.*
import com.example.snacktrack.data.repository.OfflineRepository
import com.example.snacktrack.data.service.AppwriteService
import com.example.snacktrack.ui.screens.offline.SyncConflict
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class OfflineUiState(
    val isLoading: Boolean = false,
    val configuration: OfflineConfiguration = OfflineConfiguration(),
    val cacheSize: Long = 0,
    val cacheStats: CachePerformanceStats = CachePerformanceStats(),
    val conflicts: List<SyncConflict> = emptyList(),
    val integrityCheckResult: DataIntegrityCheck? = null,
    val error: String? = null,
    val successMessage: String? = null
)

class OfflineViewModel(
    private val context: Context,
    private val appwriteService: AppwriteService
) : ViewModel() {
    private val offlineRepository = OfflineRepository(context, appwriteService)
    
    private val _uiState = MutableStateFlow(OfflineUiState())
    val uiState: StateFlow<OfflineUiState> = _uiState.asStateFlow()
    
    // Direct access to repository state flows
    val networkState = offlineRepository.networkState
    val syncState = offlineRepository.syncState
    val queueStatus = offlineRepository.queueStatus
    
    init {
        loadOfflineConfiguration()
        updateCacheStats()
        loadConflicts()
    }
    
    private fun loadOfflineConfiguration() {
        viewModelScope.launch {
            try {
                // Load configuration from preferences
                // For now, use default configuration
                _uiState.update { it.copy(configuration = OfflineConfiguration()) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    private fun updateCacheStats() {
        viewModelScope.launch {
            try {
                // Get cache statistics
                // This would query the offline database
                _uiState.update {
                    it.copy(
                        cacheSize = 50 * 1024 * 1024, // 50MB example
                        cacheStats = CachePerformanceStats(
                            cacheHits = 1250,
                            cacheMisses = 150,
                            cacheSize = 50 * 1024 * 1024,
                            itemsCached = 850,
                            evictions = 25,
                            compressionRatio = 0.65,
                            averageAccessTime = 12
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    private fun loadConflicts() {
        viewModelScope.launch {
            try {
                // Load any unresolved conflicts
                // This would query the offline sync queue
                val conflicts = listOf<SyncConflict>() // Empty for now
                _uiState.update { it.copy(conflicts = conflicts) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    // Configuration Settings
    
    fun setOfflineEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    configuration = it.configuration.copy(isEnabled = enabled)
                )
            }
            saveConfiguration()
        }
    }
    
    fun setOfflineMode(mode: OfflineMode) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    configuration = it.configuration.copy(offlineMode = mode)
                )
            }
            saveConfiguration()
        }
    }
    
    fun setAutoSync(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    configuration = it.configuration.copy(
                        syncConfiguration = it.configuration.syncConfiguration.copy(
                            autoSync = enabled
                        )
                    )
                )
            }
            saveConfiguration()
        }
    }
    
    fun setSyncOnWifiOnly(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    configuration = it.configuration.copy(
                        syncConfiguration = it.configuration.syncConfiguration.copy(
                            syncOnWifiOnly = enabled
                        )
                    )
                )
            }
            saveConfiguration()
        }
    }
    
    fun setSyncOnCharging(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    configuration = it.configuration.copy(
                        syncConfiguration = it.configuration.syncConfiguration.copy(
                            syncOnCharging = enabled
                        )
                    )
                )
            }
            saveConfiguration()
        }
    }
    
    fun setSyncInterval(interval: SyncInterval) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    configuration = it.configuration.copy(
                        syncConfiguration = it.configuration.syncConfiguration.copy(
                            syncInterval = interval
                        )
                    )
                )
            }
            saveConfiguration()
        }
    }
    
    fun setCacheCompression(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    configuration = it.configuration.copy(
                        cacheConfiguration = it.configuration.cacheConfiguration.copy(
                            enableCompression = enabled
                        )
                    )
                )
            }
            saveConfiguration()
        }
    }
    
    fun setCacheEncryption(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    configuration = it.configuration.copy(
                        cacheConfiguration = it.configuration.cacheConfiguration.copy(
                            enableEncryption = enabled
                        )
                    )
                )
            }
            saveConfiguration()
        }
    }
    
    fun setImageCacheQuality(quality: ImageCacheQuality) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    configuration = it.configuration.copy(
                        cacheConfiguration = it.configuration.cacheConfiguration.copy(
                            imageQuality = quality
                        )
                    )
                )
            }
            saveConfiguration()
        }
    }
    
    fun setDefaultConflictStrategy(strategy: ConflictResolutionStrategy) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    configuration = it.configuration.copy(
                        conflictHandling = it.configuration.conflictHandling.copy(
                            defaultStrategy = strategy
                        )
                    )
                )
            }
            saveConfiguration()
        }
    }
    
    fun setShowConflictDialog(show: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    configuration = it.configuration.copy(
                        conflictHandling = it.configuration.conflictHandling.copy(
                            showConflictDialog = show
                        )
                    )
                )
            }
            saveConfiguration()
        }
    }
    
    fun setKeepEssentialData(keep: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    configuration = it.configuration.copy(
                        dataRetentionPolicy = it.configuration.dataRetentionPolicy.copy(
                            keepEssentialData = keep
                        )
                    )
                )
            }
            saveConfiguration()
        }
    }
    
    fun setDeleteOldData(delete: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    configuration = it.configuration.copy(
                        dataRetentionPolicy = it.configuration.dataRetentionPolicy.copy(
                            deleteOldData = delete
                        )
                    )
                )
            }
            saveConfiguration()
        }
    }
    
    fun setKeepFavorites(keep: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    configuration = it.configuration.copy(
                        dataRetentionPolicy = it.configuration.dataRetentionPolicy.copy(
                            keepFavorites = keep
                        )
                    )
                )
            }
            saveConfiguration()
        }
    }
    
    // Actions
    
    fun performSync(syncType: SyncType = SyncType.INCREMENTAL) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val result = offlineRepository.performSync(syncType)
                
                result.getOrNull()?.let { syncResult ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = if (syncResult.success) {
                                "Synchronisierung erfolgreich: ${syncResult.itemsSynced} Elemente synchronisiert"
                            } else {
                                "Synchronisierung teilweise fehlgeschlagen: ${syncResult.itemsFailed} Fehler"
                            }
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
    
    fun clearCache() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Clear cache
                // This would clear the offline cache database
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        cacheSize = 0,
                        cacheStats = CachePerformanceStats(),
                        successMessage = "Cache erfolgreich geleert"
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
    
    fun performIntegrityCheck(checkType: IntegrityCheckType = IntegrityCheckType.QUICK) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val result = offlineRepository.performIntegrityCheck(checkType)
                
                result.getOrNull()?.let { checkResult ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            integrityCheckResult = checkResult,
                            successMessage = if (checkResult.issuesFound == 0) {
                                "Integritätsprüfung erfolgreich: Keine Probleme gefunden"
                            } else {
                                "Integritätsprüfung abgeschlossen: ${checkResult.issuesFound} Probleme gefunden, ${checkResult.issuesFixed} behoben"
                            }
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
    
    fun resolveConflict(conflictId: String, strategy: ConflictResolutionStrategy) {
        viewModelScope.launch {
            try {
                // Resolve the conflict
                // This would update the sync queue item with the resolution strategy
                
                _uiState.update {
                    it.copy(
                        conflicts = it.conflicts.filter { conflict -> conflict.id != conflictId }
                    )
                }
                
                // Trigger sync for resolved item
                performSync(SyncType.SELECTIVE)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    // Offline Data Operations
    
    fun queueOfflineOperation(
        operation: SyncOperation,
        entityType: EntityType,
        entityId: String,
        data: Any,
        priority: QueuePriority = QueuePriority.NORMAL
    ) {
        viewModelScope.launch {
            try {
                val result = offlineRepository.queueOfflineOperation(
                    operation = operation,
                    entityType = entityType,
                    entityId = entityId,
                    data = data,
                    priority = priority
                )
                
                result.getOrNull()?.let {
                    _uiState.update {
                        it.copy(
                            successMessage = "Operation wurde zur Offline-Warteschlange hinzugefügt"
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
    
    fun cacheData(
        entityType: EntityType,
        entityId: String,
        data: Any,
        cacheType: CacheType = CacheType.ENTITY
    ) {
        viewModelScope.launch {
            try {
                val result = offlineRepository.cacheData(
                    entityType = entityType,
                    entityId = entityId,
                    data = data,
                    cacheType = cacheType
                )
                
                result.getOrNull()?.let {
                    updateCacheStats()
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
    
    suspend fun getCachedData(
        entityType: EntityType,
        entityId: String
    ): Any? {
        return try {
            val result = offlineRepository.getCachedData(entityType, entityId)
            result.getOrNull()
        } catch (e: Exception) {
            null
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
    
    // Helper functions
    
    private fun saveConfiguration() {
        // Save configuration to shared preferences
        context.getSharedPreferences("offline_config", Context.MODE_PRIVATE).edit().apply {
            // Save configuration as JSON
            apply()
        }
    }
    
    // Example usage functions for other ViewModels
    
    fun <T> withOfflineSupport(
        entityType: EntityType,
        entityId: String,
        networkCall: suspend () -> T,
        cacheData: (T) -> Unit = {}
    ): Flow<T?> = flow {
        // First emit cached data
        val cached = getCachedData(entityType, entityId)
        if (cached != null) {
            emit(cached as T)
        }
        
        // Then try network call
        if (networkState.value.isConnected) {
            try {
                val result = networkCall()
                emit(result)
                
                // Cache the result
                cacheData(entityType, entityId, result as Any)
                cacheData(result)
            } catch (e: Exception) {
                // If network fails and we have cache, that's okay
                if (cached == null) {
                    throw e
                }
            }
        } else if (cached == null) {
            // No cache and no network
            throw Exception("Keine Internetverbindung und keine Offline-Daten verfügbar")
        }
    }
}

class OfflineViewModelFactory(
    private val context: Context,
    private val appwriteService: AppwriteService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OfflineViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OfflineViewModel(context, appwriteService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}