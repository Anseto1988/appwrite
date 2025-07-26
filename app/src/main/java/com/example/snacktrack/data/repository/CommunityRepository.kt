package com.example.snacktrack.data.repository

import android.content.Context
import android.util.Log
import com.example.snacktrack.data.service.AppwriteService
import com.example.snacktrack.data.service.AppwriteConfig
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.models.Document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.example.snacktrack.data.model.CommunityPost
import com.example.snacktrack.data.model.CommunityComment
import com.example.snacktrack.data.model.CommunityProfile

class CommunityRepository(private val context: Context) : BaseRepository() {
    
    private val appwriteService = AppwriteService.getInstance(context)
    private val databases = appwriteService.databases
    
    /**
     * Holt alle Community-Posts
     */
    fun getPosts(category: String? = null): Flow<List<CommunityPost>> = flow {
        try {
            Log.d("CommunityRepository", "Lade Community-Posts, Kategorie: $category")
            
            // Prüfe Session
            if (!appwriteService.ensureValidSession()) {
                Log.e("CommunityRepository", "Keine gültige Session")
                emit(emptyList())
                return@flow
            }
            
            val user = appwriteService.account.get()
            
            // Query für Posts
            val queries = mutableListOf<String>()
            if (category != null) {
                queries.add(Query.equal("category", category))
            }
            queries.add(Query.orderDesc("timestamp"))
            
            val response = databases.listDocuments(
                databaseId = AppwriteConfig.DATABASE_ID,
                collectionId = AppwriteConfig.COLLECTION_COMMUNITY_POSTS,
                queries = queries
            )
            
            val posts = response.documents.map { doc ->
                convertDocumentToPost(doc, user.id)
            }
            
            Log.d("CommunityRepository", "Posts geladen: ${posts.size}")
            emit(posts)
            
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Fehler beim Laden der Posts: ${e.message}", e)
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Erstellt einen neuen Post
     */
    suspend fun createPost(
        title: String,
        content: String,
        category: String,
        postType: String = "general",
        images: List<String> = emptyList()
    ): Result<CommunityPost> = withContext(Dispatchers.IO) {
        safeApiCall {
            val user = appwriteService.account.get()
            
            val data = mapOf(
                "authorId" to user.id,
                "authorName" to user.name,
                "title" to title,
                "content" to content,
                "category" to category,
                "postType" to postType,
                "timestamp" to LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "likes" to 0,
                "comments" to 0,
                "imageIds" to images,
                "likedBy" to emptyList<String>()
            )
            
            val response = databases.createDocument(
                databaseId = AppwriteConfig.DATABASE_ID,
                collectionId = AppwriteConfig.COLLECTION_COMMUNITY_POSTS,
                documentId = ID.unique(),
                data = data
            )
            
            convertDocumentToPost(response, user.id)
        }
    }
    
    /**
     * Holt Kommentare für einen Post
     */
    fun getComments(postId: String): Flow<List<CommunityComment>> = flow {
        try {
            val user = appwriteService.account.get()
            
            val response = databases.listDocuments(
                databaseId = AppwriteConfig.DATABASE_ID,
                collectionId = AppwriteConfig.COLLECTION_COMMUNITY_COMMENTS,
                queries = listOf(
                    Query.equal("postId", postId),
                    Query.orderDesc("timestamp")
                )
            )
            
            val comments = response.documents.map { doc ->
                convertDocumentToComment(doc, user.id)
            }
            
            emit(comments)
            
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Fehler beim Laden der Kommentare: ${e.message}", e)
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Erstellt einen neuen Kommentar
     */
    suspend fun createComment(postId: String, content: String): Result<CommunityComment> = withContext(Dispatchers.IO) {
        safeApiCall {
            val user = appwriteService.account.get()
            
            val data = mapOf(
                "postId" to postId,
                "authorId" to user.id,
                "authorName" to user.name,
                "content" to content,
                "timestamp" to LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "likes" to 0,
                "likedBy" to emptyList<String>()
            )
            
            val response = databases.createDocument(
                databaseId = AppwriteConfig.DATABASE_ID,
                collectionId = AppwriteConfig.COLLECTION_COMMUNITY_COMMENTS,
                documentId = ID.unique(),
                data = data
            )
            
            // Erhöhe Kommentarzähler im Post
            incrementCommentCount(postId)
            
            convertDocumentToComment(response, user.id)
        }
    }
    
    /**
     * Liked oder unliked einen Post
     */
    suspend fun toggleLikePost(postId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        safeApiCall {
            val user = appwriteService.account.get()
            
            // Hole aktuellen Post
            val post = databases.getDocument(
                databaseId = AppwriteConfig.DATABASE_ID,
                collectionId = AppwriteConfig.COLLECTION_COMMUNITY_POSTS,
                documentId = postId
            )
            
            @Suppress("UNCHECKED_CAST")
            val likedBy = (post.data["likedBy"] as? List<String>)?.toMutableList() ?: mutableListOf()
            val isLiked = user.id in likedBy
            
            if (isLiked) {
                likedBy.remove(user.id)
            } else {
                likedBy.add(user.id)
            }
            
            // Update Post
            databases.updateDocument(
                databaseId = AppwriteConfig.DATABASE_ID,
                collectionId = AppwriteConfig.COLLECTION_COMMUNITY_POSTS,
                documentId = postId,
                data = mapOf(
                    "likedBy" to likedBy,
                    "likes" to likedBy.size
                )
            )
            
            !isLiked
        }
    }
    
    /**
     * Sucht Posts nach Stichwörtern
     */
    fun searchPosts(query: String): Flow<List<CommunityPost>> = flow {
        try {
            val user = appwriteService.account.get()
            
            // Suche in Titel und Inhalt
            val titleResults = databases.listDocuments(
                databaseId = AppwriteConfig.DATABASE_ID,
                collectionId = AppwriteConfig.COLLECTION_COMMUNITY_POSTS,
                queries = listOf(
                    Query.search("title", query),
                    Query.orderDesc("timestamp")
                )
            )
            
            val contentResults = databases.listDocuments(
                databaseId = AppwriteConfig.DATABASE_ID,
                collectionId = AppwriteConfig.COLLECTION_COMMUNITY_POSTS,
                queries = listOf(
                    Query.search("content", query),
                    Query.orderDesc("timestamp")
                )
            )
            
            // Kombiniere und dedupliziere Ergebnisse
            val allResults = (titleResults.documents + contentResults.documents)
                .distinctBy { it.id }
                .map { convertDocumentToPost(it, user.id) }
            
            emit(allResults)
            
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Fehler bei der Suche: ${e.message}", e)
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Holt beliebte Posts
     */
    fun getPopularPosts(timeframe: String = "week"): Flow<List<CommunityPost>> = flow {
        try {
            val user = appwriteService.account.get()
            
            // Berechne Zeitfenster
            val cutoffDate = when (timeframe) {
                "day" -> LocalDateTime.now().minusDays(1)
                "week" -> LocalDateTime.now().minusWeeks(1)
                "month" -> LocalDateTime.now().minusMonths(1)
                else -> LocalDateTime.now().minusWeeks(1)
            }
            
            val response = databases.listDocuments(
                databaseId = AppwriteConfig.DATABASE_ID,
                collectionId = AppwriteConfig.COLLECTION_COMMUNITY_POSTS,
                queries = listOf(
                    Query.greaterThan("timestamp", cutoffDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)),
                    Query.orderDesc("likes"),
                    Query.limit(20)
                )
            )
            
            val posts = response.documents.map { doc ->
                convertDocumentToPost(doc, user.id)
            }
            
            emit(posts)
            
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Fehler beim Laden beliebter Posts: ${e.message}", e)
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Holt Posts eines bestimmten Autors
     */
    fun getPostsByAuthor(authorId: String): Flow<List<CommunityPost>> = flow {
        try {
            val user = appwriteService.account.get()
            
            val response = databases.listDocuments(
                databaseId = AppwriteConfig.DATABASE_ID,
                collectionId = AppwriteConfig.COLLECTION_COMMUNITY_POSTS,
                queries = listOf(
                    Query.equal("authorId", authorId),
                    Query.orderDesc("timestamp")
                )
            )
            
            val posts = response.documents.map { doc ->
                convertDocumentToPost(doc, user.id)
            }
            
            emit(posts)
            
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Fehler beim Laden der Autor-Posts: ${e.message}", e)
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)
    
    // Hilfsfunktionen
    
    private fun convertDocumentToPost(doc: Document<Map<String, Any>>, currentUserId: String): CommunityPost {
        @Suppress("UNCHECKED_CAST")
        val likedBy = (doc.data["likedBy"] as? List<String>) ?: emptyList()
        
        val categoryName = doc.data["category"]?.toString() ?: "general"
        val postTypeValue = doc.data["postType"]?.toString() ?: "photo"
        val postType = try {
            com.example.snacktrack.data.model.PostType.fromDatabaseValue(postTypeValue)
        } catch (e: Exception) {
            com.example.snacktrack.data.model.PostType.PHOTO
        }
        
        return CommunityPost(
            id = doc.id,
            userId = doc.data["authorId"]?.toString() ?: doc.data["userId"]?.toString() ?: "",
            title = doc.data["title"]?.toString() ?: "",
            content = doc.data["content"].toString(),
            category = categoryName,
            postType = postType,
            imageUrls = (doc.data["imageIds"] as? List<*>)?.map { it.toString() } ?: emptyList(),
            hashtags = (doc.data["tags"] as? List<*>)?.map { it.toString() } ?: emptyList(),
            likesCount = (doc.data["likes"] as? Number)?.toInt() ?: 0,
            commentsCount = (doc.data["comments"] as? Number)?.toInt() ?: 0,
            createdAt = doc.data["timestamp"]?.toString(),
            isLikedByCurrentUser = currentUserId in likedBy,
            userProfile = CommunityProfile(
                id = doc.data["authorId"]?.toString() ?: "",
                userId = doc.data["authorId"]?.toString() ?: "",
                displayName = doc.data["authorName"]?.toString() ?: "Unknown",
                profileImageUrl = doc.data["authorAvatar"]?.toString()
            )
        )
    }
    
    private fun convertDocumentToComment(doc: Document<Map<String, Any>>, currentUserId: String): CommunityComment {
        @Suppress("UNCHECKED_CAST")
        val likedBy = (doc.data["likedBy"] as? List<String>) ?: emptyList()
        
        return CommunityComment(
            id = doc.id,
            postId = doc.data["postId"].toString(),
            userId = doc.data["authorId"]?.toString() ?: doc.data["userId"]?.toString() ?: "",
            content = doc.data["content"].toString(),
            createdAt = doc.data["timestamp"]?.toString(),
            userProfile = CommunityProfile(
                id = doc.data["authorId"]?.toString() ?: "",
                userId = doc.data["authorId"]?.toString() ?: "",
                displayName = doc.data["authorName"]?.toString() ?: "Unknown",
                profileImageUrl = null
            )
        )
    }
    
    private suspend fun incrementCommentCount(postId: String) {
        try {
            val post = databases.getDocument(
                databaseId = AppwriteConfig.DATABASE_ID,
                collectionId = AppwriteConfig.COLLECTION_COMMUNITY_POSTS,
                documentId = postId
            )
            
            val currentCount = (post.data["comments"] as? Number)?.toInt() ?: 0
            
            databases.updateDocument(
                databaseId = AppwriteConfig.DATABASE_ID,
                collectionId = AppwriteConfig.COLLECTION_COMMUNITY_POSTS,
                documentId = postId,
                data = mapOf("comments" to currentCount + 1)
            )
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Fehler beim Erhöhen der Kommentarzahl: ${e.message}", e)
        }
    }
}

// Data classes für Kommentare
data class CommunityComment(
    val id: String,
    val postId: String,
    val authorId: String,
    val authorName: String,
    val content: String,
    val timestamp: LocalDateTime,
    val likes: Int = 0,
    val isLiked: Boolean = false
)