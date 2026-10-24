# Issue #8 — P11 Android 10–16/OEM, Foreground, Bluetooth and Accessibility Source Hardening

**Date:** 2026-10-24  
**Branch:** `feature/8-audio-focus-route-recovery` · **Issue:** [#8](https://github.com/latticeward-labs/music-copilot/issues/8) OPEN  
**Result:** **PASS_SOURCE_ONLY_NOT_COMPILED_OR_TESTED** · **P12 NEXT**  
**No-build rule:** P00–P12 strictly forbid app/Gradle/Kotlin compilation, lint, instrumentation, emulator/handset tests, automated unit tests, Actions, or APK. P13 assembles one cumulative APK for owner-run M01–M45. Technical T01–T15 stay NOT_RUN unless actually authorized and executed. Original AC01–AC12 stay UNVERIFIED.

## 1. Input authority and checked current source

- **Actual attachment** `MSM2(1)(1).rar` SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`, 12 English original PDFs; hash rechecked on 2026-10-24. Relevant sources: PRD FR-03, MVP Week 4 device/car validation, architecture, technical documentation, ADR-0001 native Android 10+, ADR-0002 one service-owned Media3 playback, ADR-0003 local-first, ADR-0004 Voice replaceability, ADR-0006 MVP exclusions, original UF-11 and TS-04.
- GitHub branch baseline: `app/build.gradle.kts` = `minSdk 29`, `compileSdk 36`, `targetSdk 36`; uses Media3 service-owned ExoPlayer with Media3 Audio Focus and noisy handling, Main-thread `AudioDeviceCallback`, conditional transient focus recovery, P05 first-loss route generation, P06 head-unit auto-Play quarantine, P07 serialized command receipt epochs, P08 one snapshot/notification authority, P09 Voice cancel, P10 privacy-safe error code/UI. Reused all of these; no duplicate AudioManager focus or MediaSession.
- Public primary references checked 2026-10-24:
  - [AudioDeviceInfo API reference](https://developer.android.com/reference/android/media/AudioDeviceInfo): `TYPE_BLE_HEADSET` and `TYPE_BLE_SPEAKER` added **API 31**.
  - [Android 15 behavior changes](https://developer.android.com/about/versions/15/behavior-changes-15): targeting API 35+, audio focus requests **require top-app state or existing foreground service** or may fail.
  - [Manage audio focus](https://developer.android.com/media/optimize/audio-focus): Android 12+ applies OS fade/suppression; applications must not override Media3's loss/duck policy.
  - [Media3 background playback service](https://developer.android.com/media/media3/session/background-playback): `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, service declaration, Media3 notification/FGS lifecycle, API 33+ System UI metadata from MediaSession. Foreground status is NOT guaranteed just because the Java/Kotlin service instance exists.
- **Android Auto is EXCLUDED from the approved MVP** (ADR-0006). Bluetooth/A2DP/AVRCP support with ordinary car head units and three parked-vehicle field configurations is different from and must not be marketed as an Android Auto/CarPlay implementation. The current `feature/driving/PackageBoundary.kt` is still not a completed Driving UI.

## 2. Committed P11 production source hardening

| Path | Actual P11 change | Why it matters |
|---|---|---|
| [`PlaybackInterruptionGate.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/PlaybackInterruptionGate.kt) | Added explicit `Build.VERSION.SDK_INT < Build.VERSION_CODES.S` guard, centralized `isBleOutputOnApi31Plus(type)` and used it from `isPersonalOutput` / `isBluetoothOutput`. Existing wired, USB, A2DP, SCO and hearing aid categories remain without unsupported BLE constants in API29/30 branches. | API 29/30 must not depend on API31-only device categories; strict lint/API policy. Neither an unknown device type nor mere BLE connection is an implicit resume permission. |
| [`strings.xml`](../../app/src/main/res/values/strings.xml) | Added three static, concise variants of P10 guidance: safe-pause focus, unavailable focus and output-changed. | Avoid multi-sentence truncation on compact phone/head-unit UIs, large-font scaling and TalkBack navigation; no personal content/call diagnosis. |
| [`MiniPlayerBar.kt`](../../app/src/main/java/com/latticeward/musiccopilot/feature/player/MiniPlayerBar.kt) | Uses short status variants in the *existing* `playbackStatusLabel`, retains `LiveRegionMode.Polite`, `maxLines=2`, 72dp minimum bar and 48dp+ touch targets. | Full/Mini read the same service-originated Snapshot, with no UI-owned Player state or new widget needing a foreground permission. |
| [`FullPlayerScreen.kt`](../../app/src/main/java/com/latticeward/musiccopilot/feature/player/FullPlayerScreen.kt) | Adds `LiveRegionMode.Polite` semantics to the authoritative status text. Keeps longer P10 help, heading, verified Manual Resume and existing responsive large-type controls. | TalkBack can announce a changed safe-pause state without an urgent interruption; no spoken user-private metadata in new warnings. |

**Preserved without unnecessary mutation:** `app/src/main/AndroidManifest.xml`, `app/build.gradle.kts`, `ServiceAudioRouteMonitor.kt`, `MusicPlaybackService.kt`, `ServiceSessionTransportPlayer.kt` and `AndroidOnDeviceSpeechToTextEngine.kt`. They were inspected; the manifest already declares `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, `mediaPlayback` foreground service type and an exported MediaSessionService. It deliberately has **no** `READ_PHONE_STATE`, `BLUETOOTH_CONNECT`, `POST_NOTIFICATIONS`, BOOT_COMPLETED receiver or playback startup service receiver. Permission-minimal restrictions and the OS MediaSession notification path were preserved.

## 3. Android API source compatibility decision table — NOT runtime tested

| Android/API | Source behavior / hardening decision | Unverified runtime risk |
|---|---|---|
| Android 10 / **29** | Minimum supported. Classic wired/USB, A2DP/SCO and API28 hearing aid type may be classified; BLE type constants are explicitly not evaluated before API31. Standard `READ_EXTERNAL_STORAGE` grant model; Media3 service-owned player, paused restore. Local on-device STT provider intentionally **unavailable** on API29 (manual fallback). | Manufacturer audio device callback delivery, incoming calls, real headset speaker-leak/foreground. |
| Android 11 / **30** | Same API29+ compatibility path; API31 BLE categories not queried. Android privacy and background work are OS-owned; no new wake receiver/permission. | Process lifecycle, auto-Play head units, permission revocation and focus transitions. |
| Android 12 / **31–32** | BLE headset/speaker type classifications active only here and above. Android 12 system may enforce fade-out or focus suppression; Media3 remains only focus request owner. Existing on-device STT only if device actually supports it and user gave mic permission. | Actual OS suppression ordering, callback coalescing, OEM speech provider availability. |
| Android 13 / **33** | `READ_MEDIA_AUDIO` granted through existing permission flow. Media3 session-derived System UI controls; media notification exempt from requiring a separate notification permission prompt in this design, but user-visible OS notification settings may still affect display. | Media notification parity, lock screen/media button behavior, Android OEM notification policy. |
| Android 14 / **34** | Typed `mediaPlayback` foreground service and Media3 lifecycle used; no arbitrary background task promoted into service. Existing Safe Paused checkpoint and no forced autoplay after Activity recents removal. | Runtime foreground policy, phone call/duck/focus and notification state under OEM power rules. |
| Android 15 / **35** | For target35+, top-app or **already running FGS** eligibility is required for successful Audio Focus request. Media3 owns request; do **not** call AudioManager separately or claim to make background Play eligible. Service rejects stale/ambiguous system Play after route or focus loss and never treats failure as success. | Explicit Play while app is background and not FGS may be refused. Mark refusal as policy-conforming/needs manual foreground action, not a regression-free promise. Check physical device. |
| Android 16 / **36** | Target and compile version in repository. Maintain API35+ focus eligibility and foreground restrictions; no boot/other background restart path, no generated permissions or background voice start. | Android 16/OEM behavioral changes still require hardware inspection; source review is not certification. |

**Do not incorrectly claim** that merely having a `MediaSessionService` instance makes it a *foreground service*: Media3 promotes it according to real Player/notification lifecycle. A play request from a background-cold service on target35+ may fail Audio Focus before FGS elevation is possible. Never circumvent this by preemptively starting a microphone/phoneCall FGS, adding dummy playback, multiple Audio Focus owners, or spoofing actual `isPlaying`.

## 4. Manifest, lifecycle, head units and privacy checkpoints

| Topic | Source-based disposition |
|---|---|
| Foreground notification | One Media3 session and built-in notification provider, no Activity-owned NotificationCompat or independent foreground toggle. `onUpdateNotification` delegates to Media3. API33+ System UI relies on actual MediaSession metadata, not custom notification text. |
| Service activity/task removal | `onTaskRemoved` defers to existing `PlaybackTaskLifetimePolicy`; retains only genuinely ongoing, user-requested, foreground playback, not idle/failed/paused phantom services. `onDestroy` closes registered audio device callbacks **before** release, P03 focus adapter, bounded command worker/futures and Media3 runtime. |
| Audio routes | Device callback uses `isSink` and a small, privacy-safe category allowlist; old routes invalidate in-flight MediaStore work even with empty/IDLE Player. Public API alone does not provide proof of the *currently active* media output on all OEMs, so conservative pause is permitted. |
| Bluetooth reconnect / car AVRCP | A2DP/LE add while paused arms ambiguous system-Play quarantine. Manual physical head-unit key Play may be indistinguishable from synthesized Play and **also be refused** while quarantine active; user can choose a verified in-app Play. Do not promise full key-control after reconnect on every car. |
| Android Auto | **Out of MVP scope** per ADR-0006; ordinary headset/car BT play/pause controls are not Android Auto. No MediaBrowserService integration or Android Automotive tests inferred from an exported MediaSessionService. |
| Audio Focus/phone | Media3 is sole focus requester. Media3 loss/suppression events do **not** prove phone identity or guarantee that all calls are detected when Player is idle. P02 four guards, P09 Voice cancel and P10 notice are source-linked, not physical evidence. |
| Background STT/TTS | No always-on microphone or background speech capture. API31+ explicit on-device STT with permission and provider availability; Android10/11 fail closed to manual controls. No extra `BLUETOOTH_CONNECT` or `READ_PHONE_STATE`. |
| Accessibility | Minimum service UI control targets and responsive Full Player remain; Mini status abbreviated for 2-line display; Full and Mini status use polite live announcements. Narration says **audio interrupted**, never fabricates “call from X.” |
| Data/privacy | No new logs, route IDs, addresses, URI, transcripts, cloud analytics, user data in diagnostics, or contact permission. Existing quality telemetry sink is NoOp/strict allowlist in MVP. |

## 5. OEM/vehicle field matrix — planned, **NOT_RUN**

| Planned field row / equipment | Scope and safety check | Evidence now |
|---|---|---|
| **Honor x7d Android 15** (owner device) | User final P13 APK: M01–M45 including phone call/focus, wired/USB, BT reconnect, real audible route, screen off, recents and notification; include M39–M45 interruption/accessibility; exact Android patch/route documented by owner without private IDs. | NOT_RUN for Issue #8; previous Issue #4/6 phone PASS does not carry forward. |
| **Pixel Android 10/12/14+ representative** | Real Media3 focus interruption & Play/notification, USB/Bluetooth loss, foreground legality; no speaker leakage, M cases if available. | NOT_RUN / separate future device |
| **Samsung One UI / Android 12/14+** | OEM power modes, notification & MediaSession, pause/duck/route fallback, headset hardware events. | NOT_RUN / separate future device |
| **Xiaomi MIUI/HyperOS Android 12/14+** | Background/battery optimization, media-key/route event delivery and focus denied; conservative fail-closed. | NOT_RUN / separate future device |
| **Parked vehicle A — standard A2DP head unit** | Manual paired BT, start/stop on engine simulated only while parked, remove/reconnect, AVRCP auto Play vs user Play, phone interruption. | NOT_RUN; no driving |
| **Parked vehicle B — alternate OEM head unit/AVRCP profile** | MediaSession key events, background playback, UI/notification parity, volume not restored in app. | NOT_RUN; no driving |
| **Parked vehicle C — BT accessory / alternate controller** | Rapid duplicate events, route invalidation and manual resume. | NOT_RUN; no driving |

Owner is required to test **one cumulative APK in P13**, not each phase. Extra phone/OEM/vehicle evidence is desirable and **deferred** until available; do not manufacture PASS or block the owner's initial manual feedback from being logged.

### Concrete P13 handset checks most relevant to P11

- Android 15 foreground: start playback in app, lock, unlock, leave app, use notification and finally remove task; confirm no unintended sound after loss and no crash.
- Forced non-foreground/cold media-key Play: identify if OS denies focus and the app stays silent; this may be expected API35+ policy, not a test pass for full playback.
- Wired jack, USB, A2DP/BLE headset: remove while audible, then reconnect while paused. Confirm **no speaker leak** and no auto resume; use explicit in-app Play.
- Phone/focus/ducking: another app or incoming call while Music is playing and Voice is active; verify whether Android actually reports observable suppression/loss; note if call remains invisible.
- Compact/large-font + TalkBack: Full/Mini show concise safe pause status, logical heading, no truncation of essential recovery control; verify touch targets and screen-reader feedback. Distinguish not-yet-implemented Driving screen from Full/Mini.
- Rapid events: external Play queued then headset removal; late route callbacks after closing service; confirm no stale Play, no crash, no wrong notification.

## 6. Remaining risk register and source-only gate

| Risk | Pre-P13 state / owner |
|---|---|
| API31 BLE compatibility | P11 **source guard implemented**; compiled/API29/device evidence NOT_RUN → P12 authored test and P13 device. |
| Android15 FGS/focus refusal | Current Media3 service ownership compliant by design, but cold-background Play refusal and potential UI feedback NOT verified; P13. |
| 3+ vehicles / OEM coverage | Hardware not available to assistant. Documented matrix only; AC11 remains **UNVERIFIED**, T12–T13 **NOT_RUN**. |
| Android Auto | Excluded MVP; cannot be marked PASS or advertised. |
| Real phone calls/zero-speaker leak | Device-dependent, no field proof. AC03/04/12 stay UNVERIFIED. |
| Full Driving screen | Repository placeholder; no visual parity claim. |
| Compilation/lint/test-source integration | P12 static reconciliation and test-source authoring **without execution**; P13 first build. |
| Lifecycle/notification/Voice STT | Conservatively wired but no runtime proof; record real failures before release. |

- [x] Rechecked uploaded 12-PDF source and accepted ADR-0001/0002/0006 exclusions.
- [x] Reviewed min/target/compile SDK, Media3 service manifest/permissions and one-focus ownership.
- [x] Added API31 BLE constant guards for API29/30.
- [x] Kept Bluetooth presence observation separate from intentional Player Play.
- [x] Reviewed Android15/16 FGS and Audio Focus restrictions without risky second focus manager.
- [x] Shortened safe-pause status strings and added polite Full Player announcement.
- [x] Authored full Android/OEM/three parked vehicle field matrix and Android Auto exclusion.
- [x] No compilation, lint, tests, emulator/device, Actions, APK, PR/merge or issue close.
- [ ] P12 static code reconciliation + deterministic authored test source, no execution.
- [ ] P13 one cumulative APK, owner M01–M45 actual phone findings; T01–T15 still separate authorization.

**P11 disposition: SOURCE HARDENING / DOCUMENTARY COMPATIBILITY REVIEW COMPLETE, runtime validation NOT DONE. P12 NEXT.**
