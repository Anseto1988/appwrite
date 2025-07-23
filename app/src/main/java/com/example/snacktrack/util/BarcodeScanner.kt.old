package com.example.snacktrack.util

import android.content.Context
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

/**
 * Utility-Klasse für das Scannen von Barcodes mit ML Kit
 */
class BarcodeScanner(
    private val context: Context,
    private val onBarcodeDetected: (barcodes: List<Barcode>) -> Unit
) : ImageAnalysis.Analyzer {
    
    private val scanner = BarcodeScanning.getClient()
    private val executor = Executors.newSingleThreadExecutor()
    
    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }
        
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    onBarcodeDetected(barcodes)
                }
            }
            .addOnFailureListener {
                // Fehlerbehandlung hier
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
    
    companion object {
        /**
         * Erstellt eine ImageAnalysis-Instanz für die Barcode-Erkennung
         */
        fun createAnalyzer(context: Context, onBarcodeDetected: (List<Barcode>) -> Unit): ImageAnalysis {
            val analyzer = BarcodeScanner(context, onBarcodeDetected)
            return ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(Executors.newSingleThreadExecutor(), analyzer)
                }
        }
        
        /**
         * Extrahiert die EAN aus einem Barcode
         */
        fun extractEAN(barcode: Barcode): String? {
            return if (barcode.format == Barcode.FORMAT_EAN_13 || 
                       barcode.format == Barcode.FORMAT_EAN_8 ||
                       barcode.format == Barcode.FORMAT_UPC_A ||
                       barcode.format == Barcode.FORMAT_UPC_E) {
                barcode.rawValue
            } else {
                null
            }
        }
    }
} 