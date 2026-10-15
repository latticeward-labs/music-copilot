# Issue #4 — HONOR X7d / Android 15: user-reported physical handset smoke

**Recorded in repository:** 16 October 2026. **Original user conversation and screenshot:** 15 October 2026 (screenshot status bar clock is not an independently attested timestamp).  
**Classification:** **MANUAL / USER-REPORTED, ONE PHYSICAL HANDSET**; **not** a verified ADB instrumented run, **not** an independent reviewer approval, **not** Phase 13 or Issue #4 PASS.  
**Device reported by the tester:** **HONOR X7d, Android 15** (API 35 nominal platform mapping, not independently read from `adb shell getprop`). Manufacturer inferred from model family; physical serial/device fingerprint intentionally not collected.  
**Product:** root Music Copilot Issue #4 Android Debug foundation; user shared a recognizable screenshot of the dark Bootstrap screen titled `Music Copilot` with `Local-first Android foundation...` and `Local media is unavailable...` text.

## Provenance and actual observations

| Test | What was actually provided | Disposition |
|---|---|---|
| Real phone claimed, OEM/model/Android | Tester explicitly reported **HONOR X7d / Android 15** | **USER-REPORTED**, not independently ADB-attested |
| APK installation | Tester opened the app and supplied the running-app screenshot, implying a working install | **USER-REPORTED / screenshot supports visible UI**, installation logs absent |
| Launch and Bootstrap UI | In-conversation 687×1536 JPEG shows app visible with readable title/body, dark theme and Android navigation/status bars | **VISIBLE SCREENSHOT SUPPORT**; does not prove all runtime initialization or crash-free logcat |
| Home / background / foreground return | Tester stated they repeatedly switched between this conversation and Music Copilot and that it worked | **USER-REPORTED SUCCESS**, number of repetitions and elapsed time not measured; **five-minute timed trial not independently confirmed** |
| Close from Android Recent Apps and reopen | After explicit test instructions, tester said `باز اوکیه` (reopen OK) | **USER-REPORTED SUCCESS**, no ADB process/restart evidence |
| First-launch permissions | Tester explicitly said the app did not request any permissions at startup | **USER-REPORTED FACT**; consistent with the foundation Bootstrap; **not proof** of actual OS grant flags or permission-revocation handling |
| Offline, rotation, measured five-minute delay, actual process kill, logcat crash buffer | Not explicitly tested or evidenced | **NOT VERIFIED** |
| Android build ID, package version on phone, UTC start timestamp, OEM fingerprint | Not measured from the phone | **NOT VERIFIED** |
| Second physically distinct phone | No device or trial supplied | **MISSING (BLOCKER)** |

**Screenshot provenance:** supplied in this conversation by the tester and visually inspected by the assistant. SHA-256 of the original conversation JPEG: `cb56e4825f7e78151c3bbec7b0392eb31d8a0b9773460b1c418b2c2e93c5e094`. The original screenshot is **not** claimed to be stored in the GitHub repository, and its visible status bar clock is not a UTC timestamp. Avoid reposting status notifications or user/device identifying metadata into public issue comments.

**Reference APK provenance (not a device attestation):** the earlier locally supplied `Musico_Issue4_Phase13_Debug.apk` matches the API29 clean-build artifact SHA-256 `2d5c5bd3d21d4a8b02b2254e297429137a0a2ed7e834b5d288b27e4e88522ffc`, built in [dual-emulator run #38559652982](https://github.com/music-copilot-organization/music-copilot/actions/runs/38559652982), code commit `dbdea7061b375b4660febed1f49e1d5e90c91af8`. This establishes the **reference download's bytes**, **not** that this exact binary hash/version was independently read off the HONOR phone. The tester subsequently discussed downloadable artifacts; therefore installed-APK identity is not verified with ADB.

## Acceptance disposition and next steps

- **Physical user-observation count: 1 reported, 0 independently instrumented.** A user-provided screenshot is valid evidence of rendered Bootstrap UI, but is neither a complete on-device validation record nor an independent lab validation.
- Original Issue #4 AC `App launches on two representative devices` is still **OPEN**. One HONOR user report plus two independently executed **virtual devices** does not meet the approved two-*physical*-device gate. See [Phase 13 device acceptance](ISSUE_4_DEVICE_SMOKE.md).
- To finish correctly, record a real USB/ADB run for this HONOR and **another physically distinct** Android 10+ handset with the same APK. The preferred coverage is Android 10/API29 and Android 14+/API34+; check actual `ro.kernel.qemu=0`, app install/launch, return from Home, force stop/relaunch, permission denied, no crash, build SHA, APK digest and timestamp. Follow [`scripts/issue4_phase13_device_smoke.sh`](../../scripts/issue4_phase13_device_smoke.sh) and [`scripts/check_phase13_device_evidence.py`](../../scripts/check_phase13_device_evidence.py), without archiving identifying serials or personal media.
- Independent cross-role PR approvals and explicit `main` integration-target exception are still pending. Do **not** merge [Draft PR #71](https://github.com/music-copilot-organization/music-copilot/pull/71), close Issue #4, alter Project Done, or claim Phase 13/14 PASS until all contractual gates truly pass.
- **Issue #5 was not modified, opened for implementation, or changed as part of this record.**


## Formal Issue #4 acceptance change — 16 October 2026

The project owner/requester subsequently **reduced the foundation delivery criterion to one manually tested physical handset** and directly requested Issue #4 merge/closure. See [owner-directed acceptance addendum](ISSUE_4_OWNER_SCOPE_AND_REVIEW_ADDENDUM_2026-10-16.md). Accordingly **this manual one-device evidence suffices for the *revised* foundation-only AC-11**; it does **not** suddenly become ADB/independent verified, does **not** show the originally required second device, and does **not** satisfy broader QA (#13).
