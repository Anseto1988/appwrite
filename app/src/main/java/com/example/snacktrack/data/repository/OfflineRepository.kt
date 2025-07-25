package com.example.snacktrack.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.snacktrack.data.model.*
import com.example.snacktrack.data.service.AppwriteService
import io.appwrite.Query
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.Document
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.util.UUID
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class OfflineRepository(
    private val context: Context,
    private val appwriteService: AppwriteService
) {
    private val databases = appwriteService.databases
    private val databaseId = AppwriteService.DATABASE_ID
    
    // Local storage for offline data
    private val sharedPrefs = context.getSharedPreferences("offline_data", Context.MODE_PRIVATE)
    private val offlineDb = context.openOrCreateDatabase("snacktrack_offline.db", Context.MODE_PRIVATE, null)
    
    // State flows
    private val _syncState = MutableStateFlow(SyncState())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    private val _networkState = MutableStateFlow(NetworkState())
    val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()
    
    private val _queueStatus = MutableStateFlow(OfflineQueueStatus())
    val queueStatus: StateFlow<OfflineQueueStatus> = _queueStatus.asStateFlow()
    
    init {
        initializeOfflineDatabase()
        startNetworkMonitoring()
        loadSyncState()
    }
    
    private fun initializeOfflineDatabase() {
        // Create tables for offline storage
        offlineDb.execSQL("""
            CREATE TABLE IF NOT EXISTS offline_sync_queue (
                id TEXT PRIMARY KEY,
                operation TEXT NOT NULL,
                entity_type TEXT NOT NULL,
                entity_id TEXT NOT NULL,
                data TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                retry_count INTEGER DEFAULT 0,
                last_error TEXT,
                sync_status TEXT NOT NULL,
                conflict_resolution TEXT
            )
        """)
        
        offlineDb.execSQL("""
            CREATE TABLE IF NOT EXISTS offline_cache (
                id TEXT PRIMARY KEY,
                user_id TEXT NOT NULL,
                cache_type TEXT NOT NULL,
                entity_type TEXT,
                entity_id TEXT,
                data TEXT NOT NULL,
                metadata TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                last_accessed_at INTEGER NOT NULL,
                expires_at INTEGER
            )
        """)
        
        offlineDb.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_sync_queue_status 
            ON offline_sync_queue(sync_status)
        """)
        
        offlineDb.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_cache_user 
            ON offline_cache(user_id)
        """)
        
        offlineDb.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_cache_entity 
            ON offline_cache(entity_type, entity_id)
        """)
    }
    
    // Network Monitoring
    
    private fun startNetworkMonitoring() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        // Monitor network changes
        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                updateNetworkState()
                if (_networkState.value.isConnected && hasOfflineChanges()) {
                    GlobalScope.launch {
                        performSync()
                    }
                }
            }
            
            override fun onLost(network: android.net.Network) {
                updateNetworkState()
            }
            
            override fun onCapabilitiesChanged(
                network: android.net.Network,
                networkCapabilities: NetworkCapabilities
            ) {
                updateNetworkState()
            }
        })
        
        // Initial network state
        updateNetworkState()
    }
    
    private fun updateNetworkState() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = network?.let { connectivityManager.getNetworkCapabilities(it) }
        
        val isConnected = capabilities != null
        val connectionType = when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> ConnectionType.WIFI
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> {
                // Determine cellular type (simplified)
                ConnectionType.MOBILE_4G
            }
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> ConnectionType.ETHERNET
            else -> ConnectionType.NONE
        }
        
        val isMetered = connectivityManager.isActiveNetworkMetered
        val bandwidth = capabilities?.linkDownstreamBandwidthKbps
        
        val connectionQuality = when {
            bandwidth == null -> ConnectionQuality.UNKNOWN
            bandwidth < 150 -> ConnectionQuality.POOR
            bandwidth < 550 -> ConnectionQuality.MODERATE
            bandwidth < 2000 -> ConnectionQuality.GOOD
            else -> ConnectionQuality.EXCELLENT
        }
        
        _networkState.value = NetworkState(
            isConnected = isConnected,
            connectionType = connectionType,
            connectionQuality = connectionQuality,
            isMetered = isMetered,
            bandwidthKbps = bandwidth,
            lastChecked = LocalDateTime.now()
        )
    }
    
    // Offline Queue Management
    
    suspend fun queueOfflineOperation(
        operation: SyncOperation,
        entityType: EntityType,
        entityId: String,
        data: Any,
        priority: QueuePriority = QueuePriority.NORMAL
    ): Result<OfflineSyncItem> = withContext(Dispatchers.IO) {
        try {
            val syncItem = OfflineSyncItem(
                id = UUID.randomUUID().toString(),
                operation = operation,
                entityType = entityType,
                entityId = entityId,
                data = JSONObject.wrap(data).toString(),
                timestamp = LocalDateTime.now(),
                syncStatus = OfflineSyncStatus.PENDING
            )
            
            // Insert into offline queue
            offlineDb.execSQL(
                """
                INSERT INTO offline_sync_queue 
                (id, operation, entity_type, entity_id, data, timestamp, sync_status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                arrayOf(
                    syncItem.id,
                    syncItem.operation.name,
                    syncItem.entityType.name,
                    syncItem.entityId,
                    syncItem.data,
                    syncItem.timestamp.toEpochSecond(java.time.ZoneOffset.UTC),
                    syncItem.syncStatus.name
                )
            )
            
            updateQueueStatus()
            
            // Try to sync immediately if online
            if (_networkState.value.isConnected && shouldSyncNow()) {
                syncSingleItem(syncItem)
            }
            
            Result.success(syncItem)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun syncSingleItem(item: OfflineSyncItem): Boolean {
        return try {
            // Update status to in progress
            updateSyncItemStatus(item.id, OfflineSyncStatus.IN_PROGRESS)
            
            // Perform the sync operation
            val success = when (item.operation) {
                SyncOperation.CREATE -> syncCreate(item)
                SyncOperation.UPDATE -> syncUpdate(item)
                SyncOperation.DELETE -> syncDelete(item)
                SyncOperation.PATCH -> syncPatch(item)
            }
            
            if (success) {
                // Remove from queue
                offlineDb.execSQL(
                    "DELETE FROM offline_sync_queue WHERE id = ?",
                    arrayOf(item.id)
                )
                updateQueueStatus()
                true
            } else {
                // Update retry count and status
                val newRetryCount = item.retryCount + 1
                updateSyncItemStatus(
                    item.id,
                    if (newRetryCount >= 3) OfflineSyncStatus.FAILED else OfflineSyncStatus.PENDING,
                    newRetryCount
                )
                false
            }
        } catch (e: Exception) {
            // Handle conflict
            if (e is AppwriteException && e.code == 409) {
                handleConflict(item)
            } else {
                updateSyncItemStatus(item.id, OfflineSyncStatus.FAILED, item.retryCount + 1, e.message)
            }
            false
        }
    }
    
    private suspend fun syncCreate(item: OfflineSyncItem): Boolean {
        val data = JSONObject(item.data)
        val collection = getCollectionForEntityType(item.entityType)
        
        return try {
            database.createDocument(
                databaseId = databaseId,
                collectionId = collection,
                documentId = item.entityId,
                data = data.toMap()
            )
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private suspend fun syncUpdate(item: OfflineSyncItem): Boolean {
        val data = JSONObject(item.data)
        val collection = getCollectionForEntityType(item.entityType)
        
        return try {
            appwriteService.databases.updateDocument(
                databaseId = databaseId,
                collectionId = collection,
                documentId = item.entityId,
                data = data.toMap()
            )
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private suspend fun syncDelete(item: OfflineSyncItem): Boolean {
        val collection = getCollectionForEntityType(item.entityType)
        
        return try {
            database.deleteDocument(
                databaseId = databaseId,
                collectionId = collection,
                documentId = item.entityId
            )
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private suspend fun syncPatch(item: OfflineSyncItem): Boolean {
        // Similar to update but only sends changed fields
        return syncUpdate(item)
    }
    
    // Offline Cache Management
    
    suspend fun cacheData(
        entityType: EntityType,
        entityId: String,
        data: Any,
        cacheType: CacheType = CacheType.ENTITY
    ): Result<OfflineCache> = withContext(Dispatchers.IO) {
        try {
            val config = getOfflineConfiguration()
            val jsonData = JSONObject.wrap(data).toString()
            
            // Compress if enabled
            val processedData = if (config.cacheConfiguration.enableCompression) {
                compressData(jsonData.toByteArray())
            } else {
                jsonData.toByteArray()
            }
            
            // Encrypt if enabled
            val finalData = if (config.cacheConfiguration.enableEncryption) {
                encryptData(processedData)
            } else {
                processedData
            }
            
            val metadata = CacheMetadata(
                size = finalData.size.toLong(),
                compressionType = if (config.cacheConfiguration.enableCompression) CompressionType.GZIP else CompressionType.NONE,
                encryptionType = if (config.cacheConfiguration.enableEncryption) EncryptionType.AES_256 else EncryptionType.NONE,
                checksum = calculateChecksum(finalData)
            )
            
            val cache = OfflineCache(
                id = UUID.randomUUID().toString(),
                userId = appwriteService.account.get().id,
                cacheType = cacheType,
                entityType = entityType,
                entityId = entityId,
                data = android.util.Base64.encodeToString(finalData, android.util.Base64.DEFAULT),
                metadata = metadata,
                createdAt = LocalDateTime.now(),
                lastAccessedAt = LocalDateTime.now(),
                expiresAt = LocalDateTime.now().plusSeconds(config.cacheConfiguration.maxCacheAge / 1000)
            )
            
            // Insert into cache
            offlineDb.execSQL(
                """
                INSERT OR REPLACE INTO offline_cache 
                (id, user_id, cache_type, entity_type, entity_id, data, metadata, created_at, last_accessed_at, expires_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                arrayOf(
                    cache.id,
                    cache.userId,
                    cache.cacheType.name,
                    cache.entityType?.name,
                    cache.entityId,
                    cache.data,
                    JSONObject.wrap(cache.metadata).toString(),
                    cache.createdAt.toEpochSecond(java.time.ZoneOffset.UTC),
                    cache.lastAccessedAt.toEpochSecond(java.time.ZoneOffset.UTC),
                    cache.expiresAt?.toEpochSecond(java.time.ZoneOffset.UTC)
                )
            )
            
            // Clean old cache if needed
            cleanupCache()
            
            Result.success(cache)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getCachedData(
        entityType: EntityType,
        entityId: String
    ): Result<Any?> = withContext(Dispatchers.IO) {
        try {
            val cursor = offlineDb.rawQuery(
                """
                SELECT data, metadata FROM offline_cache 
                WHERE entity_type = ? AND entity_id = ? 
                AND (expires_at IS NULL OR expires_at > ?)
                ORDER BY last_accessed_at DESC
                LIMIT 1
                """,
                arrayOf(
                    entityType.name,
                    entityId,
                    LocalDateTime.now().toEpochSecond(java.time.ZoneOffset.UTC).toString()
                )
            )
            
            if (cursor.moveToFirst()) {
                val encodedData = cursor.getString(0)
                val metadataJson = cursor.getString(1)
                val metadata = parseMetadata(metadataJson)
                
                // Update last accessed time
                offlineDb.execSQL(
                    "UPDATE offline_cache SET last_accessed_at = ? WHERE entity_type = ? AND entity_id = ?",
                    arrayOf(
                        LocalDateTime.now().toEpochSecond(java.time.ZoneOffset.UTC),
                        entityType.name,
                        entityId
                    )
                )
                
                // Decode and decompress data
                var data = android.util.Base64.decode(encodedData, android.util.Base64.DEFAULT)
                
                // Decrypt if needed
                if (metadata.encryptionType == EncryptionType.AES_256) {
                    data = decryptData(data)
                }
                
                // Decompress if needed
                if (metadata.compressionType == CompressionType.GZIP) {
                    data = decompressData(data)
                }
                
                val jsonData = String(data)
                Result.success(JSONObject(jsonData))
            } else {
                Result.success(null)
            }
            cursor.close()
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Sync Operations
    
    suspend fun performSync(
        syncType: SyncType = SyncType.INCREMENTAL
    ): Result<SyncResult> = withContext(Dispatchers.IO) {
        if (_syncState.value.syncInProgress) {
            return@withContext Result.failure(Exception("Sync already in progress"))
        }
        
        _syncState.update { it.copy(syncInProgress = true, currentSyncType = syncType) }
        
        val startTime = System.currentTimeMillis()
        var itemsSynced = 0
        var itemsFailed = 0
        var conflicts = 0
        val errors = mutableListOf<SyncError>()
        
        try {
            // Get pending items from queue
            val pendingItems = getPendingItems()
            
            // Group items by entity type for batch processing
            val groupedItems = pendingItems.groupBy { it.entityType }
            
            for ((entityType, items) in groupedItems) {
                val batchResult = syncBatch(entityType, items)
                itemsSynced += batchResult.synced
                itemsFailed += batchResult.failed
                conflicts += batchResult.conflicts
                errors.addAll(batchResult.errors)
            }
            
            // Perform downstream sync if needed
            if (syncType == SyncType.FULL || syncType == SyncType.INITIAL) {
                val downstreamResult = performDownstreamSync()
                // Process downstream results
            }
            
            val duration = System.currentTimeMillis() - startTime
            
            val result = SyncResult(
                success = itemsFailed == 0,
                itemsSynced = itemsSynced,
                itemsFailed = itemsFailed,
                conflicts = conflicts,
                duration = duration,
                errors = errors
            )
            
            // Update sync state
            _syncState.update { state ->
                state.copy(
                    syncInProgress = false,
                    currentSyncType = null,
                    lastSyncTimestamp = LocalDateTime.now(),
                    lastSyncResult = result,
                    pendingChanges = state.pendingChanges - itemsSynced,
                    failedItems = itemsFailed,
                    conflicts = conflicts,
                    syncHistory = (state.syncHistory + SyncHistoryItem(
                        syncType = syncType,
                        result = result,
                        trigger = SyncTrigger.MANUAL
                    )).takeLast(100)
                )
            }
            
            // Save sync state
            saveSyncState()
            
            Result.success(result)
        } catch (e: Exception) {
            _syncState.update { it.copy(syncInProgress = false, currentSyncType = null) }
            Result.failure(e)
        }
    }
    
    private suspend fun syncBatch(
        entityType: EntityType,
        items: List<OfflineSyncItem>
    ): BatchSyncResult = coroutineScope {
        val results = items.map { item ->
            async {
                try {
                    syncSingleItem(item)
                } catch (e: Exception) {
                    false
                }
            }
        }.awaitAll()
        
        val synced = results.count { it }
        val failed = results.count { !it }
        
        BatchSyncResult(
            synced = synced,
            failed = failed,
            conflicts = 0, // Would be updated by conflict handling
            errors = emptyList() // Would collect actual errors
        )
    }
    
    // Conflict Resolution
    
    private suspend fun handleConflict(item: OfflineSyncItem) {
        val config = getOfflineConfiguration()
        val strategy = config.conflictHandling.entityStrategies[item.entityType]
            ?: config.conflictHandling.defaultStrategy
        
        when (strategy) {
            ConflictResolutionStrategy.LOCAL_WINS -> {
                // Force update with local data
                forceSyncItem(item)
            }
            ConflictResolutionStrategy.SERVER_WINS -> {
                // Discard local changes
                discardLocalChanges(item)
            }
            ConflictResolutionStrategy.MERGE -> {
                // Attempt to merge changes
                mergeChanges(item)
            }
            ConflictResolutionStrategy.MANUAL -> {
                // Mark for manual resolution
                markForManualResolution(item)
            }
        }
    }
    
    // Data Integrity
    
    suspend fun performIntegrityCheck(
        checkType: IntegrityCheckType = IntegrityCheckType.QUICK
    ): Result<DataIntegrityCheck> = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()
            var itemsChecked = 0
            var issuesFound = 0
            var issuesFixed = 0
            val corruptedItems = mutableListOf<CorruptedItem>()
            val recommendations = mutableListOf<String>()
            
            // Check sync queue integrity
            val queueIssues = checkSyncQueueIntegrity()
            itemsChecked += queueIssues.itemsChecked
            issuesFound += queueIssues.issues.size
            corruptedItems.addAll(queueIssues.issues)
            
            // Check cache integrity
            val cacheIssues = checkCacheIntegrity()
            itemsChecked += cacheIssues.itemsChecked
            issuesFound += cacheIssues.issues.size
            corruptedItems.addAll(cacheIssues.issues)
            
            // Attempt repairs if requested
            if (checkType == IntegrityCheckType.REPAIR) {
                for (item in corruptedItems.filter { it.fixable }) {
                    if (repairCorruptedItem(item)) {
                        issuesFixed++
                    }
                }
            }
            
            // Generate recommendations
            if (issuesFound > 0) {
                recommendations.add("Found $issuesFound issues during integrity check")
                if (issuesFixed < issuesFound) {
                    recommendations.add("${issuesFound - issuesFixed} issues require manual intervention")
                }
            }
            
            if (queueIssues.oldItems > 10) {
                recommendations.add("${queueIssues.oldItems} items have been in queue for over 24 hours")
            }
            
            val check = DataIntegrityCheck(
                checkType = checkType,
                itemsChecked = itemsChecked,
                issuesFound = issuesFound,
                issuesFixed = issuesFixed,
                corruptedItems = corruptedItems,
                recommendations = recommendations
            )
            
            Result.success(check)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Helper functions
    
    private fun getCollectionForEntityType(entityType: EntityType): String = when (entityType) {
        EntityType.DOG -> "dogs"
        EntityType.FEEDING -> "feedings"
        EntityType.WEIGHT -> "weight_entries"
        EntityType.HEALTH_ENTRY -> "health_entries"
        EntityType.MEDICATION -> "medications"
        EntityType.ACTIVITY -> "activities"
        EntityType.EXPENSE -> "expenses"
        EntityType.DOCUMENT -> "documents"
        EntityType.PHOTO -> "photos"
        EntityType.TEAM_MEMBER -> "team_members"
        EntityType.TASK -> "team_tasks"
        EntityType.RECIPE -> "community_recipes"
        EntityType.SHOPPING_ITEM -> "shopping_items"
    }
    
    private suspend fun getOfflineConfiguration(): OfflineConfiguration {
        // Load from preferences or return default
        val configJson = sharedPrefs.getString("offline_config", null)
        return if (configJson != null) {
            // Parse configuration
            OfflineConfiguration()
        } else {
            OfflineConfiguration()
        }
    }
    
    private fun compressData(data: ByteArray): ByteArray {
        val output = ByteArrayOutputStream()
        GZIPOutputStream(output).use { it.write(data) }
        return output.toByteArray()
    }
    
    private fun decompressData(data: ByteArray): ByteArray {
        return GZIPInputStream(data.inputStream()).use { it.readBytes() }
    }
    
    private fun encryptData(data: ByteArray): ByteArray {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        val secretKey = keyGenerator.generateKey()
        
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        
        // Store key securely (simplified for example)
        sharedPrefs.edit().putString("encryption_key", 
            android.util.Base64.encodeToString(secretKey.encoded, android.util.Base64.DEFAULT)
        ).apply()
        
        return cipher.doFinal(data)
    }
    
    private fun decryptData(data: ByteArray): ByteArray {
        val keyString = sharedPrefs.getString("encryption_key", null)
            ?: throw Exception("Encryption key not found")
        
        val keyBytes = android.util.Base64.decode(keyString, android.util.Base64.DEFAULT)
        val secretKey = SecretKeySpec(keyBytes, "AES")
        
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        
        return cipher.doFinal(data)
    }
    
    private fun calculateChecksum(data: ByteArray): String {
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val digest = md.digest(data)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }
    
    private fun hasOfflineChanges(): Boolean {
        val cursor = offlineDb.rawQuery(
            "SELECT COUNT(*) FROM offline_sync_queue WHERE sync_status = ?",
            arrayOf(OfflineSyncStatus.PENDING.name)
        )
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        return count > 0
    }
    
    private suspend fun shouldSyncNow(): Boolean {
        val config = getOfflineConfiguration()
        val network = _networkState.value
        
        return when {
            !config.syncConfiguration.autoSync -> false
            config.syncConfiguration.syncOnWifiOnly && network.connectionType != ConnectionType.WIFI -> false
            network.isMetered && config.syncConfiguration.syncOnWifiOnly -> false
            else -> true
        }
    }
    
    private suspend fun getPendingItems(): List<OfflineSyncItem> {
        val items = mutableListOf<OfflineSyncItem>()
        val cursor = offlineDb.rawQuery(
            """
            SELECT * FROM offline_sync_queue 
            WHERE sync_status IN (?, ?)
            ORDER BY timestamp ASC
            LIMIT ?
            """,
            arrayOf(
                OfflineSyncStatus.PENDING.name,
                OfflineSyncStatus.FAILED.name,
                "50" // Batch size
            )
        )
        
        while (cursor.moveToNext()) {
            items.add(parseSyncItem(cursor))
        }
        cursor.close()
        
        return items
    }
    
    private fun parseSyncItem(cursor: android.database.Cursor): OfflineSyncItem {
        return OfflineSyncItem(
            id = cursor.getString(cursor.getColumnIndex("id")),
            operation = SyncOperation.valueOf(cursor.getString(cursor.getColumnIndex("operation"))),
            entityType = EntityType.valueOf(cursor.getString(cursor.getColumnIndex("entity_type"))),
            entityId = cursor.getString(cursor.getColumnIndex("entity_id")),
            data = cursor.getString(cursor.getColumnIndex("data")),
            timestamp = LocalDateTime.ofEpochSecond(
                cursor.getLong(cursor.getColumnIndex("timestamp")),
                0,
                java.time.ZoneOffset.UTC
            ),
            retryCount = cursor.getInt(cursor.getColumnIndex("retry_count")),
            lastError = cursor.getString(cursor.getColumnIndex("last_error")),
            syncStatus = OfflineSyncStatus.valueOf(cursor.getString(cursor.getColumnIndex("sync_status")))
        )
    }
    
    private fun updateSyncItemStatus(
        id: String,
        status: OfflineSyncStatus,
        retryCount: Int? = null,
        error: String? = null
    ) {
        val params = mutableListOf<Any>(status.name)
        var query = "UPDATE offline_sync_queue SET sync_status = ?"
        
        if (retryCount != null) {
            query += ", retry_count = ?"
            params.add(retryCount)
        }
        
        if (error != null) {
            query += ", last_error = ?"
            params.add(error)
        }
        
        query += " WHERE id = ?"
        params.add(id)
        
        offlineDb.execSQL(query, params.toTypedArray())
    }
    
    private fun updateQueueStatus() {
        val cursor = offlineDb.rawQuery(
            """
            SELECT 
                COUNT(*) as total,
                SUM(CASE WHEN sync_status = ? THEN 1 ELSE 0 END) as pending,
                SUM(CASE WHEN sync_status = ? THEN 1 ELSE 0 END) as failed,
                MIN(timestamp) as oldest
            FROM offline_sync_queue
            """,
            arrayOf(OfflineSyncStatus.PENDING.name, OfflineSyncStatus.FAILED.name)
        )
        
        if (cursor.moveToFirst()) {
            val total = cursor.getInt(0)
            val pending = cursor.getInt(1)
            val failed = cursor.getInt(2)
            val oldestTimestamp = cursor.getLong(3)
            
            _queueStatus.value = OfflineQueueStatus(
                totalItems = total,
                pendingItems = pending,
                failedItems = failed,
                oldestItem = if (oldestTimestamp > 0) {
                    LocalDateTime.ofEpochSecond(oldestTimestamp, 0, java.time.ZoneOffset.UTC)
                } else null
            )
        }
        cursor.close()
    }
    
    private fun loadSyncState() {
        val stateJson = sharedPrefs.getString("sync_state", null)
        if (stateJson != null) {
            // Parse and load sync state
            _syncState.value = SyncState()
        }
    }
    
    private fun saveSyncState() {
        // Save sync state to preferences
        sharedPrefs.edit().putString("sync_state", JSONObject.wrap(_syncState.value).toString()).apply()
    }
    
    private suspend fun cleanupCache() {
        // Remove expired items
        offlineDb.execSQL(
            "DELETE FROM offline_cache WHERE expires_at IS NOT NULL AND expires_at < ?",
            arrayOf(LocalDateTime.now().toEpochSecond(java.time.ZoneOffset.UTC))
        )
        
        // Check cache size and remove oldest items if needed
        val config = getOfflineConfiguration()
        val cursor = offlineDb.rawQuery("SELECT SUM(LENGTH(data)) FROM offline_cache", null)
        cursor.moveToFirst()
        val currentSize = cursor.getLong(0)
        cursor.close()
        
        if (currentSize > config.cacheConfiguration.maxCacheSize) {
            // Remove oldest items until under limit
            offlineDb.execSQL(
                """
                DELETE FROM offline_cache 
                WHERE id IN (
                    SELECT id FROM offline_cache 
                    ORDER BY last_accessed_at ASC 
                    LIMIT 100
                )
                """
            )
        }
    }
    
    private fun parseMetadata(json: String): CacheMetadata {
        val obj = JSONObject(json)
        return CacheMetadata(
            version = obj.optInt("version", 1),
            checksum = obj.optString("checksum", ""),
            size = obj.optLong("size", 0),
            compressionType = obj.optString("compressionType")?.let { 
                CompressionType.valueOf(it) 
            },
            encryptionType = obj.optString("encryptionType")?.let { 
                EncryptionType.valueOf(it) 
            }
        )
    }
    
    // Extension functions
    private fun JSONObject.toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        keys().forEach { key ->
            map[key] = get(key)
        }
        return map
    }
}

// Data classes for internal use
private data class BatchSyncResult(
    val synced: Int,
    val failed: Int,
    val conflicts: Int,
    val errors: List<SyncError>
)

private data class IntegrityCheckResult(
    val itemsChecked: Int,
    val issues: List<CorruptedItem>,
    val oldItems: Int = 0
)

private suspend fun OfflineRepository.performDownstreamSync(): Any {
    // Placeholder for downstream sync logic
    return Unit
}

private suspend fun OfflineRepository.checkSyncQueueIntegrity(): IntegrityCheckResult {
    // Check sync queue for issues
    return IntegrityCheckResult(0, emptyList())
}

private suspend fun OfflineRepository.checkCacheIntegrity(): IntegrityCheckResult {
    // Check cache for issues
    return IntegrityCheckResult(0, emptyList())
}

private suspend fun OfflineRepository.repairCorruptedItem(item: CorruptedItem): Boolean {
    // Attempt to repair corrupted item
    return false
}

private suspend fun OfflineRepository.forceSyncItem(item: OfflineSyncItem) {
    // Force sync with local data winning
}

private suspend fun OfflineRepository.discardLocalChanges(item: OfflineSyncItem) {
    // Discard local changes
}

private suspend fun OfflineRepository.mergeChanges(item: OfflineSyncItem) {
    // Merge local and server changes
}

private suspend fun OfflineRepository.markForManualResolution(item: OfflineSyncItem) {
    // Mark item for manual conflict resolution
}