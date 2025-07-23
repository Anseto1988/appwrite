package com.example.snacktrack.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Utility-Klasse für Datumsformatierung und -validierung
 */
object DateUtils {
    
    // Formatter für deutsches Datumsformat TT.MM.JJJJ
    private val GERMAN_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    
    // Formatter für ISO-Datum (für Appwrite-Speicherung)
    private val ISO_DATE_FORMATTER = DateTimeFormatter.ISO_DATE
    
    /**
     * Konvertiert ein deutsches Datum (TT.MM.JJJJ) zu LocalDate
     * @param germanDateString Datum im Format TT.MM.JJJJ
     * @return LocalDate oder null bei ungültigem Format
     */
    fun parseGermanDate(germanDateString: String): LocalDate? {
        return try {
            if (germanDateString.isBlank()) return null
            LocalDate.parse(germanDateString, GERMAN_DATE_FORMATTER)
        } catch (e: DateTimeParseException) {
            null
        }
    }
    
    /**
     * Konvertiert LocalDate zu deutschem Datumsformat (TT.MM.JJJJ)
     * @param date LocalDate
     * @return String im Format TT.MM.JJJJ
     */
    fun formatToGerman(date: LocalDate): String {
        return date.format(GERMAN_DATE_FORMATTER)
    }
    
    /**
     * Konvertiert LocalDate zu ISO-Format für Appwrite
     * @param date LocalDate
     * @return String im ISO-Format (JJJJ-MM-TT)
     */
    fun formatToISO(date: LocalDate): String {
        return date.format(ISO_DATE_FORMATTER)
    }
    
    /**
     * Konvertiert ISO-Datum zu LocalDate
     * @param isoDateString Datum im ISO-Format (JJJJ-MM-TT)
     * @return LocalDate oder null bei ungültigem Format
     */
    fun parseISODate(isoDateString: String): LocalDate? {
        return try {
            if (isoDateString.isBlank()) return null
            LocalDate.parse(isoDateString, ISO_DATE_FORMATTER)
        } catch (e: DateTimeParseException) {
            null
        }
    }
    
    /**
     * Validiert deutsches Datumsformat und gibt Fehlermeldung zurück
     * @param germanDateString Datum im Format TT.MM.JJJJ
     * @return null wenn gültig, sonst Fehlermeldung
     */
    fun validateGermanDate(germanDateString: String): String? {
        if (germanDateString.isBlank()) return null
        
        // Prüfe grundlegendes Format
        if (!germanDateString.matches(Regex("\\d{2}\\.\\d{2}\\.\\d{4}"))) {
            return "Bitte verwende das Format TT.MM.JJJJ (z.B. 15.03.2020)"
        }
        
        // Prüfe ob Datum parsebar ist
        val date = parseGermanDate(germanDateString)
        if (date == null) {
            return "Ungültiges Datum. Bitte prüfe Tag und Monat."
        }
        
        // Prüfe ob Datum in der Zukunft liegt
        if (date.isAfter(LocalDate.now())) {
            return "Das Geburtsdatum darf nicht in der Zukunft liegen."
        }
        
        // Prüfe ob Datum zu weit in der Vergangenheit liegt (max. 30 Jahre)
        if (date.isBefore(LocalDate.now().minusYears(30))) {
            return "Das Geburtsdatum ist zu weit in der Vergangenheit."
        }
        
        return null // Gültiges Datum
    }
    
    /**
     * Formatiert Eingabe automatisch während der Texteingabe
     * Fügt automatisch Punkte ein und begrenzt auf 10 Zeichen
     * @param input Roheingabe des Benutzers
     * @return Formatierte Eingabe
     */
    fun formatInputWhileTyping(input: String): String {
        // Entferne alle Nicht-Ziffern außer bereits vorhandenen Punkten
        val digitsOnly = input.filter { it.isDigit() }
        
        // Begrenze auf maximal 8 Ziffern (TTMMJJJJ)
        val limitedDigits = digitsOnly.take(8)
        
        return when (limitedDigits.length) {
            0 -> ""
            1, 2 -> limitedDigits
            3, 4 -> "${limitedDigits.substring(0, 2)}.${limitedDigits.substring(2)}"
            5, 6, 7, 8 -> "${limitedDigits.substring(0, 2)}.${limitedDigits.substring(2, 4)}.${limitedDigits.substring(4)}"
            else -> "${limitedDigits.substring(0, 2)}.${limitedDigits.substring(2, 4)}.${limitedDigits.substring(4, 8)}"
        }
    }
}
