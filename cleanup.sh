#!/bin/bash
# Cleanup-Skript zum Entfernen alter Dateien nach dem Klonen

echo "Cleaning up old files..."

# Entferne alte NavGraph.kt falls vorhanden
if [ -f "app/src/main/java/com/example/snacktrack/ui/navigation/NavGraph.kt" ]; then
    rm "app/src/main/java/com/example/snacktrack/ui/navigation/NavGraph.kt"
    echo "Removed old NavGraph.kt"
fi

echo "Cleanup completed!"