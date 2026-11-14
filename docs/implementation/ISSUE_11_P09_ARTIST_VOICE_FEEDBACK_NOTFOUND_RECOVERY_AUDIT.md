# Issue #11 — P09: English Voice Feedback, NotFound, Recovery and ACK Safety

**Date:** 2026-11-14  
**Branch:** `feature/11-play-artist-one-step-ambiguity`  
**Disposition:** `SOURCE_IMPLEMENTED_UNCOMPILED_NOT_RUN`. This is an authored-source audit, **not** successful build, test, TTS playback or device evidence.

## 1. Controlling sources (inspected)

The **exact uploaded** `MSM2(1)(1).rar` (RAR5, SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`) was opened and its twelve English PDF documents listed and read where relevant. References:

- **ADR-0004, Replaceable Voice Provider**, pp. 2–5: at most one short sentence per TTS response, replaceable Android/offline availability, one recognition retry, no raw-audio history, no provider ownership of queue, TTS failure leaves playback/manual controls intact.
- **ADR-0005, Restricted Deterministic Intent Mapping**, pp. 2–5: exact six English intents, no guessing, bounded artist ambiguity and one confirmation, no matched artist -> safe Unknown/NotFound, one short result without another command.
- **Music Copilot PRD v1 EN Clarified**, p. 6: example distinct responses for Playing, “Did you mean ...?”, and no local match (“I couldn't find that in your library”), plus safe manual recovery.
- **Music Copilot MVP v1 EN**, pp. 4–5: US-06/US-07, short TTS, confirmation and NotFound; state machine Idle -> Listening -> Processing -> Confirming -> Executed/Failed.
- **Music Copilot System Architecture MVP v1 EN**, pp. 10–12: TTS optional; its failure cannot block a successful command; unknown/unsupported preserves current Player.
- Actual GitHub [Issue #11](https://github.com/latticeward-labs/music-copilot/issues/11), `docs/implementation/ISSUE_11_PHASED_EXECUTION_PLAN_FA.md` **P09**, current P07 Coordinator, P08 shared panel, P03/P04/P05/P06 domain and service acknowledgements, existing Issue #10 TTS contracts and feedback provider.

**Implementation choice:** To avoid exposing personal local metadata through feedback/audio, artist text is **generic and constant** (“the selected artist”) even though the historical PRD gives examples containing an artist's name. The actual selected artist remains visible only in the local P08 confirmation UI. We do not silently claim the PRD mandates this generic wording; it is the P09 privacy-preserving source design. The physical audibility and appropriateness of copy must be reviewed in P13.

## 2. Modified production source

| File | P09 changes |
|---|---|
| `app/src/main/java/com/latticeward/musiccopilot/voice/feedback/VoiceFeedbackCatalog.kt` | Add 10 dedicated `VoiceFeedbackKey` values for ArtistPlaying, ArtistConfirmOne/Two, NotFound, Unsupported, Declined, Canceled, Stale, Unavailable, PlaybackFailure. Each text is deterministic, constant, privacy-safe and at most one short sentence; support **one question mark as the final punctuation** for spoken clarification, while rejecting multiple clauses. Stable `i11_feedback_artist_*` SpeechOutput keys. Prior five control texts/keys remain unchanged |
| `app/src/main/java/com/latticeward/musiccopilot/voice/android/AndroidVoiceFeedbackCatalog.kt` | Android English resources mapped 1:1 to every new key; existing provider and offline-only voice behavior unchanged |
| `app/src/main/java/com/latticeward/musiccopilot/core/result/AppError.kt` | Six typed sanitized error codes `VOICE_ARTIST_NOT_FOUND`, `_UNSUPPORTED`, `_DECLINED`, `_STALE`, `_UNAVAILABLE`, `_PLAYBACK_FAILED`. They carry **no artist name, URI, token or transcript** |
| `app/src/main/java/com/latticeward/musiccopilot/voice/session/DefaultVoiceSessionCoordinator.kt` | Wire P09 to P07: map P05/P06 distinct results to typed terminal voice failures, `Recoverability.TERMINAL_FOR_ATTEMPT` and `RecoveryAction.ManualFallback`, without changing five-control retries. Queue optional bounded English TTS **after** P07 Confirming (no blocking options). One prompt Job per generation is canceled on selection, decline, timeout and lifecycle. On **both direct and confirmed artist playback**, publish `ArtistPlaying` only after `P06` reports `PlayArtistDecision.Executed` and `publishExecuted` succeeds. No TTS failure can reverse a service ACK |
| `app/src/main/java/com/latticeward/musiccopilot/core/application/voice/VoiceOverlayPresentationPolicy.kt` | Map typed artist failures to distinct shared `VoiceOverlayStatus` values for Player/Driving, `showRetry=false` with manual fallback |
| `app/src/main/java/com/latticeward/musiccopilot/feature/voice/VoiceSessionPanel.kt` | Show resource-backed short English NotFound/Unsupported/Declined/Stale/Unavailable/PlaybackFailure statuses and one manual fallback explanation. Same existing Player/Driving panel, no new navigation or player |
| `app/src/main/res/values/strings.xml` | 10 constant short spoken messages + seven generic artist failure/manual-fallback display strings; distinct from eight P08 confirmation option and TalkBack resource IDs |

**Existing issue #10 source-author fixture:** `Issue10Phase8FeedbackTest.kt` was edited **only** to accept a single spoken question ending in `?` as a one-sentence response, without treating multiple questions or an exclamation as valid. It was **not executed**.

## 3. Safe speech and recovery matrix

| Source outcome | UI | Spoken key, if permitted/available | Media3 effect |
|---|---|---|---|
| Clear high-confidence local artist + P06 valid ACK | Executed | `ArtistPlaying`, only **after** service ACK | Exactly the P06 authorized ReplaceQueue |
| Approved ambiguous local artist, 1 candidate | Confirming (one P08 option) | `ArtistConfirmOne`: one short question, never claims success | **None** until positive choice |
| Approved ambiguous local artist, 2 candidates | Confirming (two P08 options) | `ArtistConfirmTwo`: one short question | **None** until positive choice |
| Selected option + P06 valid ACK | Executed | `ArtistPlaying`, only after P06 ACK | One P06 authorized ReplaceQueue |
| No plausible local artist | Failed, ArtistNotFound, manual | `ArtistNotFound` | **None**; current playback preserved |
| Unsupported artist request | Failed, ArtistUnsupported, manual | `ArtistUnsupported` | **None** |
| Neither (matching P07 token) | Failed, ArtistDeclined, manual | `ArtistDeclined` is available in catalog; **immediate terminal path intentionally stops active prompt and stays silent** | **None**, no second question |
| Cancel/Back (matching P07 token) | Idle; previous manual controls | `ArtistCanceled` exists in catalog; intentionally **not automatically spoken after cancellation** to avoid late audio | **None** |
| Pending library revision changed or permission lost | Failed, ArtistStale, manual | `ArtistStale` on processing failure; synchronous selection rejection is intentionally silent | **None** before dispatch |
| Resolver, index or provider unavailable | Failed, ArtistUnavailable, manual | `ArtistUnavailable` | **None** |
| P06 service refusal, bad ACK, no playable track | Failed, ArtistPlaybackFailure, manual | `ArtistPlaybackFailure` (never “Playing”) | Previous command may have reached service; never claim completion or retry automatically |
| Android TTS absent/errors/hangs | Existing Exec/Failed/Confirming state | Silent, bounded `MAX_SPEAK_MS=2000` | **Never** blocks/undoes Media3 or manual controls |
| Existing Issue #10 five direct commands | Existing Executed/Failed | Existing messages, unchanged | Existing dispatcher, unchanged |

**Nuance:** one spoken clarification question and one spoken acknowledgement are **separate interaction responses**, each bounded to one sentence; selection interrupts and cancels a pending question so they do not intentionally overlap. No “Playing” message is emitted at the initial recognition/ambiguity stage. P10 must verify any platform callback races.

## 4. Failure semantics, privacy, and lifecycle

- **No fabricated positive result:** P05 `Direct` and P08 `Select` are not ACKs. Only `PlayArtistDecision.Executed` following P06 service-owned ACK can call `publishExecuted` and subsequently claim the ArtistPlaying sentence. The AudioService independently validates MediaStore refs and route/focus. Buffering ACK does **not prove audible music**.
- **No new backend/provider:** reuse `AndroidOfflineTextToSpeechEngine`, `TextToSpeechEngine`, and `VoiceFeedbackCatalog` interfaces, local bounded English phrases; no network/Cloud TTS, new recognizer, wake word, second player or permission.
- **No UI lookup:** Player and Driving still display typed `VoiceSessionState`; the panel never queries media, artist index or tokens.
- **No private metadata in feedback:** no transcript, artist display name, song title, local ref, token, recording or path is formatted into the spoken text, resource key, error code or telemetry.
- **Typed recovery:** `AppError.Voice(code, TERMINAL_FOR_ATTEMPT, ManualFallback)` for artist terminal outcomes. `NotFound`, `Declined`, `Stale`, `Unsupported`, `Unavailable`, and `PlaybackFailure` cannot auto-retry microphone or choose a guessed candidate. The existing five Issue #10 commands still retain at most one recognition retry when allowed.
- **Cancellation:** P07 invalidation cancels current generation, short prompt worker and session TTS. On `Neither`/expiry, stop optional prompt and return manual; on positive Select, stop prompt first and reserve success voice for ACK. A canceled/late ACK cannot publish a new success to the old generation. Once a Media3 service command is already accepted it cannot be retroactively unsent by a late UI cancellation — P10/P13 must validate actual behavior.

## 5. Source-authored QA (no execution)

New synthetic authored fixtures:

- `Issue11Phase9ArtistFeedbackAndRecoveryTest.kt`: **32** pure JUnit cases for one English sentence/one question mark, constant keys/copy, NotFound/Unsupported/Declined/Stale/Unavailable/PlaybackFailure mapping, no raw metadata, UI manual fallback, and source ACK gate.
- `Issue11Phase9ArtistTtsCoordinatorIntegrationTest.kt`: **9** fake STT/Room/P06 service/TTS cases for null-confidence clarification, unavailable TTS, pending ACK, confirmed ACK, missing artist, bad ACK, TTS error, Neither with hanging spoken question, and unsupported remote artist phrase.
- `Issue10Phase8FeedbackTest.kt`: **existing suite** adapted only in authoring for the one-question rule. **Not run, not counted as new P09 tests**.

**Total NEW P09 methods: 41; executed: 0; passed: 0; failed: 0.** No Gradle, Kotlin/JVM compilation, lint, Android tests, Compose instrumentation, GitHub Actions, real TTS, emulator, APK or device tests. No real p95 latency or STT accuracy claim. All Issue #11 AC remain unverified.

Original Issue #11 P13 suites M01–M48, T01–T10 and L01–L06 remain `NOT_RUN`. [Issue #80](https://github.com/latticeward-labs/music-copilot/issues/80) P0 speech-recognition quality remains OPEN; previous owner-reported Issue #10 lab was 24/50 (48%) versus 90% required. P09 does not fix the STT model.

## 6. P10–P13 handoff

- **P10:** real Activity stop/call/focus/Bluetooth permission invalidation during optional prompt, confirmation tap, async TTS and late service ACK; exact pending speech stop and route safety; existing service remains authoritative.
- **P11:** typed privacy-safe telemetry allowlist, assert no private feedback/session metadata logging or persistence.
- **P12:** final source wiring, acceptance traceability and test-ready QA matrix, **no builds or tests**.
- **P13:** **first/only cumulative APK build** permitted, owner performs Android manual P13 checks, assistant records the owner's actual results before any merge or issue closure.

**Disposition:** P09 source committed, NOT_BUILT / NOT_TESTED; Issue #11 stays OPEN, feature branch unmerged.
