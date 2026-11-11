package com.suyzissgram.ui.i18n

import com.suyzissgram.domain.model.AppLanguage

data class AppStrings(
    val chats: String,
    val favorite: String,
    val subtitle: String,
    val empty: String,
    val emptyHint: String,
    val message: String,
    val language: String,
    val local: String,
    val send: String,
    val settings: String,
    val appearance: String,
    val theme: String,
    val dark: String,
    val light: String,
    val system: String,
    val textSize: String,
    val storage: String,
    val clearMessages: String,
    val clearConfirm: String,
    val cancel: String,
    val clear: String,
    val privacy: String,
    val privacyNote: String,
    val version: String,
    val testing: String,
    val back: String
) {
    companion object {
        fun forLanguage(language: AppLanguage): AppStrings = when (language) {
            AppLanguage.EN -> AppStrings(
                "Chats", "Favorite", "Your personal space", "No messages yet",
                "Write the first message to yourself", "Message", "Language",
                "Stored on this device", "Send", "Settings", "Appearance", "Theme",
                "Dark", "Light", "System", "Text size", "Storage",
                "Clear local messages", "Delete all locally saved messages?",
                "Cancel", "Clear", "Privacy",
                "App lock and account security will be added before online release.",
                "Version", "Testing build", "Back"
            )
            AppLanguage.RU -> AppStrings(
                "Чаты", "Избранное", "Личное пространство", "Пока нет сообщений",
                "Напиши первое сообщение себе", "Сообщение", "Язык",
                "Хранится на этом устройстве", "Отправить", "Настройки", "Внешний вид",
                "Тема", "Тёмная", "Светлая", "Как в системе", "Размер текста",
                "Хранилище", "Удалить локальные сообщения",
                "Удалить все сохранённые на устройстве сообщения?", "Отмена",
                "Удалить", "Конфиденциальность",
                "Блокировка приложения и защита аккаунта появятся до онлайн-релиза.",
                "Версия", "Тестовая сборка", "Назад"
            )
            AppLanguage.JA -> AppStrings(
                "チャット", "お気に入り", "あなたの個人スペース", "メッセージはありません",
                "最初のメッセージを書いてみましょう", "メッセージ", "言語",
                "この端末に保存されます", "送信", "設定", "外観", "テーマ",
                "ダーク", "ライト", "システム", "文字サイズ", "ストレージ",
                "ローカルメッセージを削除", "保存されたメッセージをすべて削除しますか？",
                "キャンセル", "削除", "プライバシー",
                "アプリロックとアカウント保護はオンライン公開前に追加予定です。",
                "バージョン", "テストビルド", "戻る"
            )
            AppLanguage.KK -> AppStrings(
                "Чаттар", "Таңдаулылар", "Жеке кеңістігің", "Әзірге хабарлама жоқ",
                "Өзіңе алғашқы хабарламаны жаз", "Хабарлама", "Тіл",
                "Осы құрылғыда сақталады", "Жіберу", "Баптаулар", "Көрініс", "Тақырып",
                "Қараңғы", "Ашық", "Жүйелік", "Мәтін өлшемі", "Жад",
                "Жергілікті хабарламаларды өшіру",
                "Құрылғыда сақталған барлық хабарламалар өшірілсін бе?",
                "Болдырмау", "Өшіру", "Құпиялылық",
                "Қолданба құлпы мен аккаунт қауіпсіздігі онлайн іске қосылғанға дейін қосылады.",
                "Нұсқа", "Сынақ нұсқасы", "Артқа"
            )
        }
    }
}
