# 🚀 Dog Food Crawler - Ausführungsanleitung

## 📋 Manuelle Ausführung

### Option 1: Über die Appwrite Console (Empfohlen)

1. **Gehe zu**: https://parse.nordburglarp.de/console
2. **Navigate zu**: Functions → dog-food-crawler
3. **Klicke auf**: "Execute" Button (oben rechts)
4. **Execution erstellen**:
   - Data (optional): `{}` (leeres JSON-Objekt)
   - Headers (optional): leer lassen
   - Klicke "Execute"
5. **Überwache Ausführung**:
   - Klicke auf die neue Execution in der Liste
   - Sieh Live-Logs und Status
   - Prüfe Response für Ergebnisse

### Option 2: Über API/SDK

```javascript
// Mit Node.js SDK
const sdk = require('node-appwrite');

const client = new sdk.Client();
client
    .setEndpoint('https://parse.nordburglarp.de/v1')
    .setProject('snackrack2')
    .setKey('YOUR_API_KEY');

const functions = new sdk.Functions(client);

// Function ausführen
const execution = await functions.createExecution(
    'dog-food-crawler',
    '{}', // data
    false // async
);

console.log('Execution started:', execution.$id);
```

### Option 3: Über cURL

```bash
curl -X POST \
  'https://parse.nordburglarp.de/v1/functions/dog-food-crawler/executions' \
  -H 'X-Appwrite-Project: snackrack2' \
  -H 'X-Appwrite-Key: YOUR_API_KEY' \
  -H 'Content-Type: application/json' \
  -d '{}'
```

## ⏰ Tägliche Ausführung um 2:00 Uhr

### Schedule ist bereits konfiguriert! ✅

Die Function hat bereits einen Schedule von `0 2 * * *` (CRON Format), was bedeutet:
- **0**: Minute 0
- **2**: Stunde 2 (2:00 Uhr)
- **\***: Jeden Tag im Monat
- **\***: Jeden Monat
- **\***: Jeden Wochentag

**= Täglich um 2:00 Uhr UTC**

### Schedule aktivieren/deaktivieren:

#### In der Console:
1. Functions → dog-food-crawler
2. "Settings" Tab
3. "Schedule" Sektion
4. Toggle "Enabled" ein/aus

#### Über SDK:
```javascript
// Schedule aktivieren
await functions.update(
    'dog-food-crawler',
    'Dog Food Crawler',
    'node-16.0',
    ['any'],
    [],
    '0 2 * * *', // CRON schedule
    900,
    true, // enabled = true
    true
);

// Schedule deaktivieren
await functions.update(
    'dog-food-crawler',
    'Dog Food Crawler',
    'node-16.0',
    ['any'],
    [],
    '', // Leerer String deaktiviert Schedule
    900,
    true,
    true
);
```

## 📊 Execution überwachen

### In der Console:
1. Functions → dog-food-crawler
2. "Executions" Tab
3. Klicke auf eine Execution für Details:
   - Status (waiting, processing, completed, failed)
   - Logs (Echtzeit während Ausführung)
   - Response (Ergebnisse nach Abschluss)
   - Duration
   - Errors (falls vorhanden)

### Execution Status:
- **⏳ Waiting**: In Warteschlange
- **🔄 Processing**: Läuft gerade
- **✅ Completed**: Erfolgreich abgeschlossen
- **❌ Failed**: Fehler aufgetreten

## 🔍 Ergebnisse prüfen

Nach erfolgreicher Ausführung:

1. **Check Response**:
   ```json
   {
     "success": true,
     "sessionId": "unique-id",
     "duration": 120000,
     "results": {
       "openPetFoodFacts": 25,
       "duplicates": 3,
       "errors": 0,
       "totalProcessed": 25
     }
   }
   ```

2. **Check foodSubmissions Collection**:
   - Neue Einträge mit `status: "pending"`
   - `crawlSessionId` entspricht der Session
   - `source: "opff"` für Open Pet Food Facts

3. **Check crawlState Collection**:
   - `totalProcessed` wurde erhöht
   - `lastRunDate` wurde aktualisiert
   - `opffPage` zeigt nächste Seite

## 🛠️ Troubleshooting

### Function läuft nicht automatisch:
1. Prüfe ob Function "Enabled" ist
2. Prüfe Schedule in Settings
3. Prüfe Timezone (Schedule ist in UTC!)

### Manuelle Execution schlägt fehl:
1. Prüfe Logs für Fehlerdetails
2. Stelle sicher, dass alle Umgebungsvariablen gesetzt sind
3. Prüfe ob Deployment aktiv ist

### Keine neuen Produkte:
1. Prüfe crawlState - vielleicht alle Seiten durchlaufen
2. Prüfe auf Duplikate in foodSubmissions
3. Schaue in Logs nach Rate Limiting

## 💡 Tipps

- **Zeitzone beachten**: 2:00 UTC = 3:00 MEZ oder 4:00 MESZ
- **Execution Limit**: Max. 15 Minuten Laufzeit
- **Rate Limiting**: Function wartet zwischen Requests
- **Monitoring**: Prüfe regelmäßig crawlState für Fortschritt

---

Die Function ist vollständig konfiguriert und bereit! Der Schedule läuft automatisch, sobald die Function deployed ist.