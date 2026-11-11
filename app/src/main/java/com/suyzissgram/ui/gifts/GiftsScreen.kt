package com.suyzissgram.ui.gifts

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suyzissgram.domain.model.AppLanguage
import com.suyzissgram.domain.model.GiftCatalog
import com.suyzissgram.domain.model.GiftDefinition
import com.suyzissgram.domain.model.GiftHistoryEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GiftsScreen(
    state: GiftUiState,
    language: AppLanguage,
    onBack: () -> Unit,
    onSendGift: (String) -> Unit,
    onClearError: () -> Unit
) {
    val isRussian = language == AppLanguage.RU
    var selectedGift by remember { mutableStateOf<GiftDefinition?>(null) }

    if (selectedGift != null) {
        val gift = selectedGift!!
        AlertDialog(
            onDismissRequest = { selectedGift = null },
            title = { Text(if (isRussian) "Отправить подарок?" else "Send this gift?") },
            text = {
                Text(
                    if (isRussian) {
                        "Подарок «${gift.nameRu}» будет добавлен в локальную историю. Стоимость: ${gift.price} демо Stars."
                    } else {
                        "The ${gift.nameEn} gift will be added to local history. Cost: ${gift.price} demo Stars."
                    }
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !state.isSending && state.wallet.starsBalance >= gift.price,
                    onClick = {
                        onSendGift(gift.id)
                        selectedGift = null
                    }
                ) {
                    Text(if (isRussian) "Подтвердить" else "Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedGift = null }) {
                    Text(if (isRussian) "Отмена" else "Cancel")
                }
            }
        )
    }

    if (state.error != null) {
        AlertDialog(
            onDismissRequest = onClearError,
            title = { Text(if (isRussian) "Не удалось отправить" else "Could not send gift") },
            text = {
                Text(
                    if (state.error == "NOT_ENOUGH_STARS" && isRussian) {
                        "Недостаточно демо Stars для этого подарка."
                    } else if (state.error == "NOT_ENOUGH_STARS") {
                        "You do not have enough demo Stars for this gift."
                    } else {
                        state.error
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = onClearError) {
                    Text(if (isRussian) "Понятно" else "OK")
                }
            }
        )
    }

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = if (isRussian) "Назад" else "Back")
            }
            Text(
                if (isRussian) "Подарки" else "Gifts",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (isRussian) "Демо-баланс" else "Demo balance",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 13.sp
                            )
                            Text(
                                state.wallet.starsBalance.toString(),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 30.sp
                            )
                            Text(
                                if (isRussian) "Только для тестирования" else "For testing only",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 11.sp
                            )
                        }
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }
                Text(
                    if (isRussian) {
                        "Подарки сохраняются только на этом устройстве. Демо Stars не являются валютой и не имеют денежной стоимости."
                    } else {
                        "Gifts are saved on this device only. Demo Stars are not currency and have no cash value."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }

            item {
                Text(
                    if (isRussian) "Каталог подарков" else "Gift catalog",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 17.sp
                )
            }

            items(GiftCatalog.items, key = { it.id }) { gift ->
                GiftCard(
                    gift = gift,
                    isRussian = isRussian,
                    balance = state.wallet.starsBalance,
                    isSending = state.isSending,
                    onSelect = { selectedGift = gift }
                )
            }

            item {
                Text(
                    if (isRussian) "История" else "History",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 17.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (state.wallet.history.isEmpty()) {
                item {
                    Text(
                        if (isRussian) "Пока подарков нет" else "No gifts yet",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            } else {
                items(state.wallet.history, key = { it.id }) { entry ->
                    GiftHistoryRow(entry, isRussian)
                }
            }
        }
    }
}

@Composable
private fun GiftCard(
    gift: GiftDefinition,
    isRussian: Boolean,
    balance: Int,
    isSending: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GiftArtwork(gift)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (isRussian) gift.nameRu else gift.nameEn,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
                Text(
                    if (isRussian) gift.descriptionRu else gift.descriptionEn,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(gift.accentColor.toInt()),
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        " ${gift.price}",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp
                    )
                }
            }
            Button(
                onClick = onSelect,
                enabled = !isSending && balance >= gift.price
            ) {
                Text(if (isRussian) "Выбрать" else "Select")
            }
        }
    }
}

@Composable
private fun GiftArtwork(gift: GiftDefinition) {
    val transition = rememberInfiniteTransition(label = "gift-artwork")
    val pulse by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gift-pulse"
    )
    val giftColor = Color(gift.accentColor.toInt())
    val ribbonColor = Color(0xFFFFE4A1)

    Canvas(
        Modifier.size(76.dp).graphicsLayer {
            scaleX = pulse
            scaleY = pulse
        }
    ) {
        val width = size.width
        val height = size.height
        drawRoundRect(
            color = giftColor,
            topLeft = Offset(width * 0.15f, height * 0.42f),
            size = Size(width * 0.70f, height * 0.48f),
            cornerRadius = CornerRadius(width * 0.08f)
        )
        drawRoundRect(
            color = giftColor.copy(alpha = 0.82f),
            topLeft = Offset(width * 0.09f, height * 0.31f),
            size = Size(width * 0.82f, height * 0.18f),
            cornerRadius = CornerRadius(width * 0.07f)
        )
        drawRect(
            color = ribbonColor,
            topLeft = Offset(width * 0.44f, height * 0.32f),
            size = Size(width * 0.12f, height * 0.57f)
        )
        drawRect(
            color = ribbonColor,
            topLeft = Offset(width * 0.16f, height * 0.46f),
            size = Size(width * 0.68f, height * 0.09f)
        )

        val leftBow = Path().apply {
            moveTo(width * 0.50f, height * 0.32f)
            cubicTo(width * 0.15f, height * 0.05f, width * 0.14f, height * 0.42f, width * 0.48f, height * 0.38f)
            close()
        }
        val rightBow = Path().apply {
            moveTo(width * 0.50f, height * 0.32f)
            cubicTo(width * 0.85f, height * 0.05f, width * 0.86f, height * 0.42f, width * 0.52f, height * 0.38f)
            close()
        }
        drawPath(leftBow, giftColor)
        drawPath(rightBow, giftColor)
        drawCircle(ribbonColor, radius = width * 0.065f, center = Offset(width * 0.50f, height * 0.36f))
        drawRoundRect(
            color = Color.White.copy(alpha = 0.35f),
            topLeft = Offset(width * 0.15f, height * 0.42f),
            size = Size(width * 0.70f, height * 0.48f),
            cornerRadius = CornerRadius(width * 0.08f),
            style = Stroke(width = width * 0.018f)
        )
    }
}

@Composable
private fun GiftHistoryRow(entry: GiftHistoryEntry, isRussian: Boolean) {
    val formatter = remember(isRussian) {
        SimpleDateFormat(if (isRussian) "dd.MM.yyyy HH:mm" else "yyyy-MM-dd HH:mm", Locale.getDefault())
    }
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                if (isRussian) entry.giftNameRu else entry.giftNameEn,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                formatter.format(Date(entry.createdAt)),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
        Text(
            "-${entry.price}",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp
        )
    }
}
