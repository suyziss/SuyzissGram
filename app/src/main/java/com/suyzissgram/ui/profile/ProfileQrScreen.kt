package com.suyzissgram.ui.profile

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.suyzissgram.domain.model.AppLanguage
import com.suyzissgram.domain.model.UserProfile
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

private const val PROFILE_SCHEME = "suyzissgram"
private val USERNAME_QR_PATTERN = Regex("^[A-Za-z][A-Za-z0-9_]{3,13}$")

fun profileShareUri(username: String): String =
    Uri.Builder().scheme(PROFILE_SCHEME).authority("profile").appendPath(username).build().toString()

private fun createProfileQr(payload: String): Bitmap? = runCatching {
    val matrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, 720, 720)
    Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888).also { bitmap ->
        for (y in 0 until matrix.height) {
            for (x in 0 until matrix.width) {
                bitmap.setPixel(x, y, if (matrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE)
            }
        }
    }
}.getOrNull()

private fun parseProfileQr(raw: String): String? = runCatching {
    val uri = Uri.parse(raw.trim())
    if (uri.scheme != PROFILE_SCHEME || uri.host != "profile") return@runCatching null
    uri.pathSegments.singleOrNull()?.takeIf(USERNAME_QR_PATTERN::matches)
}.getOrNull()

@Composable
fun ProfileQrScreen(
    profile: UserProfile,
    language: AppLanguage,
    onBack: () -> Unit,
    onOpenOwnProfile: () -> Unit
) {
    val isRu = language == AppLanguage.RU || language == AppLanguage.KK
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val clipboard = LocalClipboardManager.current
    val link = remember(profile.username) { profileShareUri(profile.username) }
    val qrBitmap = remember(link) { createProfileQr(link) }
    var selectedTab by remember { mutableStateOf(0) }
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var scanResult by remember { mutableStateOf<String?>(null) }
    var scanError by remember { mutableStateOf<String?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasCameraPermission = it
        if (!it) scanError = if (isRu) "Разрешите доступ к камере в настройках приложения." else "Allow camera access in app settings."
    }
    val cameraController = remember(context) { LifecycleCameraController(context) }
    val scanner = remember {
        val options = BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
        BarcodeScanning.getClient(options)
    }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val scanHandled = remember { AtomicBoolean(false) }
    val sweepTransition = rememberInfiniteTransition(label = "qr_scan_sweep")
    val sweepPosition by sweepTransition.animateFloat(0f, 1f, infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse), label = "qr_scan_line")

    DisposableEffect(cameraController, scanner, analysisExecutor) {
        onDispose {
            cameraController.clearImageAnalysisAnalyzer()
            cameraController.unbind()
            scanner.close()
            analysisExecutor.shutdown()
        }
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = if (isRu) "Назад" else "Back") }
            Text(if (isRu) "QR профиля" else "Profile QR", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        }

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp)
                .clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(4.dp)
        ) {
            listOf(if (isRu) "Мой QR" else "My QR", if (isRu) "Сканировать" else "Scan").forEachIndexed { index, title ->
                val active = selectedTab == index
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                        .background(if (active) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { selectedTab = index; scanResult = null; scanError = null }
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) { Text(title, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal) }
            }
        }

        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = { (fadeIn(tween(180)) + scaleIn(initialScale = 0.98f, animationSpec = tween(180))) togetherWith fadeOut(tween(120)) },
            label = "profile_qr_tabs"
        ) { tab ->
            if (tab == 0) {
                Column(
                    Modifier.fillMaxSize().padding(horizontal = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
                    ) {
                        Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                Modifier.size(76.dp).clip(CircleShape)
                                    .background(androidx.compose.ui.graphics.Color(profile.accentColor.toInt()).copy(alpha = .18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(profile.displayName.firstOrNull()?.uppercase() ?: "@", fontSize = 30.sp, fontWeight = FontWeight.Bold,
                                    color = androidx.compose.ui.graphics.Color(profile.accentColor.toInt()))
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(profile.displayName.ifBlank { profile.username }, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                            Text("@${profile.username}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(18.dp))
                            if (qrBitmap != null) {
                                Image(qrBitmap.asImageBitmap(), contentDescription = if (isRu) "QR-код профиля" else "Profile QR code",
                                    modifier = Modifier.size(236.dp).clip(RoundedCornerShape(12.dp)).background(androidx.compose.ui.graphics.Color.White).padding(8.dp))
                            } else {
                                Text(if (isRu) "Не удалось создать QR-код" else "Could not generate QR code", color = MaterialTheme.colorScheme.error)
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(if (isRu) "Отсканируйте, чтобы распознать ссылку профиля" else "Scan to read the profile link",
                                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    OutlinedButton(onClick = { clipboard.setText(androidx.compose.ui.text.AnnotatedString(link)) }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text(if (isRu) "Копировать ссылку профиля" else "Copy profile link")
                    }
                    Text(if (isRu) "Ссылка работает как идентификатор SuyzissGram. Просмотр чужих профилей появится после подключения серверной части."
                        else "This link identifies a SuyzissGram profile. Viewing other users will require the server.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(top = 12.dp))
                }
            } else {
                Column(Modifier.fillMaxSize().padding(horizontal = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (hasCameraPermission) {
                        Box(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(20.dp)).background(androidx.compose.ui.graphics.Color.Black)) {
                            AndroidView(
                                modifier = Modifier.fillMaxSize(),
                                factory = { viewContext ->
                                    PreviewView(viewContext).also { preview ->
                                        preview.scaleType = PreviewView.ScaleType.FILL_CENTER
                                        cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                        cameraController.bindToLifecycle(lifecycleOwner)
                                        preview.controller = cameraController
                                        cameraController.setImageAnalysisAnalyzer(analysisExecutor, ImageAnalysis.Analyzer { imageProxy ->
                                            val mediaImage = imageProxy.image
                                            if (mediaImage == null) {
                                                imageProxy.close()
                                            } else {
                                                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                                scanner.process(image)
                                                    .addOnSuccessListener { barcodes ->
                                                        val raw = barcodes.firstNotNullOfOrNull { it.rawValue }
                                                        if (raw != null && scanHandled.compareAndSet(false, true)) {
                                                            val username = parseProfileQr(raw)
                                                            if (username != null) scanResult = username
                                                            else scanError = if (isRu) "QR найден, но это не ссылка профиля SuyzissGram." else "QR found, but it is not a SuyzissGram profile link."
                                                        }
                                                    }
                                                    .addOnCompleteListener { imageProxy.close() }
                                            }
                                        })
                                    }
                                }
                            )
                            Box(Modifier.align(Alignment.Center).size(244.dp).border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(22.dp)))
                            Box(Modifier.align(Alignment.Center).fillMaxWidth(.68f).offset(y = ((sweepPosition - .5f) * 220f).dp).height(2.dp).background(MaterialTheme.colorScheme.primary))
                            Text(if (isRu) "Поместите QR-код в рамку" else "Place the QR code inside the frame",
                                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                                    .clip(RoundedCornerShape(50)).background(androidx.compose.ui.graphics.Color.Black.copy(alpha = .68f)).padding(horizontal = 14.dp, vertical = 8.dp),
                                color = androidx.compose.ui.graphics.Color.White, fontSize = 12.sp)
                        }
                    } else {
                        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                                Text(if (isRu) "Для сканирования нужен доступ к камере" else "Camera permission is required", modifier = Modifier.padding(12.dp))
                                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                                    Text(if (isRu) "Разрешить камеру" else "Allow camera")
                                }
                            }
                        }
                    }
                    scanResult?.let { username ->
                        Card(Modifier.fillMaxWidth().padding(top = 12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Text(if (isRu) "Профиль распознан" else "Profile recognized", modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.SemiBold)
                                }
                                Text("@$username", modifier = Modifier.padding(top = 8.dp), fontSize = 18.sp)
                                Text(if (username.equals(profile.username, true)) {
                                    if (isRu) "Это ваш профиль на этом устройстве." else "This is your profile on this device."
                                } else {
                                    if (isRu) "Профиль распознан, но поиск других пользователей пока недоступен без сервера." else "Profile recognized, but finding other users requires the server."
                                }, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                                if (username.equals(profile.username, true)) {
                                    Button(onClick = onOpenOwnProfile, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                                        Text(if (isRu) "Открыть мой профиль" else "Open my profile")
                                    }
                                }
                            }
                        }
                    }
                    scanError?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(12.dp)) }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}
