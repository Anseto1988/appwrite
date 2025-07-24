package com.example.snacktrack.ui.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.snacktrack.data.model.*
import com.example.snacktrack.ui.components.CommonTopAppBar
import com.example.snacktrack.ui.viewmodel.AdvancedStatisticsViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedStatisticsScreen(
    navController: NavController,
    dogId: String,
    viewModel: AdvancedStatisticsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedPeriod by remember { mutableStateOf(AnalyticsPeriod.MONTH) }
    
    LaunchedEffect(dogId, selectedPeriod) {
        viewModel.loadStatistics(dogId, selectedPeriod)
    }
    
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Erweiterte Statistiken",
                navController = navController,
                actions = {
                    IconButton(onClick = { navController.navigate("statistics/custom-report/$dogId") }) {
                        Icon(Icons.Default.Analytics, contentDescription = "Custom Report")
                    }
                    IconButton(onClick = { viewModel.exportStatistics() }) {
                        Icon(Icons.Default.Share, contentDescription = "Export")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Period Selector
                item {
                    PeriodSelector(
                        selectedPeriod = selectedPeriod,
                        onPeriodSelected = { selectedPeriod = it }
                    )
                }
                
                // Overview Cards
                item {
                    OverviewSection(uiState.statistics)
                }
                
                // Weight Analytics
                item {
                    AnalyticsCard(
                        title = "Gewichtsanalyse",
                        icon = Icons.Default.TrendingUp,
                        color = Color(0xFF2196F3)
                    ) {
                        WeightAnalyticsContent(uiState.statistics?.weightAnalytics)
                    }
                }
                
                // Nutrition Analytics
                item {
                    AnalyticsCard(
                        title = "Ernährungsanalyse",
                        icon = Icons.Default.Restaurant,
                        color = Color(0xFF4CAF50)
                    ) {
                        NutritionAnalyticsContent(uiState.statistics?.nutritionAnalytics)
                    }
                }
                
                // Health Analytics
                item {
                    AnalyticsCard(
                        title = "Gesundheitsanalyse",
                        icon = Icons.Default.Favorite,
                        color = Color(0xFFE91E63)
                    ) {
                        HealthAnalyticsContent(uiState.statistics?.healthAnalytics)
                    }
                }
                
                // Activity Analytics
                item {
                    AnalyticsCard(
                        title = "Aktivitätsanalyse",
                        icon = Icons.Default.DirectionsRun,
                        color = Color(0xFFFF9800)
                    ) {
                        ActivityAnalyticsContent(uiState.statistics?.activityAnalytics)
                    }
                }
                
                // Cost Analytics
                item {
                    AnalyticsCard(
                        title = "Kostenanalyse",
                        icon = Icons.Default.Euro,
                        color = Color(0xFF9C27B0)
                    ) {
                        CostAnalyticsContent(uiState.statistics?.costAnalytics)
                    }
                }
                
                // Behavioral Analytics
                item {
                    AnalyticsCard(
                        title = "Verhaltensanalyse",
                        icon = Icons.Default.Psychology,
                        color = Color(0xFF00BCD4)
                    ) {
                        BehavioralAnalyticsContent(uiState.statistics?.behavioralAnalytics)
                    }
                }
                
                // Predictive Insights
                item {
                    AnalyticsCard(
                        title = "Vorhersagen & Empfehlungen",
                        icon = Icons.Default.AutoAwesome,
                        color = Color(0xFFFF5722)
                    ) {
                        PredictiveInsightsContent(uiState.statistics?.predictiveInsights)
                    }
                }
                
                // Comparative Analysis
                item {
                    AnalyticsCard(
                        title = "Vergleichsanalyse",
                        icon = Icons.Default.CompareArrows,
                        color = Color(0xFF795548)
                    ) {
                        ComparativeAnalysisContent(uiState.statistics?.comparativeAnalysis)
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    selectedPeriod: AnalyticsPeriod,
    onPeriodSelected: (AnalyticsPeriod) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AnalyticsPeriod.values().forEach { period ->
            FilterChip(
                selected = selectedPeriod == period,
                onClick = { onPeriodSelected(period) },
                label = { 
                    Text(
                        when (period) {
                            AnalyticsPeriod.WEEK -> "Woche"
                            AnalyticsPeriod.MONTH -> "Monat"
                            AnalyticsPeriod.QUARTER -> "Quartal"
                            AnalyticsPeriod.YEAR -> "Jahr"
                            AnalyticsPeriod.CUSTOM -> "Benutzerdefiniert"
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun OverviewSection(statistics: AdvancedStatistics?) {
    if (statistics == null) return
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Health Score
        MetricCard(
            modifier = Modifier.weight(1f),
            title = "Gesundheit",
            value = "${statistics.healthAnalytics.healthScore.toInt()}%",
            subtitle = "Score",
            color = getHealthScoreColor(statistics.healthAnalytics.healthScore),
            icon = Icons.Default.Favorite
        )
        
        // Weight Status
        MetricCard(
            modifier = Modifier.weight(1f),
            title = "Gewicht",
            value = "${statistics.weightAnalytics.currentWeight} kg",
            subtitle = getTrendText(statistics.weightAnalytics.weightTrend),
            color = getWeightTrendColor(statistics.weightAnalytics.weightTrend),
            icon = Icons.Default.TrendingUp
        )
        
        // Activity Level
        MetricCard(
            modifier = Modifier.weight(1f),
            title = "Aktivität",
            value = getActivityLevelText(statistics.activityAnalytics.activityLevel),
            subtitle = "${statistics.activityAnalytics.dailyActivityMinutes.toInt()} Min/Tag",
            color = getActivityLevelColor(statistics.activityAnalytics.activityLevel),
            icon = Icons.Default.DirectionsRun
        )
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    icon: ImageVector
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AnalyticsCard(
    title: String,
    icon: ImageVector,
    color: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            content()
        }
    }
}

@Composable
private fun WeightAnalyticsContent(analytics: WeightAnalytics?) {
    if (analytics == null) return
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Current vs Ideal Weight
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoItem(
                label = "Aktuelles Gewicht",
                value = "${analytics.currentWeight} kg"
            )
            InfoItem(
                label = "Idealgewicht",
                value = "${analytics.idealWeight} kg"
            )
            InfoItem(
                label = "Differenz",
                value = "${String.format("%.1f", analytics.currentWeight - analytics.idealWeight)} kg"
            )
        }
        
        Divider()
        
        // Trend Information
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoItem(
                label = "Trend",
                value = getTrendText(analytics.weightTrend)
            )
            InfoItem(
                label = "Änderung",
                value = "${String.format("%+.1f", analytics.weightChangePercent)}%"
            )
            InfoItem(
                label = "Geschwindigkeit",
                value = "${String.format("%.2f", analytics.weightVelocity)} kg/Woche"
            )
        }
        
        // Body Condition Score
        BodyConditionScoreIndicator(analytics.bodyConditionScore)
        
        // Days to ideal weight
        analytics.daysToIdealWeight?.let { days ->
            InfoMessage(
                text = "Bei aktuellem Trend wird das Idealgewicht in $days Tagen erreicht",
                type = MessageType.INFO
            )
        }
        
        // Consistency Score
        LinearProgressIndicator(
            progress = analytics.consistencyScore / 100f,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "Konsistenz: ${analytics.consistencyScore.toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NutritionAnalyticsContent(analytics: NutritionAnalytics?) {
    if (analytics == null) return
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Calorie Balance
        CalorieBalanceIndicator(
            current = analytics.averageDailyCalories,
            recommended = analytics.recommendedDailyCalories,
            balance = analytics.calorieBalance
        )
        
        Divider()
        
        // Macronutrient Breakdown
        MacronutrientChart(analytics.macronutrientBreakdown)
        
        // Key Scores
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ScoreIndicator(
                label = "Proteinqualität",
                score = analytics.proteinQualityScore,
                color = Color(0xFF4CAF50)
            )
            ScoreIndicator(
                label = "Vielfalt",
                score = analytics.dietDiversityScore,
                color = Color(0xFF2196F3)
            )
            ScoreIndicator(
                label = "Regelmäßigkeit",
                score = analytics.mealRegularityScore,
                color = Color(0xFFFF9800)
            )
        }
        
        // Treat Percentage Warning
        if (analytics.treatPercentage > 15) {
            InfoMessage(
                text = "Leckerli-Anteil bei ${analytics.treatPercentage.toInt()}% - sollte unter 10% liegen",
                type = MessageType.WARNING
            )
        }
        
        // Nutritional Completeness
        NutritionalCompletenessIndicator(analytics.nutritionalCompleteness)
        
        // Top Deficiencies
        if (analytics.topNutrientDeficiencies.isNotEmpty()) {
            Text(
                "Nährstoffmängel:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            analytics.topNutrientDeficiencies.forEach { deficiency ->
                NutrientIssueItem(
                    nutrient = deficiency.nutrient,
                    level = deficiency.deficiencyPercent,
                    impact = deficiency.healthImpact,
                    isDeficiency = true
                )
            }
        }
    }
}

@Composable
private fun HealthAnalyticsContent(analytics: HealthAnalytics?) {
    if (analytics == null) return
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Health Score Circle
        HealthScoreCircle(analytics.healthScore)
        
        // Key Health Metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${analytics.vetVisitFrequency}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Tierarztbesuche/Jahr",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${analytics.medicationAdherence.toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Medikamenten-Compliance",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // Vaccine Status
        VaccineStatusCard(analytics.vaccineStatus)
        
        // Health Risk Factors
        if (analytics.healthRiskFactors.isNotEmpty()) {
            Text(
                "Risikofaktoren:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            analytics.healthRiskFactors.forEach { risk ->
                RiskFactorItem(risk)
            }
        }
        
        // Symptom Frequency
        if (analytics.symptomFrequency.isNotEmpty()) {
            Text(
                "Häufige Symptome:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            analytics.symptomFrequency.entries.sortedByDescending { it.value }.take(3).forEach { (symptom, count) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(symptom, style = MaterialTheme.typography.bodyMedium)
                    Text("${count}x", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun ActivityAnalyticsContent(analytics: ActivityAnalytics?) {
    if (analytics == null) return
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Activity Level Gauge
        ActivityLevelGauge(
            currentMinutes = analytics.dailyActivityMinutes,
            recommendedMinutes = analytics.recommendedActivityMinutes,
            level = analytics.activityLevel
        )
        
        // Activity Breakdown
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActivityMetric(
                icon = Icons.Default.DirectionsWalk,
                value = "${analytics.walkFrequency}x",
                label = "Spaziergänge/Woche"
            )
            ActivityMetric(
                icon = Icons.Default.Timer,
                value = "${analytics.averageWalkDuration.toInt()} Min",
                label = "Ø Dauer"
            )
            ActivityMetric(
                icon = Icons.Default.PlayArrow,
                value = "${analytics.playTimeMinutes.toInt()} Min",
                label = "Spielzeit/Tag"
            )
        }
        
        // Exercise Type Distribution
        Text(
            "Aktivitätsverteilung:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        analytics.exerciseTypeDistribution.forEach { (type, percentage) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    getExerciseTypeText(type),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(percentage.toFloat() / 100f)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
                Text(
                    "${percentage.toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(40.dp),
                    textAlign = TextAlign.End
                )
            }
        }
        
        // Activity Consistency
        LinearProgressIndicator(
            progress = analytics.activityConsistency / 100f,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "Aktivitätskonsistenz: ${analytics.activityConsistency.toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CostAnalyticsContent(analytics: CostAnalytics?) {
    if (analytics == null) return
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Monthly Cost Overview
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
                    "Monatliche Kosten",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    "€${String.format("%.2f", analytics.totalMonthlySpend)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "€${String.format("%.2f", analytics.averageDailyCost)}/Tag",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // Cost Breakdown Chart
        CostBreakdownChart(analytics.costBreakdown)
        
        // Cost per kg
        InfoItem(
            label = "Kosten pro kg Körpergewicht",
            value = "€${String.format("%.2f", analytics.costPerKg)}"
        )
        
        // Cost Optimization Opportunities
        if (analytics.costOptimizationOpportunities.isNotEmpty()) {
            Text(
                "Einsparmöglichkeiten:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            analytics.costOptimizationOpportunities.forEach { optimization ->
                CostOptimizationCard(optimization)
            }
        }
        
        // Annual Projection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoItem(
                label = "Jahresprognose",
                value = "€${String.format("%.0f", analytics.projectedAnnualCost)}"
            )
            InfoItem(
                label = "Mit Inflation",
                value = "€${String.format("%.0f", analytics.projectedAnnualCost * 1.05)}"
            )
        }
    }
}

@Composable
private fun BehavioralAnalyticsContent(analytics: BehavioralAnalytics?) {
    if (analytics == null) return
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Eating Behavior Score
        ScoreCard(
            title = "Essverhalten",
            score = analytics.eatingBehaviorScore,
            description = "Gesamtbewertung des Essverhaltens"
        )
        
        // Key Behavioral Indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BehaviorIndicator(
                label = "Futtermotivation",
                value = getFoodMotivationText(analytics.foodMotivationLevel),
                color = getFoodMotivationColor(analytics.foodMotivationLevel)
            )
            BehaviorIndicator(
                label = "Essgeschwindigkeit",
                value = getEatingSpeedText(analytics.eatingSpeed),
                color = getEatingSpeedColor(analytics.eatingSpeed)
            )
        }
        
        // Meal Time Consistency
        LinearProgressIndicator(
            progress = analytics.mealTimeConsistency / 100f,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "Fütterungszeiten-Konsistenz: ${analytics.mealTimeConsistency.toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        // Food Preferences
        if (analytics.foodPreferences.isNotEmpty()) {
            Text(
                "Futterpräferenzen:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            analytics.foodPreferences.take(3).forEach { preference ->
                FoodPreferenceItem(preference)
            }
        }
        
        // Behavioral Concerns
        if (analytics.pickeyEaterScore > 50) {
            InfoMessage(
                text = "Zeigt wählerisches Essverhalten (Score: ${analytics.pickeyEaterScore.toInt()})",
                type = MessageType.WARNING
            )
        }
    }
}

@Composable
private fun PredictiveInsightsContent(insights: PredictiveInsights?) {
    if (insights == null) return
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Weight Prediction
        PredictionCard(
            title = "Gewichtsprognose",
            predictions = listOf(
                "30 Tage: ${String.format("%.1f", insights.weightPrediction.predictedWeight30Days)} kg",
                "90 Tage: ${String.format("%.1f", insights.weightPrediction.predictedWeight90Days)} kg"
            ),
            confidence = insights.weightPrediction.confidenceLevel,
            icon = Icons.Default.TrendingUp
        )
        
        // Health Predictions
        if (insights.healthPredictions.isNotEmpty()) {
            Text(
                "Gesundheitsprognosen:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            insights.healthPredictions.forEach { prediction ->
                HealthPredictionItem(prediction)
            }
        }
        
        // Risk Assessment
        RiskAssessmentCard(insights.riskAssessment)
        
        // Recommended Interventions
        if (insights.recommendedInterventions.isNotEmpty()) {
            Text(
                "Empfohlene Maßnahmen:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            insights.recommendedInterventions.forEach { intervention ->
                InterventionCard(intervention)
            }
        }
        
        // Life Stage Transition
        insights.lifestageTransitionPrediction?.let { transition ->
            LifeStageTransitionCard(transition)
        }
    }
}

@Composable
private fun ComparativeAnalysisContent(analysis: ComparativeAnalysis?) {
    if (analysis == null) return
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Breed Comparison
        ComparisonSection(
            title = "Rassenvergleich",
            comparison = analysis.breedComparison,
            type = ComparisonType.BREED
        )
        
        // Age Group Comparison
        ComparisonSection(
            title = "Altersgruppen-Vergleich",
            comparison = analysis.ageGroupComparison,
            type = ComparisonType.AGE_GROUP
        )
        
        // Similar Dogs Comparison
        if (analysis.similarDogsComparison.sampleSize > 0) {
            Text(
                "Vergleich mit ähnlichen Hunden (n=${analysis.similarDogsComparison.sampleSize}):",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            analysis.similarDogsComparison.metrics.forEach { (metric, comparison) ->
                ComparisonMetricRow(metric, comparison)
            }
        }
        
        // Historical Comparison
        if (analysis.historicalComparison.improvements.isNotEmpty() || 
            analysis.historicalComparison.declines.isNotEmpty()) {
            HistoricalComparisonCard(analysis.historicalComparison)
        }
        
        // Goal Progress
        if (analysis.goalComparison.goals.isNotEmpty()) {
            GoalProgressSection(analysis.goalComparison)
        }
    }
}

// Helper Composables

@Composable
private fun InfoItem(
    label: String,
    value: String
) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun InfoMessage(
    text: String,
    type: MessageType
) {
    val (backgroundColor, contentColor, icon) = when (type) {
        MessageType.INFO -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            Icons.Default.Info
        )
        MessageType.WARNING -> Triple(
            Color(0xFFFFF3E0),
            Color(0xFFE65100),
            Icons.Default.Warning
        )
        MessageType.ERROR -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            Icons.Default.Error
        )
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor
            )
        }
    }
}

@Composable
private fun ScoreIndicator(
    label: String,
    score: Double,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "${score.toInt()}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

// Helper functions

private fun getTrendText(trend: TrendDirection): String = when (trend) {
    TrendDirection.INCREASING -> "Steigend"
    TrendDirection.DECREASING -> "Fallend"
    TrendDirection.STABLE -> "Stabil"
    TrendDirection.VOLATILE -> "Schwankend"
}

private fun getHealthScoreColor(score: Double): Color = when {
    score >= 80 -> Color(0xFF4CAF50)
    score >= 60 -> Color(0xFFFF9800)
    else -> Color(0xFFF44336)
}

private fun getWeightTrendColor(trend: TrendDirection): Color = when (trend) {
    TrendDirection.STABLE -> Color(0xFF4CAF50)
    TrendDirection.VOLATILE -> Color(0xFFFF9800)
    else -> Color(0xFF2196F3)
}

private fun getActivityLevelText(level: ActivityLevel): String = when (level) {
    ActivityLevel.SEDENTARY -> "Inaktiv"
    ActivityLevel.LOW -> "Wenig"
    ActivityLevel.MODERATE -> "Moderat"
    ActivityLevel.HIGH -> "Hoch"
    ActivityLevel.VERY_HIGH -> "Sehr hoch"
}

private fun getActivityLevelColor(level: ActivityLevel): Color = when (level) {
    ActivityLevel.SEDENTARY -> Color(0xFFF44336)
    ActivityLevel.LOW -> Color(0xFFFF9800)
    ActivityLevel.MODERATE -> Color(0xFF4CAF50)
    ActivityLevel.HIGH -> Color(0xFF2196F3)
    ActivityLevel.VERY_HIGH -> Color(0xFF9C27B0)
}

private fun getExerciseTypeText(type: ExerciseType): String = when (type) {
    ExerciseType.WALKING -> "Spazieren"
    ExerciseType.RUNNING -> "Laufen"
    ExerciseType.SWIMMING -> "Schwimmen"
    ExerciseType.PLAYING -> "Spielen"
    ExerciseType.TRAINING -> "Training"
    ExerciseType.HIKING -> "Wandern"
    ExerciseType.AGILITY -> "Agility"
}

private fun getFoodMotivationText(level: FoodMotivation): String = when (level) {
    FoodMotivation.LOW -> "Niedrig"
    FoodMotivation.NORMAL -> "Normal"
    FoodMotivation.HIGH -> "Hoch"
    FoodMotivation.EXCESSIVE -> "Übermäßig"
}

private fun getFoodMotivationColor(level: FoodMotivation): Color = when (level) {
    FoodMotivation.LOW -> Color(0xFFFF9800)
    FoodMotivation.NORMAL -> Color(0xFF4CAF50)
    FoodMotivation.HIGH -> Color(0xFF2196F3)
    FoodMotivation.EXCESSIVE -> Color(0xFFF44336)
}

private fun getEatingSpeedText(speed: EatingSpeed): String = when (speed) {
    EatingSpeed.VERY_SLOW -> "Sehr langsam"
    EatingSpeed.SLOW -> "Langsam"
    EatingSpeed.NORMAL -> "Normal"
    EatingSpeed.FAST -> "Schnell"
    EatingSpeed.VERY_FAST -> "Sehr schnell"
}

private fun getEatingSpeedColor(speed: EatingSpeed): Color = when (speed) {
    EatingSpeed.VERY_SLOW -> Color(0xFFFF9800)
    EatingSpeed.SLOW -> Color(0xFF2196F3)
    EatingSpeed.NORMAL -> Color(0xFF4CAF50)
    EatingSpeed.FAST -> Color(0xFFFF9800)
    EatingSpeed.VERY_FAST -> Color(0xFFF44336)
}

enum class MessageType {
    INFO,
    WARNING,
    ERROR
}

enum class ComparisonType {
    BREED,
    AGE_GROUP,
    SIMILAR_DOGS,
    HISTORICAL
}

// Additional UI components would be defined here...