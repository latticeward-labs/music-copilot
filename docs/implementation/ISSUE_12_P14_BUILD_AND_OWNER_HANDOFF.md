# Issue #12 — P14 Integrated BUILD-ONLY APK and Owner QA Handoff

**Date:** 2026-11-15 (UTC+0)  
**Phase:** P14; **build status:** `BUILD_SUCCEEDED_APK_SHA256_VERIFIED_OWNER_QA_NOT_RUN`  
**Source branch:** `feature/12-privacy-safe-telemetry-measurement`  
**Exact source commit compiled:** `599c4035511bbe80c318413853f956496a0416f3`  
**Source archive used for scope:** the exact user upload `MSM2(1)(1).rar`, SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`, as compared with its 12 original English PDFs and GitHub documents during P01–P13.

## 1. Real build evidence — checked against GitHub Actions and the materialized ZIP

| Field | Directly verified result |
|---|---|
| GitHub Actions workflow | [`issue-12-p14-one-integrated-apk.yml`](../../.github/workflows/issue-12-p14-one-integrated-apk.yml) |
| Run | [#43710955663](https://github.com/latticeward-labs/music-copilot/actions/runs/43710955663), event `push`, attempt **1**, **completed SUCCESS** |
| Job | `132802164685`: `assemble-issue12-p14-only`, **SUCCESS** |
| Source HEAD at the exact Build | `599c4035511bbe80c318413853f956496a0416f3`; later doc-only commits are NOT included in the APK |
| Java / SDK | JDK 17, Android compile SDK 36, Android build-tools 35.0.0 |
| Gradle task actually run | `./gradlew --no-daemon --console=plain -Pissue10QaArm64Only=true :app:assembleDebug` |
| Gradle result | **BUILD SUCCESSFUL in 3m 33s**, 39 actionable tasks, 39 executed (compilation and packaging tasks; NOT QA tests) |
| Application ID variant | `com.latticeward.musiccopilot.debug`; debug-signed, ARM64 native libraries only |
| Filename inside GitHub Artifact | `music-copilot-issue12-p14-debug-arm64.apk` |
| APK exact byte size | **100205689** (approximately 100.2 MB decimal) |
| APK SHA-256 | **`cbe10d9efadd09118a1726dee97d08b4da9318b7da4dcbf2da117a5f29671392`** |
| Artifact | [GitHub Artifact #14160576631](https://github.com/latticeward-labs/music-copilot/actions/runs/43710955663/artifacts/14160576631) |
| Artifact ZIP size | 56,750,084 bytes (not the 100,205,689 byte APK) |
| GitHub artifact expiry | 2026-11-29T11:55:45Z (14 days) |
| Build provenance files in ZIP | `BUILD_PROVENANCE.txt`, `INPUT_SHA256SUMS.txt`, `SOURCE_COMMIT.txt`, `APK_SIZE_BYTES.txt`, `APK_SHA256SUM.txt` |
| APK delivery in this conversation | Direct extracted `/mnt/data/music-copilot-issue12-p14-debug-arm64.apk`; **hash and exact byte length independently recomputed and matched ZIP provenance**; this is not a repo file/link |
| APK integrity-only inspection | AndroidManifest present, 19 classes*.dex, 6 ARM64 shared libraries, 0 other-ABI shared libraries, Moonshine encoder + Silero VAD present in APK archive |
| Automated tests/JVM/Android/Compose/Python | **0 run (NOT_RUN)** |
| Lint / static validation | **NOT_RUN** (only build/packaging) |
| Phone/vehicle QA | **NOT_RUN** (owner has not reported results) |
| Issue #12 acceptance criteria | **14/14 NOT_VERIFIED** |
| Merge and Issue state | **No PR, no merge; Issue #12 OPEN** |

Build-only source commit was created by an explicit user-requested P14 marker [`.github/issue12-p14-apk-request.txt`](../../.github/issue12-p14-apk-request.txt); only that marker path triggers this workflow. Source/issue documentation commits after the build will NOT retrigger assembly. No automatic test, `check`, `lint`, emulator or extra Build was launched by this project workflow.

### Source and third-party offline voice dependencies

Build step downloaded the existing Issue #11 proven dependencies, not new STT models: `sherpa-onnx-moonshine-tiny-en-quantized-2026-02-27` `encoder_model.ort`, `decoder_model_merged.ort` and `tokens.txt`; `silero_vad.onnx`; `sherpa-onnx-2.0.0.aar` (AAR verified SHA-256 `f0f8716982d27cb4ce695e3934018b1342d9d3d6fdb6ef1f1658fe8bad50b4fc`). Full actual source hashes are inside `INPUT_SHA256SUMS.txt` in the Artifact ZIP. No cloud STT or analytics vendor is installed. This APK does not fix separately known #80/#82 voice instability.

### Nonfatal compiler/build messages

The build log includes a known Room KAPT warning: Kotlin language >=2.0 fallback to 1.9 for KAPT stubs; Android Java API deprecation warning for status/navigation bar colors; three Kotlin `Condition is always 'true'` warnings in `AppNavHost.kt`; warnings that several JNI libraries cannot be stripped. **None caused a compile failure**, but this is not proof of their runtime safety. Device and technical QA must still review behavior.

## 2. Owner installation and immediate checks

1. Download the exact GitHub [Artifact #14160576631](https://github.com/latticeward-labs/music-copilot/actions/runs/43710955663/artifacts/14160576631) ZIP and extract `music-copilot-issue12-p14-debug-arm64.apk`; alternatively use the verified direct APK attachment in this conversation. Check SHA-256 before installation where a checksum app is available.
2. On your Android 15 Honor phone, install the ARM64 **debug** APK. This has application ID `com.latticeward.musiccopilot.debug`; it is an **independent debug install** from a nondebug app with `com.latticeward.musiccopilot`. Debug keystore identity can differ from historical debug APKs, so if Android says `App not installed` because of an update-signature mismatch, inspect the installed debug copy and export any important app-owned data before removing it; do not randomly clear all application/media data.
3. Start with basic manual playback/Voice launch/Driving/Settings Privacy. The P04 four production diagnostics approvals remain **false** and the P09 sink **NoOp**; absence of uploaded diagnostics is expected and **does not establish correct emission of eight events**.
4. Work through [M001–M121 Persian Owner QA Checklist](ISSUE_12_PHONE_QA_CHECKLIST_FA.md). First **87 `OWNER_PHONE` cases** are interactive on-device; the remaining 15 technical, 12 Product and 7 Beta cases require independent methods and cannot become PASS from the phone alone. Avoid intentional crash/fault injections while driving; carry out route/BT/voice checks safely in a parked vehicle.
5. Send back concise results `M001 PASS`, `M002 FAIL + description`, etc. Owner observations will be recorded against the **specific compiled SHA/APK hash**, not later docs-only commits. Redact music/artist/voice/device data before GitHub comments.

**QA state file:** [`ISSUE_12_QA_RESULTS.json`](ISSUE_12_QA_RESULTS.json) starts with all 121 `NOT_RUN`; only the **build** metadata may now be updated. `testDebugUnitTest`, Python dashboard tests, Android instrumented cases, emulator, accidental CI and remote analytics are **not authorized** by this build request.

## 3. True remaining Product and Beta gates (not Build failures)

- **#80 P0 OPEN:** original Moonshine/Silero five-command owner lab **24/50 = 48%** vs 45/50=90% laboratory threshold; this APK was not tested and does not authorize "fixed".
- **#82 P0 OPEN:** Artist punctuation/ambiguity and confidence stability defects, including missing Android QA.
- P04 diagnostics actual collection approvals (copy, retention, provider adapter, Legal) **all FALSE**, production NoOp. Real signed-off retrospective/forward collection still missing.
- **AC07**: no app-owned short playback-history table/DAO; UI displays `NOT_STORED`, forbids falsely clearing the live Media3 queue, user audio, Likes or Room derived library.
- **AC10**: 271 test methods source-authored and **zero run**; a successfully compiled main APK is not execution of test source.
- **AC11/13/14**: no real independent Beta 50 consenting drivers/14-day/3+ unit observations, P10 acoustic-end-to-perceivable P95 calibration, complete local accepted controls, real event coverage and crash-fatal attribution; P11 offline synthetic report cannot become Beta PASS.
- No PR/merge/close until the owner reports device results and explicitly authorizes release/exception handling. A source successfully compiled into a debug APK is **not a Beta GO**.

## 4. Release decision boundary

**P14 build deliverable COMPLETED successfully. P14 owner QA/acceptance NOT_STARTED. Issue #12 remains OPEN.** Parent phase may be recorded as `BUILD_SUCCEEDED_PENDING_OWNER_QA`; do not mark it `VERIFIED_PASS` and do not change any M-case to `PASS` before real owner evidence. Once the user provides actual results, update the approved QA JSON and Issue #12 per-case, transparently carrying forward all unresolved blockers and the true APK digest.
