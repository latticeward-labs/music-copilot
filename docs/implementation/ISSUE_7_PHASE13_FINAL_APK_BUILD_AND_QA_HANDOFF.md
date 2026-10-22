# Issue #7 P13 — Real cumulative APK build delivered · Owner phone QA pending

**Date:** 2026-10-23  
**Status:** **APK_BUILT_OWNER_QA_PENDING** — Build SUCCESS; actual on-device functionality **NOT VERIFIED**.  
**User's revised authorization:** GitHub Actions is permitted **only to build the single cumulative APK**; explicitly **do not run tests**. This replaced the previous no-Actions restriction solely for APK assembly.

## Delivered APK (one final candidate)

| Field | Verified actual |
|---|---|
| Source GitHub repository | `latticeward-labs/music-copilot` |
| Feature branch | `feature/7-media3-playback-queue-session` |
| Exact compiled source commit | `a66b06cdcdfb88d7a3df506158a373b543021d6b` |
| Successful GitHub Actions run | [39854659624](https://github.com/latticeward-labs/music-copilot/actions/runs/39854659624) |
| Build-only job ID | `120089606655` |
| Upload artifact ID | `12458647942` |
| GitHub APK artifact | [issue7-p13-one-cumulative-phone-apk](https://github.com/latticeward-labs/music-copilot/actions/runs/39854659624/artifacts/12458647942) |
| APK contained in ZIP | `app-debug.apk` |
| APK byte count | `36,605,879` |
| APK SHA-256 | `e3aadcb37dac2c291aed8501acb19de2204324d2ceb7d3374758cae5b325aca3` |
| GitHub artifact lifetime | Created 2026-10-22 21:09 UTC; **expires 2026-11-05 21:09 UTC** (GitHub's recorded 14-day retention) |
| Build toolchain | Ubuntu 24.04 runner, JDK17, Gradle Wrapper 8.11.1, compile SDK Android 36 |
| Exact Gradle command | `./gradlew --no-daemon --console=plain :app:assembleDebug --stacktrace` |
| Build result | **BUILD SUCCESSFUL** |
| JUnit, Android instrumentation, emulator, Lint | **NOT_RUN** / none in the workflow |

**Important provenance:** The artifact's `source-commit.txt` contains exactly `a66b06cdcdfb88d7a3df506158a373b543021d6b`. The `app-debug.apk.sha256` inside the archive matches the independently computed SHA256 of extracted `app-debug.apk`, and the APK ZIP integrity check returned **No errors detected**. The ChatGPT conversation has an independently attached copy of the same verified APK at `MusicCopilot-Issue7-P13-debug.apk` (same SHA256). This last local conversation artifact is not committed to GitHub.

## Scope and two build attempts — still NO tests

1. The [dedicated build-only workflow](../../.github/workflows/issue-7-p13-apk-build-only.yml) was committed with trigger restricted to the exact `.github/issue7-p13-apk-request.txt` path on the feature branch; unrelated pushes cannot start it. Only a single `assemble-one-cumulative-apk` job is present. Its Gradle invocation builds the complete `:app` Android app; no test/JUnit/AndroidTest/Emulator/Lint workflow steps.
2. First attempt [39854155499](https://github.com/latticeward-labs/music-copilot/actions/runs/39854155499) failed at real Kotlin compiler API mismatch: `MediaSession.ConnectionResult.AcceptedResultBuilder(session, controller)` had too many arguments in Media3 1.5.1. The production service code was corrected to `AcceptedResultBuilder(session)`. **No APK was uploaded in that failed attempt.**
3. Second attempt [39854659624](https://github.com/latticeward-labs/music-copilot/actions/runs/39854659624) after the compile correction returned **SUCCESS** with one debug APK artifact. Android package ZIP is structurally valid and independently hashed. **No tests were executed in either run.**
4. The earlier [environment blocker report](ISSUE_7_PHASE13_BUILD_BLOCKER_AND_OWNER_RELEASE_HANDOFF.md) is preserved as a dated *historical record before* the user's limited Actions authorization; it is superseded for build outcome by this successful real run, not deleted.

## Full owner's phone checklist and pending technical evidence

- The [canonical user phone QA checklist](ISSUE_7_FINAL_PHONE_QA_CHECKLIST.md) enumerates **M01–M36** and technical **T01–T15**. All **36 owner manual cases remain NOT_RUN** pending owner observation on the EXACT APK hash above. The only permitted source of PASS/FAIL/BLOCKED/N/A is actual observed case-level user evidence.
- User's initial phone is Honor x7d / Android 15. Install this APK, prepare 3–5 valid local files, run actual scenarios beginning at M01. Bluetooth/physical headset and actual call cases can remain BLOCKED/NOT_RUN if unavailable. Never test anything while driving and never damage sole personal audio copy for corrupt-file scenarios.
- Technical T01–T15 are separate. This user authorized **Build-only Actions**, not JUnit or Android automated tests. They remain **NOT_RUN** even though the APK successfully compiled. In particular **T14** includes Lint, signature/install and report evidence, so a successful Gradle build alone is not a complete T14 PASS.
- All **13 original product acceptance criteria AC01–AC13 remain NOT_VERIFIED**. The source/compile milestone and ready APK are not evidence that background audio, permission recovery or OS media controls behave correctly on a real phone.
- [Machine-readable QA ledger](ISSUE_7_QA_RESULTS.json) is now **`APK_BUILT_AWAITING_OWNER_PHONE_QA`**, with the actual SHA256 + artifact/commit. Individual case statuses were not modified.

## Release governance

- **P13 gate:** `APK_BUILT_OWNER_QA_PENDING`, **not full PASS**. The single required cumulative APK now exists. Do not rebuild or replace the user's binary silently; later fixes need explicit fingerprinted new build, with re-test mapping.
- GitHub Issue #7 stays **OPEN**. No Pull Request created, no merge, no issue closure and no blanket acceptance. Future PR/review/merge/close requires actual owner results and an explicit documented treatment of unexecuted technical QA, not an automatically manufactured approval.
- Artifact expires on 2026-11-05. The owner should save the direct attached APK or download the GitHub ZIP while available; the canonical SHA is permanent release evidence.
