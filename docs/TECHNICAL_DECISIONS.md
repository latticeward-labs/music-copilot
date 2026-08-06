# Technical Decisions - MVP v1.0

- **Status:** Preliminary ADR gate approved on 2026-08-06 through [Issue #2](https://github.com/latticeward-labs/music-copilot/issues/2)
- **Authority:** [PRD v1.0](PRD.md), [MVP v1.0](MVP.md) and accepted [ADRs](adr/README.md)
- **Final architecture gate:** [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16)

This is the accepted high-level baseline. It does not replace [Software Architecture](SOFTWARE_ARCHITECTURE.md).

## Accepted preliminary baseline

| Concern | Accepted decision | ADR |
|---|---|---|
| Platform | Native Android 10+ with Kotlin | [0001](adr/0001-native-android-platform.md) |
| UI/default stack | Compose, Coroutines/Flow and Hilt | [0001](adr/0001-native-android-platform.md) |
| Playback | Service-owned Media3/ExoPlayer outside UI lifecycle | [0002](adr/0002-service-owned-media3-playback.md) |
| Data | MediaStore authority, rebuildable Room index/app data, DataStore preferences | [0003](adr/0003-local-first-data-boundaries.md) |
| Voice | Replaceable Android STT/TTS interfaces | [0004](adr/0004-replaceable-voice-provider.md) |
| Interpretation | Deterministic mapper with exactly six English intents | [0005](adr/0005-deterministic-intent-mapping.md) |
| Product services | No account, product backend, sync, catalog or streaming | [0006](adr/0006-no-backend-account-streaming.md) |
| Quality | Privacy-safe diagnostics; separately reviewed support processors only | [0006](adr/0006-no-backend-account-streaming.md) |

## Accepted logical boundaries

UI, Application/Domain, Playback, Voice, Data and Quality/Diagnostics are separate logical boundaries. Final contracts, dependency direction, navigation, state ownership, concurrency and module granularity remain for Issue #16 after User Flows and Wireframes.

## Voice, privacy and configuration constraints

- Push-to-talk; six-second maximum; one retry; one short TTS sentence
- No raw voice or full personal-file titles in telemetry
- STT transcription remains separate from deterministic mapping
- No offline-STT guarantee until tested
- Cloud STT requires separate consent and provider/destination, purpose and retention disclosure
- Threshold configuration has a safe local default and may tune only a bounded value
- Remote configuration cannot add intents, change scope or bypass confirmation
- Failure/refusal preserves manual control

## Review evidence routed forward

| Evidence | Destination |
|---|---|
| Android/OEM/head-unit validation | QA and field testing |
| Service lifecycle, process death, command ordering | Issue #16 spike |
| Permissions/revocation, index invalidation/migrations, 5,000-file scan | User Flows, Issue #16, QA |
| STT availability/connectivity/consent | User Flows, privacy review, Issue #16 |
| Phrase corpus, thresholds/fuzzy bounds, ambiguity | Voice tests and Issue #16 |
| Navigation/back, Voice overlay and screen states | Issues #3, #15, #16 |
| ViewModels, errors, coroutines and modules | Issue #16 |

These do not weaken accepted scope decisions; they prevent implementation from guessing details.

## Verification baseline

Two-device build; 5,000-file scan; background playback; call/headset/Bluetooth/route cases; Android 10/12/14+; Pixel/Samsung/Xiaomi; three vehicles/head units; zero open P0 before Beta.

## Decision stages

1. **Issue #2:** completed - six ADRs reviewed, revised and accepted.
2. **Issue #3:** complete User Flows/state inventory.
3. **Issue #15:** validate low-fidelity Wireframes.
4. **Issue #16:** approve final Software Architecture and add/supersede ADRs if required.
5. **Issue #4:** implement approved foundation.

## Intentionally open until Issue #16

Navigation/back behavior; screen/shared state ownership; Voice overlay ownership; ViewModel/use-case boundaries; package/Gradle modules; P1 queue restoration; error/concurrency policy; exact service/controller type; process-death limits; schemas/migrations; provider-specific adapter details.
