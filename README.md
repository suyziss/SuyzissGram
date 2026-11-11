# SuyzissGram

<p align="center">
  <img src="https://raw.githubusercontent.com/suyziss/Ludora/main/file_00000000267c824690fc0a9b227b0405.png" alt="Suyziss character" width="220" />
</p>

<h1 align="center">SuyzissGram</h1>
<p align="center"><strong>Your conversations. Your space.</strong><br/>An independent Android messenger project with an original Material You interface.</p>

<p align="center">
  <img alt="Android" src="https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white" />
  <img alt="Kotlin" src="https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white" />
  <img alt="Jetpack Compose" src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4" />
  <img alt="Version" src="https://img.shields.io/badge/Version-0.8.004%20βeta-8A63D2" />
  <img alt="Status" src="https://img.shields.io/badge/Status-Testing-orange" />
</p>

---

## About

**SuyzissGram** is an independent Android messenger developed with Kotlin and Jetpack Compose. Its product goal is broad feature parity with modern messengers, including rich profiles, chats, media, privacy, groups, calls and detailed settings. It uses its own name, code, icons and visual identity. Telegram source code and proprietary assets are not copied.

## Current release — 0.8.004 βeta

<p align="center"><strong>Early development · Local prototype · Android</strong></p>

Available in the current prototype:

- Local Favorites chat with on-device message persistence.
- One-step local authentication: an existing email signs in; a new email starts registration using the supplied username and password (no email verification code).
- Generated permanent 10-digit numeric account ID shown as read-only in the profile.
- Editable local profile: display name, username and bio.
- Profile avatar selection through the Android system Photo Picker.
- Profile data and selected avatar URI saved locally.
- Settings screen with language selection: English, Russian, Japanese and Kazakh.
- Functional dark, light and system theme selection.
- App-wide text scaling preference.
- Confirmation before clearing locally stored messages.
- Back navigation between the main screen, chats, settings and profile.
- Photo and video selection through Android Photo Picker, including multi-select.
- Photo sending with a choice between locally compressed image copies and original files. Compression downsamples to a maximum 1920-pixel dimension and uses lossy WebP on Android 11+ or JPEG on older versions.
- Video attachments are retained in their original quality in this release; video transcoding is not yet implemented.
- File picker accepts any MIME type (`*/*`), and selected files are copied into app-private attachment storage so message attachments remain available after the picker grant expires.
- Location sharing requests Android location permission and creates a local message with coordinates. Tapping it opens a compatible map app.
- Attachments are stored only on this device and are not uploaded or delivered to another user.
- Five selectable local chat palettes: Violet, Ocean, Emerald, Sunset and Graphite. The selected palette is saved in app settings.
- Six profile accent colors. The chosen accent is saved with the local profile and is used on the profile avatar placeholder.
- A local demo Stars wallet starts with 100 test Stars. Four original vector-style animated gift designs can be selected and recorded in local gift history; the demo balance is reduced on confirmation.
- Demo Stars cannot be purchased, withdrawn or transferred. Gift records remain on the current device and are not delivered to another account.

The prototype currently stores one local account per app installation. If a different email is entered while that local account exists, the app asks the user to sign in with the saved email. Email and username uniqueness are not coordinated across devices, and the numeric account ID is generated on-device; production-wide uniqueness must be enforced by the future server database. Email is not verified. Passwords are stored as salted PBKDF2-HMAC-SHA256 hashes, never as plaintext, but local credentials are not yet protected by an online authentication service or Android Keystore-backed encrypted storage. There is no password recovery flow.

## Architecture

The current package structure, dependency direction, DI decision and future Gradle module plan are documented in [Phase 0 — Step 1](docs/architecture/phase-0-step-1.md).

## Interface and feature roadmap

### Profile and account
- [x] Local profile editor for display name, username and bio.
- [x] Local avatar selection.
- [ ] Phone number, birthday, privacy visibility and QR profile.
- [x] Local email/password registration and sign-in without email-code verification.
- [ ] Server-side account creation, globally unique ID allocation, password recovery and cloud profile sync.

### Chats and navigation
- [ ] Conversation list with per-chat avatars, previews and timestamps.
- [ ] Pinned and archived chats, folders and unread counters.
- [ ] Search, drafts and chat-specific settings.

### Messaging
- [ ] Replies, forwarding, editing and deleting messages.
- [ ] Reactions, message selection and context menus.
- [x] Photo, video, file and album selection for local attachment messages.
- [x] Local chat palette selection from the chat toolbar.
- [x] Local demo gift sending and wallet history.
- [ ] Voice messages and media previews.

### Personalization
- [x] Five local chat color palettes.
- [x] Local profile accent color selection.
- [x] Demo Stars wallet, original animated vector gift catalog and local gift history.
- [ ] Custom chat backgrounds and detailed text controls.
- [ ] Profile themes and decorative profile elements.
- [ ] Full profile privacy controls and complete multilingual resources.
- [ ] Accessibility, tablet layouts and responsive navigation.

### Online features
- [ ] Backend API, authentication and cloud synchronization.
- [ ] Real-time messaging, groups and channels.
- [ ] Push notifications and multi-device sessions.
- [ ] Privacy controls, application lock and end-to-end encryption.
- [ ] Voice and video calls.

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

- **Current version:** `0.8.004-beta`
- **Development status:** active testing
- **Architecture:** native Android app, Kotlin + Jetpack Compose
- **Current mode:** local-only prototype
- **Online release target:** `01.01.2027` (planned target, not a guaranteed release date)

### Current limitations

The current build does not yet provide user accounts, a production server, cloud synchronization, messaging between different users, calls, push notifications or end-to-end encryption. Profile fields are stored only on the current device. Application lock and advanced privacy protections are also not implemented. Avoid using the prototype to store sensitive information.

## Feedback and contributions

Found a bug or have an idea? Open a [GitHub Issue](https://github.com/suyziss/SuyzissGram/issues). Please include steps to reproduce the issue and your device/Android version.

## Credits

Created by [@suyziss](https://github.com/suyziss). The Suyziss character artwork is maintained in the [Ludora repository](https://github.com/suyziss/Ludora).

---

<p align="center"><strong>SuyzissGram</strong><br/><sub>Built independently · Currently in beta testing</sub></p>
