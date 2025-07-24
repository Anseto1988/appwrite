# 🚀 Google OAuth Setup für SnackTrack - Schnellanleitung

## ✅ Die App ist bereits fertig konfiguriert!

Die gesamte OAuth-Implementierung ist bereits in der App vorhanden:
- Google Login Button ✓
- OAuth Callback Handler ✓  
- Appwrite Integration ✓

Du musst nur noch die Google Console und Appwrite Dashboard konfigurieren.

---

## 📋 Was du brauchst:

### 1. SHA-1 Fingerprint generieren
Führe diesen Befehl auf deinem Entwicklungsrechner aus:
```bash
# Für Debug Build (Entwicklung)
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android | grep SHA1

# Windows:
keytool -list -v -keystore %USERPROFILE%\.android\debug.keystore -alias androiddebugkey -storepass android -keypass android | findstr SHA1
```

### 2. App Details
- **Package Name:** `com.example.snacktrack`
- **Appwrite Project ID:** `snackrack2`
- **Appwrite Endpoint:** `https://parse.nordburglarp.de/v1`

---

## 🔧 Schritt-für-Schritt Anleitung:

### Schritt 1: Google Cloud Console einrichten

1. Gehe zu https://console.cloud.google.com/
2. Erstelle ein neues Projekt oder wähle ein bestehendes
3. Aktiviere die **Google+ API**
4. Gehe zu **APIs & Services → Credentials**

### Schritt 2: OAuth Clients erstellen (WICHTIG: Du brauchst ZWEI!)

#### Client 1: Android App
1. Klicke auf **+ CREATE CREDENTIALS → OAuth 2.0 Client ID**
2. Wähle **Android** als Application type
3. Fülle aus:
   - **Name:** SnackTrack Android
   - **Package name:** `com.example.snacktrack`
   - **SHA-1 certificate fingerprint:** [Dein generierter SHA-1]
4. Erstellen

#### Client 2: Web Application (für Appwrite)
1. Klicke erneut auf **+ CREATE CREDENTIALS → OAuth 2.0 Client ID**
2. Wähle **Web application** als Application type
3. Fülle aus:
   - **Name:** SnackTrack Web OAuth
   - **Authorized JavaScript origins:**
     ```
     https://parse.nordburglarp.de
     ```
   - **Authorized redirect URIs:**
     ```
     https://parse.nordburglarp.de/v1/account/sessions/oauth2/callback/google/snackrack2
     appwrite-callback-snackrack2://auth
     ```
4. Erstellen

⚠️ **WICHTIG:** Nutze die Credentials vom **Web Application** Client für Appwrite!

---

### Schritt 3: Appwrite Dashboard konfigurieren

1. Gehe zu https://parse.nordburglarp.de
2. Logge dich ein und navigiere zu deinem Projekt
3. Gehe zu **Auth → Settings**
4. Scrolle zu **OAuth2 Providers**
5. Aktiviere **Google**
6. Trage ein:
   - **App ID:** Client ID vom Web Application OAuth Client
   - **App Secret:** Client Secret vom Web Application OAuth Client
7. Speichern

---

## 🧪 Testen

1. Starte die App
2. Klicke auf "Mit Google anmelden"
3. Der Browser öffnet sich mit Google Login
4. Nach erfolgreicher Anmeldung wirst du zur App zurückgeleitet
5. Du bist eingeloggt! 🎉

---

## 🐛 Fehlerbehebung

### "Error 400: redirect_uri_mismatch"
→ Stelle sicher, dass BEIDE Redirect URIs im Web Client eingetragen sind

### "Invalid OAuth provider"
→ Google Provider in Appwrite ist nicht aktiviert

### "OAuth callback failed"
→ Prüfe, ob du die richtigen Credentials (vom Web Client) verwendest

### App kehrt nicht zurück nach Login
→ SHA-1 Fingerprint stimmt nicht oder falscher OAuth Client verwendet

---

## 📱 Für Production Release

Wenn du die App veröffentlichen willst:
1. Generiere SHA-1 für deinen Release Keystore
2. Füge diesen SHA-1 zum Android OAuth Client hinzu
3. Teste mit einem Release Build

---

## 🔐 Sicherheitshinweise

- Teile niemals dein Client Secret öffentlich
- Verwende für Production eigene SHA-1 Fingerprints
- Überwache die OAuth Usage in der Google Console
- Rotiere regelmäßig deine OAuth Credentials

---

## ✨ Fertig!

Sobald du diese Schritte abgeschlossen hast, funktioniert Google Login in deiner App. Die gesamte Implementierung ist bereits vorhanden und wartet nur auf die Konfiguration!