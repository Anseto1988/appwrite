package com.example.snacktrack.ui.screens.health

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.snacktrack.data.model.*
import com.example.snacktrack.ui.components.CommonTopAppBar
import com.example.snacktrack.ui.viewmodel.HealthViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthDashboardScreen(
    navController: NavController,
    dogId: String,
    viewModel: HealthViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(dogId) {
        viewModel.loadHealthData(dogId)
    }
    
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Gesundheits-Dashboard",
                onBackClick = { navController.navigateUp() }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Allergies Section
            item {
                HealthSection(
                    title = "Allergien & Unverträglichkeiten",
                    icon = Icons.Default.Warning,
                    iconTint = MaterialTheme.colorScheme.error,
                    onAddClick = { navController.navigate("health/add-allergy/$dogId") }
                ) {
                    if (uiState.allergies.isEmpty()) {
                        EmptyStateText("Keine Allergien erfasst")
                    } else {
                        uiState.allergies.forEach { allergy ->
                            AllergyCard(allergy = allergy)
                        }
                    }
                }
            }
            
            // Medications Section
            item {
                HealthSection(
                    title = "Medikamente",
                    icon = Icons.Default.Medication,
                    iconTint = MaterialTheme.colorScheme.primary,
                    onAddClick = { navController.navigate("health/add-medication/$dogId") }
                ) {
                    if (uiState.medications.isEmpty()) {
                        EmptyStateText("Keine aktiven Medikamente")
                    } else {
                        uiState.medications.forEach { medication ->
                            MedicationCard(
                                medication = medication,
                                onEditClick = { navController.navigate("health/edit-medication/${medication.id}") }
                            )
                        }
                    }
                }
            }
            
            // Today's Reminders
            if (uiState.todayReminders.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Heutige Erinnerungen",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            uiState.todayReminders.forEach { reminder ->
                                ReminderItem(reminder = reminder)
                            }
                        }
                    }
                }
            }
            
            // Recent Health Entries
            item {
                HealthSection(
                    title = "Gesundheitstagebuch",
                    icon = Icons.Default.Book,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    onAddClick = { navController.navigate("health/add-entry/$dogId") }
                ) {
                    if (uiState.recentEntries.isEmpty()) {
                        EmptyStateText("Keine Einträge vorhanden")
                    } else {
                        uiState.recentEntries.take(5).forEach { entry ->
                            HealthEntryCard(
                                entry = entry,
                                onClick = { navController.navigate("health/entry-detail/${entry.id}") }
                            )
                        }
                        if (uiState.recentEntries.size > 5) {
                            TextButton(
                                onClick = { navController.navigate("health/all-entries/$dogId") },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Alle Einträge anzeigen")
                            }
                        }
                    }
                }
            }
            
            // Health Insights
            if (uiState.insights.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Gesundheits-Insights",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            uiState.insights.forEach { insight ->
                                InsightItem(insight = insight)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onAddClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconTint
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onAddClick) {
                    Icon(Icons.Default.Add, contentDescription = "Hinzufügen")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun AllergyCard(allergy: DogAllergy) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (allergy.severity) {
                DogAllergySeverity.CRITICAL -> Color(0xFFFFEBEE)
                DogAllergySeverity.SEVERE -> Color(0xFFFFF3E0)
                DogAllergySeverity.MODERATE -> Color(0xFFFFF8E1)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    allergy.allergen,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    allergy.severity.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(allergy.severity.colorCode)
                )
            }
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = Color(allergy.severity.colorCode),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun MedicationCard(
    medication: DogMedication,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        medication.medicationName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "${medication.dosage} - ${medication.frequency.displayName}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (medication.foodInteraction != FoodInteraction.NONE) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Restaurant,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                medication.foodInteraction.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Bearbeiten",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReminderItem(reminder: com.example.snacktrack.data.repository.MedicationReminder) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                reminder.scheduledTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            reminder.medication.medicationName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun HealthEntryCard(
    entry: DogHealthEntry,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.entryType.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    entry.entryDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                    style = MaterialTheme.typography.bodySmall
                )
                if (entry.symptoms.isNotEmpty()) {
                    Text(
                        "Symptome: ${entry.symptoms.take(2).joinToString { it.displayName }}${if (entry.symptoms.size > 2) "..." else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun InsightItem(insight: com.example.snacktrack.ui.viewmodel.HealthInsight) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                insight.icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.tertiary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    insight.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    insight.description,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun EmptyStateText(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}