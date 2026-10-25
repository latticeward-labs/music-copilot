# Issue #10 — P10 | Foreground, Focus/Route, Cancellation, Late-Callback & Race Hardening

**Date:** 2026-10-25  
**Branch:** `feature/10-ptt-voice-five-control-intents`  
**Source status:** `P10_SOURCE_IMPLEMENTED_UNBUILT`  
**Verification:** `SOURCE_REVIEW_ONLY / AUTHORED_ONLY / NOT_RUN`; no compile, Gradle, Actions, JUnit, Lint, emulator, phone or APK.

## Sources and constraints

Source package: `MSM2(1)(1).rar` (12 PDFs), SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`. Specifically reviewed:
- **ADR-0002 Service-Owned Media3 Playback:** audio focus, incoming call, wired/Bluetooth route-loss and no wrong-speaker auto-resume belong to the single MediaSessionService; playback must continue independently of Activity UI.
- **ADR-0004 Replaceable Voice Provider:** cancelable STT/TTS, provider absence must preserve manual controls.
- **Music Copilot System Architecture, pages 10–11, 15–16:** explicit tap → bounded Voice session → STT → deterministic mapper → typed playback → short TTS; at most 6s listening and one retry; one service/queue owner; structured concurrency and safe interruption.
- **GitHub Issue #10 and source P00–P09:** exactly five direct intents, no wake listener, no cloud fallback, no separate playback owner, no tests in execution phases P00–P12.

This document records **code changes and static inspection only**, not observed Android event ordering.

## Baseline risks found in P09 source

1. `DefaultVoiceSessionCoordinator.start()` unconditionally invalidated a previous active session and replaced it. A rapid double Mic tap racing Compose could cancel/replace an already listening session instead of rejecting the second tap.
2. Android recognizer's `invokeOnCancellation` always used `Handler.post`, even when already on Main: a retry might see the old recognizer still registered or a delayed cancel with the **same logical VoiceSessionId** might kill attempt two. `RecognitionListener` callback ownership needed identity checks serialized on Main, including unusual OEM threads.
3. `AndroidOfflineTextToSpeechEngine` `stop(sessionId)` matched only a logical session id. Two attempts reuse that id; a delayed stop could end a newer utterance.
4. `MainActivity.onStop` already canceled Voice but `ApplicationBindings.onUiStopped` itself detached MediaController without a defensive cancellation. If Activity callback ordering changed, a late request could slip between detach/reconnect.
5. P07 sent Voice-unDuck without waiting for a service receipt before Activity detached its MediaController. Service-side 22s TTL remained the last safeguard, but an already-sent release packet could be lost on immediate disconnect.
6. Output-disconnect and focus safety paths already existed in Issue #8; BT sink arrival was only a *suggestion/observation*, not a checked Voice session boundary. New output while capturing speech should invalidate Voice, but never start audio.

## Implementation delivered

| File | P10 source change |
|---|---|
| [VoiceLifecycleSafetyPolicy.kt](../../app/src/main/java/com/latticeward/musiccopilot/core/application/voice/VoiceLifecycleSafetyPolicy.kt) | **New pure policy**: only Idle admits a fresh explicit tap; all non-Idle states require invalidation on UI boundary; no Voice state is restored after recreation |
| [DefaultVoiceSessionCoordinator.kt](../../app/src/main/java/com/latticeward/musiccopilot/voice/session/DefaultVoiceSessionCoordinator.kt) | Atomic lock admission rejects a second tap while Listening/Processing/Executed/Failed; rejects start/retry if Activity STARTED lease absent; leaves existing generation guards, one-command claim, one allowed retry, ACK gating and 6s timeout intact |
| [VoiceBindings.kt](../../app/src/main/java/com/latticeward/musiccopilot/app/di/VoiceBindings.kt) | Injects `canBeginSession` callback into same app-scoped coordinator; test fixtures may supply a fake foreground observation |
| [ApplicationBindings.kt](../../app/src/main/java/com/latticeward/musiccopilot/app/di/ApplicationBindings.kt) | Volatile, nonpersisted `uiVoiceStarted` set only by onUiStarted/onUiStopped; onUiStopped **denies new Voice → cancels current Voice → detaches Controller**; revoked media access cancels Voice before index invalidation; close disallows any subsequent Voice |
| [AndroidOnDeviceSpeechToTextEngine.kt](../../app/src/main/java/com/latticeward/musiccopilot/voice/android/AndroidOnDeviceSpeechToTextEngine.kt) | Makes live recognizer reference volatile; cancels immediately if already on Main; captures exact recognizer instance before queued cancel; `onResults/onError` serialize on Main and check active recognizer identity and cancellation before completion |
| [AndroidOfflineTextToSpeechEngine.kt](../../app/src/main/java/com/latticeward/musiccopilot/voice/android/AndroidOfflineTextToSpeechEngine.kt) | Captures exact `activeUtteranceId`; asynchronous stop/cancellation of utterance A cannot stop newer utterance B even if both share the same logical session ID |
| [MusicPlaybackService.kt](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/MusicPlaybackService.kt) | On Bluetooth sink arrival, immediately revokes temporary Voice duck and forwards **ROUTE_INVALIDATED** to existing app Voice owner, without Play/Resume or changing the saved BT invitation policy; existing urgent output-loss/focus latch stays authoritative |
| [PlaybackControllerConnection.kt](../../app/src/main/java/com/latticeward/musiccopilot/playback/controller/PlaybackControllerConnection.kt) | Tracks final in-flight Voice duck release; UI disconnect invalidates its generation/visible snapshot immediately but keeps the old controller **at most 1s** to receive service release acknowledgement before releasing it. Service 22s TTL remains fallback |
| [Issue10Phase2VoiceStateMachineTest.kt](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue10Phase2VoiceStateMachineTest.kt) | Existing authored-only replacement scenario adapted to required P10 explicit cancel-then-new-start semantics; **not executed** |
| [Issue10Phase10LifecycleRaceSafetyTest.kt](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue10Phase10LifecycleRaceSafetyTest.kt) | **17 new source-authored JUnit test methods, 0 executed** |

**No new service, player, audio focus manager, microphone listener, Room/DataStore entity, cloud provider, manifest permission or screen.** The existing `onStop` from `MainActivity`, P03 navigation-origin cancellation, P07 token-guarded Duck and #8 Media3 focus/route reducer continue operating. Double cancel is idempotent and does not issue a player command.

## Truth table: failure & concurrency boundaries

| Boundary | Source handling | Remaining proof |
|---|---|---|
| Two rapid taps on Mic | Second `start` fails under gate unless state already Idle; no active generation is replaced | M31 physical double-tap |
| Late tap/Retry after onStop | `uiVoiceStarted=false` before cancellation/detach; injected predicate rejects both | M44/M45 Activity lifecycle |
| Rotation or Activity recreation | Existing onStop cancels; no Voice state persisted/restored; no onStart/onResume auto-record | M44 and ActivityResult edge |
| Permission denied or revoked | Mic permission rechecked at explicit start and onResume, cancellation on loss; media-access revoke cancels Voice before library invalidation | M06–M10, M47 |
| Stale onResults/onError from attempt 1 | Same recognizer instance must own `live`; Main-serialized callback; coordinator Job generation still matches | M27/M28 / source JVM |
| Late STT cancel racing attempt 2 | Captures old recognizer object at issue time; cannot tear down new recognizer with same VoiceSessionId | OEM STT cancellation race |
| Late TTS stop racing new feedback | Session ID **and exact utterance ID** must match current; stale cancellation ignored | M39/M40 audible overlapping |
| Late service ACK after Cancel | Coordinator invalidates generation; does not present Executed or success TTS. **Already accepted Media3 mutation cannot be rolled back** | M30/M33 |
| Manual Back/Driving exit | Existing origin-owned callback cancels STT/TTS/P07 Duck; no auto Play or navigate into Voice destination | M29/M43/M46 |
| Call/focus loss | Existing Media3 focus reducer/latch cancels Voice and service instantly revokes Duck; Media3 playback policy outranks Voice | M41, call/focus OEM |
| Wired/headset/Bluetooth route lost | Existing urgent service route/noisy handlers invalidate queued receipts, latch safe Pause and cancel Voice | M42/M58, actual headset |
| Bluetooth sink available | Observation invalidates Voice and Duck, **not** playback/BT permission/Driving navigation/Play | M55/M56 OEM route |
| Activity detach after P07 unDuck | Bounded 1s old Controller release receipt hold, then detach; no microphone/Activity retained | M34–M38 audible restore |
| App process death | In-process coordinator, provider callbacks and MediaController disappear; Voice never revived from saved State; service owns only ongoing playback | P13 process/recents tests |

### Strict limits: no unsupported "exactly once" or "never leaks" claims

- A Media3 command accepted by the worker *before* cancellation may still mutate playback. P02/P06 suppress duplicate dispatch and stale **acknowledgements**, but cannot undo a command executed previously; physical behavior must be measured.
- MediaSession release receipt can be delayed/dropped; 1s controller-drain is bounded best effort and **does not guarantee** restoration. P07 service-side 22-second expiry is a fallback only when its process lives.
- Android OEM audio focus, phone calls, Bluetooth route transitions, microphone/foreground behavior and late callback semantics remain **NOT_VERIFIED**. Wrong-speaker playback is a P0 release blocker, not a source-only PASS.
- Android 10/11 remains manual-only for STT per P04. No remote recognition, network fallback or background wake-phrase is enabled.
- A transient foreground window or Activity going away before actual dispatch is mitigated by coordinator generation check, app started lease, Media3 receipt/focus/route guard; full proof requires P13 tests.

## Authored tests (17 new + 1 revised fixture)

`Issue10Phase10LifecycleRaceSafetyTest.kt` includes 17 methods: explicit Idle admission, non-Idle cancellation, no state restore, second-tap suppression, late Mic rejection after onStop, background Retry rejection, late STT result after route-cancel, late Media3 ACK, repeated Cancel, fresh session after explicit Cancel, recognizer identity checks, exact TTS utterance stop, BT sink safety, app boundary cancellation order, media permission revoke order, release receipt before detach, and absence of a second Player/history. The P02 legacy authored fixture was updated to cancel-then-start.

All source tests: `AUTHORED_ONLY`. Every actual JVM/instrumented/Compose/OEM phone test: `NOT_RUN`.

## Definition of Done (source phase only)

- [x] One Voice session admission gate and foreground lease, not an Activity-local recognizer.
- [x] Atomic refusal of rapid second tap and late background Retry.
- [x] Late/cancelled STT and TTS callbacks matched to recognizer/utterance identity.
- [x] Foreground/media permission loss invalidation before controller/MediaStore boundary.
- [x] Audio Focus and BT route policy remains service-owned with no implicit Resume; sink arrival invalidates Voice.
- [x] Bounded controller drain for un-Duck release and existing service TTL.
- [x] 17 new JUnit scenarios authored; prior authored replacement fixture corrected for explicit cancel.
- [x] Source evidence recorded; no Voice raw transcript/exception log/history persisted.
- [ ] **Kotlin compilation/Gradle/Lint, JUnit, Compose UI, instrumented, emulator, physical device, APK, PR merge and Issue closure**: not performed.

**Final status:** `P10_SOURCE_IMPLEMENTED_UNBUILT`; **P11 NEXT — privacy, permission, cloud-consent boundary and data retention audit.**
