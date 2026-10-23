# Issue #8 — P03 Media3 Audio Focus Observation and Service Integration

**Date:** 2026-10-23  
**Gate:** **PASS_SOURCE_ONLY / CODE AUTHORED, NOT BUILT OR TESTED**  
**Branch:** `feature/8-audio-focus-route-recovery`  
**Issue #8:** OPEN; original AC01–AC12 UNVERIFIED.  
**Next:** P04 — transient/permanent focus, calls and ducking policy reconciliation.

## 1. Source authority and inputs

- Reopened the current user-uploaded `MSM2(1)(1).rar` and verified SHA-256 **`0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`**, identical to P00/P01. The previously extracted 12 English source PDFs remain available. [ADR-0002](../adr/0002-service-owned-media3-playback.md) and the original PRD/MVP/Code & Technical Documentation require a single service-owned player, Android audio focus/route safety, no unwanted speaker continuation and synchronized system controls.
- [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16), [Runtime Policies §8](../architecture/RUNTIME_POLICIES.md), [UF-11](../user-flows/UF-11_INTERRUPTION_AUDIO_ROUTE.md), [TS-04](../user-flows/evidence/TS-04_AUDIO_FOCUS_POLICY.md), [P00](ISSUE_8_PHASE0_SOURCE_AND_READINESS_AUDIT.md), [P01](ISSUE_8_PHASE1_GITHUB_EXECUTION_CONTRACT.md), [P02 pure policy](ISSUE_8_PHASE2_DETERMINISTIC_INTERRUPTION_POLICY.md) are the controlling repository evidence.
- Existing Issue #7 Media3 `MusicPlaybackService` was preserved. Audio focus is still owned **only** by the existing `ExoPlayer.setAudioAttributes(USAGE_MEDIA/MUSIC, true)`, with `setHandleAudioBecomingNoisy(true)`, service-owned Session/Player and the established command processor.
- Public SDK references (reviewed for actual, not conjectured callback meanings): [Media3 Player](https://developer.android.com/reference/androidx/media3/common/Player) and [Player.Listener](https://developer.android.com/reference/androidx/media3/common/Player.Listener). A `PLAYBACK_SUPPRESSION_REASON_TRANSIENT_AUDIO_FOCUS_LOSS` is observable suppression; `PLAY_WHEN_READY_CHANGE_REASON_AUDIO_FOCUS_LOSS` indicates a Media3 focus-related pause but **does not identify a phone call or reliably classify every OS loss type**.

## 2. Files actually changed in P03

| Repository file | P03 work |
|---|---|
| [`app/src/main/java/com/latticeward/musiccopilot/playback/service/Media3FocusObservationAdapter.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/Media3FocusObservationAdapter.kt) | **Added:** one service-Player-looper Media3 listener bridge. Normalizes observed focus suppression, terminal focus-related playWhenReady change, actual isPlaying, verified explicit user Play/Pause/Stop into P02 envelopes. Sequence/time are injectable; source contains no focus request, telephony probe, second Player, new network dependency, raw device identity or volume-control system. |
| [`app/src/main/java/com/latticeward/musiccopilot/playback/service/MusicPlaybackService.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/MusicPlaybackService.kt) | **Updated:** instantiates bridge after existing Player/command worker; routes `onPlayWhenReadyChanged`, `onPlaybackSuppressionReasonChanged`, `onIsPlayingChanged` to it; sends validated player Play/queue selections and manual Pause/Stop events into policy; closes observer on service teardown. Existing `onEvents -> publishSnapshot()` remains authoritative and legacy safety gate remains intact. |
| [P03 report](ISSUE_8_PHASE3_MEDIA3_FOCUS_SERVICE_INTEGRATION.md) | **Added:** observation/effect map and residual limitations. |
| [QA ledger](ISSUE_8_QA_RESULTS.json), [phased plan](ISSUE_8_PHASED_EXECUTION_PLAN.md), [traceability](ISSUE_8_PHASE1_TRACEABILITY_INDEX.json) | P03 documentary state and dependency/evidence map; manual/technical/AC acceptance unaffected. |

### Existing service ownership unchanged

- Exactly **one** ExoPlayer + MediaSession + `PlaybackCommandProcessor` remain service-owned; no `AudioManager.requestAudioFocus`, focus listener, phone-state receiver, Activity player, stream source, new `FOREGROUND_SERVICE` policy or secondary persistent playback store.
- **Media3 itself** performs actual OS focus requests, suppression, ducking and recovery. The bridge only observes public Player callbacks and records pure policy decisions; it does not issue a duplicate `player.play()` when suppression disappears.
- `onEvents` continues to capture and broadcast real `PlaybackSnapshot` and MediaSession extras; no new UI-created Playing state, no progress changes, no second source of truth.
- Service's `PlaybackInterruptionGate` retains priority for explicit pause/headset/noisy safe-pause. P03 does not replace route observer nor alter route permissions.
- The bridge refuses to crash the service if a theoretical sequence counter saturates: repeated saturated sequence is rejected by the existing pure reducer.

## 3. Observed signal → normalized policy event

| Real service callback/source | P02 event/effect mapping | Important limitation |
|---|---|---|
| `onPlaybackSuppressionReasonChanged(TRANSIENT_AUDIO_FOCUS_LOSS)` | `FocusLostTransient(wasPlayingBeforeLoss=lastObservedAudiblePlaying && player.playWhenReady)` with candidate token if supported; emits **pure** Pause/Voice-cancel effect | Media3 has **already suppressed** audio; calling `player.pause()` again would prevent automatic unsuppression. P03 never invokes the pure transient PAUSE effect. Voice cancellation effect remains pending P09. Callback may arrive after `isPlaying=false`: fail closed (no candidate), never fabricate prior-playing proof. |
| Suppression changes from transient → `NONE` | `FocusRestored(previousObservedEpisode)`; reducer checks episode, route/manual revision and eligibility | The bridge emits **no duplicate Play**. P04 must reconcile modelled eligibility versus Media3's own unsuppression and prevent a denied resume if signals arrive in adverse order. |
| Suppression changes from transient → unsuitable/unknown reason | Terminal `FocusLostPermanent` (fail-closed) | Not evidence of a returned focus grant; cannot assert user-visible call behavior. |
| `onPlayWhenReadyChanged(false,AUDIO_FOCUS_LOSS)` | Conservative `FocusLostPermanent` | The public reason does not disclose whether an actual phone call occurred. P04 must refine behavior without an independent Audio Focus owner. Media3 already paused; no second Pause request. |
| `onIsPlayingChanged(isPlaying)` | `PlaybackObserved(isPlaying)`, model synchronized from actual Player | Only actual audible unsafe playback may call the **existing same Player** `pause()`; no fabricated Playing or callback-timer effects. Existing route/manual gate also remains active. |
| Validated explicit Play/playing queue selection after MediaStore preflight | `PlayRequested(VERIFIED_USER, currentPolicyRouteGeneration)`, capture pending user retry | A Play command is **not proof** of focus grant or audible playback. Real `isPlaying=true` after explicit retry is used only to reconcile a *previously classified terminal* policy state; no synthetic OS gain callback is claimed. Distinguishing reconnect-generated AVRCP commands is **P06/P07** and remains a risk. |
| Service manual Pause/Stop / deliberately paused selection | `UserPause` or `UserStop`; clears candidate and requires explicit Play | Manual choice wins over candidate stored by P02, no automatic Resume effect. |
| Noisy route changes and AudioDeviceCallback removal | Existing #7 `PlaybackInterruptionGate` + immediate service pause | Full normalized `RouteDisconnected`/route epoch synchronization is **not** introduced in P03. It is explicitly assigned to P05/P07; selected/valid Bluetooth route and reconnect mapping are P06. |

## 4. No dual-focus and effect-handling boundary

```text
Android AudioManager (OS)
        |
Media3 ExoPlayer single audio-focus manager
        |
Player.Listener (MediaSessionService Main looper)
   |         |             |
suppression  focus-loss     real isPlaying
   \         |             /
    Media3FocusObservationAdapter
        |
typed InterruptionEnvelope(sequence,elapsedTime,event)
        |
P02 InterruptionReducer (pure policy)
        |
+-- logical result / four-guard focus candidate (no second authority)
+-- unsafe audible playback safety PAUSE on *same* Player
+-- all other playback resume/duck/Voice effects deferred to approved phases
        |
MusicPlaybackService onEvents()
        |
one real PlaybackSnapshot --> MediaSession --> OS and UI controllers
```

The P02 reducer emits `RESUME_SERVICE_PLAYER` on a valid matched focus episode, but **P03 intentionally does not translate it into an extra Play**: Media3 owns focus/unsuppression already. In P04, review how to suppress disallowed Media3 auto-resume without accidentally defeating legitimate guarded resume; do not claim the four-guard contract is physically enforced until that policy is integrated and tested.

### 4.1 Snapshot/foreground invariants

- The observer must not call `publishSnapshot()` with an invented Playing state; `MusicPlaybackService.onEvents` continues to call existing `Media3SnapshotProjector.capture(player, runtimeError)` and `session.setSessionExtras`. Full Player, Mini Player, Driving and notifications remain downstream of one service.
- `onUpdateNotification()` and `onTaskRemoved()` are untouched; Media3's own mediaPlayback foreground lifecycle remains in place. Android 15+/API 35 foreground/focus entitlement needs P11 source audit + P13 field evidence.
- `onIsPlayingChanged` may trigger synchronous same-player safety Pause in response to **actually audible** unsafe state; there is no additional owner, thread, queue or OS focus request. Observer teardown precedes disposal of Player/Session.

## 5. P03 source review and limitations (no execution)

| Review | Conclusion | Qualification |
|---|---|---|
| Branch/source alignment | #7 merged `main` baseline, P02 policy reused, archive SHA matched | No new local/CI build used to prove compilation |
| Focus request ownership | Existing ExoPlayer remains sole focus owner | No duplicate `requestAudioFocus` added |
| Media3 callback names | Public Player.Listener callbacks, public Player reason constants used | SDK compatibility needs later build |
| Clock/sequence seam | Injected clock/sequence, event ordering preserved by service Main callback | No simulated tests ran |
| Policy classification | Only actually exposed focus reason/suppression treated as observed, no call identity | Ducking attenuation/call lifecycle is not fully observable via Player API |
| Temporary focus and safe resume | Reducer receives episode signals; no duplicate Play | P04 must enforce denied auto-resume and refine permanent/transient |
| Route and late IO | Existing #7 safety latch still applies | P05/P07 must close idle-Player route epoch gap; P06 BT reconnect |
| Voice | Pure Voice-cancel effect defined by P02 | P09 app-scoped Voice coordinator integration pending |
| Privacy | No route identifiers, contact/call identity, private media, STT or telemetry added | P10 still responsible for typed error diagnostics |
| Runtime QA | **0 builds, 0 test executions, 0 APK, 0 Actions dispatched** | 45 M-cases and 15 T-cases stay NOT_RUN; 12 AC UNVERIFIED |

## 6. Known issues and phase handoff

1. **P04:** OS-managed duck cannot be classified from a public volume-gain callback; do not re-duck. Review Media3 suppression callback ordering and distinctions between transient suppression and a focus-related `playWhenReady=false`. Enforce allowed vs denied auto-resume using only available evidence; no telephony claims. Existing explicit Play recovery must remain possible after user intervention.
2. **P05:** route disconnect path in #7 currently only latches if Player has an active/non-idle queue, so an initial pending long MediaStore request against an idle Player can slip through; update urgent route epoch under all relevant callbacks.
3. **P06:** actual Bluetooth reconnect validation and AVRCP user-intent provenance is not solved by observed Media3 callbacks; conservative fallback is required.
4. **P07:** receipts and asynchronous MediaStore/command worker require monotonic epoch cancellation; severe race windows and OEM duplicate callback sequencing must be addressed.
5. **P08:** ensure notification, controller and accessible UI follow one authoritative snapshot during complex interruptions.
6. **P09:** cancel active Voice STT/TTS/confirmation on focus or route invalidation through existing coordinator; no full #10 implementation assumed.
7. **P10–P11:** interruption error taxonomies, privacy, Android 10/12/13/15+/target36 constraints, OEM behavior.
8. **P12:** author source test matrix only; **NO execution**. **P13:** one cumulative debug APK, owner phone report; technical/other OEM cases unverified until actual evidence.

## 7. P03 completion gate

- [x] Preserved source and existing accepted #7 playback/MediaSession ownership.
- [x] Implemented a source-level Media3 Player.Listener-to-P02 typed-event adapter.
- [x] Wired suppression, focus-related pause, real isPlaying and manual command pathways into service.
- [x] Reused existing published Player snapshot and service cleanup.
- [x] Did not introduce secondary focus/volume/telephony/Player ownership or new sensitive permission.
- [x] Registered P03 source-only review and explicit P04/P05/P07 dependencies.
- [x] No Gradle/Kotlin compile, test, emulator, phone, APK or GitHub Actions.
- [ ] P04 resume/call/duck integrated safety; not a P03 claim.
- [ ] P05–P07 route and race hardening; not a P03 claim.
- [ ] P13 integrated build and owner smartphone results; not a P03 claim.

**P03 disposition: SOURCE IMPLEMENTATION COMPLETE, NOT COMPILATION OR BEHAVIOR VERIFIED. NEXT P04.**
