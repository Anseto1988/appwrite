package com.example.snacktrack.data.model

import java.time.LocalDateTime

/**
 * Offline functionality models
 */

// Offline Sync Queue

data class OfflineSyncItem(
    val id: String = "",
    val operation: SyncOperation = SyncOperation.CREATE,
    val entityType: EntityType = EntityType.DOG,
    val entityId: String = "",
    val data: String = "", // JSON serialized data
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val retryCount: Int = 0,
    val lastError: String? = null,
    val syncStatus: OfflineSyncStatus = OfflineSyncStatus.PENDING,
    val conflictResolution: ConflictResolutionStrategy? = null
)

enum class SyncOperation {
    CREATE,
    UPDATE,
    DELETE,
    PATCH
}

enum class EntityType {
    DOG,
    FEEDING,
    WEIGHT,
    HEALTH_ENTRY,
    MEDICATION,
    ACTIVITY,
    EXPENSE,
    DOCUMENT,
    PHOTO,
    TEAM_MEMBER,
    TASK,
    RECIPE,
    SHOPPING_ITEM
}

enum class OfflineSyncStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    CONFLICT,
    CANCELLED
}

enum class ConflictResolutionStrategy {
    LOCAL_WINS,
    SERVER_WINS,
    MERGE,
    MANUAL
}

// Offline Cache

data class OfflineCache(
    val id: String = "",
    val userId: String = "",
    val cacheType: CacheType = CacheType.ENTITY,
    val entityType: EntityType? = null,
    val entityId: String? = null,
    val data: String = "", // JSON or binary data
    val metadata: CacheMetadata = CacheMetadata(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastAccessedAt: LocalDateTime = LocalDateTime.now(),
    val expiresAt: LocalDateTime? = null
)

enum class CacheType {
    ENTITY,
    LIST,
    QUERY,
    IMAGE,
    DOCUMENT,
    STATISTICS,
    REPORT
}

data class CacheMetadata(
    val version: Int = 1,
    val checksum: String = "",
    val size: Long = 0,
    val compressionType: CompressionType? = null,
    val encryptionType: EncryptionType? = null,
    val tags: List<String> = emptyList()
)

enum class CompressionType {
    NONE,
    GZIP,
    ZLIB,
    LZ4
}

enum class EncryptionType {
    NONE,
    AES_256,
    RSA_2048
}

// Offline Configuration

data class OfflineConfiguration(
    val id: String = "",
    val userId: String = "",
    val offlineMode: OfflineMode = OfflineMode.AUTO,
    val syncConfiguration: OfflineSyncConfiguration = OfflineSyncConfiguration(),
    val cacheConfiguration: OfflineCacheConfiguration = OfflineCacheConfiguration(),
    val dataRetentionPolicy: DataRetentionPolicy = DataRetentionPolicy(),
    val conflictHandling: ConflictHandlingConfiguration = ConflictHandlingConfiguration(),
    val isEnabled: Boolean = true,
    val lastModified: LocalDateTime = LocalDateTime.now()
)

enum class OfflineMode {
    DISABLED,
    MANUAL,
    AUTO,
    AGGRESSIVE
}

data class OfflineSyncConfiguration(
    val autoSync: Boolean = true,
    val syncOnWifiOnly: Boolean = true,
    val syncOnCharging: Boolean = false,
    val syncInterval: OfflineSyncInterval = OfflineSyncInterval.ON_APP_OPEN,
    val maxRetries: Int = 3,
    val retryDelay: Long = 60000, // milliseconds
    val batchSize: Int = 50,
    val priorityOrder: List<EntityType> = EntityType.values().toList()
)

enum class OfflineSyncInterval {
    ON_APP_OPEN,
    EVERY_HOUR,
    EVERY_6_HOURS,
    DAILY,
    ON_DEMAND
}

data class OfflineCacheConfiguration(
    val maxCacheSize: Long = 536870912, // 512MB default
    val maxCacheAge: Long = 604800000, // 7 days in milliseconds
    val cacheStrategy: CacheStrategy = CacheStrategy.LRU,
    val enableCompression: Boolean = true,
    val enableEncryption: Boolean = false,
    val preloadEntities: List<EntityType> = listOf(
        EntityType.DOG,
        EntityType.FEEDING,
        EntityType.WEIGHT,
        EntityType.HEALTH_ENTRY
    ),
    val imageQuality: ImageCacheQuality = ImageCacheQuality.MEDIUM
)

enum class CacheStrategy {
    LRU, // Least Recently Used
    LFU, // Least Frequently Used
    FIFO, // First In First Out
    ADAPTIVE
}

enum class ImageCacheQuality {
    LOW, // 50% quality, max 800px
    MEDIUM, // 75% quality, max 1200px
    HIGH, // 90% quality, max 1600px
    ORIGINAL // 100% quality, no resize
}

data class DataRetentionPolicy(
    val keepEssentialData: Boolean = true,
    val essentialDataTypes: List<EntityType> = listOf(
        EntityType.DOG,
        EntityType.MEDICATION,
        EntityType.HEALTH_ENTRY
    ),
    val deleteOldData: Boolean = true,
    val dataAgeThreshold: Long = 2592000000, // 30 days in milliseconds
    val keepFavorites: Boolean = true,
    val keepRecentlyViewed: Boolean = true,
    val recentlyViewedThreshold: Long = 604800000 // 7 days
)

data class ConflictHandlingConfiguration(
    val defaultStrategy: ConflictResolutionStrategy = ConflictResolutionStrategy.SERVER_WINS,
    val entityStrategies: Map<EntityType, ConflictResolutionStrategy> = mapOf(
        EntityType.HEALTH_ENTRY to ConflictResolutionStrategy.MERGE,
        EntityType.MEDICATION to ConflictResolutionStrategy.SERVER_WINS
    ),
    val showConflictDialog: Boolean = true,
    val autoResolveMinorConflicts: Boolean = true
)

// Sync State

data class SyncState(
    val id: String = "",
    val userId: String = "",
    val lastSyncTimestamp: LocalDateTime? = null,
    val syncInProgress: Boolean = false,
    val currentSyncType: SyncType? = null,
    val pendingChanges: Int = 0,
    val failedItems: Int = 0,
    val conflicts: Int = 0,
    val lastSyncResult: SyncResult? = null,
    val nextScheduledSync: LocalDateTime? = null,
    val syncHistory: List<SyncHistoryItem> = emptyList()
)

enum class SyncType {
    FULL,
    INCREMENTAL,
    SELECTIVE,
    EMERGENCY,
    INITIAL
}

data class SyncResult(
    val success: Boolean = false,
    val itemsSynced: Int = 0,
    val itemsFailed: Int = 0,
    val conflicts: Int = 0,
    val duration: Long = 0, // milliseconds
    val bytesTransferred: Long = 0,
    val errors: List<SyncError> = emptyList()
)

data class SyncError(
    val entityType: EntityType,
    val entityId: String,
    val operation: SyncOperation,
    val errorCode: String,
    val message: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

data class SyncHistoryItem(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val syncType: SyncType,
    val result: SyncResult,
    val trigger: SyncTrigger
)

enum class SyncTrigger {
    MANUAL,
    SCHEDULED,
    APP_OPEN,
    APP_BACKGROUND,
    NETWORK_AVAILABLE,
    LOW_STORAGE,
    DATA_CHANGE
}

// Offline Queue Management

data class OfflineQueueStatus(
    val totalItems: Int = 0,
    val pendingItems: Int = 0,
    val failedItems: Int = 0,
    val oldestItem: LocalDateTime? = null,
    val estimatedSyncTime: Long = 0, // milliseconds
    val queueSizeBytes: Long = 0,
    val itemsByType: Map<EntityType, Int> = emptyMap(),
    val priorityItems: Int = 0
)

data class OfflineQueueItem(
    val syncItem: OfflineSyncItem,
    val priority: QueuePriority = QueuePriority.NORMAL,
    val dependencies: List<String> = emptyList(), // IDs of other items that must sync first
    val metadata: QueueItemMetadata = QueueItemMetadata()
)

enum class QueuePriority {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL
}

data class QueueItemMetadata(
    val source: OfflineDataSource = OfflineDataSource.USER_ACTION,
    val importance: ImportanceLevel = ImportanceLevel.NORMAL,
    val canBatch: Boolean = true,
    val requiresConnection: ConnectionRequirement = ConnectionRequirement.ANY,
    val maxAge: Long? = null // milliseconds before item expires
)

enum class OfflineDataSource {
    USER_ACTION,
    BACKGROUND_SYNC,
    AUTO_SAVE,
    IMPORT,
    MIGRATION
}

enum class ImportanceLevel {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL
}

enum class ConnectionRequirement {
    ANY,
    WIFI_ONLY,
    UNMETERED_ONLY,
    FAST_CONNECTION
}

// Network State

data class NetworkState(
    val isConnected: Boolean = false,
    val connectionType: ConnectionType = ConnectionType.NONE,
    val connectionQuality: ConnectionQuality = ConnectionQuality.UNKNOWN,
    val isMetered: Boolean = true,
    val bandwidthKbps: Int? = null,
    val latencyMs: Int? = null,
    val lastChecked: LocalDateTime = LocalDateTime.now()
)

enum class ConnectionType {
    NONE,
    MOBILE_2G,
    MOBILE_3G,
    MOBILE_4G,
    MOBILE_5G,
    WIFI,
    ETHERNET,
    OTHER
}

enum class ConnectionQuality {
    UNKNOWN,
    POOR,
    MODERATE,
    GOOD,
    EXCELLENT
}

// Offline Analytics

data class OfflineAnalytics(
    val id: String = "",
    val userId: String = "",
    val period: LocalDateTime = LocalDateTime.now(),
    val offlineUsage: OfflineUsageStats = OfflineUsageStats(),
    val syncPerformance: SyncPerformanceStats = SyncPerformanceStats(),
    val cachePerformance: CachePerformanceStats = CachePerformanceStats(),
    val dataUsage: DataUsageStats = DataUsageStats(),
    val reliability: ReliabilityStats = ReliabilityStats()
)

data class OfflineUsageStats(
    val totalOfflineTime: Long = 0, // milliseconds
    val offlineSessions: Int = 0,
    val averageOfflineDuration: Long = 0,
    val offlineActions: Map<String, Int> = emptyMap(),
    val dataCreatedOffline: Int = 0,
    val dataModifiedOffline: Int = 0
)

data class SyncPerformanceStats(
    val totalSyncs: Int = 0,
    val successfulSyncs: Int = 0,
    val failedSyncs: Int = 0,
    val averageSyncDuration: Long = 0,
    val totalDataSynced: Long = 0, // bytes
    val conflictsResolved: Int = 0,
    val itemsQueued: Int = 0,
    val oldestQueuedItem: LocalDateTime? = null
)

data class CachePerformanceStats(
    val cacheHits: Int = 0,
    val cacheMisses: Int = 0,
    val cacheSize: Long = 0, // bytes
    val itemsCached: Int = 0,
    val evictions: Int = 0,
    val compressionRatio: Double = 0.0,
    val averageAccessTime: Long = 0 // milliseconds
)

data class DataUsageStats(
    val totalDataDownloaded: Long = 0, // bytes
    val totalDataUploaded: Long = 0,
    val wifiDataUsed: Long = 0,
    val mobileDataUsed: Long = 0,
    val dataSavedByCache: Long = 0,
    val dataSavedByCompression: Long = 0
)

data class ReliabilityStats(
    val syncSuccessRate: Double = 0.0,
    val dataIntegrityIssues: Int = 0,
    val corruptedItems: Int = 0,
    val recoveredItems: Int = 0,
    val averageQueueTime: Long = 0,
    val criticalFailures: Int = 0
)

// Offline Capabilities

data class OfflineCapabilities(
    val supportedOperations: Map<EntityType, List<SyncOperation>> = mapOf(
        EntityType.DOG to listOf(SyncOperation.CREATE, SyncOperation.UPDATE),
        EntityType.FEEDING to SyncOperation.values().toList(),
        EntityType.WEIGHT to SyncOperation.values().toList(),
        EntityType.HEALTH_ENTRY to SyncOperation.values().toList(),
        EntityType.MEDICATION to listOf(SyncOperation.CREATE, SyncOperation.UPDATE),
        EntityType.ACTIVITY to SyncOperation.values().toList(),
        EntityType.EXPENSE to SyncOperation.values().toList(),
        EntityType.PHOTO to listOf(SyncOperation.CREATE, SyncOperation.DELETE),
        EntityType.DOCUMENT to listOf(SyncOperation.CREATE, SyncOperation.DELETE)
    ),
    val offlineFeatures: List<OfflineFeature> = OfflineFeature.values().toList(),
    val limitations: List<OfflineLimitation> = emptyList()
)

enum class OfflineFeature {
    BASIC_DATA_ENTRY,
    PHOTO_CAPTURE,
    DOCUMENT_VIEWING,
    STATISTICS_VIEWING,
    REMINDER_NOTIFICATIONS,
    BARCODE_SCANNING,
    RECIPE_ACCESS,
    TEAM_MESSAGING,
    AI_PREDICTIONS,
    EXPORT_DATA
}

data class OfflineLimitation(
    val feature: String,
    val description: String,
    val workaround: String? = null
)

// Data Integrity

data class DataIntegrityCheck(
    val id: String = "",
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val checkType: IntegrityCheckType = IntegrityCheckType.FULL,
    val itemsChecked: Int = 0,
    val issuesFound: Int = 0,
    val issuesFixed: Int = 0,
    val corruptedItems: List<CorruptedItem> = emptyList(),
    val recommendations: List<String> = emptyList()
)

enum class IntegrityCheckType {
    QUICK,
    FULL,
    DEEP,
    REPAIR
}

data class CorruptedItem(
    val entityType: EntityType,
    val entityId: String,
    val issue: String,
    val severity: OfflineIssueSeverity,
    val fixable: Boolean,
    val fixApplied: Boolean = false
)

enum class OfflineIssueSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

// Migration Support

data class OfflineMigration(
    val id: String = "",
    val fromVersion: Int,
    val toVersion: Int,
    val migrationSteps: List<MigrationStep> = emptyList(),
    val status: MigrationStatus = MigrationStatus.PENDING,
    val startedAt: LocalDateTime? = null,
    val completedAt: LocalDateTime? = null,
    val errors: List<String> = emptyList()
)

data class MigrationStep(
    val step: Int,
    val description: String,
    val action: MigrationAction,
    val entityTypes: List<EntityType>,
    val completed: Boolean = false,
    val error: String? = null
)

enum class MigrationAction {
    SCHEMA_UPDATE,
    DATA_TRANSFORM,
    INDEX_REBUILD,
    CACHE_CLEAR,
    SYNC_RESET
}

enum class MigrationStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    ROLLED_BACK
}