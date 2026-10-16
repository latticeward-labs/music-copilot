# Issue #5 — Phase 5 Media Permission Before Scan

**Source checked:** uploaded `ISSUE_5_PHASED_EXECUTION_PLAN(1).md` SHA-256 `32f8095cac6c4f85a9654bd006ca73e6c27f0db3644a5bd4adef847cf738276f`; `MSM2(1)(1).rar` SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`; approved WF-03/HF-03 Permissions Rationale, UF-02, Issue #16 architecture and Issue #17 Compose handoff. Date: 2026-10-16.

## Exact P05 contract and implementation

1. **Rationale first:** `feature/permission/MediaPermissionRationaleScreen.kt` is the canonical WF-03 explanation, with approved `Permissions`, `PERMISSION`, `Media access`, `Index and play local music.`, Continue/Not now/Back and explicit Android-owned handoff notice. Supports scroll/safe insets/48dp touch targets.
2. **No cold-start prompt:** `MainActivity` registers `ActivityResultContracts.RequestPermission` but launches only after an explicit WF-03 Continue. Back/Not now reaches a manual Library access-deferred surface. Voice/microphone and notifications are untouched.
3. **OS/version:** PermissionPlatformPolicy maps API29–32 to `READ_EXTERNAL_STORAGE` (Manifest maxSdkVersion=32); API33–36 to `READ_MEDIA_AUDIO`. No image/video or all-files capability is used.
4. **No duplicate/in-flight prompt:** `core/application/permission/MediaRequestGate.kt` implements a one-shot `pendingBlockedAction=StartScan`, with a pending callback guard and SavedInstanceState reattachment (without relaunch). A duplicate/stale callback cannot authorize a second action.
5. **Fresh OS after native grant:** the ActivityResult Boolean is NOT accepted as permission truth; `checkSelfPermission` is read again. The gate returns StartScan only for a matching fresh grant, then consumes it. `ApplicationBindings.scanAfterExplicitMediaGrant()` checks OS grant again under a scan Mutex, and `RoomLibraryRepository` and `MediaStoreScanner` each enforce current access independently. Neither onCreate, Bootstrap, onResume nor failed/dismissed native dialog calls reconcile.
6. **Scope stop:** This phase adds only a factual denial hint and manual exit; the complete Granted/Denied/PermanentlyDenied WF-03 Recovery state family belongs **P06**, Android Settings handoff/revocation **P07**, and comprehensive Library readiness semantics **P08**. No Account, login, cloud STT, native microphone prompt, playback ownership change, telemetry, or scanning without access.

## Evidence gates

| Gate | Check | GitHub evidence |
|---|---|---|
| P05 type/unit | 12 JVM tests: API29–36 policy, no prompt at bootstrap, grant/denied/cancel, duplicate protection, restoration, manifest, no auto scan, WF copy | [Native P05 JVM/Lint/APK #38883438174](https://github.com/music-copilot-organization/music-copilot/actions/runs/38883438174) — **PENDING actual conclusion** |
| P05 real native | Android 10/API29 actual permissioncontroller rationale→Not now→native DENY→manual→fresh native ALLOW→verified empty scan | [API29/API33 combined run #38883438170](https://github.com/music-copilot-organization/music-copilot/actions/runs/38883438170) — **PENDING actual conclusions** |
| P05 version split | Android 13/API33 same dialog scenarios and `READ_MEDIA_AUDIO` platform grant | Same native run (matrix API33) — **PENDING actual conclusion** |
| P03/P04 regression | Original first launch/Onboarding + 100–200% reflow; WF-03 copy updated to approved HF | Separate P03/P04 Actions; no waived tests |

## Success conditions and limitations

**Decision at first recording: PENDING native Android and Kotlin CI.** Source code and workflow existence alone do NOT prove runtime PASS. Final verdict requires actual GitHub success, path/hash readback, Issue #5 evidence and no privacy/architecture failures. Issue #5 remains OPEN and 13 integrated ACs unchecked; P06–P14 are untouched.

Physical HONOR/API35 and independently reviewed full device matrix are P13. API31/35 runtime checks and complete accessibility are P12–P13 unless separately executed earlier. Android Emulator is never called a physical device.


## Revalidation and native test hardening (2026-10-16)

- Actual [P05 Gradle/JUnit/Android lint and APK #38884070402](https://github.com/music-copilot-organization/music-copilot/actions/runs/38884070402): **SUCCESS**, including 12 new test cases; P03/P04 JVM regressions on same implementation commit also succeeded.
- Native [API29/API33 run #38884594723](https://github.com/music-copilot-organization/music-copilot/actions/runs/38884594723): **PARTIAL, not PASS**. API29 real native dialog, denial, explicit retry/allow and post-grant local scan succeeded with uploaded screenshots. API33 failed when the temporary UIAutomator XML could not be pulled as Android 13 permission controller was booting; this is a test-harness transport error, **not** a native Grant test pass.
- Committed a bounded, fresh-dump-per-attempt Android UIAutomator retry in `scripts/issue5_phase5_emulator_ui.py` (`41e0f6ed52bcfbbe5a67b15caaa6451483921741`). No native result is simulated and no check is skipped. A real [re-run matrix #38887620769](https://github.com/music-copilot-organization/music-copilot/actions/runs/38887620769) is ongoing and must be evaluated for **both** API29 and API33 before P05 final PASS.
- Actual app implementation remains unaltered during this test-tool correction; P06 comprehensive denial/permanent UI, P07 Settings and P08 Library readiness remain explicitly excluded.

**P05 final verification decision:** PENDING both API29 and API33 success of re-run; earlier P05 machine ledger is historical.
