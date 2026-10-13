# Issue #4 — Phase 8 Replaceable Voice Provider foundation

- **Verified date:** 13 October 2026
- **Verdict:** **PASS — Phase 8 foundation only; NOT a full Voice feature or on-device acceptance**
- **Implementation branch:** [feature/4-android-foundation](https://github.com/latticeward-labs/music-copilot/tree/feature/4-android-foundation)
- **Issue:** [#4 Android Foundation](https://github.com/latticeward-labs/music-copilot/issues/4)
- **Tested source SHA:** [5619393f10bc33b14e14d64b2b641971b8b8fe7b](https://github.com/latticeward-labs/music-copilot/commit/5619393f10bc33b14e14d64b2b641971b8b8fe7b)
- **Authoritative attached English RAR:** `MSM2(1)(1).rar`, independently verified SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`.
- **Relevant decisions:** [Issue #16 public contracts](../architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md), [Runtime Policies](../architecture/RUNTIME_POLICIES.md), [ADR-0004](../adr/0004-replaceable-voice-provider.md), [ADR-0005](../adr/0005-deterministic-intent-mapping.md), [Voice Contract](../VOICE_CONTRACT.md).

## 1. Concrete production inventory and ownership

| Component | Source | Ownership and safety |
|---|---|---|
| Replaceable STT public port | [SpeechToTextEngine](../../app/src/main/java/com/latticeward/musiccopilot/voice/api/SpeechToTextEngine.kt) | Typed recognize/cancel/availability. Provider only returns ephemeral recognition, never playback mutation. |
| Replaceable TTS public port | [TextToSpeechEngine](../../app/src/main/java/com/latticeward/musiccopilot/voice/api/TextToSpeechEngine.kt) | Typed one-sentence feedback; error never rolls back acknowledged playback. |
| Android on-device STT | [AndroidOnDeviceSpeechToTextEngine](../../app/src/main/java/com/latticeward/musiccopilot/voice/android/AndroidOnDeviceSpeechToTextEngine.kt) | Uses `SpeechRecognizer.createOnDeviceSpeechRecognizer()` **only on API 31+**, checks RECORD_AUDIO and local service availability. No general `createSpeechRecognizer()` or remote fallback. Runtime recognizer cancellation/destroy on Main. |
| Offline English TTS | [AndroidOfflineTextToSpeechEngine](../../app/src/main/java/com/latticeward/musiccopilot/voice/android/AndroidOfflineTextToSpeechEngine.kt) | Requires installed English voice whose `isNetworkConnectionRequired` is false; bounded feedback length and stop/shutdown. No implicit network voice selection. |
| Typed disabled providers | [UnavailableVoiceEngines](../../app/src/main/java/com/latticeward/musiccopilot/voice/android/UnavailableVoiceEngines.kt) | Fail-closed test/no-context fallback, no silent success. |
| Approved deterministic policy | [VoicePolicy](../../app/src/main/java/com/latticeward/musiccopilot/voice/policy/VoicePolicy.kt) | Listening 6 seconds, MAX_ATTEMPTS 2, direct-confidence threshold 0.90, accepted bounded artist criteria, feedback max 120 chars. |
| Six-intent mapping seam | [RestrictedVoiceIntentMapper](../../app/src/main/java/com/latticeward/musiccopilot/voice/intent/RestrictedVoiceIntentMapper.kt) | Exactly PLAY, PAUSE, NEXT, PREVIOUS, SEEK_BACK and PLAY_ARTIST; explicit leading `musico`, English-first, conservative low/missing-confidence handling. Pure deterministic code with no SDK/cloud/Player dependency. |
| One Voice authority | [DefaultVoiceSessionCoordinator](../../app/src/main/java/com/latticeward/musiccopilot/voice/session/DefaultVoiceSessionCoordinator.kt) | Application-owned generation/child SupervisorJob; replacement/Back/Driving exit/permission/focus/route cancellation; 6s timeout, exactly one Retry; stale callbacks ignored. Calls only public PlaybackGateway. Acknowledgement precedes Executed/TTS. |
| DI integration | [VoiceBindings](../../app/src/main/java/com/latticeward/musiccopilot/app/di/VoiceBindings.kt), [ApplicationBindings](../../app/src/main/java/com/latticeward/musiccopilot/app/di/ApplicationBindings.kt) | One coordinator and one set of engine ports per application graph; no Activity/provider singleton, no direct ExoPlayer/MediaSession imports; provider shutdown on graph close. |
| Android capability declaration | [AndroidManifest](../../app/src/main/AndroidManifest.xml) | RECORD_AUDIO declaration and recognition-service package visibility. Declaration does **not** grant user microphone permission or constitute provider/privacy consent. No INTERNET permission added. |

## 2. Runtime and privacy invariants

- Push-to-talk is the only approved entry: `VoiceSessionCoordinator.start` must be called from a later explicitly user-authorized UI flow, not automatically from service/app startup. The recognizer is never started at application construction.
- An on-device-only STT platform option is selected when available. **Android 10/11 (API 29/30) have no production speech recognizer in this foundation**; capability failure returns typed fallback without attempting cloud STT. Android 12+ is only *potentially* capable: microphone consent, availability, installed language data and OEM behavior remain dependent on the device.
- Recognition audio, transcript, recognized artist, confidence values and confirmation tokens are transient and not written into Room/DataStore or emitted as telemetry.
- Six intent names are declared in the pure mapper. Five direct transport commands may go through `PlaybackGateway` after safe mapping. **PLAY_ARTIST intentionally does not execute**; it awaits a separately validated artist matcher/confirmation path in Issue #11. Low confidence/unsupported speech never guesses a command.
- `withTimeoutOrNull(6_000)` bounds the recognition operation and `VoiceSessionCoordinator` permits at most two attempts per session. Replacement synchronously invalidates generation, cancels the child and asks STT/TTS to stop; old callback results cannot mutate Voice state or playback.
- `Executed` is exposed only after a typed `PlaybackGateway.execute` success; TTS is optional feedback after the acknowledgement. Voice cannot create a Player or mutate MediaSession directly.
- No Cloud STT backend SDK, account, endpoint, streaming library, remote key or app INTERNET permission introduced. Separate legal/provider/consent/retention gate is mandatory before future remote enablement.

## 3. Real proof versus test scope

[Phase8VoiceFoundationTest](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Phase8VoiceFoundationTest.kt) supplies **9 new synthetic JVM tests**, including: six-intent bounded grammar and invocation phrase, timeout via virtual clock, exactly one Retry, cancellation/replaced generation, response staleness, Back/Driving Exit/permission invalidation, provider unavailability, typed Playback rejection and no TTS success before acknowledgement, and source-level cloud-off checks. Existing LifecycleScopesTest and Phase 7 contracts continue to run.

**GitHub Actions:** [run #38454339598](https://github.com/latticeward-labs/music-copilot/actions/runs/38454339598) on tested SHA `5619393f10bc33b14e14d64b2b641971b8b8fe7b`, job `115493062059`, `completed/success`. Real log confirms `SELFTEST PASS: 24 fixtures`, `ARCHITECTURE PASS: one :app module; 52 package paths; 103 source files`, `:app:testDebugUnitTest`, `:app:assembleDebug`, `:app:assembleRelease`, `ROOM_SCHEMA_EXPORT PASS` and `BUILD SUCCESSFUL in 7m 22s`.

**Actual [artifact #11842495336](https://github.com/latticeward-labs/music-copilot/actions/runs/38454339598/artifacts/11842495336)** examined: 10 JVM JUnit XML suites, **58 tests, 0 errors, 0 failures, 0 skipped**; Debug and unsigned Release APKs; Room v1 schema byte-identical to [committed Room schema](../../app/schemas/com.latticeward.musiccopilot.data.room.MusicCopilotDatabase/1.json), Git blob SHA `c3b1ba7affdaa1a9d6425d3b3dc7ddf10adf300b`.

**Correction history:** [run #38453359125](https://github.com/latticeward-labs/music-copilot/actions/runs/38453359125) **failed** at Android STT/TTS Kotlin callback inference (`AppResult.Success<T>` versus `Failure`), fixed by explicitly typing `suspendCancellableCoroutine<AppResult<...>>`, with success confirmed only in subsequent run #38454339598. Cancelled predecessor workflows are not counted.

## 4. Deferred independent gates

- No actual on-device mic recognition/TTS output, runtime permission dialog, locale/engine installation, Bluetooth/audible route, inter-app call interruption, background lifecycle or OEM test executed. Android 10/11 Voice feature support is an **open coverage gap**.
- Current Voice UI/Driving entry, confirmation UX, acoustic ducking and wake/invocation UX need Issue #10 and subsequent field QA.
- PLAY_ARTIST, similarity resolution, one-option/two-option confirmation and real local queue playback require Issues #11 and #7; failure is safe in this foundation and cannot be advertised as completed artist search.
- **Phase 8 foundation PASS is not Phase 8 feature-complete Voice, final MVP acceptance, or a physical-device QA PASS.** Original Issue #4 cross-phase acceptance checklist remains 1/14 checked, 13 open; leave Issue OPEN/In Progress, branch unmerged until later gates.
