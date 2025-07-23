# Hunderassen AutoComplete Implementation

## Übersicht
Diese Implementierung fügt eine Live-Suchfunktion für Hunderassen in der AddEditDogScreen hinzu. Benutzer erhalten beim Eintippen von Rassennamen automatisch Vorschläge aus der Appwrite-Datenbank.

## Komponenten

### 1. Datenbank-Setup (Appwrite)
- **Collection**: `hunderassen` mit 130+ Hunderassen
- **Attribute**:
  - `name` (String, required): Deutscher Name der Rasse
  - `groesse` (Enum): Klein, Mittel, Groß, Sehr groß
  - `gewicht_min`/`gewicht_max` (Integer): Gewichtsbereich in kg
  - `aktivitaetslevel` (Enum): Niedrig, Mittel, Hoch, Sehr hoch

### 2. Indexes für Performance
- `name_fulltext`: Fulltext-Index für Suchfunktion
- `name_index`: Standard-Index für Name-Feld
- `groesse_index`: Index für Größenfilterung
- `breed_compound_index`: Zusammengesetzter Index für name+groesse

### 3. Datenmodell (`DogBreed.kt`)
```kotlin
data class DogBreed(
    val id: String,
    val name: String,
    val groesse: String,
    val gewichtMin: Int?,
    val gewichtMax: Int?,
    val aktivitaetslevel: String?
)
```

### 4. Repository (`DogBreedRepository.kt`)
- Implementiert robuste Suchlogik mit mehreren Fallback-Strategien:
  1. Fulltext-Suche (primär)
  2. StartsWith-Suche (fallback)
  3. Contains-Suche mit lokalem Filtering (letzter fallback)

### 5. ViewModel (`DogBreedSearchViewModel.kt`)
- Verwaltet Search-State und Debouncing (300ms)
- Handhabt Loading-States und Suggestion-Visibility
- Implementiert automatische Cleanup

### 6. UI-Komponente (`DogBreedAutocomplete.kt`)
- Material Design 3 konforme AutoComplete-Komponente
- Zeigt Rasse-Details (Größe, Gewicht, Aktivitätslevel) in Vorschlägen
- Responsive Design mit Keyboard-Integration

### 7. Integration in AddEditDogScreen
- Ersetzt das einfache TextField durch die AutoComplete-Komponente
- Implementiert Focus-Management und Suggestion-Handling
- Behält bestehende Validierung und UX bei

## Features

### Intelligente Suche
- **Debouncing**: 300ms Verzögerung um API-Calls zu reduzieren
- **Minimum Query Length**: 2 Zeichen bevor Suche startet
- **Multiple Search Strategies**: Fallback-System für beste Ergebnisse
- **Case-Insensitive**: Funktioniert unabhängig von Groß-/Kleinschreibung

### UX Features
- **Loading Indicator**: Zeigt Suchfortschritt an
- **Clear Button**: Schnelles Leeren des Eingabefelds
- **Rich Suggestions**: Zeigt Rasse-Details in Vorschlägen
- **Keyboard Navigation**: Optimiert für mobile Eingabe
- **Responsive Design**: Dropdown passt sich an verfügbaren Platz an

### Performance
- **Optimierte Queries**: Nutzt Appwrite-Indexes für schnelle Suche
- **Begrenzte Results**: Maximal 8-10 Vorschläge pro Suche
- **Efficient State Management**: StateFlow für reaktive UI-Updates

## Verwendung

1. User tippt in das Rasse-Feld
2. Nach 300ms startet automatisch die Suche
3. Vorschläge erscheinen als Dropdown mit Details
4. User kann Vorschlag anklicken oder weiter tippen
5. Bei Auswahl wird Rasse übernommen und Dropdown geschlossen

## Technische Details

### Appwrite Queries
```kotlin
// Fulltext-Suche
"search(\"name\", \"$query\")"

// StartsWith-Fallback
"startsWith(\"name\", \"$query\")"

// Lokales Filtering für Contains
breed.name.contains(query, ignoreCase = true)
```

### State Management
```kotlin
// Suggestions State
val suggestions: StateFlow<List<DogBreed>>

// Loading State
val isLoading: StateFlow<Boolean>

// Visibility State
val showSuggestions: StateFlow<Boolean>
```

## Zukünftige Erweiterungen

1. **Favorites**: Häufig verwendete Rassen bevorzugt anzeigen
2. **Filtering**: Filter nach Größe oder Aktivitätslevel
3. **Fuzzy Search**: Toleranz für Tippfehler
4. **Offline Support**: Lokale Caching-Strategie
5. **Analytics**: Tracking der beliebtesten Suchanfragen

## Wartung

- **Neue Rassen**: Einfaches Hinzufügen über Appwrite Console
- **Index Maintenance**: Regelmäßige Überprüfung der Index-Performance
- **Update Strategy**: Schema-Änderungen über Migration-Scripts
