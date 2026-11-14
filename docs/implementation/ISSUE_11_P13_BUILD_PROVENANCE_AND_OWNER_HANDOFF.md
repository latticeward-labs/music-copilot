# Issue #11 — P13: cumulative APK build result and owner handoff

**Date:** 2026-11-14  
**Repository:** `latticeward-labs/music-copilot`  
**Branch:** `feature/11-play-artist-one-step-ambiguity`  
**Source commit BUILT:** `a8191014a1a95e4d337314caf196b282749f9f9d`  
**State:** `BUILD_SUCCESS_OWNER_QA_NOT_RUN`  
**GitHub run:** https://github.com/latticeward-labs/music-copilot/actions/runs/43606645653

## What really happened

The owner explicitly started P13 after P00–P12 source authoring. The previously staged marker-only GitHub workflow was triggered **once** by the P13 request commit, with `:app:assembleDebug` as its sole Gradle task. GitHub Actions reported run `43606645653` **completed / success**, job `132482610942` completed successfully. Java 17, Android SDK/compileSdk 36, local pinned Moonshine Tiny + Silero VAD + Sherpa-ONNX native AAR were supplied to the cumulative debug build. The artifact was uploaded successfully with its SHA-256 and exact source commit.

There were **no** JVM tests, Android instrumentation tests, Compose tests, lint, emulator checks or owner-device checks. Build compilation/packaging **succeeded**; this does not mean the code passed tests.

## Artifact and verified integrity

- **Artifact ID:** `14119115504` — `issue11-p13-one-cumulative-build-only-apk`
- **GitHub run (log, summary and artifact download):** https://github.com/latticeward-labs/music-copilot/actions/runs/43606645653
- **Artifact retention:** until `2026-11-28T14:51:13Z` unless GitHub policy changes
- **APK file:** `music-copilot-issue11-p13-debug.apk`
- **APK bytes:** `99,853,110` (≈99.85 MB decimal; ≈95.23 MiB)
- **APK SHA-256:** `b77004afa8d1b4a61ff483ec90347d0200742bcbc8f49d9b8bd8e96f588e812d`
- **Source commit:** `a8191014a1a95e4d337314caf196b282749f9f9d`
- **GitHub Artifact ZIP:** `56,628,179` bytes compressed

The ZIP was downloaded through the GitHub connector and inspected locally. Its APK SHA-256, byte size and `SOURCE_COMMIT.txt` **exactly matched** the values published in `APK_SHA256SUM.txt`, `APK_SIZE_BYTES.txt` and `BUILD_PROVENANCE.txt`. A static APK archive inspection showed the bundled arm64 Sherpa JNI libraries and Moonshine Tiny English/Silero assets. These **integrity checks are not playback/device or unit tests**.

### Offline runtime input hashes from the successful build

| Input | SHA-256 |
|---|---|
| Moonshine Tiny model tarball | `9ec31b342d8fa3240c3b81b8f82e1cf7e3ac467c93ca5a999b741d5887164f8d` |
| Moonshine encoder_model.ort | `94e90a4654fc45cdfedb77c4c08e1739f48862998e58fada384b25118134f221` |
| Moonshine decoder_model_merged.ort | `cf524c4862d36e9e5ab032eddc73637efd822d70e868ac575cf1a46e1e4708a0` |
| Moonshine tokens.txt | `2870d843e14c1e187bf1913a521562a63b53933814bd7f2145120468f494a049` |
| Silero VAD silero_vad.onnx | `9e2449e1087496d8d4caba907f23e0bd3f78d91fa552479bb9c23ac09cbb1fd6` |
| Sherpa ONNX Android 2.0.0 AAR | `f0f8716982d27cb4ce695e3934018b1342d9d3d6fdb6ef1f1658fe8bad50b4fc` |

The source was originally grounded in owner-provided RAR5 `MSM2(1)(1).rar` SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8` and the same-branch GitHub P12 implementation and acceptance matrix. No tests were executed against the uploaded PDFs or application.

## Owner action: physical phone QA, not automated tests

**Full Persian checklist:** [ISSUE_11_P12_P13_OWNER_PHONE_QA_CHECKLIST_FA.md](https://github.com/latticeward-labs/music-copilot/blob/feature/11-play-artist-one-step-ambiguity/docs/implementation/ISSUE_11_P12_P13_OWNER_PHONE_QA_CHECKLIST_FA.md)

The owner must personally install the exact debug APK from the artifact and execute:

1. **M01–M48:** 48 ordered manual handset cases — grammar, existing artist/NotFound, near-equal/tie resolution, single-choice confirmation, no double queue mutation, revision/permission access, Media3 ACK, Player/Driving, Bluetooth/headset focus/route, TTS, prior five intents and privacy.
2. **T01–T10:** ten specialized QA groups — **not executed**. Source-authored fixtures already exist but no PASS evidence.
3. **L01–L06:** six voice intents, ten attempts each (**60 total**) — **not executed**. Gate **≥54/60 correct (90%) and zero unintended wrong-command execution**, report p95 against **2,500 ms** target.

**Current:** all M/T/L rows `NOT_RUN`. All thirteen original Issue #11 acceptance criteria remain `NOT_VERIFIED`. Source compilation success does not alter those statuses. APK download, launch, mic permission, Moonshine and actual audio/playback are **not checked on owner's phone yet**.

**Known limitation:** Moonshine currently does not supply a calibrated `providerConfidence`; the P05 direct high-confidence case **must not be declared PASS** without actual supporting provider evidence, even if exact local artist matching succeeds. The designed safe path may ask one confirmation.

**P0 release stop:** unexpected playback, wrong automatic artist selection, speaker leakage upon headset/Bluetooth route loss, persistent duck or personal transcript/file/artist metadata leakage. Those require an issue-specific remediation, another source-pinned APK build and owner retesting.

## Reporting and closure policy

For each manual check, owner replies `M01: PASS` / `M01: FAIL — brief non-identifying note` / `M01: BLOCKED`. For L01–L06 report `correct/10`, unintended wrong commands and confirmation count. Do not upload private recordings, paths, actual artist names, contact or Bluetooth MAC to GitHub.

Once owner phone results are actually supplied, update `ISSUE_11_P13_BUILD_RESULT_AND_OWNER_QA_REGISTER.json`, the acceptance matrix, Issue #11 comments and any follow-up fixes. **Do not merge or close Issue #11 before actual owner QA and approval.** Independent Issue #80 speech accuracy remains OPEN (historical reported 24/50=48%).

Build is a **debug APK** for the owner's field test. This is not Play Store release-signing or external production certification.
