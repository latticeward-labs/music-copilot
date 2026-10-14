# Issue #4 — Phase 12 Production GitHub Actions CI: execution and acceptance

**Decision:** **PASS — 14 October 2026**, scoped to `feature/4-android-foundation`. This is **not** approval for Issue #4 closure, PR merge, release signing or two-device/vehicle QA.

## Binding sources and execution identity

- Uploaded English archive `MSM2(1)(1).rar` verified SHA-256: `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8` (12 English source PDFs).
- Authoritative Issue: [#4 Android Foundation and CI](https://github.com/latticeward-labs/music-copilot/issues/4); accepted software architecture Issue #16, architecture ADRs, and [Phased Execution Plan](ISSUE_4_PHASED_EXECUTION_PLAN.md).
- [Production workflow](../../.github/workflows/android-foundation.yml), maintained in the root `.github/workflows/`, **not** the old spike project.
- **Hardened implementation commit:** [`c0131d4ef9fd9de39937d354fd1489bf092c8c84`](https://github.com/latticeward-labs/music-copilot/commit/c0131d4ef9fd9de39937d354fd1489bf092c8c84).
- **Real GitHub Actions success:** [run #38556526636](https://github.com/latticeward-labs/music-copilot/actions/runs/38556526636), completed/success, not a simulated CI run or screenshot.
- **Independently inspected actual evidence ZIP:** [Artifact #11890378853](https://github.com/latticeward-labs/music-copilot/actions/runs/38556526636/artifacts/11890378853), retained for **14 days**.
- Baseline before final hardening also green: [run #38549129200](https://github.com/latticeward-labs/music-copilot/actions/runs/38549129200). The **hardened** run above, not that earlier baseline, is the decisive acceptance proof.

## Acceptance checklist and exact evidence

| Required Phase 12 gate | Verified result and basis |
|---|---|
| Root-app workflow on PR and `main` | **PASS (configuration):** `push` on `main` and `feature/4-android-foundation`; `pull_request` targeting `main`; `workflow_dispatch`. Existing spike, Issue #16 and Phase #10 workflows untouched. A real PR/main-branch run is **not** claimed before Phase 13. |
| Clean checkout, deterministic toolchain and caching | **PASS:** `actions/checkout@v5` with `persist-credentials: false`; Temurin JDK 17; Android platform SDK 36, Build Tools 35.0.0, Gradle 8.11.1 wrapper validation; `gradle/actions/setup-gradle@v4` dependency/cache setup. |
| Real static/format/unit/lint/APK checks | **PASS:** Gradle `spotlessCheck detekt :app:lintDebug :app:testDebugUnitTest :app:assembleDebug` on clean GitHub runner, `BUILD SUCCESSFUL`. |
| Architecture and fixture controls | **PASS:** Python architecture self-test **26** positive/negative fixtures and production dependency check; privacy self-test (safe plus 3 deliberately rejected violations); Compose token self-test (3 deliberately rejected regressions); tracked sensitive-file scanning with **5** rejected synthetic secret/media fixtures. |
| Real tests | **PASS:** independently parsed `app/build/test-results/testDebugUnitTest/TEST-*.xml`: **14 suites, 94 tests, 0 failures, 0 errors, 0 skipped**. |
| Real Android Lint and Detekt outputs | **PASS:** parsed `app/build/reports/lint-results-debug.xml`, **0 issues** under the documented two narrowly scoped pre-existing Lint exceptions, and `build/reports/detekt/detekt.xml`, **0 error elements**. Spotless also passed. |
| Build + archive | **PASS:** actual `app/build/outputs/apk/debug/app-debug.apk` is **35,408,724 bytes**, SHA-256 `2c1c7af5e8ccd07e95209cf5b41bacc5a3eb484fd53681bff865d423ec2692f5`; test XML/HTML, lint XML/HTML/text, Detekt reports and Gradle logs archived. |
| Negative failure detection, then clean recovery | **PASS:** deliberately added `Phase12FormatProbe.kt` with trailing whitespace; real Gradle log `format-rejection.txt` contains `:spotlessKotlinAndGradleWhitespaceCheck FAILED`, `The following files had format violations:`, exact probe path and whitespace diff. The probe is removed; clean `spotlessCheck` reruns green. The hardened gate rejects unrelated network/configuration failures masquerading as a formatting violation. |
| Artifact security and retention | **PASS:** permissions `contents: read`, no write permissions, no workflow secrets, `persist-credentials: false`, 14-day retention, `if: always()` artifacts with `if-no-files-found: error` and checks requiring APK, JUnit XML, Lint XML/HTML, Detekt XML and Gradle logs. |
| CI concurrency | **PASS:** workflow grouping by `github.sha` prevents late delivery of an obsolete branch push from cancelling a newer commit's validation. `cancel-in-progress: true` still de-duplicates runs for the same SHA. |

### Integrity and limits

- The local, independently downloaded ZIP contains **48 files**, all required report families, a real Debug APK and the negative-format regression log. CI reports and the artifact were inspected outside the job itself, not merely inferred from its green status.
- `app/lint.xml` has two **documented, limited** exceptions from Phase 11 (`ExportedService` Media3-system controls and the Room `KaptUsageInsteadOfKsp` performance warning); no blanket Lint-disable rule is introduced by Phase 12.
- The content and tests are **offline/local-first**. A CI Debug APK does **not** imply a signed production build or an installed application passing two physical Android devices.
- Only the **branch push** was executed in the evidence above. `main` and PR triggers are configured but not asserted tested; signed release, merge, head-unit and OEM device acceptance stay with Phases 13–14 and downstream ownership.
- GitHub artifact retention is finite (14 days). This repository report preserves stable run ID, tested commit, APK digest, test counts and failure-evidence disposition.

**Outcome:** All **five** Phase 12 checkboxes are backed by real evidence. Phase 12 is **PASS**. Issue #4 remains **OPEN / IN PROGRESS**.
