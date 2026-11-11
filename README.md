# SuyzissGram

<p align="center">
  <strong>A messenger built around the way you chat.</strong><br />
  Telegram-inspired Android experience · Kotlin · Jetpack Compose
</p>

<p align="center">
  <img alt="Android" src="https://img.shields.io/badge/Android-16%2B-3DDC84?logo=android&logoColor=white" />
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white" />
  <img alt="Jetpack Compose" src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4" />
  <img alt="Status" src="https://img.shields.io/badge/status-testing-orange" />
</p>

---

## Project status

<p align="center">
  <strong>Currently in active testing</strong><br />
  Planned online release: <strong>01 January 2027</strong>
</p>

SuyzissGram is an independent Android messenger project. The current build is an early local prototype. We are testing the interface, navigation, message composer and on-device behavior before connecting the app to online services.

**The date above is a target, not a promise.** The online release depends on testing, backend development, security work and stability checks.

## The vision

The goal is to build a full-featured messenger with a familiar, polished chat experience inspired by the usability of Telegram, while developing SuyzissGram as its own product and visual identity.

Planned experience:

- **Chats:** conversation list, pinned and archived chats, unread counters, drafts and search.
- **Messaging:** replies, forwards, edits, deletion, reactions, message selection and delivery/read states.
- **Media:** photo and video sharing, files, voice messages, previews and albums.
- **People:** profiles, contacts, usernames, groups and channels.
- **Customization:** light, dark and system themes, accent colors, chat backgrounds, font sizing and language settings.
- **Privacy:** app lock, session controls and carefully designed privacy settings.
- **More details:** message context menus, swipe actions, chat folders, notification controls and thoughtful empty/loading states.

These are planned features, not claims about the current beta. They will be introduced and tested in stages.

## What works in the current prototype

- One local **Favorites** conversation.
- Send messages to the local conversation.
- Save messages on the device between launches.
- Interface language options: English, Russian, Japanese and Kazakh.
- Basic navigation between the chat list and conversation.

## Current limitations

This build is **local-only**. It is not yet an online multi-user messenger.

- No account registration or sign-in.
- No message delivery to other users or devices.
- No cloud synchronization or server-side storage.
- No calls, push notifications or online presence.
- No end-to-end encryption in the current prototype.

Do not use the current test build to store sensitive information.

## Testing the Android build

Debug APKs are produced by the GitHub Actions workflow when changes are pushed.

1. Open [GitHub Actions](https://github.com/suyziss/SuyzissGram/actions).
2. Open the latest successful **Android APK** workflow run.
3. Download the `SuyzissGram-debug-apk` artifact.
4. Extract the ZIP archive and install the APK on an Android test device.

Debug builds are intended for testing and may contain unfinished features or bugs.

## Build locally

Requirements:

- Android Studio (recent stable version)
- JDK 17
- Android SDK 35
- Gradle 8.9

Open the project in Android Studio, sync Gradle and run the `app` configuration. Or build from the command line:

```bash
gradle --no-daemon assembleDebug
```

The APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Technology

- Kotlin
- Jetpack Compose
- Material 3
- AndroidX Activity

## Development roadmap

1. **Polish the foundation** — fix navigation, keyboard behavior, localization and responsive layouts.
2. **Complete the app shell** — settings, themes, profile, chat list, search and chat management.
3. **Build messaging features** — replies, reactions, editing, forwarding, attachments and voice messages.
4. **Prepare online infrastructure** — accounts, backend APIs, secure transport, synchronization and notifications.
5. **Test and release** — closed testing, bug fixes, performance and security review before the planned online launch.

## Feedback

Found a bug or have an idea? Open a [GitHub Issue](https://github.com/suyziss/SuyzissGram/issues) with steps to reproduce it and, if possible, screenshots and device details.

---

<p align="center">
  Made with care by <a href="https://github.com/suyziss">@suyziss</a><br />
  <sub>SuyzissGram · Testing now · Online release planned for 01.01.2027</sub>
</p>