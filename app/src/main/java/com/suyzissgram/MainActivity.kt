package com.suyzissgram

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Page = Color(0xFF0E0D14)
private val Panel = Color(0xFF191722)
private val Accent = Color(0xFF9B7BFF)
private val Muted = Color(0xFF9995A8)
private val Bubble = Color(0xFF292238)

private enum class AppLanguage { EN, RU, JA, KK }

private data class ChatMessage(
    val id: Long,
    val text: String,
    val time: String,
    val outgoing: Boolean
)

private fun strings(language: AppLanguage) = when (language) {
    AppLanguage.EN -> mapOf(
        "chats" to "Chats", "favorite" to "Favorite", "subtitle" to "Your personal space",
        "empty" to "No messages yet", "empty_hint" to "Write the first message to yourself",
        "message" to "Message", "language" to "Language", "local" to "Stored on this device",
        "today" to "Today", "send" to "Send"
    )
    AppLanguage.RU -> mapOf(
        "chats" to "Чаты", "favorite" to "Избранное", "subtitle" to "Личное пространство",
        "empty" to "Пока нет сообщений", "empty_hint" to "Напиши первое сообщение себе",
        "message" to "Сообщение", "language" to "Язык", "local" to "Хранится на этом устройстве",
        "today" to "Сегодня", "send" to "Отправить"
    )
    AppLanguage.JA -> mapOf(
        "chats" to "チャット", "favorite" to "お気に入り", "subtitle" to "あなたの個人スペース",
        "empty" to "メッセージはありません", "empty_hint" to "最初のメッセージを書いてみましょう",
        "message" to "メッセージ", "language" to "言語", "local" to "この端末に保存されます",
        "today" to "今日", "send" to "送信"
    )
    AppLanguage.KK -> mapOf(
        "chats" to "Чаттар", "favorite" to "Таңдаулылар", "subtitle" to "Жеке кеңістігің",
        "empty" to "Әзірге хабарлама жоқ", "empty_hint" to "Өзіңе алғашқы хабарламаны жаз",
        "message" to "Хабарлама", "language" to "Тіл", "local" to "Осы құрылғыда сақталады",
        "today" to "Бүгін", "send" to "Жіберу"
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(14, 13, 20)
        window.navigationBarColor = android.graphics.Color.rgb(14, 13, 20)
        setContent { SuyzissApp() }
    }
}

@Composable
private fun SuyzissApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("suyzissgram_local", Context.MODE_PRIVATE) }
    var language by remember {
        mutableStateOf(runCatching { AppLanguage.valueOf(prefs.getString("language", "RU") ?: "RU") }.getOrDefault(AppLanguage.RU))
    }
    val t = strings(language)
    var inChat by remember { mutableStateOf(false) }
    var languageMenu by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessage>().apply { addAll(loadMessages(prefs)) } }

    fun save() {
        val array = JSONArray()
        messages.forEach { message ->
            array.put(JSONObject().put("id", message.id).put("text", message.text)
                .put("time", message.time).put("outgoing", message.outgoing))
        }
        prefs.edit().putString("messages", array.toString()).apply()
    }

    fun sendMessage() {
        val value = draft.trim()
        if (value.isNotEmpty()) {
            val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            messages.add(ChatMessage(System.currentTimeMillis(), value, time, true))
            draft = ""
            save()
        }
    }

    BackHandler(enabled = inChat) { inChat = false }

    MaterialTheme(colorScheme = darkColorScheme(
        background = Page, surface = Panel, primary = Accent,
        onBackground = Color.White, onSurface = Color.White
    )) {
        Surface(Modifier.fillMaxSize(), color = Page) {
            AnimatedContent(
                targetState = inChat,
                transitionSpec = {
                    if (targetState) {
                        (slideInHorizontally(tween(260)) { it / 3 } + fadeIn(tween(220))) togetherWith
                            (slideOutHorizontally(tween(220)) { -it / 5 } + fadeOut(tween(180)))
                    } else {
                        (slideInHorizontally(tween(260)) { -it / 5 } + fadeIn(tween(220))) togetherWith
                            (slideOutHorizontally(tween(220)) { it / 3 } + fadeOut(tween(180)))
                    }
                },
                label = "screen_transition"
            ) { chatOpen ->
                if (chatOpen) {
                    ChatScreen(
                        title = t.getValue("favorite"),
                        subtitle = t.getValue("local"),
                        emptyTitle = t.getValue("empty"),
                        emptyHint = t.getValue("empty_hint"),
                        messageHint = t.getValue("message"),
                        sendLabel = t.getValue("send"),
                        messages = messages,
                        draft = draft,
                        onDraftChange = { draft = it },
                        onSend = { sendMessage() },
                        onBack = { inChat = false }
                    )
                } else {
                    Column(Modifier.fillMaxSize()) {
                        Row(
                            Modifier.fillMaxWidth().padding(start = 22.dp, end = 16.dp, top = 22.dp, bottom = 18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("SUYZISSGRAM", color = Accent, fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold, letterSpacing = 2.2.sp)
                                Spacer(Modifier.height(5.dp))
                                Text(t.getValue("chats"), color = Color.White,
                                    fontSize = 29.sp, fontWeight = FontWeight.Bold)
                            }
                            Box {
                                IconButton(onClick = { languageMenu = true }) {
                                    Icon(Icons.Default.Language, contentDescription = t.getValue("language"), tint = Color.White)
                                }
                                DropdownMenu(expanded = languageMenu, onDismissRequest = { languageMenu = false },
                                    containerColor = Panel) {
                                    listOf(
                                        AppLanguage.EN to "English",
                                        AppLanguage.RU to "Русский",
                                        AppLanguage.JA to "日本語",
                                        AppLanguage.KK to "Қазақша"
                                    ).forEach { (code, label) ->
                                        DropdownMenuItem(
                                            text = { Text(label, color = if (language == code) Accent else Color.White) },
                                            onClick = {
                                                language = code
                                                prefs.edit().putString("language", code.name).apply()
                                                languageMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(20.dp)).background(Panel)
                                .clickable { inChat = true }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier.size(54.dp).clip(CircleShape).background(Accent.copy(alpha = .18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Accent, modifier = Modifier.size(27.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(t.getValue("favorite"), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                Spacer(Modifier.height(4.dp))
                                Text(messages.lastOrNull()?.text ?: t.getValue("subtitle"),
                                    color = Muted, fontSize = 13.sp, maxLines = 1)
                            }
                            Text(messages.lastOrNull()?.time ?: "", color = Muted, fontSize = 11.sp)
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(t.getValue("local"), color = Muted, fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 20.dp))
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.ChatBubbleOutline, null, tint = Accent.copy(alpha = .65f),
                                    modifier = Modifier.size(34.dp))
                                Spacer(Modifier.height(9.dp))
                                Text("SuyzissGram Beta · Offline", color = Muted, fontSize = 12.sp)
                            }
                        }

                    }
                }
            }
        }
    }
}

@Composable
private fun ChatScreen(
    title: String,
    subtitle: String,
    emptyTitle: String,
    emptyHint: String,
    messageHint: String,
    sendLabel: String,
    messages: List<ChatMessage>,
    draft: String,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onBack: () -> Unit
) {
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().background(Panel).padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Box(Modifier.size(40.dp).clip(CircleShape).background(Accent.copy(alpha = .2f)),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Star, null, tint = Accent)
            }
            Spacer(Modifier.width(11.dp))
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(subtitle, color = Muted, fontSize = 11.sp)
            }
        }
        if (messages.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(66.dp).clip(CircleShape).background(Accent.copy(alpha = .12f)),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Star, null, tint = Accent, modifier = Modifier.size(30.dp))
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(emptyTitle, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                    Spacer(Modifier.height(5.dp))
                    Text(emptyHint, color = Muted, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp),
                state = listState,
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement =
                        if (message.outgoing) Arrangement.End else Arrangement.Start) {
                        Column(
                            Modifier.widthIn(max = 300.dp).clip(
                                RoundedCornerShape(
                                    topStart = 18.dp, topEnd = 18.dp,
                                    bottomStart = if (message.outgoing) 18.dp else 5.dp,
                                    bottomEnd = if (message.outgoing) 5.dp else 18.dp
                                )
                            ).background(if (message.outgoing) Bubble else Panel).padding(horizontal = 13.dp, vertical = 9.dp)
                        ) {
                            Text(message.text, color = Color.White, fontSize = 15.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(message.time, color = Muted, fontSize = 10.sp,
                                modifier = Modifier.align(Alignment.End))
                        }
                    }
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().background(Page).navigationBarsPadding()
                .imePadding().padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = onDraftChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(messageHint, color = Muted) },
                shape = RoundedCornerShape(24.dp),
                maxLines = 5,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Accent, unfocusedBorderColor = Panel,
                    focusedContainerColor = Panel, unfocusedContainerColor = Panel,
                    cursorColor = Accent
                )
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(
                onClick = onSend,
                enabled = draft.isNotBlank(),
                modifier = Modifier.size(50.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Accent, disabledContainerColor = Panel
                )
            ) {
                Icon(Icons.Default.Send, contentDescription = sendLabel, tint = Color.White)
            }
        }
    }
}

private fun loadMessages(prefs: android.content.SharedPreferences): List<ChatMessage> {
    return runCatching {
        val array = JSONArray(prefs.getString("messages", "[]"))
        buildList {
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                add(ChatMessage(
                    item.getLong("id"),
                    item.getString("text"),
                    item.getString("time"),
                    item.optBoolean("outgoing", true)
                ))
            }
        }
    }.getOrDefault(emptyList())
}
