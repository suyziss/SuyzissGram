# SuyzissGram

<p align="center">
  <img src="https://raw.githubusercontent.com/suyziss/Ludora/main/file_00000000267c824690fc0a9b227b0405.png" alt="Suyziss character" width="220" />
</p>

<h1 align="center">SuyzissGram</h1>
<p align="center"><strong>Your conversations. Your space.</strong><br/>An independent Android messenger project inspired by familiar modern messaging apps.</p>

<p align="center">
  <img alt="Android" src="https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white" />
  <img alt="Kotlin" src="https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white" />
  <img alt="Jetpack Compose" src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4" />
  <img alt="Version" src="https://img.shields.io/badge/Version-0.0.3%20βeta-8A63D2" />
  <img alt="Status" src="https://img.shields.io/badge/Status-Testing-orange" />
</p>

---

## About

**SuyzissGram** is an independent Android messenger being developed with Kotlin and Jetpack Compose. The project aims to deliver a polished, familiar messaging experience, paying attention not only to the main screens but also to the small interactions, settings and customization details that make a messenger comfortable to use.

The project has its own identity and implementation. It is inspired by the interaction patterns of Telegram, but does not use Telegram's proprietary source code or present itself as an official Telegram client.

## Current release — 0.0.3 βeta

<p align="center"><strong>Early development · Local prototype · Android</strong></p>

Available in the current prototype:

- Local Favorites chat with on-device message persistence.
- Settings screen with language selection: English, Russian, Japanese and Kazakh.
- Functional dark, light and system theme selection.
- App-wide text scaling preference.
- Confirmation before clearing locally stored messages.
- Back navigation between the main screen, chats and settings.

The UI has been split into feature packages with ViewModels and repository interfaces. The current version remains an early local prototype, not a finished messenger.

## Architecture

The current package structure, dependency direction, DI decision and future Gradle module plan are documented in [Phase 0 — Step 1](docs/architecture/phase-0-step-1.md).

## Interface and feature roadmap

### Chats and navigation
- [ ] Conversation list with avatars, previews and timestamps.
- [ ] Pinned and archived chats, folders and unread counters.
- [ ] Search, drafts and chat-specific settings.

### Messaging
- [ ] Replies, forwarding, editing and deleting messages.
- [ ] Reactions, message selection and context menus.
- [ ] Photo, video, file and album sharing.
- [ ] Voice messages and media previews.

### Personalization
- [ ] Fully functional light, dark and system themes.
- [ ] Accent colors, chat backgrounds and detailed text controls.
- [ ] Profile, username and avatar customization.
- [ ] Complete multilingual interface.

### Accounts and online features
- [ ] Account registration and authentication.
- [ ] Backend API, database and cloud synchronization.
- [ ] Real-time messaging, groups and channels.
- [ ] Push notifications and multi-device sessions.
- [ ] Privacy controls and application lock.

Roadmap items are planned work; they are not claims about features already implemented.

## Download and test

No stable public release is available yet. Test APKs are produced by GitHub Actions when the build workflow succeeds.

1. Open [GitHub Actions](https://github.com/suyziss/SuyzissGram/actions).
2. Select the latest successful Android build.
3. Download the `SuyzissGram-debug-apk` artifact.
4. Extract the archive and install the APK on an Android test device.

Debug builds may contain unfinished features and bugs. Only install builds you have chosen to test.

## Build from source

Requirements: JDK 17, Android SDK 35 and Gradle 8.9.

```bash
gradle --no-daemon assembleDebug
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Project status

- **Current version:** `0.0.3-beta`
- **Development status:** active testing
- **Architecture:** native Android app, Kotlin + Jetpack Compose
- **Current mode:** local-only prototype
- **Online release target:** `01.01.2027` (planned target, not a guaranteed release date)

### Current limitations

The current build does not yet provide user accounts, a production server, cloud synchronization, messaging between different users, calls, push notifications or end-to-end encryption. Application lock and advanced privacy protections are also not implemented. Avoid using the prototype to store sensitive information.

## Feedback and contributions

Found a bug or have an idea? Open a [GitHub Issue](https://github.com/suyziss/SuyzissGram/issues). Please include steps to reproduce the issue and your device/Android version.

## Credits

Created by [@suyziss](https://github.com/suyziss). The Suyziss character artwork is maintained in the [Ludora repository](https://github.com/suyziss/Ludora).

---

<p align="center"><strong>SuyzissGram</strong><br/><sub>Built independently · Currently in beta testing</sub></p>