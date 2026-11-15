# Issue #12 — P08: Driving Session Ownership and Truthful Completion

**Date:** 2026-11-15  
**Branch:** `feature/12-privacy-safe-telemetry-measurement`  
**Status:** `SOURCE_IMPLEMENTED_UNCOMPILED_NOT_RUN`  
**Owner source:** directly inspected user-uploaded `MSM2(1)(1).rar` (verified SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`; contains 12 English PDF sources).

## 1. Source of authority and exact scope

1. **Original English PRD v1 Clarified, printed p.10:** `session_completed` is an approved event, the product North Star is "**Successful driving session with at least one voice command / at least 50% of weekly users**", driving reuse target >=50%, weekly return >=30%. No unbounded listening or private voice/track telemetry.
2. **Original English MVP v1, printed p.10:** `session_completed(duration_bucket, commands_count)` is the entire authorized event payload. The source does **not** specify a unique identifier, duration thresholds, exact command-count semantics, crash-recovery marker, completed-session formula or how to join participants across weeks. Printed p.9 sets >=99% crash-free Beta sessions and strict Beta evidence requirements.
3. **Original English technical blueprint, printed telemetry table p.39:** same event and fields. Source provides conceptual best-effort privacy filtering, **not** permission to export ID, timestamp, artist, title, audio or arbitrary crash details.
4. **Original System Architecture + ADR-0002:** one service-owned Media3 player and snapshot; app navigation never becomes playback authority. **ADR-0003/0004/0005/0006**: local-first, no voice transcript, deterministic intent, no account/backend/streaming, user consent and deletion boundaries, privacy-safe nonblocking diagnostics.
5. **Approved GitHub Issue #16 + current sources:** the existing Activity-scoped `DrivingNavigationViewModel` has the authoritative user-action navigation edge, retained across normal configuration changes but **not** restored after process death. Existing `MusicPlaybackService` owns playback, and `DefaultVoiceSessionCoordinator.publishExecuted` is the only accepted current-generation, real service-ACK voice execution transition. P02 frozen metric dictionary, P03 eight-event v1 allowlist, P04 default-deny diagnostic consent, P05 Driving entry, P06 Voice ACK, and P07 OS historical crash classifier were read on the feature branch.

**Interpretations explicitly identified as engineering decisions, NOT direct PDF quotes:**
- `commands_count` currently counts **Voice commands which reached the current Voice owner's `Executed` state after validated Media3 ACK while Driving was the origin**. It does not count manual navigation/Play/Pause taps, Mic attempts, unknown intent, failed/late ACK, confirmation choices or OS reconnects. The PDF gives no broader command-count definition; Product+QA must approve this mapping before Beta GO.
- `duration_bucket` uses elapsed-realtime monotonic boundaries: `under_1m` for [0,60000ms), `1_to_5m` [60000,300000ms), `5_to_15m` [300000,900000ms), `over_15m` >=900000ms. P03 approved these names but **did not officially approve the numeric boundaries**; Product review is pending.
- An **explicit Exit/Back accepted from an active Driving screen** is the only eligible clean finish; app backgrounding, process kill, runtime corruption and source-route invalidation are **not** equivalent to a user choosing Exit.

## 2. Single state owner and exact field envelope

New [DrivingSessionTelemetryOwner.kt](../../app/src/main/java/com/latticeward/musiccopilot/core/application/driving/DrivingSessionTelemetryOwner.kt) belongs to the **one app process composition graph** (`ApplicationBindings`), not to a Compose destination or second MediaSession. It is an ordinary synchronized, testable, no-Android-dependency class with an injected monotonic clock. No Media3 command, player, audio focus, Bluetooth permission, microphone, file or network access is in the owner.

One volatile active entry contains ONLY:
- `startedAt: Long` in elapsedRealtime monotonic milliseconds — **in-process only**, never exported or persisted;
- `acknowledgedVoiceCommands: Int` from 0 to 1000, exactly incremented once per acknowledged current Voice attempt while origin is Driving;
- `countOverflow: Boolean` — fail closed if the 1001st ACK would make the fixed v1 bound false.

Owner retains at most one coarse `ExitEvidence` enum (`NONE`, `EXPLICIT_COMPLETED`, `ABANDONED_UNKNOWN`, `INVALID_MEASUREMENT`) for in-process inspection; no event, user key, installation ID, VoiceSessionId, route/device ID, saved-state bundle, Room record, DataStore preference or daily cohort key is stored. It does not attempt to infer app crash-freedom.

The **only outbound payload** remains the preapproved P03 eight-family `TelemetryEvent.SessionCompleted(durationBucket,commandsCount)`. A once-only explicit closure constructs exactly those two fields and places them through the existing P04 consent-check -> P06 bounded FIFO (capacity=64) -> P03 privacy schema -> gated `NoOpTelemetrySink`. No raw monotonic timestamps or session marker leave the owner.

## 3. Lifecycle and event attribution matrix

| Boundary | Source of truth | P08 action | Why |
|---|---|---|---|
| Eligible Library Driving tap, or Player Driving tap | `DrivingNavigationViewModel.enterFrom` admits inactive → active | Begin one app-scoped live ticket, only if P04 diagnostic consent+all approvals already valid | Matches P05 real entry; not Compose render |
| Explicit accepted Bluetooth invitation from foreground Library/Player | Existing P05 `enterDrivingOnce` after user claim | Same begin; **no auto entry** from reconnect | Normal entry semantics |
| Rapid duplicate Driving tap / navigation recomposition | Existing active navigation guard | No new ticket, no repeat begin | No double session |
| Rotation or configuration recreation retaining Activity-scoped ViewModel | Android normal configuration lifecycle | No end, no new begin; app graph and single owner remain | Screen recreation is not session termination |
| App in background, Home, screen off, normal UI stop | `Activity.onStop` (existing) | **No clean completion and no abandon**; live in-RAM ticket remains if process alive | Audio may legitimately continue; no explicit Exit |
| Phone call, focus/route or Bluetooth loss | Existing Media3/Voice interruption policy | No success or end; Driving navigation remains the arbiter. Voice may be canceled by existing safety logic | A paused/focus-suppressed song is not a finished journey |
| One current Driving Voice command returned validated Media3 ACK | `DefaultVoiceSessionCoordinator.publishExecuted` after service ACK | App callback increments bounded counter **only for VoiceOrigin.Driving and active ticket** | Do not count detection, intention, TTS or permission prompt |
| `PLAY_ARTIST` direct or confirmed successful Media3 ACK | Same existing `publishExecuted` | One bounded increment for that executed attempt | Choice alone is not a command execution |
| Voice unknown, failed, timeout, canceled, stale ACK, extra Mic, confirmation-only | Existing Voice state/generation gate | No increment | Avoid false North Star |
| User taps Driving **Exit** or Android **Back** while actually active | Existing `exitDriving`, calls `DrivingNavigationViewModel.exit()` | Consume live ticket first; if valid monotonic duration and count, enqueue **one** `session_completed` | Real explicit terminal boundary |
| Repeat Exit, stale callback after already inactive | Same existing navigation state | No new callback, no duplicate completion | Exactly-once claim |
| Onboarding/bootstrap/settings/replay/media permission recovery invalidates Driving | Existing `clearForSystemBoundary`, only if state was active | Abandon as **UNKNOWN**, no completion | System-forced navigation is not intentional Exit |
| UI permission revoked, store recovery or unsafe view state | Same authoritative system route | Abandon/UNKNOWN where navigation invalidation occurs | No automatic completion or crash-free |
| Consent missing, declined, revoked, pending permissions/approvals | P04 independent non-audio authorizer | Do not create ticket or event; clear active ticket on explicit revoke/close | No inferred retroactive telemetry |
| App process killed / OS LMK / ANR / native crash | OS lifecycle | RAM ticket is lost; **no fabricated completion** | P07 historical exit reason cannot safely join to a particular Driving session |
| Elapsed time invalid, monotonic clock fails, >1000 verified voice ACKs | Pure owner | Consume ticket but classify INVALID_MEASUREMENT; **drop completion** | Never invent missing time / truncate count |
| A new explicit re-entry after valid exit or invalidation | Existing navigation admission | New independent ticket, count resets | Genuine new session |

**Notice:** Android process loss is **unobserved/UNKNOWN** without a durable marker, not a retained `ABANDONED_UNKNOWN` record after process restart. This is deliberate privacy-preserving missingness and must not be portrayed as comprehensive session capture.

## 4. Integration and ownership

| Existing/new file | P08 narrow modification |
|---|---|
| `core/application/driving/DrivingSessionTelemetryOwner.kt` (new) | Single synchronized volatile owner; begin/ACK count/explicit complete/abandon/privacy forget; exact v1 duration and count bounds |
| `voice/session/DefaultVoiceSessionCoordinator.kt` | Optional `onAcknowledgedDrivingCommand(VoiceOrigin)` callback **inside the existing** real current-generation `publishExecuted` transition, only if origin=Driving, best effort. Does not touch STT, deterministic mapper, artist matching, command receipt, Media3 or TTS |
| `app/di/VoiceBindings.kt` | Forward the optional ACK-only callback without breaking old constructors/fixtures |
| `app/di/ApplicationBindings.kt` | Own one telemetry session owner; begin after accepted P05 navigation entry, ACK count with live consent, publish only on explicit user exit, invalidate/forget without emit on forced route, revocation, close |
| `app/navigation/AppNavHost.kt` | Existing Exit/Back shared lambda triggers a new optional `onDrivingModeExplicitlyExited` **after** actual active → inactive navigation; existing system invalidation effect calls `onDrivingModeSystemInvalidated` only if Driving was previously active. No observers in Compose recomposition/start/stop |
| `app/MainActivity.kt` | Wire the two new callbacks to the same app-scoped bindings. No navigation or playback ownership change |
| `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue12Phase8DrivingSessionOwnerTest.kt` (new) | **17 authored, NOT_RUN** state, timing, overflow, privacy, duplicate, ACK and unknown cases |
| `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue12Phase8DrivingLifecycleContractTest.kt` (new) | **11 authored, NOT_RUN** wiring, rotation, focus, system invalidation, source safety and schema contract checks |

**No change** to any Media3 Service/player, playback command, queue, audio focus, Bluetooth route recovery, Vosk/Moonshine STT, intent grammar, PLAY_ARTIST algorithm, Room/MediaStore, onboarding or OS permission UX. No new analytics SDK or provider.

## 5. Consent, P07 crash and P09 retention gate

P04's four legal/copy/retention/collection-adapter approvals remain `false` and the actual release sink remains `NoOpTelemetrySink`. The owner **does not start** when telemetry collection is not authorized. No P08 event is actually being stored or exported now; a stored `Granted` user preference alone is insufficient.

This phase adds **NO persistent marker**, because P09 has not yet approved the legitimate need, TTL, cap, backups, erase semantics or retention duration for even a minimal diagnostic marker. The app never silently writes a new Room row or DataStore preference on Driving entry. A future approved P09 implementation may implement a **minimal consented pending/abandoned flag** and on-device aggregate completeness, with no exported person/session ID. Until such approval exists:
- Restart after OS kill is recognized neither as success nor proof of a particular crash.
- Process exit reasons from P07 are **historical per-process categories**, not per-Driving-session verdicts.
- A `session_completed` event does not prove absence of a serious Player defect while in Driving; crash-free requires independent reliable evidence as defined in P02.
- A real completion could be missing because consent was off, the v1 schema rejected a field, the bounded FIFO dropped it, Android Bluetooth observation was unknown on P05 start, or the process died; never infer 0/100% from observed events alone.
- P10 must keep started/finished/abandoned/unknown coverage with trusted denominator provenance; **the eight outbound families carry no session identifier** and cannot alone reconstruct exact same-session joins. Any Beta participant/reuse statistic remains not verified until independent consented cohort evidence is available.
- A session with `commands_count=0` is a valid explicit navigation completion but **not** a successful North-Star Voice session.
- The "one accepted Driving Voice ACK" criterion is a performance proxy, **not** independent proof that the action was the one the user intended. QA-labeled correctness and the P02 North Star caveat remain outstanding.

**Unapproved thresholds / semantics:** exact 1/5/15 minute boundaries, Voice-only `commands_count` and any in-process clock/marker completeness handling are engineering proposals, not ratified product text. The limit 1000 is the existing P03 engineering bound. The owner does not silently saturate to 1000 after overflow.

## 6. Written-but-unexecuted QA checklist / exit

- [x] Reviewed the exact archive/PDFs and current #12 issue/plan, P02 dictionary, P03 allowlist, P04 privacy gate, P05 entry, P06 ACK callback, P07 crash truth.
- [x] Added exactly one app-scoped, bounded process-local Driving owner; no second playback or UI-owning session.
- [x] Wired accepted explicit user entry, accepted current-generation Media3-backed Voice execution count, and accepted Exit/Back only.
- [x] Treated rotation/background/focus/route interruptions as non-terminal, system/permission invalidation as UNKNOWN, and abnormal process death as unobserved UNKNOWN.
- [x] No unapproved persistence, personal media identifiers, timestamp payloads or external transport.
- [x] Authored 28 source test cases, including exact duration boundaries, 1001st-command overflow, stale/duplicate callbacks, permission/revoke and acceptance path.
- [ ] **NO Kotlin/JVM, Android, emulator, lint or device test executed**. NO Gradle, GitHub Actions, APK build, PR, merge, issue close.
- [ ] P09 approved retention and optional consented marker, P10 data completeness + North Star correctness, Product/Privacy/Legal signature and P14 physical-phone QA remain outstanding.
- [ ] All 14 Issue #12 acceptance criteria remain `NOT_VERIFIED` pending final user-led QA and signoffs.

**P08 result: `SOURCE_IMPLEMENTED_UNCOMPILED_NOT_RUN`, not PASS.** Next: P09 local diagnostic storage, retention, erase and optional reviewed local aggregate export. Inherited quality #80 Moonshine laboratory failure and #82 artist ambiguity remain separate open issues.
