# ADR-0004: Replaceable voice provider boundary

- **Status:** Accepted
- **Accepted:** 2026-07-28
- **Revised:** 2026-07-28
- **Date:** 2026-07-27
- **Decision owner:** Latticeward Labs
- **Approval:** [Issue #2](https://github.com/latticeward-labs/music-copilot/issues/2)
- **Authority:** PRD v1.1 FR-06 to FR-09 and sections 08-09; MVP v1.1 Voice Contract

## Context

The MVP uses push-to-talk STT and short TTS, but provider availability, offline behavior, latency and privacy characteristics can vary by device and locale. Provider details must not leak into product rules.

## Decision

Define replaceable `SpeechRecognizer` and `SpeechSynthesizer` domain-facing interfaces, with consent-aware Android STT/TTS adapters selected through Hilt. Provider selection is on-device-first.

- Push-to-talk is the only microphone entry point.
- Listening lasts at most six seconds.
- Playback ducks while listening and recovers predictably.
- A transcript exists in memory only for the active command and is discarded immediately.
- Application code never persists raw audio or transcript.
- TTS output is at most one short sentence.
- On API 31+, check `SpeechRecognizer.isOnDeviceRecognitionAvailable()` and use `createOnDeviceSpeechRecognizer()` when available.
- The general Android recognizer may stream audio to remote servers. It is disabled by default and requires clear, revocable consent describing the available processor, destination and retention behavior.
- If on-device recognition is unavailable and consent is not granted, Voice remains unavailable while full manual control remains functional.
- Availability, consent and provider errors are normalized into typed domain results.
- No embedded Whisper/Vosk-style model is included in MVP v1.1; a future embedded model requires a separate size, battery, accuracy, ABI and privacy decision.

## Consequences

Voice orchestration and intent tests do not depend on Android provider implementation. A provider can be replaced without changing the Voice contract. Users never silently cross the on-device/network boundary. The trade-off is adapter, consent, capability-detection and lifecycle complexity; Voice coverage on older or unsupported devices may be lower.

## Rejected alternatives

- **Direct Android STT calls from UI/ViewModel:** couples UI lifecycle and provider details to product logic.
- **Always-on wake word:** outside scope and increases privacy/battery risk.
- **Persisting transcripts for debugging:** conflicts with approved privacy rules.

## Validation

Contract tests use fakes for on-device available/unavailable, consent granted/denied/revoked, success, timeout, cancellation, no match, provider error and TTS failure. Instrumented tests cover API 31 capability detection, microphone denial and lifecycle cancellation without stopping or corrupting playback. Beta reporting segments Voice results by provider mode and excludes unsupported/no-consent sessions from accuracy denominators while retaining them in availability reporting.
