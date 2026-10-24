# Issue #9 — P03 explicit Driving navigation, origin and Exit/Back: source audit

**Recorded:** 2026-10-24 · **Branch:** `feature/9-driving-mode-bluetooth-suggestion`  
**Source archive:** `MSM2(1)(1).rar` SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8` (the same 12 English source PDFs reviewed in P00).  
**Phase gate:** `PASS_SCOPED_NAVIGATION_SOURCE_ONLY_UNTESTED`. Implementation/source exists; this does **not** imply Android compile, navigation on device, UI acceptance, tests passing or Issue #9 completed. P04 NEXT.  
**Requirements:** PRD FR-04/FR-07; MVP P0 Driving + manual entry; UF-07, UF-11; WF-07 Playing/Paused/No Track; GS-04/GS-05; accepted ADR-0002/0007/0008, Issue #16 single NavHost/root origin, Issue #17 `docs/handoff/SCREEN_SPECS.md` and `docs/handoff/STATE_ACTION_MATRIX.md`. [P01 execution contract](ISSUE_9_PHASE1_EXECUTION_CONTRACT.md), [P02 state policy](ISSUE_9_PHASE2_DRIVING_STATE_AND_COMMAND_POLICY.md), [trace/evidence](ISSUE_9_EVIDENCE_INDEX.json).

## 1. Exact P03 production changes committed

| Repository source | Scope actually authored | Boundary |
|---|---|---|
| `app/navigation/DrivingNavigationViewModel.kt` (**new**) | Navigation-only `DrivingNavigationState(active, origin)`, pure `DrivingNavigationPolicy.enter/exit/destinationAfterExit`, Activity-scoped `DrivingNavigationViewModel` for explicit transitions and system-priority clearing | Origin must be Library or FullPlayer. No Player, queue, MediaStore, BT, Voice, saved media/metadata or persistence. ViewModel survives configuration recreation; fresh process initializes inactive (no cold-launch automatic Driving) |
| `app/navigation/AppNavHost.kt` (**modified**) | Activates the **existing** declared `Destination.Driving` exactly once in the existing single NavHost; adds explicit guarded entry callbacks for Home/Library and Full Player; Driving target ordered after Bootstrap/Onboarding/PermissionRecovery and Settings; shared Back + Exit callback restores recorded origin | No second NavHost, Activity, process, Player, MediaSession or Voice destination; route is never triggered by Bluetooth, queue, playback or app launch |
| `feature/library/LibraryBrowseScreen.kt` (**modified**) | Adds a visible `Driving` tap in Library header; disabled when Android media permission isn't granted; `onEnterDriving` is an **optional last callback** to preserve pre-existing call signatures | No playback/scan/permission state changes |
| `feature/player/FullPlayerScreen.kt` (**modified**) | Adds a separate visible `Driving` button in the existing header; `onEnterDriving` is an optional final callback, preserving all existing Player and queue actions | Playback model, position, queue, Back and notification behavior left untouched |
| `app/src/main/res/values/strings.xml` (**modified**) | Seven English-first labels for Enter, Exit, Driving heading, three service-observed status labels and a transparent **P03-only temporary navigation shell** | Approved full HF-07 UI must replace shell in P04/P05; no fake complete screen |
| `core/application/driving/DrivingPresentationPolicy.kt` (**narrow source correction**) | Normalizes five stray literal `\\n` sequences from P02 into real source line breaks near `SEEK_BACK_MS` so Kotlin source is lexically valid at that location | No changed Playback command semantics; no #7/#8 or service mutation |
| `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue9Phase3DrivingNavigationPolicyTest.kt` (**new**) | **12** pure source-authored JUnit methods defining allowed origin, duplicate tap handling, fresh process inactivity, return targets and fallback | **AUTHORED, NOT EXECUTED** |

`Destination.kt` already contained `Destination.Driving`; it is intentionally **not** modified. Root ownership is implemented by the new activity-scoped navigation ViewModel, separate from the startup readiness-only `RootNavigationStateHolder`. No `AndroidManifest.xml`, playback service, Media3, STT/Voice provider, Bluetooth, analytics, persistence or permission declaration was changed.

## 2. Route transition and priority contract

| Initial surface/condition | Event | Expected root state / target |
|---|---|---|
| Home/Library, `BootstrapRoute.HomeLibrary`, permission Granted, no system recovery/prompt | Tap Driving | `DrivingNavigationState(active=true, origin=Library)` → `Destination.Driving` |
| Full Player under the same valid app/system conditions | Tap Driving | `active=true, origin=FullPlayer` → `Destination.Driving` |
| Driving entered from Library | Tap Exit **or Android Back** | Return Library via existing NavHost, same playback snapshot/service state (no pause/stop) |
| Driving entered from Full Player | Tap Exit **or Android Back** | Restore `playerOpen=true` from recorded origin and return Full Player via existing NavHost (no queue rebuild) |
| Two repeated Enter taps | Second tap or illegal different-origin overwrite | Policy refuses overwrite; original non-driving return destination is retained |
| Illegal entry from Search/Settings/Onboarding/Bootstrap/PermissionRecovery/Driving | Any attempted entry | Pure policy returns unchanged inactive session, no new route |
| App route moves to Bootstrap/Onboarding/PermissionRecovery or Settings, or media permission becomes invalid | System-route/permission change | Driving session cleared; app-owned system/recovery priority takes precedence |
| Configuration change while Activity-scoped ViewModel survives | Android recreation | Driving session/origin remain ViewModel-owned; complete Player read again from existing shared `PlayerViewModel` snapshot, no new service |
| Process death and fresh Activity creation | Relaunch | New NavigationViewModel begins **inactive**; NavHost reconciliation targets approved Library or other current system route (no auto Driving). Previous playback remains service-owned |
| Unexpected or stale saved origin | Exit | Pure fallback destination is Library; no crash or app exit |
| Background BT connection/reconnection | Android route signal | No Driving entry from P03 (no Bluetooth observer or receiver was added) |

**Host reconciliation:** The existing `LaunchedEffect(target)` pops back to an already-present destination when possible, otherwise navigates with Bootstrap as root and `launchSingleTop`. On a Driving Exit, the root state is synchronously reset, then the existing NavHost reconciles to the recorded Library/FullPlayer route. Back and Exit invoke **the same** callback. Exit never calls `PlaybackGateway.execute`, creates a Player, or changes `VoiceSessionCoordinator` (full Voice overlay cancellation arrives in P07, when overlay actually exists).

**Authorization boundary:** Permission-dependent active Library/FullPlayer entry is blocked on current permission, unavailable recovery and unsupported bootstrap route; the Library entry button is disabled when permission is revoked. No direct entry from Search or Settings. **No automatic launch** on app start, permission grant, playback switch, Bluetooth connect, focus return or Voice feedback.

## 3. P03-only minimal visible shell (not final UI)

The new NavHost Driving composable reads `DrivingPresentationPolicy.project(playerUi.connection, playerUi.snapshot, playerUi.pending)` and displays **only** a truthful Playing/Paused/No Track label, the notice that controls are being prepared, and a reachable Exit button. A BackHandler maps Android Back to that same Exit callback. It reuses existing `FirstLaunchBoundary`, preserving one root NavHost. This is an **intentional intermediate step** to prove the navigation wiring in source. It does **not** claim HF-07 title/artist/artwork, four giant buttons, Mic, 200%-font/TalkBack acceptance or P0 road-ready UI has been implemented. Those are explicitly owned by P04/P05/P06/P07/P08.

No Compose preview, emulator, Android device, permission dialog, service command, Media3 start or playback test ran in P03.

## 4. P03 authored test inventory (all NOT_RUN)

`Issue9Phase3DrivingNavigationPolicyTest.kt` contains **12** JUnit `@Test` methods: initial inactive state/no auto-entry, explicit Library tap, Full Player tap, gate rejection, idempotent/overlapping entry, illegal origins, Exit from Player, Exit from Library, repeat Exit, stale origin fallback, explicit new entry after exit, and absence of media/Bluetooth/Player authority in navigation-only state.

All methods are **source authored**: 0 executed. Specific runtime QA handoff is M01–M03, M17, M18–M21 from [owner checklist](ISSUE_9_FINAL_PHONE_QA_CHECKLIST.md); Owner field tests follow **the one cumulative P13 APK**. A pure policy method is not evidence the NavHost compiled or that Android Back actually worked on a phone.

## 5. Static code-readback gate and remaining risks

- [x] Source `MSM2(1)(1).rar` SHA-256 matches P00/P01 source lock.
- [x] Exactly one active `composable(Destination.Driving.route)` in existing `AppNavHost`.
- [x] Exactly two approved explicit entry callbacks: Library and FullPlayer, both guarded by active `HomeLibrary`/permissions.
- [x] Origin and active state owned by a retained nonpersistent root ViewModel; no `rememberSaveable` Driving automatic restoration.
- [x] Back and Exit are identical navigation-only callbacks; recorded origin drives Library vs FullPlayer restoration.
- [x] Bootstrap/Onboarding/permission/settings precedence and revoke clearing authored.
- [x] No changes to service Media3/queue, #8 focus/BT safety, Voice, DataStore, manifest, Android Auto.
- [x] Seven accessible string resources and honest temporary route shell authored.
- [x] Twelve descriptive JUnit policy methods committed **without execution**.
- [x] Narrow P02 literal-escaped-newline source formatting defect corrected.
- [ ] Kotlin/Gradle/Android compilation, instrumented navigation, activity-recreation/OS back tests, Play/notification/BT regression, owner QA — **NOT_RUN by instruction**.
- [ ] HF-07 full Playing/Paused/No Track screen and four real playback controls — **P04–P06, NOT YET IMPLEMENTED**.
- [ ] Voice origin-overlay and P1 device selection/suggestion — **P07 and P09–P11, NOT YET IMPLEMENTED**.

**Precise gate: P03 source-only NAVIGATION PASS / P04 NEXT.** Issue #9 original 14 product acceptance criteria still `NOT_VERIFIED`. M01–M46 and five technical groups remain `NOT_RUN`; no APK `NOT_BUILT`; no Gradle/test/Actions/PR/Merge/Close. The single final cumulative P13 APK remains reserved for owner's phone validation, including parked-vehicle GS-05.
