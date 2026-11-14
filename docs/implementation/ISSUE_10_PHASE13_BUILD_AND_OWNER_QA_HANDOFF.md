# Issue #10 — P13 | One cumulative debug APK: verified build and phone-QA handoff

**Build date:** 2026-10-25  
**Branch:** `feature/10-ptt-voice-five-control-intents`  
**Build source commit:** `16a71b5b412361fd83d37d196676bf5b4c39bd24`  
**GitHub Actions run:** https://github.com/latticeward-labs/music-copilot/actions/runs/40180566730  
**Workflow:** `.github/workflows/issue-10-p13-one-cumulative-apk.yml`  
**Request:** `.github/issue10-p13-apk-request.txt`  
**Action/job status:** `completed / success` (single attempt), build step `success`, upload artifact step `success`  
**Gradle:** `./gradlew --no-daemon --console=plain :app:assembleDebug` ONLY — actual `BUILD SUCCESSFUL in 2m 20s`; Kotlin/KAPT compile completed.  
**No JVM, Compose UI, Android instrumentation, Lint, emulator, playback automation, microphone automation or phone tests executed.**

## Build artifact: actual verified bytes

| Field | Observed evidence |
|---|---|
| Artifact name | `issue10-p13-one-cumulative-debug-apk` |
| Artifact ID | `12593353938` |
| Verified GitHub artifact page | https://github.com/latticeward-labs/music-copilot/actions/runs/40180566730/artifacts/12593353938 |
| Original artifact ZIP compressed bytes | `13,563,944` |
| ZIP SHA-256 from GitHub and locally rechecked | `8b00edcf3fda48406c85aeb65875f07c2a43d0acf0c16afa923310da130987ce` |
| Inner installable filename | `app-debug.apk` |
| APK bytes, ZIP internal size and BUILD_PROVENANCE | `37,285,182` |
| APK SHA-256, actual `sha256sum` line, and independently rechecked after ZIP extraction | `4c22acd630fd1c8b62044cd52d30ccf309d97fc787e6a5ecde7e4694f936b396` |
| Verified source SHA inside ZIP | `16a71b5b412361fd83d37d196676bf5b4c39bd24` |
| App ID (Gradle debug configuration) | `com.latticeward.musiccopilot.debug` |
| minSdk / compileSdk / targetSdk | `29 / 36 / 36` |
| Version code / version name source | `1 / 0.1.0-foundation-debug` |
| Install/speaker/BT/device inspection | **NOT_RUN** |

An actual connector-downloaded ZIP was independently opened, verified as non-corrupt, cross-compared to the GitHub ZIP digest and had the embedded APK extracted. Embedded `app-debug.apk.sha256`, `source-commit.txt`, `apk-size-bytes.txt` and `BUILD_PROVENANCE.txt` all matched the current run. Integrity/extraction checks are **artifact verification**, not app tests. The GitHub link remains the canonical durable download.

**Warning:** The ZIP SHA256 is not the APK SHA256. Do not copy the ZIP hash into the APK field.

## Combined source coverage

This is ONE `app-debug.apk` assembled from all previous source deliveries **P00–P12**, plus the phase-specific triggering Build workflow (build commit above). No APK per phase was built. Build-only success is compiler/package evidence, not an Android execution proof, user acceptance PASS or independent code review.

P12 source audit: [ISSUE_10_PHASE12_FINAL_SOURCE_AUDIT.md](ISSUE_10_PHASE12_FINAL_SOURCE_AUDIT.md). Issue #10 control scope: five deterministic English commands `PLAY`, `PAUSE`, `NEXT`, `PREVIOUS`, `SEEK_BACK`; `PLAY_ARTIST` and free-form artist search remain Issue #11.

## QA for the owner: immediate next step

1. Download the verified [GitHub artifact](https://github.com/latticeward-labs/music-copilot/actions/runs/40180566730/artifacts/12593353938), unzip and install **`app-debug.apk`** on the personal Android phone. A `*.zip` must **not** be installed as an Android application. Record real device model, Android version and recognition provider availability.
2. Follow the [Persian M01–M60 owner checklist](ISSUE_10_FINAL_PHONE_QA_CHECKLIST_FA.md). Report `PASS`, `FAIL`, `BLOCKED`, `N/A` or `NOT_RUN` with observed behavior and evidence; never assume PASS from a completed Gradle build. No recording must start on app launch, BT auto-connect, permission callback or background; speech commands require a fresh explicit Mic tap.
3. For [the five-by-ten voice lab corpus](ISSUE_10_P13_FIVE_BY_TEN_DIRECT_VOICE_LAB_CORPUS.json), do **ten independent explicit Mic trials for each of five supported intents** = 50 attempts. Log actual music response; threshold **at least 45 real PASS out of all 50**; keep individual five intent success counts. Missing English offline STT support and low/absent provider confidence must not be counted as successful.
4. Ensure a phone uses valid local playable media, permissions and supported offline English recognition. Android 10/11 (API29/30) is a documented manual-only fallback; do not falsely FAIL/PASS the STT network policy. Prior Issue #9 M45 is still pending proof.
5. Report failures (especially audio focus, calls, Bluetooth route/restore, notification Play and unexpected speaker output) immediately. Do not mark Issue #10 done or merge until reported owner QA and actual acceptance evidence permits.

## Recorded truthful phase status immediately after build

- `P13_BUILD_SUCCESS_OWNER_PHONE_QA_PENDING`.
- **APK**: `BUILT_VERIFIED_ARTIFACT`, SHA and source recorded.
- **Manual phone tests**: M01–M60 **NOT_RUN**, `0 / 60` actual results.
- **Voice lab**: 50 attempts **NOT_RUN**, `0 / 50` measured; AC12 (≥45 PASS) **NOT_VERIFIED**.
- **Technical groups**: 10 **NOT_RUN**; their JUnit sources are authored but **0** of them were run.
- **Acceptance criteria**: AC01–AC16 **NOT_VERIFIED**, `0 / 16` verified.
- **PR / merge**: not created; **Issue #10 OPEN**. Base branch `main` remains unchanged.

**Next outcome:** Wait for real user test observations in a future user request. Do NOT schedule work, auto-merge or convert source coverage to PASS.
