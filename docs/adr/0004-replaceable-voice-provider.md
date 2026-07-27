# ADR-0004: Replaceable voice provider boundary

- **Status:** Proposed
- **Date:** 2026-07-27
- **Decision owner:** Latticeward Labs
- **Approval:** [Issue #2](https://github.com/latticeward-labs/music-copilot/issues/2)
- **Authority:** PRD v1.0 FR-05 to FR-07 and sections 08-09; MVP v1.0 Voice Contract

## Context

The MVP uses push-to-talk STT and short TTS, but provider availability, offline behavior, latency and privacy characteristics can vary by device and locale. Provider details must not leak into product rules.

## Decision

Define replaceable `SpeechRecognizer` and `SpeechSynthesizer` domain-facing interfaces, with Android STT/TTS adapters selected through Hilt.

- Push-to-talk is the only microphone entry point.
- Listening lasts at most six seconds.
- Playback ducks while listening and recovers predictably.
- A transcript exists in memory only for the active command and is discarded immediately.
- Application code never persists raw audio or transcript.
- TTS output is at most one short sentence.
- Provider errors are normalized into typed domain failures.
- If a cloud STT option is proposed, it requires a separate consent, processor, destination and retention decision before release.

## Consequences

Voice orchestration and intent tests do not depend on Android provider implementation. A provider can be replaced without changing the Voice contract. The trade-off is adapter and lifecycle complexity.

## Rejected alternatives

- **Direct Android STT calls from UI/ViewModel:** couples UI lifecycle and provider details to product logic.
- **Always-on wake word:** outside scope and increases privacy/battery risk.
- **Persisting transcripts for debugging:** conflicts with approved privacy rules.

## Validation

Contract tests use fakes for success, timeout, cancellation, no match, provider error and TTS failure. Instrumented tests cover microphone denial and lifecycle cancellation without stopping or corrupting playback.
