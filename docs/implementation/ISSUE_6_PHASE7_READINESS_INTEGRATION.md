# Issue #6 — Phase P07: Library readiness, permission recovery and Lifecycle integration

**Date:** 2026-10-22 · **Branch:** `feature/6-mediastore-library-browse-search` · [Issue #6](https://github.com/latticeward-labs/music-copilot/issues/6)  
**Status:** P07 SOURCE-IMPLEMENTED; static GitHub readback scoped gate, JVM/Android/runtime verification **NOT_RUN** (P12 only). No Android APK, merged PR or issue closure.

## 1. Source of authority and prior-phase contract

- The user's exact `MSM2(1)(1).rar` was present at `/mnt/data/MSM2(1)(1).rar` and checksum independently confirmed as `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`. The existing extracted 12 English PDFs were available and relevant authority re-reviewed (PRD FR-01; MVP US-02; System Architecture local-first OS MediaStore → derived Room).
- Current accepted GitHub [ADR-0003](../adr/README.md), Issue #16 data/service boundaries, [UF-02](../user-flows/UF-02_PERMISSION_RECOVERY.md), [UF-03](../user-flows/UF-03_LIBRARY_READINESS.md), [UF-04](../user-flows/UF-04_BROWSE_AND_SEARCH.md), GS-01/02 and the [P00–P12 phase plan](ISSUE_6_PHASED_EXECUTION_PLAN.md) are binding. Issue #5's verified/owner-accepted first-use PermissionViewModel and ActivityResult flow must not be replaced.
- **P06 known blocker inherited:** Room V2 source and migration exist but genuinely generated `2.json` and real KAPT/migration tests still not run. P07 cannot erase or silently PASS that blocking evidence.
- The **single combined APK** and owner M01–M34 tests, plus technical T01–T10, remain reserved for P12. #7 service-owned playback and open #73 Issue #5 deferred QA are separate.

## 2. Actual existing wiring verified BEFORE changes

1. `MusicCopilotApplication` creates `ApplicationBindings` once per process; it survives ordinary Activity rotation and is reconstructed after process death.
2. `MainActivity` has actual OS `checkSelfPermission` for API 29–32 / 33+, Android ActivityResult permission callback, `PermissionViewModel` and explicit one-use Granted Continue. It already prohibits scanning from native permission callback, `onResume`, bootstrap and general settings return.
3. `LibraryScanUseCase` already serializes granted initial scans, retry-only on `RecoverableError`, and user-initiated rescan; `RoomLibraryRepository` is the **only** source of scan state `Unavailable, Scanning, Ready, Empty, RecoverableError`, subject to access epoch and MediaStore/Room truth.
4. `RootComposeShell` observes library, OS permission, storage migration and DataStore preferences through lifecycle-aware state collection and routes via `BootstrapRoutePolicy`. The current `PermissionViewModel` owns only safe UI presentation and Settings-return tickets.
5. **Actual gap:** `ApplicationBindings` previously launched each Retry/Rescan tap into a new `repositoryScope.launch` and merely replaced a `Job` reference. Repeated rapid taps could enqueue extra post-scan work even though the downstream use-case Mutex prevents concurrent scanner execution. Readiness routing also used raw library status whereas the displayed status used `LibraryReadinessResolver`, risking projection drift.

## 3. Committed implementation and invariants

| File | What changed |
|---|---|
| `core/application/library/LibraryScanLaunchGate.kt` | New process-owned, coroutine-backed single-flight launcher. Under a lock, one **explicit** scan Job is reserved with `CoroutineStart.LAZY` before it starts; duplicate in-flight callback is **coalesced**, not queued. Completion releases only its own lease, and revocation cancels it without accidentally clearing a newer lease |
| `app/di/ApplicationBindings.kt` | Connects the gate to the existing `LibraryScanUseCase` for **only** explicit Granted Continue, Retry and Rescan; no scanning from `onForeground`. Adds `onForegroundMediaPermissionObserved(granted)` that invalidates a known/active Room scan on OS access loss, cancels the active job, and never auto-scans on regrant |
| `app/MainActivity.kt` | `onResume` calls one process-owned fresh-OS observation method rather than owning scan cancellation logic directly. The same **permission-verified** `LibraryIndexStatus` now feeds **both** `BootstrapRoutePolicy` and the visible Library status projection. Existing SettingsReturnCoordinator and `PermissionViewModel` still require explicit one-shot Granted Continue; none of `onResume`, native permission result, rotation or app startup invokes MediaStore scans |
| `core/application/library/LibraryScanUseCase.kt` | Strengthens `LibraryReadinessResolver`: a purported `Ready` now requires `itemCount > 0` **and** `revision > 0`. Empty already required a genuinely accepted nonzero revision with zero rows; lost OS access always yields Unavailable |
| `app/navigation/LibraryActionPolicy.kt` | New pure presentation decision mapping OS permission + read-only Library readiness → `ReviewAccess, StartScan, RetryScan, Rescan, None`. Not a new media source, state owner, data cache or ViewModel |
| `app/navigation/AppNavHost.kt` | Library shell renders action text/callback from **one** policy instead of two mirrored switch statements. `Scanning`/Initializing never expose a new scan button; revoked/denied permissions require review, error requires manual Retry, Ready/Empty permit manual Rescan. P08 later replaces shell with 4 category UI without owning scans |
| `app/src/test/java/.../Issue6Phase7ReadinessLifecycleTest.kt` | **12 synthetic JUnit methods AUTHORED, NOT_RUN:** duplicate tap single-flight, cancellation/regrant lease, no unsolicited initial scan, verified false Ready/Empty rejection, action matrix, fresh grant and Bootstrap/Room failure route guards |

**No added or modified database schema**, telemetric logging, file deletes, remote access, implicit scan triggers, Media3 player/queue, microphone or Voice execution. Existing `PermissionViewModel` and Compose lifecycle-aware collection remain the intended UI state layer; application graph and repository remain the only source of underlying scan truth.

## 4. Truth table for UI/readiness

| Fresh Android grant | Actual repository snapshot | Derived display | Explicit user action |
|---|---|---|---|
| Missing/denied/revoked | Any previous value | `Unavailable` / Permission Recovery | Review access (never scan) |
| Granted | `Unavailable` | Unavailable until verified scan | `Scan` by explicit tap |
| Granted | `Scanning` | Indexing | None; duplicate taps coalesced |
| Granted | `Ready` / count positive / revision positive | Ready | Optional explicit Rescan |
| Granted | `Empty` / count zero / revision positive | Empty | Optional explicit Rescan |
| Granted | `RecoverableError` | Recoverable error | Explicit Retry |
| Granted | impossible `Ready` with zero count or zero revision | Unavailable | Explicit Scan |
| Granted | boot failed / Room migration failed | Bootstrap RetryStartup | Bootstrap Retry, **not** pretend Library Ready |

**Settings return and cold reentry:** only a fresh OS check updates permission; no code calls `LibraryScanUseCase.execute` automatically on `onResume`. Returning from Settings may show Granted one-shot Continue if prior valid Settings handoff was recorded; process death never restores an optimistic granted scan authorization. Rotation retains process ApplicationBindings and its one active scan Job; new process initializes LibraryIndexSnapshot to Unavailable, despite any old derived Room rows. A user manually triggers a fresh scan to reach Ready/Empty.

**Cancellation:** if OS revokes access during a scan, `onForegroundMediaPermissionObserved(false)` cancels the process gate and `RoomLibraryRepository.invalidateMediaAccess()` invalidates the public index; P05 cancellation inside transactional Room writes is handled there. No physical songs/Room cache are removed by revocation.

**No false claim:** source grep/readback checks only prove method declarations and wiring; they do not prove Android runtime ordering, actual ActivityResult/Settings lifecycle, physical permission/Room atomicity or complete tests.

## 5. P12 execution / acceptance debt

P07 T05/T06 + M01–M34 at the **single final consolidated APK** shall verify on a real device: initial denial, explicit grant/Continue once, rotation while Scanning, screen-off/on, app background/foreground, SETTINGS revocation/regrant, interrupted scan, process death/reentry, duplicate Retry/Rescan taps, true Empty vs inaccessible storage, recoverable error, and retention/visibility of existing local data. JVM run must execute 12 new synthetic P07 tests and relevant earlier scan/permission tests, and Android native tests must compare lifecycle UI against real OS permission on Android 10/12/13+/OEM. Room V2 `2.json` compile and migration blocker from P06 **must pass before final issue acceptance**.

P07 evidence index disposition to report only source-delivery scoped PASS on verified readback; product AC-04/06/11 and all 12 original AC checkboxes remain **NOT_VERIFIED** (AC-12 P2 deferred). No P12 tests, APK, PR or merge are claimed. Final Issue #6 remains OPEN.

**Next phase:** P08 four-category Compose Library UI on existing verified readiness and Browse ports; P06 Room native/schema verification remains open for P12.
