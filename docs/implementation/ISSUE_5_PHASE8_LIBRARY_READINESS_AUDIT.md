# Issue #5 / Phase 8 — Permission-to-Library readiness gate and handoff

Status: **IMPLEMENTED; VALIDATION PENDING**. No unconditional PASS until CI verifies this exact production and test revision.

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
