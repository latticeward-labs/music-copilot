# Issue #5 — Phase 3: Real Bootstrap and First-Launch Routing Audit

**Date:** 2026-10-16. **Scope:** user-requested P03 actual Bootstrap, first-use two-value-slide wiring, local persistence, recovery-safe routing, native read-only platform check. **Issue #5 remains OPEN.**

## Provenance and non-negotiable authority

- User-supplied archive: `MSM2(1)(1).rar` SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`, matching the frozen English 12-PDF register `docs/design-system/ISSUE_17_SOURCE_MANIFEST.md`.
- Binding product scope: `docs/PRD.md` and `docs/MVP.md`. Engineering ownership: accepted ADR-0001..0008, `docs/SOFTWARE_ARCHITECTURE.md`, Issue #16 architecture contracts and runtime policies. Approved behavior/design: UF-01/UF-02/UF-03, GS-01, WF-01/WF-02/WF-03/WF-04 and Issue #17 Hi-Fi/Compose handoff. Proposed archive implementation PDFs are not a substitute for accepted GitHub contracts.
- Existing P00/P01/P02 audits and their real CI are unchanged. Exact P03 result belongs to the GitHub Actions runs linked in Issue #5 and the machine validation. Do not extrapolate permission/device pass beyond verified outcomes.

## Implemented topology and tested seams

| Layer/owner | Change | Hard boundary |
|---|---|---|
| Android `MainActivity` | Observes DataStore preferences/load status, Room storage readiness, library and service state using lifecycle-aware Compose; queries current media access on Resume | No native request yet; no media grant stored; no second Player or Voice owner |
| `ApplicationBindings` + `DataBindings` | Non-destructive Room readable open/migration verification and retry; no library reconcile while first-use completion not verified | Room/MediaStore remain authoritative. No destructive DB fallback and no first-run early scan |
| `PreferencesRepository` / DataStore | Explicit `Loading`, `Ready`, `Failed`; retry read without converting failure into false first-install; persisted first-use checkpoint/flag unchanged | DataStore never owns Android permission truth; no account/cloud/session fields added |
| `BootstrapRoutePolicy` (P02) | Now actually projected from combined verified `BootstrapEvidence` | Unknown OS access or unfinished storage cannot become Ready; existing state machine unchanged |
| One `AppNavHost` | Bootstrap always start; routes Onboarding, PermissionRecovery and Library only, with safe `popUpTo` and single-top navigation | No Driving, Player or Voice destination, no mock native dialog |
| `feature/onboarding/FirstUseScreen` | Two approved steps, first-use Continue/Skip/Back/Finish; persistence via state machine and exact write ACK; retry/Back after failed save | No microphone, notification permission prompt, SpeechRecognizer, MediaStore scan or cloud activation |
| `scripts/check_phase10_design_tokens.py` | Strict allowed route set expanded from original 1 to exact current 4; negative unapproved Driving fixture still rejected | Does NOT relax design tokens, architecture direction, lifecycle or privacy enforcement |

## Real First Launch decision table

| Verified conditions | Route/result |
|---|---|
| Preferences not loaded OR Room not verified | Bootstrap Preparing, no false onboarding or library |
| Local preference read or Room startup failure | Bootstrap typed migration/read failure, explicit Retry, no destructive clear |
| Local settings loaded and onboarding incomplete | WF-02 two value slides, resumed from last ACK checkpoint, non-mutating Back/Skip |
| Completion ACK and media permission Unknown | Neutral Bootstrap until actual OS check |
| Completed and OS media access absent | WF-03 access explanation boundary; Not now grants safe manual Library entry |
| Completed, OS media Granted, library index Unavailable/Scanning/Empty/Ready/Error | Library navigation with **literal index status**, never fabricated Ready or playable media |
| Returning user revokes grant | Fresh foreground `checkSelfPermission` blocks Ready; permission recovery (request history/permanent denial belongs P04/P06) |
| User returns after completion | The first-use flag persists, no forced value-slide replay or automatic media/mic/notification prompt |

## First-use state/effect fidelity

On the first install the user sees two (and only two) value/safety slides. Continue from slide 1 persists the second-slide checkpoint; Back from slide 2 persists slide 1; Skip from slide 1 or Finish slide 2 persists completion. The saving state disables repeated taps, and out-of-generation persisted ACK callbacks cannot advance the application reducer. Failing DataStore reads remain `Failed` and show retry, not fake onboarding defaults. Media grant is always re-evaluated from the OS on Resume. The first-use route changes only following successful completion ACK. The educational content explicitly avoids guaranteeing driving safety or provider availability.

## Actual test contracts

- New `Issue5Phase3FirstLaunchTest` covers boot truth, returning first use, denied/manual fallback, no false Ready, exact four routes and forbidden feature navigation, local OS read-on-resume, DataStore effect wiring, no SDK permission dialogs, and no early scan or destructive migration.
- New `Issue5Phase3PreferencesLoadingTest` uses a deliberately failing synthetic DataStore to verify read failure, explicit recovery and repeated failure, with no optimistic `Ready`.
- Updated Phase-10 shell regression retains one NavHost and typed destination inventory while accepting exactly four **approved** P03 routes.
- Workflow `.github/workflows/issue-5-phase3-first-launch.yml` runs P01 contract, dependency negative fixtures, privacy, exact Night Signal design tokens, Kotlin/JVM, Spotless, Detekt, Android Lint, Debug APK and uploads actual evidence.
- Workflow `.github/workflows/issue-5-phase3-api29-emulator.yml` boots real Android 10 Google APIs AVD, verifies both first-run slides through Android accessibility hierarchy, taps Continue/Finish/Not now, verifies persisted completion after force-stop/relaunch, no media or microphone grant, no app crash and saves a screenshot. A queued or failed run does not constitute device evidence.

## Retained failure/correction ledger

- First P03 CI [#38868295997](https://github.com/music-copilot-organization/music-copilot/actions/runs/38868295997) FAIL: forbidden feature-to-app-theme dependency; fixed by consuming shared `MaterialTheme` semantic tokens inside the feature.
- Next [#38868414557](https://github.com/music-copilot-organization/music-copilot/actions/runs/38868414557) FAIL: original Phase10 design token guard expected only Bootstrap; updated allowlist to **exactly** Bootstrap/Onboarding/PermissionRecovery/Library, preserving unapproved-route negative self-test.
- [#38868575383](https://github.com/music-copilot-organization/music-copilot/actions/runs/38868575383) FAIL: escaped regex in amended guard; corrected without removing the guard.
- [#38868658142](https://github.com/music-copilot-organization/music-copilot/actions/runs/38868658142) FAIL: JVM old Route Registry eagerly constructed typed singleton references; converted companion object collections to computed getters and kept strict allowlist.
- [#38869097320](https://github.com/music-copilot-organization/music-copilot/actions/runs/38869097320) FAIL: Android Lint detected unused legacy `foundation_shell_message`; removed unused resource instead of suppressing Lint. [#38869470585](https://github.com/music-copilot-organization/music-copilot/actions/runs/38869470585) SUCCESS after correction.
- Real [API29 emulator UI run #38869071281](https://github.com/music-copilot-organization/music-copilot/actions/runs/38869071281) and [JVM/Android CI #38869470585](https://github.com/music-copilot-organization/music-copilot/actions/runs/38869470585) independently verify state and compiled code at their explicitly identified commit SHAs. Failed runs are historical evidence and are NEVER relabeled PASS.

## P03 completion gate

- [x] Correct authenticated owner and feature Branch; no default branch mutation; same archive SHA and approved authority.
- [x] Actual Bootstrap loading/retry/onboarding/permission/manual library routes implemented.
- [x] Local DataStore ACK, Room verification and foreground platform permission read wired without native prompts.
- [x] First-use two slides, persisted checkpoint, safe Back/Skip/Finish implemented.
- [x] New JVM tests, stronger design validator and API29 emulator smoke workflow committed.
- [x] Actual latest app-code [Gradle/JVM/Android lint/Spotless/Detekt/APK CI #38869470585](https://github.com/music-copilot-organization/music-copilot/actions/runs/38869470585) **SUCCESS** on `f4f071a0e3a741703b2242ceca1969a919c81ae5`. All JUnit classes were run; two new classes contain 13 tests. Resource-only cleanup fixed the last Lint issue.
- [x] Actual [API29 emulator run #38869071281](https://github.com/music-copilot-organization/music-copilot/actions/runs/38869071281) **SUCCESS** on `3b53efa7e0e106438515db38011482870263d0cb`: first slide → Continue → second slide → Finish → permission boundary → Not now manual Library → force-stop/restart returns to permission boundary, with OS media/mic grants still absent. Real APK and screenshot artifact ID `12032378967`. More recent resource-cleanup head's independent [emulator run #38869470447](https://github.com/music-copilot-organization/music-copilot/actions/runs/38869470447) is tracked separately and must not be called successful until it finishes.
- [ ] Final documentation-only commit CI/readback and Issue #5 P03 closeout (record actual final SHA).

**Not P03:** Real Permission request/rationale result classifications, permanent denial Settings handoff, notifications on Android 13+, JIT microphone/Voice practice, full playback/library browsing, cross-OEM Android/API matrix, production accessibility audit, human PR review/merge. These are downstream P04–P14. **No claim of Issue #5 final 13/13.**


## Verified P03 build and emulator evidence

- Exact built code commit: `f4f071a0e3a741703b2242ceca1969a919c81ae5`. [JVM/lint/assemble CI](https://github.com/music-copilot-organization/music-copilot/actions/runs/38869470585) completed **SUCCESS**; log includes `:app:testDebugUnitTest`, `:app:lintDebug`, `:app:assembleDebug` and `BUILD SUCCESSFUL in 2m 10s`.
- Real Android API29 [emulator smoke](https://github.com/music-copilot-organization/music-copilot/actions/runs/38869071281) **SUCCESS** on stated commit `3b53efa7e0e106438515db38011482870263d0cb`; [artifact #12032378967](https://github.com/music-copilot-organization/music-copilot/actions/runs/38869071281/artifacts/12032378967) contains screenshot, permission evidence and build. A pending later run is not represented as successful.
- P03 **PASS for Bootstrap/first-use/app navigation + JVM/Android API29 smoke coverage only**; other device levels, native OS permission dialogs, final 13 ACs and independent review remain P04–P14 work.
