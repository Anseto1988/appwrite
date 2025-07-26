package com.example.snacktrack.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.snacktrack.data.repository.AuthRepository
import com.example.snacktrack.data.repository.DogRepository
import com.example.snacktrack.data.repository.TeamRepository
import com.example.snacktrack.data.service.AppwriteService
import com.example.snacktrack.ui.navigation.Screen
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class DashboardTile(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val backgroundColor: Color,
    val backgroundGradient: List<Color>? = null,
    val route: String? = null,
    val action: (() -> Unit)? = null,
    val badge: String? = null,
    val enabled: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TileDashboardScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository(context) }
    val dogRepository = remember { DogRepository(context) }
    val teamRepository = remember { TeamRepository(context) }
    
    var userName by remember { mutableStateOf("") }
    var dogCount by remember { mutableStateOf(0) }
    var teamCount by remember { mutableStateOf(0) }
    var todayFeedingsCount by remember { mutableStateOf(0) }
    var activeNotifications by remember { mutableStateOf(0) }
    
    // Lade Benutzerdaten
    LaunchedEffect(Unit) {
        authRepository.getCurrentUser().collect { user ->
            userName = user?.name ?: "Benutzer"
        }
    }
    
    // Lade Statistiken
    LaunchedEffect(Unit) {
        dogRepository.getDogs().collect { dogs ->
            dogCount = dogs.size
            // TODO: Lade heutige Fütterungen
            todayFeedingsCount = dogs.size * 2 // Placeholder
        }
    }
    
    LaunchedEffect(Unit) {
        teamRepository.getTeams().collect { teams ->
            teamCount = teams.size
        }
    }
    
    val tiles = remember(dogCount, teamCount, todayFeedingsCount, activeNotifications) {
        val primaryColor = Color(0xFF6200EE)
        val primaryContainerColor = Color(0xFFBB86FC)
        
        listOf(
            // Hauptfunktionen
            DashboardTile(
                id = "dogs",
                title = "Meine Hunde",
                subtitle = "$dogCount Hunde",
                icon = Icons.Default.Pets,
                backgroundColor = primaryColor,
                backgroundGradient = listOf(
                    primaryColor,
                    primaryContainerColor
                ),
                route = Screen.DogList.route,
                badge = if (dogCount == 0) "!" else null
            ),
            
            DashboardTile(
                id = "quick_feed",
                title = "Schnell-Fütterung",
                subtitle = "Heute: $todayFeedingsCount",
                icon = Icons.Default.Restaurant,
                backgroundColor = Color(0xFF4CAF50),
                backgroundGradient = listOf(
                    Color(0xFF4CAF50),
                    Color(0xFF81C784)
                ),
                route = Screen.DogList.route, // For now navigate to dog list to select dog for feeding
                enabled = dogCount > 0
            ),
            
            DashboardTile(
                id = "barcode",
                title = "Barcode Scanner",
                subtitle = "Futter scannen",
                icon = Icons.Default.QrCodeScanner,
                backgroundColor = Color(0xFF2196F3),
                backgroundGradient = listOf(
                    Color(0xFF2196F3),
                    Color(0xFF64B5F6)
                ),
                route = Screen.DogList.route, // For now navigate to dog list to select dog for barcode scanning
                enabled = dogCount > 0
            ),
            
            DashboardTile(
                id = "statistics",
                title = "Statistiken",
                subtitle = "Analysen & Trends",
                icon = Icons.Default.Analytics,
                backgroundColor = Color(0xFF9C27B0),
                backgroundGradient = listOf(
                    Color(0xFF9C27B0),
                    Color(0xFFBA68C8)
                ),
                route = Screen.DogList.route, // For now navigate to dog list to select dog for statistics
                enabled = dogCount > 0
            ),
            
            // Community & Team
            DashboardTile(
                id = "teams",
                title = "Teams",
                subtitle = "$teamCount Team(s)",
                icon = Icons.Default.Groups,
                backgroundColor = Color(0xFFFF9800),
                backgroundGradient = listOf(
                    Color(0xFFFF9800),
                    Color(0xFFFFB74D)
                ),
                route = Screen.TeamManagement.route,
                badge = if (activeNotifications > 0) activeNotifications.toString() else null
            ),
            
            DashboardTile(
                id = "community",
                title = "Community",
                subtitle = "Forum & Tipps",
                icon = Icons.Default.Forum,
                backgroundColor = Color(0xFF00BCD4),
                backgroundGradient = listOf(
                    Color(0xFF00BCD4),
                    Color(0xFF4DD0E1)
                ),
                route = Screen.Community.route
            ),
            
            // Verwaltung
            DashboardTile(
                id = "food_db",
                title = "Futterdatenbank",
                subtitle = "Alle Futtersorten",
                icon = Icons.Default.Storage,
                backgroundColor = Color(0xFF795548),
                backgroundGradient = listOf(
                    Color(0xFF795548),
                    Color(0xFFA1887F)
                ),
                route = Screen.FoodDatabase.route
            ),
            
            DashboardTile(
                id = "health",
                title = "Gesundheit",
                subtitle = "Vorsorge & Termine",
                icon = Icons.Default.HealthAndSafety,
                backgroundColor = Color(0xFFE91E63),
                backgroundGradient = listOf(
                    Color(0xFFE91E63),
                    Color(0xFFF06292)
                ),
                route = Screen.PreventionPlan.route
            ),
            
            DashboardTile(
                id = "settings",
                title = "Einstellungen",
                subtitle = "App konfigurieren",
                icon = Icons.Default.Settings,
                backgroundColor = Color(0xFF607D8B),
                backgroundGradient = listOf(
                    Color(0xFF607D8B),
                    Color(0xFF90A4AE)
                ),
                route = Screen.Settings.route
            )
        )
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "SnackTrack",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, d. MMMM yyyy")),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    if (activeNotifications > 0) {
                        BadgedBox(
                            badge = {
                                Badge {
                                    Text(activeNotifications.toString())
                                }
                            }
                        ) {
                            IconButton(onClick = { /* TODO: Show notifications */ }) {
                                Icon(Icons.Default.Notifications, contentDescription = "Benachrichtigungen")
                            }
                        }
                    }
                    
                    IconButton(onClick = {
                        scope.launch {
                            authRepository.logout()
                            navController.navigate("auth") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Abmelden")
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
            // Begrüßung
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.WavingHand,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "Willkommen zurück, $userName!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (dogCount == 0) {
                            Text(
                                "Füge deinen ersten Hund hinzu, um loszulegen!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            Text(
                                "Was möchtest du heute tun?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
            
            // Kachel-Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(tiles) { tile ->
                    DashboardTileItem(
                        tile = tile,
                        onClick = {
                            when {
                                tile.route != null -> navController.navigate(tile.route)
                                tile.action != null -> tile.action.invoke()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardTileItem(
    tile: DashboardTile,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (tile.backgroundGradient != null) {
                    Brush.verticalGradient(tile.backgroundGradient)
                } else {
                    Brush.linearGradient(listOf(tile.backgroundColor, tile.backgroundColor))
                }
            )
            .clickable(enabled = tile.enabled) { onClick() }
    ) {
        if (!tile.enabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )
        }
        
        // Badge
        tile.badge?.let { badge ->
            Badge(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Text(badge)
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                tile.icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.White
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = tile.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            tile.subtitle?.let { subtitle ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}