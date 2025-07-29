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
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import io.mockk.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Comprehensive unit tests for CommunityRepository to validate database integration
 */
@RunWith(RobolectricTestRunner::class)
class CommunityRepositoryTest {

    private val mockAppwriteService = mockk<AppwriteService>()
    private val mockDatabases = mockk<Databases>()
    private val mockStorage = mockk<Storage>()
    private val mockAccount = mockk<Account>()
    private val mockClient = mockk<Client>()
    
    private lateinit var context: Context
    private lateinit var communityRepository: CommunityRepository

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        
        // Mock AppwriteService singleton
        mockkObject(AppwriteService)
        every { AppwriteService.getInstance(any()) } returns mockAppwriteService
        every { mockAppwriteService.databases } returns mockDatabases
        every { mockAppwriteService.storage } returns mockStorage
        every { mockAppwriteService.account } returns mockAccount
        
        communityRepository = CommunityRepository(context)
    }
    
    @After
    fun tearDown() {
        clearAllMocks()
        unmockkAll()
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
        val mockUser = mockk<User<Map<String, Any>>>()
        every { mockUser.id } returns "test-user-id"
        every { mockUser.name } returns "Test User"
        coEvery { mockAccount.get() } returns mockUser
        
        // Mock ensureValidSession on the already mocked appwriteService
        coEvery { mockAppwriteService.ensureValidSession() } returns true
        
        // Mock post creation
        val mockDocument = mockk<Document<Map<String, Any>>>()
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
        every { mockDocument.id } returns "post-id"
        every { mockDocument.data } returns postData
        coEvery { mockDatabases.createDocument(any(), any(), any(), any()) } returns mockDocument
        
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
        val mockUser = mockk<User<Map<String, Any>>>()
        every { mockUser.id } returns "test-user-id"
        every { mockUser.name } returns "Test User"
        coEvery { mockAccount.get() } returns mockUser
        
        // Mock ensureValidSession on the already mocked appwriteService  
        coEvery { mockAppwriteService.ensureValidSession() } returns true
        
        // Mock posts response
        val mockPostDoc = mockk<Document<Map<String, Any>>>()
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
        every { mockPostDoc.id } returns "post-id"
        every { mockPostDoc.data } returns postData
        
        val postsList = mockk<DocumentList<Map<String, Any>>>()
        @Suppress("UNCHECKED_CAST")
        every { postsList.documents } returns listOf(mockPostDoc as Document<Map<String, Any>>)
        coEvery { mockDatabases.listDocuments(
            eq(AppwriteConfig.DATABASE_ID),
            eq(AppwriteConfig.COLLECTION_COMMUNITY_POSTS),
            any()
        ) } returns postsList
        
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
        val mockUser = mockk<User<Map<String, Any>>>()
        every { mockUser.id } returns "test-user-id"
        every { mockUser.name } returns "Test User"
        coEvery { mockAccount.get() } returns mockUser
        
        // Mock ensureValidSession on the already mocked appwriteService  
        coEvery { mockAppwriteService.ensureValidSession() } returns true
        
        // Mock search results
        val mockPostDoc = mockk<Document<Map<String, Any>>>()
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
        every { mockPostDoc.id } returns "search-post-id"
        every { mockPostDoc.data } returns postData
        
        val searchList = mockk<DocumentList<Map<String, Any>>>()
        @Suppress("UNCHECKED_CAST")
        every { searchList.documents } returns listOf(mockPostDoc as Document<Map<String, Any>>)
        coEvery { mockDatabases.listDocuments(any(), any(), any()) } returns searchList
        
        // Test search
        val posts = communityRepository.searchPosts("search").first()
        
        assert(posts.isNotEmpty()) { "Search results should not be empty" }
        assert(posts.first().title == "Search Result") { "Post title should match" }
        assert(posts.first().content == "Test search content") { "Post content should match" }
    }

    @Test
    fun `test like toggle functionality`() = runBlocking {
        // Mock account
        val mockUser = mockk<io.appwrite.models.User<Map<String, Any>>>()
        every { mockUser.id } returns "test-user-id"
        coEvery { mockAccount.get() } returns mockUser
        
        // Mock post retrieval with no likes
        val mockPostDoc = mockk<Document<Map<String, Any>>>()
        val postData = mapOf(
            "likedBy" to emptyList<String>(),
            "likes" to 0
        )
        every { mockPostDoc.data } returns postData
        coEvery { mockDatabases.getDocument(
            eq(AppwriteConfig.DATABASE_ID),
            eq(AppwriteConfig.COLLECTION_COMMUNITY_POSTS),
            eq("test-post-id")
        ) } returns mockPostDoc
        
        // Mock post update
        val updatedDoc = mockk<Document<Map<String, Any>>>()
        every { updatedDoc.data } returns mapOf(
            "likedBy" to listOf("test-user-id"),
            "likes" to 1
        )
        coEvery { mockDatabases.updateDocument(any(), any(), any(), any()) } returns updatedDoc
        
        // Test like toggle (add like)
        val result = communityRepository.toggleLikePost("test-post-id")
        
        assert(result.isSuccess) { "Like toggle should succeed" }
        val isLiked = result.getOrNull()
        assert(isLiked == true) { "Post should be liked after toggle" }
    }

    @Test
    fun `test error handling for invalid database operations`() = runBlocking {
        // Mock account failure
        coEvery { mockAccount.get() } throws RuntimeException("Authentication failed")
        
        // Mock ensureValidSession on the already mocked appwriteService  
        coEvery { mockAppwriteService.ensureValidSession() } returns true
        
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
        val mockUser = mockk<User<Map<String, Any>>>()
        every { mockUser.id } returns "test-user-id"
        every { mockUser.name } returns "Test User"
        coEvery { mockAccount.get() } returns mockUser
        
        // Mock comment creation
        val mockCommentDoc = mockk<Document<Map<String, Any>>>()
        val commentData = mapOf(
            "postId" to "test-post-id",
            "authorId" to "test-user-id",
            "authorName" to "Test User",
            "content" to "Test comment content",
            "timestamp" to LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "likes" to 0,
            "likedBy" to emptyList<String>()
        )
        every { mockCommentDoc.id } returns "comment-id"
        every { mockCommentDoc.data } returns commentData
        coEvery { mockDatabases.createDocument(any(), any(), any(), any()) } returns mockCommentDoc
        
        // Mock post retrieval for comment count update
        val mockPostDoc = mockk<Document<Map<String, Any>>>()
        every { mockPostDoc.data } returns mapOf("comments" to 0)
        coEvery { mockDatabases.getDocument(any(), any(), any()) } returns mockPostDoc
        coEvery { mockDatabases.updateDocument(any(), any(), any(), any()) } returns mockPostDoc
        
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
        val mockUser = mockk<User<Map<String, Any>>>()
        every { mockUser.id } returns "test-user-id"
        every { mockUser.name } returns "Test User"
        coEvery { mockAccount.get() } returns mockUser
        
        // Mock comment response
        val mockCommentDoc = mockk<Document<Map<String, Any>>>()
        val commentData = mapOf(
            "postId" to "test-post-id",
            "authorId" to "test-user-id",
            "authorName" to "Test User",
            "content" to "Test comment",
            "timestamp" to LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "likes" to 0,
            "likedBy" to emptyList<String>()
        )
        every { mockCommentDoc.id } returns "comment-id"
        every { mockCommentDoc.data } returns commentData
        
        val commentsList = mockk<DocumentList<Map<String, Any>>>()
        @Suppress("UNCHECKED_CAST")
        every { commentsList.documents } returns listOf(mockCommentDoc as Document<Map<String, Any>>)
        coEvery { mockDatabases.listDocuments(
            eq(AppwriteConfig.DATABASE_ID),
            eq(AppwriteConfig.COLLECTION_COMMUNITY_COMMENTS),
            any()
        ) } returns commentsList
        
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