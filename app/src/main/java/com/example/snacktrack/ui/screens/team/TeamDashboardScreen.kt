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
import com.example.snacktrack.ui.viewmodel.TeamDashboardViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDashboardScreen(
    navController: NavController,
    teamId: String
) {
    val context = LocalContext.current
    val viewModel = remember { TeamDashboardViewModel(context, teamId) }
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    
    LaunchedEffect(teamId) {
        viewModel.loadTeamData()
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
            QuickStatsRowContent(
                statistics = viewModel.statistics.collectAsState().value,
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
                0 -> TasksTabContent(
                    tasks = viewModel.upcomingTasks.collectAsState().value,
                    isLoading = viewModel.isLoading.collectAsState().value,
                    onTaskComplete = { taskId ->
                        viewModel.completeTask(taskId)
                    }
                )
                1 -> ShoppingTabContent(
                    shoppingItems = viewModel.shoppingItems.collectAsState().value,
                    predictions = viewModel.consumptionPredictions.collectAsState().value,
                    isLoading = viewModel.isLoading.collectAsState().value,
                    onItemPurchased = { itemId ->
                        viewModel.markItemAsPurchased(itemId)
                    },
                    onAddPrediction = { prediction ->
                        viewModel.addPredictionToShoppingList(prediction)
                    }
                )
                2 -> ActivityFeedTabContent(
                    activities = viewModel.recentActivities.collectAsState().value,
                    isLoading = viewModel.isLoading.collectAsState().value,
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

@Composable
private fun QuickStatsRowContent(
    statistics: com.example.snacktrack.ui.viewmodel.TeamStatistics?,
    modifier: Modifier = Modifier
) {
    if (statistics == null) return
    
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            StatCard(
                title = "Aufgaben",
                value = statistics.activeTasks.toString(),
                subtitle = "aktiv",
                icon = Icons.Default.TaskAlt,
                color = Color(0xFF4CAF50)
            )
        }
        
        item {
            StatCard(
                title = "Hunde",
                value = statistics.totalDogs.toString(),
                subtitle = "im Team",
                icon = Icons.Default.Pets,
                color = Color(0xFF2196F3)
            )
        }
        
        item {
            StatCard(
                title = "Mitglieder",
                value = statistics.memberCount.toString(),
                subtitle = "aktiv",
                icon = Icons.Default.Groups,
                color = Color(0xFFFF9800)
            )
        }
    }
}

@Composable
private fun TasksTabContent(
    tasks: List<com.example.snacktrack.ui.viewmodel.TeamTask>,
    isLoading: Boolean,
    onTaskComplete: (String) -> Unit
) {
    if (isLoading && tasks.isEmpty()) {
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
            Text(
                text = "Keine Aufgaben vorhanden",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tasks) { task ->
                TaskCard(
                    task = task,
                    onComplete = { onTaskComplete(task.id) }
                )
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: com.example.snacktrack.ui.viewmodel.TeamTask,
    onComplete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Zugeteilt an: ${task.assignedTo}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Fällig: ${task.dueDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (!task.isCompleted) {
                Button(
                    onClick = onComplete,
                    modifier = Modifier.size(40.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Als erledigt markieren",
                        modifier = Modifier.size(20.dp)
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
}

@Composable
private fun ShoppingTabContent(
    shoppingItems: List<com.example.snacktrack.ui.viewmodel.ShoppingItem>,
    predictions: List<com.example.snacktrack.ui.viewmodel.ConsumptionPrediction>,
    isLoading: Boolean,
    onItemPurchased: (String) -> Unit,
    onAddPrediction: (com.example.snacktrack.ui.viewmodel.ConsumptionPrediction) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (predictions.isNotEmpty()) {
            item {
                Text(
                    text = "Nachkauf-Empfehlungen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            items(predictions) { prediction ->
                PredictionCard(
                    prediction = prediction,
                    onAddToList = { onAddPrediction(prediction) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Einkaufsliste",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
        
        if (shoppingItems.isNotEmpty()) {
            items(shoppingItems) { item ->
                ShoppingItemCard(
                    item = item,
                    onPurchased = { onItemPurchased(item.id) }
                )
            }
        } else {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Einkaufsliste ist leer",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PredictionCard(
    prediction: com.example.snacktrack.ui.viewmodel.ConsumptionPrediction,
    onAddToList: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = prediction.foodName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Für: ${prediction.dogName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Leer in ${prediction.daysRemaining} Tagen",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (prediction.daysRemaining <= 3) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Button(
                onClick = onAddToList,
                modifier = Modifier.size(40.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Zur Liste hinzufügen",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ShoppingItemCard(
    item: com.example.snacktrack.ui.viewmodel.ShoppingItem,
    onPurchased: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Menge: ${item.quantity}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (!item.isPurchased) {
                Button(
                    onClick = onPurchased,
                    modifier = Modifier.size(40.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = "Als gekauft markieren",
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Gekauft",
                    tint = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
private fun ActivityFeedTabContent(
    activities: List<com.example.snacktrack.ui.viewmodel.TeamActivity>,
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
                ActivityCardContent(activity = activity)
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
private fun ActivityCardContent(
    activity: com.example.snacktrack.ui.viewmodel.TeamActivity
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        activity.type,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Text(
                    activity.timestamp.take(10), // Simple date format
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                activity.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}