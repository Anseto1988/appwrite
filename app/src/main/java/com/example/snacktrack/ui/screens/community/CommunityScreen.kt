package com.example.snacktrack.ui.screens.community

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.snacktrack.data.repository.CommunityRepository
import com.example.snacktrack.ui.components.CommonTopAppBar
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class CommunityPost(
    val id: String,
    val authorName: String,
    val authorAvatar: String? = null,
    val title: String,
    val content: String,
    val category: PostCategory,
    val timestamp: LocalDateTime,
    val likes: Int,
    val comments: Int,
    val isLiked: Boolean = false,
    val tags: List<String> = emptyList()
)

enum class PostCategory(val displayName: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val color: Color) {
    TIPS("Tipps & Tricks", Icons.Default.Lightbulb, Color(0xFF4CAF50)),
    QUESTION("Fragen", Icons.Default.HelpOutline, Color(0xFF2196F3)),
    RECIPE("Rezepte", Icons.Default.Restaurant, Color(0xFFFF9800)),
    HEALTH("Gesundheit", Icons.Default.HealthAndSafety, Color(0xFFE91E63)),
    TRAINING("Training", Icons.Default.SportsMartialArts, Color(0xFF9C27B0)),
    GENERAL("Allgemein", Icons.Default.Forum, Color(0xFF607D8B))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val communityRepository = remember { CommunityRepository(context) }
    
    var selectedCategory by remember { mutableStateOf<PostCategory?>(null) }
    var posts by remember { mutableStateOf<List<CommunityPost>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showCreatePost by remember { mutableStateOf(false) }
    
    // Demo-Daten
    LaunchedEffect(Unit) {
        posts = listOf(
            CommunityPost(
                id = "1",
                authorName = "Max Mustermann",
                title = "Beste Trainingssnacks für empfindliche Mägen",
                content = "Hallo zusammen! Mein Hund hat einen empfindlichen Magen...",
                category = PostCategory.TIPS,
                timestamp = LocalDateTime.now().minusHours(2),
                likes = 15,
                comments = 8,
                isLiked = true,
                tags = listOf("Training", "Empfindlicher Magen", "Snacks")
            ),
            CommunityPost(
                id = "2",
                authorName = "Sarah Schmidt",
                title = "Selbstgemachte Leberwurst-Kekse",
                content = "Hier mein Lieblingsrezept für gesunde Hundekekse...",
                category = PostCategory.RECIPE,
                timestamp = LocalDateTime.now().minusDays(1),
                likes = 42,
                comments = 12,
                tags = listOf("DIY", "Rezept", "Gesund")
            ),
            CommunityPost(
                id = "3",
                authorName = "Dr. Vet",
                title = "Wichtig: Neue Erkenntnisse zu Getreideallergien",
                content = "Liebe Community, ich möchte euch über neue Studien informieren...",
                category = PostCategory.HEALTH,
                timestamp = LocalDateTime.now().minusDays(2),
                likes = 89,
                comments = 23,
                tags = listOf("Allergie", "Getreide", "Wissenschaft")
            )
        )
        isLoading = false
    }
    
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Community",
                onBackClick = { navController.navigateUp() },
                actions = {
                    IconButton(onClick = { /* TODO: Suche */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Suchen")
                    }
                    IconButton(onClick = { /* TODO: Filter */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtern")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreatePost = true },
                icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                text = { Text("Neuer Beitrag") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Kategorie-Filter
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("Alle") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.AllInclusive,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
                
                items(PostCategory.values().toList()) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { 
                            selectedCategory = if (selectedCategory == category) null else category 
                        },
                        label = { Text(category.displayName) },
                        leadingIcon = {
                            Icon(
                                category.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedCategory == category) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    category.color
                                }
                            )
                        }
                    )
                }
            }
            
            // Posts
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val filteredPosts = if (selectedCategory == null) {
                        posts
                    } else {
                        posts.filter { it.category == selectedCategory }
                    }
                    
                    if (filteredPosts.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Forum,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "Keine Beiträge in dieser Kategorie",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredPosts) { post ->
                            CommunityPostCard(
                                post = post,
                                onClick = { /* TODO: Navigate to post detail */ },
                                onLikeClick = { /* TODO: Like post */ }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Create Post Dialog
    if (showCreatePost) {
        CreatePostDialog(
            onDismiss = { showCreatePost = false },
            onCreate = { title, content, category ->
                // TODO: Create post
                showCreatePost = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommunityPostCard(
    post: CommunityPost,
    onClick: () -> Unit,
    onLikeClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Avatar
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                post.authorName.first().toString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    Column {
                        Text(
                            post.authorName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            post.timestamp.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Category Badge
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = post.category.color.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            post.category.icon,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = post.category.color
                        )
                        Text(
                            post.category.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = post.category.color
                        )
                    }
                }
            }
            
            // Content
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    post.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    post.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Tags
            if (post.tags.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    post.tags.forEach { tag ->
                        SuggestionChip(
                            onClick = { },
                            label = { Text(tag, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
            }
            
            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Like Button
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onLikeClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = if (post.isLiked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            post.likes.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Comment Count
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            post.comments.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Share Button
                IconButton(
                    onClick = { /* TODO: Share */ },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Teilen",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatePostDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, PostCategory) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(PostCategory.GENERAL) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Neuer Beitrag") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titel") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Inhalt") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                
                // Category Selection
                Text(
                    "Kategorie",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Column {
                    PostCategory.values().forEach { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCategory = category }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                category.icon,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = category.color
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(category.displayName)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(title, content, selectedCategory) },
                enabled = title.isNotBlank() && content.isNotBlank()
            ) {
                Text("Erstellen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}