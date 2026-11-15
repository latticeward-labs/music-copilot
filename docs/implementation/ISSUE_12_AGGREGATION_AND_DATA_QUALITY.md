# Issue #12 — P10 Exact Measurement Projection, Endpoints and Data Quality

**Date:** 2026-11-15  
**Branch:** `feature/12-privacy-safe-telemetry-measurement`  
**Phase result:** `SOURCE_IMPLEMENTED_UNCOMPILED_NOT_RUN`; code and QA fixtures only, no Gradle, Kotlin build, tests, APK, CI or device evidence.  
**Primary file:** User-uploaded `MSM2(1)(1).rar`, SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`. The archive's English PRD/MVP/Architecture/Technical Blueprint/ADRs were opened directly. This document separates their stated requirements from this phase's engineering implementation.

## 1. Authoritative requirements — not negotiable

- **Original English PRD p.9**: local command **P95 strictly <1 second**; Voice response **P95 strictly <2.5 seconds after END OF SPEECH**; at least 99% crash-free journeys; **fewer than one critical Player error per 100 sessions**.
- **Original English PRD p.10**: North Star successful Driving+Voice session for **>=50% of weekly users**; direct-command correctness **>=85%**; post-command required manual correction **<15%**; incorrect command execution **<3%**; weekly return **>=30%**; Driving reuse **>=50%**. Eight approved event families, privacy minimization, user deletion and no default raw voice processing.
- **Original English MVP p.2, p.9, p.10**: Direct success >=85%; driving reuse >=50%; Voice P95 <2.5s; crash-free >=99%; closed Beta **50 qualified drivers, 14 days, 3+ vehicles/head-unit models**, with informed non-audio diagnostics consent. Beta decision indicators: Driving reuse green >=50%, yellow 30–49%; direct-command green >=85%, yellow 70–84%; correction green <15%, yellow 15–25%; crash-free green >=99%, yellow 97–98.9%. Descriptive rounded bands do NOT change exact numerical PRD target comparisons.
- **Original System Architecture pp.17,19 and Technical Blueprint pp.39,48,52**: eight non-identifying typed telemetry families, service-authoritative ACK, no backend/account/streaming, Quality non-authoritative, short Voice processing, measurable outcomes. The shorter English MVP event table lists seven rows, omitting `intent_failed`; P03 reconciled this using the PRD/Architecture eight-family contract.
- **Original ADR-0002/0003/0004/0005/0006**: one Media3 player and ACK authority, local-first app data, independent diagnostics consent/retention/delete, no raw transcription, deterministic six intents, no app account/backend/streaming.
- **GitHub P02 [frozen metric dictionary](ISSUE_12_METRIC_DICTIONARY.md)** defines QA ground truth, denominators, unknown reconciliation, exact nearest-rank P95, anchored W1/W2 UTC, and cross-cohort non-substitution. P03 through P09 have source-implemented privacy gates and owners, but **none has been tested or approved for real collection**.

**Source-vs-engineering policy:** Exact numerical thresholds above are source-locked. Engineering labels, monotonic timing hooks, bucket cutoffs, QA input data structures and unverified-proxy classifications are implementation interpretations that require Product/Voice/Privacy/QA signoff. No synthetic tests represent actual Beta performance.

## 2. Exact QA-cohort metric formulas and fail-closed verdicts

[QualityMetricProjection.kt](../../app/src/main/java/com/latticeward/musiccopilot/quality/measurement/QualityMetricProjection.kt) is a pure Kotlin module; it has **no Android, Media3, file, network, user-identifying or transcript dependency**. It accepts only independently verified eligible aggregate YES/NO/UNKNOWN counts and a labelled source (`QA_VERIFIED_AGGREGATE`, `CONTROLLED_BETA_VERIFIED_AGGREGATE`, `OBSERVED_EVENTS_ONLY`, `SYNTHETIC_FIXTURE`, `NOT_PROVIDED`). It cannot manufacture an enrolled participant, a session ID or a P95 sample.

| ID | Numerator / denominator | Locked limit | Required independent evidence |
|---|---|---|---|
| **NS01** | Distinct eligible weekly active people with >=1 **truly correct, ACKed** Voice Driving session / **all eligible weekly active people** | **>=50%** | Actual approved participant-week evidence; no session count substitution |
| **Q01** | Correctly executed QA-labelled direct Voice attempts without necessary correction / all eligible direct attempts including ASR failures, explicit Retries and wrong actions | **>=85%** | Ground-truth intended command + service result; `command_executed` alone insufficient |
| **F01** | Explicitly verified corrections attributable to attempts / all eligible evaluated Voice attempts | **<15%** | Confirmed attribution, non-observed corrections UNKNOWN, not automatically FALSE |
| **G01** | Independently verified *incorrect* actions / all eligible attempts | **<3%** | Expected-vs-actual QA labels; a correctly ACKed wrong song is still wrong |
| **R01** | Verified W1-active people also active in W2 / all eligible W1-active people | **>=30%** | Paired approved person-week aggregates, no exported person/device key |
| **R02** | Actual enrolled qualified participants with >=2 verified Driving entries / all eligible enrolled qualified participants | **>=50%** | Enrolled-cohort denominator, not weekly active or session count |
| **S01** | Verified crash-free Driving journeys / **all eligible started Driving journeys**, including known crashes+UNKNOWN | **>=99%** | Corroborated fatal/serious Player crash evidence and full start denominator |
| **S02** | Verified **critical** distinct Player incidents ×100 / all eligible started Driving sessions | **<1 incident per 100 starts** | Playback/QA-approved code severity and incident dedup, not the percentage of sessions affected |
| **P01** | Nearest-rank P95 of actual speech-end→first perceivable terminal response | **strict <2500ms** | Verified acoustic speech end + physical UI/audio response boundary, complete sample frame |
| **P02** | Nearest-rank P95 of actual admitted manual control→verified Media3 ACK | **strict <1000ms** | Complete admitted manual/foreground control sample frame, failures/missing explicitly accounted |

**EvidenceCounts** requires nonnegative `verifiedTrue`, `verifiedFalse` and `unknown`, with `D = true+false+unknown`, never discarding unobserved attempts. `D=0` => `NOT_MEASURABLE`; otherwise if any UNKNOWN, missing source queue events, evictions, uncontacted Beta participants, unresolved trial labels, missing approval, or source not independently verified => `NOT_VERIFIED` (NOT PASS and NOT automatic FAIL). With complete, independently validated counts, compare unrounded percent with the exact source-locked boundary.

For quality-review transparency, a ratio exposes the conservative observed lower percentage `100×true/D` and the mathematical optimistic upper `100×(true+unknown)/D`. Neither optimistic bound is a PASS verdict. The `S02` metric may have **more critical incidents than started sessions**; its unit is incidents per 100 started sessions. Critical-error severity mapping still lacks signed-off Playback/QA code classification; no observed P07 PlayerError automatically becomes a critical incident.

**No unexpected zero:** No `manual_correction` event cannot prove `F01=0%`; no `intent_failed` event cannot prove `Q01=100%`; no process-exit report cannot prove `S01=100%`. The P07 typed Player error stream has conservative episode dedup and may undercount repeated identical incidents. P08 RAM-only Driving tickets may disappear after process kill. P09 ring cap/queue can drop observations. These are **unknown coverage**, not just empty event arrays.

## 3. P01: true speech-end P95 versus measured signal proxy — critical distinction

The source's wording **after the end of speech** is stricter than Issue #11's inherited `System.nanoTime()` **STT-attempt-start→terminal** `latency_bucket`. The inherited Fast/Expected/Slow buckets cannot establish official Voice P95; `Expected` includes exactly 2500ms, which **fails** the PRD strict threshold. P10 intentionally does not repurpose or rename P03's `latency_bucket`.

**Actual source-backed endpoint hooks written in P10:**
1. `SpeechRecognitionRequest.onEndpointSignal` is optional and carries only an in-memory monotonic `SpeechEndpointEvidence(elapsedRealtimeNanos, quality)`. No raw sound sample, transcript, file, token, user key or per-event timestamp is encoded/stored.
2. The production local `MoonshineSileroSpeechToTextEngine` emits `VAD_DETECTED` on the real nonempty Silero segment. **Silero's configured `minSilenceDuration=0.55f` proves this signal is DELAYED relative to physical end-of-speech by design**; it is NOT an independent sample-level speech end marker. A flushed/timeout segment without this signal is UNKNOWN, not a fabricated marker.
3. The alternative `AndroidOnDeviceSpeechToTextEngine` emits `ANDROID_RECOGNIZER_DETECTED` from its real `RecognitionListener.onEndOfSpeech`, after checking current session ownership and live continuation. OEM end callbacks can be delayed/uncalibrated; not automatically a physically verified last sample.
4. `DefaultVoiceSessionCoordinator` accepts a marker **only for the active session, attempt and coroutine generation**. Retry and cancel reset the in-memory marker; late STT callbacks cannot contaminate the next attempt. No use of start-of-listening as fallback.
5. The existing `VoiceSessionPanel` measures only a final `VoiceSessionState.Executed` or `Failed` at its **first heading draw** via `Modifier.drawWithContent`. It must have actually rendered a terminal label; it does not count Processing/Listening/Confirming, service ACK alone or TTS scheduling. The current route, visible Player/Driving origin, microphone/library permissions and coordinator's current matching state are rechecked in `AppNavHost`, and the Coordinator claims that first draw exactly once. **A Compose drawn heading is a UI rendering observation, not hardware-certified photons or audible TTS onset.**
6. Both marker and first draw use Android `SystemClock.elapsedRealtimeNanos()` on production, then a transient positive difference is reduced to fixed aggregate buckets. `LocalLatencyHistogram` retains only **under1000, 1000–1999, 2000–2499, >=2500, UNKNOWN**. Missing/negative/mismatched clocks and no endpoint are UNKNOWN. Per-command nanoseconds/milliseconds are not persisted, transmitted or exported.
7. **Provenance is `ENDPOINT_SIGNAL_TO_TERMINAL_DRAW_PROXY` only.** This histogram **DOES NOT qualify as certified `P01` PRD P95**, regardless of apparent speed. Real GO requires calibrated acoustic end markers (or independently annotated QA) and a device/voice-owner verified first visible/audible terminal response, including failures/timeouts/cancellation reconciliation. Once validated, only approved binned counts may enter `StrictP95Boundary` with a verified source and complete denominator.

**Strict nearest-rank implementation:** `rank = ceil(0.95×n)` with overflow-safe integral calculation. For a fully verified histogram divided at exactly 2500ms, `rank <= count(duration < 2500ms)` means P95 passes; otherwise fails. At **2500ms**, FAIL. The histogram can evaluate this boundary without writing individual raw timings. Exactly zero measured -> NOT_MEASURABLE. Non-verified proxy input is NOT_VERIFIED. Every missing/failed/cancelled attempt must be classified as eligible/unknown/not applicable in the future QA frame rather than silently excluded for speed.

## 4. P02: admitted manual control → actual Media3 ACK

Existing `PlayerViewModel.submit` is the single admitted UI-tap→service `PlaybackGateway.execute` path for visible Full Player/Driving manual controls. P10 captures a monotonic `System.nanoTime()` **after the command is admitted** and, **only when the result is AppResult.Success on the current request epoch**, computes transient milliseconds and forwards it through the route/permission-checked `AppNavHost` callback to `ApplicationBindings`'s fixed on-device `LocalManualAckHistogram`.

- Failed Gateway result/throw => **UNKNOWN**, never 0ms or 100% success.
- Stale request epochs/other screens are not attributed to the visible local sample.
- Existing typed Media3 result remains authoritative; no new player, ACK handler or command semantics.
- `LocalManualAckHistogram` tracks **<1000ms, >=1000ms, UNKNOWN**, with clear on user diagnostic clear and revoke. **Exactly 1000ms fails**.
- Source coverage is **only admitted visible UI controls** (plus existing Driving route forwarding via the shared PlayerViewModel); not the complete notification, headset, OS media controls or provider event stream. That limitation prevents global P02 P95 GO until QA independently establishes completeness.
- Physical-device performance and the precise definition of accepted local command input remain Product/Playback/QA-review items.

## 5. Real Beta population and time basis, NOT invented

[ControlledBetaCohortProjection.kt](../../app/src/main/java/com/latticeward/musiccopilot/quality/measurement/ControlledBetaCohortProjection.kt) implements **one real owner-supplied T0 (UTC)**; `W1=[T0,T0+7d)`; `W2=[T0+7d,T0+14d)`; observations at exact boundaries belong to only one anchored week. **No T0, 50-person enrollment or observed outcomes have been filled in by this phase**.

One `ControlledBetaCohortEvidence` takes **already-reconciled, anonymous aggregate YES/NO/UNKNOWN** counts for NS01 W1, NS01 W2, W1→W2 return and 14-day Driving reuse, with actual enrolled headcount, qualified vehicle/head-unit count and days observed. It requires meaningful denominator bounds (e.g., users returned in W2 cannot exceed actual enrolled, and Driving reuse uses enrolled denominator, not W1 actives). It marks the 50/14-day/3+ models protocol **ready only when separately verified source and complete coverage exist**, without automatically declaring release GO. A synthetic fixture is permanently NOT_VERIFIED.

**Operational limitation:** eight v1 event families intentionally export no persistent session/user/installation identity; P09 retains no durable participant-week boolean or 14-day history; remote processor is OFF. These events alone cannot de-duplicate weekly users or join W1 to W2 after process death. The Product/Privacy/Legal-reviewed closed-Beta-owner survey/consented aggregate intake must supply population truth, or P10 correctly returns NOT_MEASURABLE/NOT_VERIFIED. No hidden per-device ID, contacts, Bluetooth address or external telemetry backend is introduced.

## 6. P09 quality completeness and privacy wiring

`QualityMetricProjection.observedLocalOnly()` accepts the P09 `LocalDiagnosticsSummary` only and returns eight-family event counts, bounded queue drop and oldest-ring eviction counts as **OBSERVED_EVENTS_ONLY / NOT_VERIFIED**; a null store/consent denial returns NOT_MEASURABLE. The app-scoped P10 `LocalMeasurementSnapshot` contains this observed evidence, the **uncertified Voice endpoint→draw proxy**, and the **visible manual→ACK local latency sample histogram**, with official P95 result fields defaulted to NOT_VERIFIED.

- Both histograms and P09 event counts are **RAM only** and cleared under the same short erase lock on `ApplicationBindings.clearLocalDiagnostics()`, `revokeDiagnosticsConsent()`, consent-observer denial, or graph close. No timer, transcript or user ID is saved in DataStore/Room, OS backup or a file.
- The existing P04 four production approvals (reviewed privacy notice, approved retention, approved adapter and Legal) remain **FALSE**, and `QualityBindings.PRODUCTION_LOCAL_DIAGNOSTICS_MODE` remains **NoOp** for both debug/release. P10 adds **zero** automatic diagnostic collection, SDK, network upload, crash export or background polling to current builds.
- Error/permission/route/voice cancellations still retain their original behavioral owners. P10 observations are best-effort and cannot start STT, replay a command, pause audio or claim crash-free performance.

## 7. QA artifacts (source-authored only) and release stop-conditions

| File | Test source authored, NO execution |
|---|---|
| `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue12Phase10MetricProjectionTest.kt` | **23**: strict 85/15/3/99 bounds, exact 2500/1000, nearest-rank, missing denominators, synthetic/observed provenance, critical errors per 100 |
| `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue12Phase10MeasurementWiringTest.kt` | **16**: provider VAD/OEM endpoint proof, current-generation ownership, first drawn terminal UI, Player/Driving origins, ACK path, clear/revoke |
| `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue12Phase10ControlledBetaCohortTest.kt` | **9**: 14-day half-open anchored UTC weeks, distinct populations, verified-source gating and no fake enrollment |
| **Total** | **48 test methods AUTHORED / 0 EXECUTED**. No compilation or build was requested/used. |

**Still OPEN and NOT VERIFIED:**
- Final acoustic speech-end calibration, actual first *perceptible* terminal response and error/timeout sample coverage. The current VAD/OEM endpoint-to-drawn-text path is a PROXY; it is **not** the locked Voice P95.
- Complete accepted local-command source coverage beyond visible UI, and independent QA/Playback severity mapping for S02.
- Intended-versus-actual command labels for Q01/G01 and unbiased correction labels for F01; Issue #11 artist ambiguity quality follow-up #82 remains.
- Local participant-week de-duplication/provenance with lawful 14-day data window, actual Beta T0, 50 consenting qualified drivers and 3+ head units; no user-specific identifier exported.
- Crash-free session reconciliation across Android process death; P07 exit category and P08 process-local session cannot reconstruct precise per-journey outcome. UNKNOWN remains in denominator.
- Known inherited lab blocker Issue #80: **24/50=48%** owner-labelled Issue #10 controlled-lab observations versus >=90% lab gate. **This is a known past lab finding, NOT a new P10 test, and is not silently passed.**
- P11 dashboard/report input format, P12 Settings/Privacy disclosure+clear, P13 test source verification, P14 authorized APK build and owner phone validation.
- No Product/Privacy/Legal/Voice/Playback/QA acceptance signoffs, no tested runtime evidence, **all 14 Issue #12 acceptance criteria remain NOT_VERIFIED**. No PR, merge, issue close or Beta GO.

**Phase exit:** P10 implementation and explicit **NOT_MEASURABLE / NOT_VERIFIED when evidence missing** are authored. The source does NOT yet establish that production can collect sufficient true ground truth for the three headline Beta questions. Thus the phase is recorded `SOURCE_IMPLEMENTED_UNCOMPILED_NOT_RUN`, not PASS. Next is **P11 weekly dashboard/report** using only approved aggregates and these same declared missingness rules.
