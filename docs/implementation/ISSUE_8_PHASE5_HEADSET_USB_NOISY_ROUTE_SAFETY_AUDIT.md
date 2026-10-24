# Issue #8 — P05 Headset / USB / Becoming-Noisy Route Safety

**Date:** 2026-10-23  
**Phase:** P05 (wired, USB, personal-output removal / Media3 becoming noisy)  
**Execution branch:** `feature/8-audio-focus-route-recovery`; target `main`; issue [#8](https://github.com/latticeward-labs/music-copilot/issues/8) remains OPEN  
**Result:** **PASS_SOURCE_ONLY_NOT_COMPILED_OR_TESTED** — source changes committed, **0 Gradle/build, 0 test execution, 0 APK, 0 deliberately triggered Actions**. **P06 NEXT.**  
**All release evidence:** AC01–AC12 = UNVERIFIED; M01–M45 = NOT_RUN; T01–T15 = NOT_RUN. No claim of audible no-leak proof, device tests or PR merge.

## 1. Authority and exact source baseline

- Rechecked the **current user attachment** `MSM2(1)(1).rar` SHA-256 **`0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`**, matching P00–P04. RAR metadata enumerates **12 PDFs**: ADR-0001 through ADR-0006 plus PRD, MVP, architecture, technical, and two user-flow documents. The validated source PDFs/requirements previously reviewed in [P00](ISSUE_8_PHASE0_SOURCE_AND_READINESS_AUDIT.md) and [P01](ISSUE_8_PHASE1_GITHUB_EXECUTION_CONTRACT.md) remain the accepted baseline.
- Authority: original FR-03, ADR-0002 **service-owned Media3**, #16 [Runtime Policies §8](../architecture/RUNTIME_POLICIES.md), [UF-11](../user-flows/UF-11_INTERRUPTION_AUDIO_ROUTE.md), [TS-04](../user-flows/evidence/TS-04_AUDIO_FOCUS_POLICY.md), P02 pure [`InterruptionReducer`](../../app/src/main/java/com/latticeward/musiccopilot/playback/interruption/InterruptionPolicy.kt), P03 Media3 observer and P04 conditional focus recovery.
- Issue #7 already supplied the sole MediaSessionService, ExoPlayer `setHandleAudioBecomingNoisy(true)`, Media3 Audio Focus `setAudioAttributes(..., true)`, `ServiceAudioRouteMonitor` on the Main Handler, `PlaybackInterruptionGate`, serialized `PlaybackCommandProcessor`, and Safe-Paused restore. **Do not duplicate Player, MediaSession, Audio Focus manager, service or URI store.**
- Initial P05 discovery: the real service previously ignored a removed personal sink if `player.mediaItemCount == 0` or `playbackState == STATE_IDLE`. Consequently a suspended initial `ReplaceQueue` MediaStore verification could complete **after** a disconnected headset/USB device and start sound through speaker with an unchanged route generation. Media3 noisy was already latching but could independently increment an extra generation for the same physical removal. This is a source-level P0 hazard, not a measured field leak.

## 2. Exact P05 source changes

| File | Executed source change |
|---|---|
| [`MusicPlaybackService.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/MusicPlaybackService.kt) | New **`handlePersonalOutputDisconnected()`** owned by the existing service. Both Android removed-sink callback and Media3 `PLAY_WHEN_READY_CHANGE_REASON_AUDIO_BECOMING_NOISY` call it on Main. **No Player/mediaItemCount/IDLE guard** before raising route epoch. First signal invalidates `restoreGeneration`, `recoveryGeneration` and decoder attempt budget; notifies P02 policy through focus adapter; safely pauses the same Player if actually playing/preparing; publishes authoritative existing snapshot. |
| [`PlaybackInterruptionGate.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/PlaybackInterruptionGate.kt) | `onHeadsetOrBluetoothLost(): Boolean` now returns **true for first route-loss signal of the current explicit-play episode** and **false for duplicate signals**. It increments `routeGeneration` before asynchronous IO can complete, even while Player is empty/idle. `requiresExplicitResume` remains true until a later verified deliberate Play; no reconnect-only resume. Saturating epoch prevents wrap/reuse; exhaustion remains fail-closed. |
| [`Media3FocusObservationAdapter.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/Media3FocusObservationAdapter.kt) | New `onPersonalOutputDisconnected()` forwards exactly one typed **`RouteDisconnected`** to the P02 reducer, invalidating any pending focus-resume candidate/epoch and clearing in-flight verified Play intention. A later verified deliberate Play can mark the newly user-selected output logically validated with `RouteValidated`; this is **not** a physical BT reconnect detector (P06), and a media-key auto-Play must **not** be treated as verified (P06/P07). |
| [`ServiceAudioRouteMonitor.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/ServiceAudioRouteMonitor.kt) | Removed-sink classification remains via public `AudioDeviceInfo.isSink` and personal-output type allowlist (wired headset/headphones, USB headset/device/accessory, A2DP/SCO/BLE/hearing aid). One removal callback containing multiple sinks creates **one** safety signal, regardless of Player queue. Closed/unregistered observer ignores late Android callbacks; no endpoint names, addresses, device IDs or Bluetooth permissions introduced. |
| [P05 source audit](ISSUE_8_PHASE5_HEADSET_USB_NOISY_ROUTE_SAFETY_AUDIT.md), [P01 traceability](ISSUE_8_PHASE1_TRACEABILITY_INDEX.json), [QA ledger](ISSUE_8_QA_RESULTS.json), [execution plan](ISSUE_8_PHASED_EXECUTION_PLAN.md), [Issue #8](https://github.com/latticeward-labs/music-copilot/issues/8) | Evidence/phase tracking only; original ACs are not checked based on source presence. |

### Exact route-loss execution order on service Main

```text
A. Android AudioDeviceCallback onAudioDevicesRemoved()
   OR
   Media3 Player.Listener onPlayWhenReadyChanged(false, AUDIO_BECOMING_NOISY)
           |
           v
B. MusicPlaybackService.handlePersonalOutputDisconnected()
           |
           +-- PlaybackInterruptionGate.onHeadsetOrBluetoothLost()
           |      first event -> routeGeneration++, requiresExplicitResume=true
           |      same episode duplicate -> return false, no extra generation
           |
           +-- [first event] restoreGeneration++        (invalidate cold restore & in-flight queue)
           +-- [first event] recoveryGeneration++       (invalidate pending decoder retry)
           +-- [first event] focusObserver.onPersonalOutputDisconnected()
           |         -> P02 RouteDisconnected
           |         -> routeValid=false, pendingResume=null, focus episode fenced
           |
           +-- service's *same* ExoPlayer.pause() if needed
           +-- service publishes real PlaybackSnapshot via same MediaSession
```

Both Android route callbacks and ExoPlayer application listener are already Main-looper sources. The protection runs outside any paused `queueEngine.prepare`/MediaStore IO coroutine and does **not** wait for that coroutine to resume. The existing `ReplaceQueue` and `SelectQueueIndex` post-IO checks compare `routeGeneration` to the start stamp; existing cold restore and forward recovery paths also check generation. Therefore the first new P05 removal while a queue preparation is suspended changes its generation **even if the Player was empty and `STATE_IDLE`**.

### Idempotence scope and conservative active-route classification

- Noisy and device-removal callbacks for the **same** disconnected personal output are folded into one **armed safety episode** (`requiresExplicitResume=true`). An explicit accepted Play closes that episode; a later removal starts a new generation. During the armed interval additional removals are conservatively treated as part of the same no-resume period. P07 must inspect rapidly overlapping *new* intent/route event provenance rather than assuming OS callbacks always arrive in one order.
- Public `AudioDeviceCallback` reliably supplies removed endpoint **categories**, **not** universally reliable currently-routed media sink identity. A non-active connected USB/Bluetooth device removal may produce an extra safe pause. Prefer **fail-closed** over potentially routing music to the loudspeaker when output attribution is uncertain. Do not log device names or MAC addresses. P06 can refine available route validation only with public unprivileged APIs and explicit user intent.
- A manual fresh user Play **after** route loss may intentionally choose the new Android-selected output (including built-in speaker). Merely plugging in a headset, reconnecting Bluetooth, removing suppression or restoring a process **never** implies user consent to restart.
- The existing service explicitly reports same-Player, Player-derived playback state. This P05 path does not create a competing MediaSession, queue, Audio Focus requester, volume manager, wake listener or new UI state.

## 3. Acceptance/risk traceability and source-only evidence

| P05 contribution | Source evidence | Still pending |
|---|---|---|
| R08-02 idle/empty Player route-loss generation hole | `handlePersonalOutputDisconnected()` unconditionally calls route gate; `restoreGeneration` + `recoveryGeneration` increment on first event | **P07** validate receipt-time command epochs/cancellation of commands queued before dispatch |
| R08-04 duplicate noisy + removed device | Route gate returns Boolean; only first signal commits route-generation/focus-policy loss | **P07** rapid alternation, device events and intermediate user intent |
| AC03 immediate headset/USB/BT Safe Paused | One high-priority service Main callback, same ExoPlayer.pause and snapshot | **P13 M18–M23, M27–M30 / T05–T06** physical/technical verification |
| AC04 no unexpected phone-speaker route | Source rejects stale IO candidate and automatic focus resume; no reactivation at reconnect | Physical audible no-leak under actual device/OEM timing is **UNVERIFIED** |
| AC06 reattachment no auto-resume | Safety latch survives removal and blocked until verified Play | **P06** Bluetooth A2DP/BLE/reconnect/AVRCP classification |
| AC07 race/idempotence | Duplicate signals while gate is armed no longer increment epoch twice | **P07** rapid simultaneous route+focus/command receipts |
| AC08 Voice cancellation | Pure P02 reducer emits `CANCEL_VOICE_FOR_ROUTE` | **P09** wire app-scoped VoiceSessionCoordinator; actual cancellation **NOT** done |
| AC05 UI/MediaSession snapshot parity | Existing `publishSnapshot()` from real Player retained | **P08** system/notification/full/mini/driving parity |

## 4. Important limitations and downstream dependencies

1. **API/OEM timing:** Media3 becoming-noisy event and `AudioDeviceCallback` are not guaranteed before all speaker samples on every manufacturer. Source-only inspection **cannot** claim measurable zero-speaker leakage or correctness on Honor/Pixel/Samsung/Xiaomi. Field testing is reserved for the P13 owner APK. If actual leakage is heard, mark FAIL and halt release.
2. **Monitor registration errors:** Some OEMs restrict `registerAudioDeviceCallback`; the existing monitor catches runtime registration exceptions and keeps Media3 noisy handling as fallback. Neither a fallback nor a callback proves every hardware configuration.
3. **No active-route identity inference:** `AudioDeviceInfo.type` categorizes disconnected devices but does not prove which was playing. Conservative pause can be overinclusive; acceptable pending P06/P11.
4. **Bluetooth and media-key authority:** This phase does **not** authorize head-unit reconnect `Play`. P06/P07 must stop automatic AVRCP attempts that could be misclassified by earlier generic `onExplicitUserPlaybackRequest()` paths.
5. **Voice safety:** Interruption reducer now records route loss and requests cancellation **in model only**; P09 must execute the effect. Do not claim STT/TTS stopped.
6. **Snapshot/UI:** existing service snapshot preserved, but cross-surface consistency is P08.
7. **Technical test source & final artifact:** P12 authors relevant test *source* without execution; P13 is the only cumulative APK handoff with 45 owner manual phone cases. T01–T15 remain NOT_RUN without separately authorized technical execution.
8. **No P05 tests:** not even compile/Gradle/lint/unit/instrumented/emulator/phone or Actions; documentation and source review only, as requested.

## 5. Completion gate — no fabricated runtime PASS

- [x] Verified owner archive identity and 12 source PDF entries; previous approved requirements/ADRs retained.
- [x] Compared Issue #8 feature branch to merged #7 source on `main`, reused existing Play/Queue/MediaSession.
- [x] Removed the service's active-media guard that skipped idle/empty Player route interruptions.
- [x] Added a single high-priority loss path for `AudioDeviceCallback` and `AUDIO_BECOMING_NOISY`.
- [x] Invalidated in-flight restore/queue/recovery generations **before** stale IO completion can continue.
- [x] De-duplicated dual route callbacks without accidental second route epoch.
- [x] Wired P02 pure reducer `RouteDisconnected` and invalidated pending focus-resume eligibility.
- [x] Kept actual Media3 ownership, privacy limits, snapshot and service/Player lifecycle.
- [x] No build, test, APK, workflow, PR/merge or Issue closure.
- [ ] P06 Bluetooth route/reconnect/media-key provenance.
- [ ] P07 rapid-event command receipt and asynchronous race/dupe hardening.
- [ ] P09 actual Voice cancellation effect.
- [ ] P13 real hardware APK, manual phone cases and independent technical proof.

**Disposition: P05 SOURCE IMPLEMENTATION COMPLETE / PASS_SOURCE_ONLY_NOT_COMPILED_OR_TESTED. P06 NEXT.**
