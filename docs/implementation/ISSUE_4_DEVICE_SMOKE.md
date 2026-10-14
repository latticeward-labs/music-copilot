# Issue #4 — Phase 13 clean checkout, install and two-device smoke evidence

**Status:** **IN PROGRESS / PHYSICAL-DEVICE GATE UNVERIFIED. This is not Phase 13 PASS.**
**Date:** 14 October 2026; **branch:** `feature/4-android-foundation`.
**Code revision submitted for test:** [`dbdea7061b375b4660febed1f49e1d5e90c91af8`](https://github.com/latticeward-labs/music-copilot/commit/dbdea7061b375b4660febed1f49e1d5e90c91af8).

## Acceptance contract

The [approved Phase 13 plan](ISSUE_4_PHASED_EXECUTION_PLAN.md) explicitly requires a **fresh workspace build** and two individually documented real installs/launches of **the root Issue #4 Android app**, not the separate spike. One device should be Android 10/API 29; another Android 14+/API 34+; record model/OEM/API/build SHA/UTC time and perform permissionless startup, foreground/background, explicit stop/relaunch, no app crash and clean logs. It explicitly says if physical phones are unavailable, **report the emulator result as emulator only and leave the required two-device acceptance unverified**.

The English [MSM2 source archive](../../docs/implementation/ISSUE_4_PHASED_EXECUTION_PLAN.md) is SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`; its technical docs page 47 also describes a **broader later QA matrix** (Android 10/12/14+, Pixel/Stock, Samsung/One UI, Xiaomi/MIUI and 3+ vehicle/head-unit combinations). This Phase 13 smoke does **not** replace later OEM/vehicle certification.

## GitHub-executed clean-checkout smoke (virtual devices only)

- [Phase 13 workflow](../../.github/workflows/issue-4-phase13-device-smoke.yml): independent clean `actions/checkout@v5` workers, read-only permissions, no repo signing files, Gradle wrapper verification with dependency cache disabled; root `:app:testDebugUnitTest`, `:app:assembleDebug` and unsigned `:app:assembleRelease`.
- Emulator matrix **A:** Android 10 API 29 / Google APIs / Pixel 3a **virtual AVD**.
- Emulator matrix **B:** Android 15 API 35 / Google APIs / Pixel 6 **virtual AVD**.
- [Current matrix workflow execution](https://github.com/latticeward-labs/music-copilot/actions/runs/38559652982) on SHA `dbdea7061b375b4660febed1f49e1d5e90c91af8`. **At report creation, final result was still pending**; do not treat this link alone as successful evidence.
- The [strict pre-Phase-13 root CI run #38556526636](https://github.com/latticeward-labs/music-copilot/actions/runs/38556526636) passed build/test/static gates; it is **not** proof of two installs.

Each emulator job must create `evidence.json` and a screenshot from **synthetic/empty** media state; verify install, app package, first launch, absence of runtime permission grants for audio/microphone, living process, activity registered, background→foreground, explicit `am force-stop`→cold relaunch and absence of this app in the crash buffer. No Bluetooth/vehicle/audio playback feature acceptance is asserted.

**Actual results / run artifact URLs:** awaiting both job conclusions and inspection. Virtual-device result cannot count as either required physical phone.

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
| Truly fresh root build, Gradle unit tests, debug/unsigned release APK without account/credentials | **IN PROGRESS** | GitHub matrix run pending |
| Two distinct real devices installed + launched, manufacturer/model/API/SHA/time | **BLOCKED — NO PHYSICAL EVIDENCE** | Local physical capture script supplied; no real handset reports |
| API 29 and 14+ coverage; emulator not misrepresented as phone | **IN PROGRESS** for emulator, **BLOCKED** for physical | Matrix API29/API35; validator reject emulator |
| Startup, foreground return, explicit process relaunch, no grants/crash | **IN PROGRESS** for emulator, **BLOCKED** for physical | Device smoke checks in `scripts/issue4_phase13_device_smoke.sh` |
| Synthetic/no personal media/IDs in archived evidence | **IMPLEMENTED — verify from artifact** | App synthetic AVD; physical attestation + no screenshot and sanitized JSON |

**Not in scope of Phase 13:** full MediaSession/real songs, voice recognition in road noise, Bluetooth/focus/calls, dynamic permission recovery, 3+ vehicles, TalkBack/200% font complete flows or signed release. These are not to be claimed based on smoke tests.

**Decision:** Phase 13 **cannot be PASS** until the two required real physical runs have actual reviewed evidence. Phase 14/PR merge/Issue #4 closure must stay blocked.
