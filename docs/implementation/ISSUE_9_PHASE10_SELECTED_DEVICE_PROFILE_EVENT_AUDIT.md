# Issue #9 — P10: selected-device Bluetooth audio-profile connection gate

**2026-10-24 — PASS_SCOPED_SOURCE_ONLY_24_JUNIT_AUTHORED_NOT_RUN — P11 NEXT.**

## Source lock and exact scope

- User-supplied `MSM2(1)(1).rar` SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8` matched the P00–P09 source baseline.
- PRD FR-04 manual Driving (P0), FR-07 selected-device *suggestion* (P1); native Android 10+ local-first MVP; UF-07/UF-12, HF-10/CE-20, GS-04/05, and ADR-0002 service-owned Media3 audio; no automatic entry/playback/mic, streaming, Android Auto, cloud or pairing.
- **P10 promises only a source-derived eligible connection/verified reconnect event. P11, NOT P10, owns the nonblocking Enter/Not now suggestion, explicit user choice and suppression.** No P10 changes to NavHost Driving callback, PlayerViewModel, Media3 service, focus/route recovery, external media controls, notification or Voice coordinator.

## Committed production implementation

| File | Concrete contract |
|---|---|
| `core/application/bluetooth/SelectedBluetoothConnectionGate.kt` (new) | Pure state/status and one-shot versioned `connectionEventId`. Explicit `NoSelection`, `PermissionRequired`, `BluetoothOff`, `Unsupported`, `DeviceUnpaired`, `WaitingForProfiles`, `Disconnected`, `ConnectedA2dp`, `ConnectedHeadset`, `Unavailable`, `Inactive`. Already connected at foreground startup is a **baseline**, not a new suggestion. Only a previously verified **Disconnected → selected A2DP/Headset connected** with an actual matching profile event creates a new edge. ACL/profile-proxy arrival or arbitrary refresh alone never creates one. Same selected connection duplicate broadcasts do not increment the edge. Invalid selection, revocation, unpair, stop and failures remove live eligibility. Never exposes name/MAC/salted token in state. Stale older-token observations are ignored rather than overwriting new selection. |
| `app/platform/bluetooth/AndroidSelectedAudioProfileMonitor.kt` (new) | Lifecycle-bound receiver for `BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED`, `BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED`, adapter power, bond and ACL events; main-thread profile callback processing; private Android `BluetoothProfile.ServiceListener` + `BluetoothAdapter.getProfileProxy(A2DP/HEADSET)`; **fresh** already-bonded audio-class whitelist queried from existing P09 `AndroidPairedAudioDeviceSource` with fresh `BLUETOOTH_CONNECT` check. Broadcast device must match the private selected salted token before its event is considered a profile-connect pulse, and that pulse is only a trigger to query **actual** `BluetoothProfile.connectedDevices`. Generic ACL, stale event extras, remembered MAC and bonded-only do **not** authorize eligibility. Results remain false-closed if profile proxy not ready/permission revoked/OEM errors. A coalesced 180ms read and at-most-2s matching event pulse avoid rapid-broadcast races and indefinite replay; generation + sequence + cancelled coroutine prevent async old result resurrection. |
| `app/platform/bluetooth/SelectedBluetoothConnectionCoordinator.kt` (new) | Exactly one app-owned preference observer (existing local DataStore, no new storage). `combine(preferences, loadState)` verifies `Ready` and current `v1` salted hint; **starts monitor only while Activity foreground AND a saved valid selection exists**. On selected preference removal/change or lifecycle stop, old receiver, proxies, jobs and eligibility are invalidated immediately. Exposes `StateFlow<SelectedAudioConnectionState>` as the future P11-only read seam. No UI/notification command or privileged device identifier exposed. |
| `app/di/ApplicationBindings.kt` (narrow modification) | Owns the one coordinator on `applicationScope`. `onUiStarted()` begins foreground eligible monitoring, `onForeground()` safely rechecks OS permission/radio/profile without changing service playback, `onUiStopped()` and `close()` release receiver, profile proxy and state. No duplicate MediaSession, no changed playback gateway and no background Bluetooth monitor. |
| `app/MainActivity.kt` (narrow modification) | `onStart()` calls `bindings.onUiStarted()`; existing `onResume()` → `bindings.onForeground()`, `onStop()` → `bindings.onUiStopped()` from #7/#8 is retained. No new manifest exported receiver, permission prompt, Activity start, navigation, auto-play or mic activation. |
| `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue9Phase10SelectedBluetoothEventGateTest.kt` (new) | **24 pure JVM JUnit tests AUTHORED / NOT RUN**: P09 salted token startup guard, first connected baseline, real A2DP/HFP edge, reconnect increment, dedupe, ACL/proxy/refresh cannot create edge, permission/bond/radio invalidation, selected token change/stale old callbacks, Activity stop/reopen, no raw identifier in UI state. All MACs are synthetic. |

## Invariants and edge matrix

| Android observation | Expected P10 state |
|---|---|
| No selected saved audio token, DataStore not Ready or UI background | `Inactive/NoSelection`, monitor is stopped; no UI action |
| Android 12+ lacks fresh `BLUETOOTH_CONNECT`, or permission revoked | `PermissionRequired`/no eligible offer; Android 10/11 use normal legacy paired-device permission |
| Bluetooth adapter off/unavailable | `BluetoothOff/Unsupported`; no forced enable, no route changes |
| Selected address no longer in freshly bonded allowed audio list | `DeviceUnpaired`; previous preference remains locally removable, never treated connected |
| ACL connected only, unrelated device profile event, paired-only | No selected-audio suggestion edge, whatever the apparent transport |
| Current selected item in actual `A2DP.connectedDevices` or `HEADSET.connectedDevices` | `ConnectedA2dp/ConnectedHeadset`; this proves a profile connection, **not** that Media3 is routed to it |
| Current selected profile connected when UI first starts/returns from background | Baseline connected status, **no new event** to suggest automatically |
| A prior confirmed `Disconnected`, then selected matching profile broadcast and fresh profile proof | One `connectionEventId` advance + one transient `canOfferEntry` pulse, for P11 to consume |
| Duplicate connected broadcasts or A2DP→Headset change while still connected | Same event ID, no new pulse |
| Connected→Disconnected, then newly confirmed profile connection | New event ID, still no automatic navigation/playback |
| Unpair, preference Remove, permission revocation, OS Bluetooth off, UI stop, stale callback | Invalidate live eligibility/receiver, never wake/reopen the app |

**Privacy:** Actual MAC/name is kept only in the P09 active paired-memory list or Android system API callback; never in P10 UI StateFlow, log, DataStore mutation, telemetry, GitHub report, event payload or screenshot. Saved value is already the P09 versioned salted private matching hint. No full audio route identity or contact/device catalog is requested.

**Focus/route caveat:** Audio profile connected `HEADSET`/HFP may support voice-call routing but is NOT proof that Media3/music is actively routed to it. P10 never changes route or music and does not assert current playback device; P11 may narrow suggestions to suitable profile conditions based on UX safety review. Android/phone OEM profile behavior needs device validation.

## Authored-only QA / unchanged safety gates

- [x] GitHub source readback and RAR hash; P09 DataStore token/paired-source contracts re-used.
- [x] Main/foreground lifecycle start and background stop, deduped event policy, checked profile callbacks + permission + paired matching implemented.
- [x] Reconnect broadcasts are not trusted without fresh paired and audio-profile evidence; ACL is verification-only.
- [x] No new background service, manifest permissions, scan/location, BT pairing/connect, app Activity launch, autoplay, voice capture or Media3/focus/notification code.
- [x] 24 pure JUnit source test methods committed; **ZERO executed**.
- [ ] Gradle Kotlin compile/JVM tests, Android/instrumentation, Lint, OEM Bluetooth/vehicle/device, CI/Actions, emulator, phone: **NOT_RUN**.
- [ ] All 14 original product AC **NOT_VERIFIED**; 46 owner M tests **NOT_RUN**; five technical groups **NOT_RUN**; APK **NOT_BUILT**; no PR, merge or Issue close.
- [ ] P11 suggestion/accept-decline UI, P12 integration/traceability source audit, P13 single cumulative build-only APK + owner phone tests remain.
- [ ] Physical callback order, Android 10/12/14/15 OEM profile semantics, 2s pulse window and HFP-only routing remain runtime **NOT_VERIFIED** (source-only phase).

**Disposition:** P10 scoped implementation source complete and reviewed, P11 NEXT; no claim that tests passed or the app compiled.
