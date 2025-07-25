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
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
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
    var selectedPeriod by remember { mutableStateOf(AnalyticsPeriod.MONTHLY) }
    
    LaunchedEffect(dogId, selectedPeriod) {
        viewModel.loadStatistics(dogId, selectedPeriod)
    }
    
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Erweiterte Statistiken",
                onBackClick = { navController.navigateUp() },
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
                        BehavioralAnalyticsContent(uiState.statistics?.behaviorAnalytics)
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
                            AnalyticsPeriod.DAILY -> "Tag"
                            AnalyticsPeriod.WEEKLY -> "Woche"
                            AnalyticsPeriod.MONTHLY -> "Monat"
                            AnalyticsPeriod.QUARTERLY -> "Quartal"
                            AnalyticsPeriod.YEARLY -> "Jahr"
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
        // TODO: Implement BodyConditionScoreIndicator
        // BodyConditionScoreIndicator(analytics.bodyConditionScore)
        
        // Days to ideal weight - removed as field doesn't exist
        
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
        if (analytics.nutrientDeficiencies.isNotEmpty()) {
            Text(
                "Nährstoffmängel:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            analytics.nutrientDeficiencies.forEach { deficiency ->
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
            description = "Gesamtbewertung des Essverhaltens",
            icon = Icons.Default.Restaurant
        )
        
        // Key Behavioral Indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    getFoodMotivationText(analytics.foodMotivationLevel),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = getFoodMotivationColor(analytics.foodMotivationLevel)
                )
                Text(
                    "Futtermotivation",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    getEatingSpeedText(analytics.eatingSpeed),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = getEatingSpeedColor(analytics.eatingSpeed)
                )
                Text(
                    "Essgeschwindigkeit",
                    style = MaterialTheme.typography.bodySmall
                )
            }
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
            prediction = "30 Tage: ${String.format("%.1f", insights.weightPrediction.predictedWeight30Days)} kg, 90 Tage: ${String.format("%.1f", insights.weightPrediction.predictedWeight90Days)} kg",
            confidence = insights.weightPrediction.confidenceLevel,
            timeframe = "Nächste 90 Tage"
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
            comparison = analysis.breedComparison
        ) {
            analysis.breedComparison?.let { comparison ->
                ComparisonMetricRow(
                    "Durchschnittsgewicht",
                    "${comparison.yourDog.weight} kg",
                    "${comparison.breedAverage.weight} kg"
                )
                ComparisonMetricRow(
                    "Aktivitätslevel",
                    getActivityLevelText(comparison.yourDog.activityLevel),
                    getActivityLevelText(comparison.breedAverage.activityLevel)
                )
            }
        }
        
        // Age Group Comparison
        ComparisonSection(
            title = "Altersgruppen-Vergleich",
            comparison = analysis.ageGroupComparison
        ) {
            analysis.ageGroupComparison?.let { comparison ->
                ComparisonMetricRow(
                    "Gesundheitswert",
                    "${comparison.yourDog.healthScore}",
                    "${comparison.ageAverage.healthScore}"
                )
            }
        }
        
        // Similar Dogs Comparison
        ComparisonSection(
            title = "Vergleich mit ähnlichen Hunden",
            comparison = analysis.similarDogsComparison
        ) {
            analysis.similarDogsComparison?.let { comparison ->
                Text(
                    "Stichprobengröße: ${comparison.sampleSize} Hunde",
                    style = MaterialTheme.typography.bodySmall
                )
                comparison.metrics.forEach { metric ->
                    ComparisonMetricRow(
                        metric.name,
                        metric.yourValue,
                        metric.averageValue,
                        metric.unit
                    )
                }
            }
        }
        
        // Historical Comparison
        analysis.historicalComparison?.let { comparison ->
            comparison.periods.forEach { period ->
                HistoricalComparisonCard(
                    period.name,
                    period.improvement,
                    period.details
                )
            }
        }
        
        // Goal Progress
        analysis.goals?.let { goals ->
            GoalProgressSection(goals)
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

private fun getTrendText(trend: StatisticsTrendDirection): String = when (trend) {
    StatisticsTrendDirection.INCREASING -> "Steigend"
    StatisticsTrendDirection.DECREASING -> "Fallend"
    StatisticsTrendDirection.STABLE -> "Stabil"
}

private fun getHealthScoreColor(score: Double): Color = when {
    score >= 80 -> Color(0xFF4CAF50)
    score >= 60 -> Color(0xFFFF9800)
    else -> Color(0xFFF44336)
}

private fun getWeightTrendColor(trend: StatisticsTrendDirection): Color = when (trend) {
    StatisticsTrendDirection.STABLE -> Color(0xFF4CAF50)
    StatisticsTrendDirection.INCREASING -> Color(0xFF2196F3)
    StatisticsTrendDirection.DECREASING -> Color(0xFFFF9800)
}

private fun getActivityLevelText(level: StatisticsActivityLevel): String = when (level) {
    StatisticsActivityLevel.SEDENTARY -> "Inaktiv"
    StatisticsActivityLevel.LOW -> "Wenig"
    StatisticsActivityLevel.MODERATE -> "Moderat"
    StatisticsActivityLevel.HIGH -> "Hoch"
    StatisticsActivityLevel.VERY_HIGH -> "Sehr hoch"
}

private fun getActivityLevelColor(level: StatisticsActivityLevel): Color = when (level) {
    StatisticsActivityLevel.SEDENTARY -> Color(0xFFF44336)
    StatisticsActivityLevel.LOW -> Color(0xFFFF9800)
    StatisticsActivityLevel.MODERATE -> Color(0xFF4CAF50)
    StatisticsActivityLevel.HIGH -> Color(0xFF2196F3)
    StatisticsActivityLevel.VERY_HIGH -> Color(0xFF9C27B0)
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

// Additional UI components

@Composable
private fun CalorieBalanceIndicator(
    consumed: Double,
    needed: Double,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val balance = consumed - needed
            val percentage = (consumed / needed * 100).toInt()
            
            Text(
                "$percentage%",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = when {
                    percentage < 90 -> Color(0xFFFF9800)
                    percentage > 110 -> Color(0xFFF44336)
                    else -> Color(0xFF4CAF50)
                }
            )
            
            Text(
                "${consumed.toInt()} / ${needed.toInt()} kcal",
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (balance != 0.0) {
                Text(
                    "${if (balance > 0) "+" else ""}${balance.toInt()} kcal",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (balance > 0) Color.Red else Color.Green
                )
            }
        }
    }
}

@Composable
private fun MacronutrientChart(breakdown: MacronutrientBreakdown?) {
    if (breakdown == null) return
    
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Makronährstoffe",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Protein
            MacronutrientRow("Protein", breakdown.proteinPercent, Color(0xFF4CAF50))
            
            // Fat
            MacronutrientRow("Fett", breakdown.fatPercent, Color(0xFFFF9800))
            
            // Carbs
            MacronutrientRow("Kohlenhydrate", breakdown.carbPercent, Color(0xFF2196F3))
            
            // Fiber
            MacronutrientRow("Ballaststoffe", breakdown.fiberPercent, Color(0xFF9C27B0))
        }
    }
}

@Composable
private fun MacronutrientRow(
    name: String,
    percentage: Double,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            name,
            modifier = Modifier.weight(0.3f),
            style = MaterialTheme.typography.bodyMedium
        )
        
        Box(
            modifier = Modifier
                .weight(0.6f)
                .height(20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Gray.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percentage.toFloat() / 100f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color)
            )
        }
        
        Text(
            "${percentage.toInt()}%",
            modifier = Modifier.weight(0.1f),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun NutritionalCompletenessIndicator(score: Double) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Nährwert-Vollständigkeit",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(100.dp)
            ) {
                CircularProgressIndicator(
                    progress = score.toFloat() / 100f,
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 8.dp,
                    color = when {
                        score >= 90 -> Color(0xFF4CAF50)
                        score >= 70 -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    }
                )
                
                Text(
                    "${score.toInt()}%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun NutrientIssueItem(
    nutrient: String,
    level: Double,
    impact: String,
    isDeficiency: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDeficiency) 
                Color(0xFFFFF3E0) else Color(0xFFFFEBEE)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isDeficiency) Icons.Default.Warning else Icons.Default.Error,
                contentDescription = null,
                tint = if (isDeficiency) Color(0xFFFF9800) else Color(0xFFF44336),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    nutrient,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${if (isDeficiency) "-" else "+"}${level.toInt()}% • $impact",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun HealthScoreCircle(score: Double) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(getHealthScoreColor(score).copy(alpha = 0.1f))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    score.toInt().toString(),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = getHealthScoreColor(score)
                )
                Text(
                    "Gesundheitswert",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun VaccineStatusCard(vaccines: Map<String, VaccineInfo>) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Impfstatus",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            vaccines.forEach { (vaccine, info) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(vaccine, style = MaterialTheme.typography.bodyMedium)
                    
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                when (info.status) {
                                    VaccineStatus.UP_TO_DATE -> "Aktuell"
                                    VaccineStatus.DUE_SOON -> "Bald fällig"
                                    VaccineStatus.OVERDUE -> "Überfällig"
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = when (info.status) {
                                VaccineStatus.UP_TO_DATE -> Color(0xFF4CAF50)
                                VaccineStatus.DUE_SOON -> Color(0xFFFF9800)
                                VaccineStatus.OVERDUE -> Color(0xFFF44336)
                            }
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun RiskFactorItem(factor: RiskFactor) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (factor.severity) {
                RiskSeverity.LOW -> Color(0xFFE8F5E9)
                RiskSeverity.MEDIUM -> Color(0xFFFFF3E0)
                RiskSeverity.HIGH -> Color(0xFFFFEBEE)
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    factor.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    "${(factor.probability * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = when (factor.severity) {
                        RiskSeverity.LOW -> Color(0xFF4CAF50)
                        RiskSeverity.MEDIUM -> Color(0xFFFF9800)
                        RiskSeverity.HIGH -> Color(0xFFF44336)
                    }
                )
            }
            
            if (factor.prevention.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Prävention: ${factor.prevention}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ActivityLevelGauge(level: StatisticsActivityLevel) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Aktivitätslevel",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(getActivityLevelColor(level).copy(alpha = 0.1f))
            ) {
                Text(
                    getActivityLevelText(level),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = getActivityLevelColor(level),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ActivityMetric(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CostBreakdownChart(breakdown: CostBreakdown) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Kostenaufschlüsselung",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val categories = listOf(
                "Futter" to breakdown.food,
                "Tierarzt" to breakdown.veterinary,
                "Zubehör" to breakdown.accessories,
                "Versicherung" to breakdown.insurance,
                "Sonstiges" to breakdown.other
            )
            
            categories.forEach { (category, amount) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(category, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "€${amount.toInt()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Gesamt",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "€${(breakdown.food + breakdown.veterinary + breakdown.accessories + breakdown.insurance + breakdown.other).toInt()}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CostOptimizationCard(suggestion: CostOptimizationSuggestion) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8F5E9)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    suggestion.category,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    "€${suggestion.potentialSaving.toInt()}/Monat",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4CAF50)
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                suggestion.recommendation,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ScoreCard(
    title: String,
    score: Double,
    description: String,
    icon: ImageVector
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "${score.toInt()}%",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BehaviorIndicator(
    behavior: String,
    frequency: String,
    trend: StatisticsTrendDirection
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            behavior,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                frequency,
                style = MaterialTheme.typography.bodySmall
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Icon(
                when (trend) {
                    StatisticsTrendDirection.INCREASING -> Icons.Default.TrendingUp
                    StatisticsTrendDirection.DECREASING -> Icons.Default.TrendingDown
                    else -> Icons.Default.TrendingFlat
                },
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = when (trend) {
                    StatisticsTrendDirection.INCREASING -> Color(0xFFF44336)
                    StatisticsTrendDirection.DECREASING -> Color(0xFF4CAF50)
                    else -> Color.Gray
                }
            )
        }
    }
}

@Composable
private fun FoodPreferenceItem(preference: FoodPreference) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            preference.food,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Row {
            repeat(5) { index ->
                Icon(
                    if (index < preference.rating) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFFFFB300)
                )
            }
        }
    }
}

@Composable
private fun PredictionCard(
    title: String,
    prediction: String,
    confidence: Double,
    timeframe: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF3E5F5)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    timeframe,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                prediction,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Konfidenz:",
                    style = MaterialTheme.typography.bodySmall
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                LinearProgressIndicator(
                    progress = confidence.toFloat(),
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    "${(confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun HealthPredictionItem(prediction: HealthPrediction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (prediction.riskLevel) {
                StatisticsRiskLevel.LOW -> Color(0xFFE8F5E9)
                StatisticsRiskLevel.MEDIUM -> Color(0xFFFFF3E0)
                StatisticsRiskLevel.HIGH -> Color(0xFFFFEBEE)
                StatisticsRiskLevel.CRITICAL -> Color(0xFFFFCDD2)
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    prediction.condition,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    "${(prediction.probability * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            if (prediction.preventiveMeasures.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Prävention: ${prediction.preventiveMeasures.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun RiskAssessmentCard(assessment: RiskAssessment) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Risikobewertung",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            assessment.categories.forEach { (category, risk) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        category,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                when (risk.level) {
                                    StatisticsRiskLevel.LOW -> "Niedrig"
                                    StatisticsRiskLevel.MEDIUM -> "Mittel"
                                    StatisticsRiskLevel.HIGH -> "Hoch"
                                    StatisticsRiskLevel.CRITICAL -> "Kritisch"
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = when (risk.level) {
                                StatisticsRiskLevel.LOW -> Color(0xFF4CAF50)
                                StatisticsRiskLevel.MEDIUM -> Color(0xFFFF9800)
                                StatisticsRiskLevel.HIGH -> Color(0xFFF44336)
                                StatisticsRiskLevel.CRITICAL -> Color(0xFF9C27B0)
                            }
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun InterventionCard(intervention: Intervention) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(
            1.dp,
            when (intervention.urgency) {
                InterventionUrgency.LOW -> Color(0xFF4CAF50)
                InterventionUrgency.MEDIUM -> Color(0xFFFF9800)
                InterventionUrgency.HIGH -> Color(0xFFF44336)
                InterventionUrgency.IMMEDIATE -> Color(0xFF9C27B0)
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    intervention.type,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    when (intervention.urgency) {
                        InterventionUrgency.LOW -> "Niedrig"
                        InterventionUrgency.MEDIUM -> "Mittel"
                        InterventionUrgency.HIGH -> "Hoch"
                        InterventionUrgency.IMMEDIATE -> "Sofort"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when (intervention.urgency) {
                        InterventionUrgency.LOW -> Color(0xFF4CAF50)
                        InterventionUrgency.MEDIUM -> Color(0xFFFF9800)
                        InterventionUrgency.HIGH -> Color(0xFFF44336)
                        InterventionUrgency.IMMEDIATE -> Color(0xFF9C27B0)
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                intervention.recommendation,
                style = MaterialTheme.typography.bodySmall
            )
            
            Text(
                "Erwarteter Nutzen: ${intervention.expectedBenefit}",
                style = MaterialTheme.typography.bodySmall,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

@Composable
private fun LifeStageTransitionCard(transition: LifeStageTransition) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE1F5FE)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Lebensphase: ${transition.fromStage} → ${transition.toStage}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Übergang in ${transition.estimatedTimeframe}",
                style = MaterialTheme.typography.bodySmall
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Empfohlene Anpassungen:",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            
            transition.recommendations.forEach { recommendation ->
                Text(
                    "• $recommendation",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun ComparisonSection(
    title: String,
    comparison: Any?,
    content: @Composable () -> Unit
) {
    if (comparison == null) return
    
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            content()
        }
    }
}

@Composable
private fun ComparisonMetricRow(
    metric: String,
    yourValue: String,
    averageValue: String,
    unit: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            metric,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            "$yourValue$unit",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(80.dp),
            textAlign = TextAlign.End
        )
        
        Text(
            "$averageValue$unit",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(80.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun HistoricalComparisonCard(
    period: String,
    improvement: Double,
    details: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (improvement > 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    period,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    details,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Text(
                "${if (improvement > 0) "+" else ""}${improvement.toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (improvement > 0) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
        }
    }
}

@Composable
private fun GoalProgressSection(goals: List<GoalProgress>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        goals.forEach { goal ->
            Card {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            goal.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            "${goal.progress}%",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    LinearProgressIndicator(
                        progress = goal.progress / 100f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (goal.estimatedCompletion != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Voraussichtlich: ${goal.estimatedCompletion}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoMessage(
    text: String,
    type: MessageType,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (type) {
                MessageType.INFO -> Color(0xFFE3F2FD)
                MessageType.WARNING -> Color(0xFFFFF3E0)
                MessageType.ERROR -> Color(0xFFFFEBEE)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (type) {
                    MessageType.INFO -> Icons.Default.Info
                    MessageType.WARNING -> Icons.Default.Warning
                    MessageType.ERROR -> Icons.Default.Error
                },
                contentDescription = null,
                tint = when (type) {
                    MessageType.INFO -> Color(0xFF1976D2)
                    MessageType.WARNING -> Color(0xFFF57C00)
                    MessageType.ERROR -> Color(0xFFD32F2F)
                },
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
        }
    }
}