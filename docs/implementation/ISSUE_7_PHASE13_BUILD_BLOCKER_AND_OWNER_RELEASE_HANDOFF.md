# Issue #7 — P13 final combined APK release gate and build-environment blocker

**Date:** 2026-10-23 · **Branch:** `feature/7-media3-playback-queue-session`  
**Actual phase status:** **BLOCKED_BUILD_ENV_NO_APK** (not PASS, not complete).  
**Scope:** P13 is the only permitted phase to assemble and hand off a cumulative P00–P12 APK, obtain owner M01–M36 phone observations, record T01–T15 technical proof, then decide PR/review/merge/Issue closure. At the present checkpoint, only build handoff and the environment blocker are recorded; **the APK does not exist**.

## Attached authority and GitHub baseline

- Attached `MSM2(1)(1).rar` is present at the user-supplied sandbox path and **SHA256 verified**: `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`. This matches the 12 approved English PDFs and previous [P00 source audit](ISSUE_7_PHASE0_SOURCE_PREREQUISITE_AUDIT.md).
- The full P00–P12 implementation/source-review history is retained in the existing [phased plan](ISSUE_7_PHASED_EXECUTION_PLAN.md), [evidence ledger](ISSUE_7_EVIDENCE_INDEX.json), issue body and linked phase reports.
- The last checked feature branch was **199 commits ahead of main, 0 behind**, Issue #7 OPEN and 0 feature PRs before P13 preparation edits. No merge; no evidence that Android builds or audible playback pass.
- Existing build configuration declares Android SDK 36, Java target 17, Kotlin 2.0.21, Gradle Wrapper 8.11.1, Android Gradle Plugin 8.10.1 and Media3 1.5.1. Package for the debug APK is `com.latticeward.musiccopilot.debug` (`applicationIdSuffix=.debug`). This is a distinct application ID from a non-debug install.

## Actually inspected build context — hard blocker

A live check of this sandbox found:

| Dependency | Observation |
|---|---|
| Attached MSM2 .rar | Present; exact SHA256 matched |
| Repository GitHub connector | Connected, branch and issue read verified |
| OpenJDK | `java` and `javac` installed (sandbox uses Java 21, not the required pinned Windows JDK 17) |
| Android SDK/platforms/build-tools | **NOT INSTALLED / NOT FOUND** — no `android.jar` or `sdkmanager` |
| Gradle executable | **NOT INSTALLED / NOT FOUND** — repository wrapper files live in private GitHub, not in a local full checkout |
| HTTPS/DNS outbound | `curl https://services.gradle.org/distributions/gradle-8.11.1-bin.zip` failed DNS resolution (`Could not resolve host`); `git ls-remote https://github.com/...git` also failed DNS resolution |
| Repo checkout in sandbox | Only small prior-phase fixture files, not the full 1,495-entry repository tree; the GitHub connector exposes UTF8 files but not a full private clone through this terminal |
| Authorized GitHub Actions fallback | **PROHIBITED by owner**; none dispatched and no hidden use of workflow artifact/release for this current branch |
| Android APK assembly attempted | **NOT RUN:** required Android SDK and complete checkout unavailable, so an actual Gradle invocation cannot start |
| Signed/verifiable cumulative APK | **NOT PRODUCED**. Cannot claim build success, SHA256, URL, installability, playback, QA PASS or distribution. |

**No dummy, earlier-Issue, alternate-source or synthetic APK is an acceptable substitution.** Even a perfectly structured source branch cannot be called “built” without true Android packaging/signing output.

## Reproducible owner/local Windows build handoff committed

- [`scripts/build_issue7_p13_local.ps1`](../../scripts/build_issue7_p13_local.ps1) is now checked into this exact branch. It requires a **complete clean clone of this branch**, exact Git HEAD and Android Platform 36 under the locally configured SDK (default `C:\Android\Sdk`), plus a real JDK 17. It explicitly rejects a dirty tree, missing prerequisites and prior delivered P13 APK.
- On a machine with preinstalled Android SDK/Gradle wrapper dependencies, run from repository root in **Windows PowerShell** (supply *your actual* Temurin JDK 17 path):
  ```powershell
  git switch feature/7-media3-playback-queue-session
  git pull --ff-only
  $env:JAVA_HOME = "C:\YOUR_ACTUAL_JDK_17_DIRECTORY"
  powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_issue7_p13_local.ps1 -AndroidSdk "C:\Android\Sdk" -JavaHome $env:JAVA_HOME
  ```
  **These commands have NOT been executed.** Do not type the placeholder as a literal directory. Internet or pre-cached Gradle artifacts may be required by the user's machine. Nothing calls Actions or GitHub workflow APIs.
- The script invokes **local `gradlew.bat --no-daemon --console=plain :app:clean :app:assembleDebug` only**. Android Gradle's debug signing produces an installable (subject to successful build) debug APK; it does not run JUnit/androidTest or start GitHub Actions.
- **Only upon verified build success**, it creates `dist/issue7-p13/MusicCopilot-Issue7-P13-debug.apk` and `dist/issue7-p13/MusicCopilot-Issue7-P13-build.json` containing the exact source SHA, APK SHA256, byte count, actual build timestamp and declared toolchain. Both are gitignored to avoid committing binary/user artifacts to source. The output folder must not contain an existing P13 APK, preventing silent evidence overwrite.
- If the local build fails, preserve its **real terminal error log**; fix source on the same feature branch, then rebuild the **one final candidate** (never claim a failed or preliminary APK is the release artifact). The final SHA must point to that exact successfully built binary and commit.

## Owner phone and technical gate — NOT_RUN

- [`ISSUE_7_FINAL_PHONE_QA_CHECKLIST.md`](ISSUE_7_FINAL_PHONE_QA_CHECKLIST.md) defines **M01–M36**, all still `NOT_RUN`. Owner's target phone is Honor x7d Android 15; do not pre-fill any result. A second phone/vehicle is **not required for the first owner QA pass**. Note any unavailable Bluetooth/headset tests as BLOCKED/NOT_RUN.
- The [machine-readable QA register](ISSUE_7_QA_RESULTS.json) must receive the same final `apk_sha256`, source commit, owner device, actual execution date, and **individually observed** manual cases. We must never bulk-PASS, derive phone PASS from a compilation, or substitute unverified self-report.
- **T01–T15** are distinct technical proof cases; only actual results from JUnit/Android instrumented/lint/local structural evidence can pass them. All remain `NOT_RUN`. The user prohibited GitHub Actions, so those cannot be used to fabricate technical evidence; phone interactions alone are insufficient for several T cases.
- The original **AC01–AC13 all remain `NOT_VERIFIED`**. A complete product acceptance/PR closeout needs those genuine observations or an explicitly documented owner-authorized deferral with clear scope. No automatic merge just because a debug APK exists.

## Current action disposition

| Gate | Value |
|---|---|
| P00–P12 code | `PASS_SCOPED_SOURCE_ONLY`, **not Android runtime validated** |
| P13 build script | **Committed / read-back pending**; unexecuted |
| P13 Android SDK/Gradle compile | `BLOCKED_NO_SDK_NO_GRADLE_NO_NETWORK` |
| P13 APK | **NONE** |
| P13 SHA256/source commit of APK | **NULL** |
| M01–M36 / T01–T15 | **ALL NOT_RUN** |
| AC01–AC13 | **ALL NOT_VERIFIED** |
| GitHub Actions runs from assistant for #7 | **ZERO** |
| PR/merge/Issue | **No PR; not merged; Issue #7 OPEN** |

**Next genuine completion condition:** an approved, non-GitHub-Actions machine with Android SDK36 + JDK17 + Gradle-wrapper dependencies must execute the committed script on the entire branch and furnish the exact APK and JSON attestation. Only **then** can the owner's one-APK M01–M36 tests begin and the evidence ledger advance. The blocker and the inability to manufacture/claim an APK are deliberate release integrity controls, not a completed P13.
