package com.suyzissgram.ui.profile

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
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

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    language: AppLanguage,
    onBack: () -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onAvatarChange: (String?) -> Unit,
    onSave: () -> Unit
) {
    val isRussian = language == AppLanguage.RU
    val context = LocalContext.current
    val avatarUri = state.profile.avatarUri?.let(Uri::parse)

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
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(112.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
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
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
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
                    Text(if (isRussian) "Латинские буквы, цифры и _" else "Letters, numbers and _")
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
