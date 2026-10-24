# Issue #9 — P13 one cumulative APK build and owner phone QA handoff

**Date:** 2026-10-24 · **Build:** SUCCESS · **Owner phone QA:** PENDING / ALL 46 NOT_RUN · **Product AC:** 14 NOT_VERIFIED · **Technical tests:** 5 NOT_RUN.

## Authoritative build result (one combined P00–P12 APK)

| Field | Actual verified value |
|---|---|
| Feature source | `feature/9-driving-mode-bluetooth-suggestion` |
| Exact **built** source commit | `67f690483842694926f277f72000a7faf6d0db23` |
| Git tree SHA | `3ec710875652eead4b0547f0c56fd91e42b0e2b4` |
| Original MSM2(1)(1).rar SHA-256 | `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8` |
| GitHub Actions workflow | `.github/workflows/issue-9-p13-single-cumulative-apk.yml` |
| Exactly one owner-request trigger | `.github/issue9-p13-apk-request.txt` |
| Actual workflow run | [#40113515014 — SUCCESS](https://github.com/latticeward-labs/music-copilot/actions/runs/40113515014) |
| Workflow artifact | [Download **one** cumulative APK ZIP (#12567766876)](https://github.com/latticeward-labs/music-copilot/actions/runs/40113515014/artifacts/12567766876) |
| One Gradle task | `:app:assembleDebug` only |
| Android minSdk / targetSdk | 29 / 36 (Android 10+) |
| Debug application ID | `com.latticeward.musiccopilot.debug` |
| Build log | `BUILD SUCCESSFUL in 1m 48s`; `:app:assembleDebug` completed |
| APK in GitHub artifact | `app-debug.apk` |
| APK size | **37,105,222 bytes** |
| **APK SHA-256** | `71a90627c8aa8393e2dbcdc699474cd479d26bfe6a04d0743d8dcdf0539d2d1e` |
| ZIP SHA-256 | `4ce365476dcaf9b936a77b273e8191b5d7a3958d06a40f45996a922c045271ae` |
| ZIP size | 13,505,698 bytes |
| Independent archive verification | Downloaded exact workflow artifact ZIP, extracted APK and computed both SHA-256s. Embedded `app-debug.apk.sha256` matched. APK ZIP CRC passed, AndroidManifest.xml/resources.arsc/classes*.dex present. **No app installation or execution performed.** |
| JVM/JUnit/Compose/instrumented tests | **NOT_RUN** |
| Android Lint / emulator / vehicle / phone | **NOT_RUN** |
| Owner M01–M46 | **ALL 46 NOT_RUN**, owner to report |
| Issue original AC01–AC14 | **ALL 14 NOT_VERIFIED** |
| PR / merge / Issue close | **NONE**, deliberately left OPEN |

The exact APK is also delivered as a standalone `music-copilot-issue9-p13-cumulative.apk` attachment within the ChatGPT delivery conversation. GitHub stores the original `app-debug.apk` inside its one downloadable ZIP artifact. **Do not infer a raw APK GitHub URL from the ZIP artifact URL.** Documentation committed after the build changes the branch HEAD; the **only** actual APK source SHA is the frozen `67f69048...` above.

## Owner installation and test instructions (stationary only)

1. Download the **standalone APK** from the associated ChatGPT handoff, or sign into GitHub and download the Artifact ZIP from the run, extract `app-debug.apk`, then install it on an Android 10+ phone.
2. Existing debug installs may have a different signing certificate because GitHub Actions used its own debug key. If Android reports `INSTALL_FAILED_UPDATE_INCOMPATIBLE`, an existing `com.latticeward.musiccopilot.debug` install must be uninstalled **only if the owner accepts loss of this debug app's local data/preferences**; back up any needed settings first. The Android OS does not require uninstall if the signature already matches. The regular non-debug app has a different package ID.
3. Use the complete [46-case phone checklist](ISSUE_9_FINAL_PHONE_QA_CHECKLIST.md), with this APK SHA-256, phone model, Android version, stationary accessory category and actual PASS / FAIL / BLOCKED / N/A / NOT_RUN per case. Do not report actual device name, MAC, song contents or voice transcripts. Real phone testing should not be performed while driving.
4. Sequence: **A** M01–M08 manual entry/states; **B** M09–M16 shared Media3 transport/Voice; **C** lifecycle, call/audio focus/headset/Android media card/AirPods regressions; **D** font scale 150–200%, accessibility and safe-driving disclosure; **E** M33–M44 P1 selected Bluetooth Settings and opt-in suggestion; **F** M45–M46 parked GS-04/GS-05. If voice #10 is not production-ready or no real parked vehicle exists, label cases BLOCKED rather than fake PASS.
5. For the P1 Bluetooth path, manually save a *paired eligible* car/headset and observe an actual disconnected baseline in foreground before reconnecting. Device already connected when opening the app is **not** a new offer. The five-minute Not now cooldown is intentional. A connected Bluetooth device never autoplays, activates Mic or enters Driving without Enter.
6. Return actual owner observations and any reproduction steps; GitHub evidence and AC closeout happen **after** these reports and explicit merge/close approval.

## Remaining verified engineering debt

The sole Gradle build passed, but 196 test methods exist only as source and remain **unexecuted** by owner instruction. GitHub Actions logged Kotlin/KAPT warnings about Kotlin 2.x / KAPT fallback and deprecated system bar APIs, plus a navigation always-true condition; none blocked compilation. Device-specific P1 A2DP/HFP callback timing, audio route/focus under physical interruptions, 200% UI behavior, media-card Play/AirPods Play parity and actual car safety are **not yet verified**. The owner may report blockers or faults without marking the Issue fully PASS.

**Disposition:** P13 **APK BUILD COMPLETE**, user M01–M46 phone QA **AWAITING ACTUAL OBSERVATIONS**. No tests run, no PR/merge/close.
