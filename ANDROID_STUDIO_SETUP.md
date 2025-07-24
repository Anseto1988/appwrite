# Android Studio Setup für SnackTrack

## Voraussetzungen

Android Studio ist bereits installiert (Snap Version 2025.1.1.13).

## Setup-Schritte

### 1. Android Studio starten
```bash
android-studio
```

### 2. Beim ersten Start:
- Wähle "Open" und navigiere zu: `/home/anseto/programe/snacktrack`
- Android Studio wird automatisch:
  - Das Android SDK herunterladen
  - Java/JDK konfigurieren
  - Gradle sync durchführen

### 3. SDK Installation
Wenn Android Studio nach dem SDK fragt:
- Standardpfad: `/home/anseto/Android/Sdk`
- Empfohlene SDK Version: Android 14 (API Level 34) oder höher
- Build Tools: Version 34.0.0 oder höher

### 4. Nach der SDK Installation
Die `local.properties` wird automatisch mit dem korrekten SDK Pfad aktualisiert.

### 5. Gradle Sync
- Android Studio führt automatisch einen Gradle Sync durch
- Dies kann beim ersten Mal einige Minuten dauern

### 6. Emulator einrichten
1. Tools → AVD Manager
2. Create Virtual Device
3. Wähle ein Pixel Gerät
4. System Image: Android 14 (API 34)
5. Finish

### 7. App ausführen
- Grüner Play-Button oder Shift+F10
- Wähle den Emulator oder ein verbundenes Gerät

## Fehlerbehebung

### Gradle Sync Fehler
```bash
# Cache löschen und neu builden
./gradlew clean
./gradlew build
```

### JDK nicht gefunden
Android Studio bringt sein eigenes JDK mit. Falls Probleme:
- File → Project Structure → SDK Location
- JDK location sollte auf Android Studio's embedded JDK zeigen

## Wichtige Dateien
- `local.properties` - SDK Pfad (NICHT committen!)
- `gradle.properties` - Gradle Einstellungen
- `app/build.gradle.kts` - App Konfiguration

## Build von der Kommandozeile
Nach dem Setup in Android Studio:
```bash
# Debug APK erstellen
./gradlew assembleDebug

# Release APK erstellen  
./gradlew assembleRelease
```

Die APKs befinden sich dann in:
- Debug: `app/build/outputs/apk/debug/`
- Release: `app/build/outputs/apk/release/`