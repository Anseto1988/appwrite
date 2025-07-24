# 🔧 Dog Food Crawler - Troubleshooting Guide

## 🚨 Problem: Keine Logs bei der Execution

### Mögliche Ursachen:

1. **Function startet nicht richtig**
   - Deployment-Fehler
   - Fehlende Dependencies
   - Syntax-Fehler im Code

2. **Environment-Probleme**
   - Node.js Version Kompatibilität
   - Module können nicht geladen werden
   - Speicher/Timeout-Probleme

3. **Appwrite-spezifische Probleme**
   - Function-Handler Format
   - Request/Response Objekte

## 🛠️ Debugging-Schritte:

### 1. Test mit Minimal-Function

Ich habe Test-Functions erstellt:
- `dog-food-crawler-test.tar.gz` - Test-Paket mit Debug-Output
- `src/test-simple.js` - Einfache Test-Function
- `src/minimal-test.js` - Minimale Function ohne Dependencies

**Test-Deployment:**
1. Upload `dog-food-crawler-test.tar.gz`
2. Entrypoint: `src/test-simple.js` oder `src/minimal-test.js`
3. Build command: `npm install`
4. Deploy und Execute

### 2. Überprüfung in Appwrite Console

**Build Logs prüfen:**
1. Functions → dog-food-crawler → Deployments
2. Klicke auf das Deployment
3. Schaue nach "Build logs" oder "Build output"
4. Suche nach Fehlern beim `npm install`

**Execution Details:**
1. Functions → dog-food-crawler → Executions
2. Klicke auf eine Execution
3. Prüfe:
   - Status (failed/completed/processing)
   - Response body
   - Error messages
   - Logs (falls vorhanden)

### 3. Häufige Probleme & Lösungen

#### Problem: "No logs available"
**Lösung:**
- Verwende `console.log()` direkt am Anfang der Function
- Stelle sicher, dass Function-Logging aktiviert ist
- Prüfe ob die Function überhaupt startet

#### Problem: "Execution timeout"
**Lösung:**
- Reduziere `MAX_PRODUCTS_PER_RUN` auf 10 für Tests
- Füge mehr Logging zwischen Schritten ein
- Prüfe ob externe APIs erreichbar sind

#### Problem: "Module not found"
**Lösung:**
- Stelle sicher, dass `npm install` im Build-Command steht
- Prüfe package.json auf fehlende Dependencies
- Verwende Node 16 kompatible Versionen

#### Problem: "Function handler error"
**Lösung:**
- Stelle sicher, dass die Function das richtige Format hat:
  ```javascript
  module.exports = async function(req, res) {
      // Code hier
      res.json({ success: true });
  };
  ```

### 4. Test-Execution mit limitierten Daten

Erstelle eine Test-Execution mit weniger Daten:
```javascript
{
    "test": true,
    "maxProducts": 5,
    "skipDatabase": false
}
```

### 5. Logs manuell aktivieren

Falls Logs nicht erscheinen:
1. Stelle sicher, dass in der Function steht:
   ```javascript
   console.log('Function started at:', new Date().toISOString());
   ```
2. Verwende try-catch überall:
   ```javascript
   try {
       // Code
   } catch (error) {
       console.error('Error:', error.message);
       console.error('Stack:', error.stack);
   }
   ```

### 6. Alternative: Lokaler Test

Test die Function lokal:
```bash
cd functions/dog-food-crawler
npm install
node test-local.js
```

Dies zeigt, ob der Code grundsätzlich funktioniert.

## 📊 Status-Übersicht aus dem Debug:

- ✅ Function existiert und ist enabled
- ✅ 2 Deployments vorhanden (ready)
- ✅ Alle Environment-Variablen gesetzt
- ❌ Executions hängen oder timeout
- ❌ Keine Logs verfügbar

## 🎯 Empfohlene Nächste Schritte:

1. **Deploy die Test-Function** (minimal-test.js)
   - Dies zeigt, ob das Deployment-System funktioniert

2. **Prüfe Build-Logs** des Deployments
   - Dort stehen Fehler beim npm install

3. **Vereinfache den Haupt-Code**
   - Kommentiere komplexe Teile aus
   - Füge mehr console.log() ein

4. **Prüfe Netzwerk-Zugriff**
   - Kann die Function externe APIs erreichen?
   - Gibt es Firewall-Regeln?

5. **Memory/Timeout anpassen**
   - Eventuell mehr Memory allokieren
   - Timeout erhöhen (max 900s)

Die Function-Konfiguration sieht korrekt aus, aber die Execution schlägt fehl. Mit den Test-Functions können wir das Problem eingrenzen.