package com.example.snacktrack.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.example.snacktrack.data.model.*
import com.example.snacktrack.data.service.AppwriteService
// ML Kit imports removed - need to add ML Kit to dependencies
// import com.google.mlkit.vision.barcode.BarcodeScanning
// import com.google.mlkit.vision.barcode.common.Barcode
// import com.google.mlkit.vision.common.InputImage
// import com.google.mlkit.vision.text.TextRecognition
// import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import io.appwrite.Query
import io.appwrite.ID
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.Document
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BarcodeRepository(
    private val context: Context,
    private val appwriteService: AppwriteService
) {
    private val databases = appwriteService.databases
    private val storage = appwriteService.storage
    private val databaseId = "snacktrack_db"
    
    // Barcode scanner - ML Kit not available
    // private val barcodeScanner = BarcodeScanning.getClient()
    
    // Text recognizer for OCR - ML Kit not available
    // private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    // Product cache
    private val productCache = mutableMapOf<String, Product>()
    
    // External product databases
    private val externalDatabases = listOf(
        "https://world.openfoodfacts.org/api/v0/product/",
        "https://de.openpetfoodfacts.org/api/v0/product/"
    )
    
    // Barcode Scanning
    
    suspend fun scanBarcode(bitmap: Bitmap): Result<BarcodeResult> = withContext(Dispatchers.IO) {
        try {
            // ML Kit not available - return mock result for now
            // TODO: Add ML Kit dependencies or use alternative barcode scanning library
            
            val startTime = System.currentTimeMillis()
            val scanDuration = System.currentTimeMillis() - startTime
            
            val result = BarcodeResult(
                id = UUID.randomUUID().toString(),
                barcode = "4005500027638", // Mock barcode
                format = BarcodeFormat.EAN_13,
                scanTimestamp = LocalDateTime.now(),
                scanQuality = determineScanQuality("4005500027638"),
                rawData = "4005500027638",
                metadata = BarcodeMetadata(
                    scanDuration = scanDuration,
                    confidenceScore = 0.95f, // ML Kit doesn't provide confidence
                    multipleScans = 1
                )
            )
            
            // Save scan to history
            saveScanToHistory(result)
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Product Lookup
    
    suspend fun lookupProduct(barcode: String): Result<Product> = withContext(Dispatchers.IO) {
        try {
            // Check cache first
            productCache[barcode]?.let {
                return@withContext Result.success(it)
            }
            
            // Check local database
            val localProduct = getProductFromDatabase(barcode)
            if (localProduct != null) {
                productCache[barcode] = localProduct
                return@withContext Result.success(localProduct)
            }
            
            // Check external databases
            val externalProduct = fetchFromExternalDatabases(barcode)
            if (externalProduct != null) {
                // Save to local database
                saveProductToDatabase(externalProduct)
                productCache[barcode] = externalProduct
                return@withContext Result.success(externalProduct)
            }
            
            // Product not found
            Result.failure(Exception("Produkt nicht gefunden"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun getProductFromDatabase(barcode: String): Product? {
        return try {
            val response = databases.listDocuments(
                databaseId = databaseId,
                collectionId = "products",
                queries = listOf(Query.equal("barcode", barcode))
            )
            
            if (response.documents.isNotEmpty()) {
                parseProductFromDocument(response.documents.first())
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private suspend fun fetchFromExternalDatabases(barcode: String): Product? {
        for (baseUrl in externalDatabases) {
            try {
                val url = URL("$baseUrl$barcode.json")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(response)
                    
                    if (json.getInt("status") == 1) {
                        val productJson = json.getJSONObject("product")
                        return parseExternalProduct(barcode, productJson)
                    }
                }
            } catch (e: Exception) {
                // Try next database
                continue
            }
        }
        
        return null
    }
    
    private fun parseExternalProduct(barcode: String, json: JSONObject): Product {
        return Product(
            id = UUID.randomUUID().toString(),
            barcode = barcode,
            name = json.optString("product_name", "Unknown"),
            brand = json.optString("brands", ""),
            manufacturer = json.optString("manufacturing_places", null),
            category = determineCategory(json),
            description = json.optString("generic_name", ""),
            images = parseProductImages(json),
            nutritionalInfo = parseNutritionalInfo(json),
            ingredients = parseIngredients(json),
            allergens = parseAllergens(json),
            source = BarcodeDataSource.OFFICIAL_DATABASE,
            verificationStatus = VerificationStatus.COMMUNITY_VERIFIED
        )
    }
    
    // Product Management
    
    suspend fun saveProduct(product: Product): Result<Product> = withContext(Dispatchers.IO) {
        try {
            val document = databases.createDocument(
                databaseId = databaseId,
                collectionId = "products",
                documentId = product.id.ifEmpty { ID.unique() },
                data = product.toMap()
            )
            
            val savedProduct = product.copy(id = document.id)
            productCache[product.barcode] = savedProduct
            
            Result.success(savedProduct)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateProduct(product: Product): Result<Product> = withContext(Dispatchers.IO) {
        try {
            databases.updateDocument(
                databaseId = databaseId,
                collectionId = "products",
                documentId = product.id,
                data = product.toMap()
            )
            
            productCache[product.barcode] = product
            Result.success(product)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Product Comparison
    
    suspend fun compareProducts(
        productIds: List<String>,
        criteria: List<ComparisonCriterion>
    ): Result<ProductComparison> = withContext(Dispatchers.IO) {
        try {
            // Fetch all products
            val products = productIds.mapNotNull { id ->
                getProductById(id)
            }
            
            if (products.size < 2) {
                return@withContext Result.failure(Exception("Mindestens 2 Produkte für Vergleich erforderlich"))
            }
            
            // Calculate scores for each product
            val scores = mutableMapOf<String, Double>()
            val rankings = mutableListOf<ProductRanking>()
            val highlights = mutableMapOf<String, List<String>>()
            val warnings = mutableMapOf<String, List<String>>()
            
            products.forEach { product ->
                var totalScore = 0.0
                val productHighlights = mutableListOf<String>()
                val productWarnings = mutableListOf<String>()
                val pros = mutableListOf<String>()
                val cons = mutableListOf<String>()
                
                criteria.forEach { criterion ->
                    val score = calculateScore(product, criterion)
                    totalScore += score * criterion.weight
                    
                    when (criterion.type) {
                        CriterionType.PROTEIN_CONTENT -> {
                            val protein = product.nutritionalInfo?.protein ?: 0.0
                            if (protein > 30) {
                                productHighlights.add("Hoher Proteingehalt (${protein}%)")
                                pros.add("Reich an Proteinen")
                            } else if (protein < 20) {
                                productWarnings.add("Niedriger Proteingehalt (${protein}%)")
                                cons.add("Wenig Proteine")
                            }
                        }
                        CriterionType.PRICE -> {
                            product.variants.firstOrNull()?.price?.let { price ->
                                if (price.amount < 20) {
                                    productHighlights.add("Günstiger Preis")
                                    pros.add("Preiswert")
                                } else if (price.amount > 50) {
                                    productWarnings.add("Hoher Preis")
                                    cons.add("Teuer")
                                }
                            }
                        }
                        CriterionType.ALLERGEN_FREE -> {
                            if (product.allergens.isEmpty()) {
                                productHighlights.add("Keine bekannten Allergene")
                                pros.add("Allergenfrei")
                            } else {
                                productWarnings.add("Enthält ${product.allergens.size} Allergene")
                                cons.add("Enthält Allergene")
                            }
                        }
                        else -> {}
                    }
                }
                
                scores[product.id] = totalScore
                highlights[product.id] = productHighlights
                warnings[product.id] = productWarnings
                
                rankings.add(
                    ProductRanking(
                        productId = product.id,
                        score = totalScore,
                        pros = pros,
                        cons = cons
                    )
                )
            }
            
            // Sort rankings
            rankings.sortByDescending { it.score }
            rankings.forEachIndexed { index, ranking ->
                rankings[index] = ranking.copy(rank = index + 1)
            }
            
            // Generate recommendation
            val topProduct = products.find { it.id == rankings.first().productId }
            val recommendation = if (topProduct != null) {
                ComparisonRecommendation(
                    recommendedProductId = topProduct.id,
                    reason = "Beste Gesamtbewertung basierend auf Ihren Kriterien",
                    confidence = 0.85,
                    alternatives = rankings.drop(1).take(2).map { it.productId }
                )
            } else null
            
            val comparison = ProductComparison(
                id = UUID.randomUUID().toString(),
                products = products,
                comparisonCriteria = criteria,
                results = ComparisonResults(
                    scores = scores,
                    rankings = rankings,
                    highlights = highlights,
                    warnings = warnings
                ),
                recommendation = recommendation,
                createdAt = LocalDateTime.now()
            )
            
            // Save comparison
            saveComparison(comparison)
            
            Result.success(comparison)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun calculateScore(product: Product, criterion: ComparisonCriterion): Double {
        return when (criterion.type) {
            CriterionType.PRICE -> {
                val price = product.variants.firstOrNull()?.price?.amount ?: 0.0
                if (criterion.preference == PreferenceDirection.LOWER_BETTER) {
                    100.0 / (1 + price)
                } else {
                    price
                }
            }
            CriterionType.PROTEIN_CONTENT -> {
                product.nutritionalInfo?.protein ?: 0.0
            }
            CriterionType.FAT_CONTENT -> {
                val fat = product.nutritionalInfo?.fat ?: 0.0
                if (criterion.preference == PreferenceDirection.OPTIMAL_RANGE) {
                    // Optimal fat content is around 15-20%
                    100 - kotlin.math.abs(fat - 17.5) * 2
                } else {
                    fat
                }
            }
            CriterionType.ALLERGEN_FREE -> {
                if (product.allergens.isEmpty()) 100.0 else 0.0
            }
            CriterionType.CERTIFICATIONS -> {
                product.certifications.size * 20.0
            }
            else -> 50.0 // Default middle score
        }
    }
    
    // Shopping Assistant
    
    suspend fun createShoppingList(
        name: String,
        productIds: List<String>
    ): Result<BarcodeShoppingList> = withContext(Dispatchers.IO) {
        try {
            val products = productIds.mapNotNull { getProductById(it) }
            
            val items = products.map { product ->
                ShoppingListItem(
                    product = product,
                    quantity = 1,
                    alternativeProducts = findAlternatives(product)
                )
            }
            
            val shoppingList = BarcodeShoppingList(
                id = UUID.randomUUID().toString(),
                userId = appwriteService.account.get().id,
                name = name,
                items = items,
                stores = findNearbyStores(),
                totalEstimate = calculateTotalEstimate(items),
                createdAt = LocalDateTime.now()
            )
            
            // Save shopping list
            val document = databases.createDocument(
                databaseId = databaseId,
                collectionId = "shopping_lists",
                documentId = shoppingList.id,
                data = shoppingList.toMap()
            )
            
            Result.success(shoppingList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun findAlternatives(product: Product): List<Product> {
        // Find similar products based on category and price range
        return try {
            val response = databases.listDocuments(
                databaseId = databaseId,
                collectionId = "products",
                queries = listOf(
                    Query.equal("category", product.category.name),
                    Query.notEqual("id", product.id),
                    Query.limit(3)
                )
            )
            
            response.documents.mapNotNull { parseProductFromDocument(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private suspend fun findNearbyStores(): List<Store> {
        // This would use location services to find nearby stores
        // For now, return some example stores
        return listOf(
            Store(
                id = "1",
                name = "Fressnapf",
                address = "Hauptstraße 123, 12345 Berlin",
                type = StoreType.PET_STORE,
                priceLevel = PriceLevel.MEDIUM
            ),
            Store(
                id = "2",
                name = "Futterhaus",
                address = "Nebenstraße 45, 12345 Berlin",
                type = StoreType.PET_STORE,
                priceLevel = PriceLevel.BUDGET
            )
        )
    }
    
    private fun calculateTotalEstimate(items: List<ShoppingListItem>): ProductPrice {
        val total = items.sumOf { item ->
            (item.product.variants.firstOrNull()?.price?.amount ?: 0.0) * item.quantity
        }
        
        return ProductPrice(
            amount = total,
            currency = "EUR",
            unit = PriceUnit.PACKAGE
        )
    }
    
    // Inventory Management
    
    suspend fun updateInventory(
        productId: String,
        quantity: Double,
        unit: StockUnit
    ): Result<ProductInventory> = withContext(Dispatchers.IO) {
        try {
            val product = getProductById(productId)
                ?: return@withContext Result.failure(Exception("Produkt nicht gefunden"))
            
            // Get or create inventory record
            var inventory = getInventoryForProduct(productId)
            
            if (inventory == null) {
                inventory = ProductInventory(
                    id = UUID.randomUUID().toString(),
                    userId = appwriteService.account.get().id,
                    product = product,
                    currentStock = StockLevel(
                        quantity = quantity,
                        unit = unit,
                        lastUpdated = LocalDateTime.now()
                    )
                )
            } else {
                inventory = inventory.copy(
                    currentStock = inventory.currentStock.copy(
                        quantity = quantity,
                        lastUpdated = LocalDateTime.now()
                    )
                )
            }
            
            // Calculate consumption rate if we have history
            if (inventory.purchaseHistory.isNotEmpty()) {
                val consumptionRate = calculateConsumptionRate(inventory)
                inventory = inventory.copy(consumptionRate = consumptionRate)
            }
            
            // Save inventory
            val document = if (inventory.id.isEmpty()) {
                databases.createDocument(
                    databaseId = databaseId,
                    collectionId = "product_inventory",
                    documentId = ID.unique(),
                    data = inventory.toMap()
                )
            } else {
                databases.updateDocument(
                    databaseId = databaseId,
                    collectionId = "product_inventory",
                    documentId = inventory.id,
                    data = inventory.toMap()
                )
            }
            
            Result.success(inventory)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun getInventoryForProduct(productId: String): ProductInventory? {
        return try {
            val response = databases.listDocuments(
                databaseId = databaseId,
                collectionId = "product_inventory",
                queries = listOf(
                    Query.equal("userId", appwriteService.account.get().id),
                    Query.equal("productId", productId)
                )
            )
            
            if (response.documents.isNotEmpty()) {
                parseInventoryFromDocument(response.documents.first())
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun calculateConsumptionRate(inventory: ProductInventory): ConsumptionRate {
        // Calculate based on purchase history and current stock
        val sortedHistory = inventory.purchaseHistory.sortedBy { it.date }
        if (sortedHistory.size < 2) {
            return ConsumptionRate()
        }
        
        val firstPurchase = sortedHistory.first()
        val lastPurchase = sortedHistory.last()
        val daysBetween = java.time.Duration.between(firstPurchase.date, lastPurchase.date).toDays()
        
        if (daysBetween == 0L) {
            return ConsumptionRate()
        }
        
        val totalConsumed = sortedHistory.sumOf { it.quantity } - inventory.currentStock.quantity
        val averageDaily = totalConsumed / daysBetween
        
        val daysUntilEmpty = if (averageDaily > 0) {
            (inventory.currentStock.quantity / averageDaily).toInt()
        } else null
        
        // Determine trend
        val recentConsumption = sortedHistory.takeLast(3).sumOf { it.quantity } / 3
        val historicalAverage = sortedHistory.sumOf { it.quantity } / sortedHistory.size
        
        val trend = when {
            recentConsumption > historicalAverage * 1.2 -> ConsumptionTrend.INCREASING
            recentConsumption < historicalAverage * 0.8 -> ConsumptionTrend.DECREASING
            else -> ConsumptionTrend.STABLE
        }
        
        return ConsumptionRate(
            averageDaily = averageDaily,
            unit = inventory.currentStock.unit,
            daysUntilEmpty = daysUntilEmpty,
            trend = trend
        )
    }
    
    // OCR Features
    
    suspend fun extractProductInfo(bitmap: Bitmap): Result<OcrResult> = withContext(Dispatchers.IO) {
        try {
            // ML Kit not available - return mock result
            // TODO: Add ML Kit dependencies or use alternative OCR library
            val startTime = System.currentTimeMillis()
            
            val text = "Hundefutter\nMarke: Test\nInhalt: 800g" // Mock OCR result
            
            val processingTime = System.currentTimeMillis() - startTime
            
            // Extract structured data from text
            val extractedData = extractDataFromText(text)
            
            // Convert bitmap to byte array for storage
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            val imageBytes = stream.toByteArray()
            
            val ocrResult = OcrResult(
                id = UUID.randomUUID().toString(),
                image = imageBytes,
                extractedText = text,
                confidence = 0.85f, // ML Kit doesn't provide overall confidence
                language = "de",
                extractedData = extractedData,
                processingTime = processingTime
            )
            
            Result.success(ocrResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun extractDataFromText(text: String): ExtractedProductData {
        val lines = text.split("\n")
        val extractedData = ExtractedProductData()
        
        // Simple extraction logic - would be more sophisticated in production
        val ingredients = mutableListOf<String>()
        val nutritionalInfo = mutableMapOf<String, String>()
        val warnings = mutableListOf<String>()
        
        lines.forEach { line ->
            when {
                line.contains("Zutaten:", ignoreCase = true) -> {
                    // Extract ingredients
                    val ingredientLine = line.substringAfter("Zutaten:").trim()
                    ingredients.addAll(ingredientLine.split(",").map { it.trim() })
                }
                line.contains("Protein", ignoreCase = true) -> {
                    // Extract protein content
                    val proteinMatch = Regex("\\d+[,.]?\\d*").find(line)
                    proteinMatch?.let {
                        nutritionalInfo["Protein"] = it.value
                    }
                }
                line.contains("Fett", ignoreCase = true) -> {
                    // Extract fat content
                    val fatMatch = Regex("\\d+[,.]?\\d*").find(line)
                    fatMatch?.let {
                        nutritionalInfo["Fett"] = it.value
                    }
                }
                line.contains("Warnung", ignoreCase = true) || 
                line.contains("Achtung", ignoreCase = true) -> {
                    warnings.add(line)
                }
            }
        }
        
        return extractedData.copy(
            ingredients = ingredients,
            nutritionalInfo = nutritionalInfo,
            warnings = warnings
        )
    }
    
    // AI Recommendations
    
    suspend fun getProductRecommendations(
        dogId: String,
        context: RecommendationContext = RecommendationContext.GENERAL
    ): Result<List<ProductRecommendation>> = withContext(Dispatchers.IO) {
        try {
            // Get dog information
            val dog = getDogInfo(dogId)
                ?: return@withContext Result.failure(Exception("Hund nicht gefunden"))
            
            // Get dog's allergies and preferences
            val allergies = getDogAllergies(dogId)
            val preferences = getDogPreferences(dogId)
            
            // Get suitable products
            val products = getSuitableProducts(dog, allergies, preferences)
            
            // Score and rank products
            val recommendations = products.map { product ->
                val factors = mutableListOf<RecommendationFactor>()
                var score = 0.0
                
                // Age appropriateness
                val dogAge = dog.birthDate?.let { 
                    java.time.Period.between(it, LocalDate.now()).years 
                } ?: 0
                if (product.metadata.targetAge?.contains(getAgeGroup(dogAge)) == true) {
                    score += 20
                    factors.add(RecommendationFactor(
                        factor = "Altersgerecht",
                        impact = 20.0,
                        description = "Geeignet für ${getAgeGroup(dogAge)}"
                    ))
                }
                
                // Size appropriateness
                if (product.metadata.targetSize?.contains(getSizeGroup(dog.weight)) == true) {
                    score += 15
                    factors.add(RecommendationFactor(
                        factor = "Größengerecht",
                        impact = 15.0,
                        description = "Geeignet für ${getSizeGroup(dog.weight)}"
                    ))
                }
                
                // Allergen-free
                val hasAllergens = product.allergens.any { allergen ->
                    allergies.any { it.type == allergen.type }
                }
                if (!hasAllergens) {
                    score += 30
                    factors.add(RecommendationFactor(
                        factor = "Allergenfrei",
                        impact = 30.0,
                        description = "Enthält keine bekannten Allergene für Ihren Hund"
                    ))
                }
                
                // Nutritional quality
                product.nutritionalInfo?.let { nutrition ->
                    if (nutrition.protein > 25) {
                        score += 10
                        factors.add(RecommendationFactor(
                            factor = "Hoher Proteingehalt",
                            impact = 10.0,
                            description = "${nutrition.protein}% Protein"
                        ))
                    }
                }
                
                // Certifications
                if (product.certifications.isNotEmpty()) {
                    score += 5 * product.certifications.size
                    factors.add(RecommendationFactor(
                        factor = "Zertifiziert",
                        impact = (5 * product.certifications.size).toDouble(),
                        description = product.certifications.joinToString { it.type.name }
                    ))
                }
                
                ProductRecommendation(
                    id = UUID.randomUUID().toString(),
                    dogId = dogId,
                    recommendedProduct = product,
                    reason = generateRecommendationReason(product, factors),
                    score = score,
                    factors = factors,
                    alternativeProducts = findAlternatives(product),
                    createdAt = LocalDateTime.now()
                )
            }.sortedByDescending { it.score }.take(10)
            
            Result.success(recommendations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun generateRecommendationReason(
        product: Product,
        factors: List<RecommendationFactor>
    ): String {
        val topFactors = factors.sortedByDescending { it.impact }.take(2)
        return "Empfohlen wegen: ${topFactors.joinToString(" und ") { it.factor }}"
    }
    
    // Allergen Detection
    
    suspend fun checkProductForAllergens(
        productId: String,
        dogId: String
    ): Result<AllergenAlert?> = withContext(Dispatchers.IO) {
        try {
            val product = getProductById(productId)
                ?: return@withContext Result.failure(Exception("Produkt nicht gefunden"))
            
            val dogAllergies = getDogAllergies(dogId)
            if (dogAllergies.isEmpty()) {
                return@withContext Result.success(null)
            }
            
            val detectedAllergens = product.allergens.filter { productAllergen ->
                dogAllergies.any { dogAllergy ->
                    dogAllergy.type == productAllergen.type
                }
            }
            
            if (detectedAllergens.isEmpty()) {
                return@withContext Result.success(null)
            }
            
            // Determine severity
            val maxSeverity = detectedAllergens.maxOf { it.severity }
            val alertSeverity = when (maxSeverity) {
                AllergenSeverity.MILD -> AlertSeverity.INFO
                AllergenSeverity.MODERATE -> AlertSeverity.WARNING
                AllergenSeverity.SEVERE -> AlertSeverity.DANGER
                AllergenSeverity.LIFE_THREATENING -> AlertSeverity.CRITICAL
            }
            
            // Find alternative products
            val alternatives = findAllergenFreeAlternatives(product, detectedAllergens)
            
            val alert = AllergenAlert(
                id = UUID.randomUUID().toString(),
                dogId = dogId,
                product = product,
                detectedAllergens = detectedAllergens,
                severity = alertSeverity,
                recommendation = generateAllergenRecommendation(detectedAllergens, alertSeverity),
                alternativeProducts = alternatives
            )
            
            Result.success(alert)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun generateAllergenRecommendation(
        allergens: List<Allergen>,
        severity: AlertSeverity
    ): String {
        return when (severity) {
            AlertSeverity.INFO -> "Dieses Produkt enthält ${allergens.joinToString { it.type.name }}, auf die Ihr Hund mild reagieren könnte."
            AlertSeverity.WARNING -> "Vorsicht: Dieses Produkt enthält ${allergens.joinToString { it.type.name }}. Überwachen Sie Ihren Hund auf Reaktionen."
            AlertSeverity.DANGER -> "Warnung: Dieses Produkt enthält ${allergens.joinToString { it.type.name }} und sollte vermieden werden."
            AlertSeverity.CRITICAL -> "GEFAHR: Dieses Produkt enthält lebensbedrohliche Allergene (${allergens.joinToString { it.type.name }}). NICHT FÜTTERN!"
        }
    }
    
    private suspend fun findAllergenFreeAlternatives(
        product: Product,
        allergens: List<Allergen>
    ): List<Product> {
        return try {
            val response = databases.listDocuments(
                databaseId = databaseId,
                collectionId = "products",
                queries = listOf(
                    Query.equal("category", product.category.name),
                    Query.notEqual("id", product.id),
                    Query.limit(5)
                )
            )
            
            response.documents.mapNotNull { doc ->
                parseProductFromDocument(doc)
            }.filter { alternativeProduct ->
                // Check if alternative doesn't contain the allergens
                alternativeProduct.allergens.none { productAllergen ->
                    allergens.any { it.type == productAllergen.type }
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Analytics
    
    suspend fun getBarcodeAnalytics(
        userId: String,
        period: AnalyticsPeriod = AnalyticsPeriod.MONTHLY
    ): Result<BarcodeAnalytics> = withContext(Dispatchers.IO) {
        try {
            val startDate = when (period) {
                AnalyticsPeriod.DAILY -> LocalDateTime.now().minusDays(1)
                AnalyticsPeriod.WEEKLY -> LocalDateTime.now().minusWeeks(1)
                AnalyticsPeriod.MONTHLY -> LocalDateTime.now().minusMonths(1)
                AnalyticsPeriod.QUARTERLY -> LocalDateTime.now().minusMonths(3)
                AnalyticsPeriod.YEARLY -> LocalDateTime.now().minusYears(1)
                AnalyticsPeriod.CUSTOM -> LocalDateTime.now().minusMonths(1)
            }
            
            // Get scan history
            val scanHistory = getScanHistory(userId, startDate)
            
            // Get purchase history
            val purchaseHistory = getPurchaseHistory(userId, startDate)
            
            // Calculate statistics
            val scanStats = calculateScanStatistics(scanHistory)
            val productStats = calculateProductStatistics(scanHistory)
            val shoppingStats = calculateShoppingStatistics(purchaseHistory)
            val trends = analyzeTrends(scanHistory, purchaseHistory)
            
            val analytics = BarcodeAnalytics(
                id = UUID.randomUUID().toString(),
                userId = userId,
                period = LocalDateTime.now(),
                scanStatistics = scanStats,
                productStatistics = productStats,
                shoppingStatistics = shoppingStats,
                trendAnalysis = trends
            )
            
            Result.success(analytics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Helper functions
    
    private fun mapBarcodeFormat(format: Int): BarcodeFormat {
        // ML Kit constants not available
        // Map based on common format codes
        return when (format) {
            32 -> BarcodeFormat.EAN_8
            64 -> BarcodeFormat.EAN_13
            128 -> BarcodeFormat.UPC_A
            256 -> BarcodeFormat.UPC_E
            2 -> BarcodeFormat.CODE_39
            4 -> BarcodeFormat.CODE_93
            1 -> BarcodeFormat.CODE_128
            8 -> BarcodeFormat.ITF
            16 -> BarcodeFormat.CODABAR
            512 -> BarcodeFormat.QR_CODE
            1024 -> BarcodeFormat.DATA_MATRIX
            2048 -> BarcodeFormat.PDF_417
            4096 -> BarcodeFormat.AZTEC
            else -> BarcodeFormat.EAN_13
        }
    }
    
    private fun determineScanQuality(barcodeValue: String?): ScanQuality {
        // Simple quality determination based on barcode value length
        return when {
            barcodeValue == null -> ScanQuality.POOR
            barcodeValue.length < 8 -> ScanQuality.FAIR
            barcodeValue.length < 13 -> ScanQuality.GOOD
            else -> ScanQuality.EXCELLENT
        }
    }
    
    private fun determineCategory(json: JSONObject): ProductCategory {
        val categories = json.optString("categories", "").toLowerCase()
        return when {
            categories.contains("treat") || categories.contains("snack") -> ProductCategory.TREATS
            categories.contains("wet") || categories.contains("can") -> ProductCategory.WET_FOOD
            categories.contains("supplement") -> ProductCategory.SUPPLEMENTS
            categories.contains("raw") || categories.contains("barf") -> ProductCategory.RAW_FOOD
            else -> ProductCategory.DRY_FOOD
        }
    }
    
    private fun parseProductImages(json: JSONObject): List<ProductImage> {
        val images = mutableListOf<ProductImage>()
        
        json.optString("image_url")?.let { url ->
            if (url.isNotEmpty()) {
                images.add(ProductImage(
                    url = url,
                    type = ImageType.MAIN,
                    source = "OpenFoodFacts"
                ))
            }
        }
        
        json.optString("image_nutrition_url")?.let { url ->
            if (url.isNotEmpty()) {
                images.add(ProductImage(
                    url = url,
                    type = ImageType.NUTRITION_LABEL,
                    source = "OpenFoodFacts"
                ))
            }
        }
        
        return images
    }
    
    private fun parseNutritionalInfo(json: JSONObject): NutritionalInfo? {
        val nutriments = json.optJSONObject("nutriments") ?: return null
        
        return NutritionalInfo(
            servingSize = json.optString("serving_size", "100g"),
            calories = nutriments.optDouble("energy-kcal_100g", 0.0),
            protein = nutriments.optDouble("proteins_100g", 0.0),
            fat = nutriments.optDouble("fat_100g", 0.0),
            carbohydrates = nutriments.optDouble("carbohydrates_100g", 0.0),
            fiber = nutriments.optDouble("fiber_100g", 0.0)
        )
    }
    
    private fun parseIngredients(json: JSONObject): List<Ingredient> {
        val ingredientsText = json.optString("ingredients_text", "")
        if (ingredientsText.isEmpty()) return emptyList()
        
        return ingredientsText.split(",").map { ingredient ->
            Ingredient(
                name = ingredient.trim(),
                source = determineIngredientSource(ingredient)
            )
        }
    }
    
    private fun determineIngredientSource(ingredient: String): IngredientSource {
        val lowerIngredient = ingredient.toLowerCase()
        return when {
            lowerIngredient.contains("fleisch") || lowerIngredient.contains("huhn") ||
            lowerIngredient.contains("rind") || lowerIngredient.contains("fisch") -> IngredientSource.ANIMAL
            lowerIngredient.contains("vitamin") || lowerIngredient.contains("mineral") -> IngredientSource.MINERAL
            else -> IngredientSource.PLANT
        }
    }
    
    private fun parseAllergens(json: JSONObject): List<Allergen> {
        val allergens = mutableListOf<Allergen>()
        val allergensText = json.optString("allergens", "").toLowerCase()
        
        AllergenType.values().forEach { type ->
            if (allergensText.contains(type.name.toLowerCase())) {
                allergens.add(Allergen(
                    type = type,
                    severity = AllergenSeverity.MODERATE
                ))
            }
        }
        
        return allergens
    }
    
    private suspend fun saveScanToHistory(result: BarcodeResult) {
        try {
            val history = BarcodeHistory(
                id = UUID.randomUUID().toString(),
                userId = appwriteService.account.get().id,
                barcode = result.barcode,
                scanTimestamp = result.scanTimestamp,
                scanLocation = result.scanLocation,
                action = ScanAction.VIEW
            )
            
            databases.createDocument(
                databaseId = databaseId,
                collectionId = "barcode_history",
                documentId = history.id,
                data = history.toMap()
            )
        } catch (e: Exception) {
            // Log error but don't fail the scan
        }
    }
    
    private suspend fun saveProductToDatabase(product: Product) {
        try {
            databases.createDocument(
                databaseId = databaseId,
                collectionId = "products",
                documentId = product.id,
                data = product.toMap()
            )
        } catch (e: Exception) {
            // Log error
        }
    }
    
    private suspend fun saveComparison(comparison: ProductComparison) {
        try {
            databases.createDocument(
                databaseId = databaseId,
                collectionId = "product_comparisons",
                documentId = comparison.id,
                data = comparison.toMap()
            )
        } catch (e: Exception) {
            // Log error
        }
    }
    
    private suspend fun getProductById(id: String): Product? {
        return try {
            val document = databases.getDocument(
                databaseId = databaseId,
                collectionId = "products",
                documentId = id
            )
            parseProductFromDocument(document)
        } catch (e: Exception) {
            null
        }
    }
    
    private fun parseProductFromDocument(document: Document<Map<String, Any>>): Product {
        return Product(
            id = document.id,
            barcode = document.data["barcode"] as String,
            name = document.data["name"] as String,
            brand = document.data["brand"] as String,
            category = ProductCategory.valueOf(document.data["category"] as String),
            description = document.data["description"] as String
            // Parse other fields...
        )
    }
    
    private fun parseInventoryFromDocument(document: Document<Map<String, Any>>): ProductInventory {
        // Parse inventory from document
        val productData = document.data["product"] as? Map<String, Any> ?: emptyMap()
        return ProductInventory(
            id = document.id,
            product = Product(
                id = productData["id"] as? String ?: "",
                barcode = productData["barcode"] as? String ?: "",
                name = productData["name"] as? String ?: "",
                brand = productData["brand"] as? String ?: ""
            )
            // Parse other fields...
        )
    }
    
    private suspend fun getDogInfo(dogId: String): Dog? {
        // Fetch dog information
        return null // Placeholder
    }
    
    private suspend fun getDogAllergies(dogId: String): List<Allergen> {
        // Fetch dog's known allergies
        return emptyList() // Placeholder
    }
    
    private suspend fun getDogPreferences(dogId: String): DogPreferences {
        // Fetch dog's food preferences
        return DogPreferences() // Placeholder
    }
    
    private suspend fun getSuitableProducts(
        dog: Dog,
        allergies: List<Allergen>,
        preferences: DogPreferences
    ): List<Product> {
        // Query products suitable for the dog
        return emptyList() // Placeholder
    }
    
    private fun getAgeGroup(age: Int): String {
        return when {
            age < 1 -> "Welpe"
            age < 7 -> "Adult"
            else -> "Senior"
        }
    }
    
    private fun getSizeGroup(weight: Double): String {
        return when {
            weight < 10 -> "Klein"
            weight < 25 -> "Mittel"
            weight < 45 -> "Groß"
            else -> "Sehr groß"
        }
    }
    
    private suspend fun getScanHistory(userId: String, startDate: LocalDateTime): List<BarcodeHistory> {
        // Fetch scan history
        return emptyList() // Placeholder
    }
    
    private suspend fun getPurchaseHistory(userId: String, startDate: LocalDateTime): List<PurchaseRecord> {
        // Fetch purchase history
        return emptyList() // Placeholder
    }
    
    private fun calculateScanStatistics(history: List<BarcodeHistory>): ScanStatistics {
        // Calculate scan statistics
        return ScanStatistics() // Placeholder
    }
    
    private fun calculateProductStatistics(history: List<BarcodeHistory>): ProductStatistics {
        // Calculate product statistics
        return ProductStatistics() // Placeholder
    }
    
    private fun calculateShoppingStatistics(history: List<PurchaseRecord>): ShoppingStatistics {
        // Calculate shopping statistics
        return ShoppingStatistics() // Placeholder
    }
    
    private fun analyzeTrends(
        scanHistory: List<BarcodeHistory>,
        purchaseHistory: List<PurchaseRecord>
    ): TrendAnalysis {
        // Analyze trends
        return TrendAnalysis() // Placeholder
    }
    
    // Extension functions
    
    private fun Product.toMap(): Map<String, Any> = mapOf(
        "barcode" to barcode,
        "name" to name,
        "brand" to brand,
        "manufacturer" to (manufacturer ?: ""),
        "category" to category.name,
        "subCategory" to (subCategory ?: ""),
        "description" to description,
        "images" to images.map { it.toMap() },
        "nutritionalInfo" to (nutritionalInfo?.toMap() ?: emptyMap<String, Any>()),
        "ingredients" to ingredients.map { it.toMap() },
        "allergens" to allergens.map { it.toMap() },
        "certifications" to certifications.map { it.toMap() },
        "variants" to variants.map { it.toMap() },
        "metadata" to metadata.toMap(),
        "source" to source.name,
        "verificationStatus" to verificationStatus.name,
        "lastUpdated" to lastUpdated.toString()
    )
    
    private fun ProductImage.toMap(): Map<String, Any> = mapOf(
        "url" to url,
        "type" to type.name,
        "source" to source,
        "uploadedAt" to uploadedAt.toString()
    )
    
    private fun NutritionalInfo.toMap(): Map<String, Any> = mapOf(
        "servingSize" to servingSize,
        "calories" to calories,
        "protein" to protein,
        "fat" to fat,
        "carbohydrates" to carbohydrates,
        "fiber" to fiber,
        "moisture" to moisture,
        "ash" to ash
    )
    
    private fun Ingredient.toMap(): Map<String, Any> = mapOf(
        "name" to name,
        "percentage" to (percentage ?: 0.0),
        "source" to (source?.name ?: ""),
        "quality" to (quality?.name ?: ""),
        "processing" to (processing?.name ?: "")
    )
    
    private fun Allergen.toMap(): Map<String, Any> = mapOf(
        "type" to type.name,
        "severity" to severity.name,
        "notes" to (notes ?: "")
    )
    
    private fun Certification.toMap(): Map<String, Any> = mapOf(
        "type" to type.name,
        "certifier" to certifier,
        "validUntil" to (validUntil?.toString() ?: ""),
        "certificateNumber" to (certificateNumber ?: "")
    )
    
    private fun ProductVariant.toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "size" to size,
        "flavor" to (flavor ?: ""),
        "price" to (price?.toMap() ?: emptyMap<String, Any>()),
        "barcode" to (barcode ?: ""),
        "availability" to availability.name
    )
    
    private fun ProductPrice.toMap(): Map<String, Any> = mapOf(
        "amount" to amount,
        "currency" to currency,
        "unit" to unit.name,
        "retailer" to (retailer ?: ""),
        "lastUpdated" to lastUpdated.toString()
    )
    
    private fun ProductMetadata.toMap(): Map<String, Any> = mapOf(
        "manufacturerCode" to (manufacturerCode ?: ""),
        "countryOfOrigin" to (countryOfOrigin ?: ""),
        "targetAge" to (targetAge ?: ""),
        "targetBreed" to (targetBreed ?: ""),
        "targetSize" to (targetSize ?: ""),
        "feedingGuidelines" to (feedingGuidelines ?: ""),
        "storageInstructions" to (storageInstructions ?: ""),
        "warnings" to warnings,
        "recyclable" to (recyclable ?: false),
        "sustainabilityScore" to (sustainabilityScore ?: 0)
    )
    
    private fun BarcodeHistory.toMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "dogId" to dogId,
        "barcode" to barcode,
        "scanTimestamp" to scanTimestamp.toString(),
        "scanLocation" to (scanLocation?.toMap() ?: emptyMap<String, Any>()),
        "action" to action.name,
        "quantity" to (quantity ?: 0.0),
        "notes" to (notes ?: "")
    )
    
    private fun ScanLocation.toMap(): Map<String, Any> = mapOf(
        "latitude" to latitude,
        "longitude" to longitude,
        "accuracy" to accuracy,
        "locationName" to (locationName ?: "")
    )
    
    private fun BarcodeShoppingList.toMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "name" to name,
        "items" to items.map { it.toMap() },
        "stores" to stores.map { it.toMap() },
        "totalEstimate" to (totalEstimate?.toMap() ?: emptyMap<String, Any>()),
        "createdAt" to createdAt.toString(),
        "completedAt" to (completedAt?.toString() ?: "")
    )
    
    private fun ShoppingListItem.toMap(): Map<String, Any> = mapOf(
        "productId" to product.id,
        "quantity" to quantity,
        "notes" to (notes ?: ""),
        "purchased" to purchased,
        "actualPrice" to (actualPrice?.toMap() ?: emptyMap<String, Any>())
    )
    
    private fun Store.toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "name" to name,
        "address" to address,
        "location" to (location?.toMap() ?: emptyMap<String, Any>()),
        "type" to type.name,
        "priceLevel" to priceLevel.name,
        "availableProducts" to availableProducts
    )
    
    private fun StoreLocation.toMap(): Map<String, Any> = mapOf(
        "latitude" to latitude,
        "longitude" to longitude,
        "distance" to (distance ?: 0.0)
    )
    
    private fun ProductInventory.toMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "productId" to product.id,
        "currentStock" to currentStock.toMap(),
        "consumptionRate" to (consumptionRate?.toMap() ?: emptyMap<String, Any>()),
        "reorderSettings" to reorderSettings.toMap(),
        "storageLocation" to (storageLocation ?: ""),
        "expirationDate" to (expirationDate?.toString() ?: ""),
        "purchaseHistory" to purchaseHistory.map { it.toMap() }
    )
    
    private fun StockLevel.toMap(): Map<String, Any> = mapOf(
        "quantity" to quantity,
        "unit" to unit.name,
        "lastUpdated" to lastUpdated.toString(),
        "lowStockThreshold" to (lowStockThreshold ?: 0.0)
    )
    
    private fun ConsumptionRate.toMap(): Map<String, Any> = mapOf(
        "averageDaily" to averageDaily,
        "unit" to unit.name,
        "daysUntilEmpty" to (daysUntilEmpty ?: 0),
        "trend" to trend.name
    )
    
    private fun ReorderSettings.toMap(): Map<String, Any> = mapOf(
        "enabled" to enabled,
        "reorderPoint" to (reorderPoint ?: 0.0),
        "reorderQuantity" to (reorderQuantity ?: 0.0),
        "preferredSupplier" to (preferredSupplier ?: ""),
        "autoOrder" to autoOrder,
        "notificationDaysBefore" to notificationDaysBefore
    )
    
    private fun PurchaseRecord.toMap(): Map<String, Any> = mapOf(
        "date" to date.toString(),
        "quantity" to quantity,
        "price" to price.toMap(),
        "store" to store,
        "notes" to (notes ?: "")
    )
    
    private fun ProductComparison.toMap(): Map<String, Any> = mapOf(
        "productIds" to products.map { it.id },
        "comparisonCriteria" to comparisonCriteria.map { it.toMap() },
        "results" to results.toMap(),
        "recommendation" to (recommendation?.toMap() ?: emptyMap<String, Any>()),
        "createdAt" to createdAt.toString()
    )
    
    private fun ComparisonCriterion.toMap(): Map<String, Any> = mapOf(
        "type" to type.name,
        "weight" to weight,
        "preference" to preference.name
    )
    
    private fun ComparisonResults.toMap(): Map<String, Any> = mapOf(
        "scores" to scores,
        "rankings" to rankings.map { it.toMap() },
        "highlights" to highlights,
        "warnings" to warnings
    )
    
    private fun ProductRanking.toMap(): Map<String, Any> = mapOf(
        "productId" to productId,
        "rank" to rank,
        "score" to score,
        "pros" to pros,
        "cons" to cons
    )
    
    private fun ComparisonRecommendation.toMap(): Map<String, Any> = mapOf(
        "recommendedProductId" to recommendedProductId,
        "reason" to reason,
        "confidence" to confidence,
        "alternatives" to alternatives
    )
    
    // Additional methods for BarcodeViewModel
    
    suspend fun getUserInventory(userId: String): Result<List<ProductInventory>> = withContext(Dispatchers.IO) {
    try {
        val documents = databases.listDocuments(
            databaseId = databaseId,
            collectionId = "inventory",
            queries = listOf(Query.equal("userId", userId))
        )
        
        val inventory = documents.documents.mapNotNull { document ->
            val productId = document.data["productId"] as String
            val product = getProductById(productId) ?: return@mapNotNull null
            
            ProductInventory(
                id = document.id,
                userId = document.data["userId"] as String,
                product = product,
                currentStock = StockLevel(
                    quantity = (document.data["quantity"] as Number).toDouble(),
                    unit = StockUnit.valueOf(document.data["unit"] as String),
                    lastUpdated = LocalDateTime.parse(document.data["lastUpdated"] as String)
                ),
                reorderSettings = document.data["reorderSettings"]?.let { 
                    parseReorderSettings(it as Map<String, Any?>)
                } ?: ReorderSettings(),
                storageLocation = document.data["location"] as? String,
                expirationDate = document.data["expiryDate"]?.let { LocalDateTime.parse(it as String) }
            )
        }
        
        Result.success(inventory)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

    suspend fun getBarcodeHistory(userId: String, dogId: String, limit: Int = 10): Result<List<BarcodeHistory>> = withContext(Dispatchers.IO) {
    try {
        val documents = databases.listDocuments(
            databaseId = databaseId,
            collectionId = "barcode_history",
            queries = listOf(
                Query.equal("userId", userId),
                Query.equal("dogId", dogId),
                Query.orderDesc("timestamp"),
                Query.limit(limit)
            )
        )
        
        val history = documents.documents.map { document ->
            BarcodeHistory(
                id = document.id,
                userId = document.data["userId"] as String,
                dogId = document.data["dogId"] as String,
                barcode = document.data["barcode"] as String,
                product = null, // We only store basic info in history
                scanTimestamp = LocalDateTime.parse(document.data["timestamp"] as String),
                action = ScanAction.valueOf(document.data["action"] as String),
                notes = document.data["notes"] as? String
            )
        }
        
        Result.success(history)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

    suspend fun getProductByBarcode(barcode: String): Result<Product> = lookupProduct(barcode)

    suspend fun recordBarcodeHistory(
    userId: String,
    dogId: String,
    barcode: String,
    product: Product,
    action: ScanAction
): Result<BarcodeHistory> = withContext(Dispatchers.IO) {
    try {
        val document = databases.createDocument(
            databaseId = databaseId,
            collectionId = "barcode_history",
            documentId = ID.unique(),
            data = mapOf(
                "userId" to userId,
                "dogId" to dogId,
                "barcode" to barcode,
                "productName" to product.name,
                "productBrand" to product.brand,
                "action" to action.name,
                "timestamp" to LocalDateTime.now().toString()
            )
        )
        
        val history = BarcodeHistory(
            id = document.id,
            userId = userId,
            dogId = dogId,
            barcode = barcode,
            product = product,
            scanTimestamp = LocalDateTime.now(),
            action = action
        )
        
        Result.success(history)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

    suspend fun addToInventory(
    userId: String,
    product: Product,
    quantity: Double,
    unit: StockUnit
): Result<ProductInventory> = withContext(Dispatchers.IO) {
    try {
        // First save the product if it doesn't exist
        if (product.id.isEmpty()) {
            saveProduct(product)
        }
        
        val document = databases.createDocument(
            databaseId = databaseId,
            collectionId = "inventory",
            documentId = ID.unique(),
            data = mapOf(
                "userId" to userId,
                "productId" to product.id,
                "quantity" to quantity,
                "unit" to unit.name,
                "lastUpdated" to LocalDateTime.now().toString()
            )
        )
        
        val inventory = ProductInventory(
            id = document.id,
            userId = userId,
            product = product,
            currentStock = StockLevel(
                quantity = quantity,
                unit = unit,
                lastUpdated = LocalDateTime.now()
            )
        )
        
        Result.success(inventory)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

    suspend fun updateInventoryQuantity(inventoryId: String, newQuantity: Double): Result<Unit> = withContext(Dispatchers.IO) {
    try {
        databases.updateDocument(
            databaseId = databaseId,
            collectionId = "inventory",
            documentId = inventoryId,
            data = mapOf(
                "quantity" to newQuantity,
                "lastUpdated" to LocalDateTime.now().toString()
            )
        )
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

    suspend fun setReorderSettings(inventoryId: String, settings: ReorderSettings): Result<Unit> = withContext(Dispatchers.IO) {
    try {
        databases.updateDocument(
            databaseId = databaseId,
            collectionId = "inventory",
            documentId = inventoryId,
            data = mapOf(
                "reorderSettings" to mapOf(
                    "reorderPoint" to settings.reorderPoint,
                    "reorderQuantity" to settings.reorderQuantity,
                    "preferredSupplier" to settings.preferredSupplier,
                    "autoOrder" to settings.autoOrder
                ),
                "lastUpdated" to LocalDateTime.now().toString()
            )
        )
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

    suspend fun searchProducts(query: String): Result<List<Product>> = withContext(Dispatchers.IO) {
    try {
        val documents = databases.listDocuments(
            databaseId = databaseId,
            collectionId = "products",
            queries = listOf(
                Query.search("name", query),
                Query.limit(10)
            )
        )
        
        val products = documents.documents.map { parseProductFromDocument(it) }
        Result.success(products)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

    suspend fun checkProductAllergens(dogId: String, product: Product): Result<List<AllergenAlert>> {
        val result = checkProductForAllergens(dogId, product.barcode)
        return result.map { alert ->
            if (alert != null) listOf(alert) else emptyList()
        }
    }

    private fun parseReorderSettings(data: Map<String, Any?>): ReorderSettings {
        return ReorderSettings(
            reorderPoint = (data["reorderPoint"] as? Number)?.toDouble(),
            reorderQuantity = (data["reorderQuantity"] as Number).toDouble(),
            preferredSupplier = data["preferredSupplier"] as? String,
            autoOrder = data["autoOrder"] as? Boolean ?: false
        )
    }
}

// Placeholder classes
// Removed duplicate Dog class - using the one from com.example.snacktrack.data.model
data class DogPreferences(val preferredFlavors: List<String> = emptyList())
enum class RecommendationContext { GENERAL, HEALTH_FOCUSED, BUDGET, PREMIUM }