# Issue #8 — P04: Calls, Transient/Permanent Focus, Duck and Guarded Recovery

**Date:** 2026-10-23  
**Execution branch:** `feature/8-audio-focus-route-recovery`  
**Source gate:** **PASS_SOURCE_ONLY_NOT_COMPILED_OR_TESTED** — production source authored, GitHub committed, no runtime QA claim  
**Build/QA:** 0 Gradle runs, 0 APK, 0 JVM/instrumentation/emulator/physical phone tests, 0 intentionally dispatched Actions; P13 remains sole integrated APK gate.  
**Original Issue AC01–AC12:** all **UNVERIFIED**; Issue **OPEN**, no PR/merge/close. **P05 next**.

## 1. Verified authority and prerequisites

- The owner-uploaded `MSM2(1)(1).rar` SHA-256 is `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`, matching P00/P01/P02/P03. Revisited extracted owner [ADR-0002](../adr/0002-service-owned-media3-playback.md) and MVP/UF Foundation: FR-03, SYS-08 and D-018 specify call-compatible **Pause/Duck**, conditional return and no call overlap; D-019 requires disconnect to block route-to-speaker audio.
- [Issue #16 Runtime §8](../architecture/RUNTIME_POLICIES.md), [UF-11](../user-flows/UF-11_INTERRUPTION_AUDIO_ROUTE.md), [TS-04](../user-flows/evidence/TS-04_AUDIO_FOCUS_POLICY.md), [P01 ownership contract](ISSUE_8_PHASE1_GITHUB_EXECUTION_CONTRACT.md) and [P02 pure reducer](ISSUE_8_PHASE2_DETERMINISTIC_INTERRUPTION_POLICY.md) govern all implementation decisions.
- Existing Issue #7 [`MusicPlaybackService`](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/MusicPlaybackService.kt) uses **one** ExoPlayer with `setAudioAttributes(USAGE_MEDIA/MUSIC, true)`, one MediaSession/serialized command processor, `setHandleAudioBecomingNoisy(true)` and the service-owned route gate. A second `AudioManager.requestAudioFocus`, telephony permission/listener, Player or independent TTS/duck manager is **forbidden**.
- Public Android contract: [Media3 Player / suppression and playWhenReady reasons](https://developer.android.com/reference/androidx/media3/common/Player), [Player.Listener](https://developer.android.com/reference/androidx/media3/common/Player.Listener), [Android Audio Focus guide](https://developer.android.com/media/optimize/audio-focus). Android 12+ may enforce system fade/mute; on Android 15+ target 35+ focus requests require the top app or eligible foreground service. Public Player focus reason **does not prove** the origin was an actual telephone call; no phone-state identities or call monitoring added.

## 2. Exact committed P04 changes

| File | Scope |
|---|---|
| [`playback/interruption/InterruptionPolicy.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/interruption/InterruptionPolicy.kt) | Added typed `FocusAutoResumeRejected` event. This clears the previous resume candidate, requires new explicit Play, requests safe Pause and **does not increment manualRevision**, preventing a fabricated user Pause in the policy. |
| [`playback/service/Media3FocusObservationAdapter.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/Media3FocusObservationAdapter.kt) | Enhanced pre-loss audible-state capture when callbacks arrive in either order; verified episode FocusRestored four-guard check, legacy route/manual gate priority, denied auto-return latch+same-Player pause, verified explicit-user Play during suppression, terminal Audio Focus loss classification and late unexpected `isPlaying` protection. No manual focus or duplicate Player/duck operations. |
| [`playback/service/PlaybackInterruptionGate.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/PlaybackInterruptionGate.kt) | Added `onDisallowedFocusAutoResume()` to latch Safe Paused until an explicit user Play while distinguishing it from the user's own Pause in the P02 reducer. |
| [`playback/service/MusicPlaybackService.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/MusicPlaybackService.kt) | Wired the denied-resume callback to the **existing** interruption gate. Existing Player/MediaSession lifecycle, route monitor, command processor, snapshot, queue, P1 Safe-Paused cold restore and media3 focus owner remain unchanged. |

**Boundary:** No new manifest permission, SDK dependency, network, log/telemetry with private file names or device IDs, Android telephony observer, duplicate requestAudioFocus manager, Activity player, app-side duck attenuation or direct `player.play()` on focus gain.

## 3. Four-guard recovery and event ordering

An automatic return from `PLAYBACK_SUPPRESSION_REASON_TRANSIENT_AUDIO_FOCUS_LOSS` to `NONE` is **allowed** only when the reducer confirms all these predicates and the existing service safety gate is not armed:

1. **Actually previously Playing:** the last observable `Player.isPlaying` was true just before a Media3-identified transient suppression. If callback order obscures pre-loss Playing, fail closed instead of fabricating it.
2. **Real focus return to same episode:** the public Media3 suppression reason moves to `NONE`; `FocusRestored` token equals the active transient focus episode; stale/duplicate gain produces no resume effect.
3. **Same valid route:** the reducer's route epoch is unchanged and the existing legacy `PlaybackInterruptionGate` has **no route-loss latch**. The reducer's route epoch is not yet physically wired to Android removal events; **P05/P07 must complete this**.
4. **No intervening manual Pause/Stop or stronger safety reason:** original `manualRevision` matches and `requiresExplicitPlay=false`. Service gate `explicitlyPaused` also vetoes.

The reducer emits `RESUME_SERVICE_PLAYER` *as permission*, **not a new call to Player.play**. The service deliberately allows **Media3's own unsuppression** when conditions hold, avoiding a duplicate stream or race with the OS focus manager. If any guard fails, `FocusAutoResumeRejected` sets an explicit-play-only state, the existing service gate latches, and the same service Player is paused if `playWhenReady` is still true. A late unexpectedly audible `isPlaying=true` is likewise safety-paused.

**Priority:** manual Pause/Stop, route loss and permanent focus loss beat an old transient focus token. A fresh **verified** user Play is distinct from automatic focus recovery; it is subject to existing route/queue/media permission validation. Bluetooth/head-unit provenance remains under P06/P07 and cannot be assumed from these callbacks.

## 4. Detailed focus/call/duck handling matrix (source design, NOT device observation)

| Source-observable event | Expected action | Resumption authorization |
|---|---|---|
| Transient focus suppression while actually Playing | Media3 suppresses naturally, reducer records prior Playing/episode and candidate; P04 does **not** invoke a second Pause merely for loss | Matching restored episode + four guards; no extra Play |
| Transient suppression while previously Paused / pre-loss Playing cannot be proven | Candidate absent; when suppression clears, service rejects possible implicit replay | Manual verified Play only |
| Matching gain with all four guards | Media3 removes suppression; service does not produce an extra Play | Conditional automatic return |
| Gain after user Pause/Stop | Safety latch, no automatic return | Next explicit validated Play |
| Gain after headset/Bluetooth loss | Legacy route latch defeats focus candidate; P05/P07 to unify route epoch | Manual Play only, after safe route validation |
| Permanent or OS-denied Audio Focus: `PLAY_WHEN_READY_CHANGE_REASON_AUDIO_FOCUS_LOSS` | Media3 playWhenReady false; pure policy terminal Safe Paused; no pending auto-return | New user-initiated validated Play only |
| Call while playing | Android/Media3 focus or OS call mute/fade policy applied as observable; treat as a focus interruption, not as identifiable telephony | Conditional if truly transient with proof; otherwise remain Paused |
| Call while user manually Paused | No candidate armed and later focus gain cannot start music | Manual Play only |
| OS-managed duck/fade | **Do not** apply an independent `Player.volume` multiplier or duck a second time. Public Player callbacks do not expose reliable per-duck focus category. | OS/Media3 restores original attenuation; no new playback started |
| Focus restored after unknown/unmatched/suppression-to-unsuitable-output | No automatic permission, conservative Safe Paused | Manual retry after an actually valid focus/output |
| Media3 callback order reversed or late `isPlaying=true` | Reconcile current Player suppression before trusting cached event; veto unsafe audible state on one service Player | No unguarded recovery |

**Important limitation:** Android 12+ may mute/fade phone calls without a distinguishable app callback. With Media3 managing focus, public `Player.Listener` does **not** expose whether loss is `AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK`, whether the OS originated an incoming call, or the exact AudioManager grant. Consequently this implementation provides call-compatible focus safety **without claiming call-detection or phone-specific automatic resume**; that must be checked on a physical handset in final P13. A direct call-state API would require separate permissions/architecture review and is intentionally absent.

## 5. Source-only review findings and open gaps

- **Source code review:** one ExoPlayer/Session, one focus owner, single service Main callback path, no new P04 permissions or raw call/Bluetooth/media telemetry; explicit manual Pause cannot be overwritten by focus regain in the policy, and denied return latches Safe Paused.
- **Not a compile/test pass:** no Gradle/JVM/Android run was authorized. API 29–36 compilation, player callback reentrancy, exact Media3 1.5.1/OEM event order and physical no-speaker-leak are unverified.
- **P05:** urgent wired/USB/noisy loss and route epoch must invalidate pending work **even with an empty/idle Player**; existing #7 conditional route gate must be corrected.
- **P06:** distinguish reconnect-only Bluetooth auto-play/AVRCP from deliberate remote media key where possible; block ambiguous auto requests conservatively.
- **P07:** serialize receipt-time command/route/focus events and stale IO completions; formalize callback idempotence and race protection.
- **P08:** atomic Player snapshot across notification/lock screen/Full/Mini/Driving.
- **P09:** connect pure Voice-cancel effects to the actual Voice coordinator; P04 does **not** declare active STT/TTS cancelled.
- **P10–P11:** sanitized interruption errors, foreground eligibility, vendor and accessibility policy.
- **P12:** author (but **do not execute**) deterministic source tests for transient Playing → gain, Paused → gain, manual Pause → gain, permanent loss → gain, route changed → gain, duck, reversed callback order, duplicate focus changes and rapid races.
- **P13 only:** one cumulative debug APK, user M01–M45 checklist and physical report. T01–T15 remain NOT_RUN without later explicit testing authorization. Pixel/Samsung/Xiaomi/three parked vehicle configurations are separately not claimed tested.

## 6. P04 completion checklist

- [x] Reconciled verified original RAR and accepted Runtime §8/UF-11/ADR-0002.
- [x] Implemented explicit denied-focus-auto-resume pure event and service latch.
- [x] Integrated four-guard Media3 suppression return with actual Player and legacy route/manual gate.
- [x] Preserved OS-owned ducking without extra Player volume or play operations.
- [x] Handled conservative lasting Audio Focus loss, verified explicit recovery and callback-order fail-closed.
- [x] Preserved existing background/notification/service authority and privacy boundaries.
- [x] No build/test/APK/Actions, no AC PASS, no PR/merge/Issue close.
- [ ] P05/P07 unify physical route epochs and delayed IO receipt ordering.
- [ ] P09 wire actual Voice cancellation.
- [ ] P13 handset evidence and deferred technical/OEM QA.

**Disposition: P04 = PASS_SOURCE_ONLY_NOT_COMPILED_OR_TESTED. Next P05.**
