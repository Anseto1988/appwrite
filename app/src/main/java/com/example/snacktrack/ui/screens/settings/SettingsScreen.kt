package com.example.snacktrack.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import com.example.snacktrack.ui.navigation.Screen
import com.example.snacktrack.ui.components.CommonTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Einstellungen",
                showBackButton = true,
                onBackClick = { navController.navigateUp() },
                onAdminClick = { },
                onAccountClick = { navController.navigate(Screen.AccountManagement.route) },
                onLogoutClick = { 
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onCommunityClick = { navController.navigate(Screen.Community.route) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Allgemeine Einstellungen
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Allgemein",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.AccountCircle,
                        title = "Account-Verwaltung",
                        subtitle = "E-Mail, Passwort, Teams",
                        onClick = {
                            navController.navigate(Screen.AccountManagement.route)
                        }
                    )
                    
                    Divider()
                    
                    SettingsItem(
                        icon = Icons.Default.Download,
                        title = "Export & Import",
                        subtitle = "Daten sichern und wiederherstellen",
                        onClick = {
                            navController.navigate(Screen.ExportImport.route)
                        }
                    )
                }
            }
            
            // Hunde-Einstellungen
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Hunde",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Restaurant,
                        title = "Futter-Datenbank",
                        subtitle = "Futtermittel verwalten",
                        onClick = {
                            navController.navigate(Screen.FoodDatabase.route)
                        }
                    )
                    
                    Divider()
                    
                    SettingsItem(
                        icon = Icons.Default.Vaccines,
                        title = "Vorsorgeplan",
                        subtitle = "Impfungen und Untersuchungen",
                        onClick = {
                            navController.navigate(Screen.PreventionPlan.route)
                        }
                    )
                }
            }
            
            // Über die App
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Über",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "App-Version",
                        subtitle = "1.0.0",
                        onClick = { }
                    )
                    
                    Divider()
                    
                    SettingsItem(
                        icon = Icons.Default.Policy,
                        title = "Datenschutz",
                        subtitle = "Datenschutzerklärung anzeigen",
                        onClick = { }
                    )
                }
            }
            
            // Logout
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                SettingsItem(
                    icon = Icons.Default.Logout,
                    title = "Abmelden",
                    subtitle = "Aus der App ausloggen",
                    onClick = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = tint
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = tint
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = tint.copy(alpha = 0.7f)
            )
        }
        IconButton(onClick = onClick) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = tint
            )
        }
    }
}