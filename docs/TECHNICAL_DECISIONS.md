# Technical Decisions — MVP v1

**Status:** Approved baseline  
**Authority:** PRD v1.0 and MVP v1.0

## Binding decisions

| Layer | Choice | Responsibility |
|---|---|---|
| Platform | Native Android 10+ | First and only MVP platform |
| Language/UI | Kotlin + Jetpack Compose | UI, Driving Mode and voice states |
| Architecture | Layered, feature-oriented, unidirectional state | Testability and explicit boundaries |
| Playback | AndroidX Media3 / ExoPlayer in a MediaSessionService | Queue, background, audio focus, notification, lock screen |
| Local discovery | MediaStore | Consented device audio discovery |
| Local data | Room | Media index, Like, short history and session state |
| Preferences | DataStore | Settings, selected Bluetooth device, privacy choices |
| Voice | Android STT/TTS behind interfaces | Speech conversion and replaceable provider |
| Intent | Deterministic restricted mapper | Six supported intents only |
| Dependency injection | Hilt | Boundaries and test substitution |
| Async | Coroutines + Flow | Streams and lifecycle-safe state |
| Quality | Crash reporting + allowlisted analytics | Non-sensitive Beta reliability and metrics |
| Distribution | Google Play Internal/Closed Testing | Controlled Alpha/Beta |
| Design | Figma tokens/components | Ten screens and all critical states |

## Explicit non-decisions

There is no MVP backend, account, authentication, cloud sync, network catalog, streaming API or production environment. Primary product data remains on the device. Networking must not be introduced merely as infrastructure.

Kotlin Multiplatform and iOS remain discovery topics only after the Android Beta decision gate.

## Required module boundaries

- `app`: navigation and composition root
- `core:model`: domain models
- `core:database`: Room and migrations
- `core:media`: MediaStore scanner and index refresh
- `core:playback`: Media3 service, session, queue and audio focus
- `core:voice`: STT/TTS interfaces, state machine and intent mapper
- `core:telemetry`: event allowlist and crash-safe context
- `feature:onboarding`
- `feature:library`
- `feature:search`
- `feature:player`
- `feature:driving`
- `feature:liked` (P2)
- `feature:settings`

The Player is independent of UI lifecycle and owned by a service. UI observes canonical playback state. Voice providers sit behind interfaces and raw audio is never written by application code.

## State and persistence

- Queue, current item and position survive UI closure and process recreation where Android permits.
- Media index refresh is incremental and supports at least 5,000 files smoothly.
- Corrupt or missing files produce normalized errors and controlled skip.
- Bluetooth and route identity stored locally must be user-clearable.
- Database migrations require automated tests.

## Build and quality baseline

- Separate debug and release build types
- CI: formatting, static analysis, unit tests and debug build
- No private credential required for clean checkout
- Signing keys, service accounts and environment secrets remain outside Git
- English Beta UI; all strings externalized for future localization
- Remote confidence tuning may use a simple privacy-reviewed config; a build config fallback must exist
- Analytics emission is centrally allowlisted

## Required ADRs

1. Native Android and minimum SDK
2. Media3 service ownership and lifecycle
3. MediaStore/Room incremental indexing
4. Restricted Voice contract and provider boundary
5. Telemetry and no-raw-audio policy
6. No backend/account/streaming in MVP v1
