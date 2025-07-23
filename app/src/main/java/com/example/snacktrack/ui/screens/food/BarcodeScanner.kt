package com.example.snacktrack.ui.screens.food

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.snacktrack.data.repository.FoodRepository
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScanner(
    dogId: String,
    onFoodFound: (String) -> Unit,
    onFoodNotFound: (String) -> Unit,
    onBackClick: () -> Unit
) {    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var showPermissionDeniedMessage by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var scanInitiated by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (granted) {
                scanInitiated = true
            } else {
                showPermissionDeniedMessage = true
            }
        }
    )
    val foodRepository = remember { FoodRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    
    val zxingBarcodeLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        isLoading = false
        if (result.contents == null) {
            Log.d("BarcodeScanner", "Scan abgebrochen oder fehlgeschlagen")
        } else {
            val barcode = result.contents
            Log.d("BarcodeScanner", "Barcode gescannt: $barcode")
            
            // Setze isLoading wieder auf true während wir in der DB prüfen
            isLoading = true
            errorMessage = null
            
            // Suche nach dem Produkt in der Datenbank
            coroutineScope.launch {
                try {
                    Log.d("BarcodeScanner", "Starte Suche nach Barcode: $barcode")
                    val foodResult = foodRepository.getFoodByEAN(barcode)
                    isLoading = false
                    
                    Log.d("BarcodeScanner", "Suchergebnis: isSuccess=${foodResult.isSuccess}, hasValue=${foodResult.getOrNull() != null}")
                    
                    if (foodResult.isSuccess && foodResult.getOrNull() != null) {
                        // Produkt gefunden - navigiere zur Mengenangabe
                        val food = foodResult.getOrNull()!!
                        Log.d("BarcodeScanner", "Produkt gefunden: ID=${food.id}, NAME=${food.product}, EAN=${food.ean}")
                        onFoodFound(food.id)
                    } else if (foodResult.isSuccess) {
                        // Produkt nicht gefunden, aber Abfrage war erfolgreich
                        Log.d("BarcodeScanner", "Produkt nicht in Datenbank gefunden. EAN=$barcode")
                        onFoodNotFound(barcode)
                    } else {
                        // Fehler bei der Abfrage
                        val exception = foodResult.exceptionOrNull()
                        Log.e("BarcodeScanner", "Fehler bei der Datenbankabfrage: ${exception?.message}", exception)
                        errorMessage = "Datenbankfehler: ${exception?.message}"
                        onFoodNotFound(barcode)
                    }
                } catch (e: Exception) {
                    isLoading = false
                    errorMessage = "Fehler bei der Suche: ${e.message}"
                    Log.e("BarcodeScanner", "Fehler bei der Suche nach Barcode", e)
                }
            }
        }
    }    // Funktion zum Starten des Barcode-Scans
    fun startScan() {
        if (hasCameraPermission) {
            isLoading = true
            val options = ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.EAN_8, ScanOptions.EAN_13, ScanOptions.UPC_A, ScanOptions.UPC_E)
                setPrompt("Barcode auf Produkt ausrichten")
                setCameraId(0)  // 0 für Rückkamera
                setBeepEnabled(true)
                setBarcodeImageEnabled(false)
                setOrientationLocked(false)
            }
            zxingBarcodeLauncher.launch(options)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(hasCameraPermission, scanInitiated) {
        if (hasCameraPermission && scanInitiated) {
            startScan()
            scanInitiated = false 
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            scanInitiated = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Barcode scannen") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (hasCameraPermission) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Scanner wird geladen...")
                    } else {
                        Text("Bereit zum Scannen")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { scanInitiated = true }) {
                            Text("Barcode scannen")
                        }
                        
                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        if (showPermissionDeniedMessage) 
                            "Kamera-Berechtigung wurde verweigert. Bitte erteile die Berechtigung in den App-Einstellungen."
                        else 
                            "Kamera-Berechtigung wird benötigt, um Barcodes zu scannen."
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { requestPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text("Berechtigung erteilen")
                    }
                }
            }
        }
    }
}