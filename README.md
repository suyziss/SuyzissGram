# SuyzissGram

Android-first messenger demo built with Kotlin and Jetpack Compose.

## Demo mode (0.1.0)

This build is a local UI demo. It uses sample chat data and does not connect to a server. Messages are not synced or delivered to other devices. Authentication, persistent storage, real-time messaging, media upload, push notifications and end-to-end encryption are not implemented.

## Download the APK

Every push to `main` triggers the **Android APK** GitHub Actions workflow. Open the repository's **Actions** tab, select the latest successful run, and download the `SuyzissGram-debug-apk` artifact. On a tagged release (`v*`), the workflow also attaches the APK to the GitHub Release.

## Build locally

- Android Studio (recent stable version)
- JDK 17
- Android SDK 35

Open this repository in Android Studio, sync Gradle, then run the `app` configuration or execute `gradle assembleDebug`.

## Planned architecture

- Android: Kotlin + Jetpack Compose
- Backend (later): REST API + WebSocket
- Database (backend): PostgreSQL

Do not use this prototype to send private or sensitive messages.
