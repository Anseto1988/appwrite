package com.example.snacktrack.data.model

import java.time.LocalDateTime

/**
 * Advanced barcode features models
 */

// Barcode Scanning

data class BarcodeResult(
    val id: String = "",
    val barcode: String = "",
    val format: BarcodeFormat = BarcodeFormat.EAN_13,
    val scanTimestamp: LocalDateTime = LocalDateTime.now(),
    val scanLocation: ScanLocation? = null,
    val scanQuality: ScanQuality = ScanQuality.GOOD,
    val rawData: String? = null,
    val metadata: BarcodeMetadata = BarcodeMetadata()
)

enum class BarcodeFormat {
    EAN_8,
    EAN_13,
    UPC_A,
    UPC_E,
    CODE_39,
    CODE_93,
    CODE_128,
    ITF,
    CODABAR,
    QR_CODE,
    DATA_MATRIX,
    PDF_417,
    AZTEC
}

data class ScanLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val accuracy: Double = 0.0,
    val locationName: String? = null
)

enum class ScanQuality {
    POOR,
    FAIR,
    GOOD,
    EXCELLENT
}

data class BarcodeMetadata(
    val scanDuration: Long = 0, // milliseconds
    val lightingCondition: LightingCondition = LightingCondition.NORMAL,
    val cameraFocus: Boolean = true,
    val multipleScans: Int = 1,
    val confidenceScore: Float = 0.0f
)

enum class LightingCondition {
    DARK,
    LOW,
    NORMAL,
    BRIGHT,
    GLARE
}

// Product Database

data class Product(
    val id: String = "",
    val barcode: String = "",
    val name: String = "",
    val brand: String = "",
    val manufacturer: String? = null,
    val category: ProductCategory = ProductCategory.DRY_FOOD,
    val subCategory: String? = null,
    val description: String = "",
    val images: List<ProductImage> = emptyList(),
    val nutritionalInfo: NutritionalInfo? = null,
    val ingredients: List<Ingredient> = emptyList(),
    val allergens: List<Allergen> = emptyList(),
    val certifications: List<Certification> = emptyList(),
    val variants: List<ProductVariant> = emptyList(),
    val metadata: ProductMetadata = ProductMetadata(),
    val source: DataSource = DataSource.USER_CONTRIBUTED,
    val verificationStatus: VerificationStatus = VerificationStatus.UNVERIFIED,
    val lastUpdated: LocalDateTime = LocalDateTime.now()
)

enum class ProductCategory {
    DRY_FOOD,
    WET_FOOD,
    TREATS,
    SUPPLEMENTS,
    RAW_FOOD,
    MEDICATION,
    GROOMING,
    TOYS,
    ACCESSORIES,
    OTHER
}

data class ProductImage(
    val url: String = "",
    val type: ImageType = ImageType.MAIN,
    val source: String = "",
    val uploadedAt: LocalDateTime = LocalDateTime.now()
)

enum class ImageType {
    MAIN,
    NUTRITION_LABEL,
    INGREDIENTS,
    BARCODE,
    PACKAGING,
    OTHER
}

data class NutritionalInfo(
    val servingSize: String = "",
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val fat: Double = 0.0,
    val carbohydrates: Double = 0.0,
    val fiber: Double = 0.0,
    val moisture: Double = 0.0,
    val ash: Double = 0.0,
    val vitamins: Map<String, NutrientValue> = emptyMap(),
    val minerals: Map<String, NutrientValue> = emptyMap(),
    val additionalNutrients: Map<String, NutrientValue> = emptyMap(),
    val guaranteedAnalysis: GuaranteedAnalysis? = null
)

data class NutrientValue(
    val amount: Double = 0.0,
    val unit: String = "",
    val dailyValue: Double? = null
)

data class GuaranteedAnalysis(
    val crudeProteinMin: Double = 0.0,
    val crudeProteinMax: Double? = null,
    val crudeFatMin: Double = 0.0,
    val crudeFatMax: Double? = null,
    val crudeFiberMax: Double = 0.0,
    val moistureMax: Double = 0.0,
    val ashMax: Double? = null
)

data class Ingredient(
    val name: String = "",
    val percentage: Double? = null,
    val source: IngredientSource? = null,
    val quality: IngredientQuality? = null,
    val processing: ProcessingMethod? = null,
    val allergenInfo: AllergenInfo? = null
)

enum class IngredientSource {
    ANIMAL,
    PLANT,
    SYNTHETIC,
    MINERAL
}

enum class IngredientQuality {
    HUMAN_GRADE,
    FEED_GRADE,
    ORGANIC,
    NON_GMO,
    LOCALLY_SOURCED
}

enum class ProcessingMethod {
    RAW,
    DEHYDRATED,
    FREEZE_DRIED,
    COOKED,
    RENDERED,
    EXTRACTED
}

data class AllergenInfo(
    val isAllergen: Boolean = false,
    val commonAllergen: Boolean = false,
    val allergenType: AllergenType? = null,
    val crossContamination: Boolean = false
)

enum class AllergenType {
    CHICKEN,
    BEEF,
    LAMB,
    FISH,
    EGGS,
    DAIRY,
    WHEAT,
    CORN,
    SOY,
    PEANUTS,
    TREE_NUTS,
    SHELLFISH,
    OTHER
}

data class Allergen(
    val type: AllergenType,
    val severity: AllergenSeverity = AllergenSeverity.MODERATE,
    val notes: String? = null
)

enum class AllergenSeverity {
    MILD,
    MODERATE,
    SEVERE,
    LIFE_THREATENING
}

data class Certification(
    val type: CertificationType,
    val certifier: String = "",
    val validUntil: LocalDateTime? = null,
    val certificateNumber: String? = null
)

enum class CertificationType {
    ORGANIC,
    NON_GMO,
    FAIR_TRADE,
    SUSTAINABLE,
    CRUELTY_FREE,
    ISO_9001,
    HACCP,
    FDA_APPROVED,
    EU_APPROVED,
    OTHER
}

data class ProductVariant(
    val id: String = "",
    val size: String = "",
    val flavor: String? = null,
    val price: ProductPrice? = null,
    val barcode: String? = null,
    val availability: AvailabilityStatus = AvailabilityStatus.AVAILABLE
)

data class ProductPrice(
    val amount: Double = 0.0,
    val currency: String = "EUR",
    val unit: PriceUnit = PriceUnit.PACKAGE,
    val retailer: String? = null,
    val lastUpdated: LocalDateTime = LocalDateTime.now()
)

enum class PriceUnit {
    PACKAGE,
    KILOGRAM,
    POUND,
    LITER,
    PIECE
}

enum class AvailabilityStatus {
    AVAILABLE,
    LIMITED,
    OUT_OF_STOCK,
    DISCONTINUED,
    SEASONAL
}

data class ProductMetadata(
    val manufacturerCode: String? = null,
    val countryOfOrigin: String? = null,
    val targetAge: String? = null,
    val targetBreed: String? = null,
    val targetSize: String? = null,
    val feedingGuidelines: String? = null,
    val storageInstructions: String? = null,
    val warnings: List<String> = emptyList(),
    val recyclable: Boolean? = null,
    val sustainabilityScore: Int? = null
)

enum class DataSource {
    OFFICIAL_DATABASE,
    MANUFACTURER,
    RETAILER,
    USER_CONTRIBUTED,
    AI_EXTRACTED,
    MANUAL_ENTRY
}

enum class VerificationStatus {
    UNVERIFIED,
    COMMUNITY_VERIFIED,
    MANUFACTURER_VERIFIED,
    EXPERT_VERIFIED,
    DISPUTED
}

// Barcode History

data class BarcodeHistory(
    val id: String = "",
    val userId: String = "",
    val dogId: String = "",
    val barcode: String = "",
    val product: Product? = null,
    val scanTimestamp: LocalDateTime = LocalDateTime.now(),
    val scanLocation: ScanLocation? = null,
    val action: ScanAction = ScanAction.VIEW,
    val quantity: Double? = null,
    val notes: String? = null
)

enum class ScanAction {
    VIEW,
    ADD_TO_INVENTORY,
    PURCHASE,
    FEED,
    COMPARE,
    PRICE_CHECK,
    ALLERGY_CHECK
}

// Product Comparison

data class ProductComparison(
    val id: String = "",
    val products: List<Product> = emptyList(),
    val comparisonCriteria: List<ComparisonCriterion> = emptyList(),
    val results: ComparisonResults = ComparisonResults(),
    val recommendation: ComparisonRecommendation? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class ComparisonCriterion(
    val type: CriterionType,
    val weight: Double = 1.0,
    val preference: PreferenceDirection = PreferenceDirection.HIGHER_BETTER
)

enum class CriterionType {
    PRICE,
    PROTEIN_CONTENT,
    FAT_CONTENT,
    CALORIES,
    INGREDIENT_QUALITY,
    ALLERGEN_FREE,
    CERTIFICATIONS,
    USER_RATINGS,
    AVAILABILITY,
    SUSTAINABILITY
}

enum class PreferenceDirection {
    HIGHER_BETTER,
    LOWER_BETTER,
    OPTIMAL_RANGE
}

data class ComparisonResults(
    val scores: Map<String, Double> = emptyMap(), // productId -> score
    val rankings: List<ProductRanking> = emptyList(),
    val highlights: Map<String, List<String>> = emptyMap(), // productId -> highlights
    val warnings: Map<String, List<String>> = emptyMap() // productId -> warnings
)

data class ProductRanking(
    val productId: String = "",
    val rank: Int = 0,
    val score: Double = 0.0,
    val pros: List<String> = emptyList(),
    val cons: List<String> = emptyList()
)

data class ComparisonRecommendation(
    val recommendedProductId: String = "",
    val reason: String = "",
    val confidence: Double = 0.0,
    val alternatives: List<String> = emptyList()
)

// Shopping Assistant

data class ShoppingList(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val items: List<ShoppingListItem> = emptyList(),
    val stores: List<Store> = emptyList(),
    val totalEstimate: ProductPrice? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val completedAt: LocalDateTime? = null
)

data class ShoppingListItem(
    val product: Product,
    val quantity: Int = 1,
    val notes: String? = null,
    val purchased: Boolean = false,
    val actualPrice: ProductPrice? = null,
    val alternativeProducts: List<Product> = emptyList()
)

data class Store(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val location: StoreLocation? = null,
    val type: StoreType = StoreType.PET_STORE,
    val priceLevel: PriceLevel = PriceLevel.MEDIUM,
    val availableProducts: List<String> = emptyList(), // product IDs
    val openingHours: Map<String, OpeningHours> = emptyMap()
)

data class StoreLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val distance: Double? = null // from user
)

enum class StoreType {
    PET_STORE,
    SUPERMARKET,
    VETERINARY_CLINIC,
    ONLINE,
    SPECIALTY_STORE,
    WAREHOUSE
}

enum class PriceLevel {
    BUDGET,
    MEDIUM,
    PREMIUM,
    LUXURY
}

data class OpeningHours(
    val open: String = "",
    val close: String = "",
    val isOpen: Boolean = true
)

// Inventory Management

data class ProductInventory(
    val id: String = "",
    val userId: String = "",
    val product: Product,
    val currentStock: StockLevel = StockLevel(),
    val consumptionRate: ConsumptionRate? = null,
    val reorderSettings: ReorderSettings = ReorderSettings(),
    val storageLocation: String? = null,
    val expirationDate: LocalDateTime? = null,
    val purchaseHistory: List<PurchaseRecord> = emptyList()
)

data class StockLevel(
    val quantity: Double = 0.0,
    val unit: StockUnit = StockUnit.PACKAGES,
    val lastUpdated: LocalDateTime = LocalDateTime.now(),
    val lowStockThreshold: Double? = null
)

enum class StockUnit {
    PACKAGES,
    KILOGRAMS,
    POUNDS,
    CANS,
    BAGS,
    PIECES
}

data class ConsumptionRate(
    val averageDaily: Double = 0.0,
    val unit: StockUnit = StockUnit.KILOGRAMS,
    val daysUntilEmpty: Int? = null,
    val trend: ConsumptionTrend = ConsumptionTrend.STABLE
)

enum class ConsumptionTrend {
    INCREASING,
    STABLE,
    DECREASING,
    VARIABLE
}

data class ReorderSettings(
    val enabled: Boolean = false,
    val reorderPoint: Double? = null,
    val reorderQuantity: Double? = null,
    val preferredSupplier: String? = null,
    val autoOrder: Boolean = false,
    val notificationDaysBefore: Int = 7
)

data class PurchaseRecord(
    val date: LocalDateTime = LocalDateTime.now(),
    val quantity: Double = 0.0,
    val price: ProductPrice,
    val store: String = "",
    val notes: String? = null
)

// Barcode Analytics

data class BarcodeAnalytics(
    val id: String = "",
    val userId: String = "",
    val period: LocalDateTime = LocalDateTime.now(),
    val scanStatistics: ScanStatistics = ScanStatistics(),
    val productStatistics: ProductStatistics = ProductStatistics(),
    val shoppingStatistics: ShoppingStatistics = ShoppingStatistics(),
    val trendAnalysis: TrendAnalysis = TrendAnalysis()
)

data class ScanStatistics(
    val totalScans: Int = 0,
    val uniqueProducts: Int = 0,
    val scansByCategory: Map<ProductCategory, Int> = emptyMap(),
    val scansByTime: Map<String, Int> = emptyMap(), // hour -> count
    val scanLocations: List<ScanLocation> = emptyList(),
    val averageScanQuality: ScanQuality = ScanQuality.GOOD,
    val mostScannedProducts: List<ProductScanCount> = emptyList()
)

data class ProductScanCount(
    val productId: String = "",
    val productName: String = "",
    val scanCount: Int = 0,
    val lastScanned: LocalDateTime = LocalDateTime.now()
)

data class ProductStatistics(
    val totalProducts: Int = 0,
    val productsByCategory: Map<ProductCategory, Int> = emptyMap(),
    val averageRating: Double = 0.0,
    val priceRange: PriceRange = PriceRange(),
    val topBrands: List<BrandCount> = emptyList(),
    val allergenFrequency: Map<AllergenType, Int> = emptyMap()
)

data class PriceRange(
    val min: Double = 0.0,
    val max: Double = 0.0,
    val average: Double = 0.0,
    val currency: String = "EUR"
)

data class BrandCount(
    val brand: String = "",
    val count: Int = 0,
    val percentage: Double = 0.0
)

data class ShoppingStatistics(
    val totalPurchases: Int = 0,
    val totalSpent: Double = 0.0,
    val averageBasketSize: Double = 0.0,
    val favoriteStores: List<StoreVisit> = emptyList(),
    val savingsFromAlternatives: Double = 0.0,
    val purchaseFrequency: Map<String, Int> = emptyMap() // dayOfWeek -> count
)

data class StoreVisit(
    val storeId: String = "",
    val storeName: String = "",
    val visitCount: Int = 0,
    val totalSpent: Double = 0.0
)

data class TrendAnalysis(
    val emergingProducts: List<TrendingProduct> = emptyList(),
    val decliningProducts: List<TrendingProduct> = emptyList(),
    val seasonalTrends: Map<String, List<String>> = emptyMap(), // season -> productIds
    val priceFluctuations: List<PriceFluctuation> = emptyList(),
    val brandSwitching: List<BrandSwitch> = emptyList()
)

data class TrendingProduct(
    val productId: String = "",
    val productName: String = "",
    val trendScore: Double = 0.0,
    val growthRate: Double = 0.0
)

data class PriceFluctuation(
    val productId: String = "",
    val originalPrice: Double = 0.0,
    val currentPrice: Double = 0.0,
    val changePercent: Double = 0.0,
    val date: LocalDateTime = LocalDateTime.now()
)

data class BrandSwitch(
    val fromBrand: String = "",
    val toBrand: String = "",
    val reason: String = "",
    val date: LocalDateTime = LocalDateTime.now()
)

// AI Features

data class ProductRecommendation(
    val id: String = "",
    val dogId: String = "",
    val recommendedProduct: Product,
    val reason: String = "",
    val score: Double = 0.0,
    val factors: List<RecommendationFactor> = emptyList(),
    val alternativeProducts: List<Product> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class RecommendationFactor(
    val factor: String = "",
    val impact: Double = 0.0,
    val description: String = ""
)

data class AllergenAlert(
    val id: String = "",
    val dogId: String = "",
    val product: Product,
    val detectedAllergens: List<Allergen> = emptyList(),
    val severity: AlertSeverity = AlertSeverity.WARNING,
    val recommendation: String = "",
    val alternativeProducts: List<Product> = emptyList()
)

enum class AlertSeverity {
    INFO,
    WARNING,
    DANGER,
    CRITICAL
}

// OCR Features

data class OcrResult(
    val id: String = "",
    val image: ByteArray,
    val extractedText: String = "",
    val confidence: Float = 0.0f,
    val language: String = "de",
    val extractedData: ExtractedProductData = ExtractedProductData(),
    val processingTime: Long = 0 // milliseconds
)

data class ExtractedProductData(
    val productName: String? = null,
    val brand: String? = null,
    val ingredients: List<String> = emptyList(),
    val nutritionalInfo: Map<String, String> = emptyMap(),
    val warnings: List<String> = emptyList(),
    val expirationDate: String? = null,
    val batchNumber: String? = null
)