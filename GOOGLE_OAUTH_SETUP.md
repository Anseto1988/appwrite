# Google OAuth Setup für SnackTrack

## 1. Google Cloud Console Konfiguration

### Schritt 1: Google Cloud Projekt erstellen/auswählen
1. Gehe zu [Google Cloud Console](https://console.cloud.google.com/)
2. Erstelle ein neues Projekt oder wähle ein bestehendes aus
3. Aktiviere die **Google+ API** für dein Projekt

### Schritt 2: OAuth 2.0 Credentials erstellen
1. Navigiere zu **APIs & Services** > **Credentials**
2. Klicke auf **+ CREATE CREDENTIALS** > **OAuth 2.0 Client IDs**
3. Wähle **Android** als Application type
4. Konfiguriere folgende Einstellungen:
   - **Name**: SnackTrack Android App
   - **Package name**: `com.example.snacktrack`
   - **SHA-1 certificate fingerprint**: [Dein Debug/Release SHA-1 Key]

### Schritt 3: SHA-1 Fingerprint generieren
```bash
# Für Debug Build
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android

# Für Release Build
keytool -list -v -keystore path/to/your/release-keystore.jks -alias your-key-alias
```

### Schritt 4: Authorized redirect URIs konfigurieren
Füge folgende Redirect URI hinzu:
```
appwrite-callback-snackrack2://auth
```

## 2. Appwrite Dashboard Konfiguration

### Schritt 1: OAuth Provider aktivieren
1. Öffne dein Appwrite Dashboard
2. Navigiere zu **Auth** > **Settings**
3. Scrolle zu **OAuth2 Providers**
4. Aktiviere **Google** Provider

### Schritt 2: Google Credentials eintragen
1. Klicke auf **Google** Provider Settings
2. Trage ein:
   - **App ID**: Client ID aus Google Console
   - **App Secret**: Client Secret aus Google Console

### Schritt 3: Redirect URLs konfigurieren
Appwrite zeigt automatisch die korrekte Redirect URL an:
```
https://parse.nordburglarp.de/v2/account/sessions/oauth2/callback/google/snackrack2
```

**Wichtig**: Diese URL muss in der Google Console unter "Authorized redirect URIs" hinzugefügt werden!

## 3. Android App Konfiguration

### Bereits implementiert:
- ✅ OAuth Callback Activity in AndroidManifest.xml
- ✅ Google Login Button in LoginScreen
- ✅ AuthRepository.loginWithGoogle() Methode
- ✅ Callback URL Schema: `appwrite-callback-snackrack2`

### Erforderliche URLs für Google Console:

#### Authorized JavaScript origins:
```
https://parse.nordburglarp.de
```

#### Authorized redirect URIs:
```
https://parse.nordburglarp.de/v2/account/sessions/oauth2/callback/google/snackrack2
appwrite-callback-snackrack2://auth
```

## 4. Testing

### Login Flow testen:
1. Starte die App
2. Klicke auf "Mit Google anmelden"
3. Browser/WebView öffnet sich mit Google Login
4. Nach erfolgreicher Anmeldung kehrt die App zurück
5. User ist eingeloggt

### Troubleshooting:

**Fehler: "Error 400: redirect_uri_mismatch"**
- Prüfe, ob alle Redirect URIs korrekt in Google Console eingetragen sind

**Fehler: "Invalid OAuth provider"**
- Stelle sicher, dass Google Provider in Appwrite aktiviert ist

**Fehler: "OAuth callback failed"**
- Prüfe AndroidManifest.xml Callback Activity Konfiguration

## 5. Sicherheitshinweise

- Verwende für Production Build eigene SHA-1 Fingerprints
- Speichere Client Secret sicher (nur auf Server-Seite)
- Regelmäßige Rotation der OAuth Credentials
- Monitoring der OAuth Usage in Google Console

## 6. Erweiterte Konfiguration

### Scopes hinzufügen:
```kotlin
account.createOAuth2Session(
    provider = "google",
    scopes = listOf("email", "profile", "openid"),
    success = "appwrite-callback-snackrack2://auth",
    failure = "appwrite-callback-snackrack2://auth"
)
```

### User Profile Daten abrufen:
```kotlin
suspend fun getUserProfile(): Result<User<Map<String, Any>>> {
    return try {
        val user = account.get()
        Result.success(user)
    } catch (e: AppwriteException) {
        Result.failure(e)
    }
}
```