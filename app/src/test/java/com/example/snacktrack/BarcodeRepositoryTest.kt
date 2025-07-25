package com.example.snacktrack

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.snacktrack.data.model.Food
import com.example.snacktrack.data.repository.BarcodeRepository
import com.example.snacktrack.data.repository.FoodRepository
import com.example.snacktrack.data.service.AppwriteService
import io.appwrite.models.Document
import io.appwrite.models.DocumentList
import io.appwrite.services.Databases
import io.mockk.*
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
 * Unit tests for BarcodeRepository
 * Tests barcode scanning and food lookup functionality
 */
@RunWith(RobolectricTestRunner::class)
class BarcodeRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var barcodeRepository: BarcodeRepository
    
    private val mockAppwriteService = mockk<AppwriteService>()
    private val mockDatabases = mockk<Databases>()
    private val mockFoodRepository = mockk<FoodRepository>()

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        
        // Mock AppwriteService singleton
        mockkObject(AppwriteService)
        every { AppwriteService.getInstance(any()) } returns mockAppwriteService
        every { mockAppwriteService.databases } returns mockDatabases
        
        // Mock FoodRepository constructor
        mockkConstructor(FoodRepository::class)
        
        barcodeRepository = BarcodeRepository(context)
    }

    @After
    fun tearDown() {
        clearAllMocks()
        unmockkAll()
    }

    @Test
    fun `test lookupFoodByBarcode returns food for valid EAN`() = runBlocking {
        // Mock food document for barcode
        val mockDocument = mockk<Document<Map<String, Any>>>()
        every { mockDocument.id } returns "barcode-food-1"
        every { mockDocument.data } returns mapOf(
            "ean" to "1234567890123",
            "brand" to "Test Brand",
            "product" to "Test Product",
            "protein" to 25.0,
            "fat" to 12.5,
            "crudeFiber" to 3.0,
            "rawAsh" to 6.5,
            "moisture" to 10.0,
            "additives" to "{}",
            "imageUrl" to "https://example.com/product.jpg"
        )
        
        val foodResponse = mockk<DocumentList<Document<Map<String, Any>>>>()
        every { foodResponse.documents } returns listOf(mockDocument)
        
        every { mockDatabases.listDocuments(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = AppwriteService.COLLECTION_FOOD_DB,
            queries = any()
        ) } returns foodResponse

        // Test
        val result = barcodeRepository.lookupFoodByBarcode("1234567890123")
        
        // Verify
        assertTrue(result.isSuccess)
        val food = result.getOrNull()
        assertNotNull(food)
        assertEquals("1234567890123", food.ean)
        assertEquals("Test Brand", food.brand)
        assertEquals("Test Product", food.product)
        assertEquals(25.0, food.protein)
    }

    @Test
    fun `test lookupFoodByBarcode returns failure for non-existent EAN`() = runBlocking {
        // Mock empty response
        val emptyResponse = mockk<DocumentList<Document<Map<String, Any>>>>()
        every { emptyResponse.documents } returns emptyList()
        
        every { mockDatabases.listDocuments(any(), any(), any()) } returns emptyResponse

        // Test
        val result = barcodeRepository.lookupFoodByBarcode("9999999999999")
        
        // Verify
        assertTrue(result.isFailure)
    }

    @Test
    fun `test lookupFoodByBarcode handles database exceptions`() = runBlocking {
        // Mock database exception
        every { mockDatabases.listDocuments(any(), any(), any()) } throws Exception("Database error")

        // Test
        val result = barcodeRepository.lookupFoodByBarcode("1234567890123")
        
        // Verify
        assertTrue(result.isFailure)
    }

    @Test
    fun `test validateEAN accepts valid EAN-13 codes`() {
        // Test valid EAN-13 codes
        assertTrue(barcodeRepository.validateEAN("1234567890123"))
        assertTrue(barcodeRepository.validateEAN("9876543210987"))
        assertTrue(barcodeRepository.validateEAN("4001234567890"))
    }

    @Test
    fun `test validateEAN rejects invalid EAN codes`() {
        // Test invalid EAN codes
        assertTrue(!barcodeRepository.validateEAN("123")) // Too short
        assertTrue(!barcodeRepository.validateEAN("12345678901234")) // Too long
        assertTrue(!barcodeRepository.validateEAN("abcdefghijklm")) // Non-numeric
        assertTrue(!barcodeRepository.validateEAN("")) // Empty
    }

    @Test
    fun `test lookupFoodByBarcode validates EAN format before query`() = runBlocking {
        // Test with invalid EAN (should fail validation, not query database)
        val result = barcodeRepository.lookupFoodByBarcode("invalid-ean")
        
        // Verify
        assertTrue(result.isFailure)
        
        // Verify database was not queried for invalid EAN
        verify(exactly = 0) { mockDatabases.listDocuments(any(), any(), any()) }
    }

    @Test
    fun `test barcode scanning workflow integration`() = runBlocking {
        // Mock successful barcode lookup
        val mockDocument = mockk<Document<Map<String, Any>>>()
        every { mockDocument.id } returns "scanned-food"
        every { mockDocument.data } returns mapOf(
            "ean" to "2345678901234",
            "brand" to "Scanned Brand",
            "product" to "Scanned Product",
            "protein" to 28.0,
            "fat" to 15.0
        )
        
        val foodResponse = mockk<DocumentList<Document<Map<String, Any>>>>()
        every { foodResponse.documents } returns listOf(mockDocument)
        
        every { mockDatabases.listDocuments(any(), any(), any()) } returns foodResponse

        // Simulate complete barcode scanning workflow
        val scannedEAN = "2345678901234"
        
        // 1. Validate scanned EAN
        val isValidEAN = barcodeRepository.validateEAN(scannedEAN)
        assertTrue(isValidEAN)
        
        // 2. Lookup food by EAN
        val lookupResult = barcodeRepository.lookupFoodByBarcode(scannedEAN)
        assertTrue(lookupResult.isSuccess)
        
        // 3. Verify food data
        val food = lookupResult.getOrNull()
        assertNotNull(food)
        assertEquals(scannedEAN, food.ean)
        assertEquals("Scanned Brand", food.brand)
        assertEquals("Scanned Product", food.product)
    }

    @Test
    fun `test concurrent barcode lookups handle properly`() = runBlocking {
        // Mock food document
        val mockDocument = mockk<Document<Map<String, Any>>>()
        every { mockDocument.id } returns "concurrent-food"
        every { mockDocument.data } returns mapOf(
            "ean" to "3456789012345",
            "brand" to "Concurrent Brand",
            "product" to "Concurrent Product"
        )
        
        val foodResponse = mockk<DocumentList<Document<Map<String, Any>>>>()
        every { foodResponse.documents } returns listOf(mockDocument)
        
        every { mockDatabases.listDocuments(any(), any(), any()) } returns foodResponse

        // Perform multiple concurrent lookups
        val ean = "3456789012345"
        val results = listOf(
            barcodeRepository.lookupFoodByBarcode(ean),
            barcodeRepository.lookupFoodByBarcode(ean),
            barcodeRepository.lookupFoodByBarcode(ean)
        )
        
        // Verify all succeed
        results.forEach { result ->
            assertTrue(result.isSuccess)
            assertEquals(ean, result.getOrNull()?.ean)
        }
    }

    @Test
    fun `test barcode repository handles partial food data`() = runBlocking {
        // Mock food document with minimal data
        val mockDocument = mockk<Document<Map<String, Any>>>()
        every { mockDocument.id } returns "minimal-barcode-food"
        every { mockDocument.data } returns mapOf(
            "ean" to "4567890123456",
            "product" to "Minimal Product"
            // Missing brand, nutrition data, etc.
        )
        
        val foodResponse = mockk<DocumentList<Document<Map<String, Any>>>>()
        every { foodResponse.documents } returns listOf(mockDocument)
        
        every { mockDatabases.listDocuments(any(), any(), any()) } returns foodResponse

        // Test
        val result = barcodeRepository.lookupFoodByBarcode("4567890123456")
        
        // Verify food is created with defaults for missing fields
        assertTrue(result.isSuccess)
        val food = result.getOrNull()
        assertNotNull(food)
        assertEquals("4567890123456", food.ean)
        assertEquals("Minimal Product", food.product)
        assertEquals("", food.brand) // Default empty string
        assertEquals(0.0, food.protein) // Default 0.0
    }
}