# Issue #9 — P04 WF-07 / HF-07 Playing Compose: source implementation & static audit

**Date:** 2026-10-24  
**Scope outcome:** `PASS_SCOPED_PLAYING_COMPOSE_SOURCE_ONLY` — GitHub implementation/readback, **NOT** Kotlin/Gradle/UI/device/road/vehicle validation. **P05 NEXT.**  
**Issue:** https://github.com/latticeward-labs/music-copilot/issues/9  
**Branch:** `feature/9-driving-mode-bluetooth-suggestion` (reuse P00–P03; do not merge yet).  
**Authoritative upload:** `MSM2(1)(1).rar` SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`; 12 English PDF sources P00 inventoried and directly re-hashed for P04. No separate program APK in RAR.
**Canonical GitHub specs:** [PRD FR-04](../PRD.md), [MVP P0 Driving](../MVP.md), [accepted ADR-0002/0007/0008](../adr/README.md), [WF-07 Playing](../wireframes/app-states/WF-07_DRIVING_MODE_PLAYING.md), [approved HF-07 Playing](../high-fidelity/WF-07/HF-07_DRIVING_MODE_PLAYING.md), [Screen handoff](../handoff/SCREEN_SPECS.md), [component rules](../handoff/COMPONENT_SPECS.md), [exact Compose tokens](../handoff/COMPOSE_TOKEN_MAPPING.md), [P02 service projection](ISSUE_9_PHASE2_DRIVING_STATE_AND_COMMAND_POLICY.md), [P03 navigation](ISSUE_9_PHASE3_DRIVING_NAVIGATION_AUDIT.md).

## 1. Exact delivered source files and ownership

| Path | P04 change | Authority/boundary |
|---|---|---|
| `app/src/main/java/com/latticeward/musiccopilot/feature/driving/DrivingModeScreen.kt` (**new**) | WF-07/HF-07 Compose hierarchy: title row + immediate 56dp Exit; square locally neutral ARTWORK placeholder; service-selected track title/artist; explicit service-derived PLAYING/PAUSED/NO TRACK; four ordered 64dp+ transport controls; 72dp+ Mic; one-layer and road-responsibility notes; non-color-only connection/pending/error label | **Presentation only**. No ExoPlayer/MediaSession/Room DAO/cursor, queue creation, audio route mutation, Voice capture, Android Auto, Bluetooth or cloud/telemetry |
| `app/navigation/AppNavHost.kt` (**changed**) | Replaces P03 temporary `FirstLaunchBoundary` Driving shell with `DrivingModeScreen` in existing **single** `Destination.Driving` composable; passes P02 `DrivingPresentationPolicy.project` on existing `playerUi`; passes existing `libraryRepository`, permission and Exit callback | Leaves P03 explicit entry, origin, Back=Exit unchanged. **Does not** bind a transport/Mic callback in P04; these belong to P06/P07 |
| `app/src/main/res/values/strings.xml` (**changed**) | Eight resources (Exit, ARTWORK, unknown track/artist, unavailable transport explanation, `-10s`, `Mic · Voice`, one-layer rule) and reuse of existing service/UI safety strings; removes unused P03-only temporary message | No hard-coded private track title, artist, data URI or user data |
| `feature/driving/PackageBoundary.kt` (**deleted**) | Retires exactly one obsolete structural placeholder, per its own instruction, now that real `feature/driving` Compose code exists | No further driving feature deletion |
| `app/core/application/driving/DrivingPresentationPolicy.kt` (**untouched**) | P02 remains authoritative pure three-state and command-eligibility projection | No new player/queue authority |

## 2. Approved hierarchy and exact design tokens

The approved reference is **360×800dp** (static HF PNG at 3× pixels is *not* an Android runtime screen). P04 implements an actual declarative Compose UI, **not** an image-as-screen:

1. Header labelled **DRIVING MODE**, immediately reachable **Exit** (action occurs through P03 root).
2. Subordinate 160dp square **ARTWORK** placeholder via approved CMP-08 fallback; no invented real album artwork while no verified artwork loader/URI contract is present.
3. Current song **title** and **artist** from local `LibraryRepository.get()` only for the currently selected *validated* `PlaybackSnapshot.currentItem` and granted media permission, otherwise honest “Local track”/“Unknown local artist” placeholders.
4. Explicit **PLAYING** state (or truthful PAUSED / NO TRACK when the actual gateway state differs), **not** an optimistic `isPlaying` boolean.
5. Four fixed transport affordances in order **Previous → Play/Pause → Next → -10s** (CMP-05, >=64dp target).
6. Separate **Mic · Voice** (CMP-06, >=72dp target) **disabled** until P07 actually wires checked Voice provider/permission.
7. **One layer · no typing · no lists** and already approved `first_use_road_safety` (“Pay attention to the road. Voice does not make driving safe.”). No typing, queue, nested menus, Settings or prolonged animation inside Driving.

Theme/geometry exactly reuse real repo `MusicColors`, `MusicTextStyles.drivingTitle` (28sp / 36sp), `drivingMeta` (18sp / 26sp), `drivingState` (16sp / 24sp), `MusicGrid.drivingMarginDp` (20dp), `MusicSize.drivingControlMinDp` (64dp), `drivingMicMinDp` (72dp), `drivingExitMinDp` (56dp), standard 8dp/12dp/16dp spacings and approved dark palette. `safeDrawingPadding()` accounts for status/navigation insets. A single scrollable **Column** allows content reflow without clipping at 200% font; scroll is not a library/list and there is no search UI. Four buttons remain 4-across when width/text scale permits and reflow to 2×2 for `fontScale >= 1.5` or narrow width. P08 still owns device-level TalkBack reading order, semantics focus, font 200% and contrast proof.

## 3. Data privacy, truthful states, no stale metadata

- `DrivingModeScreen` receives `DrivingPresentationState` from existing P02 projector, **never** a local playback-state setter, and a service-owned `currentItem`.
- Display-only asynchronous local `library.get(safeRef)` uses an IO dispatcher and Compose `produceState` keyed by **`safeRef` and permission**. The displayed metadata is checked against that exact current reference before rendering; outdated lookup/old track/denied permission cannot cause stale title leakage. Errors fail closed to a neutral placeholder. Coroutine cancellation is rethrown.
- The screen does not use a queue index or clicked song to select media, cannot change audio focus/route and stores no media path, raw artist/title or BT ID to analytics/storage.
- `PlaybackConnectionState.Disconnected/Connecting/RecoverableError`, `PlaybackState.Buffering/Error` and pending are projected through P02. Where present, the UI shows explicit user-readable notices; it does not label disconnected cached Playing as audible playback.
- No actual album artwork image is loaded: CMP-08 **placeholder** is deliberate. A future *verified local-only artwork loader* must be separate/permission-safe and cannot imply arbitrary network access.

## 4. Explicit P04 → P05/P06/P07 phase cut (not fake clickable UI)

- **P04 is the Playing visual Compose surface.** The new component renders the four large action surfaces with **real disabled semantics until the callback is supplied**: `enabled = control.enabled && onTransport != null`. In P04 the root deliberately passes no `onTransport`; clicking controls cannot mislead the user with no-op fake playback. P06 will supply the real service-ACK mapping, revision/pending and route guard.
- The large Mic is visible but **disabled** if the explicit Voice capability is unavailable and/or `onMic` is null; P07 owns provider, microphone permission and `VoiceSessionCoordinator` handoff; *no* “Listening” state is fabricated.
- Exit is enabled **always** even if playback is disconnected, metadata is unreadable, a request is pending, or the screen is in No Track. It calls the P03 root navigation callback, **not** a playback stop/pause command.
- P05 still owns the fully reviewed **Paused and No Track design variants**, unavailable controls, optional MediaStore-revalidated Play Last, playback error/route recovery and consistent semantics. The P04 shell renders truthful status/placeholders but must **not** be claimed as complete P05 UX.
- P08 owns actual screen-reader traversal/status ordering and accessibility test source; screenshot and source inspection are not evidence of runtime 200% reflow.
- P09–P11 optional selected Bluetooth device settings/suggestion are absent and therefore cannot auto-enter Driving.

## 5. Static audit findings and AC mapping

| AC | Partial source evidence in P04 | What is **not verified** |
|---|---|---|
| AC01 | WF-07 composition: title/artist/artwork **placeholder**, four large transport affordances, separate Mic, Exit | Live controls require P06; live Mic #10/P07; real artwork not claimed |
| AC02 | Playing state rendered from existing P02 service snapshot, fallback labels truthful | Full Paused/No Track/errors belong P05; no device test |
| AC04 | 64/72/56dp declared touch dimensions and textual state | TalkBack and contrast/200% proof P08 and owner M27–M30 |
| AC06 | Single fixed layer, no keyboard, nested menu or long list | Behavior on physical OEM/car validation P13 |
| AC13 | Road responsibility copy displayed verbatim | Owner phone M32, accessibility review P08 |

No original product AC was changed to PASS. This phase contains source only. No new `@Test` suites were required or executed in P04; existing 21 P02 and 12 P03 JUnit **remain authored but NOT_RUN**. Additional Compose/accessibility source tests are planned for P08/P12.

## 6. P04 gate truth and next phase

- [x] Original uploaded source hash and approved WF/HF/component/token references matched.
- [x] One new `feature/driving/DrivingModeScreen.kt` committed and read back.
- [x] Existing NavHost Driving route now renders the Compose screen instead of the temporary P03 navigation shell.
- [x] One service-owned snapshot projection, metadata requested by validated service item, permission-gated local lookup, stale result protection.
- [x] Four fixed ≥64dp controls, Mic ≥72dp, Exit ≥56dp and visual responsive reflow source.
- [x] Road notice, “One layer” limitation, visible state/connection/pending/error text; no Android Auto/BT detection.
- [x] No no-op **enabled** buttons: P06 real transport and P07 real Mic binding remain disabled until integrated.
- [x] Removes obsolete placeholder; no modifications to #7/#8 playback/focus/Bluetooth route/notification/AirPods fixes.
- [ ] **Not run:** Gradle/JVM/Compose test/instrumentation/Lint/emulator/Actions/app install/actual phone/vehicle. None authorized until the final owner handoff process.
- [ ] **Still pending:** real Paused/NoTrack/error UX (P05), transport command ACK (P06), Voice (P07/#10), UI/a11y test source & run evidence (P08/P12), P1 BT (P09–P11), one P13 APK and M01–M46 owner results.

**Disposition: P04 source scoped PASS; P05 NEXT.** All 14 product criteria remain `NOT_VERIFIED`, all 46 handset `NOT_RUN`, all five technical groups `NOT_RUN`, APK `NOT_BUILT`. Issue #9 remains open; no PR/merge or closure.
