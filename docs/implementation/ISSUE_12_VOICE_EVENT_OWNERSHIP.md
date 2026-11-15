# Issue #12 — P06: Voice detection, failure, execution and observed manual correction

**Date:** 2026-11-15
**Branch:** `feature/12-privacy-safe-telemetry-measurement`
**Status:** `SOURCE_IMPLEMENTED_UNCOMPILED_NOT_RUN` — source changes and source-authored test fixtures only; no compilation, JVM/Android tests, Gradle, CI/Actions, emulator, APK or handset evidence.
**Source archive:** user-uploaded `MSM2(1)(1).rar`, exact SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`, 12 English PDFs opened directly.

## 1. Authoritative source and unresolved contract discrepancy

- **English PRD v1 Clarified, printed p.10:** exactly eight external events (including `intent_detected`, `intent_failed`, `command_executed`, `manual_correction`); direct-command success >=85%, correction rate <15%, incorrect execution <3%. Raw audio is never stored/transmitted by default.
- **English MVP v1, printed p.10:** `intent_detected(intent_type,confidence_bucket)`, `command_executed(intent_type,latency_bucket)`, `manual_correction(previous_intent,correction_type)`, and consent-aware coarse categories. The shorter event table omits `intent_failed`.
- **English System Architecture, printed p.17:** eight external families, typed event -> privacy allowlist -> non-authoritative best-effort Quality adapter. No personal track or full speech content.
- **English technical blueprint, printed p.39:** `intent_failed(failure_type, mapper_version)`, explicitly **different** from this branch's existing [P03 eight-event schema](ISSUE_12_EVENT_SCHEMA.md), which uses `intent_failed(intent_type,failure_code,confidence_bucket,latency_bucket)`. **This discrepancy is not silently 'resolved'**: P06 retains the already chosen P03 implementation without creating an unauthorized ninth event or unreviewed field; Product/Privacy must explicitly approve the final failure schema before live collection.
- **English ADR-0005, §10–12:** six bounded supported intents, no guessed/unsupported execution, no raw transcript, record only approved coarse intent/latency/outcome and explicitly observed manual correction. **ADR-0002** requires service-owned Media3 ACK, **ADR-0003/0004/0006** require nonblocking privacy control and local/offline functionality without accounts/backend.
- **Existing GitHub source:** Issue #10 `DefaultVoiceSessionCoordinator`, `VoicePlaybackDispatcher`, `VoiceTransitionPolicy`; Issue #11 `PlayArtistPlaybackUseCase` and one-step confirmation; Issue #12 [P02 metrics](ISSUE_12_METRIC_DICTIONARY.md), [P03 schema](ISSUE_12_EVENT_SCHEMA.md), [P04 consent](ISSUE_12_P04_DIAGNOSTICS_CONSENT_AND_PRIVACY_GATE.md), [P05 entry events](ISSUE_12_P05_DRIVING_VOICE_ENTRY_OWNERSHIP.md).

**Do not conflate:** (1) a user's Mic tap, (2) deterministic intent mapping, (3) a pending Media3 command, (4) a validated service ACK, (5) optional TTS feedback, or (6) a user-confirmed manual correction. They are separate states and cannot be substituted as numerator evidence.

## 2. Exactly four P06 approved event families — attribution table

| Family | Only authoritative production boundary | Conditions / negative evidence |
|---|---|---|
| `intent_detected` | Existing **single app-owned** `DefaultVoiceSessionCoordinator`, `noteAcceptedIntent`, after an admitted deterministic five-control mapping or accepted `PLAY_ARTIST` direct/one-step ambiguity; run under current generation lock | One detection per actual accepted attempt. Requires **finite calibrated confidence** to create the mandatory `confidence_bucket`. Moonshine local exact/VAD phrases have **null** calibrated score; omit this event and report an unknown-confidence gap, never invent High/Low or a transcript. Never record unknown/unsupported or rejected weak commands as detected. |
| `intent_failed` | One **real** `VoiceSessionState.Failed` transition, through `failAttempt` or `finishArtistConfirmationLocked`; original typed stable `AppErrorCodes` only | One failure per terminal **attempt**, including first failed attempt if an explicit retry is offered. Retry resets claims. It can have a null `intentType` for no recognized supported intent. Never emit from a canceled/invalidated generation, a random caught exception string, a mere ambiguous confirmation prompt, a stale artist response token, or an unclaimed callback. |
| `command_executed` | Existing `VoicePlaybackDispatcher` or `PlayArtistPlaybackUseCase` returns a **validated service ACK**, then `publishExecuted` accepts the **current** generation and moves to `VoiceSessionState.Executed` | Exactly one per truly completed attempt. A mapping, artist candidate, positive one-step choice, pending service command, TTS narration or late ACK can never produce success. No new Media3/service or STT execution path. `latency_bucket` comes from the existing monotonic attempt clock and is **not** PRD's speech-end-to-visible-response P95. |
| `manual_correction` | **Explicit two-step user evidence:** terminal Voice panel's **Correct using manual controls** action, then **same-origin, ACK-confirmed** existing manual playback action from `PlayerViewModel` before expiry | No ordinary Play/Pause/Next touch is automatically a correction. The ticket must be explicitly armed on current terminal Voice, holds only closed intent+origin in RAM for <=60 seconds; first permitted ACK consumes it. Failed commands, mismatched Player/Driving origin, stale navigation/permissions, multiple ACKs, background exit, new PTT or consent withdrawal produce no correction. No output from artist confirmation, Neither/cancel or generic manual fallback alone. |

**Approved six intents only:** `PLAY`, `PAUSE`, `NEXT`, `PREVIOUS`, `SEEK_BACK`, `PLAY_ARTIST`. No new intent, live transcript, personal artist name, MediaRef, queue, device ID, command ID, voice session ID or file URI is forwarded to Quality. The private Issue #11 ninth `VoiceIntentOutcome` remains typed and local-only, **rejected by P03's outbound filter**.

## 3. Event sequencing and strictly bounded concurrency

One successful explicit PTT start (P05) starts a private generation + recognition attempt. The P06 reducer resets `telemetryIntent`, `telemetryConfidence`, `telemetryDetectedClaimed`, `telemetryTerminalClaimed` when `beginAttemptLocked` begins. After the deterministic mapper accepts a supported intent, only the existing coordinator's gate may claim its detection. A `PLAY_ARTIST` phrase is not considered playback success: direct routing and one-step confirmation retain their existing resolver/index revision, token, timeout and service ACK gates.

**ACK:** For five transport commands, only `AppResult.Success` from the existing `VoicePlaybackDispatcher.execute` can precede `publishExecuted`. For sixth-intent direct and confirmation paths, only `PlayArtistDecision.Executed` backed by the existing `PlayArtistPlaybackUseCase.execute` ACK can precede `publishExecuted`. After a truly accepted `Executed` transition, the one-shot terminal claim prevents a second `command_executed`. `VoiceSessionState.Failed` emits at most one `intent_failed` for that attempt. A new explicit Retry is a **new attempt**, not a replay of previous success/failure.

**Failure:** The code in `intent_failed.failure_code` is a fixed, allowlisted `AppErrorCodes` value; unknown internal/provider errors first map to the coordinator's existing `VOICE_UNAVAILABLE` or `VOICE_ARTIST_UNAVAILABLE/PLAYBACK_FAILED` codes. Never copy exception messages. If recognition has no valid deterministic supported intent, `intentType=null` is serialized as `none` by the v1 privacy gate.

**Confidence missingness:** The existing local `MoonshineSileroSpeechToTextEngine` does **not** produce a calibrated score. The app may still safely execute a deterministic exact-VAD phrase according to Issue #10 policy, but cannot truthfully label the event `High` just because the grammar was exact. P06 intentionally drops `intent_detected` when the required bucket cannot be measured. An accepted ACK can still produce a valid `command_executed`, and a failure can still produce valid `intent_failed` with `confidence_bucket=none`. P10 must explicitly report mismatch/unknown confidence, not interpret missing `intent_detected` as a failed command or a zero denominator.

**Timing:** The P06 `latency_bucket` is derived from the inherited P11 `System.nanoTime()` attempt-start-to-ACK/failed clock (engineering bucket <=1000ms / <=2500ms / >2500ms). That is NOT the separately required PRD strict speech-end -> perceptible response/ACK P95 boundary; it remains future P10. Do not infer individual response times from coarse buckets.

## 4. Explicit manual-correction two-step protocol

1. Voice overlay displays a new optional, accessibility-native resource-backed **Correct using manual controls** button **only in terminal** `VoiceSessionState.Executed` or `Failed`, not while listening, processing or awaiting artist confirmation. No forced or automatic correction prompt.
2. The user clicks it; `AppNavHost` confirms the same valid Player/Driving Voice origin. `ApplicationBindings.armExplicitManualCorrection` requires **current P04 consent/approvals** and reads only a **terminal supported coarse intent** from `DefaultVoiceSessionCoordinator.acceptedTerminalIntentForCorrection`. Without known intent or approved consent, the user still returns to normal manual controls but no diagnostic ticket is created.
3. `ExplicitManualCorrectionOwner` stores **only** `VoiceOrigin`, `VoiceDiagnosticIntent`, monotonic expiry <=60 seconds, **not** a session ID or artist/track metadata. Voice is then dismissed before the user uses normal manual controls.
4. The *unchanged* Media3 Gateway controls remain authoritative: `PlayerViewModel.submit` has a new optional observation hook **after** `PlaybackGateway.execute(command)` actually returns `AppResult.Success` for the current UI request epoch. It emits only closed categories: `manual_play` from Play; `manual_pause` from Pause; `manual_skip` from Next/Previous; `manual_select` from SelectQueueIndex. Seek/Stop/ReplaceQueue don't invent a correction category. `manual_search` cannot be proven from this UI ACK seam yet and is **not emitted**; any future Search correction needs its own explicit verified source.
5. `AppNavHost` rechecks the latest visible origin/permission/route at ACK time and offers the enum to `ApplicationBindings.recordVerifiedManualCorrection`. The ticket claims once and emits `TelemetryEvent.ManualCorrection(previousIntent,correctionType)` only when both origin and deadline match. Generic non-armed manual playback actions do nothing.
6. Exit/background, new PTT, Driving re-entry or diagnostic consent withdrawal clears the ticket. Consent is rechecked at both enqueue and delivery; no diagnostics are enabled in production yet.

**Limitation explicitly preserved:** This is opt-in **self-identified** correction supported by a real manual ACK, not an automatic detector of all user corrections. It cannot establish a complete unbiased correction denominator; P10 must present opt-in coverage and unknown/unobserved manual corrections separately, and Product/UX must approve the additional affordance before Beta GO.

## 5. Quality dispatch, drop policy and application ownership

The only production composition root (`ApplicationBindings`) injects one P06 `(TelemetryEvent)->Unit` callback into the **existing** Voice owner via `VoiceBindings`. Its only action inside the Voice lock is: check P04 authorization and `Channel<TelemetryEvent>(capacity=64).trySend`. No suspension, filesystem, Media3, network or provider call happens on the Voice critical path. Exactly one application-scoped `Dispatchers.Default` consumer drains FIFO events through the **P03 allowlist** and **P04 authorization-aware** `BestEffortQualityEventPublisher` and `ConsentGatedTelemetrySink(NoOpTelemetrySink)`.

- Overflow means **DROP** (not blocking or retrying). P10 must add an allowed, privacy-safe dropped-event aggregate and quality/missingness classification if any real collection is approved, not silently treat observed count as complete.
- Revocation changes the authorization latch synchronously; any queued event is denied again at publisher/sink before it could be sent.
- There is **no installed real analytics provider**, no SDK, account, remote endpoint, Room database, file transport or uploader. Current P04 approval booleans are FALSE; so **no live output events are actually collected**. The event shape/source code is implemented but unverified.

## 6. Changed files and intentionally untouched systems

| Path | P06 source-only change |
|---|---|
| `voice/session/DefaultVoiceSessionCoordinator.kt` | P06 typed approved event callback; one intent / failure / service-ACK terminal claim per attempt; read-only terminal intent for correction |
| `app/di/VoiceBindings.kt` | Additive injection seam for approved P06 events, preserving existing ninth internal P11 callback |
| `app/di/ApplicationBindings.kt` | Privacy-aware bounded FIFO Quality dispatch, correction ticket owner, fresh consent checks, current origin ACK handling and revocation cleanup |
| `core/application/voice/ExplicitManualCorrectionOwner.kt` | New nonidentifying, RAM-only, explicit one-shot correction ticket with <=60s expiry |
| `feature/player/PlayerViewModel.kt` | After confirmed manual PlaybackGateway ACK only, optional typed correction category observation; playback/UI state logic unchanged |
| `app/navigation/AppNavHost.kt` | Current-visible-origin checks, explicit arming callbacks, ACK route validation and no automatic inference |
| `feature/voice/VoiceSessionPanel.kt` | Terminal-only optional native button to deliberately return to manual correction |
| `feature/player/FullPlayerScreen.kt`, `feature/driving/DrivingModeScreen.kt` | Pipe the same explicit Voice-overlay action through existing screens |
| `app/MainActivity.kt` | Bind optional P06 app-owner callbacks |
| `app/src/main/res/values/strings.xml` | Add only generic nonpersonal explicit-action label `i12_voice_correct_manually` |
| `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue12Phase6VoiceOutcomeEventsTest.kt` | Nine new JVM test scenarios **authored, NOT_RUN** |
| `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue12Phase6ManualCorrectionTest.kt` | Eleven correction/missingness/route scenarios **authored, NOT_RUN** |

**Intentionally unchanged:** Media3 service, `VoicePlaybackDispatcher` ACK validation, Room/MediaStore library, on-device Moonshine/Silero recognition, VAD/intent grammar/resolver thresholds, six-intent command semantics, Bluetooth/Audio Focus/route and OS microphone permission behavior, permanent diagnostic storage or external telemetry provider. Issue #11 ninth diagnostic is still internal and no new ninth outbound family exists.

## 7. QA source written; acceptance NOT verified

- [x] Source authored for direct command mapping, typed attempt failures, service ACK-only execution and explicit user-observed/ACK-backed correction.
- [x] One accepted detection/terminal claim per attempt, reset only on new explicit Retry; stale/canceled generations blocked by existing gate.
- [x] Unknown provider confidence not fabricated; raw audio, transcripts, private media and stable identifiers excluded from P06 telemetry.
- [x] Manual correction requires explicit user opt-in + verified Player/Driving origin + a real subsequent service ACK within one minute.
- [x] Two new source test files with **20 authored test methods**, including delayed/rejected ACK, canceled generation, unknown phrase, missing confidence, explicit correction, wrong origin and expiry.
- [ ] **No tests executed, compiled, linted, run on JVM/device, built as APK or triggered via GitHub Actions.** Static inspection alone is not a PASS.
- [ ] Human Product/Privacy/UX approval of manual correction affordance, missing confidence, intent_failed schema conflict, queue-loss budget and collection retention still required.
- [ ] P10/P11 must distinguish observed sample and unknown sample; P14 owner manual QA is the first authorized compilation/build/test phase.

**Phase result:** `SOURCE_IMPLEMENTED_UNCOMPILED_NOT_RUN`, not `PASS`. All 14 Issue #12 AC remain NOT_VERIFIED. No PR, merge, issue close or production provider enabled. Inherited voice-quality follow-ups #80 and #82 remain open.

**Next planned phase:** P07 Player Error and Crash Reporting, with privacy-first/no-raw-exception boundaries.
