# Issue #11 — P07: Real Voice Coordinator Confirming, Token Ownership and Exactly-Once Admission

**Date:** 2026-11-14  
**Repo:** latticeward-labs/music-copilot  
**Branch:** feature/11-play-artist-one-step-ambiguity  
**Status:** SOURCE_IMPLEMENTED_UNVERIFIED, **NOT_BUILT**, **NOT_RUN**. P07 is a source implementation, not a passed functional test. No APK, Gradle, Kotlin/JVM/Android compilation, lint, CI, GitHub Actions, emulator or device testing occurred. No Issue close or merge.

## 1. Source basis

Verified the **exact attached** MSM2(1)(1).rar file, SHA-256 0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8; inspected its 12 PDFs. P07-specific authorities:
- **ADR-0004 — Replaceable Voice Provider**, §§3–7: push-to-talk only, ≤6 seconds STT, at most one recognition retry, temporary duck while listening, one short response, no raw-voice storage and a manual fallback.
- **ADR-0005 — Restricted Deterministic Intent Mapping**, §§3–11: precisely six supported intents, direct only after real provider confidence and deterministic match, one-step confirmation for ambiguity, no guessing or a second clarifying question, no unknown/unsupported execution.
- **Music Copilot MVP v1 EN**, Voice Contract and State Machine: Idle → Listening → Processing → Confirming → Executed/Failed; bounded choice; short feedback.
- **Technical Blueprint** Voice & Intent and system architecture: one app-scoped VoiceSessionCoordinator, separate provider/mapper/resolver, sole Media3 service-owned player.
- The approved GitHub Issue #11 and Phase Plan P07, actual P01–P06 Kotlin types, P03 local LibraryRepository, and Issue #10 existing state/lifecycle semantics.

The **10,000ms** UI confirmation window is an explicit **new, versioned P07 implementation choice**, not a fixed numeric timeout stated verbatim by ADR-0005. It must be checked in physical P13 QA and can be adjusted by reviewed product policy.

## 2. Source modifications on the feature branch

| File | P07 change |
|---|---|
| app/src/main/java/com/latticeward/musiccopilot/voice/session/DefaultVoiceSessionCoordinator.kt | P02 phrase admission; P04 resolver; P05 confidence gate; P06 playback use case; coordinator-owned immutable 1–2 option Confirmation snapshot; random token/session/generation/revision binding; strict single-use selection; Neither/Cancel and expiry; ACK-driven terminal state; recognition/duck release while awaiting confirmation |
| app/src/main/java/com/latticeward/musiccopilot/voice/api/VoiceSessionCoordinator.kt | Add additive, token-scoped neither(token)/cancelConfirmation(token) interface methods with fail-closed default implementations for existing test adapters; redacted Confirming and option diagnostics; contiguous option indices |
| app/src/main/java/com/latticeward/musiccopilot/voice/session/VoiceTransitionPolicy.kt | Explicit Processing → Confirming and Confirming → one claimed action admission rules; prior retry/transport rules unchanged |
| app/src/main/java/com/latticeward/musiccopilot/app/di/VoiceBindings.kt | Pass the actual local LibraryRepository and existing P05 BuildConfig-calibrated ArtistDecisionPolicy to the single application coordinator; no synthetic confidence or second voice owner |
| app/src/main/java/com/latticeward/musiccopilot/app/di/ApplicationBindings.kt | Inject the existing application-scoped libraryRepository; retain existing canBeginSession foreground guard and Media3 controller |
| app/src/test/java/com/latticeward/musiccopilot/foundation/Issue11Phase7ArtistCoordinatorConfirmationTest.kt | **26 source-authored synthetic JUnit cases, 0 executed** |

**No changes:** Issue #10 five control grammar/mapper/transport dispatcher, P03 Room tables/SQL, P04 ranking, P05 confidence formula, P06 MediaStore/ACK use case, Media3 PlaybackGateway/Service/Player, STT/Moonshine provider, UI/Driving Mode screens, cloud/back-end and analytics.

## 3. Deterministic P07 pipeline

1. A **fresh explicit Mic** creates one app-scoped VoiceSessionId and one cancellable generation after foreground gate; duck ACK before <=6s STT. The five existing control phrases still go through **unchanged** RestrictedVoiceIntentMapper; no PLAY_ARTIST rewrite of the five-control dictionary.
2. A strictly approved P02 phrase, e.g. "Play songs by [artist]", follows **P04 ArtistResolver** over the P03 real Room artist group and P05 ArtistDecisionPolicy over actual nullable provider confidence. Unsupported, unknown, malformed, missing index and no match never fabricate a player command.
3. **P05 Direct**: the existing coordinator's dispatchClaimed is atomically set while Processing; one P06 claim, original library revision and process-unique service command ID; pass a **live generation/processing** predicate into P06; publish Executed only after P06 receives an ACK-backed valid snapshot.
4. **P05 Ambiguous (1–2 real candidates)**: while still Processing, check that the same attempt is owned, not claimed, Activity foreground remains, and local index is still Ready at P05's original revision. Coordinator creates one random in-memory VoiceCandidateToken and an immutable PlayArtistConfirmationSnapshot containing original artist identities, options 0/1, session/origin, revision and attempt generation. Publish VoiceSessionState.Confirming with a separate **unmodifiable display-only** option list. No queue action yet.
5. **Recognition completion**: release the service's temporary voice duck lease and stop recognition handling; retain only the current session-generation lease and the one bounded confirmation timer. The Mic is not reopened and no arbitrary follow-on utterance is interpreted as a reply. App/Activity recreation never restores this ephemeral pending snapshot.
6. **Valid confirm(token, optionIndex)**: while the same generation is active, compare with the CURRENT pending token and UI state, verify option index, foreground predicate and unchanged Ready library revision **synchronously under the coordinator gate**. Mint exactly one P06 claim from the privately owned snapshot, clear pending, cancel expiry, set dispatchClaimed **before launching any suspension**, transition Confirming → Processing, then call P06 with a live state-generation predicate. Duplicate taps and stale tokens fail before dispatch.
7. **P06 confirmed service ACK**: only a successfully correlated ACK-backed service queue with coherent snapshot becomes VoiceSessionState.Executed. Failure/timeout/stale/cancel cannot publish an optimistic execution, and P06 already re-queries all selected tracks from live MediaStore. One accepted UI selection is **not** an ACK.
8. **Neither(token)** is one terminal negative response: Failed with canRetry=false; no second clarification/STT run and no service queue mutation. **cancelConfirmation(token)** returns Idle with zero service dispatch. A wrong/old token never consumes the current one. A matching token with invalid index is a consumed bad one-step response → Failed/manual, not a second chance to pick.
9. **Confirmation timer**: after 10,000ms without a valid response, publish terminal Failed(VOICE_TIMEOUT, canRetry=false), cancel generation and drop pending token. No automatic listen/second question or old token restoration.
10. **Lifecycle/route/permission**: app foreground stop, navigation, permission revocation, call/focus or BT route invalidation call existing synchronous coordinator.cancel(reason), discarding the single pending generation/claim before a delayed callback may dispatch; P06 and the existing service each independently recheck their local/route conditions. No new Media3 owner is created.

## 4. State and response matrix

| Current state/event | Valid? | Next state | Service commands |
|---|---|---|---|
| Processing + P05 Ambiguous(1–2) + same Ready revision | Yes | Confirming(token, options) | 0 |
| Processing + P05 Direct + real high-confidence STT | Yes | Processing → Executed only after P06 ACK, else Failed | At most 1 |
| Confirming + matching token + valid 0/1 selection + Ready unchanged revision | Yes | Processing → Executed if ACK, else Failed | At most 1 |
| Confirming + matching token + invalid option | No | Failed(nonretry) | 0 |
| Confirming + old/wrong token | No | Confirming unchanged | 0 |
| Confirming + duplicate selection after first claim | No | Processing/terminal unchanged | 0 additional |
| Confirming + Neither(matching token) | Yes, terminal | Failed(nonretry) | 0 |
| Confirming + Cancel(matching token) | Yes | Idle | 0 |
| Confirming + inactivity timeout | Yes, terminal | Failed(nonretry) | 0 |
| Confirming + library revision/permission no longer valid | No | Failed(nonretry), manual | 0 |
| Confirming + app/Driving exit, route/call/focus/permission invalidation | Canceled | Idle; generation canceled | 0 if not previously dispatched |
| Confirming + second Mic, recognition retry, second clarification | Not admitted | Confirming unchanged (or terminal after own timeout) | 0 |
| Processing + P06 ACK failure/late stale response | No success | Failed or ignored if generation invalidated | 0/1 already attempted |
| Existing five #10 controls | Existing policy | Existing Processing → Executed/Failed | Existing transport path only |

## 5. Single-use and cancellation safety audit

**Coordinator is the only authority for the pending response.** It holds the private artist identity list and a token bound to one current session/generation; the UI sees only labels and 0-based indices. Tokens and artist IDs are never persisted or telemetered. The 1–2 UI option list is unmodifiable, and diagnostic toString() redacts labels/token. The P06 claim's independent AtomicBoolean prevents re-entry with the same claim. Together they defend against duplicated UI callbacks, stale generations and mistaken replay without adding an extra Player.

**Lifecycle caveat:** once an authorized ReplaceQueue has already been sent, canceling the UI cannot retroactively retract a command in the service. The existing service has route/focus and serialized receipt protections, but a timeout or Activity cancellation after command acceptance is not mathematical proof that playback will remain unchanged. The coordinator never publishes a late Executed after it is invalidated and never resubmits with a new command ID automatically. P10 and P13 must verify late-ACK, route and focus race behavior on actual devices before release.

**User acknowledgment caveat:** ExoPlayer Buffering ACK does not prove sound has been heard. A confirmed one-step choice still requires owner-run P13 practical media playback checks; P07 makes no voice accuracy or p95 performance claim.

**Missing-provider confidence caveat:** the current Moonshine implementation reports null confidence. Consequently a clear local artist is offered for explicit confirmation, not direct playback based on fabricated confidence.

## 6. Source-only QA matrix

**26 new JVM JUnit methods** authored in Issue11Phase7ArtistCoordinatorConfirmationTest.kt. Scenarios:
- Moonshine missing confidence → single-option Confirming; microphone lease release, live generation retention.
- Confirmation selection → one service ReplaceQueue and ACK-driven Executed; immediate duplicate tap rejection; wrong token cannot consume valid choice.
- Malformed index consumes one answer → Failed, no second clarification; Neither terminal; Cancel Idle; old-token negative controls ignored.
- 10s expiry; revision change; permission revocation; canceled queued worker; late non-cancellable ACK cannot publish stale success; bad ACK rejected.
- High calibrated confidence direct path; VAD-only null-confidence still requires confirmation; five Issue #10 transport controls stay outside Confirming.
- No-match/unsupported/non-English safety; second Mic/retry block; new explicit session invalidates old token; 2 canonical exact artist options.
- Private artist option labels/token redacted from string diagnostics; published options unmodifiable.

**All 26 tests are NOT_RUN.** No Kotlin compiler result or actual service ACK was obtained in P07. Earlier P02–P06 tests also remain NOT_RUN. P13 phone checklist M01–M48, technical T01–T10 and lab L01–L06 remain NOT_RUN. The 13 Issue #11 acceptance criteria retain 0 verified PASS.

## 7. Dependencies before final acceptance

- **P08:** Compose Player/Driving must render Confirming, 1–2 options and call confirm(token,index), neither(token) or cancelConfirmation(token), with accessible labels and no new mic turn. P07 source does not yet implement UI.
- **P09:** one short TTS clarification and concise typed UX for Unknown, Unsupported, NotFound, timeout, stale and failure. Current P07 only preserves existing Issue #10 voice feedback and terminal state messages.
- **P10:** validate call/focus, route, headset, Bluetooth and app foreground race safety, especially a previously accepted Media3 worker command.
- **P11/P12:** privacy allowlist, complete wiring and source-authored integration fixtures. No tests/build until P13.
- **P13:** first cumulative APK, source/Android validation only as approved, owner device tests and actual pass/fail record. Merge/close Issue #11 **only after owner acceptance**.

Independent P0 debt: [Issue #80](https://github.com/latticeward-labs/music-copilot/issues/80) remains open; last owner-reported Issue #10 voice lab 24/50 = 48% against the 90% gate. P07 does not repair STT accuracy.

**P07 disposition:** Coordinator source implementation committed; all assertions and physical behavior remain UNVERIFIED / NOT_RUN.
