package com.example.snacktrack.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.snacktrack.data.repository.AuthRepository
import com.example.snacktrack.utils.ValidationUtils
import kotlinx.coroutines.launch
import com.example.snacktrack.BuildConfig

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository(context) }
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Prüfen, ob der Nutzer bereits eingeloggt ist
    LaunchedEffect(Unit) {
        if (authRepository.isLoggedIn()) {
            onLoginSuccess()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SnackTrack",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Dein Ernährungscoach für Hunde",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-Mail") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Passwort") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                errorMessage = null
                
                // Validate email
                if (!ValidationUtils.isValidEmail(email.trim())) {
                    errorMessage = "Bitte geben Sie eine gültige E-Mail-Adresse ein"
                    return@Button
                }
                
                // Validate password
                if (password.isBlank()) {
                    errorMessage = "Bitte geben Sie Ihr Passwort ein"
                    return@Button
                }
                
                isLoading = true
                
                scope.launch {
                    authRepository.login(email.trim(), password)
                        .onSuccess {
                            isLoading = false
                            // Debug: Verify session after login
                            val sessionStatus = authRepository.debugSessionStatus()
                            android.util.Log.d("LoginScreen", "Session after login: $sessionStatus")
                            onLoginSuccess()
                        }
                        .onFailure { e ->
                            isLoading = false
                            android.util.Log.e("LoginScreen", "Login failed: ${e.message}")
                            errorMessage = when {
                                e.message?.contains("401") == true -> "E-Mail oder Passwort falsch"
                                e.message?.contains("network", ignoreCase = true) == true -> "Netzwerkfehler. Bitte überprüfen Sie Ihre Internetverbindung"
                                else -> "Anmeldung fehlgeschlagen: ${e.message}"
                            }
                        }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Anmelden")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Google Login Button
        OutlinedButton(
            onClick = {
                isLoading = true
                errorMessage = null
                scope.launch {
                    authRepository.loginWithGoogle(context as ComponentActivity)
                        .onSuccess {
                            isLoading = false
                            onLoginSuccess()
                        }
                        .onFailure { e ->
                            isLoading = false
                            errorMessage = "Google-Anmeldung fehlgeschlagen: ${e.message}"
                        }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Login,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Mit Google anmelden")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = " oder ",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrieren")
        }
        
        // Debug button (only in debug builds)
        if (BuildConfig.DEBUG) {
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = {
                    scope.launch {
                        val debugInfo = authRepository.debugSessionStatus()
                        errorMessage = debugInfo
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Debug Session Status")
            }
        }
    }
} 