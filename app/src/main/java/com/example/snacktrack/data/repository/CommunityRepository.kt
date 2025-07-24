package com.example.snacktrack.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.snacktrack.data.model.CommunityPost
import com.example.snacktrack.data.model.CommunityProfile
import com.example.snacktrack.data.model.CommunityComment
import com.example.snacktrack.data.model.Dog
import com.example.snacktrack.data.model.PostType
import com.example.snacktrack.data.service.AppwriteService
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Repository für Community-Funktionen wie Posts, Profile, Kommentare etc.
 */
class CommunityRepository(private val context: Context) {

    private val TAG = "CommunityRepository"
    
    // Appwrite Services
    private val appwriteService = AppwriteService.getInstance(context)
    private val databases = appwriteService.databases
    private val storage = appwriteService.storage
    private val account = appwriteService.account
    
    // Konstanten für Collections
    companion object {
        const val COMMUNITY_DATABASE_ID = "snacktrack-db"  // Main database for consistency
        
        const val COLLECTION_COMMUNITY_POSTS = "community_posts" 
        const val COLLECTION_COMMUNITY_PROFILES = "community_profiles"
        const val COLLECTION_COMMUNITY_COMMENTS = "community_comments"
        const val COLLECTION_COMMUNITY_LIKES = "community_likes"
        const val COLLECTION_COMMUNITY_FOLLOWS = "community_follows"
        
        const val BUCKET_COMMUNITY_IMAGES = "community_images"
    }
    
    // State Flows für UI Updates
    private val _feedPostsStateFlow = MutableStateFlow<List<CommunityPost>>(emptyList())
    val feedPostsStateFlow: StateFlow<List<CommunityPost>> = _feedPostsStateFlow.asStateFlow()
    
    private val _userProfileStateFlow = MutableStateFlow<CommunityProfile?>(null)
    val userProfileStateFlow: StateFlow<CommunityProfile?> = _userProfileStateFlow.asStateFlow()
    
    /**
     * Erstellt oder aktualisiert ein Community-Profil
     */
    suspend fun createOrUpdateProfile(
        displayName: String, 
        bio: String? = null,
        profileImageUri: Uri? = null
    ): Result<CommunityProfile> = withContext(Dispatchers.IO) {
        try {
            // Aktuellen Benutzer abrufen
            val currentUser = account.get()
            val userId = currentUser.id
            
            // Prüfen, ob Profil bereits existiert
            val existingProfiles = databases.listDocuments(
                databaseId = COMMUNITY_DATABASE_ID,
                collectionId = COLLECTION_COMMUNITY_PROFILES,
                queries = listOf(Query.equal("userId", userId))
            )
            
            var profileImageUrl: String? = null
            
            // Wenn ein neues Profilbild hochgeladen werden soll
            if (profileImageUri != null) {
                try {
                    val inputStream = context.contentResolver.openInputStream(profileImageUri)
                    inputStream?.use { stream ->
                        val file = File.createTempFile("profile", ".jpg", context.cacheDir)
                        file.outputStream().use { outputStream ->
                            stream.copyTo(outputStream)
                        }
                        
                        // Bild hochladen
                        val uploadResult = storage.createFile(
                            bucketId = BUCKET_COMMUNITY_IMAGES,
                            fileId = ID.unique(),
                            file = io.appwrite.models.InputFile.fromFile(file)
                        )
                        
                        // Generiere URL für das Bild
                        val fileView = storage.getFileView(
                            bucketId = BUCKET_COMMUNITY_IMAGES,
                            fileId = uploadResult.id
                        )
                        profileImageUrl = fileView.toString()
                        
                        file.delete() // Temporäre Datei löschen
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Fehler beim Hochladen des Profilbildes: ${e.message}", e)
                    // Wir setzen trotzdem fort ohne das Bild
                }
            }
            
            // Profildaten vorbereiten
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
            
            val communityProfile = if (existingProfiles.documents.isNotEmpty()) {
                // Profil aktualisieren
                val existingProfile = existingProfiles.documents.first()
                val existingId = existingProfile.id
                
                // Alte Werte beibehalten wenn keine neuen angegeben sind
                val existingImageUrl = existingProfile.data["profileImageUrl"]?.toString()
                val updateImageUrl = profileImageUrl ?: existingImageUrl
                
                val data = mutableMapOf(
                    "displayName" to displayName,
                    "bio" to (bio ?: existingProfile.data["bio"]?.toString() ?: "")
                )
                
                // Nur hinzufügen, wenn nicht null
                updateImageUrl?.let { data["profileImageUrl"] = it }
                
                val response = databases.updateDocument(
                    databaseId = COMMUNITY_DATABASE_ID,
                    collectionId = COLLECTION_COMMUNITY_PROFILES,
                    documentId = existingId,
                    data = data
                )
                
                CommunityProfile(
                    id = response.id,
                    userId = response.data["userId"].toString(),
                    displayName = response.data["displayName"].toString(),
                    bio = response.data["bio"]?.toString(),
                    profileImageUrl = response.data["profileImageUrl"]?.toString(),
                    isPremium = response.data["isPremium"] as? Boolean ?: false,
                    followersCount = (response.data["followersCount"] as? Number)?.toInt() ?: 0,
                    followingCount = (response.data["followingCount"] as? Number)?.toInt() ?: 0,
                    postsCount = (response.data["postsCount"] as? Number)?.toInt() ?: 0,
                    createdAt = response.data["createdAt"]?.toString()
                )
            } else {
                // Neues Profil erstellen
                val response = databases.createDocument(
                    databaseId = COMMUNITY_DATABASE_ID,
                    collectionId = COLLECTION_COMMUNITY_PROFILES,
                    documentId = ID.unique(),
                    data = mapOf(
                        "userId" to userId,
                        "displayName" to displayName,
                        "bio" to (bio ?: ""),
                        "profileImageUrl" to profileImageUrl,
                        "isPremium" to false,
                        "followersCount" to 0,
                        "followingCount" to 0,
                        "postsCount" to 0,
                        "createdAt" to now
                    )
                )
                
                CommunityProfile(
                    id = response.id,
                    userId = response.data["userId"].toString(),
                    displayName = response.data["displayName"].toString(),
                    bio = response.data["bio"]?.toString(),
                    profileImageUrl = response.data["profileImageUrl"]?.toString(),
                    isPremium = false,
                    followersCount = 0,
                    followingCount = 0,
                    postsCount = 0,
                    createdAt = now
                )
            }
            
            // StateFlow aktualisieren
            _userProfileStateFlow.value = communityProfile
            
            Result.success(communityProfile)
        } catch (e: Exception) {
            Log.e(TAG, "Fehler beim Erstellen/Aktualisieren des Profils: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Holt das Profil des aktuellen Benutzers
     */
    suspend fun getCurrentUserProfile(): Result<CommunityProfile?> = withContext(Dispatchers.IO) {
        try {
            val currentUser = account.get()
            val userId = currentUser.id
            
            val response = databases.listDocuments(
                databaseId = COMMUNITY_DATABASE_ID,
                collectionId = COLLECTION_COMMUNITY_PROFILES,
                queries = listOf(Query.equal("userId", userId))
            )
            
            val profile = if (response.documents.isNotEmpty()) {
                val doc = response.documents.first()
                CommunityProfile(
                    id = doc.id,
                    userId = doc.data["userId"].toString(),
                    displayName = doc.data["displayName"].toString(),
                    bio = doc.data["bio"]?.toString(),
                    profileImageUrl = doc.data["profileImageUrl"]?.toString(),
                    isPremium = doc.data["isPremium"] as? Boolean ?: false,
                    followersCount = (doc.data["followersCount"] as? Number)?.toInt() ?: 0,
                    followingCount = (doc.data["followingCount"] as? Number)?.toInt() ?: 0,
                    postsCount = (doc.data["postsCount"] as? Number)?.toInt() ?: 0,
                    createdAt = doc.data["createdAt"]?.toString()
                )
            } else {
                null
            }
            
            // StateFlow aktualisieren
            _userProfileStateFlow.value = profile
            
            Result.success(profile)
        } catch (e: Exception) {
            Log.e(TAG, "Fehler beim Abrufen des Benutzerprofils: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Erstellt einen neuen Community-Post
     */
    suspend fun createPost(
        content: String,
        postType: PostType,
        dogId: String? = null,
        imageUris: List<Uri> = emptyList(),
        hashtags: List<String> = emptyList()
    ): Result<CommunityPost> = withContext(Dispatchers.IO) {
        try {
            val currentUser = account.get()
            val userId = currentUser.id
            
            // Prüfen, ob Benutzer ein Profil hat, sonst erstellen
            val userProfile = getCurrentUserProfile().getOrNull() ?: createOrUpdateProfile(
                displayName = currentUser.name
            ).getOrNull()
            
            // Bilder hochladen
            val imageUrls = mutableListOf<String>()
            for (uri in imageUris) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    inputStream?.use { stream ->
                        val file = File.createTempFile("post", ".jpg", context.cacheDir)
                        file.outputStream().use { outputStream ->
                            stream.copyTo(outputStream)
                        }
                        
                        // Bild hochladen
                        val uploadResult = storage.createFile(
                            bucketId = BUCKET_COMMUNITY_IMAGES,
                            fileId = ID.unique(),
                            file = io.appwrite.models.InputFile.fromFile(file)
                        )
                        
                        // URL für das Bild generieren
                        val imageView = storage.getFileView(
                            bucketId = BUCKET_COMMUNITY_IMAGES,
                            fileId = uploadResult.id
                        )
                        
                        imageView?.toString()?.let { imageUrls.add(it) }
                        
                        file.delete() // Temporäre Datei löschen
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Fehler beim Hochladen eines Post-Bildes: ${e.message}", e)
                    // Wir setzen trotzdem fort ohne das Bild
                }
            }
            
            // Post erstellen
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
            
            val response = databases.createDocument(
                databaseId = COMMUNITY_DATABASE_ID,
                collectionId = COLLECTION_COMMUNITY_POSTS,
                documentId = ID.unique(),
                data = mapOf(
                    "userId" to userId,
                    "dogId" to dogId,
                    "content" to content,
                    "postType" to postType.databaseValue,
                    "imageUrls" to imageUrls,
                    "hashtags" to hashtags,
                    "likesCount" to 0,
                    "commentsCount" to 0
                ).filterValues { it != null }
            )
            
            // Post-Anzahl im Profil aktualisieren
            userProfile?.let {
                databases.updateDocument(
                    databaseId = COMMUNITY_DATABASE_ID,
                    collectionId = COLLECTION_COMMUNITY_PROFILES,
                    documentId = it.id,
                    data = mapOf(
                        "postsCount" to (it.postsCount + 1)
                    )
                )
            }
            
            val post = CommunityPost(
                id = response.id,
                userId = response.data["userId"].toString(),
                dogId = response.data["dogId"]?.toString(),
                content = response.data["content"].toString(),
                postType = PostType.fromDatabaseValue(response.data["postType"].toString()),
                imageUrls = (response.data["imageUrls"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                hashtags = (response.data["hashtags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                likesCount = 0,
                commentsCount = 0,
                createdAt = response.createdAt,
                userProfile = userProfile
            )
            
            // Feed aktualisieren
            refreshFeed()
            
            Result.success(post)
        } catch (e: Exception) {
            Log.e(TAG, "Fehler beim Erstellen eines Posts: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Holt die neuesten Posts für den Feed
     */
    suspend fun refreshFeed(): Result<List<CommunityPost>> = withContext(Dispatchers.IO) {
        try {
            val response = databases.listDocuments(
                databaseId = COMMUNITY_DATABASE_ID,
                collectionId = COLLECTION_COMMUNITY_POSTS,
                queries = listOf(
                    Query.orderDesc("\$createdAt"),
                    Query.limit(50)
                )
            )
            
            val posts = response.documents.map { doc ->
                // User-Profil abrufen für jeden Post
                val userProfileResponse = try {
                    databases.listDocuments(
                        databaseId = COMMUNITY_DATABASE_ID,
                        collectionId = COLLECTION_COMMUNITY_PROFILES,
                        queries = listOf(Query.equal("userId", doc.data["userId"].toString()))
                    )
                } catch (e: Exception) {
                    null
                }
                
                val userProfile = userProfileResponse?.documents?.firstOrNull()?.let { profileDoc ->
                    CommunityProfile(
                        id = profileDoc.id,
                        userId = profileDoc.data["userId"].toString(),
                        displayName = profileDoc.data["displayName"].toString(),
                        bio = profileDoc.data["bio"]?.toString(),
                        profileImageUrl = profileDoc.data["profileImageUrl"]?.toString(),
                        isPremium = profileDoc.data["isPremium"] as? Boolean ?: false,
                        followersCount = (profileDoc.data["followersCount"] as? Number)?.toInt() ?: 0,
                        followingCount = (profileDoc.data["followingCount"] as? Number)?.toInt() ?: 0,
                        postsCount = (profileDoc.data["postsCount"] as? Number)?.toInt() ?: 0
                    )
                }
                
                // Post-Objekt erstellen
                CommunityPost(
                    id = doc.id,
                    userId = doc.data["userId"].toString(),
                    dogId = doc.data["dogId"]?.toString(),
                    content = doc.data["content"].toString(),
                    postType = PostType.fromDatabaseValue(doc.data["postType"].toString()),
                    imageUrls = (doc.data["imageUrls"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                    hashtags = (doc.data["hashtags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                    likesCount = (doc.data["likesCount"] as? Number)?.toInt() ?: 0,
                    commentsCount = (doc.data["commentsCount"] as? Number)?.toInt() ?: 0,
                    createdAt = doc.createdAt, // Use Appwrite's automatic timestamp
                    userProfile = userProfile
                )
            }
            
            // StateFlow aktualisieren
            _feedPostsStateFlow.value = posts
            
            Result.success(posts)
        } catch (e: Exception) {
            Log.e(TAG, "Fehler beim Abrufen des Feeds: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Löscht einen Post
     */
    suspend fun deletePost(postId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            databases.deleteDocument(
                databaseId = COMMUNITY_DATABASE_ID,
                collectionId = COLLECTION_COMMUNITY_POSTS,
                documentId = postId
            )
            
            // Feed aktualisieren
            refreshFeed()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Fehler beim Löschen des Posts: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Liked oder Unlikes einen Post
     */
    suspend fun toggleLike(postId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val currentUser = account.get()
            val userId = currentUser.id
            
            // Prüfen, ob der Benutzer den Post bereits geliked hat
            val likeResponse = databases.listDocuments(
                databaseId = COMMUNITY_DATABASE_ID,
                collectionId = COLLECTION_COMMUNITY_LIKES,
                queries = listOf(
                    Query.equal("userId", userId),
                    Query.equal("postId", postId)
                )
            )
            
            val isLiked = likeResponse.documents.isNotEmpty()
            
            if (isLiked) {
                // Like entfernen
                val likeId = likeResponse.documents.first().id
                databases.deleteDocument(
                    databaseId = COMMUNITY_DATABASE_ID,
                    collectionId = COLLECTION_COMMUNITY_LIKES,
                    documentId = likeId
                )
                
                // Likes-Zähler aktualisieren
                val postResponse = databases.getDocument(
                    databaseId = COMMUNITY_DATABASE_ID,
                    collectionId = COLLECTION_COMMUNITY_POSTS,
                    documentId = postId
                )
                
                val currentLikes = (postResponse.data["likesCount"] as? Number)?.toInt() ?: 0
                databases.updateDocument(
                    databaseId = COMMUNITY_DATABASE_ID,
                    collectionId = COLLECTION_COMMUNITY_POSTS,
                    documentId = postId,
                    data = mapOf("likesCount" to maxOf(0, currentLikes - 1))
                )
                
                Result.success(false) // Nicht mehr geliked
            } else {
                // Like hinzufügen
                val now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                databases.createDocument(
                    databaseId = COMMUNITY_DATABASE_ID,
                    collectionId = COLLECTION_COMMUNITY_LIKES,
                    documentId = ID.unique(),
                    data = mapOf(
                        "userId" to userId,
                        "postId" to postId
                    )
                )
                
                // Likes-Zähler aktualisieren
                val postResponse = databases.getDocument(
                    databaseId = COMMUNITY_DATABASE_ID,
                    collectionId = COLLECTION_COMMUNITY_POSTS,
                    documentId = postId
                )
                
                val currentLikes = (postResponse.data["likesCount"] as? Number)?.toInt() ?: 0
                databases.updateDocument(
                    databaseId = COMMUNITY_DATABASE_ID,
                    collectionId = COLLECTION_COMMUNITY_POSTS,
                    documentId = postId,
                    data = mapOf("likesCount" to currentLikes + 1)
                )
                
                Result.success(true) // Geliked
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fehler beim Liken/Unliken: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Erstellt einen neuen Kommentar zu einem Post
     */
    suspend fun createComment(
        postId: String,
        content: String
    ): Result<CommunityComment> = withContext(Dispatchers.IO) {
        try {
            val currentUser = account.get()
            val userId = currentUser.id
            
            // Kommentar erstellen
            val response = databases.createDocument(
                databaseId = COMMUNITY_DATABASE_ID,
                collectionId = COLLECTION_COMMUNITY_COMMENTS,
                documentId = ID.unique(),
                data = mapOf(
                    "postId" to postId,
                    "userId" to userId,
                    "content" to content
                )
            )
            
            // User-Profil für den Kommentar abrufen
            val userProfile = getCurrentUserProfile().getOrNull()
            
            val comment = CommunityComment(
                id = response.id,
                postId = response.data["postId"].toString(),
                userId = response.data["userId"].toString(),
                content = response.data["content"].toString(),
                createdAt = response.createdAt,
                userProfile = userProfile
            )
            
            // Kommentar-Anzahl im Post aktualisieren
            val postResponse = databases.getDocument(
                databaseId = COMMUNITY_DATABASE_ID,
                collectionId = COLLECTION_COMMUNITY_POSTS,
                documentId = postId
            )
            
            val currentComments = (postResponse.data["commentsCount"] as? Number)?.toInt() ?: 0
            databases.updateDocument(
                databaseId = COMMUNITY_DATABASE_ID,
                collectionId = COLLECTION_COMMUNITY_POSTS,
                documentId = postId,
                data = mapOf("commentsCount" to currentComments + 1)
            )
            
            Result.success(comment)
        } catch (e: Exception) {
            Log.e(TAG, "Fehler beim Erstellen eines Kommentars: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Holt alle Kommentare zu einem Post
     */
    suspend fun getCommentsForPost(postId: String): Result<List<CommunityComment>> = withContext(Dispatchers.IO) {
        try {
            val response = databases.listDocuments(
                databaseId = COMMUNITY_DATABASE_ID,
                collectionId = COLLECTION_COMMUNITY_COMMENTS,
                queries = listOf(
                    Query.equal("postId", postId),
                    Query.orderAsc("\$createdAt")
                )
            )
            
            val comments = response.documents.map { doc ->
                // User-Profil für jeden Kommentar abrufen
                val userProfileResponse = try {
                    databases.listDocuments(
                        databaseId = COMMUNITY_DATABASE_ID,
                        collectionId = COLLECTION_COMMUNITY_PROFILES,
                        queries = listOf(Query.equal("userId", doc.data["userId"].toString()))
                    )
                } catch (e: Exception) {
                    null
                }
                
                val userProfile = userProfileResponse?.documents?.firstOrNull()?.let { profileDoc ->
                    CommunityProfile(
                        id = profileDoc.id,
                        userId = profileDoc.data["userId"].toString(),
                        displayName = profileDoc.data["displayName"].toString(),
                        bio = profileDoc.data["bio"]?.toString(),
                        profileImageUrl = profileDoc.data["profileImageUrl"]?.toString(),
                        isPremium = profileDoc.data["isPremium"] as? Boolean ?: false,
                        followersCount = (profileDoc.data["followersCount"] as? Number)?.toInt() ?: 0,
                        followingCount = (profileDoc.data["followingCount"] as? Number)?.toInt() ?: 0,
                        postsCount = (profileDoc.data["postsCount"] as? Number)?.toInt() ?: 0
                    )
                }
                
                CommunityComment(
                    id = doc.id,
                    postId = doc.data["postId"].toString(),
                    userId = doc.data["userId"].toString(),
                    content = doc.data["content"].toString(),
                    createdAt = doc.createdAt,
                    userProfile = userProfile
                )
            }
            
            Result.success(comments)
        } catch (e: Exception) {
            Log.e(TAG, "Fehler beim Abrufen der Kommentare: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Löscht einen Kommentar
     */
    suspend fun deleteComment(commentId: String, postId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            databases.deleteDocument(
                databaseId = COMMUNITY_DATABASE_ID,
                collectionId = COLLECTION_COMMUNITY_COMMENTS,
                documentId = commentId
            )
            
            // Kommentar-Anzahl im Post aktualisieren
            val postResponse = databases.getDocument(
                databaseId = COMMUNITY_DATABASE_ID,
                collectionId = COLLECTION_COMMUNITY_POSTS,
                documentId = postId
            )
            
            val currentComments = (postResponse.data["commentsCount"] as? Number)?.toInt() ?: 0
            databases.updateDocument(
                databaseId = COMMUNITY_DATABASE_ID,
                collectionId = COLLECTION_COMMUNITY_POSTS,
                documentId = postId,
                data = mapOf("commentsCount" to maxOf(0, currentComments - 1))
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Fehler beim Löschen des Kommentars: ${e.message}", e)
            Result.failure(e)
        }
    }
}
