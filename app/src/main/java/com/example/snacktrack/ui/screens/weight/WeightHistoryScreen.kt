package com.example.snacktrack.ui.screens.weight

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.snacktrack.data.model.WeightEntry
import com.example.snacktrack.data.repository.WeightRepository
import com.example.snacktrack.ui.components.CommonTopAppBar
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun WeightHistoryScreen(
    dogId: String,
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val weightRepository = remember { WeightRepository(context) }
    
    var weightEntries by remember { mutableStateOf<List<WeightEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(dogId) {
        scope.launch {
            weightRepository.getWeightHistory(dogId).collect { entries ->
                weightEntries = entries.sortedBy { it.timestamp }
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Gewichtsverlauf",
                showBackButton = true,
                onBackClick = { navController.popBackStack() },
                onAccountClick = {},
                onLogoutClick = { 
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Gewicht hinzufügen")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                if (weightEntries.isNotEmpty()) {
                    WeightChart(
                        entries = weightEntries,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                LazyColumn {
                    items(weightEntries.reversed()) { entry ->
                        WeightEntryCard(entry = entry)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
    
    if (showAddDialog) {
        AddWeightDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { weight ->
                scope.launch {
                    val newEntry = WeightEntry(
                        dogId = dogId,
                        weight = weight,
                        timestamp = LocalDateTime.now()
                    )
                    weightRepository.addWeightEntry(newEntry)
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun WeightChart(
    entries: List<WeightEntry>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 16.dp, 16.dp, 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gewichtsverlauf",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                if (entries.size >= 2) {
                    val weightDiff = entries.last().weight - entries.first().weight
                    val isIncreasing = weightDiff > 0
                    Text(
                        text = "${if (isIncreasing) "+" else ""}${String.format("%.1f", weightDiff)} kg",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isIncreasing) Color(0xFF4CAF50) else Color(0xFFF44336),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            if (entries.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "📊",
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Mindestens 2 Einträge für Diagramm benötigt",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                val primaryColor = MaterialTheme.colorScheme.primary
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 52.dp, end = 16.dp, top = 16.dp, bottom = 40.dp)
                    ) {
                        val maxWeight = entries.maxOf { it.weight }
                        val minWeight = entries.minOf { it.weight }
                        val weightRange = if (maxWeight - minWeight > 0) maxWeight - minWeight else 1.0
                        val padding = weightRange * 0.15
                        val adjustedMaxWeight = maxWeight + padding
                        val adjustedMinWeight = minWeight - padding
                        val adjustedRange = adjustedMaxWeight - adjustedMinWeight
                        
                        // Gitterlinien Y-Achse
                        val numYLines = 6
                        for (i in 0..numYLines) {
                            val y = size.height * i / numYLines
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.2f),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                        
                        // Gitterlinien X-Achse
                        val numXLines = minOf(entries.size - 1, 6)
                        if (numXLines > 0) {
                            for (i in 0..numXLines) {
                                val x = size.width * i / numXLines
                                drawLine(
                                    color = Color.Gray.copy(alpha = 0.2f),
                                    start = Offset(x, 0f),
                                    end = Offset(x, size.height),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                        }
                        
                        // Berechne Punkte
                        val points = entries.mapIndexed { index, entry ->
                            val x = (index.toFloat() / (entries.size - 1)) * size.width
                            val y = size.height - ((entry.weight - adjustedMinWeight) / adjustedRange * size.height).toFloat()
                            Offset(x, y)
                        }
                        
                        // Zeichne Linien
                        for (i in 0 until points.size - 1) {
                            drawLine(
                                color = primaryColor,
                                start = points[i],
                                end = points[i + 1],
                                strokeWidth = 3.dp.toPx()
                            )
                        }
                        
                        // Zeichne Punkte
                        points.forEach { point ->
                            drawCircle(
                                color = Color.White,
                                radius = 6.dp.toPx(),
                                center = point
                            )
                            drawCircle(
                                color = primaryColor,
                                radius = 4.dp.toPx(),
                                center = point
                            )
                        }
                    }
                    
                    // Y-Achsen-Labels
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(48.dp)
                            .padding(start = 4.dp, top = 16.dp, bottom = 40.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        val maxWeight = entries.maxOf { it.weight }
                        val minWeight = entries.minOf { it.weight }
                        val weightRange = if (maxWeight - minWeight > 0) maxWeight - minWeight else 1.0
                        val padding = weightRange * 0.15
                        val adjustedMaxWeight = maxWeight + padding
                        val adjustedMinWeight = minWeight - padding
                        
                        val numLabels = 6
                        for (i in 0..numLabels) {
                            val weight = adjustedMaxWeight - (adjustedMaxWeight - adjustedMinWeight) * i / numLabels
                            Text(
                                text = "${String.format("%.1f", weight)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                    
                    // Y-Achsen-Titel
                    Text(
                        text = "kg",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 4.dp, top = 0.dp)
                    )
                    
                    // X-Achsen-Labels
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(start = 52.dp, end = 16.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = entries.first().timestamp.format(DateTimeFormatter.ofPattern("dd.MM")),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                        
                        if (entries.size >= 3) {
                            Text(
                                text = entries[entries.size / 2].timestamp.format(DateTimeFormatter.ofPattern("dd.MM")),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                        
                        if (entries.size > 1) {
                            Text(
                                text = entries.last().timestamp.format(DateTimeFormatter.ofPattern("dd.MM")),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                    
                    // X-Achsen-Titel
                    Text(
                        text = "Datum",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeightEntryCard(entry: WeightEntry) {
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
            Column {
                Text(
                    text = "${entry.weight} kg",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = entry.timestamp.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            entry.note?.let { note ->
                Text(
                    text = note,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AddWeightDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var weight by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gewicht hinzufügen") },
        text = {
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Gewicht (kg)") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    weight.toDoubleOrNull()?.let { onConfirm(it) }
                }
            ) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}
