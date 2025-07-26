package com.example.snacktrack.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.snacktrack.ui.components.CommonTopAppBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportImportScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Export & Import",
                onBackClick = { navController.navigateUp() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Export-Karte
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Upload,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Daten exportieren",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Text(
                        "Erstellen Sie eine Sicherungskopie Ihrer Daten. Die Datei enthält alle Ihre Hunde, Fütterungen, Gewichtseinträge und Einstellungen.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Button(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isProcessing
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Daten exportieren")
                        }
                    }
                }
            }
            
            // Import-Karte
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Daten importieren",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Text(
                        "Stellen Sie Ihre Daten aus einer Sicherungsdatei wieder her. Achtung: Bestehende Daten werden überschrieben!",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    OutlinedButton(
                        onClick = { showImportDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isProcessing
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Datei auswählen")
                    }
                }
            }
            
            // Hinweise
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            "Hinweise",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "• Exportierte Dateien werden im Downloads-Ordner gespeichert\n" +
                            "• Das Dateiformat ist JSON\n" +
                            "• Bilder werden als Base64 kodiert\n" +
                            "• Regelmäßige Backups werden empfohlen",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            
            // Nachrichten
            message?.let { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (msg.contains("Fehler")) 
                            MaterialTheme.colorScheme.errorContainer
                        else 
                            MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (msg.contains("Fehler")) Icons.Default.Error else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (msg.contains("Fehler"))
                                MaterialTheme.colorScheme.onErrorContainer
                            else
                                MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            msg,
                            color = if (msg.contains("Fehler"))
                                MaterialTheme.colorScheme.onErrorContainer
                            else
                                MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
    
    // Export-Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Daten exportieren") },
            text = { 
                Text("Möchten Sie wirklich alle Daten exportieren? Die Datei wird im Downloads-Ordner gespeichert.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            isProcessing = true
                            showExportDialog = false
                            // TODO: Export-Logik implementieren
                            kotlinx.coroutines.delay(2000) // Simuliere Export
                            isProcessing = false
                            message = "Daten erfolgreich exportiert!"
                        }
                    }
                ) {
                    Text("Exportieren")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
    
    // Import-Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Daten importieren") },
            text = { 
                Text("WARNUNG: Beim Import werden alle bestehenden Daten überschrieben! Sind Sie sicher, dass Sie fortfahren möchten?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            isProcessing = true
                            showImportDialog = false
                            // TODO: Import-Logik implementieren
                            kotlinx.coroutines.delay(2000) // Simuliere Import
                            isProcessing = false
                            message = "Daten erfolgreich importiert!"
                        }
                    }
                ) {
                    Text("Importieren", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}