# Issue #10 — P08 | One-Sentence Voice Feedback, Offline TTS & Safe Completion

**Date:** 2026-10-25  
**Status:** `P08_SOURCE_IMPLEMENTED_UNBUILT` — all authored tests `NOT_RUN`, Build/APK `NOT_BUILT`.  
**Issue:** https://github.com/latticeward-labs/music-copilot/issues/10  
**Branch:** `feature/10-ptt-voice-five-control-intents`  
**Attachment baseline:** `MSM2(1)(1).rar`, SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`, 12 PDFs. Relevant sources: ADR-0004, Music Copilot Code & Technical Documentation, MVP and System Architecture; repository Issue #10 P00–P07 and accepted ADRs prevail for implementation details.

> **No compilation, Gradle task, JUnit/UI/instrumented test, Lint, GitHub Actions, simulator, real phone or APK was executed.** All statements below describe source changes and static inspection only, not observed audio or QA PASS.

## 1. P08 scope and architectural rules

- Exactly **one short English sentence maximum per *eligible terminal outcome***. Five direct successful transport commands map to five deterministic short sentences. A final failure (timeout, no match, unavailable) maps to one distinct non-personal sentence. **Retryable intermediate failures are silent** so speech does not interrupt the user's next explicit attempt.
- Success feedback may be requested **only after the Media3 `PlaybackGateway` ACK has been accepted by the P06 Coordinator** and `Executed` has already been published. TTS status is not an ACK substitute and cannot reverse the service command.
- Feedback is optional and **fail-open**: when the TTS engine is absent, lacks a packaged offline English voice, returns Failure, throws or exceeds a **2,000ms speech budget**, the state stays Executed/Failed as appropriate; manual controls and the queue remain under Media3 authority.
- TTS never owns STT, the queue or Play/Pause. An existing Android offline English-only implementation is reused; no network, storage, second audio synthesis instance, Activity, microphone entry or playback controller is added.
- The full versioned feedback catalog is backed by **eight Android `strings.xml` resources** in production, with a pure English fallback for JVM/no-Context scenarios. Only an immutable `VoiceFeedbackKey` enters the resource lookup; raw transcripts, personal music metadata or service error exception strings never enter speech.
- The existing `TextToSpeechEngine` port remains replaceable. No changes to `SpeechRecognitionResult`, five-intent mapper, on-device STT adapter, `MediaSessionService`, permission or consent behavior.

## 2. Catalog: final spoken copy

| Origin/result | Resource key | Exact English sentence |
|---|---|---|
| `PLAY` ACK | `i10_feedback_play` | Play command accepted. |
| `PAUSE` ACK | `i10_feedback_pause` | Playback paused. |
| `NEXT` ACK | `i10_feedback_next` | Next track selected. |
| `PREVIOUS` ACK | `i10_feedback_previous` | Previous command accepted. |
| `SEEK_BACK` ACK | `i10_feedback_seekback` | Moved back ten seconds. |
| Final `VOICE_TIMEOUT` | `i10_feedback_timeout` | I didn't hear a command. |
| Final `VOICE_NO_MATCH` | `i10_feedback_notrecognized` | Command not recognized. |
| Final unavailable/provider/ACK failure | `i10_feedback_unavailable` | Use the manual player controls. |

No `PLAY_ARTIST` success message is permitted in Issue #10. The wording for Play/Previous emphasizes *accepted command* rather than claiming the device has produced an audible sound; buffering/focus and transport details remain service-owned.

`VoiceFeedbackPolicy.isShortSentence` enforces a nonblank, trimmed, printable, at-most-120-character string with exactly one terminating period and no earlier sentence punctuation or newlines. Invalid or throwing catalog output is discarded without speech. Keys are stable; Android resource translations may be added later with the same policy enforced on the resolved string.

## 3. Terminal lifecycle and concurrency

```text
Explicit PTT → duck ACK → ≤6s STT → deterministic intent mapping
  → typed Media3 command → exact correlated service ACK
  → VoiceSessionState.Executed published
  → check terminal generation + claim one feedback
  → optional Android offline TTS (bounded to 2s) → service duck release
```

- **ACK failure**: source Coordinator instead publishes Failed (non-retryable playback error), releases Voice duck and *may* speak the corresponding failure sentence if offline TTS is available. It never speaks a command-success message. A playback command is never retried because TTS failed.
- **Recognition no-match/timeout first attempt**: Failed with `canRetry=true` and **no TTS**. If the user explicitly retries, the same logical Session has a fresh generation/duck token; second terminal failure may say one sentence.
- **Provider unavailable/invalid command/error**: fail-closed; optional final message. Only known error code class chooses the copy, not raw utterance text or exception.
- **Cancel, Back, origin exit, permission revocation, focus/route interruption or replacement**: generation invalidation and existing `tts.stop(sessionId)` prevent late feedback; stale session/attempt cannot claim a new output. Already-completed Media3 commands are not rolled back.
- **One output at a time**: Coordinator's `feedbackClaimed` gate is set *before suspension*. Adapter's `active`, `activeUtteranceId` and Android Main-Looper serialization prevent two overlapping active outputs. `QUEUE_FLUSH` only controls TTS's own speech queue, not Media3 audio.
- **Bounded time**: `withTimeoutOrNull(2_000L)` prevents a hung TTS callback from holding the Coordinator's job or P07 duck lease indefinitely; adapter's cancellation callback stops the active utterance. The P07 service also retains its 22-second emergency Duck TTL. Real OEM timing/stop response remain device-test obligations.
- **Missing TTS**: no repeated errors or extra screen. The current terminal state remains available to both Full Player and Driving; P09 owns full WF-08 accessibility and visual copy.

## 4. Files committed for P08

| Source | Change |
|---|---|
| [VoiceFeedbackCatalog.kt](../../app/src/main/java/com/latticeward/musiccopilot/voice/feedback/VoiceFeedbackCatalog.kt) | **New** pure catalog port, eight stable keys, approved outcome policy, single-sentence validation and 2s limit |
| [AndroidVoiceFeedbackCatalog.kt](../../app/src/main/java/com/latticeward/musiccopilot/voice/android/AndroidVoiceFeedbackCatalog.kt) | **New** Android `R.string` resolver with no STT / Media3 dependency |
| [strings.xml](../../app/src/main/res/values/strings.xml) | Eight English-first localizable message strings, no extra permission |
| [DefaultVoiceSessionCoordinator.kt](../../app/src/main/java/com/latticeward/musiccopilot/voice/session/DefaultVoiceSessionCoordinator.kt) | Terminal state published before optional TTS; one-shot generation-bound feedback, silent retryable failure, 2s limit, cancellation-safe state |
| [AndroidOfflineTextToSpeechEngine.kt](../../app/src/main/java/com/latticeward/musiccopilot/voice/android/AndroidOfflineTextToSpeechEngine.kt) | Validates one short sentence and observes both Android TTS `onError` overloads and `onStop` in addition to existing `onDone` |
| [VoiceBindings.kt](../../app/src/main/java/com/latticeward/musiccopilot/app/di/VoiceBindings.kt) | Injects context-backed catalog in production or pure fallback for synthetic fixtures |
| [ApplicationBindings.kt](../../app/src/main/java/com/latticeward/musiccopilot/app/di/ApplicationBindings.kt) | Passes existing application Context through composition root; no new singleton |
| [Issue10Phase8FeedbackTest.kt](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue10Phase8FeedbackTest.kt) | **15 authored-only JUnit methods** covering policy, ACK, provider absence/failure, timeout, cancellation, retries and source boundaries |
| [Phase8VoiceFoundationTest.kt](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Phase8VoiceFoundationTest.kt) | One pre-existing authored assertion adapted from old "Done" to explicit `PAUSE` message (not executed) |

No production changes to Media3 player/session, playback queue, Bluetooth, AndroidManifest, Room/DataStore, cloud permissions, Voice mapper, or the Voice entry routes.

## 5. Authored-only test matrix — execution remains zero

The 15 cases cover: five success mappings; eight messages short/one-sentence; invalid/multi-sentence copy; stable privacy-safe resource keys; invalid/throwing catalog; silent retryable vs final failure; no pre-ACK success speech; ACK failure doesn't speak success; TTS unavailable; TTS Failure; TTS hang/2s bounded wait; cancellation while TTS active; second-attempt final no-match speaks only once; main-thread utterance identity and stop callback; Android resources and no personal transcript/network. One Phase8 legacy assertion was updated.

**These are source files, not observed test results.** `T06` can be marked `AUTHORED_ONLY`, but its `execution_status` remains `NOT_RUN`; all physical checks (especially M39/M40, no overlapping audio on AirPods/car, back/call/focus interruptions) remain with the user in P13.

## 6. Known limitations and next phases

- Android 10/11 speech recognition remains manual-only as decided in P04. TTS readiness itself depends on installed offline English voices; Android may delay provider initialization.
- A two-second speech cap favors low distraction but may truncate an abnormally slow provider. P13 checks whether wording/duration is suitable on real phones and vehicles. Provider availability cannot be asserted by source inspection.
- Service Duck belongs to P07 and restores after bounded TTS; outside focus interruption it may remain for the optional feedback interval. No auto-resume is issued.
- P09 owns WF-08 precise visual/a11y states, focus order and accessible one-layer Driving interaction. P10 owns deep lifecycle/OEM/race checks across TTS and Duck. P11/P12 own privacy/final static audit; P13 owns one cumulative APK and phone QA.
- The Issue #10 six-intent MVP architecture still reserves `PLAY_ARTIST` for #11. No output here bypasses the five-intent restriction.

## 7. Definition of Done — source only

- [x] Eight deterministic English sentences and production Android string resource catalog.
- [x] At most one terminal sentence and silent intermediate recognition Retry.
- [x] TTS only after valid ACK for success; fallback message after final Failed state.
- [x] One-shot, generation-aware Coordinator claim before suspension.
- [x] Provider-unavailable/error/timeout/cancel does not undo Media3 command or block manual playback.
- [x] Android callback `onError`/stop completion and utterance identity handling.
- [x] 15 JUnit cases authored (0 executed) and legacy authored assertion adjusted.
- [ ] **Gradle/compile/Lint/CI/Unit/UI/physical audio test/APK**: **NOT_RUN**.

**Final source status:** `P08_SOURCE_IMPLEMENTED_UNBUILT`; **P09 NEXT — WF-08 Voice UI & accessibility**.
