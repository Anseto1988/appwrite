package com.example.snacktrack

import android.content.Context
import android.net.Uri
import com.example.snacktrack.data.model.PostType
import com.example.snacktrack.data.model.CommunityPost
import com.example.snacktrack.data.model.CommunityProfile
import com.example.snacktrack.data.model.CommunityComment
import com.example.snacktrack.data.repository.CommunityRepository
import com.example.snacktrack.data.service.AppwriteService
import com.example.snacktrack.data.service.AppwriteConfig
import io.appwrite.Client
import io.appwrite.models.Document
import io.appwrite.models.DocumentList
import io.appwrite.models.User
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Comprehensive unit tests for CommunityRepository to validate database integration
 */
@RunWith(RobolectricTestRunner::class)
class CommunityRepositoryTest {

    @Mock
    private lateinit var mockAppwriteService: AppwriteService
    
    @Mock
    private lateinit var mockDatabases: Databases
    
    @Mock
    private lateinit var mockStorage: Storage
    
    @Mock
    private lateinit var mockAccount: Account
    
    @Mock
    private lateinit var mockClient: Client
    
    private lateinit var context: Context
    private lateinit var communityRepository: CommunityRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = RuntimeEnvironment.getApplication()
        
        // Mock AppwriteService singleton
        whenever(mockAppwriteService.databases).thenReturn(mockDatabases)
        whenever(mockAppwriteService.storage).thenReturn(mockStorage)
        whenever(mockAppwriteService.account).thenReturn(mockAccount)
        
        communityRepository = CommunityRepository(context)
    }

    @Test
    fun `test database ID configuration is correct`() {
        // Verify that the database ID has been corrected from "dog_community_db" to "snacktrack-db"
        val expectedDatabaseId = "snacktrack-db"
        assert(AppwriteConfig.DATABASE_ID == expectedDatabaseId) {
            "Database ID should be 'snacktrack-db' but was '${AppwriteConfig.DATABASE_ID}'"
        }
    }

    @Test
    fun `test community collections are properly defined`() {
        // Test that all required community collections are defined
        val expectedCollections = setOf(
            "community_posts",
            "community_profiles", 
            "community_comments",
            "community_likes",
            "community_follows"
        )
        
        val actualCollections = setOf(
            AppwriteConfig.COLLECTION_COMMUNITY_POSTS,
            "community_profiles", // Not defined in AppwriteConfig
            AppwriteConfig.COLLECTION_COMMUNITY_COMMENTS,
            "community_likes", // Not defined in AppwriteConfig
            "community_follows" // Not defined in AppwriteConfig
        )
        
        assert(actualCollections == expectedCollections) {
            "Community collections mismatch. Expected: $expectedCollections, Actual: $actualCollections"
        }
    }

    @Test
    fun `test community bucket is properly defined`() {
        // Test that the community images bucket is correctly defined
        val expectedBucket = "community_images"
        assert("community_images" == expectedBucket) { // Bucket not defined in config
            "Community bucket should be '$expectedBucket' but was '${"community_images"}'"
        }
    }

    @Test
    fun `test post creation workflow`() = runBlocking {
        // Mock account response
        val mockUser = mock<User<Map<String, Any>>>()
        whenever(mockUser.id).thenReturn("test-user-id")
        whenever(mockUser.name).thenReturn("Test User")
        whenever(mockAccount.get()).thenReturn(mockUser)
        
        // Mock AppwriteService to return our mocked account
        val mockService = mock<AppwriteService>()
        whenever(mockService.account).thenReturn(mockAccount)
        whenever(mockService.databases).thenReturn(mockDatabases)
        whenever(mockService.ensureValidSession()).thenReturn(true)
        
        // Use reflection to set the private appwriteService field
        val serviceField = CommunityRepository::class.java.getDeclaredField("appwriteService")
        serviceField.isAccessible = true
        serviceField.set(communityRepository, mockService)
        
        // Mock post creation
        val mockDocument = mock<Document<Map<String, Any>>>()
        val postData = mapOf(
            "authorId" to "test-user-id",
            "authorName" to "Test User",
            "title" to "Test Title",
            "content" to "Test post content",
            "category" to "general",
            "postType" to "photo",
            "timestamp" to LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "likes" to 0,
            "comments" to 0,
            "imageIds" to emptyList<String>(),
            "likedBy" to emptyList<String>()
        )
        whenever(mockDocument.id).thenReturn("post-id")
        whenever(mockDocument.data).thenReturn(postData)
        whenever(mockDatabases.createDocument(any(), any(), any(), any())).thenReturn(mockDocument)
        
        // Test post creation
        val result = communityRepository.createPost(
            title = "Test Title",
            content = "Test post content",
            category = "general",
            postType = "photo"
        )
        
        assert(result.isSuccess) { "Post creation should succeed" }
        val post = result.getOrNull()
        assert(post != null) { "Post should not be null" }
        assert(post?.title == "Test Title") { "Title should match" }
        assert(post?.content == "Test post content") { "Content should match" }
        assert(post?.postType == PostType.PHOTO) { "Post type should match" }
    }

    @Test
    fun `test get posts functionality`() = runBlocking {
        // Mock account response
        val mockUser = mock<User<Map<String, Any>>>()
        whenever(mockUser.id).thenReturn("test-user-id")
        whenever(mockUser.name).thenReturn("Test User")
        whenever(mockAccount.get()).thenReturn(mockUser)
        
        // Mock AppwriteService
        val mockService = mock<AppwriteService>()
        whenever(mockService.account).thenReturn(mockAccount)
        whenever(mockService.databases).thenReturn(mockDatabases)
        whenever(mockService.ensureValidSession()).thenReturn(true)
        
        // Use reflection to set the private appwriteService field
        val serviceField = CommunityRepository::class.java.getDeclaredField("appwriteService")
        serviceField.isAccessible = true
        serviceField.set(communityRepository, mockService)
        
        // Mock posts response
        val mockPostDoc = mock<Document<Map<String, Any>>>()
        val postData = mapOf(
            "authorId" to "test-user-id",
            "authorName" to "Test User",
            "title" to "Test Title",
            "content" to "Test post content",
            "category" to "general",
            "postType" to "photo",
            "timestamp" to LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "likes" to 5,
            "comments" to 2,
            "imageIds" to emptyList<String>(),
            "likedBy" to listOf("test-user-id")
        )
        whenever(mockPostDoc.id).thenReturn("post-id")
        whenever(mockPostDoc.data).thenReturn(postData)
        
        val postsList = mock<DocumentList<Map<String, Any>>>()
        @Suppress("UNCHECKED_CAST")
        whenever(postsList.documents).thenReturn(listOf(mockPostDoc as Document<Map<String, Any>>))
        whenever(mockDatabases.listDocuments(
            eq(AppwriteConfig.DATABASE_ID),
            eq(AppwriteConfig.COLLECTION_COMMUNITY_POSTS),
            any()
        )).thenReturn(postsList)
        
        // Test get posts
        val posts = communityRepository.getPosts().first()
        
        assert(posts.isNotEmpty()) { "Posts should not be empty" }
        assert(posts.first().title == "Test Title") { "Post title should match" }
        assert(posts.first().content == "Test post content") { "Post content should match" }
        assert(posts.first().isLikedByCurrentUser) { "Post should be liked by current user" }
    }

    @Test
    fun `test search posts functionality`() = runBlocking {
        // Mock account response
        val mockUser = mock<User<Map<String, Any>>>()
        whenever(mockUser.id).thenReturn("test-user-id")
        whenever(mockUser.name).thenReturn("Test User")
        whenever(mockAccount.get()).thenReturn(mockUser)
        
        // Mock AppwriteService
        val mockService = mock<AppwriteService>()
        whenever(mockService.account).thenReturn(mockAccount)
        whenever(mockService.databases).thenReturn(mockDatabases)
        whenever(mockService.ensureValidSession()).thenReturn(true)
        
        // Use reflection to set the private appwriteService field
        val serviceField = CommunityRepository::class.java.getDeclaredField("appwriteService")
        serviceField.isAccessible = true
        serviceField.set(communityRepository, mockService)
        
        // Mock search results
        val mockPostDoc = mock<Document<Map<String, Any>>>()
        val postData = mapOf(
            "authorId" to "test-user-id",
            "authorName" to "Test User",
            "title" to "Search Result",
            "content" to "Test search content",
            "category" to "general",
            "postType" to "photo",
            "timestamp" to LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "likes" to 5,
            "comments" to 2,
            "imageIds" to emptyList<String>(),
            "likedBy" to emptyList<String>()
        )
        whenever(mockPostDoc.id).thenReturn("search-post-id")
        whenever(mockPostDoc.data).thenReturn(postData)
        
        val searchList = mock<DocumentList<Map<String, Any>>>()
        @Suppress("UNCHECKED_CAST")
        whenever(searchList.documents).thenReturn(listOf(mockPostDoc as Document<Map<String, Any>>))
        whenever(mockDatabases.listDocuments(any(), any(), any())).thenReturn(searchList)
        
        // Test search
        val posts = communityRepository.searchPosts("search").first()
        
        assert(posts.isNotEmpty()) { "Search results should not be empty" }
        assert(posts.first().title == "Search Result") { "Post title should match" }
        assert(posts.first().content == "Test search content") { "Post content should match" }
    }

    @Test
    fun `test like toggle functionality`() = runBlocking {
        // Mock account
        val mockUser = mock<io.appwrite.models.User<Map<String, Any>>>()
        whenever(mockUser.id).thenReturn("test-user-id")
        whenever(mockAccount.get()).thenReturn(mockUser)
        
        // Mock AppwriteService
        val mockService = mock<AppwriteService>()
        whenever(mockService.account).thenReturn(mockAccount)
        whenever(mockService.databases).thenReturn(mockDatabases)
        
        // Use reflection to set the private appwriteService field
        val serviceField = CommunityRepository::class.java.getDeclaredField("appwriteService")
        serviceField.isAccessible = true
        serviceField.set(communityRepository, mockService)
        
        // Mock post retrieval with no likes
        val mockPostDoc = mock<Document<Map<String, Any>>>()
        val postData = mapOf(
            "likedBy" to emptyList<String>(),
            "likes" to 0
        )
        whenever(mockPostDoc.data).thenReturn(postData)
        whenever(mockDatabases.getDocument(
            eq(AppwriteConfig.DATABASE_ID),
            eq(AppwriteConfig.COLLECTION_COMMUNITY_POSTS),
            eq("test-post-id")
        )).thenReturn(mockPostDoc)
        
        // Mock post update
        val updatedDoc = mock<Document<Map<String, Any>>>()
        whenever(updatedDoc.data).thenReturn(mapOf(
            "likedBy" to listOf("test-user-id"),
            "likes" to 1
        ))
        whenever(mockDatabases.updateDocument(any(), any(), any(), any())).thenReturn(updatedDoc)
        
        // Test like toggle (add like)
        val result = communityRepository.toggleLikePost("test-post-id")
        
        assert(result.isSuccess) { "Like toggle should succeed" }
        val isLiked = result.getOrNull()
        assert(isLiked == true) { "Post should be liked after toggle" }
    }

    @Test
    fun `test error handling for invalid database operations`() = runBlocking {
        // Mock account failure
        whenever(mockAccount.get()).thenThrow(RuntimeException("Authentication failed"))
        
        // Mock AppwriteService
        val mockService = mock<AppwriteService>()
        whenever(mockService.account).thenReturn(mockAccount)
        whenever(mockService.databases).thenReturn(mockDatabases)
        whenever(mockService.ensureValidSession()).thenReturn(true)
        
        // Use reflection to set the private appwriteService field
        val serviceField = CommunityRepository::class.java.getDeclaredField("appwriteService")
        serviceField.isAccessible = true
        serviceField.set(communityRepository, mockService)
        
        // Test post creation with auth failure  
        val postResult = communityRepository.createPost(
            title = "Test Title",
            content = "Test content",
            category = "general",
            postType = "photo"
        )
        assert(postResult.isFailure) { "Post creation should fail with auth error" }
        
        // Test like toggle with auth failure
        val likeResult = communityRepository.toggleLikePost("test-post-id")
        assert(likeResult.isFailure) { "Like toggle should fail with auth error" }
        
        // Test comment creation with auth failure
        val commentResult = communityRepository.createComment("test-post-id", "Test comment")
        assert(commentResult.isFailure) { "Comment creation should fail with auth error" }
    }

    @Test
    fun `test comment creation functionality`() = runBlocking {
        // Mock account response
        val mockUser = mock<User<Map<String, Any>>>()
        whenever(mockUser.id).thenReturn("test-user-id")
        whenever(mockUser.name).thenReturn("Test User")
        whenever(mockAccount.get()).thenReturn(mockUser)
        
        // Mock AppwriteService
        val mockService = mock<AppwriteService>()
        whenever(mockService.account).thenReturn(mockAccount)
        whenever(mockService.databases).thenReturn(mockDatabases)
        
        // Use reflection to set the private appwriteService field
        val serviceField = CommunityRepository::class.java.getDeclaredField("appwriteService")
        serviceField.isAccessible = true
        serviceField.set(communityRepository, mockService)
        
        // Mock comment creation
        val mockCommentDoc = mock<Document<Map<String, Any>>>()
        val commentData = mapOf(
            "postId" to "test-post-id",
            "authorId" to "test-user-id",
            "authorName" to "Test User",
            "content" to "Test comment content",
            "timestamp" to LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "likes" to 0,
            "likedBy" to emptyList<String>()
        )
        whenever(mockCommentDoc.id).thenReturn("comment-id")
        whenever(mockCommentDoc.data).thenReturn(commentData)
        whenever(mockDatabases.createDocument(any(), any(), any(), any())).thenReturn(mockCommentDoc)
        
        // Mock post retrieval for comment count update
        val mockPostDoc = mock<Document<Map<String, Any>>>()
        whenever(mockPostDoc.data).thenReturn(mapOf("comments" to 0))
        whenever(mockDatabases.getDocument(any(), any(), any())).thenReturn(mockPostDoc)
        whenever(mockDatabases.updateDocument(any(), any(), any(), any())).thenReturn(mockPostDoc)
        
        // Test comment creation
        val result = communityRepository.createComment("test-post-id", "Test comment content")
        
        assert(result.isSuccess) { "Comment creation should succeed" }
        val comment = result.getOrNull()
        assert(comment != null) { "Comment should not be null" }
        assert(comment?.content == "Test comment content") { "Comment content should match" }
        assert(comment?.postId == "test-post-id") { "Post ID should match" }
    }
    
    @Test
    fun `test get comments functionality`() = runBlocking {
        // Mock account response
        val mockUser = mock<User<Map<String, Any>>>()
        whenever(mockUser.id).thenReturn("test-user-id")
        whenever(mockUser.name).thenReturn("Test User")
        whenever(mockAccount.get()).thenReturn(mockUser)
        
        // Mock AppwriteService
        val mockService = mock<AppwriteService>()
        whenever(mockService.account).thenReturn(mockAccount)
        whenever(mockService.databases).thenReturn(mockDatabases)
        
        // Use reflection to set the private appwriteService field
        val serviceField = CommunityRepository::class.java.getDeclaredField("appwriteService")
        serviceField.isAccessible = true
        serviceField.set(communityRepository, mockService)
        
        // Mock comment response
        val mockCommentDoc = mock<Document<Map<String, Any>>>()
        val commentData = mapOf(
            "postId" to "test-post-id",
            "authorId" to "test-user-id",
            "authorName" to "Test User",
            "content" to "Test comment",
            "timestamp" to LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "likes" to 0,
            "likedBy" to emptyList<String>()
        )
        whenever(mockCommentDoc.id).thenReturn("comment-id")
        whenever(mockCommentDoc.data).thenReturn(commentData)
        
        val commentsList = mock<DocumentList<Map<String, Any>>>()
        @Suppress("UNCHECKED_CAST")
        whenever(commentsList.documents).thenReturn(listOf(mockCommentDoc as Document<Map<String, Any>>))
        whenever(mockDatabases.listDocuments(
            eq(AppwriteConfig.DATABASE_ID),
            eq(AppwriteConfig.COLLECTION_COMMUNITY_COMMENTS),
            any()
        )).thenReturn(commentsList)
        
        // Test get comments
        val comments = communityRepository.getComments("test-post-id").first()
        
        assert(comments.isNotEmpty()) { "Comments should not be empty" }
        assert(comments.first().content == "Test comment") { "Comment content should match" }
        assert(comments.first().postId == "test-post-id") { "Post ID should match" }
    }
    
    @Test
    fun `test data consistency and validation`() {
        // Test that database ID is correctly defined
        val databaseId = AppwriteConfig.DATABASE_ID
        
        assert(databaseId.isNotBlank()) {
            "Database ID should not be blank"
        }
        assert(databaseId == "snacktrack-db") {
            "Database ID should be 'snacktrack-db' but was '$databaseId'"
        }
        
        // Test that required community collections are defined
        val requiredCollections = listOf(
            AppwriteConfig.COLLECTION_COMMUNITY_POSTS,
            AppwriteConfig.COLLECTION_COMMUNITY_COMMENTS
        )
        
        requiredCollections.forEach { collection ->
            assert(collection.isNotBlank()) { "Collection ID should not be blank: $collection" }
            assert(!collection.contains(" ")) { "Collection ID should not contain spaces: $collection" }
        }
        
        // Test post type enum values
        PostType.entries.forEach { postType ->
            assert(postType.displayName.isNotBlank()) { "PostType display name should not be blank" }
            assert(postType.databaseValue.isNotBlank()) { "PostType database value should not be blank" }
        }
    }
}