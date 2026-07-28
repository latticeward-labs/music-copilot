# Technical Decisions - MVP v1.0

- **Status:** Proposed; planning approval pending in [Issue #2](https://github.com/latticeward-labs/music-copilot/issues/2)
- **Authority:** [PRD v1.0](PRD.md) and [MVP v1.0](MVP.md)
- **Applies to:** Android MVP v1.0 and the 10-week build

The approved v1.0 product documents define a proposed architecture, not a locked v1.1 implementation baseline.

## Proposed baseline

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

## Boundaries

- UI: screens, Driving Mode and Voice states
- Domain: use cases
- Playback: Media3, queue, Media Session and Audio Focus
- Voice: STT/TTS and restricted intent mapping
- Data: MediaStore, Room and DataStore

No seven-module Gradle map is defined in v1.0. No fixed `minSdk`, `compileSdk`, `targetSdk` or ABI-filter policy is defined beyond Android 10+ product support.

## Voice and privacy

- Push-to-talk only
- Listening timeout at most six seconds
- One retry
- At most one short TTS sentence
- Log intent/result, not raw voice
- Confidence threshold through simple Remote Config or Build Config
- If cloud STT is used, obtain separate consent and disclose processing destination and retention

Version 1.0 does not require on-device-first recognition and does not define network recognition as disabled by default.

## Verification baseline

- Healthy build on two devices in week 1
- 5,000-file library without blocking error
- Screen-off/background playback
- Call, headset, Bluetooth and route edge cases
- Android 10, Android 12 and Android 14+ coverage
- Pixel/stock, Samsung/One UI and Xiaomi/MIUI
- At least three vehicles/head units
- Zero open P0 before Beta

## ADR register

ADR-0001 through ADR-0006 are **Proposed** and must be reviewed through Issue #2 before becoming binding.
