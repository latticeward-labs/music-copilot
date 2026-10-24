# Issue #8 — P08 Unified Media3 Service, Notification and UI Snapshot Synchronization

**Date:** 2026-10-23  
**Repository:** `latticeward-labs/music-copilot` · **Branch:** `feature/8-audio-focus-route-recovery` · **Issue:** [#8](https://github.com/latticeward-labs/music-copilot/issues/8) OPEN  
**Gate:** `PASS_SOURCE_ONLY_NOT_COMPILED_OR_TESTED`. **P09 NEXT.**  
**Owner's execution constraint:** P00–P12 **no** compilation, Gradle, lint, tests, GitHub Actions or APK. P13 **one cumulative** debug APK + user manual QA. All runtime criteria remain unverified.

## 1. Original source and accepted ownership

Uploaded `MSM2(1)(1).rar` SHA-256 **`0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`**, matching accepted P00–P07 12-PDF inventory. Governing baseline: original FR-03 / UF-11 / SYS-07–09, [ADR-0002](../adr/0002-service-owned-media3-playback.md), [Issue #16 Runtime Policies §8](../architecture/RUNTIME_POLICIES.md), [P01 execution contract](ISSUE_8_PHASE1_GITHUB_EXECUTION_CONTRACT.md), [P07 receipt/race audit](ISSUE_8_PHASE7_EVENT_ORDERING_AND_RACE_CONTAINMENT_AUDIT.md), and merged Issue #7 Media3/Controller source.

**Only** `MusicPlaybackService` owns ExoPlayer, MediaSession, system playback/foreground notification and the authoritative queue. Internal controllers, Full Player and MiniPlayer consume the same immutable `PlaybackSnapshot`; no Activity Player, extra MediaSession, separate notification manager, optimistic UI-Playing flag or second route/focus owner is introduced by P08.

## 2. P08 baseline defects identified from real current source

1. **Buffering vs safe pause:** `Media3SnapshotProjector` previously checked `Player.STATE_BUFFERING` before `playWhenReady`, Media3 transient focus suppression or the Issue #8 route/manual latch. This could report UI **Buffering** instead of **Paused** during an interruption, even when new audio must be withheld.
2. **Revision inflation:** the projector unconditionally incremented `revision` on every service refresh, including same state, position, queue, duration and error. This produced spurious session-extra updates and impeded meaningful controller revision ordering.
3. **Late response rollback outside StateFlow:** `PlaybackControllerConnection.refreshSnapshot/execute` correctly rejected older `revision` publications through `ControllerSnapshotRelay`, but still returned `AppResult.Success` holding the **stale response snapshot** to its caller. P08 ensures that callers receive the latest accepted relay snapshot instead.
4. **Detached controller visual truth:** `PlayerUiState.isPlaying` previously read only last cached `snapshot.playbackState`; disconnected UI could still label its last observation Playing despite having no valid controller connection. P08 requires `ready`.
5. **Independent flow collector ordering:** PlayerViewModel observes connection and snapshots in separate coroutines. P08 reads the **current pair** from the one gateway relay upon each flow update, rather than combining new status with an obsolete captured snapshot value.
6. **Stale failed command notice:** late failure from a pre-interruption UI command could display a generic error over a newer focus/route snapshot. P08 accepts transient request failures only for the same connection-ready current revision; authoritative `lastError` still follows the service snapshot.

These are **source-observed correctness risks**, not proof of an app/OEM defect reproduced on a phone.

## 3. Exact files modified and how

| File | Committed source change |
|---|---|
| [`Media3SnapshotProjector.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/media3/Media3SnapshotProjector.kt) | `capture(player,error,safePauseRequired=false)` derives status from actual `Player`. Priority: real terminal Error; Ended; empty Idle; **route/manual latch, `!player.playWhenReady` or non-NONE focus suppression -> Paused**; actual isPlaying -> Playing; authorized actively-buffering -> Buffering. Monotonic `revision` now advances on complete snapshot changes only; identical observations return the same immutable snapshot/revision. Queue revision continues to advance only if logical queue/selected occurrence changed. Existing two-argument callers remain source-compatible via default value. |
| [`MusicPlaybackService.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/MusicPlaybackService.kt) | `publishSnapshot()` samples the one actual Player and the existing route/manual/BT safety gate **together on service Main**; returns early when nothing changed, else sets one complete `session.setSessionExtras(wrap(next))` then checkpoints that same `next`. Existing Media3 `onEvents` calls the guard's same-Player pause **before** publication. Media3's own notification provider/lock-screen metadata and foreground ownership remain unchanged. |
| [`PlaybackControllerConnection.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/controller/PlaybackControllerConnection.kt) | On refresh and execute completion, publish the response via existing revision-aware `ControllerSnapshotRelay` then return `relay.snapshot.value`, never a stale acknowledged snapshot after a newer focus/route event. Initial connection, `onExtrasChanged` and reconnect keep the same one Media3 Controller. |
| [`PlayerViewModel.kt`](../../app/src/main/java/com/latticeward/musiccopilot/feature/player/PlayerViewModel.kt) | `isPlaying` only when `connection=Ready` **and** service snapshot Playing. On independent connection/snapshot flow updates, re-read both latest gateway values so UI never pairs a new Ready with an old playing payload; suppress a stale failed command notice when authoritative snapshot revision changed. Full Player and MiniPlayer already use this one PlayerUiState. |
| [`ControllerSnapshotRelay.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/controller/ControllerSnapshotRelay.kt) | **Unmodified:** already enforces one full snapshot before Ready, drops duplicate/older revisions and explicitly resets only after a verified new connection. |
| [`FullPlayerScreen.kt`, `MiniPlayerBar.kt`](../../app/src/main/java/com/latticeward/musiccopilot/feature/player/) | **Unmodified:** already render `PlayerUiState` and its shared `playbackStatusLabel`, never own queue/clock/player. Source-aligned P08 status changes flow to both. |

## 4. Source-authoritative observation flow

```text
One Android OS Audio Focus + one Media3 ExoPlayer (service Main)
   |             |                  |
focus suppression / route loss / urgent manual pause / actual queue/position
   |             |                  |
   +-- P02–P07 safety latch + same ExoPlayer pause
   +-- Player.Listener.onEvents() / service publishSnapshot()
                  |
Media3SnapshotProjector.capture(player,error,safePauseRequired)
   - one immutable playback state, queue/currentItem, position, error, revisions
   - same observation -> same revision
                  |
MusicPlaybackService.confirmed
   +-- same MediaSession.setSessionExtras(KEY_SNAPSHOT, complete Bundle)
   +-- same paused checkpoint after snapshot
   +-- Media3 native notification + lock screen: SAME actual session/player
                  |
PlaybackControllerConnection (ONE application MediaController)
   +-- ControllerSnapshotRelay (older revisions discarded)
                  |
PlayerViewModel (one state derived from Ready + latest service snapshot)
                  |
Full Player + MiniPlayer + any future Driving consumer
```

**No phantom notification:** the code does **not** install a second notification provider or synthesize system playback state. The OS controls use the actual Media3 session and Player. A paused focus/route state may include `playWhenReady=true` internally during OS-managed transient suppression, but `Player.isPlaying=false`; service Snapshot now projects Safe Paused instead of mislabeling Buffering/Playing. P04's four-guard denied-resume veto and the P07 route receipt fence remain active.

## 5. Expected status/revision parity matrix (NO tests executed)

| Source / controller situation | Full/Mini expected status | Session/notification source | QA boundary |
|---|---|---|---|
| Genuine audible ExoPlayer Playing, focus/route valid | Playing | Same Player/Session indicates active playback | M33–M39 |
| Media3 transient focus suppression with playWhenReady still true | Paused; no optimistic Playing | Actual Media3 Player suppressed | M06–M17 |
| Route removed/noisy while buffer pending | Paused, same queue/index, no new audio | Same ExoPlayer paused by service Main | M18–M30 |
| A2DP/BLE reconnect with auto-Play quarantine | Paused; explicit user Play required | Media3 continues Paused, no separate notification | M27–M32 |
| User manual Pause then old focus gain | Paused, no later resumed Playing | Session remains same Player | M11–M17 |
| Actual buffering AND playWhenReady true, focus available, route valid | Buffering | Same Media3 Player BUFFERING | M33–M39 |
| MediaStore/decoder terminal error | Error from sanitized AppError | Native Media3 player error/paused state | M39 |
| Nonfatal skipped MediaItem while successor actually Playing | Playing + recoverable notice | Real successor Player not fabricated as Error | M33–M39 |
| Duplicate service refresh with identical sampled fields | No revision change, no extra session-extras write | No fake Player command or notification change | T07–T09 |
| Controller reconnect with old revision history | One complete new service snapshot before Ready | Same service MediaSession, no new Player | M33–M39 |
| Old refresh/execute response after newer route loss | Old response rejected by relay; caller receives newest accepted snapshot | Same newest session Player | M40–M43 |
| UI disconnected after previously Playing | Disconnected status, Playing flag false | Physical background playback may legitimately continue under Media3 | M33–M39 |

## 6. Driving scope and transparent gap

The repository's `app/.../feature/driving/PackageBoundary.kt` is **still only a package reservation**, not an active Driving UI. Therefore P08 cannot truthfully claim **Driving-screen visual parity already demonstrated**; it provides the **same PlaybackGateway/PlaybackSnapshot contract** for future Driving implementation (tracked separately with Driving/Voice UI issues). Neither Android Auto/hardware-car integration nor a Driving composable is invented or marked tested. If the original AC05 requires physically rendering a Driving surface, it remains **UNVERIFIED** until that feature exists and P13/appropriate future issue validation occurs.

## 7. Open dependencies / no-build handoff

- **P09:** wire actual app-scoped Voice coordinator cancellation to focus/route effects; no Voice UI or STT/TTS runtime result asserted here.
- **P10:** privacy-safe typed `AppError.Interruption` and disabled-identifier diagnostics.
- **P11:** Android 10/12/14+/15+, notification/foreground behavior, Bluetooth/vehicle/OEM and accessibility consistency.
- **P12:** author revision, controller reconnect, duplicate refresh, suppression/Buffering, stale command response, Home/Mini/Full/Driving projection technical test sources; **do not run**.
- **P13:** build **one cumulative debug APK** after P12; user manually performs M01–M45. T01–T15 technical execution requires separate authorization. No independent Pixel/Samsung/Xiaomi/parked-car evidence exists yet.

## 8. P08 completion declaration

- [x] Verified uploaded RAR identity, GitHub Issue #8, original #7/ADR service ownership.
- [x] Fixed source-level focus/noisy/route Safe Paused state priority.
- [x] Preserved Media3 notification and system playback authority, with no second notification.
- [x] Published complete changed-only snapshot with monotonic revisions and consistent queue token.
- [x] Prevented a delayed controller response from returning a stale success payload.
- [x] Used Ready and latest gateway snapshot together for Full/Mini presentation.
- [x] Explicitly recorded Driving UI placeholder and unverified parity.
- [x] No compilation/Gradle/test/Actions/APK/PR/merge/Issue closure.
- [ ] Future Driving UI integration, independent technical tests and P13 owner-phone proof.

**P08 = PASS_SOURCE_ONLY_NOT_COMPILED_OR_TESTED. P09 NEXT.**
