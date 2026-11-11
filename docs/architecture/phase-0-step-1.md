# Phase 0 · Step 1 — Client architecture

## Scope

This step extracts the existing local demo into feature-oriented packages inside the existing `:app` Gradle module. It intentionally does not add a backend, HTTP client, database, authentication flow, or new product features.

## Current implementation

The original `MainActivity.kt` contained the activity, Compose navigation state, chat list, chat screen, settings, translations, theme values, JSON persistence, and message actions.

The current demo contains one local “Favorite” conversation. Messages and settings remain stored in the existing `suyzissgram_local` SharedPreferences file so this refactor does not discard local demo data.

## Package layout now

```text
com.suyzissgram
├── MainActivity.kt
├── di
│   └── AppContainer.kt
├── domain
│   ├── model
│   │   ├── AppSettings.kt
│   │   └── ChatMessage.kt
│   └── repository
│       ├── ChatRepository.kt
│       └── SettingsRepository.kt
├── data
│   └── repository
│       ├── FakeChatRepository.kt
│       └── FakeSettingsRepository.kt
└── ui
    ├── chat
    │   ├── ChatScreen.kt
    │   └── ChatViewModel.kt
    ├── chatlist
    │   ├── ChatListScreen.kt
    │   └── ChatListViewModel.kt
    ├── common
    │   └── ViewModelFactory.kt
    ├── i18n
    │   └── AppStrings.kt
    ├── navigation
    │   └── SuyzissNavHost.kt
    ├── settings
    │   ├── SettingsScreen.kt
    │   └── SettingsViewModel.kt
    └── theme
        └── Theme.kt
```

## Dependency direction

```text
Compose screens → ViewModels → domain repository interfaces
                                  ↑
                     fake data implementations
```

UI code does not depend on SharedPreferences or JSON. Replacing the fake implementations with Room-backed or remote-synchronizing implementations can happen behind the same repository contracts.

## DI decision

Use a small manual `AppContainer` for this phase. There are only two repositories and one application entry point. Hilt or Koin would add setup and generated/runtime machinery without solving a current problem. Revisit DI when multiple modules and scoped dependencies exist.

## Gradle module plan

Keep a single `:app` module for the first extraction. Split only when ownership and dependency boundaries are stable:

| Module | Responsibility |
| --- | --- |
| `:app` | Application entry point, DI composition, navigation host |
| `:core:ui` | Theme, shared Compose components, design tokens |
| `:core:domain` | Shared domain contracts and result types |
| `:core:data` | Shared persistence abstractions and mappings |
| `:core:network` | Future HTTP/WebSocket contracts and implementations |
| `:feature:chat` | Chat list, conversation, message state |
| `:feature:auth` | Future onboarding and authentication |
| `:feature:settings` | Settings and preferences |
| `:feature:calls` | Future call UI and call state |

Do not create empty Gradle modules just to match this diagram. Extract them when a feature has enough code to justify an independent build boundary.

## Data and coroutine rules

- ViewModels own screen state and launch work in `viewModelScope`.
- Repository contracts expose cold/read-only `Flow` streams for observable data and suspend functions for commands.
- UI collects state with lifecycle awareness.
- No `GlobalScope` and no detached application coroutine scopes.
- The fake repository preserves the existing SharedPreferences keys during this migration.
- The current fake repository is a compatibility layer, not the final offline-first storage design. Room becomes the source of truth in Phase 1.

## Current limitations

- The demo still has a single saved conversation.
- Chat messages are persisted as one JSON value in SharedPreferences; this is not suitable for a production message database.
- Fake repository methods are local and do not model server delivery or synchronization.
- API, socket, crypto, media, and call implementations are intentionally absent.
- Language strings are extracted from the screen code, but a later localization pass should move them into Android string resources and plurals.
- Error state fields are present in screen state; structured domain failures and one-shot UI effects are a follow-up within Phase 0.

## Dependencies introduced

- Navigation Compose `2.8.5` — AndroidX, Apache-2.0.
- Lifecycle ViewModel Compose / Runtime Compose `2.8.7` — AndroidX, Apache-2.0.
- kotlinx.coroutines Android `1.9.0` — Apache-2.0.

No network or cryptography libraries are added in this step.
