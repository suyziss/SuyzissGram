package com.suyzissgram.ui.chatlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suyzissgram.ui.i18n.AppStrings

@Composable
fun ChatListScreen(
    state: ChatListUiState,
    labels: AppStrings,
    onOpenChat: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxSize().background(colors.background)) {
        Row(
            Modifier.fillMaxWidth().padding(start = 22.dp, end = 16.dp, top = 22.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "SUYZISSGRAM",
                    color = colors.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.2.sp
                )
                Spacer(Modifier.size(5.dp))
                Text(labels.chats, color = colors.onBackground, fontSize = 29.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onOpenProfile) {
                Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = colors.onBackground)
            }
            IconButton(onClick = onOpenProfile) {
                Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = colors.onBackground)
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Default.Settings, contentDescription = labels.settings, tint = colors.onBackground)
            }
        }

        if (state.isLoading) {
            Text("Loading…", color = colors.onBackground, modifier = Modifier.padding(20.dp))
        } else if (state.error != null) {
            Text(state.error, color = colors.error, modifier = Modifier.padding(20.dp))
        } else {
            state.chats.forEach { chat ->
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.surface)
                        .clickable(onClick = onOpenChat)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(54.dp).clip(CircleShape).background(colors.primary.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = colors.primary, modifier = Modifier.size(27.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(labels.favorite, color = colors.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Spacer(Modifier.size(4.dp))
                        Text(chat.lastMessage.ifBlank { labels.subtitle }, color = colors.onSurfaceVariant, fontSize = 13.sp, maxLines = 1)
                    }
                    Text(chat.lastMessageTime, color = colors.onSurfaceVariant, fontSize = 11.sp)
                }
            }
            Spacer(Modifier.size(14.dp))
            Text(labels.local, color = colors.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 20.dp))
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = colors.primary.copy(alpha = 0.65f), modifier = Modifier.size(34.dp))
                    Spacer(Modifier.size(9.dp))
                    Text("SuyzissGram · 0.0.3 βeta", color = colors.onSurfaceVariant, fontSize = 12.sp)
                }
            }
        }
    }
}
