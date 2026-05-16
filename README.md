# Aura Assistant

> A modern AI assistant Android application built with Jetpack Compose, following Clean Architecture principles with an offline-first data strategy.

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Module Structure](#module-structure)
- [Tech Stack](#tech-stack)
- [Feature Breakdown](#feature-breakdown)
  - [Onboarding Flow](#onboarding-flow)
  - [Home Screen](#home-screen)
  - [Message State Machine](#message-state-machine)
  - [Offline-First Sync](#offline-first-sync)
- [Data Layer](#data-layer)
- [Presentation Layer](#presentation-layer)
- [Navigation](#navigation)
- [Dependency Injection](#dependency-injection)
- [Testing](#testing)
- [UI & Theming](#ui--theming)
- [Permissions](#permissions)
- [Getting Started](#getting-started)

---

## Overview

Aura Assistant is a conversational AI assistant app with a focus on a fluid, modern user experience. It features an animated onboarding flow, a custom Canvas-drawn "Aura Circle" that reacts to microphone input, paginated chat history, and a robust coroutine-based message processing pipeline — all backed by an offline-first Room database with WorkManager background sync.

---

## Architecture

The project follows **Clean Architecture** with a strict separation of three layers:

```
┌─────────────────────────────────────────────┐
│              Presentation Layer              │
│  Compose UI · ViewModels · State Holders    │
├─────────────────────────────────────────────┤
│                Domain Layer                  │
│   Repository Interfaces · Domain Models     │
├─────────────────────────────────────────────┤
│                 Data Layer                   │
│  Room · DataStore · WorkManager · Mappers   │
└─────────────────────────────────────────────┘
```

### Key Architectural Decisions

**1. Clean Architecture with Dependency Inversion**

The domain layer defines repository interfaces (`UserRepository`, `ChatRepository`). The data layer provides concrete implementations (`UserRepositoryImpl`, `ChatRepositoryImpl`). The presentation layer depends only on the domain interfaces — never on data layer classes directly. This makes each layer independently testable and swappable.

**2. Unidirectional Data Flow (UDF)**

Every screen is driven by a single `UiState` data class exposed as a `StateFlow` from its `ViewModel`. The UI collects state reactively and sends one-way events back to the ViewModel via function references. There is no two-way data binding or shared mutable state between the UI and ViewModel.

```
UI Event ──► ViewModel ──► Repository ──► Data Source
                │
                ▼
           StateFlow<UiState>
                │
                ▼
           Compose UI (recompose)
```

**3. Sealed Class State Modelling**

All complex states — message pipeline states (`MessageState`), sync status (`SyncStatus`), and Aura circle animation states (`AuraCircleState`) — are modelled as sealed classes. This makes state exhaustive and eliminates illegal state combinations entirely at compile time.

**4. Single Source of Truth**

- **User profile** → DataStore (single source, observed via `Flow`)
- **Chat messages** → Room database (single source, observed via `Flow`)
- **Sync status** → `SyncState` singleton `StateFlow` (updated by `SyncWorker`, observed by `HomeViewModel`)

**5. Offline-First Data Strategy**

All reads and writes go to the local Room database first. WorkManager syncs changed rows to the remote server on a network-constrained periodic schedule. **Local wins on conflict** — no remote data overwrites local user data. Changed rows are identified by `lastSyncedAt` timestamp comparison.

---

## Module Structure

```
app/
└── src/main/java/com/nitish/auraassistant/
    │
    ├── di/                          # Hilt dependency injection modules
    │   ├── DatabaseModule.kt        # Room DB, DAOs
    │   └── RepositoryModule.kt      # Repository interface bindings
    │
    ├── domain/                      # Pure Kotlin — no Android dependencies
    │   ├── model/
    │   │   ├── ChatMessage.kt
    │   │   ├── MessageMeta.kt
    │   │   ├── Reminder.kt
    │   │   └── UserProfile.kt
    │   └── repository/
    │       ├── ChatRepository.kt
    │       └── UserRepository.kt
    │
    ├── data/
    │   ├── local/
    │   │   ├── datastore/
    │   │   │   └── UserPreferencesDataStore.kt
    │   │   └── db/
    │   │       ├── AuraDatabase.kt
    │   │       ├── dao/             # ChatMessageDao, UserProfileDao, ReminderDao
    │   │       └── entity/          # ChatMessageEntity, UserProfileEntity, ReminderEntity
    │   ├── repository/
    │   │   ├── ChatRepositoryImpl.kt
    │   │   └── UserRepositoryImpl.kt
    │   └── sync/
    │       └── SyncManager.kt       # SyncWorker + SyncScheduler + SyncState + SyncStatus
    │
    ├── statemachine/
    │   ├── MessageState.kt          # Sealed class hierarchy
    │   └── MessageStateMachine.kt   # Coroutine pipeline
    │
    ├── presentation/
    │   ├── navigation/
    │   │   ├── AppNavigation.kt
    │   │   └── NavViewModel.kt
    │   ├── theme/
    │   │   ├── Color.kt
    │   │   ├── Theme.kt
    │   │   └── Type.kt
    │   ├── onboarding/
    │   │   ├── OnboardingViewModel.kt
    │   │   └── screens/
    │   │       ├── OnboardingScreen.kt
    │   │       ├── Step1ValueProps.kt
    │   │       ├── Step2UserInfo.kt
    │   │       └── Step3Personality.kt
    │   └── home/
    │       ├── HomeViewModel.kt
    │       └── screens/
    │           ├── HomeScreen.kt
    │           ├── AuraCircle.kt
    │           └── ChatHistoryList.kt
    │
    ├── AuraApp.kt                   # Application class — Hilt + WorkManager init
    └── MainActivity.kt
```

---

## Tech Stack

| Category | Library | Version |
|---|---|---|
| UI | Jetpack Compose + Material3 | BOM 2024.11.00 |
| Language | Kotlin | 2.0.21 |
| DI | Hilt (Dagger) | 2.52 |
| Local DB | Room | 2.6.1 |
| Preferences | DataStore Preferences | 1.1.1 |
| Background Sync | WorkManager | 2.10.0 |
| Coroutines | kotlinx-coroutines | 1.9.0 |
| Navigation | Navigation Compose | 2.8.4 |
| Serialization | kotlinx-serialization-json | 1.7.3 |
| Testing | JUnit 4 + Turbine + coroutines-test | — |
| Build | KSP + AGP | 8.7.0 |

---

## Feature Breakdown

### Onboarding Flow

A **3-step swipeable onboarding** implemented with `HorizontalPager`:

| Step | Screen | Content |
|---|---|---|
| 1 | `Step1ValueProps` | Animated value proposition cards with staggered fade-in |
| 2 | `Step2UserInfo` | Name, age, phone number input + mock OTP verification (pin: `1234`) |
| 3 | `Step3Personality` | Tag-chip grid for selecting exactly 3 personality traits |

**Architecture decisions:**
- `OnboardingViewModel` manages a single `OnboardingUiState` data class covering all three steps, eliminating the need for cross-step state sharing.
- DataStore is read once on `init` using `Flow.first()` to pre-fill fields on re-entry — avoiding a continuous `collect` that would overwrite user edits mid-session.
- Partial profile is saved to DataStore after Step 2, so data survives app kills before onboarding completes.
- The `isOnboardingComplete` flag in `UserProfile` drives the navigation start destination — set to `true` only when the user successfully finishes Step 3.
- Back navigation within the pager is handled by `OnboardingViewModel.goToPreviousStep()`, restoring all previously entered data without re-fetching from storage.

---

### Home Screen

The home screen has two visual modes driven by a vertical swipe gesture:

**Aura Circle Mode (default)**
- A custom `Canvas`-drawn glowing circle (`AuraCircle.kt`) with two animation states:
  - `AuraCircleState.Idle` — breathing pulse with orbiting particle ring, driven by `infiniteTransition`
  - `AuraCircleState.Listening` — amplitude-reactive glow rings that expand with mic input intensity
- Swipe up gesture transitions to Chat History mode via `AnimatedContent`

**Chat History Mode**
- `ChatHistoryList` — `LazyColumn` with `reverseLayout = true` (newest at bottom)
- Pagination loads 20 messages per page; next page triggers automatically via `derivedStateOf` scroll detection
- Sender-differentiated chat bubbles: user messages aligned right (violet), Aura messages aligned left (surface)
- "Back to Aura" button returns to circle mode

**Bottom Input Bar** supports two input modes:
- **Voice** — mic button requests `RECORD_AUDIO` permission at runtime, then streams `AudioRecord` PCM amplitude via a coroutine on `Dispatchers.IO`, normalised to `[0, 1]` and fed into `AuraCircleState.Listening`
- **Text** — keyboard toggle slides up `OutlinedTextField` with `imePadding`; send dispatches into the `MessageStateMachine` pipeline

---

### Message State Machine

Located in `statemachine/`, this is a coroutine-based finite state machine managing the full message processing lifecycle.

**State graph:**

```
         onTyping()
Idle ──────────────► Typing
  ▲                     │
  │             sendMessage()
  │                     ▼
  │               Validating ──► Error (blank message)
  │                     │
  │                     ▼
  │               Processing ──► Error (timeout after 8s)
  │                     │            │
  │                     ▼            │ retry()
  │               Responding         │
  │                     │            ▼
  └─────────────────────┘        Processing...
         (auto-reset)
```

**Key design decisions:**

- **Single `pipelineJob`** — sending a new message while `Processing` or `Responding` cancels the current `Job` before launching a new one. `CancellationException` is always re-thrown to preserve structured concurrency.
- **`withTimeout(8_000L)`** — wraps the simulated AI response call. On `TimeoutCancellationException`, transitions to `Error` with a retry prompt rather than crashing.
- **Injectable `responseDelayMs`** — the AI response delay is a constructor parameter defaulting to `1_500ms` in production. Tests inject `0ms` to run pipelines instantly without virtual time manipulation for simple cases.
- **`retry()`** — reads `error.text` from the current `Error` state and re-runs the pipeline, preserving the original message.
- **`reset()`** — cancels any active job and returns to `Idle` unconditionally (used for screen exits and hard resets).

---

### Offline-First Sync

Background sync is implemented entirely with **WorkManager** and **Room** — no third-party sync libraries.

**How it works:**

1. `SyncScheduler.schedulePeriodicSync()` is called from `AuraApp.onCreate()`, enqueuing a `PeriodicWorkRequest` every **15 minutes** with `NetworkType.CONNECTED` constraint.
2. `SyncWorker` (a `@HiltWorker` `CoroutineWorker`) reads `lastSyncedAt` from `inputData` and queries only rows changed since that timestamp via `getChangedSince(lastSyncedAt)` on each DAO.
3. Changed rows are pushed to the remote API (currently simulated — replace `simulateNetworkSync()` with your actual API call).
4. On success, `SyncState.update(SyncStatus.Success(now))` broadcasts the timestamp via a global `StateFlow`.
5. On failure, `Result.retry()` triggers WorkManager's **exponential backoff**.
6. `HomeViewModel` observes `SyncState.status: StateFlow<SyncStatus>` and surfaces it in the UI via `SyncStatusBadge`.

**Conflict resolution: Local wins.** Room uses `OnConflictStrategy.REPLACE` for inserts, and since the device is the only writer, local data is never silently overwritten by a remote response.

---

## Data Layer

### Room Database — `AuraDatabase`

Three entities, one database:

| Entity | Table | Key Fields |
|---|---|---|
| `UserProfileEntity` | `user_profiles` | Personality traits stored as comma-separated `String` |
| `ChatMessageEntity` | `chat_messages` | `MessageMeta` stored via `TypeConverter` (JSON) |
| `ReminderEntity` | `reminders` | `triggerAt` timestamp for scheduling |

All entities include a `lastSyncedAt: Long` column used by the sync worker to identify changed rows without a full table scan.

### DataStore — `UserPreferencesDataStore`

Stores the `UserProfile` as individual `Preferences.Key` entries (not Proto DataStore). Exposes `userProfileFlow: Flow<UserProfile>` — a cold Flow that emits on every preference change.

### Mappers

Each `RepositoryImpl` contains private `toDomain()` and `toEntity()` extension functions, keeping mapping logic co-located with the repository and out of both the domain models and the DAOs.

---

## Presentation Layer

### ViewModels

| ViewModel | Responsibility |
|---|---|
| `NavViewModel` | Bridges Hilt injection into `AppNavigation` composable for startup destination resolution |
| `OnboardingViewModel` | Manages all 3 onboarding steps in one `StateFlow<OnboardingUiState>`, validation, DataStore persistence |
| `HomeViewModel` | Chat pagination, mic `AudioRecord` pipeline, `MessageStateMachine` integration, sync status observation |

### UI State

Each ViewModel exposes a single immutable `data class UiState` as `StateFlow`. Compose collects it with `collectAsState()`. All mutations go through `MutableStateFlow.update { }` to ensure thread-safe atomic updates.

---

## Navigation

`AppNavigation` uses Jetpack Navigation Compose with two destinations:

```kotlin
sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home       : Screen("home")
}
```

**Startup routing** is determined in a `LaunchedEffect` by calling `userRepository.isOnboardingComplete()` (a `suspend` function reading DataStore). The `NavHost` is not composed until the start destination is resolved, preventing a flash of the wrong screen.

**Post-onboarding navigation** pops the entire onboarding back stack (`inclusive = true`) before navigating to Home, making the back button exit the app rather than returning to onboarding.

---

## Dependency Injection

Hilt is configured across two modules:

**`DatabaseModule`** (`SingletonComponent`, `@object`)
- Provides `AuraDatabase` singleton
- Provides `UserProfileDao`, `ChatMessageDao`, `ReminderDao` from the database

**`RepositoryModule`** (`SingletonComponent`, `@abstract class`)
- Binds `UserRepositoryImpl` → `UserRepository`
- Binds `ChatRepositoryImpl` → `ChatRepository`

`AuraApp` implements `Configuration.Provider` to inject `HiltWorkerFactory` into WorkManager, enabling Hilt injection inside `SyncWorker`.

---

## Testing

Unit tests for `MessageStateMachine` live in `app/src/test/`:

| Test | Covers |
|---|---|
| `happy path transitions Idle through all states back to Idle` | Full pipeline: Validating → Processing → Responding → Idle |
| `sending message while Processing cancels current job and restarts` | `pipelineJob.cancel()` + new pipeline launch |
| `processing timeout of 8s triggers Error state` | `withTimeout` → `TimeoutCancellationException` → `Error` |
| `blank message skips Processing and goes to Error` | Validation branch in `runPipeline` |
| `reset cancels pipeline and returns to Idle` | `reset()` cleans up active job |

**Testing strategy:**
- `runTest` with `TestCoroutineScheduler` for virtual time control
- `advanceTimeBy(8_500L)` to simulate the 8-second timeout without real waiting
- `app.cash.turbine` for ergonomic `StateFlow` assertion with `awaitItem()`
- `responseDelayMs = 0L` constructor injection eliminates the need for time advancement on happy-path tests

---

## UI & Theming

The app uses a **dark purple/violet brand palette** defined in `Color.kt`:

| Token | Hex | Usage |
|---|---|---|
| `AuraDeepBg` | `#0A0A14` | Screen backgrounds |
| `AuraSurface` | `#111124` | Card / bottom bar surfaces |
| `AuraSurfaceVariant` | `#1A1A2E` | Elevated containers |
| `AuraAccent` | `#7C6AF5` | Primary buttons, active states |
| `AuraAccentBright` | `#A78BFA` | Secondary accents, links |
| `AuraGlow` | `#6D5CE8` | Canvas glow effects |
| `AuraInputBg` | `#1E1E35` | Text field backgrounds |
| `AuraError` | `#FF6B81` | Error states, banners |
| `AuraSuccess` | `#4FFFB0` | Sync success indicator |

`Typography` provides a full Material3 type scale from `headlineLarge` (32sp Bold) down to `labelSmall` (11sp Medium).

---

## Permissions

| Permission | When Requested | Purpose |
|---|---|---|
| `RECORD_AUDIO` | Runtime — when user taps mic button | `AudioRecord` for voice amplitude detection |
| `INTERNET` | Install-time | Future API calls |
| `ACCESS_NETWORK_STATE` | Install-time | WorkManager network constraint evaluation |
| `RECEIVE_BOOT_COMPLETED` | Install-time | WorkManager reschedule after reboot |

Runtime permission is handled in `HomeScreen` via `rememberLauncherForActivityResult(RequestPermission)`. If denied, the mic button is a no-op; if granted, `HomeViewModel.startListening()` is called immediately.

---

## Getting Started

### Prerequisites

- Android Studio Ladybug or newer
- JDK 17
- Android SDK 26+

### Build

```bash
git clone https://github.com/your-username/AuraAssistant.git
cd AuraAssistant
./gradlew assembleDebug
```

### Run Tests

```bash
./gradlew :app:test
```

### Replacing the Mock AI

`MessageStateMachine.simulateAiResponse()` currently returns a hardcoded string after a delay. Replace it with your actual API call:

```kotlin
private suspend fun simulateAiResponse(text: String): String {
    return apiService.sendMessage(text).response  // your implementation
}
```

### Replacing the Mock Sync

`SyncWorker.simulateNetworkSync()` is the stub for the backend push. Replace with:

```kotlin
apiService.syncMessages(changedMessages)
apiService.syncProfiles(changedProfiles)
```

---

*Built with Jetpack Compose · Clean Architecture · Kotlin Coroutines*
