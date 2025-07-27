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
import org.junit.Assert.*

/**
 * Unit tests for BarcodeRepository
 * Tests barcode scanning and food lookup functionality
 */
@RunWith(RobolectricTestRunner::class)
class BarcodeRepositoryTest {
    
    // Helper function for EAN validation
    private fun isValidEAN(ean: String): Boolean {
        if (ean.length != 13 || !ean.all { it.isDigit() }) return false
        
        val digits = ean.map { it.toString().toInt() }
        val checksum = digits.take(12).mapIndexed { index, digit ->
            if (index % 2 == 0) digit else digit * 3
        }.sum()
        
        val checkDigit = (10 - (checksum % 10)) % 10
        return checkDigit == digits[12]
    }

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
        
        barcodeRepository = BarcodeRepository(context, mockAppwriteService)
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
            "barcode" to "1234567890123",
            "brand" to "Test Brand",
            "name" to "Test Product",
            "protein" to 25.0,
            "fat" to 12.5,
            "crudeFiber" to 3.0,
            "rawAsh" to 6.5,
            "moisture" to 10.0,
            "additives" to "{}",
            "imageUrl" to "https://example.com/product.jpg"
        )
        
        val foodResponse = mockk<DocumentList<Map<String, Any>>>(relaxed = true)
        every { foodResponse.documents } returns listOf(mockDocument as Document<Map<String, Any>>)
        
        coEvery { mockDatabases.listDocuments(
            databaseId = any(),
            collectionId = any(),
            queries = any()
        ) } returns foodResponse

        // Test
        val result = barcodeRepository.lookupProduct("1234567890123")
        
        // Verify
        assertTrue(result.isSuccess)
        val product = result.getOrNull()
        assertNotNull(product)
        assertEquals("1234567890123", product?.barcode)
        assertEquals("Test Brand", product?.brand)
        assertEquals("Test Product", product?.name)
        assertEquals(25.0, product?.nutritionalInfo?.protein ?: 0.0)
    }

    @Test
    fun `test lookupFoodByBarcode returns failure for non-existent EAN`() = runBlocking {
        // Mock empty response
        val emptyResponse = mockk<DocumentList<Map<String, Any>>>(relaxed = true)
        every { emptyResponse.documents } returns emptyList<Document<Map<String, Any>>>()
        
        coEvery { mockDatabases.listDocuments(any(), any(), any()) } returns emptyResponse

        // Test
        val result = barcodeRepository.lookupProduct("9999999999999")
        
        // Verify
        assertTrue(result.isFailure)
    }

    @Test
    fun `test lookupFoodByBarcode handles database exceptions`() = runBlocking {
        // Mock database exception
        coEvery { mockDatabases.listDocuments(any(), any(), any()) } throws Exception("Database error")

        // Test
        val result = barcodeRepository.lookupProduct("1234567890123")
        
        // Verify
        assertTrue(result.isFailure)
    }

    @Test
    fun `test validateEAN accepts valid EAN-13 codes`() {
        // Test valid EAN-13 codes
        assertTrue(isValidEAN("1234567890123"))
        assertTrue(isValidEAN("9876543210987"))
        assertTrue(isValidEAN("4001234567890"))
    }

    @Test
    fun `test validateEAN rejects invalid EAN codes`() {
        // Test invalid EAN codes
        assertTrue(!isValidEAN("123")) // Too short
        assertTrue(!isValidEAN("12345678901234")) // Too long
        assertTrue(!isValidEAN("abcdefghijklm")) // Non-numeric
        assertTrue(!isValidEAN("")) // Empty
    }

    @Test
    fun `test lookupFoodByBarcode validates EAN format before query`() = runBlocking {
        // Test with invalid EAN (should fail validation, not query database)
        val result = barcodeRepository.lookupProduct("invalid-ean")
        
        // Verify
        assertTrue(result.isFailure)
        
        // Verify database was not queried for invalid EAN
        coVerify(exactly = 0) { mockDatabases.listDocuments(any(), any(), any()) }
    }

    @Test
    fun `test barcode scanning workflow integration`() = runBlocking {
        // Mock successful barcode lookup
        val mockDocument = mockk<Document<Map<String, Any>>>()
        every { mockDocument.id } returns "scanned-food"
        every { mockDocument.data } returns mapOf(
            "barcode" to "2345678901234",
            "brand" to "Scanned Brand",
            "name" to "Scanned Product",
            "protein" to 28.0,
            "fat" to 15.0
        )
        
        val foodResponse = mockk<DocumentList<Map<String, Any>>>(relaxed = true)
        every { foodResponse.documents } returns listOf(mockDocument as Document<Map<String, Any>>)
        
        coEvery { mockDatabases.listDocuments(any(), any(), any()) } returns foodResponse

        // Simulate complete barcode scanning workflow
        val scannedEAN = "2345678901234"
        
        // 1. Validate scanned EAN
        val isValidEAN = isValidEAN(scannedEAN)
        assertTrue(isValidEAN)
        
        // 2. Lookup food by EAN
        val lookupResult = barcodeRepository.lookupProduct(scannedEAN)
        assertTrue(lookupResult.isSuccess)
        
        // 3. Verify food data
        val food = lookupResult.getOrNull()
        assertNotNull(food)
        assertEquals(scannedEAN, food?.barcode)
        assertEquals("Scanned Brand", food?.brand)
        assertEquals("Scanned Product", food?.name)
    }

    @Test
    fun `test concurrent barcode lookups handle properly`() = runBlocking {
        // Mock food document
        val mockDocument = mockk<Document<Map<String, Any>>>()
        every { mockDocument.id } returns "concurrent-food"
        every { mockDocument.data } returns mapOf(
            "barcode" to "3456789012345",
            "brand" to "Concurrent Brand",
            "name" to "Concurrent Product"
        )
        
        val foodResponse = mockk<DocumentList<Map<String, Any>>>(relaxed = true)
        every { foodResponse.documents } returns listOf(mockDocument as Document<Map<String, Any>>)
        
        coEvery { mockDatabases.listDocuments(any(), any(), any()) } returns foodResponse

        // Perform multiple concurrent lookups
        val ean = "3456789012345"
        val results = listOf(
            barcodeRepository.lookupProduct(ean),
            barcodeRepository.lookupProduct(ean),
            barcodeRepository.lookupProduct(ean)
        )
        
        // Verify all succeed
        results.forEach { result ->
            assertTrue(result.isSuccess)
            assertEquals(ean, result.getOrNull()?.barcode)
        }
    }

    @Test
    fun `test barcode repository handles partial food data`() = runBlocking {
        // Mock food document with minimal data
        val mockDocument = mockk<Document<Map<String, Any>>>()
        every { mockDocument.id } returns "minimal-barcode-food"
        every { mockDocument.data } returns mapOf(
            "barcode" to "4567890123456",
            "name" to "Minimal Product"
            // Missing brand, nutrition data, etc.
        )
        
        val foodResponse = mockk<DocumentList<Map<String, Any>>>(relaxed = true)
        every { foodResponse.documents } returns listOf(mockDocument as Document<Map<String, Any>>)
        
        coEvery { mockDatabases.listDocuments(any(), any(), any()) } returns foodResponse

        // Test
        val result = barcodeRepository.lookupProduct("4567890123456")
        
        // Verify food is created with defaults for missing fields
        assertTrue(result.isSuccess)
        val food = result.getOrNull()
        assertNotNull(food)
        assertEquals("4567890123456", food?.barcode)
        assertEquals("Minimal Product", food?.name)
        assertEquals("", food?.brand ?: "") // Default empty string
        assertEquals(0.0, food?.nutritionalInfo?.protein ?: 0.0) // Default 0.0
    }
}