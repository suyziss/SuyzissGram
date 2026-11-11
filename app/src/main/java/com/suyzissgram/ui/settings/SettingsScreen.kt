package com.suyzissgram.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suyzissgram.domain.model.AppLanguage
import com.suyzissgram.domain.model.ThemeChoice
import com.suyzissgram.ui.i18n.AppStrings

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    labels: AppStrings,
    onBack: () -> Unit,
    onOpenProfile: () -> Unit,
    onLanguage: (AppLanguage) -> Unit,
    onTheme: (ThemeChoice) -> Unit,
    onTextScale: (Float) -> Unit,
    onClearMessages: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var languageExpanded by remember { mutableStateOf(false) }
    var themeExpanded by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    val languageName = when (state.settings.language) {
        AppLanguage.EN -> "English"
        AppLanguage.RU -> "Русский"
        AppLanguage.JA -> "日本語"
        AppLanguage.KK -> "Қазақша"
    }
    val themeName = when (state.settings.theme) {
        ThemeChoice.LIGHT -> labels.light
        ThemeChoice.SYSTEM -> labels.system
        ThemeChoice.DARK -> labels.dark
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(labels.storage) },
            text = { Text(labels.clearConfirm) },
            confirmButton = {
                TextButton(onClick = {
                    onClearMessages()
                    showClearDialog = false
                }) { Text(labels.clear) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text(labels.cancel) }
            }
        )
    }

    Column(Modifier.fillMaxSize().background(colors.background)) {
        Row(
            Modifier.fillMaxWidth().background(colors.surface).padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = labels.back)
            }
            Text(labels.settings, color = colors.onSurface, fontSize = 20.sp)
        }

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SettingsRow(
                    Icons.Default.Person,
                    if (state.settings.language == AppLanguage.RU) "Профиль" else "Profile",
                    if (state.settings.language == AppLanguage.RU) "Имя, фото и описание" else "Name, photo and bio",
                    onOpenProfile
                )
            }
            item { SectionTitle(labels.appearance) }
            item {
                SettingsRow(Icons.Default.Palette, labels.theme, themeName) { themeExpanded = true }
                DropdownMenu(expanded = themeExpanded, onDismissRequest = { themeExpanded = false }) {
                    listOf(
                        ThemeChoice.SYSTEM to labels.system,
                        ThemeChoice.LIGHT to labels.light,
                        ThemeChoice.DARK to labels.dark
                    ).forEach { (choice, title) ->
                        DropdownMenuItem(text = { Text(title) }, onClick = {
                            onTheme(choice)
                            themeExpanded = false
                        })
                    }
                }
            }
            item {
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                        .background(colors.surface).padding(16.dp)
                ) {
                    Text(labels.textSize, color = colors.onSurface)
                    Slider(
                        value = state.settings.textScale,
                        onValueChange = onTextScale,
                        valueRange = 0.85f..1.25f
                    )
                    Text("${(state.settings.textScale * 100).toInt()}%", color = colors.onSurfaceVariant, fontSize = 12.sp)
                }
            }
            item { SectionTitle(labels.language) }
            item {
                SettingsRow(Icons.Default.Language, labels.language, languageName) { languageExpanded = true }
                DropdownMenu(expanded = languageExpanded, onDismissRequest = { languageExpanded = false }) {
                    listOf(
                        AppLanguage.EN to "English",
                        AppLanguage.RU to "Русский",
                        AppLanguage.JA to "日本語",
                        AppLanguage.KK to "Қазақша"
                    ).forEach { (code, name) ->
                        DropdownMenuItem(text = { Text(name) }, onClick = {
                            onLanguage(code)
                            languageExpanded = false
                        })
                    }
                }
            }
            item { SectionTitle(labels.storage) }
            item {
                SettingsRow(Icons.Default.Delete, labels.clearMessages, "") {
                    showClearDialog = true
                }
            }
            item { SectionTitle(labels.privacy) }
            item {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                        .background(colors.surface).padding(16.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = colors.primary)
                    Spacer(Modifier.width(12.dp))
                    Text(labels.privacyNote, color = colors.onSurfaceVariant, fontSize = 13.sp)
                }
            }
            item {
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                        .background(colors.surface).padding(16.dp)
                ) {
                    Text(labels.version, color = colors.onSurface)
                    Text("0.8.004 βeta · ${labels.testing}", color = colors.onSurfaceVariant, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title.uppercase(),
        color = MaterialTheme.colorScheme.primary,
        fontSize = 12.sp
    )
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(14.dp))
        Text(title, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f), fontSize = 15.sp)
        if (value.isNotEmpty()) {
            Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
