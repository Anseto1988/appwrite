package com.example.snacktrack.ui.screens

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@Composable
fun BarcodeScannerScreen(
    onBarcodeDetected: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var showPermissionDeniedMessage by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) } // Für Ladezustand während des Scans

    // Launcher für die Kamera-Berechtigung
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (!granted) {
                showPermissionDeniedMessage = true
            }
        }
    )

    // Launcher für den Barcode-Scanner von ZXing
    val zxingBarcodeLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        isLoading = false // Scan beendet, Ladeanzeige ausblenden
        if (result.contents == null) {
            Log.d("BarcodeScanner", "Scan abgebrochen oder fehlgeschlagen")
            // Optional: dem Benutzer eine Nachricht anzeigen, dass der Scan abgebrochen wurde
        } else {
            Log.d("BarcodeScanner", "Barcode gescannt: ${result.contents}")
            onBarcodeDetected(result.contents)
        }
    }

    // Funktion zum Starten des Scans
    fun startScan() {
        if (hasCameraPermission) {
            isLoading = true // Ladeanzeige einblenden
            val options = ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.EAN_8, ScanOptions.EAN_13, ScanOptions.UPC_A, ScanOptions.UPC_E)
                setPrompt("Barcode scannen")
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

    // Beim ersten Start Berechtigung anfordern, falls nicht vorhanden
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            // Wenn Berechtigung vorhanden ist, starte den Scan direkt
            startScan()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            // Die ZXing-Bibliothek öffnet ihre eigene Scan-Aktivität,
            // daher ist hier keine explizite Kamera-Vorschau im Composable erforderlich.
            // Wir zeigen stattdessen eine Ladeanzeige oder eine Anleitung.
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
                    Text("Barcode-Scanner wird gestartet...")
                } else {
                    Text("Bereit zum Scannen.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { startScan() }) {
                        Text("Erneut scannen")
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
                    if (showPermissionDeniedMessage) "Kamera-Berechtigung wurde verweigert. Bitte erteile die Berechtigung in den App-Einstellungen."
                    else "Kamera-Berechtigung wird benötigt, um Barcodes zu scannen."
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { requestPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Berechtigung erteilen")
                }
            }
        }

        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Zurück",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
