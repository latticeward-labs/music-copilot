# Issue #5 P09 — GitHub-hosted Actions pre-execution unblock and final acceptance procedure

Status: **RESOLVED 18 October 2026**. The repository transferred to `latticeward-labs/music-copilot`; actual GitHub-hosted runners started and [P09 #39122946667](https://github.com/latticeward-labs/music-copilot/actions/runs/39122946667) completed with both JVM and Android13 native jobs SUCCESS. Previous organizational zero-step failures remain historic; precise billing cause remains unverified. P09 is **PASS_SCOPED**, not Issue-wide merged.

## Evidence

- [P09 #39032641862](https://github.com/latticeward-labs/music-copilot/actions/runs/39032641862), SHA `f8119cca3124a1b7bbf39d2920a845ffaf4b910e`: both Jobs `p09-jvm-lint-apk` and `p09-android-microphone-api33` fail with **zero job steps**, GitHub reported `total_ms=0` in its billable UBUNTU timing endpoint. Neither `actions/checkout` nor Gradle nor Emulator ran.
- [P09 #39031814318](https://github.com/latticeward-labs/music-copilot/actions/runs/39031814318) on `ubuntu-24.04`, including rerun attempt 2, likewise fails before any step. The same symptoms affect P01–P08 workflows. This is **not an observed Kotlin compilation failure**.
- GitHub Checks API reports one check annotation per failed job. The currently permitted app connector cannot read annotations text or the organization Billing and runner admin endpoints, so no precise billing/runner root cause may be asserted.

## Historical owner troubleshooting steps (not currently blocking P09)

1. Sign in to the **repository-owner organization `music-copilot-organization`**, not personal settings. Go to **Settings > Billing & licensing** (or **Billing & plans**) > **Usage** and **Budgets and alerts**. Inspect GitHub Actions included minutes, storage and budget(s), including budget scope and any enabled 'Stop usage' guard. A budget increase can incur charges; review before changing.
2. Check **Payment information** for unpaid/failed invoices or an invalid payment method. A GitHub account with exhausted included minutes and no paid allowance may be blocked before runner assignment. Do **not** assume this is the cause without confirming the actual job annotation.
3. Open [failed P09 job details](https://github.com/latticeward-labs/music-copilot/actions/runs/39032641862) and read the red **Job was not started** annotation banner. This distinguishes usage/payment holds from runner allocation or other account conditions.
4. If Actions usage/payment screens are healthy but jobs still fail before checkout, open [GitHub Support](https://support.github.com/) with Org, repo, failed run links, annotation text, run timing `total_ms=0`, and billing view; request investigation of hosted-runner eligibility.
5. Once execution is restored, rerun the latest branch's **Issue 5 Phase 9 - Optional six-command voice and JIT microphone** workflow. Require **both** `p09-jvm-lint-apk = SUCCESS` (all unit, architecture, Detekt, Spotless, lint and debug APK) and `p09-android-microphone-api33 = SUCCESS` with genuine OS allow/deny, screenshots, dumpsys grant/revoke, manual-only, no app crash.
6. Compare app production SHA to tested runs. Ensure no new Android behavior changes since green CI. Link exact SHA, successful job/run IDs and artifact IDs in [P09 audit](ISSUE_5_PHASE9_MIC_VOICE_BOUNDARY_AUDIT.md), [machine ledger](ISSUE_5_PHASE9_VALIDATION.json) and Issue #5. Only then mark **P09 PASS_SCOPED**; never blanket-accept Issue #5 P10–P14 or merge prematurely.

## Local Android Studio fallback (helpful but NOT CI substitute)

From checked-out `feature/5-onboarding-permissions-recovery`, JDK17 + Android SDK:
```bash
./gradlew spotlessCheck detekt :app:lintDebug :app:testDebugUnitTest :app:assembleDebug --no-daemon --stacktrace
```
Real API33 emulator/device (English OS; freshly installed APK):
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
bash scripts/issue5_phase9_native_test.sh
```
The scripted test **uninstalls/reinstalls** the debug app and requires an accessible Android emulator UI. It captures screenshots, permission dumps and crash buffer to `app/build/outputs/issue5-p09-api33`. Never run it on a device with data you need preserved. Native results on one emulator do not imply full OEM/physical matrix P13.

## Authoritative reference

GitHub Docs: [Actions billing](https://docs.github.com/en/billing/concepts/product-billing/github-actions), [budget limits](https://docs.github.com/en/billing/how-tos/set-up-budgets), [workflow troubleshooting](https://docs.github.com/en/actions/how-tos/troubleshoot-workflows).

## Resolution and source of truth

- **Whole-green P09:** [GitHub Actions #39122946667](https://github.com/latticeward-labs/music-copilot/actions/runs/39122946667), SHA `9fb0603ac6af9a0ca7ed552a873d5b1eba5a59cf`, both jobs SUCCESS with real Android OS permission dialog and screenshot artifacts.
- Use [P09 acceptance audit](ISSUE_5_PHASE9_MIC_VOICE_BOUNDARY_AUDIT.md) and [validation](ISSUE_5_PHASE9_VALIDATION.json) for current PASS. This document's earlier troubleshooting steps are for future recurrence only.
