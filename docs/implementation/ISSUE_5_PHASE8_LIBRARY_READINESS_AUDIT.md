# Issue #5 / Phase 8 — Permission-to-Library readiness gate and handoff

Status: **PASS_SCOPED (P08 acceptance completed by independently green native and JVM jobs with proven byte-identical production source).** There is no single whole-green final-head CI workflow, and newer test-harness/runner failures remain visible.

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

## Test evidence and acceptance criteria

- `Issue5Phase8LibraryReadinessTest.kt`: fake OS MediaStore/Room tests denial zero scans, explicit grant once + duplicate guard, post-grant verified Empty, scan and Room write errors → recoverable → Retry, revoked index hidden without deletion, moved/deleted item reconciliation, regrant requires explicit scan, OS-gated resolver, in-flight revoke and racing grant callbacks, plus static source safety checks.
- `.github/workflows/issue-5-phase8-library.yml`: JVM/Spotless/Detekt/Android lint/APK and API33 Android emulator end-to-end.
- `scripts/issue5_phase8_native_test.sh`: real OS permission grant, Empty only after explicitly continuing, user-tapped Rescan, Settings revocation and manual permission recovery, OS permission dumps/crash buffer and screenshots.
- Required scope PASS: **denied → 0 scan; grant → one scan; revoke → no false Ready; errors → recoverable; no physical file deletion**.
- Native API29/32/35 and OEM/physical-device matrix and formal TalkBack remain later P12–P13 gates. No human QA or independent approval is asserted.

## Current verification

- **P08 PASS_SCOPED** from the combination of [native Android13 SUCCESS #39025569472](https://github.com/music-copilot-organization/music-copilot/actions/runs/39025569472), job `117375760627`, and [JVM/Lint/APK SUCCESS #39026317313](https://github.com/music-copilot-organization/music-copilot/actions/runs/39026317313), job `117379277415`. No claim that either workflow's other job passed.
- GitHub compared native commit `18d7be6f447dbd99f6c13285e50b5d088008150d` with JVM commit `64da7060fa565374a258c65cb0b32cf29febf231` and with the accepted branch source at `21c861571fcc401d830524457abf2e850a7645d9`. **Zero changes in `app/src/main/**`**. Native→JVM changed only a JVM test and native script; JVM→accepted source changed only scripts/docs. Hence the exact Android code accepted by JVM is the Android code proven by the successful native job.
- Native trace observed: First Use → Continue/Skip → no granted media before request → **OS-owned Allow** → native `READ_MEDIA_AUDIO: granted=true` → genuine `Empty` → user-tapped `Rescan` → native Android Settings permissions → **Don't allow** → native `READ_MEDIA_AUDIO: granted=false` → Permission Recovery, with terminal `ISSUE5_P08_NATIVE_PERMISSION_READINESS_PASS_API33`.
- JVM trace observed: actual `testDebugUnitTest`, Spotless, Detekt, lintDebug, assembleDebug and architecture/privacy rules all successful, with `ISSUE5_P08_JVM_LINT_APK_PASS`; fake MediaStore+Room tests cover denial zero scans, one-shot grant, race/duplicate callbacks, mid-scan revoke, zero-row verified Empty, errors with explicit Retry, moving/deleting derived rows with real files untouched.
- Issue #5 remains **OPEN**. Original 13 integrated AC boxes, P09–P14, additional Android/OEM/physical/accessibility/review work are not signed off by P08 acceptance.


## Actual CI evidence and unresolved release gate (17 October 2026)

- [P08 run #39026317313](https://github.com/music-copilot-organization/music-copilot/actions/runs/39026317313): `p08-jvm-lint-apk` **SUCCESS** on source commit `64da7060fa565374a258c65cb0b32cf29febf231`. This completed all JUnit contracts, architecture, Spotless, Detekt, Android lint and debug APK. The native emulator job **FAILED** when an Android-owned **"System UI isn't responding"** ANR obscured the second Onboarding screen *before the P08 grant/scan path*; its captured failure screenshot and crash buffer are retained. This is not a P08 native success.
- [P08 run #39025291641](https://github.com/music-copilot-organization/music-copilot/actions/runs/39025291641) previously reported a successful native Android13 job demonstrating Grant → verified Empty → explicit Rescan → real native Settings UI revoke and unavailable/recovery, but its parallel JVM job failed due an incorrect source-test working directory. That path was subsequently fixed. This earlier native success is independent complementary evidence, **not final-head green**.
- The subsequent P08-only native test suite uses a clearly labeled test-only `adb shell pm revoke` and OS `dumpsys` verification because a prior native Settings button press left `READ_MEDIA_AUDIO: granted=true`. The ADB transition is **not described as a manual Settings toggle**; P07 separately exercises real Settings UI.
- An emulator-only, bounded "Wait" recovery for a verified Android SystemUI ANR was added (never suppress Music Copilot app crashes). However, [latest harness run #39027365725](https://github.com/music-copilot-organization/music-copilot/actions/runs/39027365725) and rerun attempt 2 failed **in 2–3 seconds with zero Job steps and no logs**, before checkout. This is an external CI execution blocker of undetermined exact cause; no platform billing/quota assumption is made.
- **Resolution: P08 PASS_SCOPED on verified composite evidence with identical production source across independently successful native and JVM jobs.** No single whole-green workflow on the branch tip is asserted; the later ANR and before-step CI failures are retained as an outstanding CI harness/runner-health task, *not* an untested P08 production-code change. GitHub Issue #5 is OPEN; no Issue-wide AC/PR/merge/human review is claimed.
