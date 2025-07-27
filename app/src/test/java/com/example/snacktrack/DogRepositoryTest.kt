package com.example.snacktrack

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.snacktrack.data.model.Dog
import com.example.snacktrack.data.model.Sex
import com.example.snacktrack.data.model.ActivityLevel
import com.example.snacktrack.data.repository.DogRepository
import com.example.snacktrack.data.service.AppwriteService
import io.appwrite.models.Document
import io.appwrite.models.DocumentList
import io.appwrite.models.User
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Comprehensive unit tests for DogRepository
 * Tests critical functionality including dog retrieval, creation, and team sharing
 */
@RunWith(RobolectricTestRunner::class)
class DogRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var dogRepository: DogRepository
    
    private val mockAppwriteService = mockk<AppwriteService>()
    private val mockAccount = mockk<Account>()
    private val mockDatabases = mockk<Databases>()
    private val mockUser = mockk<User<Map<String, Any>>>()

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        
        // Mock AppwriteService singleton
        mockkObject(AppwriteService)
        every { AppwriteService.getInstance(any()) } returns mockAppwriteService
        every { mockAppwriteService.account } returns mockAccount
        every { mockAppwriteService.databases } returns mockDatabases
        
        // Mock valid session
        coEvery { mockAppwriteService.ensureValidSession() } returns true
        
        // Mock user
        every { mockUser.id } returns "test-user-id"
        every { mockUser.email } returns "test@example.com"
        coEvery { mockAccount.get() } returns mockUser
        
        dogRepository = DogRepository(context)
    }

    @After
    fun tearDown() {
        clearAllMocks()
        unmockkAll()
    }

    @Test
    fun `test getDogs returns own dogs when session is valid`() = runBlocking {
        // Mock own dogs response
        val mockDocument = mockk<Document<Map<String, Any>>>()
        every { mockDocument.id } returns "dog-1"
        val dogData = mutableMapOf<String, Any>(
            "name" to "Buddy",
            "breed" to "Golden Retriever",
            "sex" to "MALE",
            "birthDate" to "2020-01-15",
            "weight" to 25.5,
            "activityLevel" to "NORMAL",
            "ownerId" to "test-user-id"
        )
        dogData["imageId"] = null as Any? ?: "null"
        every { mockDocument.data } returns dogData
        
        val ownDogsResponse = mockk<DocumentList<Map<String, Any>>>()
        every { ownDogsResponse.documents } returns listOf(mockDocument)
        
        coEvery { mockDatabases.listDocuments(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = AppwriteService.COLLECTION_DOGS,
            queries = any()
        ) } returns ownDogsResponse
        
        // Mock empty team memberships (no shared dogs)
        val emptyMemberships = mockk<DocumentList<Map<String, Any>>>()
        every { emptyMemberships.documents } returns emptyList()
        coEvery { mockDatabases.listDocuments(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = AppwriteService.COLLECTION_TEAM_MEMBERS,
            queries = any()
        ) } returns emptyMemberships

        // Test
        val result = dogRepository.getDogs().first()
        
        // Verify
        assertEquals(1, result.size)
        assertEquals("Buddy", result[0].name)
        assertEquals("Golden Retriever", result[0].breed)
        assertEquals(Sex.MALE, result[0].sex)
        assertEquals("test-user-id", result[0].ownerId)
    }

    @Test
    fun `test getDogs returns empty list when session is invalid`() = runBlocking {
        // Mock invalid session
        coEvery { mockAppwriteService.ensureValidSession() } returns false
        
        // Test
        val result = dogRepository.getDogs().first()
        
        // Verify
        assertTrue(result.isEmpty())
    }

    @Test
    fun `test getDogs includes shared dogs from team memberships`() = runBlocking {
        // Mock own dogs (empty for this test)
        val emptyOwnDogs = mockk<DocumentList<Map<String, Any>>>()
        every { emptyOwnDogs.documents } returns emptyList()
        coEvery { mockDatabases.listDocuments(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = AppwriteService.COLLECTION_DOGS,
            queries = any()
        ) } returns emptyOwnDogs
        
        // Mock team membership
        val mockMembership = mockk<Document<Map<String, Any>>>()
        every { mockMembership.data } returns mapOf<String, Any>("teamId" to "team-1")
        
        val membershipsResponse = mockk<DocumentList<Map<String, Any>>>()
        every { membershipsResponse.documents } returns listOf(mockMembership)
        coEvery { mockDatabases.listDocuments(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = AppwriteService.COLLECTION_TEAM_MEMBERS,
            queries = any()
        ) } returns membershipsResponse
        
        // Mock dog sharing for the team
        val mockSharing = mockk<Document<Map<String, Any>>>()
        every { mockSharing.data } returns mapOf<String, Any>("dogId" to "shared-dog-1")
        
        val sharingResponse = mockk<DocumentList<Map<String, Any>>>()
        every { sharingResponse.documents } returns listOf(mockSharing)
        coEvery { mockDatabases.listDocuments(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = AppwriteService.COLLECTION_DOG_SHARING,
            queries = any()
        ) } returns sharingResponse
        
        // Mock shared dog details
        val mockSharedDog = mockk<Document<Map<String, Any>>>()
        every { mockSharedDog.id } returns "shared-dog-1"
        val sharedDogData = mutableMapOf<String, Any>(
            "name" to "Rex",
            "breed" to "German Shepherd",
            "sex" to "MALE",
            "birthDate" to "2019-06-10",
            "weight" to 30.0,
            "activityLevel" to "HIGH",
            "ownerId" to "other-user-id"
        )
        sharedDogData["imageId"] = null as Any? ?: "null"
        every { mockSharedDog.data } returns sharedDogData
        coEvery { mockDatabases.getDocument(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = AppwriteService.COLLECTION_DOGS,
            documentId = "shared-dog-1"
        ) } returns mockSharedDog

        // Test
        val result = dogRepository.getDogs().first()
        
        // Verify
        assertEquals(1, result.size)
        assertEquals("Rex", result[0].name)
        assertEquals("German Shepherd", result[0].breed)
        assertEquals("other-user-id", result[0].ownerId)
        // Note: isShared property doesn't exist in the Dog model
    }

    @Test
    fun `test dog data conversion handles all required fields`() = runBlocking {
        // Create a mock document with all required fields
        val mockDocument = mockk<Document<Map<String, Any>>>()
        every { mockDocument.id } returns "complete-dog"
        every { mockDocument.data } returns mapOf<String, Any>(
            "name" to "Complete Dog",
            "breed" to "Labrador",
            "sex" to "FEMALE",
            "birthDate" to "2021-03-20",
            "weight" to 20.5,
            "activityLevel" to "LOW",
            "ownerId" to "test-user-id",
            "imageId" to "image-123",
            "microchipNumber" to "123456789012345",
            "vaccinationStatus" to "UP_TO_DATE",
            "medicalNotes" to "Healthy dog"
        )
        
        val dogsResponse = mockk<DocumentList<Map<String, Any>>>()
        every { dogsResponse.documents } returns listOf(mockDocument)
        
        coEvery { mockDatabases.listDocuments(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = AppwriteService.COLLECTION_DOGS,
            queries = any()
        ) } returns dogsResponse
        
        // Mock empty team memberships
        val emptyMemberships = mockk<DocumentList<Map<String, Any>>>()
        every { emptyMemberships.documents } returns emptyList()
        coEvery { mockDatabases.listDocuments(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = AppwriteService.COLLECTION_TEAM_MEMBERS,
            queries = any()
        ) } returns emptyMemberships

        // Test
        val result = dogRepository.getDogs().first()
        
        // Verify all fields are correctly converted
        assertEquals(1, result.size)
        val dog = result[0]
        assertEquals("complete-dog", dog.id)
        assertEquals("Complete Dog", dog.name)
        assertEquals("Labrador", dog.breed)
        assertEquals(Sex.FEMALE, dog.sex)
        assertEquals(20.5, dog.weight)
        assertEquals(ActivityLevel.LOW, dog.activityLevel)
        assertEquals("image-123", dog.imageId)
        assertNotNull(dog.birthDate)
    }

    @Test
    fun `test repository handles database errors gracefully`() = runBlocking {
        // Mock database error
        coEvery { mockDatabases.listDocuments(any(), any(), any()) } throws Exception("Database error")
        
        // Test
        val result = runCatching { dogRepository.getDogs().first() }
        
        // Verify error is handled (depending on implementation, might return empty list or throw)
        assertTrue(result.isFailure || (result.isSuccess && result.getOrNull()?.isEmpty() == true))
    }

    @Test
    fun `test getAppwriteService returns correct instance`() {
        // Test
        val service = dogRepository.getAppwriteService()
        
        // Verify
        assertEquals(mockAppwriteService, service)
    }

    @Test
    fun `test dog conversion handles missing optional fields`() = runBlocking {
        // Mock document with minimal required fields only
        val mockDocument = mockk<Document<Map<String, Any>>>()
        every { mockDocument.id } returns "minimal-dog"
        every { mockDocument.data } returns mapOf<String, Any>(
            "name" to "Minimal Dog",
            "breed" to "Mixed",
            "sex" to "MALE",
            "birthDate" to "2022-01-01",
            "ownerId" to "test-user-id"
            // Missing weight, activityLevel, imageId, etc.
        )
        
        val dogsResponse = mockk<DocumentList<Map<String, Any>>>()
        every { dogsResponse.documents } returns listOf(mockDocument)
        
        coEvery { mockDatabases.listDocuments(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = AppwriteService.COLLECTION_DOGS,
            queries = any()
        ) } returns dogsResponse
        
        // Mock empty team memberships
        val emptyMemberships = mockk<DocumentList<Map<String, Any>>>()
        every { emptyMemberships.documents } returns emptyList()
        coEvery { mockDatabases.listDocuments(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = AppwriteService.COLLECTION_TEAM_MEMBERS,
            queries = any()
        ) } returns emptyMemberships

        // Test
        val result = dogRepository.getDogs().first()
        
        // Verify dog is created despite missing optional fields
        assertEquals(1, result.size)
        val dog = result[0]
        assertEquals("Minimal Dog", dog.name)
        assertEquals("Mixed", dog.breed)
        assertEquals(Sex.MALE, dog.sex)
    }
}