package com.example.snacktrack.data.model

/**
 * Repräsentiert einen Eintrag in der Futterdatenbank
 */
data class Food(
    val id: String = "",
    val ean: String = "",
    val brand: String = "",
    val product: String = "",
    val protein: Double = 0.0,
    val fat: Double = 0.0,
    val crudeFiber: Double = 0.0,
    val rawAsh: Double = 0.0,
    val moisture: Double = 0.0,
    val additives: Map<String, String> = emptyMap(),
    val imageUrl: String? = null
) {
    /**
     * Berechnet die Kohlenhydrate (NFE - Nitrogen-Free Extract)
     * NFE = 100 - (Protein + Fett + Rohfaser + Rohasche + Feuchtigkeit)
     */
    val carbs: Double
        get() = 100 - (protein + fat + crudeFiber + rawAsh + moisture)
    
    /**
     * Berechnet die Kalorien pro 100g nach Atwater
     * Kcal/100g = (Protein × 3,5) + (Fett × 8,5) + (NFE × 3,5)
     */
    val kcalPer100g: Double
        get() = (protein * 3.5) + (fat * 8.5) + (carbs * 3.5)
    
    /**
     * Name des Futters (Alias für product)
     */
    val name: String
        get() = product
    
    /**
     * Kalorien pro Gramm
     */
    val caloriesPerGram: Double
        get() = kcalPer100g / 100.0
    
    /**
     * Typ des Futters (Trocken, Nass, Snack)
     */
    val type: String = "Unbekannt"
    
    /**
     * Barcode (Alias für ean)
     */
    val barcode: String
        get() = ean
    
    /**
     * Detaillierte Nährstoffinformationen
     */
    val nutritionalInfo: Map<String, Double>
        get() = mapOf(
            "protein" to protein,
            "fat" to fat,
            "crudeFiber" to crudeFiber,
            "rawAsh" to rawAsh,
            "moisture" to moisture,
            "carbs" to carbs
        )
} 