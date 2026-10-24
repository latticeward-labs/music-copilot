# Issue #8 — P10 Privacy-Safe Focus/Route Error & Recovery Mapping

**Date:** 2026-10-24 · **Branch:** `feature/8-audio-focus-route-recovery` · **Issue:** [#8](https://github.com/latticeward-labs/music-copilot/issues/8) **OPEN**  
**Gate:** `PASS_SOURCE_ONLY_NOT_COMPILED_OR_TESTED`, **P11 NEXT**. No tests, Kotlin/Gradle compilation, lint, Android device, emulator, Actions or APK executed; user-approved **P13 only** is the integrated APK phase. All 12 AC **UNVERIFIED**, M01–M45 and T01–T15 **NOT_RUN**.

## 1. Input and architecture baseline

- Uploaded `MSM2(1)(1).rar` SHA-256 reverified this turn as `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`, matching original 12 English source PDFs reviewed for P00–P09. Relevant [ADR-0003](../adr/0003-local-first-data-boundaries.md), [ADR-0002](../adr/0002-service-owned-media3-playback.md), PRD/technical blueprint Typed Errors and Data/Privacy/Quality (§05), [Runtime Policies §8](../architecture/RUNTIME_POLICIES.md), [UF-11](../user-flows/UF-11_INTERRUPTION_AUDIO_ROUTE.md), [P02 reducer](ISSUE_8_PHASE2_DETERMINISTIC_INTERRUPTION_POLICY.md), [P08 snapshot](ISSUE_8_PHASE8_UNIFIED_SESSION_NOTIFICATION_UI_AUDIT.md), and [P09 Voice cancellation](ISSUE_8_PHASE9_VOICE_FOCUS_ROUTE_CANCELLATION_AUDIT.md).
- This repository **already** defines `AppError.Interruption` and **three** stable allowlisted error codes in `AppErrorCodes` and `PlaybackWireProtocol`. P10 reuses them rather than inventing a new code family, protocol version, telemetry sink, service, focus requester or logging payload. Existing `AllowlistedTelemetryPrivacyGate` already allows those three static codes but keeps arbitrary data deny-by-default. P10 does **not** emit telemetry (not even local persistent diagnostics).
- Accepted privacy rule: diagnostic text/code MUST NEVER hold title, artist, album, MediaStore URI, MediaRef, full queue, raw audio, transcript, contact/phone status, Bluetooth MAC/name/identifier, precise location, stack trace or exception message. No new permission, backend or upload.

## 2. Production code committed

| Path | Change |
|---|---|
| [`playback/interruption/InterruptionNoticePolicy.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/interruption/InterruptionNoticePolicy.kt) | New **pure** closed `InterruptionNotice` category and `InterruptionNoticePolicy.fromAccepted(event, transition)`. Only an **applied** P02 state transition with approved effects may emit a notice, so duplicate/stale route and focus callbacks do not generate new messages. `error()` produces one static sanitized `AppError.Interruption`, no raw error/source data. |
| [`Media3FocusObservationAdapter.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/Media3FocusObservationAdapter.kt) | For each reducer-accepted transition, forwards only closed notice categories on the existing service Main callback. Existing Voice cancellation path remains; no extra observers. |
| [`MusicPlaybackService.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/MusicPlaybackService.kt) | `onSanitizedInterruptionNotice` writes **only** into existing `runtimeError` → `PlaybackSnapshot.lastError` → same MediaSession extras. Does **not** overwrite existing Permission, MediaItem, Playback or other more serious runtime failures; route-invalidated error outranks a weaker focus reminder; a successful accepted revalidation can clear only the interruption notice. No second event/error bus. |
| [`Media3SnapshotProjector.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/media3/Media3SnapshotProjector.kt) | An interruption notice alone does **not** turn a service with empty/IDLE Player into a terminal media-decoder `PlaybackState.Error`. Real Media3 errors remain authoritative. |
| [`PlaybackWireProtocol.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/command/PlaybackWireProtocol.kt) | Restricts `interruption` family to the **same three stable codes** for snapshot/command-response decoding, with `ManualResume` recovery action and correct `USER_ACTION_REQUIRED` versus `RECOVERABLE`. No personal content or new wire version. |
| [`feature/player/MiniPlayerBar.kt`](../../app/src/main/java/com/latticeward/musiccopilot/feature/player/MiniPlayerBar.kt), [`FullPlayerScreen.kt`](../../app/src/main/java/com/latticeward/musiccopilot/feature/player/FullPlayerScreen.kt), [`res/values/strings.xml`](../../app/src/main/res/values/strings.xml) | Full/Mini use the same existing PlayerUiState/PlaybackSnapshot; show **static** output/focus guidance (not a fabricated call), and Full Player exposes a **manual** Resume command only when Controller/queue is ready. It invokes the existing ViewModel/PlaybackGateway and still faces service P02–P07 route/focus/permission validation. No optimistic Play. |

## 3. Typed taxonomy / action map

| Normalized accepted P02 signal | Stable code | Recovery type / action | Presentation |
|---|---|---|---|
| Transient Media3 `FocusLostTransient` with actual accepted focus cancel effect | `CALL_OR_ROUTE_SAFE_PAUSED` | `RECOVERABLE / ManualResume` | Audio temporarily interrupted and safely paused; **NOT** assertion of a phone call |
| Media3 lasting `FocusLostPermanent` | `FOCUS_NOT_RECOVERED` | `USER_ACTION_REQUIRED / ManualResume` | Wait until interruption ends, resume explicitly |
| Media3 `FocusAutoResumeRejected` | `FOCUS_NOT_RECOVERED` | `USER_ACTION_REQUIRED / ManualResume` | Explicit Play required; OS auto-return not authorized |
| Actual P05 `RouteDisconnected` accepted only on first loss episode | `ROUTE_INVALIDATED` | `USER_ACTION_REQUIRED / ManualResume` | Check active preferred output before pressing Play; no auto-resume |
| Matching legitimate focus restoration / verified Play / authorized actually audible playback | Clear **only** `AppError.Interruption` | N/A | Return to service-confirmed state; never clear permission/corrupt-media failure |
| Duplicate old event, unmatched gain, stale delayed queued Play, unknown callback | **No new diagnostic** | Existing `PLAYBACK_REJECTED` command result where applicable | No false “call detected”; no personal payload |
| Raw exception, media URI, hardware name or unrecognized wire error category | **Never accepted** | Drop or sanitized pre-existing failure | Not logged, transmitted or rendered |

**Priority and boundaries:** Permission/MediaItem/Playback errors outrank an interruption reminder and are not silently erased by it. A physical route loss outranks a weaker focus reminder until verified manual recovery. Media3 still owns actual Player/notification and focus state; `lastError` is guidance, **not** a second Player/Session or permission to play. Focus regain is *not* a user Play. The approved four-guard resume policy remains mandatory.

## 4. Coarse event/diagnostic privacy policy

- Allowed vocabulary: only `FOCUS_TEMPORARY`, `FOCUS_UNAVAILABLE`, `ROUTE_UNSAFE`, `CLEARED` in an internal enum, or the **three** stable wire-visible codes. No free-form strings, metadata, paths, Bluetooth device IDs, telephone contact details or call identity.
- **No new telemetry emitter.** Existing privacy-gated quality APIs remain opt-in/deny-by-default; the source change does not open network sockets, SDK analytics or write diagnostic persistence. Future quality aggregation may accept only the existing code allowlist and coarse route category, but that is **not executed** here.
- No `android.util.Log` with raw failure, no user audio capture, no STT transcript, no audio/video or process secret storage. Static UI strings are local app resources and carry no device details.
- Error deduplication: identical `runtimeError` is not republished; P08 projector advances revisions only for changed complete snapshots; reducer drops duplicate/stale envelopes.

## 5. Known verification gaps

- **P11** review Android 10/12/14+/15 API/OEM foreground eligibility, accessibility announcements and notification/system callback differences; do not claim these validated.
- **P12** author source tests for priority (permission > route/focus), event-code matrix, repeated focus/route, wire roundtrip and forged error family, focus regained/cleared, empty Player not fatal, static UI help and privacy allowlist. **No execution in P12.**
- **P13** compile/build exactly **one** APK from all implemented phases and provide M01–M45. Relevant manual M39 and technical T11, plus M44–M45 for Voice. All actual tests remain NOT_RUN until their execution and results. Build-time Kotlin/API conflicts may still exist and must not be reported PASS beforehand.
- A Media3 focus callback **never proves** an actual call occurred. Unexpected no-speaker-leak or all-OEM call coverage remain **unverified**.
- The Driving screen is still only a placeholder. No P10 claim of physically rendered Driving recovery.
- An actionable ManualResume button means an explicit attempt *may be made*; it does not guarantee immediate Android Audio Focus or selected output, or bypass existing service safety checks.

## 6. Source-only completion gate

- [x] Rechecked owner-uploaded RAR hash and accepted privacy/error contracts.
- [x] Implemented typed, allowlisted, applied-transition-only interruption error policy.
- [x] Connected normalized Media3/route policy events to existing service error snapshot.
- [x] Preserved serious media/permission errors and blocked duplicate/downgraded notices.
- [x] Kept interruption safe pause separate from terminal playback errors.
- [x] Reused existing Media3 wire codes with consistent action/recoverability.
- [x] Added static accessible recovery guidance in Full/Mini Player, no new UI playback owner.
- [x] No personal or hardware identifiers, logging, network telemetry or permissions.
- [x] No Build, tests, Actions, APK, PR/merge or Issue close.
- [ ] Actual Kotlin/Android compilation and OEM/phone QA in P13; P11/P12 remain.

**Result: P10 SOURCE COMPLETE (`PASS_SOURCE_ONLY_NOT_COMPILED_OR_TESTED`); P11 NEXT.**
