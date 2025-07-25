package com.example.snacktrack.ui.screens.prevention

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.snacktrack.data.model.*
import com.example.snacktrack.ui.viewmodel.PreventionViewModel
import com.example.snacktrack.ui.viewmodel.PreventionViewModelFactory
import com.example.snacktrack.data.service.AppwriteService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Custom Color definitions
private val Orange = Color(0xFFFF9800)
private val Purple = Color(0xFF9C27B0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreventionDashboardScreen(
    navController: NavController,
    dogId: String,
    appwriteService: AppwriteService,
    viewModel: PreventionViewModel = viewModel(
        factory = PreventionViewModelFactory(appwriteService, dogId)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    
    val tabs = listOf(
        "Übersicht" to Icons.Default.Dashboard,
        "Gewicht" to Icons.Default.FitnessCenter,
        "Allergien" to Icons.Default.Warning,
        "Vorsorge" to Icons.Default.LocalHospital,
        "Impfungen" to Icons.Default.Vaccines,
        "Zähne" to Icons.Default.MedicalServices
    )
    
    LaunchedEffect(Unit) {
        viewModel.loadPreventionData()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Präventions-Dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Proaktive Gesundheitsvorsorge für ${uiState.dogName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Tab Bar
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, (title, icon) ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) },
                    icon = { Icon(icon, contentDescription = title) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tab Content
        when (selectedTab) {
            0 -> OverviewTab(uiState, viewModel)
            1 -> WeightManagementTab(uiState, viewModel)
            2 -> AllergyPreventionTab(uiState, viewModel)
            3 -> HealthScreeningTab(uiState, viewModel)
            4 -> VaccinationTab(uiState, viewModel)
            5 -> DentalCareTab(uiState, viewModel)
        }
    }
    
    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show error snackbar
        }
    }
    
    // Success message
    uiState.successMessage?.let { message ->
        LaunchedEffect(message) {
            // Show success snackbar
            viewModel.clearSuccessMessage()
        }
    }
    
    // Loading indicator
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun OverviewTab(
    uiState: com.example.snacktrack.ui.viewmodel.PreventionUiState,
    viewModel: PreventionViewModel
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Health Status Overview
        item {
            HealthStatusCard(uiState.riskAssessment)
        }
        
        // Quick Actions
        item {
            QuickActionsCard(viewModel)
        }
        
        // Upcoming Tasks
        item {
            UpcomingTasksCard(uiState.upcomingTasks)
        }
        
        // Recent Activities
        item {
            RecentActivitiesCard(uiState.recentActivities)
        }
        
        // Prevention Analytics
        if (uiState.analytics != null) {
            item {
                AnalyticsOverviewCard(uiState.analytics)
            }
        }
    }
}

@Composable
private fun HealthStatusCard(riskAssessment: PreventionRiskAssessment?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                riskAssessment == null -> MaterialTheme.colorScheme.surfaceVariant
                riskAssessment.overallRiskScore < 0.3 -> Color.Green.copy(alpha = 0.1f)
                riskAssessment.overallRiskScore < 0.6 -> Color.Yellow.copy(alpha = 0.1f)
                else -> Color.Red.copy(alpha = 0.1f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gesundheitsstatus",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                riskAssessment?.let { assessment ->
                    val riskLevel = when {
                        assessment.overallRiskScore < 0.3 -> "Niedrig"
                        assessment.overallRiskScore < 0.6 -> "Mittel"
                        else -> "Hoch"
                    }
                    
                    Badge(
                        containerColor = when {
                            assessment.overallRiskScore < 0.3 -> Color.Green
                            assessment.overallRiskScore < 0.6 -> Color.Yellow
                            else -> Color.Red
                        }
                    ) {
                        Text(
                            text = riskLevel,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (riskAssessment != null) {
                // Risk Score Progress
                LinearProgressIndicator(
                    progress = { riskAssessment.overallRiskScore.toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                    color = when {
                        riskAssessment.overallRiskScore < 0.3 -> Color.Green
                        riskAssessment.overallRiskScore < 0.6 -> Color.Yellow
                        else -> Color.Red
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Risiko-Score: ${(riskAssessment.overallRiskScore * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Top Risks
                if (riskAssessment.recommendations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Wichtigste Empfehlungen:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    riskAssessment.recommendations.take(3).forEach { recommendation ->
                        Text(
                            text = "• ${recommendation.strategy}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = "Klicken Sie auf 'Risikobewertung erstellen', um eine umfassende Gesundheitsanalyse zu erhalten.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QuickActionsCard(viewModel: PreventionViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Schnellaktionen",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.Assessment,
                    text = "Risikobewertung",
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.generateRiskAssessment()
                }
                
                QuickActionButton(
                    icon = Icons.Default.Schedule,
                    text = "Termin planen",
                    modifier = Modifier.weight(1f)
                ) {
                    // Navigate to scheduling
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.MedicalServices,
                    text = "Zahnpflege",
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.logDentalCare()
                }
                
                QuickActionButton(
                    icon = Icons.Default.Notifications,
                    text = "Erinnerung",
                    modifier = Modifier.weight(1f)
                ) {
                    // Set reminder
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun UpcomingTasksCard(tasks: List<PreventionTask>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Anstehende Aufgaben",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (tasks.isEmpty()) {
                Text(
                    text = "Keine anstehenden Aufgaben",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                tasks.take(5).forEach { task ->
                    TaskItem(task = task)
                    if (task != tasks.last()) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskItem(task: PreventionTask) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (task.category) {
                PreventionCategory.DENTAL -> Icons.Default.MedicalServices
                PreventionCategory.MEDICAL -> Icons.Default.LocalHospital
                PreventionCategory.NUTRITION -> Icons.Default.Restaurant
                PreventionCategory.EXERCISE -> Icons.Default.FitnessCenter
                else -> Icons.Default.Task
            },
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = when (task.priority) {
                RecommendationPriority.URGENT -> Color.Red
                RecommendationPriority.HIGH -> Orange
                RecommendationPriority.MEDIUM -> Color.Blue
                RecommendationPriority.LOW -> Color.Gray
            }
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = task.dueDate?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: "Kein Fälligkeitsdatum",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Badge(
            containerColor = when (task.priority) {
                RecommendationPriority.URGENT -> Color.Red
                RecommendationPriority.HIGH -> Orange
                RecommendationPriority.MEDIUM -> Color.Blue
                RecommendationPriority.LOW -> Color.Gray
            }
        ) {
            Text(
                text = when (task.priority) {
                    RecommendationPriority.URGENT -> "!"
                    RecommendationPriority.HIGH -> "H"
                    RecommendationPriority.MEDIUM -> "M"
                    RecommendationPriority.LOW -> "L"
                },
                color = Color.White,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun RecentActivitiesCard(activities: List<PreventionActivity>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Letzte Aktivitäten",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (activities.isEmpty()) {
                Text(
                    text = "Keine Aktivitäten aufgezeichnet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                activities.take(5).forEach { activity ->
                    ActivityItem(activity = activity)
                    if (activity != activities.last()) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityItem(activity: PreventionActivity) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (activity.type) {
                "dental_care" -> Icons.Default.MedicalServices
                "vaccination" -> Icons.Default.Vaccines
                "screening" -> Icons.Default.LocalHospital
                "weight_check" -> Icons.Default.FitnessCenter
                else -> Icons.Default.Event
            },
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = activity.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = activity.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (activity.notes.isNotEmpty()) {
                Text(
                    text = activity.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun AnalyticsOverviewCard(analytics: PreventionAnalytics) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Präventions-Analyse",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Compliance Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Befolgung:", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${analytics.complianceMetrics.overallCompliance.toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        analytics.complianceMetrics.overallCompliance >= 80 -> Color.Green
                        analytics.complianceMetrics.overallCompliance >= 60 -> Orange
                        else -> Color.Red
                    }
                )
            }
            
            LinearProgressIndicator(
                progress = { (analytics.complianceMetrics.overallCompliance / 100).toFloat() },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                color = when {
                    analytics.complianceMetrics.overallCompliance >= 80 -> Color.Green
                    analytics.complianceMetrics.overallCompliance >= 60 -> Orange
                    else -> Color.Red
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Cost-Benefit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("ROI:", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${analytics.costBenefitAnalysis.returnOnInvestment}x",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Green
                )
            }
            
            // Health Improvements
            if (analytics.healthImprovements.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Verbesserungen:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                
                analytics.healthImprovements.take(3).forEach { improvement ->
                    Text(
                        text = "• ${improvement.metric}: ${improvement.improvement} ${improvement.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp),
                        color = if (improvement.improvement > 0) Color.Green else Color.Red
                    )
                }
            }
        }
    }
}

// Weight Management Tab
@Composable
private fun WeightManagementTab(
    uiState: com.example.snacktrack.ui.viewmodel.PreventionUiState,
    viewModel: PreventionViewModel
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Current Weight Goal
        uiState.weightGoals.firstOrNull()?.let { goal ->
            item {
                WeightGoalCard(goal, viewModel)
            }
        } ?: item {
            CreateWeightGoalCard(viewModel)
        }
        
        // Weight History Chart
        item {
            WeightHistoryCard(uiState.weightHistory)
        }
        
        // Weight Goals List
        if (uiState.weightGoals.isNotEmpty()) {
            items(uiState.weightGoals) { goal ->
                WeightGoalItem(goal, viewModel)
            }
        }
    }
}

@Composable
private fun WeightGoalCard(goal: WeightGoal, viewModel: PreventionViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (goal.status) {
                GoalStatus.ACTIVE -> MaterialTheme.colorScheme.primaryContainer
                GoalStatus.COMPLETED -> Color.Green.copy(alpha = 0.1f)
                GoalStatus.FAILED -> Color.Red.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aktuelles Gewichtsziel",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Badge(
                    containerColor = when (goal.status) {
                        GoalStatus.ACTIVE -> MaterialTheme.colorScheme.primary
                        GoalStatus.COMPLETED -> Color.Green
                        GoalStatus.FAILED -> Color.Red
                        else -> Color.Gray
                    }
                ) {
                    Text(
                        text = when (goal.status) {
                            GoalStatus.ACTIVE -> "Aktiv"
                            GoalStatus.COMPLETED -> "Erreicht"
                            GoalStatus.FAILED -> "Fehlgeschlagen"
                            GoalStatus.PAUSED -> "Pausiert"
                            else -> "Entwurf"
                        },
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress
            LinearProgressIndicator(
                progress = { (goal.progress.percentComplete / 100).toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color = when {
                    goal.progress.percentComplete >= 80 -> Color.Green
                    goal.progress.percentComplete >= 50 -> Orange
                    else -> Color.Blue
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Aktuell: ${goal.currentWeight} kg",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Ziel: ${goal.targetWeight} kg",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${goal.progress.percentComplete.toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (goal.progress.isOnTrack) "Im Plan" else "Verzögert",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (goal.progress.isOnTrack) Color.Green else Color.Red
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.updateWeightGoal(goal.id, goal.currentWeight + 0.1) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Gewicht aktualisieren")
                }
                
                FilledTonalButton(
                    onClick = { /* Edit goal */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Bearbeiten")
                }
            }
        }
    }
}

@Composable
private fun CreateWeightGoalCard(viewModel: PreventionViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Gewichtsziel erstellen",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Erstellen Sie ein Gewichtsziel für optimale Gesundheit",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            FilledTonalButton(
                onClick = { viewModel.createWeightGoal() }
            ) {
                Text("Ziel erstellen")
            }
        }
    }
}

@Composable
private fun WeightHistoryCard(weightHistory: List<WeightEntry>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Gewichtsverlauf",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (weightHistory.isEmpty()) {
                Text(
                    text = "Keine Gewichtsdaten verfügbar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Simple weight chart placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Gewichtsdiagramm\n(${weightHistory.size} Einträge)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun WeightGoalItem(goal: WeightGoal, viewModel: PreventionViewModel) {
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
                Text(
                    text = when (goal.goalType) {
                        WeightGoalType.LOSE_WEIGHT -> "Gewicht verlieren"
                        WeightGoalType.GAIN_WEIGHT -> "Gewicht zunehmen"
                        WeightGoalType.MAINTAIN -> "Gewicht halten"
                        WeightGoalType.BUILD_MUSCLE -> "Muskeln aufbauen"
                        WeightGoalType.SENIOR_MANAGEMENT -> "Senior-Management"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = goal.targetDate.format(DateTimeFormatter.ofPattern("dd.MM.yy")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${goal.startWeight} kg → ${goal.targetWeight} kg",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Allergy Prevention Tab
@Composable
private fun AllergyPreventionTab(
    uiState: com.example.snacktrack.ui.viewmodel.PreventionUiState,
    viewModel: PreventionViewModel
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Known Allergens
        item {
            KnownAllergensCard(uiState.allergyPrevention?.knownAllergens ?: emptyList(), viewModel)
        }
        
        // Suspected Allergens
        item {
            SuspectedAllergensCard(uiState.allergyPrevention?.suspectedAllergens ?: emptyList(), viewModel)
        }
        
        // Elimination Diet
        uiState.allergyPrevention?.eliminationDiet?.let { diet ->
            item {
                EliminationDietCard(diet, viewModel)
            }
        }
        
        // Emergency Plan
        item {
            EmergencyPlanCard(uiState.allergyPrevention?.emergencyPlan ?: EmergencyPlan(), viewModel)
        }
    }
}

@Composable
private fun KnownAllergensCard(allergens: List<KnownAllergen>, viewModel: PreventionViewModel) {
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
                Text(
                    text = "Bekannte Allergene",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                FilledTonalButton(
                    onClick = { viewModel.addKnownAllergen() }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hinzufügen")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (allergens.isEmpty()) {
                Text(
                    text = "Keine bekannten Allergene",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                allergens.forEach { allergen ->
                    AllergenItem(allergen)
                    if (allergen != allergens.last()) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AllergenItem(allergen: KnownAllergen) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = when (allergen.severity) {
                PreventionAllergySeverity.LIFE_THREATENING -> Color.Red
                PreventionAllergySeverity.SEVERE -> Color.Red.copy(alpha = 0.7f)
                PreventionAllergySeverity.MODERATE -> Orange
                PreventionAllergySeverity.MILD -> Color.Yellow
            }
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = allergen.allergen,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Schweregrad: ${
                    when (allergen.severity) {
                        PreventionAllergySeverity.LIFE_THREATENING -> "Lebensbedrohlich"
                        PreventionAllergySeverity.SEVERE -> "Schwer"
                        PreventionAllergySeverity.MODERATE -> "Mittel"
                        PreventionAllergySeverity.MILD -> "Leicht"
                    }
                }",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Badge(
            containerColor = when (allergen.severity) {
                PreventionAllergySeverity.LIFE_THREATENING -> Color.Red
                PreventionAllergySeverity.SEVERE -> Color.Red.copy(alpha = 0.7f)
                PreventionAllergySeverity.MODERATE -> Orange
                PreventionAllergySeverity.MILD -> Color.Yellow
            }
        ) {
            Text(
                text = allergen.severity.name.take(1),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun SuspectedAllergensCard(allergens: List<SuspectedAllergen>, viewModel: PreventionViewModel) {
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
                Text(
                    text = "Verdächtige Allergene",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                FilledTonalButton(
                    onClick = { viewModel.addSuspectedAllergen() }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hinzufügen")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (allergens.isEmpty()) {
                Text(
                    text = "Keine verdächtigen Allergene",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                allergens.forEach { allergen ->
                    SuspectedAllergenItem(allergen, viewModel)
                    if (allergen != allergens.last()) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SuspectedAllergenItem(allergen: SuspectedAllergen, viewModel: PreventionViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.HelpOutline,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = when (allergen.suspicionLevel) {
                SuspicionLevel.VERY_LIKELY -> Color.Red
                SuspicionLevel.PROBABLE -> Orange
                SuspicionLevel.POSSIBLE -> Color.Yellow
                SuspicionLevel.UNLIKELY -> Color.Gray
            }
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = allergen.allergen,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Verdacht: ${
                    when (allergen.suspicionLevel) {
                        SuspicionLevel.VERY_LIKELY -> "Sehr wahrscheinlich"
                        SuspicionLevel.PROBABLE -> "Wahrscheinlich"
                        SuspicionLevel.POSSIBLE -> "Möglich"
                        SuspicionLevel.UNLIKELY -> "Unwahrscheinlich"
                    }
                }",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        OutlinedButton(
            onClick = { viewModel.confirmAllergen(allergen.allergen) }
        ) {
            Text("Bestätigen")
        }
    }
}

@Composable
private fun EliminationDietCard(diet: EliminationDiet, viewModel: PreventionViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Eliminationsdiät",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Phase: ${
                    when (diet.phase) {
                        DietPhase.ELIMINATION -> "Eliminierung"
                        DietPhase.REINTRODUCTION -> "Wiedereinführung"
                        DietPhase.CHALLENGE -> "Herausforderung"
                        DietPhase.MAINTENANCE -> "Erhaltung"
                    }
                }",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "Start: ${diet.startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (diet.baseIngredients.isNotEmpty()) {
                Text(
                    text = "Basis-Zutaten:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = diet.baseIngredients.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            if (diet.eliminatedIngredients.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Eliminierte Zutaten:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = diet.eliminatedIngredients.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun EmergencyPlanCard(plan: EmergencyPlan, viewModel: PreventionViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Red.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Emergency,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color.Red
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Notfallplan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (plan.veterinarianContact.phone.isEmpty()) {
                Text(
                    text = "Kein Notfallplan konfiguriert",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                FilledTonalButton(
                    onClick = { viewModel.createEmergencyPlan() },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Notfallplan erstellen")
                }
            } else {
                // Show emergency contacts and info
                Text(
                    text = "Tierarzt: ${plan.veterinarianContact.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Tel: ${plan.veterinarianContact.phone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (plan.medications.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Notfallmedikamente: ${plan.medications.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Health Screening Tab
@Composable
private fun HealthScreeningTab(
    uiState: com.example.snacktrack.ui.viewmodel.PreventionUiState,
    viewModel: PreventionViewModel
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Schedule Screening
        item {
            ScheduleScreeningCard(viewModel)
        }
        
        // Upcoming Screenings
        if (uiState.healthScreenings.any { it.status == ScreeningStatus.SCHEDULED }) {
            item {
                UpcomingScreeningsCard(
                    uiState.healthScreenings.filter { it.status == ScreeningStatus.SCHEDULED },
                    viewModel
                )
            }
        }
        
        // Screening History
        items(uiState.healthScreenings.filter { it.status == ScreeningStatus.COMPLETED }) { screening ->
            ScreeningHistoryItem(screening, viewModel)
        }
    }
}

@Composable
private fun ScheduleScreeningCard(viewModel: PreventionViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Vorsorgeuntersuchung planen",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Regelmäßige Vorsorgeuntersuchungen sind wichtig für die Gesundheit Ihres Hundes.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = { viewModel.scheduleRoutineScreening() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Routine")
                }
                
                FilledTonalButton(
                    onClick = { viewModel.scheduleBreedSpecificScreening() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Rassenspezifisch")
                }
                
                FilledTonalButton(
                    onClick = { viewModel.scheduleSeniorScreening() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Senior")
                }
            }
        }
    }
}

@Composable
private fun UpcomingScreeningsCard(screenings: List<HealthScreening>, viewModel: PreventionViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Anstehende Untersuchungen",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            screenings.forEach { screening ->
                UpcomingScreeningItem(screening, viewModel)
                if (screening != screenings.last()) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun UpcomingScreeningItem(screening: HealthScreening, viewModel: PreventionViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocalHospital,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = when (screening.screeningType) {
                    ScreeningType.ROUTINE -> "Routineuntersuchung"
                    ScreeningType.BREED_SPECIFIC -> "Rassenspezifische Untersuchung"
                    ScreeningType.AGE_BASED -> "Altersgerechte Untersuchung"
                    ScreeningType.SENIOR_PANEL -> "Senior-Untersuchung"
                    else -> "Spezialuntersuchung"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = screening.scheduledDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        OutlinedButton(
            onClick = { viewModel.completeScreening(screening.id) }
        ) {
            Text("Abschließen")
        }
    }
}

@Composable
private fun ScreeningHistoryItem(screening: HealthScreening, viewModel: PreventionViewModel) {
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
                Text(
                    text = when (screening.screeningType) {
                        ScreeningType.ROUTINE -> "Routineuntersuchung"
                        ScreeningType.BREED_SPECIFIC -> "Rassenspezifisch"
                        ScreeningType.AGE_BASED -> "Altersgerecht"
                        ScreeningType.SENIOR_PANEL -> "Senior-Panel"
                        else -> "Spezialuntersuchung"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = screening.completedDate?.format(DateTimeFormatter.ofPattern("dd.MM.yy")) ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            screening.results?.let { results ->
                Spacer(modifier = Modifier.height(8.dp))
                
                Badge(
                    containerColor = when (results.overallStatus) {
                        HealthStatus.EXCELLENT, HealthStatus.NORMAL -> Color.Green
                        HealthStatus.MINOR_CONCERNS -> Color.Yellow
                        HealthStatus.MODERATE_CONCERNS -> Orange
                        HealthStatus.SERIOUS_CONCERNS, HealthStatus.CRITICAL -> Color.Red
                    }
                ) {
                    Text(
                        text = when (results.overallStatus) {
                            HealthStatus.EXCELLENT -> "Ausgezeichnet"
                            HealthStatus.NORMAL -> "Normal"
                            HealthStatus.MINOR_CONCERNS -> "Geringfügige Befunde"
                            HealthStatus.MODERATE_CONCERNS -> "Moderate Befunde"
                            HealthStatus.SERIOUS_CONCERNS -> "Ernste Befunde"
                            HealthStatus.CRITICAL -> "Kritisch"
                        },
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                
                if (results.abnormalFindings.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Auffälligkeiten: ${results.abnormalFindings.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Red
                    )
                }
            }
        }
    }
}

// Vaccination Tab
@Composable
private fun VaccinationTab(
    uiState: com.example.snacktrack.ui.viewmodel.PreventionUiState,
    viewModel: PreventionViewModel
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Vaccination Status
        item {
            VaccinationStatusCard(uiState.vaccinationSchedule?.complianceStatus ?: ComplianceStatus())
        }
        
        // Due Vaccinations
        uiState.vaccinationSchedule?.reminders?.filter { !it.sent }?.let { dueVaccinations ->
            if (dueVaccinations.isNotEmpty()) {
                item {
                    DueVaccinationsCard(dueVaccinations, viewModel)
                }
            }
        }
        
        // Vaccination History
        uiState.vaccinationSchedule?.vaccines?.let { vaccines ->
            items(vaccines) { vaccine ->
                VaccineCard(vaccine, viewModel)
            }
        }
    }
}

@Composable
private fun VaccinationStatusCard(status: ComplianceStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                status.overallCompliance >= 90 -> Color.Green.copy(alpha = 0.1f)
                status.overallCompliance >= 70 -> Color.Yellow.copy(alpha = 0.1f)
                else -> Color.Red.copy(alpha = 0.1f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Impfstatus",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Badge(
                    containerColor = when {
                        status.overallCompliance >= 90 -> Color.Green
                        status.overallCompliance >= 70 -> Color.Yellow
                        else -> Color.Red
                    }
                ) {
                    Text(
                        text = "${status.overallCompliance.toInt()}%",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = { (status.overallCompliance / 100).toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color = when {
                    status.overallCompliance >= 90 -> Color.Green
                    status.overallCompliance >= 70 -> Color.Yellow
                    else -> Color.Red
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Kernimpfungen: ${status.coreVaccineCompliance.toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (status.overdueVaccines.isNotEmpty()) {
                    Text(
                        text = "${status.overdueVaccines.size} überfällig",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Red
                    )
                }
            }
        }
    }
}

@Composable
private fun DueVaccinationsCard(reminders: List<VaccineReminder>, viewModel: PreventionViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Orange.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Vaccines,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Orange
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Fällige Impfungen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            reminders.forEach { reminder ->
                DueVaccinationItem(reminder, viewModel)
                if (reminder != reminders.last()) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun DueVaccinationItem(reminder: VaccineReminder, viewModel: PreventionViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Impfung ${reminder.vaccineId}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Dosis ${reminder.doseNumber}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Fällig: ${reminder.reminderDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}",
                style = MaterialTheme.typography.bodySmall,
                color = Orange
            )
        }
        
        FilledTonalButton(
            onClick = { viewModel.recordVaccination(reminder.vaccineId, reminder.doseNumber) }
        ) {
            Text("Geimpft")
        }
    }
}

@Composable
private fun VaccineCard(vaccine: Vaccine, viewModel: PreventionViewModel) {
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
                Text(
                    text = vaccine.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Badge(
                    containerColor = when (vaccine.type) {
                        VaccineType.CORE -> Color.Red
                        VaccineType.NON_CORE -> Orange
                        VaccineType.LIFESTYLE -> Color.Blue
                        VaccineType.REGIONAL -> Color.Green
                        VaccineType.TRAVEL -> Purple
                    }
                ) {
                    Text(
                        text = when (vaccine.type) {
                            VaccineType.CORE -> "Kern"
                            VaccineType.NON_CORE -> "Optional"
                            VaccineType.LIFESTYLE -> "Lifestyle"
                            VaccineType.REGIONAL -> "Regional"
                            VaccineType.TRAVEL -> "Reise"
                        },
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Schutz: ${vaccine.protectionDuration}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (vaccine.doses.isNotEmpty()) {
                Text(
                    text = "Dosen: ${vaccine.doses.filter { it.administeredDate != null }.size}/${vaccine.doses.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Dental Care Tab
@Composable
private fun DentalCareTab(
    uiState: com.example.snacktrack.ui.viewmodel.PreventionUiState,
    viewModel: PreventionViewModel
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Dental Status
        item {
            DentalStatusCard(uiState.dentalCare?.currentStatus ?: DentalStatus())
        }
        
        // Daily Care Routine
        item {
            DailyCareCard(uiState.dentalCare?.careRoutine ?: DentalCareRoutine(), viewModel)
        }
        
        // Professional Cleanings
        item {
            ProfessionalCleaningsCard(uiState.dentalCare?.professionalCleanings ?: emptyList(), viewModel)
        }
        
        // Home Care Log
        item {
            HomeCareLogCard(uiState.dentalCare?.homeCareLogs ?: emptyList(), viewModel)
        }
    }
}

@Composable
private fun DentalStatusCard(status: DentalStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (status.overallHealth) {
                DentalHealth.EXCELLENT, DentalHealth.GOOD -> Color.Green.copy(alpha = 0.1f)
                DentalHealth.FAIR -> Color.Yellow.copy(alpha = 0.1f)
                DentalHealth.POOR, DentalHealth.SEVERE -> Color.Red.copy(alpha = 0.1f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Zahngesundheit",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Badge(
                    containerColor = when (status.overallHealth) {
                        DentalHealth.EXCELLENT, DentalHealth.GOOD -> Color.Green
                        DentalHealth.FAIR -> Color.Yellow
                        DentalHealth.POOR, DentalHealth.SEVERE -> Color.Red
                    }
                ) {
                    Text(
                        text = when (status.overallHealth) {
                            DentalHealth.EXCELLENT -> "Ausgezeichnet"
                            DentalHealth.GOOD -> "Gut"
                            DentalHealth.FAIR -> "Befriedigend"
                            DentalHealth.POOR -> "Schlecht"
                            DentalHealth.SEVERE -> "Schwer"
                        },
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Plaque: ${
                            when (status.plaqueLevel) {
                                PlaqueLevel.NONE -> "Keine"
                                PlaqueLevel.MINIMAL -> "Minimal"
                                PlaqueLevel.MILD -> "Leicht"
                                PlaqueLevel.MODERATE -> "Mäßig"
                                PlaqueLevel.HEAVY -> "Stark"
                                PlaqueLevel.SEVERE -> "Schwer"
                            }
                        }",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (status.gingivitisPresent) "Gingivitis vorhanden" else "Keine Gingivitis",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (status.gingivitisPresent) Color.Red else Color.Green
                    )
                }
                
                Column {
                    if (status.missingTeeth.isNotEmpty()) {
                        Text(
                            text = "${status.missingTeeth.size} fehlende Zähne",
                            style = MaterialTheme.typography.bodySmall,
                            color = Orange
                        )
                    }
                    if (status.problematicTeeth.isNotEmpty()) {
                        Text(
                            text = "${status.problematicTeeth.size} Problemzähne",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Red
                        )
                    }
                }
            }
            
            status.lastExamDate?.let { date ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Letzte Untersuchung: ${date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DailyCareCard(routine: DentalCareRoutine, viewModel: PreventionViewModel) {
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
                Text(
                    text = "Tägliche Pflege",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                FilledTonalButton(
                    onClick = { viewModel.logDentalCare() }
                ) {
                    Text("Pflege aufzeichnen")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Zähneputzen: ${routine.brushingFrequency}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (routine.dentalChews.isNotEmpty()) {
                Text(
                    text = "Kaustangen: ${routine.dentalChews.size} Produkte",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (routine.waterAdditives.isNotEmpty()) {
                Text(
                    text = "Wasserzusätze: ${routine.waterAdditives.size} Produkte",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProfessionalCleaningsCard(cleanings: List<ProfessionalCleaning>, viewModel: PreventionViewModel) {
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
                Text(
                    text = "Professionelle Reinigungen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                FilledTonalButton(
                    onClick = { viewModel.scheduleProfessionalCleaning() }
                ) {
                    Text("Termin vereinbaren")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (cleanings.isEmpty()) {
                Text(
                    text = "Keine professionellen Reinigungen aufgezeichnet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                cleanings.take(3).forEach { cleaning ->
                    ProfessionalCleaningItem(cleaning)
                    if (cleaning != cleanings.last()) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
                
                if (cleanings.size > 3) {
                    Text(
                        text = "... und ${cleanings.size - 3} weitere",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfessionalCleaningItem(cleaning: ProfessionalCleaning) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = cleaning.performedBy,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = cleaning.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (cleaning.findings.isNotEmpty()) {
            Text(
                text = cleaning.findings,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        cleaning.nextRecommendedDate?.let { date ->
            Text(
                text = "Nächste empfohlene Reinigung: ${date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun HomeCareLogCard(logs: List<HomeCareLog>, viewModel: PreventionViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Pflege-Tagebuch",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (logs.isEmpty()) {
                Text(
                    text = "Keine Pflegeeinträge vorhanden",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Show recent care activities
                val recentLogs = logs.sortedByDescending { it.date }.take(5)
                
                recentLogs.forEach { log ->
                    HomeCareLogItem(log)
                    if (log != recentLogs.last()) {
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
                
                if (logs.size > 5) {
                    Text(
                        text = "... und ${logs.size - 5} weitere Einträge",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeCareLogItem(log: HomeCareLog) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (log.careType) {
                HomeCareType.BRUSHING -> Icons.Default.Brush
                HomeCareType.DENTAL_CHEW -> Icons.Default.Pets
                HomeCareType.WATER_ADDITIVE -> Icons.Default.WaterDrop
                HomeCareType.DENTAL_SPRAY -> Icons.Default.LocalDrink
                HomeCareType.DENTAL_WIPE -> Icons.Default.CleaningServices
                HomeCareType.OTHER -> Icons.Default.MoreHoriz
            },
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (log.completed) Color.Green else Color.Gray
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = when (log.careType) {
                    HomeCareType.BRUSHING -> "Zähneputzen"
                    HomeCareType.DENTAL_CHEW -> "Kaustangen"
                    HomeCareType.WATER_ADDITIVE -> "Wasserzusatz"
                    HomeCareType.DENTAL_SPRAY -> "Dentalspray"
                    HomeCareType.DENTAL_WIPE -> "Dentaltücher"
                    HomeCareType.OTHER -> "Sonstige Pflege"
                },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (log.completed) FontWeight.Medium else FontWeight.Normal,
                color = if (log.completed) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (log.notes?.isNotEmpty() == true) {
                Text(
                    text = log.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        
        Text(
            text = log.date.format(DateTimeFormatter.ofPattern("dd.MM")),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Data classes for UI state
data class PreventionTask(
    val id: String,
    val title: String,
    val category: PreventionCategory,
    val priority: RecommendationPriority,
    val dueDate: java.time.LocalDate?
)

data class PreventionActivity(
    val id: String,
    val type: String,
    val title: String,
    val date: LocalDateTime,
    val notes: String
)

// WeightEntry is already defined in data.model package and imported