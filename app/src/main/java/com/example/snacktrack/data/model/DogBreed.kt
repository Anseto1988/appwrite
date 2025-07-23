package com.example.snacktrack.data.model

/**
 * Datenmodell für Hunderassen
 */
data class DogBreed(
    val id: String,
    val name: String,
    val groesse: String, // Klein, Mittel, Groß, Sehr groß
    val gewichtMin: Int?,
    val gewichtMax: Int?,
    val aktivitaetslevel: String? // Niedrig, Mittel, Hoch, Sehr hoch
) {
    /**
     * Gibt eine Beschreibung der Rasse zurück
     */
    fun getDescription(): String {
        val weightRange = if (gewichtMin != null && gewichtMax != null) {
            "$gewichtMin-$gewichtMax kg"
        } else {
            "Gewicht variiert"
        }
        
        return "$groesse • $weightRange" + if (aktivitaetslevel != null) " • $aktivitaetslevel Aktivität" else ""
    }
}
