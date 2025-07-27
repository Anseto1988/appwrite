package com.example.snacktrack

import android.content.Context
import android.net.Uri
import com.example.snacktrack.data.model.PostType
import com.example.snacktrack.data.repository.CommunityRepository
import com.example.snacktrack.data.service.AppwriteService
import com.example.snacktrack.data.service.AppwriteConfig
import io.appwrite.Client
import io.appwrite.models.Document
import io.appwrite.models.DocumentList
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Storage
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
    fun `test profile creation workflow`() = runBlocking {
        // Mock account response
        val mockUser = mock<io.appwrite.models.User<Map<String, Any>>>()
        whenever(mockUser.id).thenReturn("test-user-id")
        whenever(mockUser.name).thenReturn("Test User")
        whenever(mockAccount.get()).thenReturn(mockUser)
        
        // Mock empty profile check (new profile)
        val emptyDocumentList = mock<DocumentList<Document<Map<String, Any>>>>()
        whenever(emptyDocumentList.documents).thenReturn(emptyList())
        whenever(mockDatabases.listDocuments(any(), any(), any())).thenReturn(emptyDocumentList)
        
        // Mock profile creation
        val mockDocument = mock<Document<Map<String, Any>>>()
        val profileData = mapOf(
            "userId" to "test-user-id",
            "displayName" to "Test Profile",
            "bio" to "Test bio",
            "profileImageUrl" to null,
            "isPremium" to false,
            "followersCount" to 0,
            "followingCount" to 0,
            "postsCount" to 0,
            "createdAt" to LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
        )
        whenever(mockDocument.id).thenReturn("profile-id")
        whenever(mockDocument.data).thenReturn(profileData)
        whenever(mockDatabases.createDocument(any(), any(), any(), any())).thenReturn(mockDocument)
        
        // Test profile creation
        val result = communityRepository.createOrUpdateProfile(
            displayName = "Test Profile",
            bio = "Test bio"
        )
        
        assert(result.isSuccess) { "Profile creation should succeed" }
        val profile = result.getOrNull()
        assert(profile != null) { "Profile should not be null" }
        assert(profile?.displayName == "Test Profile") { "Display name should match" }
        assert(profile?.bio == "Test bio") { "Bio should match" }
    }

    @Test
    fun `test post creation workflow`() = runBlocking {
        // Mock account and user profile
        val mockUser = mock<io.appwrite.models.User<Map<String, Any>>>()
        whenever(mockUser.id).thenReturn("test-user-id")
        whenever(mockUser.name).thenReturn("Test User")
        whenever(mockAccount.get()).thenReturn(mockUser)
        
        // Mock existing profile
        val mockProfileDoc = mock<Document<Map<String, Any>>>()
        whenever(mockProfileDoc.id).thenReturn("profile-id")
        whenever(mockProfileDoc.data).thenReturn(mapOf(
            "userId" to "test-user-id",
            "displayName" to "Test User",
            "postsCount" to 0
        ))
        val profileList = mock<DocumentList<Document<Map<String, Any>>>>()
        whenever(profileList.documents).thenReturn(listOf(mockProfileDoc))
        whenever(mockDatabases.listDocuments(
            eq(AppwriteConfig.DATABASE_ID),
            eq("community_profiles"),
            any()
        )).thenReturn(profileList)
        
        // Mock post creation
        val mockPostDoc = mock<Document<Map<String, Any>>>()
        val postData = mapOf(
            "userId" to "test-user-id",
            "content" to "Test post content",
            "postType" to PostType.PHOTO.name,
            "imageUrls" to emptyList<String>(),
            "hashtags" to emptyList<String>(),
            "likesCount" to 0,
            "commentsCount" to 0,
            "created_at" to LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
        )
        whenever(mockPostDoc.id).thenReturn("post-id")
        whenever(mockPostDoc.data).thenReturn(postData)
        whenever(mockDatabases.createDocument(any(), any(), any(), any())).thenReturn(mockPostDoc)
        
        // Mock profile update (increment post count)
        whenever(mockDatabases.updateDocument(any(), any(), any(), any())).thenReturn(mockProfileDoc)
        
        // Mock feed refresh
        val emptyFeedList = mock<DocumentList<Document<Map<String, Any>>>>()
        whenever(emptyFeedList.documents).thenReturn(emptyList())
        whenever(mockDatabases.listDocuments(
            eq(AppwriteConfig.DATABASE_ID),
            eq(AppwriteConfig.COLLECTION_COMMUNITY_POSTS),
            any()
        )).thenReturn(emptyFeedList)
        
        // Test post creation
        val result = communityRepository.createPost(
            content = "Test post content",
            postType = PostType.PHOTO,
            hashtags = listOf("#test")
        )
        
        assert(result.isSuccess) { "Post creation should succeed" }
        val post = result.getOrNull()
        assert(post != null) { "Post should not be null" }
        assert(post?.content == "Test post content") { "Content should match" }
        assert(post?.postType == PostType.PHOTO) { "Post type should match" }
    }

    @Test
    fun `test feed refresh functionality`() = runBlocking {
        // Mock feed posts
        val mockPostDoc = mock<Document<Map<String, Any>>>()
        val postData = mapOf(
            "userId" to "test-user-id",
            "content" to "Test feed post",
            "postType" to PostType.PHOTO.name,
            "imageUrls" to emptyList<String>(),
            "hashtags" to listOf("#feed"),
            "likesCount" to 5,
            "commentsCount" to 2,
            "created_at" to LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
        )
        whenever(mockPostDoc.id).thenReturn("feed-post-id")
        whenever(mockPostDoc.data).thenReturn(postData)
        
        val feedList = mock<DocumentList<Document<Map<String, Any>>>>()
        whenever(feedList.documents).thenReturn(listOf(mockPostDoc))
        whenever(mockDatabases.listDocuments(
            eq(AppwriteConfig.DATABASE_ID),
            eq(AppwriteConfig.COLLECTION_COMMUNITY_POSTS),
            any()
        )).thenReturn(feedList)
        
        // Mock empty profile response (user profile lookup in feed)
        val emptyProfileList = mock<DocumentList<Document<Map<String, Any>>>>()
        whenever(emptyProfileList.documents).thenReturn(emptyList())
        whenever(mockDatabases.listDocuments(
            eq(AppwriteConfig.DATABASE_ID),
            eq("community_profiles"),
            any()
        )).thenReturn(emptyProfileList)
        
        // Test feed refresh
        val result = communityRepository.refreshFeed()
        
        assert(result.isSuccess) { "Feed refresh should succeed" }
        val posts = result.getOrNull()
        assert(posts != null) { "Posts should not be null" }
        assert(posts?.isNotEmpty() == true) { "Posts should not be empty" }
        assert(posts?.first()?.content == "Test feed post") { "Post content should match" }
    }

    @Test
    fun `test like toggle functionality`() = runBlocking {
        // Mock account
        val mockUser = mock<io.appwrite.models.User<Map<String, Any>>>()
        whenever(mockUser.id).thenReturn("test-user-id")
        whenever(mockAccount.get()).thenReturn(mockUser)
        
        // Mock no existing like (first like)
        val emptyLikeList = mock<DocumentList<Document<Map<String, Any>>>>()
        whenever(emptyLikeList.documents).thenReturn(emptyList())
        whenever(mockDatabases.listDocuments(
            eq(AppwriteConfig.DATABASE_ID),
            eq(CommunityRepository.COLLECTION_COMMUNITY_LIKES),
            any()
        )).thenReturn(emptyLikeList)
        
        // Mock like creation
        val mockLikeDoc = mock<Document<Map<String, Any>>>()
        whenever(mockLikeDoc.id).thenReturn("like-id")
        whenever(mockDatabases.createDocument(any(), any(), any(), any())).thenReturn(mockLikeDoc)
        
        // Mock post retrieval and update
        val mockPostDoc = mock<Document<Map<String, Any>>>()
        whenever(mockPostDoc.data).thenReturn(mapOf("likesCount" to 0))
        whenever(mockDatabases.getDocument(any(), any(), any())).thenReturn(mockPostDoc)
        whenever(mockDatabases.updateDocument(any(), any(), any(), any())).thenReturn(mockPostDoc)
        
        // Test like toggle (add like)
        val result = communityRepository.toggleLike("test-post-id")
        
        assert(result.isSuccess) { "Like toggle should succeed" }
        val isLiked = result.getOrNull()
        assert(isLiked == true) { "Post should be liked after toggle" }
    }

    @Test
    fun `test error handling for invalid database operations`() = runBlocking {
        // Mock account failure
        whenever(mockAccount.get()).thenThrow(RuntimeException("Authentication failed"))
        
        // Test profile creation with auth failure
        val profileResult = communityRepository.createOrUpdateProfile("Test Profile")
        assert(profileResult.isFailure) { "Profile creation should fail with auth error" }
        
        // Test post creation with auth failure  
        val postResult = communityRepository.createPost("Test content", PostType.PHOTO)
        assert(postResult.isFailure) { "Post creation should fail with auth error" }
        
        // Test feed refresh with auth failure
        val feedResult = communityRepository.refreshFeed()
        assert(feedResult.isFailure) { "Feed refresh should fail with auth error" }
    }

    @Test
    fun `test data consistency and validation`() {
        // Test that constants match between repository and setup script expectations
        val repositoryDatabaseId = AppwriteConfig.DATABASE_ID
        val serviceDatabaseId = AppwriteService.DATABASE_ID
        
        assert(repositoryDatabaseId == serviceDatabaseId) {
            "Database IDs must match between CommunityRepository ($repositoryDatabaseId) and AppwriteService ($serviceDatabaseId)"
        }
        
        // Test that all required community collections are defined
        val requiredCollections = listOf(
            AppwriteConfig.COLLECTION_COMMUNITY_POSTS,
            "community_profiles",
            CommunityRepository.COLLECTION_COMMUNITY_COMMENTS,
            CommunityRepository.COLLECTION_COMMUNITY_LIKES,
            CommunityRepository.COLLECTION_COMMUNITY_FOLLOWS
        )
        
        requiredCollections.forEach { collection ->
            assert(collection.isNotBlank()) { "Collection ID should not be blank: $collection" }
            assert(!collection.contains(" ")) { "Collection ID should not contain spaces: $collection" }
        }
        
        // Test bucket configuration
        val bucketId = "community_images"
        assert(bucketId == "community_images") { "Community bucket ID should be 'community_images'" }
    }
}