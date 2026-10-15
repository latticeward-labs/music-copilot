# Issue #4 — Phase 13 clean checkout, install and two-device smoke evidence

**Current status (16 October 2026):** **PASS only under the owner-approved single-physical-handset foundation acceptance addendum**; the original two-physical-device+ADB protocol is **UNEXECUTED/DEFERRED**. See [Issue #4 scoped amendment](ISSUE_4_OWNER_SCOPE_AND_REVIEW_ADDENDUM_2026-10-16.md). Original findings below are preserved historically.
**Date:** 14 October 2026; **branch:** `feature/4-android-foundation`.
**Code revision submitted for test:** [`dbdea7061b375b4660febed1f49e1d5e90c91af8`](https://github.com/latticeward-labs/music-copilot/commit/dbdea7061b375b4660febed1f49e1d5e90c91af8).

## Acceptance contract

The [approved Phase 13 plan](ISSUE_4_PHASED_EXECUTION_PLAN.md) explicitly requires a **fresh workspace build** and two individually documented real installs/launches of **the root Issue #4 Android app**, not the separate spike. One device should be Android 10/API 29; another Android 14+/API 34+; record model/OEM/API/build SHA/UTC time and perform permissionless startup, foreground/background, explicit stop/relaunch, no app crash and clean logs. It explicitly says if physical phones are unavailable, **report the emulator result as emulator only and leave the required two-device acceptance unverified**.

The uploaded English `MSM2(1)(1).rar` source archive is SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`; its technical docs page 47 also describes a **broader later QA matrix** (Android 10/12/14+, Pixel/Stock, Samsung/One UI, Xiaomi/MIUI and 3+ vehicle/head-unit combinations). This Phase 13 smoke does **not** replace later OEM/vehicle certification.

## GitHub-executed clean-checkout smoke (virtual devices only)

- [Phase 13 workflow](../../.github/workflows/issue-4-phase13-device-smoke.yml): independent clean `actions/checkout@v5` workers, read-only permissions, no repo signing files, Gradle wrapper verification with dependency cache disabled; root `:app:testDebugUnitTest`, `:app:assembleDebug` and unsigned `:app:assembleRelease`.
- Emulator matrix **A:** Android 10 API 29 / Google APIs / Pixel 3a **virtual AVD**.
- Emulator matrix **B:** Android 15 API 35 / Google APIs / Pixel 6 **virtual AVD**.
- [Current matrix workflow execution](https://github.com/latticeward-labs/music-copilot/actions/runs/38559652982) on SHA `dbdea7061b375b4660febed1f49e1d5e90c91af8`. **completed/success, two jobs**, actual separate clean builds and ADB installs; the artifacts below were independently downloaded and inspected.
- The [strict pre-Phase-13 root CI run #38556526636](https://github.com/latticeward-labs/music-copilot/actions/runs/38556526636) passed build/test/static gates; it is **not** proof of two installs.

Each emulator job must create `evidence.json` and a screenshot from **synthetic/empty** media state; verify install, app package, first launch, absence of runtime permission grants for audio/microphone, living process, activity registered, background→foreground, explicit `am force-stop`→cold relaunch and absence of this app in the crash buffer. No Bluetooth/vehicle/audio playback feature acceptance is asserted.

**Verified results (real GitHub runner data; two emulator profiles, NOT two physical devices):**

| Recorded device evidence | Android 10 / API 29 | Android 15 / API 35 |
|---|---|---|
| Job and run | [#38559652982](https://github.com/latticeward-labs/music-copilot/actions/runs/38559652982), `pixel3a-api29` AVD | Same run, `pixel6-api35` AVD |
| Evidence artifact | [#11891574050](https://github.com/latticeward-labs/music-copilot/actions/runs/38559652982/artifacts/11891574050) | [#11892145524](https://github.com/latticeward-labs/music-copilot/actions/runs/38559652982/artifacts/11892145524) |
| Device-reported manufacturer and model | `Google / Android SDK built for x86_64` | `Google / sdk_gphone64_x86_64` |
| Virtual marker | `ro.kernel.qemu=1` | `ro.kernel.qemu=1` |
| Real evidence timestamp (UTC) | `2026-10-14T12:01:06.360698+00:00` | `2026-10-14T12:02:38.376814+00:00` |
| Tested code SHA | `dbdea7061b375b4660febed1f49e1d5e90c91af8` | Same code SHA |
| Debug APK SHA-256 | `2d5c5bd3d21d4a8b02b2254e297429137a0a2ed7e834b5d288b27e4e88522ffc` | `21887f9082b654d6b227917fd1addf32579f988b0f598cd6881a27ebdc5b272f` |
| Debug / unsigned Release bytes | 35,408,724 / 27,277,945 | 35,408,724 / 27,277,945 |
| Actual clean Gradle build | PASS, `BUILD SUCCESSFUL` | PASS, `BUILD SUCCESSFUL` |
| JUnit XML suites / test results | 14 suites; **94/94**, no failures/errors/skips | 14 suites; **94/94**, no failures/errors/skips |
| ADB install, first launch, background→foreground, force-stop/relaunch | PASS for all five checks | PASS for all five checks |
| Default-denied media/microphone, absence of app crash | PASS | PASS |
| Synthetic/no-personal-media screenshot | Present; actual Bootstrap UI visually inspected | Present; actual Bootstrap UI visually inspected |

**Important accuracy:** The AVD profiles were configured as Pixel 3a / Pixel 6, but the OS-reported models are generic `sdk` emulator identifiers above. This does **not** demonstrate Pixel hardware, Samsung/One UI, Xiaomi/MIUI, Bluetooth, head units, music playback or Voice functionality. The screenshot shows only the intended non-feature Bootstrap empty-library screen. Separate builds may produce different APK SHA-256 hashes; **physical phones must receive the same single APK binary** for the physical evidence gate.


## Physical devices: non-fabricated gate and exact operator steps

No real USB/ADB physical phone is attached to this GitHub Actions hosted runner, and no remote physical device farm, account access or completed operator captures was provided. **Two real phone installations and launches are therefore unverified.**

Before testing, prepare two **physically separate** dedicated test phones/profiles with **only synthetic or no media**, not private recordings/songs. Prefer Android 10 and Android 14+, preferably different OEMs/model families. Do not upload screenshots, serials, phone numbers, personal media, complete `logcat` or unrestricted dumpsys output. Use the **same checked-out commit and exact same built APK** on both devices. Keep exactly one connected ADB target at a time; the script explicitly refuses virtual AVDs in `physical` mode.

From a fresh local checkout with Android SDK platform-tools, JDK 17 and SDK 36 installed:

```bash
git checkout feature/4-android-foundation
./gradlew :app:assembleDebug --no-daemon --no-build-cache
adb devices
# Connect ONLY the Android 10 physical test phone; read its actual API level.
PHASE13_SYNTHETIC_DEVICE_ATTESTED=yes bash scripts/issue4_phase13_device_smoke.sh physical "$HOME/phase13-evidence/phone-a" 29 phone-a

# Disconnect A. Connect ONLY a distinct Android 14+ physical test phone.
# Replace 34 with its actual Android API level (e.g., 35 if Android 15).
PHASE13_SYNTHETIC_DEVICE_ATTESTED=yes bash scripts/issue4_phase13_device_smoke.sh physical "$HOME/phase13-evidence/phone-b" 34 phone-b

# The validator refuses emulators, same model/OEM twice, mismatched APKs,
# missing runtime checks or a different build commit:
python3 scripts/check_phase13_device_evidence.py \
  --first "$HOME/phase13-evidence/phone-a/evidence.json" \
  --second "$HOME/phase13-evidence/phone-b/evidence.json" \
  --expected-sha "$(git rev-parse HEAD)"
```

Use the actual device API values, not guessed numbers; a physical device's `ro.kernel.qemu=1` disqualifies that evidence. The script uses only model, manufacturer, Android/API, SHA-256 of the APK, non-sensitive check booleans, commit SHA and UTC timestamp. On physical phones it **does not write screenshots or raw system/user logs**. Operator attests that test media is synthetic/absent; app data is cleared before testing, but that does not erase personal files elsewhere on the phone.

The physical evidence must be reviewed by Product/Android/Privacy owners before updating the Issue #4 checklist. An emulator cannot satisfy the physical validator.

## Gate dispositions

| Phase 13 required work | State | Evidence |
|---|---|---|
| Truly fresh root build, Gradle unit tests, debug/unsigned release APK without account/credentials | **PASS — CI clean checkout/configuration, tests and both APK variants** | [successful dual-job run #38559652982](https://github.com/latticeward-labs/music-copilot/actions/runs/38559652982), independently parsed both artifacts |
| Two distinct real devices installed + launched, manufacturer/model/API/SHA/time | **BLOCKED — NO PHYSICAL EVIDENCE** | Local physical capture script supplied; no real handset reports |
| API 29 and 14+ coverage; emulator not misrepresented as phone | **PASS — both AVD API levels**, **BLOCKED** for physical | Run #38559652982, both artifact JSONs `ro.kernel.qemu=1`; physical validator rejects emulators |
| Startup, foreground return, explicit process relaunch, no grants/crash | **PASS — both AVD lifecycle/permissions/no-crash checks**, **BLOCKED** for physical | Real installed app, cold/foreground/relaunch result flags and screenshots from both artifacts |
| Synthetic/no personal media/IDs in archived evidence | **PASS — independently reviewed AVD artifacts** | Only synthetic/no media, device model/API/commit/time flags; no serial/raw `logcat`/private files; physical script requires attestation and does not capture screenshots |

**Not in scope of Phase 13:** full MediaSession/real songs, voice recognition in road noise, Bluetooth/focus/calls, dynamic permission recovery, 3+ vehicles, TalkBack/200% font complete flows or signed release. These are not to be claimed based on smoke tests.

**Final disposition as of this report:** **4/5 Phase 13 checkboxes completed**, **1/5 BLOCKED:** actual installation and launch on two distinct physical Android devices, with separately reviewed metadata and proof. **Phase 13 must remain IN PROGRESS / NOT PASS**. Phase 14/PR merge/Issue #4 closure stay blocked. The physically recorded evidence has not been supplied and is not fabricated.


## HONOR X7d manual handset smoke — 16 October 2026

[Full single-phone manual smoke record](ISSUE_4_HONOR_X7D_ANDROID15_MANUAL_SMOKE.md): the tester supplied a screenshot of the actual Bootstrap UI on a **HONOR X7d running Android 15** and verbally reported successful repeated Home → foreground returns and an app reopen from Recent Apps. The tester reported that **no runtime permission prompt appeared** on startup. Original screenshot JPEG SHA-256 `cb56e4825f7e78151c3bbec7b0392eb31d8a0b9773460b1c418b2c2e93c5e094` (image provided in ChatGPT conversation, not stored in this Git repository). This is **one physical-device *user report***, not a GitHub Actions physical-device launch, not an ADB attestation and not an independently validated device. No verified exact APK hash on the handset, timestamp, manufacturer getprop, OS-grant state, crash buffer or timed five-minute trial was provided. **Physical independently verified 0/2, user-reported 1/2, API29 physical 0/1.** Phase 13 acceptance stays BLOCKED; no old virtual-device evidence is rewritten as physical. Issue #5 remains untouched.


## Effective Phase 13 acceptance after owner scope change — 16 October 2026

[Owner-directed acceptance addendum](ISSUE_4_OWNER_SCOPE_AND_REVIEW_ADDENDUM_2026-10-16.md) **supersedes the two-physical-phone acceptance threshold for Issue #4 foundation only**. **PASS under revised criterion:** the actual user-reported HONOR X7d Android 15 handset presented the real root Debug Bootstrap screen (JPEG SHA-256 recorded), successful return from Home and after close/reopen; API29 and API35 independent emulator CI smoke remain green. **Not executed:** second real phone, physical ADB/provenance/getprop/UTC/logcat verification, instrumented OEM or 3+ vehicles. Continue these in Issue #13 QA. Earlier unverified physical check is *not* retroactively represented as successful. The user specifically authorized **one handset sufficient for Issue #4** and requested final merge; [single-phone evidence](ISSUE_4_HONOR_X7D_ANDROID15_MANUAL_SMOKE.md) records the limitations in full.
