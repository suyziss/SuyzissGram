# SuyzissGram

<p align="center">
  <img src="assets/suyziss.png" alt="Suyziss character" width="220" />
</p>

<p align="center">
  <strong>Private space. Your messages. Your style.</strong><br />
  An offline-first Android messenger beta built with Kotlin and Jetpack Compose.
</p>

<p align="center">
  <img alt="Platform" src="https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white" />
  <img alt="Language" src="https://img.shields.io/badge/language-Kotlin-7F52FF?logo=kotlin&logoColor=white" />
  <img alt="UI" src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4" />
  <img alt="Mode" src="https://img.shields.io/badge/mode-offline-purple" />
</p>

## About

SuyzissGram is an Android messenger beta focused on a clean, dark interface and a working local chat experience. This build is intentionally disconnected from any server.

## Current beta features

- **Favorite / Избранное** — one personal conversation; no fake demo contacts.
- **Working local chat** — type and send messages to your own Favorite space.
- **Local persistence** — messages and language choice are stored on the device.
- **Four interface languages** — English, Russian, Japanese and Kazakh.
- **Animated navigation** — transitions between the chat list and conversation.
- **Android back handling** — return from the conversation using system Back.

## Offline limitations

This is a single-device local beta, not yet a multi-user messenger.

- Messages are not delivered to other people or devices.
- No account registration, server, cloud sync, calls, media transfer or push notifications.
- No end-to-end encryption or remote backup.

Avoid storing sensitive information in this beta.

## Download the APK

Every push to `main` starts the **Android APK** GitHub Actions workflow.

1. Open the [Actions page](https://github.com/suyziss/SuyzissGram/actions).
2. Select the latest successful **Android APK** run.
3. Download the `SuyzissGram-debug-apk` artifact.
4. Extract the ZIP archive and install the APK on an Android device.

The APK is a debug build. A GitHub Release is created only when a version tag starting with `v` is pushed.

## Build locally

Requirements:

- Android Studio (recent stable version)
- JDK 17
- Android SDK 35
- Gradle 8.9

Open the project in Android Studio, sync Gradle, and run the `app` configuration. Alternatively:

```bash
gradle --no-daemon assembleDebug
```

The debug APK is generated at:

```
app/build/outputs/apk/debug/app-debug.apk
```

## Tech stack

- Kotlin
- Jetpack Compose
- Material 3
- AndroidX Activity

## Roadmap

- Improve message management and local storage.
- Add attachments and richer chat interactions.
- Design the account and contacts experience.
- Add a backend only in a later, separately enabled stage.

---

<p align="center">Made by <a href="https://github.com/suyziss">@suyziss</a></p>
