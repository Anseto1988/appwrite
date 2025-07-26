package com.example.snacktrack.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snacktrack.data.model.*
import com.example.snacktrack.data.repository.BarcodeRepository
import com.example.snacktrack.data.service.AppwriteService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class BarcodeUiState(
    val isLoading: Boolean = false,
    val isScanning: Boolean = false,
    val scannedProduct: Product? = null,
    val productHistory: List<BarcodeHistory> = emptyList(),
    val comparingProducts: List<Product> = emptyList(),
    val comparisonResult: ProductComparison? = null,
    val inventory: List<ProductInventory> = emptyList(),
    val recommendations: List<ProductRecommendation> = emptyList(),
    val allergenAlerts: List<AllergenAlert> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

class BarcodeViewModel(
    private val context: android.content.Context,
    private val appwriteService: AppwriteService,
    private val dogId: String
) : ViewModel() {
    private val barcodeRepository = BarcodeRepository(context, appwriteService)
    
    private val _uiState = MutableStateFlow(BarcodeUiState())
    val uiState: StateFlow<BarcodeUiState> = _uiState.asStateFlow()
    
    init {
        loadInventory()
        loadProductHistory()
    }
    
    private fun loadInventory() {
        viewModelScope.launch {
            try {
                val userId = appwriteService.getCurrentUserId() ?: return@launch
                val result = barcodeRepository.getUserInventory(userId)
                result.fold(
                    onSuccess = { inventory ->
                        _uiState.update { it.copy(inventory = inventory) }
                    },
                    onFailure = { /* Handle error */ }
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    private fun loadProductHistory() {
        viewModelScope.launch {
            try {
                val userId = appwriteService.getCurrentUserId() ?: return@launch
                val result = barcodeRepository.getBarcodeHistory(
                    userId = userId,
                    dogId = dogId,
                    limit = 10
                )
                result.fold(
                    onSuccess = { history ->
                        _uiState.update { it.copy(productHistory = history) }
                    },
                    onFailure = { /* Handle error */ }
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    fun startScanning() {
        _uiState.update { it.copy(isScanning = true, scannedProduct = null, error = null) }
    }
    
    fun stopScanning() {
        _uiState.update { it.copy(isScanning = false) }
    }
    
    fun processBarcodeImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Scan barcode
                val scanResult = barcodeRepository.scanBarcode(bitmap)
                
                scanResult.getOrNull()?.let { barcodeResult ->
                    // Get product details
                    val productResult = barcodeRepository.getProductByBarcode(barcodeResult.barcode)
                    productResult.fold(
                        onSuccess = { product ->
                            _uiState.update { 
                                it.copy(
                                    scannedProduct = product,
                                    isLoading = false,
                                    isScanning = false
                                )
                            }
                            
                            // Check for allergens
                            launch { checkAllergens(product) }
                            
                            // Get recommendations
                            launch { getRecommendations(product) }
                            
                            // Record scan history
                            launch { recordScanHistory(barcodeResult.barcode, product) }
                        },
                        onFailure = {
                            // Product not found, try to extract from image
                            tryExtractProductInfo(bitmap, barcodeResult.barcode)
                        }
                    )
                } ?: run {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = scanResult.exceptionOrNull()?.message ?: "Barcode konnte nicht gescannt werden"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    private suspend fun tryExtractProductInfo(bitmap: Bitmap, barcode: String) {
        // Try OCR to extract product information
        val ocrResult = barcodeRepository.extractProductInfo(bitmap)
        
        ocrResult.getOrNull()?.let { extracted ->
            // Create a temporary product from extracted data
            val product = Product(
                barcode = barcode,
                name = extracted.extractedData.productName ?: "Unbekanntes Produkt",
                brand = extracted.extractedData.brand ?: "",
                ingredients = extracted.extractedData.ingredients.map { Ingredient(name = it) },
                source = BarcodeDataSource.AI_EXTRACTED,
                verificationStatus = VerificationStatus.UNVERIFIED
            )
            
            _uiState.update { 
                it.copy(
                    scannedProduct = product,
                    isLoading = false,
                    isScanning = false
                )
            }
        } ?: run {
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    error = "Produkt nicht gefunden und Informationen konnten nicht extrahiert werden"
                )
            }
        }
    }
    
    private suspend fun checkAllergens(product: Product) {
        val result = barcodeRepository.checkProductAllergens(dogId, product)
        result.getOrNull()?.let { alerts ->
            _uiState.update { it.copy(allergenAlerts = alerts) }
        }
    }
    
    private suspend fun getRecommendations(product: Product) {
        val result = barcodeRepository.getProductRecommendations(dogId, com.example.snacktrack.data.repository.RecommendationContext.GENERAL)
        result.getOrNull()?.let { recommendations ->
            _uiState.update { it.copy(recommendations = recommendations) }
        }
    }
    
    private suspend fun recordScanHistory(barcode: String, product: Product) {
        val userId = appwriteService.getCurrentUserId() ?: return
        barcodeRepository.recordBarcodeHistory(
            userId = userId,
            dogId = dogId,
            barcode = barcode,
            product = product,
            action = ScanAction.VIEW
        )
    }
    
    fun addToInventory(product: Product, quantity: Double, unit: StockUnit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val userId = appwriteService.getCurrentUserId() ?: return@launch
                val result = barcodeRepository.addToInventory(
                    userId = userId,
                    product = product,
                    quantity = quantity,
                    unit = unit
                )
                
                result.getOrNull()?.let {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            successMessage = "Produkt wurde zum Inventar hinzugefügt"
                        )
                    }
                    loadInventory() // Refresh inventory
                } ?: run {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = result.exceptionOrNull()?.message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    fun addToComparison(product: Product) {
        _uiState.update { state ->
            val updatedList = state.comparingProducts + product
            state.copy(comparingProducts = updatedList.distinct())
        }
    }
    
    fun removeFromComparison(product: Product) {
        _uiState.update { state ->
            state.copy(comparingProducts = state.comparingProducts.filter { it.id != product.id })
        }
    }
    
    fun compareProducts(criteria: List<ComparisonCriterion>) {
        viewModelScope.launch {
            if (_uiState.value.comparingProducts.size < 2) {
                _uiState.update { 
                    it.copy(error = "Mindestens 2 Produkte für Vergleich erforderlich")
                }
                return@launch
            }
            
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val result = barcodeRepository.compareProducts(
                    productIds = _uiState.value.comparingProducts.map { it.id },
                    criteria = criteria
                )
                
                result.getOrNull()?.let { comparison ->
                    _uiState.update { 
                        it.copy(
                            comparisonResult = comparison,
                            isLoading = false
                        )
                    }
                } ?: run {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = result.exceptionOrNull()?.message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    fun clearComparison() {
        _uiState.update { 
            it.copy(
                comparingProducts = emptyList(),
                comparisonResult = null
            )
        }
    }
    
    fun updateInventoryQuantity(inventoryId: String, newQuantity: Double) {
        viewModelScope.launch {
            try {
                val result = barcodeRepository.updateInventoryQuantity(inventoryId, newQuantity)
                result.getOrNull()?.let {
                    loadInventory() // Refresh inventory
                    _uiState.update { 
                        it.copy(successMessage = "Bestand aktualisiert")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    fun setReorderSettings(inventoryId: String, settings: ReorderSettings) {
        viewModelScope.launch {
            try {
                val result = barcodeRepository.setReorderSettings(inventoryId, settings)
                result.getOrNull()?.let {
                    loadInventory() // Refresh inventory
                    _uiState.update { 
                        it.copy(successMessage = "Nachbestellungseinstellungen aktualisiert")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    fun searchProductByText(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val result = barcodeRepository.searchProducts(query)
                result.getOrNull()?.let { products ->
                    if (products.isNotEmpty()) {
                        _uiState.update { 
                            it.copy(
                                scannedProduct = products.first(),
                                recommendations = products.drop(1).map {
                                    ProductRecommendation(
                                        dogId = dogId,
                                        recommendedProduct = it,
                                        reason = "Ähnliches Produkt",
                                        score = 0.8
                                    )
                                },
                                isLoading = false
                            )
                        }
                    } else {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Keine Produkte gefunden"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
    
    fun clearScannedProduct() {
        _uiState.update { 
            it.copy(
                scannedProduct = null,
                allergenAlerts = emptyList(),
                recommendations = emptyList()
            )
        }
    }
}

class BarcodeViewModelFactory(
    private val context: android.content.Context,
    private val appwriteService: AppwriteService,
    private val dogId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BarcodeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BarcodeViewModel(context, appwriteService, dogId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}