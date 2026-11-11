package com.suyzissgram

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Page = Color(0xFF101019)
private val Panel = Color(0xFF191925)
private val Accent = Color(0xFF9B7BFF)
private val Muted = Color(0xFF9291A5)

data class Chat(val name: String, val message: String, val time: String, val initials: String, val unread: Int = 0)

private val sampleChats = listOf(
    Chat("Suyziss Team", "Добро пожаловать в SuyzissGram", "12:42", "ST", 2),
    Chat("Alex", "Отправил тебе файл", "11:18", "A"),
    Chat("Design Group", "Новый макет готов", "Вчера", "DG", 4),
    Chat("Mira", "Хорошо, договорились", "Вт", "M"),
    Chat("Saved Messages", "Заметки и избранное", "Пн", "★")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SuyzissApp() }
    }
}

@Composable
fun SuyzissApp() {
    var selected by remember { mutableStateOf("Чаты") }
    var search by remember { mutableStateOf("") }
    MaterialTheme(colorScheme = darkColorScheme(
        background = Page, surface = Panel, primary = Accent,
        onBackground = Color.White, onSurface = Color.White
    )) {
        Scaffold(
            containerColor = Page,
            bottomBar = {
                NavigationBar(containerColor = Panel, contentColor = Color.White) {
                    listOf("Контакты", "Звонки", "Чаты", "Настройки").forEach { item ->
                        val icon = when(item) {
                            "Контакты" -> Icons.Default.People
                            "Звонки" -> Icons.Default.Call
                            "Чаты" -> Icons.Default.Chat
                            else -> Icons.Default.Settings
                        }
                        NavigationBarItem(
                            selected = selected == item,
                            onClick = { selected = item },
                            icon = { Icon(icon, contentDescription = item) },
                            label = { Text(item, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Accent,
                                selectedTextColor = Accent,
                                indicatorColor = Accent.copy(alpha = .15f),
                                unselectedIconColor = Muted,
                                unselectedTextColor = Muted
                            )
                        )
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { selected = "Новый чат" },
                    containerColor = Accent,
                    contentColor = Color.White
                ) { Icon(Icons.Default.Edit, contentDescription = "Новый чат") }
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("SUYZISSGRAM", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Text(selected, color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { selected = "Настройки" }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Меню", tint = Color.White)
                    }
                }
                if (selected == "Чаты") {
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        placeholder = { Text("Поиск", color = Muted) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Muted) },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Accent, unfocusedBorderColor = Panel,
                            focusedContainerColor = Panel, unfocusedContainerColor = Panel
                        )
                    )
                    Spacer(Modifier.height(12.dp))
                    LazyColumn {
                        items(sampleChats.filter { it.name.contains(search, true) || it.message.contains(search, true) }) { chat ->
                            ChatRow(chat) { selected = chat.name }
                        }
                    }
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ChatBubbleOutline, null, tint = Accent, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(
                                when(selected) {
                                    "Контакты" -> "Контакты появятся здесь"
                                    "Звонки" -> "История звонков появится здесь"
                                    "Настройки" -> "Настройки аккаунта"
                                    "Новый чат" -> "Поиск пользователей скоро появится"
                                    else -> selected
                                },
                                color = Color.White
                            )
                            Text("SuyzissGram · Preview 0.1.0", color = Muted, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatRow(chat: Chat, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(52.dp).background(Accent.copy(alpha = .20f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(chat.initials, color = Accent, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(chat.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Spacer(Modifier.height(4.dp))
            Text(chat.message, color = Muted, fontSize = 13.sp, maxLines = 1)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(chat.time, color = Muted, fontSize = 11.sp)
            if (chat.unread > 0) {
                Spacer(Modifier.height(5.dp))
                Box(Modifier.background(Accent, CircleShape).padding(horizontal = 7.dp, vertical = 3.dp)) {
                    Text(chat.unread.toString(), color = Color.White, fontSize = 10.sp)
                }
            }
        }
    }
}
