@echo off
REM Cleanup-Skript zum Entfernen alter Dateien nach dem Klonen (Windows)

echo Cleaning up old files...

REM Entferne alte NavGraph.kt falls vorhanden
if exist "app\src\main\java\com\example\snacktrack\ui\navigation\NavGraph.kt" (
    del "app\src\main\java\com\example\snacktrack\ui\navigation\NavGraph.kt"
    echo Removed old NavGraph.kt
)

echo Cleanup completed!