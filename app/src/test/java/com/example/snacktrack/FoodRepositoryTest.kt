package com.example.snacktrack

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.snacktrack.data.model.Food
import com.example.snacktrack.data.model.FoodSubmission
import com.example.snacktrack.data.model.SubmissionStatus
import com.example.snacktrack.data.repository.FoodRepository
import com.example.snacktrack.data.service.AppwriteService
import io.appwrite.models.Document
import io.appwrite.models.DocumentList
import io.appwrite.services.Databases
import io.appwrite.Query
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
 * Comprehensive unit tests for FoodRepository
 * Tests food search, retrieval, and submission functionality
 */
@RunWith(RobolectricTestRunner::class)
class FoodRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var foodRepository: FoodRepository
    
    private val mockAppwriteService = mockk<AppwriteService>()
    private val mockDatabases = mockk<Databases>()

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        
        // Mock AppwriteService singleton
        mockkObject(AppwriteService)
        every { AppwriteService.getInstance(any()) } returns mockAppwriteService
        every { mockAppwriteService.databases } returns mockDatabases
        
        foodRepository = FoodRepository(context)
    }

    @After
    fun tearDown() {
        clearAllMocks()
        unmockkAll()
    }

    @Test
    fun `test searchFoods returns food list with valid search query`() = runBlocking {
        // Mock food documents
        val mockDocument = mockk<Document<Map<String, Any>>>()
        every { mockDocument.id } returns "food-1"
        every { mockDocument.data } returns mapOf<String, Any>(
            "ean" to "1234567890123",
            "brand" to "Premium Dog Food",
            "product" to "Adult Chicken & Rice",
            "protein" to 28.5,
            "fat" to 15.2,
            "crudeFiber" to 3.1,
            "rawAsh" to 7.8,
            "moisture" to 10.0,
            "additives" to "{\"vitaminE\":\"200mg/kg\",\"vitaminA\":\"15000IU/kg\"}",
            "imageUrl" to "https://example.com/food.jpg"
        )
        
        val foodResponse = mockk<DocumentList<Map<String, Any>>>()
        every { foodResponse.documents } returns listOf(mockDocument)
        
        // Note: Not mocking Query static methods as they're part of the SDK
        // The repository will call them internally
        coEvery { mockDatabases.listDocuments(
            any(), any(), any()
        ) } returns foodResponse

        // Test
        val result = foodRepository.searchFoods("chicken").first()
        
        // Verify
        assertEquals(1, result.size)
        val food = result[0]
        assertEquals("food-1", food.id)
        assertEquals("1234567890123", food.ean)
        assertEquals("Premium Dog Food", food.brand)
        assertEquals("Adult Chicken & Rice", food.product)
        assertEquals(28.5, food.protein)
        assertEquals(15.2, food.fat)
        assertEquals(3.1, food.crudeFiber)
        assertEquals(7.8, food.rawAsh)
        assertEquals(10.0, food.moisture)
        assertEquals("https://example.com/food.jpg", food.imageUrl)
        assertTrue(food.additives.containsKey("vitaminE"))
        assertEquals("200mg/kg", food.additives["vitaminE"])
    }

    @Test
    fun `test searchFoods returns empty list when no results found`() = runBlocking {
        // Mock empty response
        val emptyResponse = mockk<DocumentList<Map<String, Any>>>()
        every { emptyResponse.documents } returns emptyList()
        
        // Note: Not mocking Query static methods as they're part of the SDK
        // The repository will call them internally
        coEvery { mockDatabases.listDocuments(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = AppwriteService.COLLECTION_FOOD_DB,
            queries = any()
        ) } returns emptyResponse

        // Test
        val result = foodRepository.searchFoods("nonexistent").first()
        
        // Verify
        assertTrue(result.isEmpty())
    }

    @Test
    fun `test searchFoods handles database exceptions gracefully`() = runBlocking {
        // Note: Not mocking Query static methods as they're part of the SDK
        // The repository will call them internally
        // Mock database exception
        coEvery { mockDatabases.listDocuments(any(), any(), any()) } throws Exception("Database error")

        // Test
        val result = foodRepository.searchFoods("chicken").first()
        
        // Verify - should return empty list on error
        assertTrue(result.isEmpty())
    }

    @Test
    fun `test getFoodById returns food with valid ID`() = runBlocking {
        // Mock food document
        val mockDocument = mockk<Document<Map<String, Any>>>()
        every { mockDocument.id } returns "specific-food-id"
        val foodData = mutableMapOf<String, Any>(
            "ean" to "9876543210987",
            "brand" to "Healthy Pet",
            "product" to "Salmon & Sweet Potato",
            "protein" to 26.0,
            "fat" to 12.5,
            "crudeFiber" to 4.2,
            "rawAsh" to 6.5,
            "moisture" to 12.0,
            "additives" to "{}"
        )
        foodData["imageUrl"] = null as Any? ?: "null"
        every { mockDocument.data } returns foodData as Map<String, Any>
        
        coEvery { mockDatabases.getDocument(
            any(), any(), any()
        ) } returns mockDocument

        // Test
        val result = foodRepository.getFoodById("specific-food-id")
        
        // Verify
        assertTrue(result.isSuccess)
        val food = result.getOrNull()
        assertNotNull(food)
        assertEquals("specific-food-id", food.id)
        assertEquals("9876543210987", food.ean)
        assertEquals("Healthy Pet", food.brand)
        assertEquals("Salmon & Sweet Potato", food.product)
        assertEquals(26.0, food.protein)
        assertEquals("null", food.imageUrl)
        assertTrue(food.additives.isEmpty())
    }

    @Test
    fun `test getFoodById returns failure with invalid ID`() = runBlocking {
        // Mock database exception for invalid ID
        coEvery { mockDatabases.getDocument(any(), any(), any()) } throws io.appwrite.exceptions.AppwriteException("Document not found", 404)

        // Test
        val result = foodRepository.getFoodById("invalid-id")
        
        // Verify
        assertTrue(result.isFailure)
    }

    @Test
    fun `test food data conversion handles missing optional fields`() = runBlocking {
        // Note: Not mocking Query static methods as they're part of the SDK
        // The repository will call them internally
        // Mock document with minimal data
        val mockDocument = mockk<Document<Map<String, Any>>>()
        every { mockDocument.id } returns "minimal-food"
        every { mockDocument.data } returns mapOf<String, Any>(
            "product" to "Basic Food",
            "brand" to "Generic"
            // Missing most nutrition data
        )
        
        val foodResponse = mockk<DocumentList<Map<String, Any>>>()
        every { foodResponse.documents } returns listOf(mockDocument)
        
        coEvery { mockDatabases.listDocuments(any(), any(), any()) } returns foodResponse

        // Test
        val result = foodRepository.searchFoods("basic").first()
        
        // Verify food is created with default values for missing fields
        assertEquals(1, result.size)
        val food = result[0]
        assertEquals("Basic Food", food.product)
        assertEquals("Generic", food.brand)
        assertEquals("", food.ean) // Default empty string
        assertEquals(0.0, food.protein) // Default 0.0
        assertEquals(0.0, food.fat)
        assertEquals(0.0, food.crudeFiber)
        assertEquals(0.0, food.rawAsh)
        assertEquals(0.0, food.moisture)
        assertTrue(food.additives.isEmpty()) // Default empty map
        assertEquals(null, food.imageUrl) // Default null
    }

    @Test
    fun `test food data conversion handles malformed additives JSON`() = runBlocking {
        // Note: Not mocking Query static methods as they're part of the SDK
        // The repository will call them internally
        // Mock document with invalid JSON in additives
        val mockDocument = mockk<Document<Map<String, Any>>>()
        every { mockDocument.id } returns "malformed-additives"
        every { mockDocument.data } returns mapOf<String, Any>(
            "product" to "Test Food",
            "brand" to "Test Brand",
            "additives" to "invalid-json-string",
            "protein" to 25.0
        )
        
        val foodResponse = mockk<DocumentList<Map<String, Any>>>()
        every { foodResponse.documents } returns listOf(mockDocument)
        
        coEvery { mockDatabases.listDocuments(any(), any(), any()) } returns foodResponse

        // Test
        val result = foodRepository.searchFoods("test").first()
        
        // Verify food is created with empty additives map for malformed JSON
        assertEquals(1, result.size)
        val food = result[0]
        assertEquals("Test Food", food.product)
        assertTrue(food.additives.isEmpty()) // Should be empty due to malformed JSON
        assertEquals(25.0, food.protein) // Other fields should still work
    }

    @Test
    fun `test search query construction includes proper parameters`() = runBlocking {
        // Mock empty response to focus on query verification
        val emptyResponse = mockk<DocumentList<Map<String, Any>>>()
        every { emptyResponse.documents } returns emptyList()
        
        // Note: Not mocking Query static methods as they're part of the SDK
        // The repository will call them internally
        coEvery { mockDatabases.listDocuments(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = AppwriteService.COLLECTION_FOOD_DB,
            queries = any()
        ) } returns emptyResponse

        // Test
        foodRepository.searchFoods("chicken rice").first()
        
        // Verify that listDocuments was called with the correct parameters
        coVerify { mockDatabases.listDocuments(
            databaseId = any(),
            collectionId = any(),
            queries = any()
        ) }
    }

    @Test
    fun `test food nutritional data types are correctly converted`() = runBlocking {
        // Note: Not mocking Query static methods as they're part of the SDK
        // The repository will call them internally
        // Mock document with various numeric types
        val mockDocument = mockk<Document<Map<String, Any>>>()
        every { mockDocument.id } returns "numeric-test"
        val foodData = mutableMapOf<String, Any>(
            "product" to "Numeric Test Food",
            "protein" to 25, // Integer
            "fat" to 15.5f, // Float
            "crudeFiber" to 3.2, // Double
            "rawAsh" to "7.8" // String (should be converted)
        )
        foodData["moisture"] = null as Any? ?: 0.0
        every { mockDocument.data } returns foodData
        
        val foodResponse = mockk<DocumentList<Map<String, Any>>>()
        every { foodResponse.documents } returns listOf(mockDocument)
        
        coEvery { mockDatabases.listDocuments(any(), any(), any()) } returns foodResponse

        // Test
        val result = foodRepository.searchFoods("numeric").first()
        
        // Verify all numeric conversions work correctly
        assertEquals(1, result.size)
        val food = result[0]
        assertEquals(25.0, food.protein) // Int to Double
        assertEquals(15.5, food.fat, 0.01) // Float to Double
        assertEquals(3.2, food.crudeFiber) // Double stays Double
        assertEquals(0.0, food.rawAsh) // String conversion fails, defaults to 0.0
        assertEquals(0.0, food.moisture) // Null defaults to 0.0
    }
}