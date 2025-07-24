# Complete Google OAuth Setup Guide for SnackTrack

## ✅ Implementation Status

The following components are **already implemented** in the SnackTrack app:

1. **AndroidManifest.xml** - OAuth Callback Activity configured
   ```xml
   <activity 
       android:name="io.appwrite.views.CallbackActivity" 
       android:exported="true">
       <intent-filter android:label="android_web_auth">
           <action android:name="android.intent.action.VIEW" />
           <category android:name="android.intent.category.DEFAULT" />
           <category android:name="android.intent.category.BROWSABLE" />
           <data android:scheme="appwrite-callback-snackrack2" />
       </intent-filter>
   </activity>
   ```

2. **LoginScreen.kt** - Google Login Button implemented
   - Button with "Mit Google anmelden" text
   - Proper error handling
   - Loading states

3. **AuthRepository.kt** - loginWithGoogle() method implemented
   - Uses correct callback URL scheme
   - Proper exception handling
   - Returns Result<Unit>

4. **AppwriteService.kt** - Correctly configured with:
   - Endpoint: `https://parse.nordburglarp.de/v1` (✅ v1, not v2)
   - Project ID: `snackrack2`

## 📋 Configuration Checklist

### 1. Generate SHA-1 Fingerprint

#### For Debug Build:
```bash
# On Linux/Mac:
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android

# On Windows:
keytool -list -v -keystore %USERPROFILE%\.android\debug.keystore -alias androiddebugkey -storepass android -keypass android

# If keytool is not found, use full path:
# Linux/Mac: $JAVA_HOME/bin/keytool
# Windows: %JAVA_HOME%\bin\keytool
# Or find it in Android Studio: File > Project Structure > SDK Location > JDK Location
```

The output will contain:
```
Certificate fingerprints:
     SHA1: XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX
```

#### For Release Build:
```bash
keytool -list -v -keystore path/to/your/release.keystore -alias your-alias-name
```

### 2. Google Cloud Console Setup

1. **Go to [Google Cloud Console](https://console.cloud.google.com/)**

2. **Create/Select Project**
   - Click on project dropdown
   - Create new project or select existing

3. **Enable Google+ API**
   - Go to "APIs & Services" > "Library"
   - Search for "Google+ API"
   - Click and Enable it

4. **Create OAuth 2.0 Credentials**

   **IMPORTANT: You need TWO OAuth clients:**

   #### A. Android Client (for app authentication)
   - Go to "APIs & Services" > "Credentials"
   - Click "+ CREATE CREDENTIALS" > "OAuth 2.0 Client ID"
   - Application type: **Android**
   - Name: `SnackTrack Android App`
   - Package name: `com.example.snacktrack`
   - SHA-1 certificate fingerprint: (paste your SHA-1 from step 1)
   - Click "Create"

   #### B. Web Application Client (for Appwrite redirect)
   - Click "+ CREATE CREDENTIALS" > "OAuth 2.0 Client ID" again
   - Application type: **Web application**
   - Name: `SnackTrack Web OAuth`
   - Authorized JavaScript origins:
     ```
     https://parse.nordburglarp.de
     ```
   - Authorized redirect URIs:
     ```
     https://parse.nordburglarp.de/v1/account/sessions/oauth2/callback/google/snackrack2
     appwrite-callback-snackrack2://auth
     ```
   - Click "Create"
   - **Save the Client ID and Client Secret** (you'll need these for Appwrite)

### 3. Appwrite Dashboard Configuration

1. **Login to Appwrite Dashboard**
   - Go to https://parse.nordburglarp.de
   - Login with your admin credentials

2. **Navigate to OAuth Settings**
   - Click on your project (snackrack2)
   - Go to "Auth" > "Settings"
   - Scroll to "OAuth2 Providers"

3. **Configure Google Provider**
   - Find "Google" in the list
   - Toggle it ON
   - Click on settings/configure
   - Enter:
     - **App ID**: Client ID from the Web Application OAuth client
     - **App Secret**: Client Secret from the Web Application OAuth client
   - Save the configuration

### 4. Verification Steps

1. **Check Appwrite shows correct redirect URL:**
   ```
   https://parse.nordburglarp.de/v1/account/sessions/oauth2/callback/google/snackrack2
   ```

2. **Verify all configurations:**
   - ✅ Android OAuth client with correct SHA-1
   - ✅ Web OAuth client with correct redirect URIs
   - ✅ Appwrite Google provider enabled with credentials
   - ✅ App callback URL matches: `appwrite-callback-snackrack2://auth`

### 5. Testing

1. **Build and run the app**
   ```bash
   ./gradlew installDebug
   ```

2. **Test the login flow:**
   - Open the app
   - Click "Mit Google anmelden"
   - Browser/WebView should open
   - Select Google account
   - Grant permissions
   - Should redirect back to app
   - User should be logged in

### 6. Common Issues and Solutions

#### "Error 400: redirect_uri_mismatch"
- Double-check ALL redirect URIs are added to the Web OAuth client
- Ensure you're using the Web client credentials in Appwrite, not Android

#### "Invalid OAuth provider"
- Verify Google provider is enabled in Appwrite
- Check that credentials are saved correctly

#### "OAuth callback failed"
- Verify callback URL scheme in app matches: `appwrite-callback-snackrack2`
- Check AndroidManifest.xml has the callback activity

#### App crashes on Google login
- Ensure you're passing ComponentActivity to loginWithGoogle()
- Check that the activity context is correct

### 7. Production Checklist

- [ ] Generate production SHA-1 fingerprint
- [ ] Add production SHA-1 to Google Console
- [ ] Create separate OAuth clients for production
- [ ] Update Appwrite with production credentials
- [ ] Test on release build
- [ ] Monitor OAuth quota usage

### 8. Security Notes

- Never commit Client Secret to version control
- Use environment variables for sensitive data
- Regularly rotate OAuth credentials
- Monitor for suspicious activity in Google Console

## Summary

With the implementation already complete in the code, you only need to:

1. Generate your SHA-1 fingerprint
2. Create TWO OAuth clients in Google Console (Android + Web)
3. Configure Google provider in Appwrite Dashboard with Web client credentials
4. Test the login flow

The app is ready to use Google OAuth once these configuration steps are completed!