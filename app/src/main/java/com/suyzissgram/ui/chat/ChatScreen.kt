package com.suyzissgram.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suyzissgram.ui.i18n.AppStrings

@Composable
fun ChatScreen(
    state: ChatUiState,
    labels: AppStrings,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onBack: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.lastIndex)
    }

    Column(Modifier.fillMaxSize().background(colors.background)) {
        Row(
            Modifier.fillMaxWidth().background(colors.surface).padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = labels.back, tint = colors.onSurface)
            }
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(colors.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = colors.primary)
            }
            Spacer(Modifier.width(11.dp))
            Column {
                Text(labels.favorite, color = colors.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(labels.local, color = colors.onSurfaceVariant, fontSize = 11.sp)
            }
        }

        if (state.isLoading) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Loading…", color = colors.onSurfaceVariant)
            }
        } else if (state.messages.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(66.dp).clip(CircleShape).background(colors.primary.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = colors.primary, modifier = Modifier.size(30.dp))
                    }
                    Spacer(Modifier.size(14.dp))
                    Text(labels.empty, color = colors.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                    Spacer(Modifier.size(5.dp))
                    Text(labels.emptyHint, color = colors.onSurfaceVariant, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp),
                state = listState,
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.messages, key = { it.id }) { message ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = if (message.outgoing) Arrangement.End else Arrangement.Start
                    ) {
                        Column(
                            Modifier.widthIn(max = 300.dp)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 18.dp,
                                        topEnd = 18.dp,
                                        bottomStart = if (message.outgoing) 18.dp else 5.dp,
                                        bottomEnd = if (message.outgoing) 5.dp else 18.dp
                                    )
                                )
                                .background(if (message.outgoing) colors.primary.copy(alpha = 0.22f) else colors.surface)
                                .padding(horizontal = 13.dp, vertical = 9.dp)
                        ) {
                            Text(message.text, color = colors.onSurface, fontSize = 15.sp)
                            Spacer(Modifier.size(4.dp))
                            Text(message.time, color = colors.onSurfaceVariant, fontSize = 10.sp, modifier = Modifier.align(Alignment.End))
                        }
                    }
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().background(colors.background).navigationBarsPadding()
                .imePadding().padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = state.draft,
                onValueChange = onDraftChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(labels.message, color = colors.onSurfaceVariant) },
                shape = RoundedCornerShape(24.dp),
                maxLines = 5,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.surface,
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    cursorColor = colors.primary
                )
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(
                onClick = onSend,
                enabled = state.draft.isNotBlank(),
                modifier = Modifier.size(50.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = labels.send)
            }
        }
        if (state.error != null) {
            Text(state.error, color = colors.error, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }
    }
}
