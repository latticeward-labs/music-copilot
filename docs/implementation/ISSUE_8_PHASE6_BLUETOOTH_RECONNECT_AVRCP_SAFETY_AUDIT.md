# Issue #8 — P06 Bluetooth Reconnect, AVRCP Play Provenance and Fail-Closed Policy

**Date:** 2026-10-23  
**Branch:** `feature/8-audio-focus-route-recovery` → eventual `main`  
**Gate:** **PASS_SOURCE_ONLY_NOT_COMPILED_OR_TESTED**. **P07 NEXT.** Issue #8 OPEN and AC01–AC12 **UNVERIFIED**.  
**Owner contract:** P00–P12 **NO** Gradle, Kotlin/JVM, lint, device or emulator tests, Actions or APK. P13 is the *single cumulative* APK/user manual-test handoff.

## 1. Input integrity, baseline and authority

- Source attachment `MSM2(1)(1).rar`: actual current uploaded file opened, SHA-256 **`0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`**; 12 RAR entries and all 12 names end `.pdf`. Same bytes as P00–P05. Refer to [P00 original PDF inventory](ISSUE_8_PHASE0_SOURCE_AND_READINESS_AUDIT.md).
- Product FR-03, [ADR-0002](../adr/0002-service-owned-media3-playback.md), [Runtime Policies §8](../architecture/RUNTIME_POLICIES.md), [UF-11](../user-flows/UF-11_INTERRUPTION_AUDIO_ROUTE.md), TS-04 and [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16) require service-owned Media3 focus, safe pause on route loss and no surprise autoplay upon output reconnect.
- [P05](ISSUE_8_PHASE5_HEADSET_USB_NOISY_ROUTE_SAFETY_AUDIT.md) already unified headset/USB/Bluetooth removed-output and noisy events, invalidated in-flight MediaStore while Player IDLE/empty, and advanced a monotonic route epoch. P06 builds on P05 instead of replacing the single ExoPlayer, MediaSession or command worker.
- Public platform constraints: [Media3 media button event handling](https://developer.android.com/media/media3/session/control-playback) and [MediaSession.Callback.onMediaButtonEvent](https://developer.android.com/reference/androidx/media3/session/MediaSession.Callback). Android can attribute a media key event to a controller/package, **but that does not prove a human pressed Play instead of the Bluetooth/head-unit/OEM synthesizing it on connection**. Generic `ForwardingPlayer.play()` receives the same method for both. An aggressive assumption of user intent would be incorrect.

## 2. Exact source changes and ownership

| Changed file | Implementation |
|---|---|
| [`PlaybackCommand.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/api/PlaybackCommand.kt) | Added `PlaybackPlayOrigin` with `TRUSTED_PRIVATE_COMMAND` default and `AMBIGUOUS_SYSTEM_TRANSPORT`. `PlaybackCommand.Play` carries optional `routeGenerationAtReceipt` for only the externally translated Play. No caller may provide trusted origin over the public MediaSession Player facade. Existing private `PlaybackWireProtocol` still encodes/decodes Play without trusting a remote serialized origin field. |
| [`ServiceSessionTransportPlayer.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/media3/ServiceSessionTransportPlayer.kt) | Same `ForwardingPlayer` / one player. `configureSystemPlaySafety` sets service-owned ingress closure before `MediaSession.Builder`. External `play()`/`setPlayWhenReady(true)` is rejected if route/focus safety latch or Bluetooth-connect quarantine is active. If accepted, stamped route generation and explicit ambiguous-system origin are attached before submitting to existing command processor. Existing `SystemTransportPolicy.translate` contract, Pause/Stop/Next/Previous/seek, allowlist and custom app controller remain unchanged. No standalone Player or MediaSession created. |
| [`MusicPlaybackService.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/MusicPlaybackService.kt) | Installs ingress policy **before** exposing the MediaSession; at `PlaybackCommand.Play` worker dispatch it independently rechecks route epoch, explicit pause/loss/Bluetooth-connect latch and normalized focus availability **before any Play or latch release**. Delayed external Play received before route removal is rejected after the epoch changes; no cached remote request may clear a newer safety pause. Acceptable private app/Voice Play retains current validation and single processor, does not silently claim Bluetooth output identity. |
| [`ServiceAudioRouteMonitor.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/ServiceAudioRouteMonitor.kt) | Added public `AudioDeviceCallback.onAudioDevicesAdded` category observation for A2DP, SCO, hearing aid and BLE sink. This does **not** claim to know active route, selected output or authenticated media-key intent. Reconnect callback **never calls Play**, changes queue/position, changes audio focus or clears route latch. It can also report already-connected endpoints during registration, so classification remains conservative. |
| [`PlaybackInterruptionGate.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/PlaybackInterruptionGate.kt) | Added `isBluetoothOutput(type)`, coarse `bluetoothReconnectedWhilePaused` and `bluetoothConnectionPlayQuarantine`. When a Bluetooth sink appears while the Player is paused/no Play requested, possible OEM auto-Play is fenced; while genuinely Playing/Play requested, a spare sink addition alone never stops playback. `onHeadsetOrBluetoothLost` keeps its P05 route-loss latch and marks quarantine; only a **fresh trusted private Play** releases it. Connections alone are never trusted to resume. |
| [This P06 source audit](ISSUE_8_PHASE6_BLUETOOTH_RECONNECT_AVRCP_SAFETY_AUDIT.md), [AC ↔ code trace](ISSUE_8_PHASE1_TRACEABILITY_INDEX.json), [QA ledger](ISSUE_8_QA_RESULTS.json) and [phase plan](ISSUE_8_PHASED_EXECUTION_PLAN.md) | State/code source review and handoff metadata; no claimed test results. |

### Contract-preserving details

- Existing `SystemTransportPolicy.translate(Action.Play,id)` still constructs default `PlaybackCommand.Play(id)` for callers/tests. The **MediaSession-only facade** wraps that result with `AMBIGUOUS_SYSTEM_TRANSPORT` + receiver-side route epoch; private in-app typed commands use default `TRUSTED_PRIVATE_COMMAND`.
- `ServiceSessionTransportPlayer(candidate) { intent -> ... }` and `MediaSession.Builder(this, systemTransport)` are kept, preserving one real Player/Session authority and the pre-existing #7 source contracts.
- Never add `BLUETOOTH_CONNECT`, `READ_PHONE_STATE`, a pairing API, `AudioManager.requestAudioFocus`, a second ExoPlayer or identifying route diagnostics just to infer intent.
- No Bluetooth endpoint names, MAC addresses, device IDs, service discovery, online catalog, cloud sync, extra MediaSession or second queue.

## 3. Critical event/command matrix — source policy, not physical evidence

| Inputs in order | Expected policy effect | Audible resume? |
|---|---|---|
| A2DP/BLE or compatible Bluetooth sink removed while Playing | P05 urgent route-loss epoch + Safe Paused + pending-IO cancellation | No |
| Bluetooth sink re-added after that loss | Coarse availability observation; route-loss latch and policy focus candidate remain invalid | **No** |
| Bluetooth arrives while app is already Paused (including initial service enumeration) | Arm conservative ambiguous-system-Play quarantine; do not mutate existing Player/queue or send Pause | **No** |
| Bluetooth added while uninterrupted current music is Playing | No interrupt solely on addition; no volume or focus change | Playback unchanged |
| OEM/head unit sends Play automatically after disconnect/reconnect | MediaSession ingress sees no provable user-intent bit; deny while quarantine/route latch is active | **No** |
| Head-unit Play received before disconnection but queued until after disconnection | Ingress-stamped `routeGenerationAtReceipt` differs at worker dispatch; reject before `onExplicitUserPlaybackRequest()` | **No** |
| Remote Play while Media3 focus permanently lost or transiently suppressed | Reject if focus policy not AVAILABLE | **No** |
| Deliberate in-app song tap or Play after route loss | Private verified command may clear route latch *after* existing queue/source/permission validation; P02 route revalidation occurs only through that command | Yes, on current OS-selected output only after explicit user action |
| Media key Play when no safety/quarantine event is outstanding and focus available | Stamp epoch and enter the existing worker, which checks it again; this does **not** prove physical button press | Possible, same as existing standard transport policy |
| Media key Pause/Stop/Next/Previous/Seek while latched | Existing transport allowlist retains non-Play controls; Pause/Stop remain permitted | No accidental new Play |
| Reconnect while old focus episode is being restored | Route-loss latch defeats auto resume; reconnect does not restore consumed candidate | No |
| App cold restore or MediaSession reconnect after prior route loss | Existing Paused-only checkpoint; no Play emitted on connection | No |

## 4. Intent uncertainty and deliberate policy tradeoff

**A Bluetooth controller's Play cannot be reliably distinguished from a user pressing the same physical button at the Media3 `ForwardingPlayer.play()` boundary.** As a result, **while route-loss/BT-connect quarantine is active, BOTH a synthesized auto-Play and a genuine Bluetooth hardware Play are conservatively refused**. The user can restart with an explicit app UI/verified internal Voice action. Do **not** mark an ignored physical-headset Play as a bug-free experience; record that user-facing limitation and validate acceptance expectations in P13.

The detection of a newly available Bluetooth endpoint is **not** proof the speaker actually routed to it. A2DP/BLE/SCO device categories and `AudioDeviceCallback` presence events are used only for a safety decision. Connecting a second unused device can cause conservative quarantine while paused; no audio output is interrupted if playback was ongoing.

**No time-based inference:** P06 intentionally does not assume “Play within N milliseconds of reconnect” is automatic or that outside this window a button press is real. Any such timing heuristic would be unsound across Android 10/12/14+/OEM and vehicle controllers.

## 5. Race, privacy, QA and follow-up boundaries

1. **P07 pending:** command-receipt route epoch is now stored on external Play and checked at dispatch, but the older internal queued ReplaceQueue/SelectQueueIndex and rapid overlapping device callbacks still require full receipt-time fencing and generation/priority analysis. P06 alone is not a universal no-leak proof.
2. **P08 pending:** single snapshot projection and system/notification/Driving UI sync remains to be audited.
3. **P09 pending:** typed P02 `CANCEL_VOICE_FOR_ROUTE` effect is not yet wired to active STT/TTS coordinator.
4. **P10/11 pending:** privacy-safe error details, API/OEM/Android Auto compatibility, accessibility and voice recovery constraints.
5. **P12 pending:** write future source tests (without running them) for BT disconnect→reconnect, auto-Play-on-connect, Play while paused, arrival while genuinely Playing, remote Play queued before loss, focus lost + remote Play, user Play/manual resume, duplicate device-added callbacks and no private transport regression.
6. **P13 only:** build one cumulative APK of all phases and hand owner M01–M45 checklist. Relevant M21–M32 (BT/headset), M40–M43 rapid events, T05–T08; owner Honor handset, representative other OEM/parked vehicle device coverage are not claimed. Do not turn the original 12 ACs green without measured/manual evidence.
7. **Unverified caveat:** GitHub source inspection alone cannot establish successful Kotlin compile, device callback order, Android media-button key timing, session threading, actual BT active route, safety on every OEM, or physical no-speaker leakage.

## 6. P06 documentary completion gate

- [x] Matched actual attachment archive SHA-256 to P00/P01 and enumerated 12 PDFs.
- [x] Reused #7 Media3 transport façade, #8 P02 focus reducer and P05 urgent route-loss gate.
- [x] Captured service-side provenance on system media-key Play without trusting external controller-supplied data.
- [x] Rejected ambiguous external Play at both receipt and serialized worker execution while route/focus/BT quarantine is active.
- [x] Recognized Bluetooth-added sink categories without automatic Player start or latch release.
- [x] Preserved queue, playback position, session/foreground and non-Play transport commands.
- [x] Documented conservative genuine-remote-button rejection tradeoff and test scenarios.
- [x] No build, tests, APK, Actions, PR/merge, Issue close or fabricated acceptance.
- [ ] P07 queued app command races and callback ordering.
- [ ] P13 real cumulative APK + owner phone and independent technical evidence.

**Final gate: P06 SOURCE COMPLETE / PASS_SOURCE_ONLY_NOT_COMPILED_OR_TESTED. P07 NEXT.**
