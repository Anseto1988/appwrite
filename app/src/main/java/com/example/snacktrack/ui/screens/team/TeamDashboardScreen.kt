package com.example.snacktrack.ui.screens.team

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.snacktrack.data.model.*
import com.example.snacktrack.ui.components.CommonTopAppBar
import com.example.snacktrack.ui.viewmodel.TeamViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDashboardScreen(
    navController: NavController,
    teamId: String,
    viewModel: TeamViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    
    LaunchedEffect(teamId) {
        viewModel.loadTeamData(teamId)
    }
    
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Team Dashboard",
                onBackClick = { navController.navigateUp() },
                actions = {
                    IconButton(onClick = { navController.navigate("team/settings/$teamId") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Einstellungen")
                    }
                }
            )
        },
        floatingActionButton = {
            when (selectedTab) {
                0 -> FloatingActionButton(
                    onClick = { navController.navigate("team/create-task/$teamId") }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Neue Aufgabe")
                }
                1 -> FloatingActionButton(
                    onClick = { navController.navigate("team/add-shopping-item/$teamId") }
                ) {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = "Artikel hinzufügen")
                }
                else -> null
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Quick Stats
            QuickStatsRow(
                statistics = uiState.statistics,
                modifier = Modifier.padding(16.dp)
            )
            
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Aufgaben") },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Einkaufen") },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Aktivitäten") },
                    icon = { Icon(Icons.Default.Timeline, contentDescription = null) }
                )
            }
            
            // Content
            when (selectedTab) {
                0 -> TasksTab(
                    tasks = uiState.upcomingTasks,
                    isLoading = uiState.isLoading,
                    onTaskComplete = { taskId, notes ->
                        viewModel.completeTask(taskId, notes)
                    },
                    onTaskClick = { task ->
                        navController.navigate("team/task-detail/${task.id}")
                    }
                )
                1 -> ShoppingTab(
                    shoppingItems = uiState.shoppingItems,
                    predictions = uiState.consumptionPredictions,
                    isLoading = uiState.isLoading,
                    onItemPurchased = { itemId ->
                        viewModel.markItemAsPurchased(itemId)
                    },
                    onAddPrediction = { prediction ->
                        viewModel.addPredictionToShoppingList(prediction)
                    }
                )
                2 -> ActivityFeedTab(
                    activities = uiState.recentActivities,
                    isLoading = uiState.isLoading,
                    onLoadMore = {
                        viewModel.loadMoreActivities()
                    }
                )
            }
        }
    }
}

@Composable
private fun QuickStatsRow(
    statistics: TeamStatistics?,
    modifier: Modifier = Modifier
) {
    if (statistics == null) return
    
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            StatCard(
                title = "Aufgaben heute",
                value = "${statistics.taskCompletionRate * 100}%",
                subtitle = "erledigt",
                icon = Icons.Default.TaskAlt,
                color = Color(0xFF4CAF50)
            )
        }
        
        item {
            StatCard(
                title = "Team-Aktivität",
                value = statistics.totalActivities.toString(),
                subtitle = "diese Woche",
                icon = Icons.Default.Groups,
                color = Color(0xFF2196F3)
            )
        }
        
        item {
            StatCard(
                title = "Antwortzeit",
                value = "${statistics.averageResponseTime}",
                subtitle = "Min. durchschn.",
                icon = Icons.Default.Timer,
                color = Color(0xFFFF9800)
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = color
                )
            }
            
            Column {
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
}

@Composable
private fun TasksTab(
    tasks: List<FeedingTask>,
    isLoading: Boolean,
    onTaskComplete: (String, String?) -> Unit,
    onTaskClick: (FeedingTask) -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (tasks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.TaskAlt,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Keine anstehenden Aufgaben",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Group tasks by date
            val tasksByDate = tasks.groupBy { it.scheduledDate }
            
            tasksByDate.forEach { (date, dateTasks) ->
                item {
                    DateHeader(date = date)
                }
                
                items(dateTasks) { task ->
                    TaskCard(
                        task = task,
                        onComplete = { notes ->
                            onTaskComplete(task.id, notes)
                        },
                        onClick = { onTaskClick(task) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DateHeader(date: LocalDate) {
    val displayText = when (date) {
        LocalDate.now() -> "Heute"
        LocalDate.now().plusDays(1) -> "Morgen"
        else -> date.format(DateTimeFormatter.ofPattern("EEEE, d. MMMM", Locale.GERMAN))
    }
    
    Text(
        displayText,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskCard(
    task: FeedingTask,
    onComplete: (String?) -> Unit,
    onClick: () -> Unit
) {
    var showCompleteDialog by remember { mutableStateOf(false) }
    var completionNotes by remember { mutableStateOf("") }
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Task type icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        task.taskType.icon,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        task.taskType.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    task.scheduledTime?.let { time ->
                        Text(
                            time.format(DateTimeFormatter.ofPattern("HH:mm")),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    task.assignedToUserId?.let {
                        Text(
                            "Zugewiesen an: $it", // Would need user name lookup
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Complete button
            if (task.status != TaskStatus.COMPLETED) {
                IconButton(
                    onClick = { showCompleteDialog = true }
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Erledigen",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Erledigt",
                    tint = Color(0xFF4CAF50)
                )
            }
        }
    }
    
    // Complete dialog
    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = { Text("Aufgabe abschließen") },
            text = {
                Column {
                    Text("Möchten Sie diese Aufgabe als erledigt markieren?")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = completionNotes,
                        onValueChange = { completionNotes = it },
                        label = { Text("Notizen (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onComplete(completionNotes.ifEmpty { null })
                        showCompleteDialog = false
                    }
                ) {
                    Text("Erledigt")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

@Composable
private fun ShoppingTab(
    shoppingItems: List<ShoppingItem>,
    predictions: List<ConsumptionPrediction>,
    isLoading: Boolean,
    onItemPurchased: (String) -> Unit,
    onAddPrediction: (ConsumptionPrediction) -> Unit
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Smart predictions
            if (predictions.isNotEmpty()) {
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
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Intelligente Vorschläge",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            predictions.take(3).forEach { prediction ->
                                PredictionItem(
                                    prediction = prediction,
                                    onAdd = { onAddPrediction(prediction) }
                                )
                                if (prediction != predictions.last()) {
                                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                                }
                            }
                        }
                    }
                }
            }
            
            // Shopping items by category
            val itemsByCategory = shoppingItems
                .filter { !it.isPurchased }
                .groupBy { it.category }
            
            itemsByCategory.forEach { (category, items) ->
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text(category.emoji, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            category.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                items(items) { item ->
                    ShoppingItemCard(
                        item = item,
                        onPurchased = { onItemPurchased(item.id) }
                    )
                }
            }
            
            // Purchased items
            val purchasedItems = shoppingItems.filter { it.isPurchased }
            if (purchasedItems.isNotEmpty()) {
                item {
                    Text(
                        "Bereits gekauft",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(purchasedItems) { item ->
                    ShoppingItemCard(
                        item = item,
                        onPurchased = {},
                        isPurchased = true
                    )
                }
            }
        }
    }
}

@Composable
private fun PredictionItem(
    prediction: ConsumptionPrediction,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                prediction.foodName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                "Reicht noch ${prediction.daysUntilEmpty} Tage",
                style = MaterialTheme.typography.bodySmall,
                color = if (prediction.daysUntilEmpty <= 3) 
                    MaterialTheme.colorScheme.error 
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        TextButton(onClick = onAdd) {
            Text("Hinzufügen")
        }
    }
}

@Composable
private fun ShoppingItemCard(
    item: ShoppingItem,
    onPurchased: () -> Unit,
    isPurchased: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isPurchased) 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else if (item.isUrgent)
                Color(0xFFFFEBEE)
            else MaterialTheme.colorScheme.surface
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
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isPurchased,
                    onCheckedChange = { if (!isPurchased) onPurchased() },
                    enabled = !isPurchased
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.productName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (isPurchased) 
                            androidx.compose.ui.text.style.TextDecoration.LineThrough 
                        else null
                    )
                    
                    Row {
                        Text(
                            "${item.quantity} ${item.unit}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        item.brand?.let {
                            Text(
                                " • $it",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    if (item.isUrgent && !isPurchased) {
                        Text(
                            "Dringend!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            item.estimatedPrice?.let { price ->
                Text(
                    "~${String.format("%.2f", price)}€",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ActivityFeedTab(
    activities: List<TeamActivity>,
    isLoading: Boolean,
    onLoadMore: () -> Unit
) {
    if (isLoading && activities.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(activities) { activity ->
                ActivityCard(activity = activity)
            }
            
            if (activities.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        TextButton(onClick = onLoadMore) {
                            Text("Mehr laden")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityCard(
    activity: TeamActivity
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (activity.isImportant) 
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Activity icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (activity.isImportant)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    activity.activityType.icon,
                    style = MaterialTheme.typography.titleSmall
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        activity.activityType.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        formatActivityTime(activity.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    activity.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                // Additional details
                if (activity.details.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    activity.details.forEach { (key, value) ->
                        Text(
                            "$key: $value",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun formatActivityTime(timestamp: LocalDateTime): String {
    val now = LocalDateTime.now()
    val today = now.toLocalDate()
    val activityDate = timestamp.toLocalDate()
    
    return when {
        activityDate == today -> {
            if (timestamp.isAfter(now.minusHours(1))) {
                val minutes = java.time.Duration.between(timestamp, now).toMinutes()
                when {
                    minutes < 1 -> "Gerade eben"
                    minutes < 60 -> "vor $minutes Min."
                    else -> "vor ${minutes / 60} Std."
                }
            } else {
                timestamp.format(DateTimeFormatter.ofPattern("HH:mm"))
            }
        }
        activityDate == today.minusDays(1) -> {
            "Gestern ${timestamp.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        }
        else -> {
            timestamp.format(DateTimeFormatter.ofPattern("dd.MM. HH:mm"))
        }
    }
}