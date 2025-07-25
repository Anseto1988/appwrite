package com.example.snacktrack.ui.screens.offline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.snacktrack.data.model.*
import com.example.snacktrack.ui.components.CommonTopAppBar
import com.example.snacktrack.ui.viewmodel.OfflineViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineSettingsScreen(
    navController: NavController,
    viewModel: OfflineViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val networkState by viewModel.networkState.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val queueStatus by viewModel.queueStatus.collectAsState()
    
    var showSyncDetails by remember { mutableStateOf(false) }
    var showCacheDetails by remember { mutableStateOf(false) }
    var showConflictDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Offline-Einstellungen",
                navController = navController,
                actions = {
                    if (syncState.syncInProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 16.dp),
                            strokeWidth = 2.dp
                        )
                    } else if (queueStatus.pendingItems > 0) {
                        IconButton(onClick = { viewModel.performSync() }) {
                            Badge(
                                modifier = Modifier.offset(x = 8.dp, y = (-8).dp)
                            ) {
                                Text("${queueStatus.pendingItems}")
                            }
                            Icon(Icons.Default.Sync, contentDescription = "Sync")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Network Status Card
            item {
                NetworkStatusCard(networkState)
            }
            
            // Sync Status Card
            item {
                SyncStatusCard(
                    syncState = syncState,
                    queueStatus = queueStatus,
                    onSyncClick = { viewModel.performSync() },
                    onDetailsClick = { showSyncDetails = true }
                )
            }
            
            // Offline Mode Settings
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Offline-Modus",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Offline-Modus aktiviert")
                            Switch(
                                checked = uiState.configuration.isEnabled,
                                onCheckedChange = { viewModel.setOfflineEnabled(it) }
                            )
                        }
                        
                        // Offline Mode Type
                        Text(
                            "Modus-Typ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OfflineMode.values().forEachIndexed { index, mode ->
                                SegmentedButton(
                                    selected = uiState.configuration.offlineMode == mode,
                                    onClick = { viewModel.setOfflineMode(mode) },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = OfflineMode.values().size
                                    )
                                ) {
                                    Text(
                                        when (mode) {
                                            OfflineMode.DISABLED -> "Aus"
                                            OfflineMode.MANUAL -> "Manuell"
                                            OfflineMode.AUTO -> "Auto"
                                            OfflineMode.AGGRESSIVE -> "Aggressiv"
                                        }
                                    )
                                }
                            }
                        }
                        
                        Text(
                            when (uiState.configuration.offlineMode) {
                                OfflineMode.DISABLED -> "Keine Offline-Funktionalität"
                                OfflineMode.MANUAL -> "Daten werden nur auf Anfrage gespeichert"
                                OfflineMode.AUTO -> "Wichtige Daten werden automatisch gespeichert"
                                OfflineMode.AGGRESSIVE -> "Alle Daten werden für Offline-Nutzung gespeichert"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Sync Configuration
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Synchronisierung",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { showSyncDetails = true }) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = "Details",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Automatische Synchronisierung")
                            Switch(
                                checked = uiState.configuration.syncConfiguration.autoSync,
                                onCheckedChange = { viewModel.setAutoSync(it) }
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Nur über WLAN")
                            Switch(
                                checked = uiState.configuration.syncConfiguration.syncOnWifiOnly,
                                onCheckedChange = { viewModel.setSyncOnWifiOnly(it) },
                                enabled = uiState.configuration.syncConfiguration.autoSync
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Nur beim Laden")
                            Switch(
                                checked = uiState.configuration.syncConfiguration.syncOnCharging,
                                onCheckedChange = { viewModel.setSyncOnCharging(it) },
                                enabled = uiState.configuration.syncConfiguration.autoSync
                            )
                        }
                        
                        // Sync Interval
                        if (uiState.configuration.syncConfiguration.autoSync) {
                            Text(
                                "Synchronisierungsintervall",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            var expanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = it }
                            ) {
                                OutlinedTextField(
                                    value = when (uiState.configuration.syncConfiguration.syncInterval) {
                                        SyncInterval.ON_APP_OPEN -> "Bei App-Start"
                                        SyncInterval.EVERY_HOUR -> "Stündlich"
                                        SyncInterval.EVERY_6_HOURS -> "Alle 6 Stunden"
                                        SyncInterval.DAILY -> "Täglich"
                                        SyncInterval.ON_DEMAND -> "Manuell"
                                    },
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    SyncInterval.values().forEach { interval ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    when (interval) {
                                                        SyncInterval.ON_APP_OPEN -> "Bei App-Start"
                                                        SyncInterval.EVERY_HOUR -> "Stündlich"
                                                        SyncInterval.EVERY_6_HOURS -> "Alle 6 Stunden"
                                                        SyncInterval.DAILY -> "Täglich"
                                                        SyncInterval.ON_DEMAND -> "Manuell"
                                                    }
                                                )
                                            },
                                            onClick = {
                                                viewModel.setSyncInterval(interval)
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Cache Configuration
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Cache-Einstellungen",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { showCacheDetails = true }) {
                                Icon(
                                    Icons.Default.Storage,
                                    contentDescription = "Cache Details",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        // Cache Size
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Cache-Größe")
                                Text(
                                    "${uiState.cacheSize / 1024 / 1024} MB / ${uiState.configuration.cacheConfiguration.maxCacheSize / 1024 / 1024} MB",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            LinearProgressIndicator(
                                progress = (uiState.cacheSize.toFloat() / uiState.configuration.cacheConfiguration.maxCacheSize).coerceIn(0f, 1f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Komprimierung aktiviert")
                            Switch(
                                checked = uiState.configuration.cacheConfiguration.enableCompression,
                                onCheckedChange = { viewModel.setCacheCompression(it) }
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Verschlüsselung aktiviert")
                            Switch(
                                checked = uiState.configuration.cacheConfiguration.enableEncryption,
                                onCheckedChange = { viewModel.setCacheEncryption(it) }
                            )
                        }
                        
                        // Image Quality
                        Text(
                            "Bildqualität im Cache",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ImageCacheQuality.values().forEachIndexed { index, quality ->
                                SegmentedButton(
                                    selected = uiState.configuration.cacheConfiguration.imageQuality == quality,
                                    onClick = { viewModel.setImageCacheQuality(quality) },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = ImageCacheQuality.values().size
                                    )
                                ) {
                                    Text(
                                        when (quality) {
                                            ImageCacheQuality.LOW -> "Niedrig"
                                            ImageCacheQuality.MEDIUM -> "Mittel"
                                            ImageCacheQuality.HIGH -> "Hoch"
                                            ImageCacheQuality.ORIGINAL -> "Original"
                                        }
                                    )
                                }
                            }
                        }
                        
                        OutlinedButton(
                            onClick = { viewModel.clearCache() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cache leeren")
                        }
                    }
                }
            }
            
            // Conflict Handling
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Konfliktbehandlung",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (queueStatus.conflicts > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error
                                ) {
                                    Text("${queueStatus.conflicts}")
                                }
                            }
                        }
                        
                        Text(
                            "Standard-Konfliktlösung",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                ConflictResolutionStrategy.LOCAL_WINS to "Lokal",
                                ConflictResolutionStrategy.SERVER_WINS to "Server",
                                ConflictResolutionStrategy.MANUAL to "Manuell"
                            ).forEachIndexed { index, (strategy, label) ->
                                SegmentedButton(
                                    selected = uiState.configuration.conflictHandling.defaultStrategy == strategy,
                                    onClick = { viewModel.setDefaultConflictStrategy(strategy) },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = 3
                                    )
                                ) {
                                    Text(label)
                                }
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Konfliktdialog anzeigen")
                            Switch(
                                checked = uiState.configuration.conflictHandling.showConflictDialog,
                                onCheckedChange = { viewModel.setShowConflictDialog(it) }
                            )
                        }
                        
                        if (queueStatus.conflicts > 0) {
                            Button(
                                onClick = { showConflictDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("${queueStatus.conflicts} Konflikte lösen")
                            }
                        }
                    }
                }
            }
            
            // Data Retention
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Datenaufbewahrung",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Wichtige Daten behalten")
                            Switch(
                                checked = uiState.configuration.dataRetentionPolicy.keepEssentialData,
                                onCheckedChange = { viewModel.setKeepEssentialData(it) }
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Alte Daten löschen")
                            Switch(
                                checked = uiState.configuration.dataRetentionPolicy.deleteOldData,
                                onCheckedChange = { viewModel.setDeleteOldData(it) }
                            )
                        }
                        
                        if (uiState.configuration.dataRetentionPolicy.deleteOldData) {
                            Text(
                                "Daten älter als 30 Tage werden automatisch gelöscht",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Favoriten behalten")
                            Switch(
                                checked = uiState.configuration.dataRetentionPolicy.keepFavorites,
                                onCheckedChange = { viewModel.setKeepFavorites(it) }
                            )
                        }
                    }
                }
            }
            
            // Integrity Check
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { viewModel.performIntegrityCheck() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    "Integritätsprüfung",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "Offline-Daten auf Fehler prüfen",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
    
    // Sync Details Dialog
    if (showSyncDetails) {
        SyncDetailsDialog(
            syncState = syncState,
            queueStatus = queueStatus,
            onDismiss = { showSyncDetails = false }
        )
    }
    
    // Cache Details Dialog
    if (showCacheDetails) {
        CacheDetailsDialog(
            cacheSize = uiState.cacheSize,
            cacheStats = uiState.cacheStats,
            onDismiss = { showCacheDetails = false },
            onClearCache = { viewModel.clearCache() }
        )
    }
    
    // Conflict Resolution Dialog
    if (showConflictDialog) {
        ConflictResolutionDialog(
            conflicts = uiState.conflicts,
            onResolve = { conflictId, strategy ->
                viewModel.resolveConflict(conflictId, strategy)
            },
            onDismiss = { showConflictDialog = false }
        )
    }
}

@Composable
private fun NetworkStatusCard(networkState: NetworkState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (networkState.connectionQuality) {
                ConnectionQuality.EXCELLENT -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                ConnectionQuality.GOOD -> Color(0xFF2196F3).copy(alpha = 0.1f)
                ConnectionQuality.MODERATE -> Color(0xFFFF9800).copy(alpha = 0.1f)
                ConnectionQuality.POOR -> Color(0xFFF44336).copy(alpha = 0.1f)
                ConnectionQuality.UNKNOWN -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            if (networkState.isConnected) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                )
                
                Column {
                    Text(
                        if (networkState.isConnected) "Online" else "Offline",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        when (networkState.connectionType) {
                            ConnectionType.WIFI -> "WLAN"
                            ConnectionType.MOBILE_2G -> "2G"
                            ConnectionType.MOBILE_3G -> "3G"
                            ConnectionType.MOBILE_4G -> "4G"
                            ConnectionType.MOBILE_5G -> "5G"
                            ConnectionType.ETHERNET -> "Ethernet"
                            ConnectionType.NONE -> "Keine Verbindung"
                            ConnectionType.OTHER -> "Andere"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (networkState.isConnected) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    networkState.bandwidthKbps?.let { bandwidth ->
                        Text(
                            "${bandwidth / 1000} Mbps",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (networkState.isMetered) {
                        Text(
                            "Kostenpflichtig",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncStatusCard(
    syncState: SyncState,
    queueStatus: OfflineQueueStatus,
    onSyncClick: () -> Unit,
    onDetailsClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Synchronisierungsstatus",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                if (syncState.syncInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(onClick = onDetailsClick) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Details"
                        )
                    }
                }
            }
            
            if (syncState.syncInProgress) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Synchronisierung läuft...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            } else {
                // Sync Status Summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SyncMetric(
                        value = "${queueStatus.pendingItems}",
                        label = "Ausstehend",
                        color = if (queueStatus.pendingItems > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SyncMetric(
                        value = "${queueStatus.failedItems}",
                        label = "Fehlgeschlagen",
                        color = if (queueStatus.failedItems > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SyncMetric(
                        value = "${syncState.conflicts}",
                        label = "Konflikte",
                        color = if (syncState.conflicts > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                syncState.lastSyncTimestamp?.let { lastSync ->
                    Text(
                        "Letzte Synchronisierung: ${lastSync.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (queueStatus.pendingItems > 0 || queueStatus.failedItems > 0) {
                    Button(
                        onClick = onSyncClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Jetzt synchronisieren")
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncMetric(
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SyncDetailsDialog(
    syncState: SyncState,
    queueStatus: OfflineQueueStatus,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Synchronisierungsdetails")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Queue Status
                Text(
                    "Warteschlange",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Gesamt:")
                    Text("${queueStatus.totalItems}")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Ausstehend:")
                    Text("${queueStatus.pendingItems}")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Fehlgeschlagen:")
                    Text("${queueStatus.failedItems}")
                }
                
                queueStatus.oldestItem?.let { oldest ->
                    Text(
                        "Ältester Eintrag: ${oldest.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                Divider()
                
                // Last Sync Result
                syncState.lastSyncResult?.let { result ->
                    Text(
                        "Letztes Ergebnis",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Synchronisiert:")
                        Text("${result.itemsSynced}")
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Fehlgeschlagen:")
                        Text("${result.itemsFailed}")
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Dauer:")
                        Text("${result.duration / 1000}s")
                    }
                }
                
                // Sync History
                if (syncState.syncHistory.isNotEmpty()) {
                    Divider()
                    Text(
                        "Verlauf",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    syncState.syncHistory.takeLast(5).forEach { history ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                history.timestamp.format(DateTimeFormatter.ofPattern("dd.MM. HH:mm")),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                if (history.result.success) "✓ ${history.result.itemsSynced}" else "✗ ${history.result.itemsFailed}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (history.result.success) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Schließen")
            }
        }
    )
}

@Composable
private fun CacheDetailsDialog(
    cacheSize: Long,
    cacheStats: CachePerformanceStats,
    onDismiss: () -> Unit,
    onClearCache: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Cache-Details")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cache Size
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Größe:")
                    Text("${cacheSize / 1024 / 1024} MB")
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Elemente:")
                    Text("${cacheStats.itemsCached}")
                }
                
                Divider()
                
                // Performance Stats
                Text(
                    "Leistung",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Treffer:")
                    Text("${cacheStats.cacheHits}")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Fehlschläge:")
                    Text("${cacheStats.cacheMisses}")
                }
                
                val hitRate = if (cacheStats.cacheHits + cacheStats.cacheMisses > 0) {
                    (cacheStats.cacheHits.toFloat() / (cacheStats.cacheHits + cacheStats.cacheMisses) * 100).toInt()
                } else 0
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Trefferquote:")
                    Text("$hitRate%")
                }
                
                LinearProgressIndicator(
                    progress = hitRate / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                
                if (cacheStats.compressionRatio > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Komprimierung:")
                        Text("${(cacheStats.compressionRatio * 100).toInt()}%")
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Ø Zugriffszeit:")
                    Text("${cacheStats.averageAccessTime} ms")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Schließen")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onClearCache()
                    onDismiss()
                }
            ) {
                Text("Cache leeren", color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

@Composable
private fun ConflictResolutionDialog(
    conflicts: List<SyncConflict>,
    onResolve: (String, ConflictResolutionStrategy) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Konflikte lösen")
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(conflicts.size) { index ->
                    val conflict = conflicts[index]
                    ConflictItem(
                        conflict = conflict,
                        onResolve = { strategy ->
                            onResolve(conflict.id, strategy)
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fertig")
            }
        }
    )
}

@Composable
private fun ConflictItem(
    conflict: SyncConflict,
    onResolve: (ConflictResolutionStrategy) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "${conflict.entityType} - ${conflict.entityId}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                "Lokal: ${conflict.localTimestamp.format(DateTimeFormatter.ofPattern("dd.MM. HH:mm"))}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "Server: ${conflict.serverTimestamp.format(DateTimeFormatter.ofPattern("dd.MM. HH:mm"))}",
                style = MaterialTheme.typography.bodySmall
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = { onResolve(ConflictResolutionStrategy.LOCAL_WINS) }
                ) {
                    Text("Lokal")
                }
                OutlinedButton(
                    onClick = { onResolve(ConflictResolutionStrategy.SERVER_WINS) }
                ) {
                    Text("Server")
                }
                OutlinedButton(
                    onClick = { onResolve(ConflictResolutionStrategy.MERGE) }
                ) {
                    Text("Zusammenführen")
                }
            }
        }
    }
}

// Data class for UI
data class SyncConflict(
    val id: String,
    val entityType: EntityType,
    val entityId: String,
    val localTimestamp: LocalDateTime,
    val serverTimestamp: LocalDateTime,
    val localData: Any,
    val serverData: Any
)