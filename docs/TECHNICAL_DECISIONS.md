# Technical Decisions - MVP v1

- **Status:** Accepted and binding under [Issue #2](https://github.com/latticeward-labs/music-copilot/issues/2)
- **Decision date:** 2026-07-28
- **Authority:** Music Copilot PRD v1.1 and MVP v1.1
- **Applies to:** Android MVP v1, ten-week build and Closed Beta

This document is the accepted implementation baseline for MVP v1.1. Issue #2 records approval and closure. Any material reversal requires a superseding ADR; any product-scope change requires product change control.

## Source hierarchy

1. Music Copilot PRD v1.1 - `Approved for MVP`
2. Music Copilot MVP v1.1 - `Ready for Development`
3. Accepted ADRs in [`docs/adr`](adr/README.md)
4. Other repository documentation and implementation

If a lower-level artifact conflicts with a higher-level source, work stops until the conflict is corrected and recorded.

## Binding baseline

| Concern | Decision | Boundary |
|---|---|---|
| Platform | Native Android 8.0+ (`minSdk 26`), compiled and targeted with API 36 | No iOS or KMP implementation in MVP; SDK levels are revalidated before each Play release |
| Language and UI | Kotlin + Jetpack Compose | UI renders state and emits actions; it never owns playback |
| State and async | Coroutines + Flow, unidirectional state | Canonical state is exposed from the owning layer |
| Playback | AndroidX Media3/ExoPlayer owned by `MediaSessionService` | Queue, MediaSession, notification, audio focus and route behavior live outside UI lifecycle |
| Discovery | Android MediaStore | System-authoritative discovery and content URIs only |
| Structured local data | Room | Derived library index, scan state, optional Like, short history and restorable session references |
| Preferences | DataStore | Small typed settings, onboarding/privacy choices and selected Bluetooth hint |
| Voice provider | Android STT/TTS behind replaceable interfaces | Provider details do not enter UI, intent rules or playback |
| Voice interpretation | Restricted deterministic mapper | Exactly six English MVP intents |
| Dependency injection | Hilt | Composition, provider replacement and test doubles |
| Telemetry | Central typed allowlist + crash diagnostics | No raw audio, transcript, filename, title, artist or album telemetry |
| Distribution | Google Play Internal Testing, then Closed Testing | No public production release before Beta gates |

## Android compatibility and Play baseline

- `minSdk 26` (Android 8.0) defines installation compatibility.
- `compileSdk 36` and `targetSdk 36` define the initial planned 2026 build and Google Play compliance baseline.
- The target SDK is revalidated against the current Google Play policy immediately before every release; a newer required target supersedes this numeric release setting without changing the product scope.
- Android 8–12 use the legacy media-read permission path where required; Android 13+ uses `READ_MEDIA_AUDIO`.
- Foreground-service, media notification, runtime-permission and Bluetooth behavior must be implemented through API-level adapters rather than duplicated feature logic.
- Pull-request CI covers API 26, 33 and 36 for build, lint, unit and smoke/permission-boundary checks.
- Nightly coverage adds API 29, 31 and 34 for scoped-storage, Android 12 Bluetooth/foreground-service behavior and intermediate platform changes.
- Physical-device evidence covers an older API 26-29 device, Android 12/13, Android 16, and representative Pixel, Samsung and Xiaomi devices where available; Bluetooth/audio-route evidence covers at least three head units.
- Release artifacts use Android App Bundle (`.aab`). No explicit `abiFilters` are configured while the application has no native dependency. If a native dependency is introduced, 64-bit compliance, package size and device coverage are reviewed before filtering any ABI.

## Explicit non-decisions and exclusions

MVP v1 has no backend, account, authentication, cloud sync, online catalog, streaming, download, upload, content distribution, subscription, advertising, royalty system, Android Auto, CarPlay, iOS, KMP, wake word or open-ended conversation.

Primary product data remains on the device. Networking must not be introduced as speculative infrastructure. A privacy-reviewed crash/analytics SDK is not a product backend and must remain optional/configurable according to the approved privacy plan.

## Logical architecture

```text
Compose UI / feature ViewModels
              |
         Domain use cases
       /         |          \
Playback port  Voice port   Data repositories
     |            |          |       |        |
Media3 service  STT/TTS   MediaStore Room  DataStore
```

Dependency direction points inward toward domain contracts:

- Feature UI may depend on domain models/use cases and shared UI primitives.
- Domain code must not depend on Compose, Activity, ViewModel, Media3, Room, MediaStore, DataStore or a concrete STT/TTS provider.
- Playback, Voice and Data adapters implement domain-facing contracts.
- UI communicates with playback through a controller/use-case boundary; it never receives ExoPlayer.
- Voice orchestration requests domain commands; it does not manipulate UI widgets.
- Telemetry receives typed, allowlisted events; features do not call a vendor SDK directly.
- Cross-feature implementation dependencies are prohibited; shared behavior moves to an approved core/domain contract.

## Initial Gradle module map

MVP starts with seven physical modules to preserve boundaries without imposing a sixteen-module build and ownership cost during the ten-week timebox.

| Module | Owns | May depend on |
|---|---|---|
| `:app` | Application, navigation, feature packages and Hilt composition root | approved core modules |
| `:core:model` | Pure domain models and value types | Kotlin only |
| `:core:domain` | Use cases and ports | `:core:model` |
| `:core:data` | MediaStore, Room, DataStore, migrations and repository adapters | model/domain contracts and Android data APIs |
| `:core:playback` | Media3 service, session, queue, audio focus and routes | model/domain contracts and Media3 |
| `:core:voice` | STT/TTS adapters, consent-aware provider selection, Voice state machine and deterministic mapper | model/domain contracts and Android speech APIs |
| `:core:ui` | Compose tokens and reusable accessible components | Compose only |

Onboarding, Library, Search, Player, Driving, Liked and Settings begin as independently owned packages inside `:app`. A package becomes a Gradle feature module only when ownership, isolation, testability or build evidence justifies the additional boundary. Telemetry remains a typed allowlisted boundary and vendor adapter; it may move to a module when a vendor is selected.

## Data ownership and lifecycle

### MediaStore

- Discovers audio the user is permitted to access.
- Supplies content URIs and current system metadata.
- Is reconciled incrementally; it is not copied wholesale into application storage.

### Room

- Stores the derived searchable index and scan bookkeeping.
- Stores optional Like, short history and restorable playback references.
- Does not store raw audio, microphone data, transcripts or secrets.
- Every schema change includes a migration and automated migration test.

### DataStore

- Stores only small typed preferences and flags.
- Does not store relational collections, media indexes or transcripts.

Queue/current item/position are restored after UI closure and process recreation where Android permits. Missing or corrupt media produces a normalized error and controlled queue continuation. All user-owned app data has a clear deletion path.

## Playback contract

- `MediaSessionService` is the sole owner of the player and canonical playback state.
- UI, notification, lock screen and Bluetooth/head unit observe/control the same session.
- Rotation, UI closure and screen-off do not stop playback.
- Incoming calls, audio-focus changes, headset disconnect and route loss are explicit state transitions.
- Route loss must not cause unintended phone-speaker playback.

## Voice contract

Push-to-talk follows:

`Idle -> Listening -> Processing -> Confirming -> Executed | Failed -> Idle`

- Listening timeout: at most six seconds.
- Retry: once.
- TTS response: at most one short sentence.
- Transcript: memory-only for the active command, then discarded.
- On API 31+, use on-device recognition when `isOnDeviceRecognitionAvailable()` is true.
- The general Android recognizer may stream audio to remote servers and is disabled by default; it requires clear, revocable consent describing the available processor, destination and retention behavior.
- If on-device recognition is unavailable and consent is not granted, Voice remains unavailable while full manual control remains functional.
- Availability, consent and provider errors are normalized into typed domain results.
- High-confidence single match: direct execution.
- Low confidence or multiple plausible matches: one short confirmation.
- Failure/no match: playback remains stable and manual control remains available.

Supported intents are exactly:

`PLAY`, `PAUSE`, `NEXT`, `PREVIOUS`, `SEEK_BACK`, `PLAY_ARTIST`.

The detailed, testable behavior is governed by [`VOICE_CONTRACT.md`](VOICE_CONTRACT.md).

## CI and merge gate expectations

The foundation workflow must run on pull requests to `main` and `develop` and on pushes to protected branches. A change is not mergeable unless all required checks pass:

1. Gradle wrapper validation and clean dependency resolution.
2. Kotlin/Android formatting check.
3. Android Lint and configured static analysis.
4. JVM unit tests for changed modules.
5. Debug APK assembly from a clean checkout with no private credential.
6. Instrumented/integration tests when the relevant emulator/test infrastructure is available.
7. Room migration tests for every database schema change.
8. Dependency/secret review with no signing key, service account or secret committed.

CI implementation is a week-1 foundation deliverable. This document defines the gate; it does not claim that enforcement already exists.

## Verification baseline

Architecture approval does not replace implementation evidence. Feature completion later requires:

- pull-request build, install and smoke tests across API 26, 33 and 36,
- nightly compatibility tests across API 29, 31 and 34,
- permission-path tests for legacy storage access and `READ_MEDIA_AUDIO`,
- foreground-service and media-notification behavior across the compatibility matrix,
- 5,000-item incremental library test without blocking error,
- screen-off and UI-reconnection playback tests,
- notification/lock-screen/Bluetooth state synchronization,
- call, audio-focus, headset and route-transition matrix,
- deterministic Voice fixtures and provider contract tests,
- permission-denial and data-clearing tests,
- telemetry schema tests proving prohibited values cannot be emitted.

## ADR register

| ADR | Decision | Status |
|---|---|---|
| [ADR-0001](adr/0001-native-android-platform.md) | Native Android 8.0+ / API 36 release baseline | Accepted |
| [ADR-0002](adr/0002-service-owned-media3-playback.md) | Service-owned Media3 playback | Accepted |
| [ADR-0003](adr/0003-local-first-data-boundaries.md) | MediaStore, Room and DataStore boundaries | Accepted |
| [ADR-0004](adr/0004-replaceable-voice-provider.md) | Replaceable STT/TTS provider boundary | Accepted |
| [ADR-0005](adr/0005-deterministic-intent-mapping.md) | Restricted deterministic intent mapping | Accepted |
| [ADR-0006](adr/0006-no-backend-account-streaming.md) | No backend, account or streaming in MVP v1 | Accepted |

ADR-0001 through ADR-0006 were accepted on 2026-07-28 under Issue #2 after PRD v1.1 and MVP v1.1 resolved the Android, Voice, module, ABI and test-matrix changes. Any later reversal requires a superseding ADR.