# Issue #8 — Verified Planning Baseline & Outstanding Implementation Gaps

**As of:** 2026-10-23 · **Read-only/source planning audit only** · **NO testing/build/Actions, no P00 implementation acceptance claimed**.

## Attached source reconciled

Read `MSM2(1)(1).rar` (RAR5, 12 English PDFs), SHA256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`. PDF sections:
- `Music_Copilot_PRD_v1_EN_Clarified.pdf`: FR-03 — calls/headphones/audio route changes must be handled sensibly, no crashes; P0.
- `Music_Copilot_MVP_v1_EN.pdf`: Week 4 focus/calls/headset/Bluetooth edge matrix; call pause/duck and immediate BT disconnect pause.
- `Music_Copilot_System_Architecture_MVP_v1_EN.pdf`: playback lifecycle safety table specifies calls/focus, BT/noisy, route recovery.
- `ADR-0002_Service_Owned_Media3_Playback.pdf`: service-owned playback/focus, no UI authority, no unwanted phone-speaker output; replaceable Voice integration.
- `Music_Copilot_User_Flow_Phase_0_Foundation_v1.0_EN.pdf`: SYS-07/08/09, D-018/019, UF-11; focus recovery four guards, no reconnect autoplay.
- Other ADRs maintain native Android, local-first, privacy, replaceable Voice, bounded commands, no backend/account/streaming.

## Repo status reviewed (actual GitHub)

- `main` planning baseline at start `62c6115ac11af4cb2790b306f885f580b82a146b`.
- [Issue #7](../../issues/7) merged/closed via [PR #76](../../pull/76) (merge commit `c4ee45ddb543b69c97db1f93b783c97fe190e18f`) 2026-10-23. Prior final APK from different earlier SHA and Issue #7 **cannot** serve as final Issue #8 candidate. #7 owner-reported M36/T15 acceptance not independently backed by JVM/instrumentation/lint logs.
- [Issue #16](../../issues/16) accepted architecture and [Runtime §8](../architecture/RUNTIME_POLICIES.md). [ADR-0007](../adr/0007-same-process-playback-runtime-and-safe-paused-restore.md) fixes one same-process service/player and cold Safe-Paused restoration.
- `app/src/main/java/com/latticeward/musiccopilot/playback/service/MusicPlaybackService.kt`: `setHandleAudioBecomingNoisy(true)`, `setWakeMode(C.WAKE_MODE_LOCAL)`, `setAudioAttributes(USAGE_MEDIA / CONTENT_TYPE_MUSIC, true)`, route callbacks and single service-owned Player; documented guard against re-opening after removal. **No second Focus requester should be introduced.**
- `playback/service/ServiceAudioRouteMonitor.kt`: API29+ `AudioDeviceCallback` for removed output sinks and privacy-safe type check; broad category detection is safety-conservative but may pause for an unrelated removed accessory.
- `playback/service/PlaybackInterruptionGate.kt`: routeGeneration, requiresExplicitResume and explicitlyPaused flags; no complete explicit four-guard transient reducer/test seam as described by Issue #16.
- `playback/api/PlaybackSnapshot.kt`: service-owned snapshot with `AppError.Interruption` allowed.
- `playback/command/SystemTransportPolicy.kt`: Android media controls translate only allowed transport commands; no arbitrary queue/repeat/remote injection.
- `app/src/main/AndroidManifest.xml`: mediaPlayback FGS and wake lock, media read, optional microphone; no READ_PHONE_STATE or Bluetooth identification permission. Android API35+ focus request eligibility must be observed, not bypassed.

## Distinct implementation gaps for Issue #8 (to close in P02–P12)

1. Guarantee four-guard transient resume and explicit Pause precedence despite Media3's independent internal focus transitions; do not shadow/fight it with a second AudioManager focus owner.
2. Define evidence-supported policy for OS duck vs call pause on supported API/OEMs. No direct call identity or telephony permission.
3. Avoid unnecessary pauses on removal of non-active Bluetooth accessory where reliable current-route distinction is possible; preserve fail-closed safety otherwise.
4. Ensure route removal is handled as high-priority immediate pause and invalidates stale focus-gain, restore, queue preflight, delayed Play or Voice request across event races.
5. Implement explicit reconnect-only ≠ user intent policy, including the ambiguity of AVRCP autoplay from some head units; safely gate unmatched auto-play.
6. Coordinate MediaSession/notification/Full Player/MiniPlayer/Driving state atomically while paused by interruption; avoid duplicated Player, inconsistent queue, fake success.
7. Define future #10 Voice-session cancellation boundary without falsely claiming #10 Voice recognition already implemented.
8. Extend stable non-sensitive diagnostics and real physical QA/OEM/head-unit evidence separately; no user private media/device metadata.

## Execution/QA truth

- All original 12 Issue #8 AC unchecked; Issue remains OPEN. Source planning does not PASS any AC.
- P00–P12: **zero builds/tests executed by rule**; test source may be written but not run.
- Final P13 APK: **not built**; hash/run/model currently unknown and must never be invented.
- M01–M45 and T01–T15: all **NOT_RUN**; second device, other OEMs and vehicles explicitly deferred without false PASS.
- Next authorized substantive task, when requested: **P00 — phase-specific detailed source/readiness audit**. This planning baseline is input only.

## Guidance

Official Android [audio focus](https://developer.android.com/media/optimize/audio-focus) recommends Media3 `setAudioAttributes(..., true)` rather than app-managed competing audio focus. API35+ target requires foreground/top-app eligibility. Official [ExoPlayer API](https://developer.android.com/reference/androidx/media3/exoplayer/ExoPlayer) documents `setHandleAudioBecomingNoisy`. Do not mistake source API consistency for actual device QA.
