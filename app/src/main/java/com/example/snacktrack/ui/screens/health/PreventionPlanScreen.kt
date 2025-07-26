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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.snacktrack.ui.components.CommonTopAppBar
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreventionPlanScreen(
    navController: NavController
) {
    val context = LocalContext.current
    
    // Beispiel-Vorsorgepläne
    val preventionItems = remember {
        listOf(
            PreventionItem(
                "Tollwut-Impfung",
                "Jährliche Auffrischung",
                LocalDate.now().plusMonths(2),
                PreventionType.VACCINATION
            ),
            PreventionItem(
                "Wurmkur",
                "Alle 3 Monate",
                LocalDate.now().plusWeeks(3),
                PreventionType.DEWORMING
            ),
            PreventionItem(
                "Zahnkontrolle",
                "Halbjährlich",
                LocalDate.now().plusMonths(4),
                PreventionType.CHECKUP
            ),
            PreventionItem(
                "DHPP-Impfung",
                "Alle 3 Jahre",
                LocalDate.now().plusMonths(8),
                PreventionType.VACCINATION
            ),
            PreventionItem(
                "Zeckenschutz",
                "Monatlich in der Saison",
                LocalDate.now().plusDays(15),
                PreventionType.TREATMENT
            )
        ).sortedBy { it.dueDate }
    }
    
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Vorsorgeplan",
                onBackClick = { navController.navigateUp() }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Übersicht
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Nächste Termine",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        val upcomingCount = preventionItems.count { 
                            it.dueDate.isBefore(LocalDate.now().plusMonths(1))
                        }
                        
                        Text(
                            "$upcomingCount Termine in den nächsten 30 Tagen",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            
            // Vorsorgetermine
            items(preventionItems) { item ->
                PreventionCard(item = item)
            }
            
            // Hinweise
            item {
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
                                "Wichtige Hinweise",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "• Sprechen Sie alle Vorsorgemaßnahmen mit Ihrem Tierarzt ab\n" +
                                "• Die Termine sind Richtwerte und können je nach Hund variieren\n" +
                                "• Bei Reisen ins Ausland können zusätzliche Impfungen nötig sein",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PreventionCard(item: PreventionItem) {
    val daysUntilDue = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), item.dueDate)
    val isOverdue = daysUntilDue < 0
    val isUrgent = daysUntilDue in 0..7
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isOverdue -> MaterialTheme.colorScheme.errorContainer
                isUrgent -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surface
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
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    when (item.type) {
                        PreventionType.VACCINATION -> Icons.Default.Vaccines
                        PreventionType.DEWORMING -> Icons.Default.Medication
                        PreventionType.CHECKUP -> Icons.Default.MedicalServices
                        PreventionType.TREATMENT -> Icons.Default.HealthAndSafety
                    },
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = when {
                        isOverdue -> MaterialTheme.colorScheme.onErrorContainer
                        isUrgent -> MaterialTheme.colorScheme.onTertiaryContainer
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Fällig: ${item.dueDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            isOverdue -> MaterialTheme.colorScheme.onErrorContainer
                            isUrgent -> MaterialTheme.colorScheme.onTertiaryContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                when {
                    isOverdue -> {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error
                        ) {
                            Text("Überfällig")
                        }
                    }
                    isUrgent -> {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        ) {
                            Text("Dringend")
                        }
                    }
                    else -> {
                        Text(
                            "in $daysUntilDue Tagen",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

data class PreventionItem(
    val name: String,
    val description: String,
    val dueDate: LocalDate,
    val type: PreventionType
)

enum class PreventionType {
    VACCINATION,
    DEWORMING,
    CHECKUP,
    TREATMENT
}