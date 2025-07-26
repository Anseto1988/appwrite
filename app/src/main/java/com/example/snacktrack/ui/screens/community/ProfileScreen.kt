package com.example.snacktrack.ui.screens.community

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.snacktrack.R
import com.example.snacktrack.data.model.CommunityPost
import com.example.snacktrack.data.model.CommunityProfile
import com.example.snacktrack.data.model.UserProfile
import com.example.snacktrack.ui.viewmodel.CommunityViewModel
import com.example.snacktrack.ui.viewmodel.CommunityViewModelFactory
import com.example.snacktrack.ui.viewmodel.ProfileEditState

// Helper function to convert UserProfile to CommunityProfile
private fun convertUserProfileToCommunityProfile(userProfile: UserProfile): CommunityProfile {
    return CommunityProfile(
        id = userProfile.userId,
        userId = userProfile.userId,
        displayName = userProfile.displayName,
        bio = userProfile.bio,
        profileImageUrl = userProfile.profileImageUrl,
        isPremium = false,
        followersCount = userProfile.followerCount,
        followingCount = userProfile.followingCount,
        postsCount = userProfile.postCount,
        createdAt = userProfile.joinedAt.toString()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    userId: String? = null // Wenn null, dann wird das eigene Profil angezeigt
) {
    val context = LocalContext.current
    val viewModel: CommunityViewModel = viewModel(factory = CommunityViewModelFactory(context))
    val userProfile by viewModel.userProfile.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    // Direct reference to the ViewModel's state property
    // No delegation needed as profileEditState is already a State object in the ViewModel
    val profileEditState = viewModel.profileEditState
    val posts by viewModel.posts.collectAsState()
    
    // Profil entweder das eigene oder ein fremdes anhand der userId
    val profile = remember(userId, userProfile) {
        if (userId == null || userId == userProfile?.userId) userProfile
        else null // Hier würde man normalerweise das fremde Profil laden
    }
    
    // Nur eigene Posts filtern (für die Profilansicht)
    val userPosts = remember(posts, profile) {
        posts.filter { it.userId == profile?.userId }
    }
    
    LaunchedEffect(true) {
        viewModel.loadUserProfile()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (userId == null) "Mein Profil" else "Profil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Zurück",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    // Bearbeiten-Button nur für eigenes Profil anzeigen
                    if (userId == null || userId == userProfile?.userId) {
                        IconButton(onClick = { isEditing = !isEditing }) {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.Close else Icons.Default.Edit,
                                contentDescription = if (isEditing) "Abbrechen" else "Bearbeiten",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (profile == null) {
                // Ladeanimation oder Profil nicht gefunden
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                if (isEditing) {
                    // Bearbeitungsansicht
                    ProfileEditContent(
                        profile = convertUserProfileToCommunityProfile(profile),
                        onSave = { displayName, bio, imageUri ->
                            viewModel.createOrUpdateProfile(
                                displayName = displayName,
                                bio = bio ?: ""
                            )
                            isEditing = false
                        },
                        profileEditState = profileEditState.collectAsState().value
                    )
                } else {
                    // Normale Profilansicht
                    ProfileContent(
                        profile = convertUserProfileToCommunityProfile(profile),
                        userPosts = userPosts,
                        navController = navController,
                        isOwnProfile = userId == null || userId == userProfile?.userId,
                        onPostClick = { postId -> navController.navigate("community_post_detail/$postId") }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileContent(
    profile: CommunityProfile,
    userPosts: List<CommunityPost>,
    navController: NavController,
    isOwnProfile: Boolean,
    onPostClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Profilinformationen
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profilbild
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (profile.profileImageUrl != null) {
                        AsyncImage(
                            model = profile.profileImageUrl,
                            contentDescription = "Profilbild",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profilbild",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Name
                Text(
                    text = profile.displayName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Premium-Badge, falls Premium
                if (profile.isPremium) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        modifier = Modifier
                            .padding(4.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Premium",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Premium",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
                
                // Bio
                if (!profile.bio.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = profile.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Statistiken: Beiträge, Follower, Following
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        count = profile.postsCount ?: userPosts.size,
                        label = "Beiträge"
                    )
                    StatItem(
                        count = profile.followersCount ?: 0,
                        label = "Follower"
                    )
                    StatItem(
                        count = profile.followingCount ?: 0,
                        label = "Following"
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Button zum Erstellen neuer Posts (nur auf eigenem Profil)
                if (isOwnProfile) {
                    Button(
                        onClick = { navController.navigate("community_create_post") },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Neuen Beitrag erstellen")
                    }
                } else {
                    // Folgen-Button für fremde Profile
                    Button(
                        onClick = { /* Folgen-Logik hier */ },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("Folgen")
                    }
                }
                
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
            }
        }
        
        // Beiträge des Benutzers
        item {
            Text(
                text = "Beiträge",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        
        if (userPosts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isOwnProfile) "Du hast noch keine Beiträge erstellt" else "${profile.displayName} hat noch keine Beiträge erstellt",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(userPosts) { post ->
                PostItem(
                    post = post,
                    onPostClick = { onPostClick(post.id) },
                    onProfileClick = { /* Bereits auf dem Profil */ },
                    onLikeClick = { /* Like-Logik würde hier implementiert */ }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ProfileEditContent(
    profile: CommunityProfile,
    onSave: (displayName: String, bio: String?, profileImageUri: Uri?) -> Unit,
    profileEditState: ProfileEditState
) {
    var displayName by remember { mutableStateOf(profile.displayName) }
    var bio by remember { mutableStateOf(profile.bio ?: "") }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    
    // Image picker
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profileImageUri = uri
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profilbild mit Bearbeitungsmöglichkeit
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                .clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (profileImageUri != null) {
                // Neu ausgewähltes Bild
                AsyncImage(
                    model = profileImageUri,
                    contentDescription = "Neues Profilbild",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (profile.profileImageUrl != null) {
                // Bestehendes Profilbild
                AsyncImage(
                    model = profile.profileImageUrl,
                    contentDescription = "Profilbild",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Kein Bild vorhanden
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profilbild",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            // Kamera-Icon zum Anzeigen der Bearbeitungsfunktion
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Bild ändern",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Eingabefelder
        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("Bio") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Speichern-Button mit Ladezustand
        Button(
            onClick = { onSave(displayName, bio.ifBlank { null }, profileImageUri) },
            modifier = Modifier.fillMaxWidth(),
            enabled = profileEditState !is ProfileEditState.Saving
        ) {
            if (profileEditState is ProfileEditState.Saving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Profil speichern")
        }
        
        // Fehleranzeige
        if (profileEditState is ProfileEditState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = (profileEditState as ProfileEditState.Error).message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun StatItem(count: Int, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
