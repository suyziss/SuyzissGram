package com.suyzissgram.ui.profile

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suyzissgram.domain.model.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val profileAccentColors = listOf(
    0xFF9B7BFFL,
    0xFF58A6FFL,
    0xFF47B881L,
    0xFFFF8A65L,
    0xFFE56BA8L,
    0xFFFFC15BL
)

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    language: AppLanguage,
    isPremium: Boolean,
    onBack: () -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onAvatarChange: (String?) -> Unit,
    onAccentColorChange: (Long) -> Unit,
    onNicknameColorChange: (Long) -> Unit,
    onProfileBackgroundColorChange: (Long) -> Unit,
    onProfileBackgroundImageChange: (String?) -> Unit,
    onShareProfile: () -> Unit,
    onOpenQr: () -> Unit,
    onSave: () -> Unit
) {
    val isRussian = language == AppLanguage.RU
    val context = LocalContext.current
    val avatarUri = state.profile.avatarUri?.let(Uri::parse)
    val backgroundUri = state.profile.profileBackgroundImageUri?.let(Uri::parse)
    val avatarScale by animateFloatAsState(if (state.isSaving) 0.84f else 1f, spring(stiffness = Spring.StiffnessLow), label = "profile_avatar_scale")

    val avatarBitmap by produceState<android.graphics.Bitmap?>(initialValue = null, avatarUri) {
        value = if (avatarUri == null) {
            null
        } else {
            withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(avatarUri)?.use { input ->
                        BitmapFactory.decodeStream(input)
                    }
                }.getOrNull()
            }
        }
    }

    val backgroundBitmap by produceState<android.graphics.Bitmap?>(initialValue = null, backgroundUri) {
        value = if (backgroundUri == null) null else withContext(Dispatchers.IO) {
            runCatching { context.contentResolver.openInputStream(backgroundUri)?.use { input -> BitmapFactory.decodeStream(input) } }.getOrNull()
        }
    }

    val backgroundPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            onProfileBackgroundImageChange(uri.toString())
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            onAvatarChange(uri.toString())
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = if (isRussian) "Назад" else "Back")
            }
            Text(
                text = if (isRussian) "Профиль" else "Profile",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 24.dp).animateContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(112.dp).then(Modifier.graphicsLayer { scaleX = avatarScale; scaleY = avatarScale }).clip(CircleShape)
                    .background(Color(state.profile.accentColor.toInt()).copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center
            ) {
                if (avatarBitmap != null) {
                    Image(
                        bitmap = avatarBitmap!!.asImageBitmap(),
                        contentDescription = if (isRussian) "Фото профиля" else "Profile photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(state.profile.accentColor.toInt()),
                        modifier = Modifier.size(52.dp)
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = {
                    photoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) {
                    Text(if (isRussian) "Изменить фото" else "Change photo")
                }
                if (state.profile.avatarUri != null) {
                    TextButton(onClick = { onAvatarChange(null) }) {
                        Text(if (isRussian) "Удалить" else "Remove")
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(onClick = onShareProfile, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(7.dp))
                    Text(if (isRussian) "Поделиться" else "Share")
                }
                androidx.compose.material3.OutlinedButton(
                    onClick = onOpenQr,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(7.dp))
                    Text(if (isRussian) "Мой QR" else "My QR")
                }
            }

            Column(Modifier.fillMaxWidth()) {
                Text(
                    if (isRussian) "Цвет профиля" else "Profile accent",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    profileAccentColors.forEach { accent ->
                        Box(
                            modifier = Modifier.size(30.dp).clip(CircleShape)
                                .background(Color(accent.toInt()))
                                .clickable { onAccentColorChange(accent) }
                                .then(
                                    if (state.profile.accentColor == accent) {
                                        Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    } else {
                                        Modifier
                                    }
                                )
                        )
                    }
                }
            }

            if (isPremium) {
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surface).padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(if (isRussian) "Premium оформление" else "Premium appearance", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Box(
                        Modifier.fillMaxWidth().height(118.dp).clip(RoundedCornerShape(14.dp))
                            .background(Color(state.profile.profileBackgroundColor.toInt())),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (backgroundBitmap != null) {
                            Image(
                                bitmap = backgroundBitmap!!.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Column(Modifier.padding(16.dp)) {
                            Text(state.profile.displayName.ifBlank { state.profile.username }, color = Color(state.profile.nicknameColor.toInt()), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text("@${state.profile.username}", color = Color(state.profile.nicknameColor.toInt()).copy(alpha = .82f), fontSize = 13.sp)
                        }
                    }
                    Text(if (isRussian) "Цвет ника" else "Nickname color", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf(0xFFFFFFFFL, 0xFFFFD166L, 0xFF7BDFF2L, 0xFFFF8FABL, 0xFFB8F2A1L, 0xFFC4A1FFL).forEach { color ->
                            Box(
                                Modifier.size(28.dp).clip(CircleShape).background(Color(color.toInt()))
                                    .clickable { onNicknameColorChange(color) }
                                    .then(if (state.profile.nicknameColor == color) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
                            )
                        }
                    }
                    Text(if (isRussian) "Фон профиля" else "Profile background", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf(0xFF17151FL, 0xFF17243AL, 0xFF17352BL, 0xFF3B2037L, 0xFF3B2B1AL, 0xFF28243BL).forEach { color ->
                            Box(
                                Modifier.size(28.dp).clip(CircleShape).background(Color(color.toInt()))
                                    .clickable { onProfileBackgroundColorChange(color) }
                                    .then(if (state.profile.profileBackgroundColor == color && backgroundUri == null) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { backgroundPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                            Text(if (isRussian) "Выбрать изображение" else "Choose image")
                        }
                        if (backgroundUri != null) {
                            TextButton(onClick = { onProfileBackgroundImageChange(null) }) {
                                Text(if (isRussian) "Убрать фон" else "Remove image")
                            }
                        }
                    }
                }
            } else {
                Text(
                    if (isRussian) "Цвет ника и собственный фон доступны в SuyzissGram Premium." else "Custom nickname colors and profile backgrounds require SuyzissGram Premium.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (state.profile.id.isNotBlank()) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Text(if (isRussian) "Постоянный ID" else "Permanent ID", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Text(state.profile.id, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                    Spacer(Modifier.size(10.dp))
                    Text(if (isRussian) "Электронная почта" else "Email", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Text(state.profile.email, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                }
            }

            OutlinedTextField(
                value = state.profile.displayName,
                onValueChange = onDisplayNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(if (isRussian) "Имя" else "Name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
            OutlinedTextField(
                value = state.profile.username,
                onValueChange = onUsernameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Username") },
                prefix = { Text("@") },
                singleLine = true,
                supportingText = {
                    Text(if (isRussian) "4–14 символов: латинские буквы, цифры и _" else "4–14 characters: letters, numbers and _")
                }
            )
            OutlinedTextField(
                value = state.profile.bio,
                onValueChange = onBioChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(if (isRussian) "О себе" else "Bio") },
                minLines = 3,
                maxLines = 5,
                supportingText = {
                    Text("${state.profile.bio.length}/120")
                }
            )

            if (state.error != null) {
                Text(state.error, color = MaterialTheme.colorScheme.error)
            }
            if (state.saved) {
                Text(
                    if (isRussian) "Изменения сохранены на устройстве" else "Changes saved on this device",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.weight(1f))
            Button(
                onClick = onSave,
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (isRussian) "Сохранить" else "Save")
                }
            }
        }
    }
}
