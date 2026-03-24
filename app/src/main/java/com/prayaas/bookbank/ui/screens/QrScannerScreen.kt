package com.prayaas.bookbank.ui.screens

import android.Manifest
import android.content.Context
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.*
import com.google.gson.Gson
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.prayaas.bookbank.data.model.QrPayload
import com.prayaas.bookbank.ui.components.PrimaryButton
import com.prayaas.bookbank.ui.theme.*
import com.prayaas.bookbank.viewmodel.AdminViewModel
import com.prayaas.bookbank.viewmodel.ScanState
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class, androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun QrScannerScreen(
    orderId: String,
    bookId: String,
    mode: String, // "issue" or "return"
    adminViewModel: AdminViewModel,
    onBack: () -> Unit,
    onComplete: () -> Unit
) {
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    val scanState by adminViewModel.scanState.collectAsState()
    var scannedPayload by remember { mutableStateOf<QrPayload?>(null) }
    var scanError by remember { mutableStateOf<String?>(null) }

    // Navigate back on success after delay
    LaunchedEffect(scanState) {
        if (scanState is ScanState.Success) {
            kotlinx.coroutines.delay(1500)
            adminViewModel.resetScanState()
            onComplete()
        }
    }

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Camera preview (full screen)
        if (cameraPermission.status.isGranted && scannedPayload == null) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                onQrScanned = { raw ->
                    try {
                        val payload = Gson().fromJson(raw, QrPayload::class.java)
                        if (payload.copyId.isNotBlank()) {
                            scannedPayload = payload
                            scanError = null
                        } else {
                            scanError = "Invalid QR code format"
                        }
                    } catch (e: Exception) {
                        // Try treating raw string directly as copyId
                        scannedPayload = QrPayload(
                            copyId = raw,
                            bookId = bookId,
                            title = "",
                            author = ""
                        )
                    }
                }
            )
        }

        // Overlay UI
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { adminViewModel.resetScanState(); onBack() },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(50))
                ) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (mode == "issue") "Issue Book" else "Return Book",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (mode == "issue") "Scan QR to issue copy" else "Scan QR to process return",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // Scanner frame in center
            if (scannedPayload == null && scanState !is ScanState.Success && scanState !is ScanState.Error) {
                ScannerFrame()
            }

            Spacer(Modifier.weight(1f))

            // Bottom panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color.White)
                    .padding(24.dp)
            ) {
                when {
                    !cameraPermission.status.isGranted -> {
                        PermissionDeniedContent(onRequestPermission = { cameraPermission.launchPermissionRequest() })
                    }
                    scanState is ScanState.Processing -> {
                        ProcessingContent()
                    }
                    scanState is ScanState.Success -> {
                        SuccessContent(message = (scanState as ScanState.Success).message)
                    }
                    scanState is ScanState.Error -> {
                        ErrorContent(
                            message = (scanState as ScanState.Error).message,
                            onRetry = {
                                adminViewModel.resetScanState()
                                scannedPayload = null
                            }
                        )
                    }
                    scannedPayload != null -> {
                        ConfirmScanContent(
                            payload = scannedPayload!!,
                            mode = mode,
                            onConfirm = {
                                if (mode == "issue") {
                                    adminViewModel.issueBook(orderId, bookId, scannedPayload!!)
                                } else {
                                    adminViewModel.returnBook(orderId, scannedPayload!!)
                                }
                            },
                            onRescan = { scannedPayload = null }
                        )
                    }
                    else -> {
                        IdleContent()
                    }
                }
            }
        }
    }
}

@Composable
private fun ScannerFrame() {
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLine"
    )

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .border(2.dp, PrayaasColors.Orange, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
        ) {
            // Scan line animation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .offset(y = (220 * scanLineY).dp)
                    .background(PrayaasColors.Orange.copy(alpha = 0.7f))
            )
        }
    }
}

@Composable
private fun ConfirmScanContent(
    payload: QrPayload,
    mode: String,
    onConfirm: () -> Unit,
    onRescan: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "QR Code Scanned",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrayaasColors.LightGray, RoundedCornerShape(10.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(PrayaasColors.Black, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) { Text("⬛", fontSize = 20.sp) }
            Column {
                Text(
                    payload.copyId,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (payload.title.isNotBlank()) payload.title else "Copy ID verified",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
                Text(
                    if (mode == "issue") "Ready to issue" else "Ready to accept return",
                    style = MaterialTheme.typography.labelSmall,
                    color = PrayaasColors.Success,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        PrimaryButton(
            text = if (mode == "issue") "✓ Confirm Issue" else "✓ Confirm Return",
            onClick = onConfirm,
            containerColor = if (mode == "issue") PrayaasColors.Orange else PrayaasColors.Success
        )
        OutlinedButton(
            onClick = onRescan,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = ButtonShape
        ) {
            Text("↩ Scan Again", color = PrayaasColors.Gray)
        }
    }
}

@Composable
private fun ProcessingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        CircularProgressIndicator(color = PrayaasColors.Orange)
        Text("Processing…", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SuccessContent(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("✓", fontSize = 36.sp, color = PrayaasColors.Success)
        Text(message, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center)
        Text("Database updated", style = MaterialTheme.typography.bodySmall, color = PrayaasColors.Gray)
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("⚠", fontSize = 28.sp, color = PrayaasColors.Error)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = PrayaasColors.Error)
        PrimaryButton(text = "Try Again", onClick = onRetry, containerColor = PrayaasColors.Error)
    }
}

@Composable
private fun IdleContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("📷", fontSize = 32.sp)
        Text("Point camera at the QR code", style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        Text("on the physical book", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), textAlign = TextAlign.Center)
    }
}

@Composable
private fun PermissionDeniedContent(onRequestPermission: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Camera permission is required to scan QR codes.",
            style = MaterialTheme.typography.bodyMedium)
        PrimaryButton(text = "Grant Permission", onClick = onRequestPermission)
    }
}

// ─── CameraX Preview with ML Kit barcode scanning ────────────────────────────
@OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun CameraPreview(
    modifier: Modifier,
    onQrScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasScanned by remember { mutableStateOf(false) }
    val executor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().apply {
                    setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .apply {
                        setAnalyzer(executor) { imageProxy ->
                            if (!hasScanned) {
                                processImageProxy(imageProxy) { result ->
                                    if (result != null && !hasScanned) {
                                        hasScanned = true
                                        onQrScanned(result)
                                    }
                                }
                            } else {
                                imageProxy.close()
                            }
                        }
                    }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalyzer
                    )
                } catch (e: Exception) {
                    android.util.Log.e("CameraPreview", "Camera binding failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier
    )
}

@OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun processImageProxy(imageProxy: ImageProxy, onResult: (String?) -> Unit) {
    val mediaImage = imageProxy.image ?: run {
        imageProxy.close()
        onResult(null)
        return
    }

    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    val scanner = BarcodeScanning.getClient()

    scanner.process(image)
        .addOnSuccessListener { barcodes ->
            val qrCode = barcodes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }
            onResult(qrCode?.rawValue)
        }
        .addOnFailureListener { onResult(null) }
        .addOnCompleteListener { imageProxy.close() }
}
