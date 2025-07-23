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
} 