# WICHTIGE BUILD-ANWEISUNGEN

## Nach dem Klonen des Repositories

Falls Sie Kompilierungsfehler bezüglich doppelter `Screen` Klassen erhalten, führen Sie bitte das Cleanup-Skript aus:

### Windows:
```bash
cleanup.bat
```

### Linux/Mac:
```bash
chmod +x cleanup.sh
./cleanup.sh
```

## Problem

Es kann vorkommen, dass Git die Löschung der alten `NavGraph.kt` Datei nicht korrekt synchronisiert. Das Cleanup-Skript entfernt diese Datei manuell.

## Manuelle Lösung

Falls das Skript nicht funktioniert, löschen Sie bitte manuell:
```
app/src/main/java/com/example/snacktrack/ui/navigation/NavGraph.kt
```

Die korrekte Navigation befindet sich in:
```
app/src/main/java/com/example/snacktrack/ui/navigation/SnackTrackNavGraph.kt
```