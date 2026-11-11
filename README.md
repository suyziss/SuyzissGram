# SuyzissGram

<p align="center"><strong>A Telegram-inspired Android messenger experience</strong><br />Kotlin · Jetpack Compose · Material 3</p>

<p align="center"><img alt="Android" src="https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white" /> <img alt="Kotlin" src="https://img.shields.io/badge/language-Kotlin-7F52FF?logo=kotlin&logoColor=white" /> <img alt="Version" src="https://img.shields.io/badge/version-0.0.3%20beta-purple" /> <img alt="Status" src="https://img.shields.io/badge/status-testing-orange" /></p>

## Version 0.0.3 βeta

**SuyzissGram is currently in active testing.** The planned online release target is **01.01.2027**. This is a target date, not a guarantee; online launch depends on backend development, testing, security and stability.

The long-term goal is a polished messenger with a familiar Telegram-style interaction model, from the chat list and settings to small everyday chat actions. SuyzissGram is an independent project with its own name and identity; it does not use Telegram's proprietary source code or branding.

## Current prototype

- Local Favorites chat with on-device message persistence.
- Settings screen with language selection (English, Russian, Japanese, Kazakh).
- Appearance preference selector and text-size preference control (initial settings UI; full visual application is still being developed).
- Confirmation dialog for clearing locally stored messages.
- Version label: `0.0.3-beta`.

## Planned Telegram-inspired experience

- Chat list, pinned and archived chats, folders, unread counters, drafts and search.
- Message replies, forwarding, editing, deletion, reactions, selection and context menus.
- Photos, videos, files, albums, voice messages and media previews.
- Profiles, contacts, usernames, groups and channels.
- Light/dark/system themes, accent colors, chat backgrounds and detailed text settings.
- Privacy controls, app lock, sessions, notifications and account settings.
- Small interaction details such as swipe actions, typing indicators, delivery states and smooth navigation.

These are roadmap items, not features already available in the current APK.

## Current limitations

This build is **local-only**, not an online multi-user messenger. It has no account system, server, cloud synchronization, delivery to other users, calls, push notifications or end-to-end encryption. App lock and advanced privacy controls are not implemented. Do not store sensitive information in this test build.

## Download the test APK

1. Open [GitHub Actions](https://github.com/suyziss/SuyzissGram/actions).
2. Select the latest successful **Android APK** workflow run.
3. Download the `SuyzissGram-debug-apk` artifact.
4. Extract the ZIP and install the APK on an Android test device.

Debug APKs are test builds and may contain unfinished features or bugs.

## Build locally

Requirements: Android Studio, JDK 17, Android SDK 35 and Gradle 8.9.

```bash
gradle --no-daemon assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## Feedback

Report bugs and suggestions through [GitHub Issues](https://github.com/suyziss/SuyzissGram/issues). Include reproduction steps and device details where possible.

---

<p align="center">Made by <a href="https://github.com/suyziss">@suyziss</a><br /><sub>Testing now · Planned online release: 01.01.2027</sub></p>