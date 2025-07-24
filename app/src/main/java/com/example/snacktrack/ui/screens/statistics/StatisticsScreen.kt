package com.example.snacktrack.ui.screens.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.snacktrack.ui.components.CommonTopAppBar
import com.example.snacktrack.ui.viewmodel.StatisticsViewModel
import com.example.snacktrack.ui.viewmodel.StatisticsViewModelFactory
import com.example.snacktrack.ui.viewmodel.StatisticsUiState
import com.example.snacktrack.ui.viewmodel.FoodFrequency
import com.example.snacktrack.ui.viewmodel.DailyCalories
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    dogId: String,
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: StatisticsViewModel = viewModel(
        factory = StatisticsViewModelFactory(context, dogId)
    )
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Statistiken",
                showBackButton = true,
                showDogDetailButton = false,
                onBackClick = { navController.popBackStack() },
                onDogDetailClick = {},
                onAdminClick = { navController.navigate("admin_moderation") },
                onAccountClick = { navController.navigate("account_management") },
                onLogoutClick = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Ein Fehler ist aufgetreten",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Kalorien-Übersicht
                    item {
                        CalorieOverviewSection(uiState)
                    }
                    
                    // Kalorien-Verlauf Chart
                    item {
                        CalorieChartSection(uiState)
                    }
                    
                    // Gewichtsverlauf Chart
                    if (uiState.weightEntries.isNotEmpty()) {
                        item {
                            WeightChartSection(uiState)
                        }
                    }
                    
                    // Häufigste Lebensmittel
                    item {
                        MostFrequentFoodsSection(uiState)
                    }
                    
                    // Fütterungszeiten
                    item {
                        FeedingTimeSection(uiState)
                    }
                    
                    // Zusätzliche Statistiken
                    item {
                        AdditionalStatsSection(uiState)
                    }
                }
            }
        }
    }
}

@Composable
fun CalorieOverviewSection(uiState: StatisticsUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Kalorien-Übersicht",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CalorieStatCard(
                    title = "Heute",
                    value = uiState.dailyAverageCalories,
                    icon = Icons.Default.Today,
                    color = MaterialTheme.colorScheme.primary
                )
                CalorieStatCard(
                    title = "Ø Woche",
                    value = uiState.weeklyAverageCalories,
                    icon = Icons.Default.DateRange,
                    color = MaterialTheme.colorScheme.secondary
                )
                CalorieStatCard(
                    title = "Ø Monat",
                    value = uiState.monthlyAverageCalories,
                    icon = Icons.Default.CalendarMonth,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
fun CalorieStatCard(
    title: String,
    value: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$value kcal",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CalorieChartSection(uiState: StatisticsUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Kalorien-Verlauf (30 Tage)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (uiState.dailyCalorieData.isNotEmpty()) {
                SimpleLineChart(
                    data = uiState.dailyCalorieData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Keine Daten vorhanden",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleLineChart(
    data: List<DailyCalories>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurfaceVariant
    val maxCalories = data.maxOfOrNull { it.calories }?.toFloat() ?: 1f
    
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 40.dp.toPx()
        val graphWidth = width - 2 * padding
        val graphHeight = height - 2 * padding
        
        // Draw grid lines
        val gridColor = onSurfaceColor.copy(alpha = 0.1f)
        val gridStroke = Stroke(
            width = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
        )
        
        // Horizontal grid lines
        for (i in 0..4) {
            val y = padding + (graphHeight / 4) * i
            drawLine(
                color = gridColor,
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 1.dp.toPx()
            )
        }
        
        // Draw the line chart
        if (data.isNotEmpty()) {
            val path = Path()
            val stepX = graphWidth / (data.size - 1).coerceAtLeast(1)
            
            data.forEachIndexed { index, dailyCalories ->
                val x = padding + index * stepX
                val y = padding + graphHeight * (1 - dailyCalories.calories / maxCalories)
                
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
                
                // Draw data points
                drawCircle(
                    color = primaryColor,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
            }
            
            // Draw the line
            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = 2.dp.toPx())
            )
        }
        
        // Draw Y-axis labels
        drawContext.canvas.nativeCanvas.apply {
            val textPaint = android.graphics.Paint().apply {
                color = onSurfaceColor.toArgb()
                textSize = 10.sp.toPx()
                textAlign = android.graphics.Paint.Align.RIGHT
            }
            
            for (i in 0..4) {
                val value = (maxCalories * (4 - i) / 4).roundToInt()
                val y = padding + (graphHeight / 4) * i + 4.dp.toPx()
                drawText(
                    "$value",
                    padding - 8.dp.toPx(),
                    y,
                    textPaint
                )
            }
        }
    }
}

@Composable
fun WeightChartSection(uiState: StatisticsUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gewichtsverlauf",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Simple weight display
            val sortedWeights = uiState.weightEntries.sortedBy { it.timestamp }
            if (sortedWeights.size >= 2) {
                val latestWeight = sortedWeights.last().weight
                val previousWeight = sortedWeights[sortedWeights.size - 2].weight
                val weightChange = latestWeight - previousWeight
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Aktuell",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${String.format("%.1f", latestWeight)} kg",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Veränderung",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${if (weightChange >= 0) "+" else ""}${String.format("%.1f", weightChange)} kg",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (weightChange > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MostFrequentFoodsSection(uiState: StatisticsUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Häufigste Lebensmittel",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (uiState.mostFrequentFoods.isEmpty()) {
                Text(
                    text = "Keine Daten vorhanden",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                uiState.mostFrequentFoods.forEach { food ->
                    FoodFrequencyItem(food)
                    if (food != uiState.mostFrequentFoods.last()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FoodFrequencyItem(food: FoodFrequency) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = food.foodName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${food.count}x gefüttert",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "${food.totalCalories} kcal",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun FeedingTimeSection(uiState: StatisticsUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fütterungszeiten",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (uiState.feedingTimeDistribution.isEmpty()) {
                Text(
                    text = "Keine Daten vorhanden",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Calculate statistics
                val totalFeedings = uiState.feedingTimeDistribution.values.sum()
                val avgFeedingsPerDay = if (uiState.periodDays > 0) {
                    totalFeedings.toFloat() / uiState.periodDays
                } else 0f
                val peakHour = uiState.feedingTimeDistribution.maxByOrNull { it.value }
                
                // Show summary stats
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "%.1f".format(avgFeedingsPerDay),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Ø pro Tag",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (peakHour != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${peakHour.key}:00",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Häufigste Zeit",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = totalFeedings.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Gesamt",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                FeedingTimeChart(
                    distribution = uiState.feedingTimeDistribution,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
                
                // Legend
                Text(
                    text = "Die Zahlen über den Balken zeigen die Anzahl der Fütterungen",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun FeedingTimeChart(
    distribution: Map<Int, Int>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val maxCount = distribution.maxOfOrNull { it.value }?.toFloat() ?: 1f
    
    Canvas(modifier = modifier) {
        val barWidth = size.width / 24
        val maxHeight = size.height * 0.7f // Reduced to make room for labels
        val bottomPadding = 30.dp.toPx()
        
        distribution.forEach { (hour, count) ->
            val barHeight = (count / maxCount) * maxHeight
            val x = hour * barWidth
            val y = size.height - bottomPadding - barHeight
            
            // Draw bar
            drawRect(
                color = primaryColor.copy(alpha = 0.8f),
                topLeft = Offset(x + barWidth * 0.1f, y),
                size = Size(barWidth * 0.8f, barHeight)
            )
            
            // Draw count on top of bar if count > 0
            if (count > 0) {
                drawContext.canvas.nativeCanvas.drawText(
                    count.toString(),
                    x + barWidth / 2,
                    y - 5.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = onSurfaceColor.toArgb()
                        textSize = 9.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                    }
                )
            }
            
            // Draw hour labels for every 3 hours
            if (hour % 3 == 0) {
                drawContext.canvas.nativeCanvas.drawText(
                    "${hour}:00",
                    x + barWidth / 2,
                    size.height - 5.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = onSurfaceColor.toArgb()
                        textSize = 10.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }
        
        // Draw a baseline
        drawLine(
            color = onSurfaceColor.copy(alpha = 0.3f),
            start = Offset(0f, size.height - bottomPadding),
            end = Offset(size.width, size.height - bottomPadding),
            strokeWidth = 1.dp.toPx()
        )
    }
}

@Composable
fun AdditionalStatsSection(uiState: StatisticsUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Weitere Statistiken",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(
                    label = "Gesamte Einträge",
                    value = uiState.totalFoodIntakes.toString(),
                    icon = Icons.Default.Restaurant
                )
                StatItem(
                    label = "Ø Einträge/Tag",
                    value = String.format("%.1f", uiState.averageDailyIntakes),
                    icon = Icons.Default.Schedule
                )
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}