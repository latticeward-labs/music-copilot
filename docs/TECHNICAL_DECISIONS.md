# Technical Decisions — MVP v1.0

- **Status:** Preliminary review in progress through [Issue #2](https://github.com/latticeward-labs/music-copilot/issues/2)
- **Authority:** [PRD v1.0](PRD.md) and [MVP v1.0](MVP.md)
- **Final architecture gate:** [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16)
- **Applies to:** Android MVP v1.0 and the locked 10-week implementation plan

This document records the high-level technical baseline supported directly by the approved product scope. It does **not** replace the final [Software Architecture baseline](SOFTWARE_ARCHITECTURE.md).

## Preliminary baseline under ADR review

| Concern | v1.0 proposal |
|---|---|
| Platform | Native Android 10+ |
| Language/UI | Kotlin + Jetpack Compose |
| Async/state | Coroutines + Flow |
| Playback | Media3 / ExoPlayer, independent of UI lifecycle and managed in a service |
| Discovery | MediaStore |
| Local data | Room + DataStore |
| Voice | Android STT/TTS behind a replaceable interface |
| Interpretation | Restricted deterministic mapper with six English intents |
| Dependency injection | Hilt |
| Quality | Privacy-safe crash reporting and analytics |
| Distribution | Internal testing followed by Closed Beta |

## Preliminary logical boundaries

- **UI:** screens, Driving Mode and presentation of Voice states
- **Application/Domain:** product actions and use cases
- **Playback:** Media3, queue, MediaSession, service ownership and Audio Focus
- **Voice:** STT/TTS adapter, state machine and restricted intent mapping
- **Data:** MediaStore, Room and DataStore
- **Quality:** privacy-safe analytics, crash reporting and diagnostics

The final ownership, public contracts, dependency direction, navigation model, concurrency rules and package/module granularity are approved only in Issue #16 after User Flows and low-fidelity Wireframes are stable.

## Voice and privacy constraints

- Push-to-talk only
- Listening timeout at most six seconds
- One retry
- At most one short TTS sentence
- Log intent/result and coarse diagnostic context, not raw voice
- Confidence threshold through simple Remote Config or Build Config
- If cloud STT is used, obtain separate consent and disclose processing destination and retention
- Full personal-file titles, precise location, contacts and unnecessary identifiers are prohibited from telemetry

## Verification baseline

- Healthy build on two devices in week 1
- 5,000-file library without blocking error
- Screen-off/background playback
- Call, headset, Bluetooth and route edge cases
- Android 10, Android 12 and Android 14+ coverage
- Pixel/stock, Samsung/One UI and Xiaomi/MIUI
- At least three vehicles/head units
- Zero open P0 before Beta

## Decision stages

1. **Issue #2:** review ADR-0001 through ADR-0006; mark each Accepted or Revised and record assumptions.
2. **Issue #3:** define User Flows and complete state inventory.
3. **Issue #15:** validate low-fidelity Wireframes and navigation behavior.
4. **Issue #16:** approve final Software Architecture baseline and add/supersede ADRs where new consequential decisions are required.
5. **Issue #4:** implement the approved foundation and architecture boundaries.

## Decisions intentionally left open until Issue #16

- Final navigation graph and back behavior
- Screen-level and shared state ownership details
- Voice overlay/navigation ownership
- ViewModel and use-case boundaries
- Exact package and Gradle-module layout
- Queue/position restoration implementation for P1 FR-08
- Detailed error type hierarchy and concurrency policy

These decisions must not be guessed during feature implementation.
