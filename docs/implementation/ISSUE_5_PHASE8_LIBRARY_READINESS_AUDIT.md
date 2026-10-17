# Issue #5 / Phase 8 — Permission-to-Library readiness gate and handoff

Status: **IMPLEMENTED, JVM/LINT/APK PASS; NATIVE VALIDATION BLOCKED — P08 NOT YET PASS**. Do not assert an exact-head green Android native workflow.

## Source and ownership

- User-uploaded 15-phase Issue #5 execution plan, P08: denied → no scan; grant → one scan; revoke → no false Ready; errors → recoverable; never delete real files.
- Uploaded MSM2 Phase 1 UF-03 Library Readiness and UF-02 recovery, PRD/MVP, ADR-0003 local-first data, System Architecture, approved Issue #16 and #17 contracts.
- Scope is gate/readiness/handoff; **Issue #6 owns** Songs/Artists/Albums/Folders full catalog and Browse/Search. Media3/Queue/Player remain service-owned; no account, cloud or raw audio telemetry.

## Changes

1. `core/application/library/LibraryScanUseCase.kt`: OS permission-predicate checked both before and after repository reconciliation, mutex serializes duplicate attempts. Explicit `AfterMediaGrant` is only eligible from Unavailable/RecoverableError; Retry only from RecoverableError; Rescan only after a real user action. All three are fail-closed on revocation. No scan from onResume/bootstrap.
2. `LibraryReadinessResolver`: Android permission dominates Room status. Ready requires a positive derived count; Empty must be a post-scan snapshot (revision > 0 and count 0); denied or stale results show Unavailable; Scanning and RecoverableError remain distinct.
3. `ApplicationBindings` now hands approved explicit grant, Retry and Rescan to the same OS-gated use case, retaining revocation cancellation and never assuming platform grants from UI callbacks.
4. `MainActivity` uses the verified status for navigation. `AppNavHost` offers Retry after genuine RecoverableError and Rescan after verified Empty or Ready, while denied access still offers Review music access/Not now. No new catalog/Player routes.
5. `MediaStoreScanner` treats `resolver.query == null` as a recoverable error, not a successful scan of zero songs. Real zero-row Cursor is the only source of verified empty; permission denial and revocation cannot claim Empty.
6. `RoomLibraryRepository` continues atomic replacement of *derived Room* rows after MediaStore scan; removed IDs are reconciled on explicit rescan and each `get` re-queries MediaStore. No physical MediaStore delete/move call; stale Room records cannot be surfaced as Ready after revoke.

## Test evidence plan

- `Issue5Phase8LibraryReadinessTest.kt`: fake OS MediaStore/Room tests denial zero scans, explicit grant once + duplicate guard, post-grant verified Empty, scan and Room write errors → recoverable → Retry, revoked index hidden without deletion, moved/deleted item reconciliation, regrant requires explicit scan, OS-gated resolver, in-flight revoke and racing grant callbacks, plus static source safety checks.
- `.github/workflows/issue-5-phase8-library.yml`: JVM/Spotless/Detekt/Android lint/APK and API33 Android emulator end-to-end.
- `scripts/issue5_phase8_native_test.sh`: real OS permission grant, Empty only after explicitly continuing, user-tapped Rescan, Settings revocation and manual permission recovery, OS permission dumps/crash buffer and screenshots.
- Required scope PASS: **denied → 0 scan; grant → one scan; revoke → no false Ready; errors → recoverable; no physical file deletion**.
- Native API29/32/35 and OEM/physical-device matrix and formal TalkBack remain later P12–P13 gates. No human QA or independent approval is asserted.

## Current verification

- Code and tests committed to `feature/5-onboarding-permissions-recovery`.
- [P08 dedicated CI](https://github.com/music-copilot-organization/music-copilot/actions/workflows/issue-5-phase8-library.yml) started, final result still pending at this audit checkpoint.
- Parent Issue #5 remains OPEN and the 13 integrated acceptance boxes are not declared accepted here; P09–P14 remain out of this phase.


## Actual CI evidence and unresolved release gate (17 October 2026)

- [P08 run #39026317313](https://github.com/music-copilot-organization/music-copilot/actions/runs/39026317313): `p08-jvm-lint-apk` **SUCCESS** on source commit `64da7060fa565374a258c65cb0b32cf29febf231`. This completed all JUnit contracts, architecture, Spotless, Detekt, Android lint and debug APK. The native emulator job **FAILED** when an Android-owned **"System UI isn't responding"** ANR obscured the second Onboarding screen *before the P08 grant/scan path*; its captured failure screenshot and crash buffer are retained. This is not a P08 native success.
- [P08 run #39025291641](https://github.com/music-copilot-organization/music-copilot/actions/runs/39025291641) previously reported a successful native Android13 job demonstrating Grant → verified Empty → explicit Rescan → real native Settings UI revoke and unavailable/recovery, but its parallel JVM job failed due an incorrect source-test working directory. That path was subsequently fixed. This earlier native success is independent complementary evidence, **not final-head green**.
- The subsequent P08-only native test suite uses a clearly labeled test-only `adb shell pm revoke` and OS `dumpsys` verification because a prior native Settings button press left `READ_MEDIA_AUDIO: granted=true`. The ADB transition is **not described as a manual Settings toggle**; P07 separately exercises real Settings UI.
- An emulator-only, bounded "Wait" recovery for a verified Android SystemUI ANR was added (never suppress Music Copilot app crashes). However, [latest harness run #39027365725](https://github.com/music-copilot-organization/music-copilot/actions/runs/39027365725) and rerun attempt 2 failed **in 2–3 seconds with zero Job steps and no logs**, before checkout. This is an external CI execution blocker of undetermined exact cause; no platform billing/quota assumption is made.
- **P08 verdict remains IMPLEMENTED / JVM VERIFIED / NATIVE BLOCKED — NOT PASS.** Native exact-implementation+test full-green validation is the remaining P08 gate. GitHub Issue #5 is open and no Issue-wide AC/PR/merge/human review is claimed.
