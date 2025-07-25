package com.example.snacktrack.ui.screens.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.snacktrack.data.model.*
import com.example.snacktrack.ui.components.CommonTopAppBar
import com.example.snacktrack.ui.viewmodel.AIViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIRecommendationsScreen(
    navController: NavController,
    dogId: String,
    viewModel: AIViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    
    LaunchedEffect(dogId) {
        viewModel.loadAIData(dogId)
    }
    
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "KI-Empfehlungen",
                onBackClick = { navController.navigateUp() },
                actions = {
                    IconButton(onClick = { viewModel.refreshAIAnalysis(dogId) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Aktualisieren")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Futter") },
                    icon = { Icon(Icons.Default.Restaurant, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Gewicht") },
                    icon = { Icon(Icons.Default.TrendingUp, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Anomalien") },
                    icon = { Icon(Icons.Default.Warning, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Risiken") },
                    icon = { Icon(Icons.Default.Security, contentDescription = null) }
                )
            }
            
            // Content
            when (selectedTab) {
                0 -> FoodRecommendationsTab(
                    recommendation = uiState.foodRecommendation,
                    isLoading = uiState.isLoading,
                    onTypeChange = { type ->
                        viewModel.generateRecommendations(dogId, type)
                    }
                )
                1 -> WeightPredictionTab(
                    prediction = uiState.weightPrediction,
                    isLoading = uiState.isLoading,
                    onRefresh = { viewModel.predictWeight(dogId) }
                )
                2 -> AnomaliesTab(
                    anomalies = uiState.anomalies,
                    isLoading = uiState.isLoading
                )
                3 -> RiskAssessmentTab(
                    assessment = uiState.healthRiskAssessment,
                    isLoading = uiState.isLoading
                )
            }
        }
    }
}

@Composable
private fun FoodRecommendationsTab(
    recommendation: FoodRecommendation?,
    isLoading: Boolean,
    onTypeChange: (RecommendationType) -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Recommendation Type Selector
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Empfehlungstyp",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AIRecommendationType.values().forEach { type ->
                                FilterChip(
                                    selected = recommendation?.recommendationType == type,
                                    onClick = { onTypeChange(type) },
                                    label = { 
                                        Text(
                                            when(type) {
                                                AIRecommendationType.DAILY -> "Täglich"
                                                AIRecommendationType.WEEKLY -> "Wöchentlich"
                                                AIRecommendationType.TRANSITION -> "Umstellung"
                                                AIRecommendationType.SPECIAL_DIET -> "Spezialdiät"
                                                AIRecommendationType.WEIGHT_LOSS -> "Abnehmen"
                                                AIRecommendationType.WEIGHT_GAIN -> "Zunehmen"
                                            }
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
            
            // AI Confidence Score
            recommendation?.let { rec ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
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
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "KI-Vertrauen",
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            Text(
                                "${(rec.confidenceScore * 100).toInt()}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                // Recommendations
                items(rec.recommendations) { item ->
                    FoodRecommendationCard(recommendation = item)
                }
                
                // Reasoning
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
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Begründung",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                rec.reasoning,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodRecommendationCard(
    recommendation: FoodRecommendationItem
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (recommendation.allergenSafe) 
                MaterialTheme.colorScheme.surfaceVariant
            else Color(0xFFFFEBEE)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        recommendation.foodName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        recommendation.brand,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Nutrition Match Score
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF4CAF50).copy(alpha = 0.2f),
                                    Color(0xFF4CAF50).copy(alpha = 0.1f)
                                )
                            )
                        )
                ) {
                    Text(
                        "${(recommendation.nutritionMatch * 100).toInt()}%",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Amount and Frequency
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Scale,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${recommendation.recommendedAmount.toInt()}g",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
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
                        when(recommendation.frequency) {
                            FeedingFrequency.ONCE_DAILY -> "1x täglich"
                            FeedingFrequency.TWICE_DAILY -> "2x täglich"
                            FeedingFrequency.THREE_TIMES_DAILY -> "3x täglich"
                            FeedingFrequency.FREE_FEEDING -> "Frei"
                            FeedingFrequency.CUSTOM -> "Individuell"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Reason
            Text(
                recommendation.reason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Allergen Warning
            if (!recommendation.allergenSafe) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFFCDD2))
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Enthält möglicherweise Allergene!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD32F2F)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeightPredictionTab(
    prediction: WeightPrediction?,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (prediction == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Keine Gewichtsprognose verfügbar",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRefresh) {
                    Text("Prognose erstellen")
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current Weight Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Aktuelles Gewicht",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "${prediction.currentWeight} kg",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Prediction Chart (simplified)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Gewichtsprognose",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Show next 3 predictions
                        prediction.predictions.take(3).forEach { point ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    point.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Row {
                                    Text(
                                        "${String.format("%.1f", point.predictedWeight)} kg",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "(${(point.confidenceLevel * 100).toInt()}%)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Factors
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Berücksichtigte Faktoren",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val factors = prediction.factors
                        FactorRow("Durchschn. Kalorien", "${factors.averageDailyCalories} kcal/Tag")
                        FactorRow("Aktivitätslevel", factors.activityLevel.displayName)
                        FactorRow("Rasse", factors.breed)
                        FactorRow("Alter", "${factors.age.toInt()} Jahre")
                    }
                }
            }
        }
    }
}

@Composable
private fun AnomaliesTab(
    anomalies: List<EatingAnomaly>,
    isLoading: Boolean
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (anomalies.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Keine Anomalien erkannt",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "Das Fressverhalten ist normal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(anomalies) { anomaly ->
                AnomalyCard(anomaly = anomaly)
            }
        }
    }
}

@Composable
private fun AnomalyCard(
    anomaly: EatingAnomaly
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (anomaly.severity) {
                AnomalySeverity.CRITICAL -> Color(0xFFFFEBEE)
                AnomalySeverity.HIGH -> Color(0xFFFFF3E0)
                AnomalySeverity.MEDIUM -> Color(0xFFFFF8E1)
                AnomalySeverity.LOW -> MaterialTheme.colorScheme.surfaceVariant
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        when (anomaly.severity) {
                            AnomalySeverity.CRITICAL -> Icons.Default.Error
                            AnomalySeverity.HIGH -> Icons.Default.Warning
                            else -> Icons.Default.Info
                        },
                        contentDescription = null,
                        tint = when (anomaly.severity) {
                            AnomalySeverity.CRITICAL -> Color(0xFFD32F2F)
                            AnomalySeverity.HIGH -> Color(0xFFF57C00)
                            AnomalySeverity.MEDIUM -> Color(0xFFFFA000)
                            AnomalySeverity.LOW -> MaterialTheme.colorScheme.primary
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        when (anomaly.anomalyType) {
                            AnomalyType.UNUSUAL_AMOUNT -> "Ungewöhnliche Menge"
                            AnomalyType.SKIPPED_MEALS -> "Übersprungene Mahlzeiten"
                            AnomalyType.TIME_PATTERN -> "Zeitabweichung"
                            AnomalyType.FOOD_REJECTION -> "Futterverweigerung"
                            AnomalyType.RAPID_CONSUMPTION -> "Zu schnelles Fressen"
                            AnomalyType.SLOW_CONSUMPTION -> "Zu langsames Fressen"
                            AnomalyType.FREQUENT_SNACKING -> "Häufige Snacks"
                            AnomalyType.WATER_INTAKE -> "Wasseraufnahme"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    anomaly.detectedAt.format(DateTimeFormatter.ofPattern("dd.MM. HH:mm")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                anomaly.description,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Recommendation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    .padding(12.dp)
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    anomaly.recommendation,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            // Vet attention warning
            if (anomaly.requiresVetAttention) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFCDD2))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocalHospital,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFD32F2F)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Tierarztbesuch empfohlen",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                }
            }
        }
    }
}

@Composable
private fun RiskAssessmentTab(
    assessment: HealthRiskAssessment?,
    isLoading: Boolean
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (assessment == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Keine Risikobewertung verfügbar",
                style = MaterialTheme.typography.titleMedium
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Overall Risk Score
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            assessment.overallRiskScore < 0.3f -> Color(0xFFE8F5E9)
                            assessment.overallRiskScore < 0.6f -> Color(0xFFFFF3E0)
                            else -> Color(0xFFFFEBEE)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Gesundheitsrisiko",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            when {
                                assessment.overallRiskScore < 0.3f -> "Niedrig"
                                assessment.overallRiskScore < 0.6f -> "Mittel"
                                else -> "Hoch"
                            },
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                assessment.overallRiskScore < 0.3f -> Color(0xFF4CAF50)
                                assessment.overallRiskScore < 0.6f -> Color(0xFFFFA000)
                                else -> Color(0xFFD32F2F)
                            }
                        )
                    }
                }
            }
            
            // Risk Factors
            if (assessment.riskFactors.isNotEmpty()) {
                item {
                    Text(
                        "Risikofaktoren",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                items(assessment.riskFactors) { factor ->
                    RiskFactorCard(factor = factor)
                }
            }
            
            // Recommendations
            if (assessment.recommendations.isNotEmpty()) {
                item {
                    Text(
                        "Empfehlungen",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                items(assessment.recommendations) { recommendation ->
                    HealthRecommendationCard(recommendation = recommendation)
                }
            }
        }
    }
}

@Composable
private fun RiskFactorCard(
    factor: RiskFactor
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    factor.factor,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when {
                                factor.severity < 0.3f -> Color(0xFF4CAF50)
                                factor.severity < 0.6f -> Color(0xFFFFA000)
                                else -> Color(0xFFD32F2F)
                            }.copy(alpha = 0.2f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        when {
                            factor.severity < 0.3f -> "Niedrig"
                            factor.severity < 0.6f -> "Mittel"
                            else -> "Hoch"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = when {
                            factor.severity < 0.3f -> Color(0xFF4CAF50)
                            factor.severity < 0.6f -> Color(0xFFFFA000)
                            else -> Color(0xFFD32F2F)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                factor.description,
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (factor.improvementPlan.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Icon(
                        Icons.Default.TipsAndUpdates,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        factor.improvementPlan,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun HealthRecommendationCard(
    recommendation: HealthRecommendation
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (recommendation.priority == 1) 
                MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Priority indicator
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (recommendation.priority == 1) 
                            MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    )
            ) {
                Text(
                    recommendation.priority.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    recommendation.action,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    recommendation.expectedBenefit,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Zeitrahmen: ${recommendation.timeframe}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = if (recommendation.priority == 1) 
                        MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun FactorRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}