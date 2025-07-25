package com.example.snacktrack.ui.screens.export

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.snacktrack.ui.components.CommonTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportIntegrationScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Export & Integration",
                navController = navController
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
            // Data Export Section
            item {
                SectionHeader(
                    title = "Datenexport",
                    icon = Icons.Default.Download
                )
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigate("export/data") }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Daten exportieren",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Exportiere deine Daten in verschiedenen Formaten",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null
                        )
                    }
                }
            }
            
            // Veterinary Integration Section
            item {
                SectionHeader(
                    title = "Tierarzt-Integration",
                    icon = Icons.Default.LocalHospital
                )
            }
            
            item {
                IntegrationCard(
                    title = "Tierarztpraxis verbinden",
                    description = "Synchronisiere Daten mit deiner Tierarztpraxis",
                    icon = Icons.Default.Sync,
                    onClick = { navController.navigate("export/veterinary") }
                )
            }
            
            // Calendar Integration Section
            item {
                SectionHeader(
                    title = "Kalender-Integration",
                    icon = Icons.Default.CalendarMonth
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CalendarProviderCard(
                        provider = "Google Calendar",
                        icon = Icons.Default.Event,
                        isConnected = false,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("export/calendar/google") }
                    )
                    CalendarProviderCard(
                        provider = "Apple Calendar",
                        icon = Icons.Default.CalendarToday,
                        isConnected = false,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("export/calendar/apple") }
                    )
                }
            }
            
            // Fitness Tracker Integration Section
            item {
                SectionHeader(
                    title = "Fitness Tracker",
                    icon = Icons.Default.Watch
                )
            }
            
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FitnessTrackerCard(
                        name = "FitBark",
                        description = "Aktivitäts- und Schlaftracking",
                        isConnected = false,
                        onClick = { navController.navigate("export/fitness/fitbark") }
                    )
                    FitnessTrackerCard(
                        name = "Whistle",
                        description = "GPS und Aktivitätstracking",
                        isConnected = false,
                        onClick = { navController.navigate("export/fitness/whistle") }
                    )
                }
            }
            
            // Cloud Backup Section
            item {
                SectionHeader(
                    title = "Cloud Backup",
                    icon = Icons.Default.Cloud
                )
            }
            
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
                                "Automatisches Backup",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Switch(
                                checked = false,
                                onCheckedChange = { /* Handle toggle */ }
                            )
                        }
                        
                        LinearProgressIndicator(
                            progress = 0.3f,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "1.5 GB von 5 GB verwendet",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Letztes Backup: Heute 02:00",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { navController.navigate("export/backup/configure") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Konfigurieren")
                            }
                            OutlinedButton(
                                onClick = { /* Trigger backup */ },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Jetzt sichern")
                            }
                        }
                    }
                }
            }
            
            // API Integration Section
            item {
                SectionHeader(
                    title = "API-Integration",
                    icon = Icons.Default.Code
                )
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigate("export/api") }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "API-Zugang verwalten",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Erstelle API-Schlüssel für Drittanbieter-Apps",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Badge {
                            Text("2 aktiv")
                        }
                    }
                }
            }
            
            // Import Section
            item {
                SectionHeader(
                    title = "Datenimport",
                    icon = Icons.Default.Upload
                )
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigate("export/import") }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Daten importieren",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Importiere Daten aus anderen Apps oder Dateien",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Default.FileUpload,
                            contentDescription = null
                        )
                    }
                }
            }
            
            // Sync Settings
            item {
                SectionHeader(
                    title = "Synchronisierung",
                    icon = Icons.Default.SyncAlt
                )
            }
            
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
                                "Automatische Synchronisierung",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Switch(
                                checked = true,
                                onCheckedChange = { /* Handle toggle */ }
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Nur über WLAN",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Switch(
                                checked = true,
                                onCheckedChange = { /* Handle toggle */ }
                            )
                        }
                        
                        Text(
                            "Letzte Synchronisierung: vor 5 Minuten",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        OutlinedButton(
                            onClick = { /* Trigger sync */ },
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
}

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun IntegrationCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        description,
                        style = MaterialTheme.typography.bodyMedium,
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

@Composable
private fun CalendarProviderCard(
    provider: String,
    icon: ImageVector,
    isConnected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                provider,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                if (isConnected) "Verbunden" else "Nicht verbunden",
                style = MaterialTheme.typography.bodySmall,
                color = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FitnessTrackerCard(
    name: String,
    description: String,
    isConnected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isConnected) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text("Verbunden")
                }
            } else {
                TextButton(onClick = onClick) {
                    Text("Verbinden")
                }
            }
        }
    }
}