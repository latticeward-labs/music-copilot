# Issue #8 — P07 Fast Events, Receipt-Time Command Ordering and Race Containment

**Date:** 2026-10-23 · **Issue:** [#8](https://github.com/latticeward-labs/music-copilot/issues/8) · **Branch:** `feature/8-audio-focus-route-recovery`  
**Gate:** **PASS_SOURCE_ONLY_NOT_COMPILED_OR_TESTED** · **Next: P08** · **Issue still OPEN**  
**Authority:** uploaded `MSM2(1)(1).rar` SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`; verified in current turn, unchanged from P00–P06. [P00](ISSUE_8_PHASE0_SOURCE_AND_READINESS_AUDIT.md), [P01](ISSUE_8_PHASE1_GITHUB_EXECUTION_CONTRACT.md), [P02](ISSUE_8_PHASE2_DETERMINISTIC_INTERRUPTION_POLICY.md), [P05](ISSUE_8_PHASE5_HEADSET_USB_NOISY_ROUTE_SAFETY_AUDIT.md), [P06](ISSUE_8_PHASE6_BLUETOOTH_RECONNECT_AVRCP_SAFETY_AUDIT.md), [UF-11](../user-flows/UF-11_INTERRUPTION_AUDIO_ROUTE.md), Issue #16 Runtime §8, ADR-0002.

## 1. Audit findings before P07

1. **P07-R1 — Worker arrival inversion (P0):** the existing single `PlaybackCommandProcessor` serializes **execution** but a command can wait behind an in-flight `queueEngine.prepare`/MediaStore revalidation. Prior code only advanced `restoreGeneration` in some ingress paths and **again at dispatch**; an old queued Play/ReplaceQueue/SelectQueueIndex could therefore acquire a fresh generation *after* a newer Pause/Stop or route event and incorrectly appear current.
2. **P07-R2 — Manual safety receipt delayed (P0):** queued Pause/Stop reached the Player only after the worker resumed. A newer urgent user Stop must not wait for file IO to stop audio.
3. **P07-R3 — Focus and BT quarantine races (P0):** a focus-loss event or Bluetooth added while paused must invalidate pending pre-event audible commands, even when an explicit route-removal event is absent; a focus gain may not authorize an old command.
4. **P07-R4 — Duplicate manual revisions (P1):** if a new receipt-time Pause and existing worker Pause both call the P02 reducer, one user action increments its manual revision twice. P07 invokes the policy only on receipt, and worker applies only actual Player/Stop checkpoint mutation.
5. **P07-R5 — Multi-signal idempotence:** Noisy + removed-device callbacks are already folded by the P05 gate while latched; P02 focus reducer maintains monotonic event sequences and discards duplicates/stale envelopes. This phase preserves both independent protections, with one Main service owner.

**Source review is not proof** of Android callback order, Audio Focus call semantics, head-unit Play intent, physical speaker leakage, or successful compilation. These remain NOT_RUN until authorized P13/manual/technical QA.

## 2. Exact production files changed in P07

| File | P07 changes |
|---|---|
| [`PlaybackCommandProcessor.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/command/PlaybackCommandProcessor.kt) | Added immutable `PlaybackReceiptStamp(routeGeneration,safetyGeneration,commandGeneration)` and `configureReceiptSafety(capture,current)`. `submit` captures its **original receipt-time** stamp synchronously after replay/deduplication and before queue admission. Worker rejects a stale Pending **before handler**, instead of restamping at dispatch. Admission exceptions resolve the reply as a sanitized failure. Existing single Channel(32), CompletableDeferred, idempotency/replay map and lifecycle still own every command. Constructor shape `PlaybackCommandProcessor(owner.scope, ::applyCommand)` unchanged for #7 compatibility. |
| [`MusicPlaybackService.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/MusicPlaybackService.kt) | Wired receipt callbacks to sole worker immediately after construction. Service Main increments receipt/restore and recovery generations for both private custom commands and system transport commands; no duplicate ingress increments. User Pause/Stop immediately sets existing gate/P02 manual policy and pauses the **same** Player, even if worker is suspended; worker later performs ordered Stop checkpoint destruction or idempotent Pause. Focus loss, transient suppression, P04 denied auto-return, first route removal and new BT-connect quarantine invalidate prior safety generation; service teardown rejects late work. An old queued Play/selection must match **original** command, route and safety generations. After IO, the existing ReplaceQueue, SelectQueueIndex, revalidate-Play, cold-restore and decoder-recovery paths retain their before/after generation checks. |
| [This audit](ISSUE_8_PHASE7_EVENT_ORDERING_AND_RACE_CONTAINMENT_AUDIT.md), [AC trace index](ISSUE_8_PHASE1_TRACEABILITY_INDEX.json), [QA ledger](ISSUE_8_QA_RESULTS.json), [plan](ISSUE_8_PHASED_EXECUTION_PLAN.md) | Source/evidence updates only. No source tests authored/executed, no APK. |

**No** second Player/Audio Focus requester/MediaSession/command worker/queue, app-side ducking, phone-state receiver, backend, private-route identities or background activity. The source project remains Android API 29+.

## 3. Concrete event precedence and Main-thread ownership

```text
MediaSession private command | system/AVRCP transport
                   |
      existing PlaybackCommandProcessor.submit() on service Main
                   |
          P07 synchronous captureCommandReceipt()
              |                         |
              | Pause/Stop             | Play/Select/Next/etc.
              |  - manual safety latch |  - no early Player mutation
              |  - P02 manual revision |  - record arrival/route/safety epoch
              |  - same Player.pause() |
              v                         v
                one existing bounded command queue
                             |
              P07 isQueuedCommandReceiptCurrent()
                 receipt.route / safety / intent generation
                   | valid             | stale
                   v                   v
           same applyCommand()       REJECT without side effect
             | await MediaStore IO
             | re-check route + command/recovery generation
             | commit same real ExoPlayer queue/player or REJECT
```

**Asynchronous urgent interruption is different from a queued playback intent:** headset removal/noisy, public focus-loss suppression, permanent focus loss and BT reconnect quarantine are handled by existing service Main callbacks outside suspended IO. They invalidate safety/restore generations, and a route loss immediately pauses the Player. Manual Pause/Stop also immediately pauses and invalidates earlier entries on receipt. No timestamp heuristic, duplicate queue owner or activity cache is used.

**P02 reducer order:** `Media3FocusObservationAdapter` serializes accepted focus/route/player events on same service Main and provides monotonic envelope sequences; focus-only `FocusRestored` still requires the same focus episode/four guards. P05 `onHeadsetOrBluetoothLost()` suppresses a second route epoch for the same armed episode. P06 untrusted AVRCP Play retains its **own** reception-stamped route epoch and must satisfy both the original P06 gate and the new P07 worker admission check.

## 4. Deterministic source-level race matrix

| Order of real observations (no test executed) | P07 expected result | Remaining gate |
|---|---|---|
| A=ReplaceQueue in long IO; B=Pause/Stop arrives | B immediately latches manual Pause and pauses service Player; A's original command epoch becomes stale, so no audible A commit after its resolver returns | P13 M40–M43/T06–T08 |
| A=ReplaceQueue arrives but awaits worker; B=route removed while Player empty | P05 bumps route & P07 safety epoch; worker drops A before it starts | P13 phone/noisy hardware |
| A=SelectQueueIndex already queued; B=new Play or selection arrives | New receipt command generation wins; old selection is rejected instead of taking a newly stamped dispatch epoch | P12 authored test source/P13 |
| A=external Play stamped epoch N; B=Bluetooth loss epoch N+1 | P06 worker route check and P07 stamp both veto A | P13 headset/Bluetooth |
| A=Play queued; B=transient focus suppression | Media3 manages focus; P07 bumps safety generation, old queued Play rejected; focus gain does not retroactively re-admit old A | P04 focus behavior field test |
| A=Play queued; B=permanent focus loss | Media3 safe pauses, P07 invalidates old queued Play; later restoration does not authorize it | P13 call/focus field test |
| A=BT reconnect while Paused; B=head unit synthesizes Play | P06 quarantine, P07 no relabeling of automatic head-unit Play as trusted | P13 owner field test |
| A=BT output added while Playing | Gate does not quarantine merely for an extra available sink; current real Player stays authoritative | P13 OEM compatibility |
| A=Noisy + AudioDeviceCallback remove in one episode | P05 route latch absorbs second signal; P07 first loss preempts pending old work | P13 noisy duplicate/race |
| A=focus loss; B=route loss; C=old matching focus gain | P02 focus episode and P05/P07 route/manual gate forbid unwanted resume | P13 rapid-event field test |
| A=Pause received while in-flight decoder recovery IO | Immediate Pause plus recovery/restore generation change; `recoverForward.stillCurrent` returns false before choosing successor | P13 corrupted media/recovery |
| A=Stop received while old queue worker busy | Immediate silence/intent latch; ordered worker later clears checkpoint and stops Player; stale audible IO is rejected | P13 Stop/restore |
| A=repeated commandId/payload replay | Processor returns the prior result without a second `capture`, physical Pause or queue commit | Later technical QA |
| A=teardown while pending worker and device callbacks | `serviceClosing`, gate generation invalidation, route monitor unregister, observer close, worker close and pending result rejection; no new player constructed | P11 lifecycle/P13 |
| A=revision/generation saturation after enormous uptime | P07 safety epoch saturates; worker fails closed for new non-safety commands; P05 route epoch saturates and blocks explicit unsafe Play | Unexercised/technical only |

### Receipt-generation policy and tradeoffs

- `Pause`/`Stop` are safety commands. Their receipt applies immediate manual latch/silence; they always remain eligible for later **ordered** worker cleanup even if an additional focus/route event arrives in the interim. No second P02 manual-revision increment at dispatch.
- `ReplaceQueue`, `SelectQueueIndex`, and `Play` received before a newer command are dropped if their recorded command generation is stale; this prioritizes the **latest explicitly received audible selection** rather than permitting old media IO to override it.
- Other transport operations (e.g., ordinary `Next`, `Previous`, `Seek`) retain existing serial worker behavior when no **safety** route/focus/manual interruption intervened. The `recoverForward` IO path still checks recovery generation before selecting an audible successor.
- Fresh **private explicit** Play after a route loss is permitted only through the existing validated service command after current-epoch acceptance and provider/permission checks. An automatic OS/AVRCP Play is never upgraded to trusted just because it arrives late.

## 5. Scope exclusions and downstream handoff

1. **P08:** confirm one authoritative Player/MediaSession/snapshot/notification/Mini/Full/Driving model across interrupted and recovered states. P07 only maintains the existing projector.
2. **P09:** actual `CANCEL_VOICE_FOR_ROUTE` and `CANCEL_VOICE_FOR_FOCUS` dispatch to app-scoped coordinator; P07 does not claim STT/TTS cancellation.
3. **P10:** typed privacy-safe `AppError.Interruption` and logs/source error mapping.
4. **P11:** Android 10/12/14+/15, OEM/car compatibility and foreground entitlement; no device execution in P07.
5. **P12:** author but **DO NOT RUN** JVM/instrumented/static test source including stale command, duplicate event, lost headset while idle, focus gain after Pause, reconnect-auto Play, fast STOP, pending corrupt recovery and lifecycle.
6. **P13 only:** exactly one integrated `:app:assembleDebug` APK from all phase commits, owner M01–M45 manual checklist, real results logged by user. T01–T15 need separate authorization. Neither compilation nor physical no-speaker leak nor 3 OEM/vehicle environments is proven by P07.

## 6. Documentary completion gate

- [x] Rechecked attached RAR hash and merged Issue #7/Issue #8 existing branch sources.
- [x] Preserved one service/Player/MediaSession/command worker.
- [x] Added immutable *receipt-time* command/route/safety epochs and fail-closed queued admission.
- [x] Prioritized immediate safety Pause/Stop without waiting for MediaStore IO.
- [x] Preserved route/noisy deduplication and P02 focus-episode/four-guard precedence.
- [x] Applied focus/permanent loss/BT quarantine event invalidations on service Main.
- [x] Removed dispatch-time retokenization and duplicate Pause/Stop P02 manual revision.
- [x] Reused existing IO post-suspension epoch checks and safety-first decoder recovery.
- [x] Kept M01–M45, T01–T15 NOT_RUN and all original AC UNVERIFIED.
- [x] Did not compile/build/run tests, emulator, handset, GitHub Actions or merge.
- [ ] P08 system/UI snapshot reconciliation, P09 Voice coordination, P12 tests authored only and P13 owner APK/hardware validation.

**P07 disposition: SOURCE IMPLEMENTATION COMPLETE, NOT COMPILATION/BEHAVIOR VERIFIED. P08 NEXT.**
