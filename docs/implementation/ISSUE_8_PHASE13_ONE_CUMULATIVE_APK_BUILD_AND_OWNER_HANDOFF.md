# Issue #8 — P13 one cumulative APK: actual GitHub Actions BUILD SUCCESS, owner phone QA pending

**Date:** 2026-10-24  
**Issue:** [#8](https://github.com/latticeward-labs/music-copilot/issues/8), OPEN  
**Target:** `main` · **Branch:** `feature/8-audio-focus-route-recovery`  
**P13 outcome:** **BUILD_SUCCESS_OWNER_PHONE_QA_PENDING** — NOT 12/12 runtime PASS; NOT merged; no app/device tests.

## 1. Verified original source and unified build scope

- Owner-uploaded `MSM2(1)(1).rar` SHA-256: `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`. Same 12 original PRD/MVP/architecture/ADRs/technical/user-flow PDFs used for P00–P12.
- Source **exactly checked out by successful GitHub Actions run**: `2951647da5a995f7e28fa2b9dfc89c596697715e`, on `feature/8-audio-focus-route-recovery`.
- This *one APK* was compiled from the entire branch: original merged Issue #7 Media3/Queue/MediaSession plus Issue #8 P02–P12 source including focus reducer, Android callback adapter, route/noisy/USB/Bluetooth, AVRCP quarantine, P07 receipt-time safety, P08 UI/snapshot, P09 Voice cancellation, P10 errors, P11 compatibility and P12 integration. No phase-specific APK builds. Main branch not mutated.
- Prepared dedicated owner-authorized `.github/workflows/issue-8-p13-one-cumulative-apk.yml` and one request marker `.github/issue8-p13-apk-request.txt`. Only the explicit marker on this Issue #8 branch triggers the job; normal documentation and code commits after the build do **not** run Actions. Repository permission scope `contents: read`; no secret, publish or release steps.

## 2. REAL verified GitHub execution details

| Evidence | Actual result |
|---|---|
| Workflow | `Issue 8 P13 - ONE cumulative APK BUILD ONLY` |
| Workflow file | [Build-only workflow](../../.github/workflows/issue-8-p13-one-cumulative-apk.yml) |
| Run | [GitHub Actions #40079909648](https://github.com/latticeward-labs/music-copilot/actions/runs/40079909648) |
| Job | `120831111496` — Build integrated debug APK without running tests |
| Source commit | `2951647da5a995f7e28fa2b9dfc89c596697715e` |
| Runner and toolchain | Ubuntu 24.04, JDK 17, Android platform 36, pinned Gradle wrapper 8.11.1 and Gradle app plugins |
| Only Gradle task | `./gradlew --no-daemon --console=plain :app:assembleDebug --stacktrace` |
| GitHub result | `completed / success` (Build log: `BUILD SUCCESSFUL in 2m 13s`) |
| Generated file | `app/build/outputs/apk/debug/app-debug.apk` — **one** APK |
| APK size | **36,691,339 bytes** |
| Verified APK SHA-256 | `20259add73fcbe06db50dd65f4e440b4cdce823f28f12d50d6f7f86cbd80f19b` |
| Artifact ID/name | `12554742975` / `issue8-p13-one-cumulative-owner-apk` |
| Artifact | [Download on GitHub](https://github.com/latticeward-labs/music-copilot/actions/runs/40079909648/artifacts/12554742975) |
| Archive includes | `app-debug.apk`, `app-debug.apk.sha256`, `BUILD_PROVENANCE.txt` |
| GitHub artifact expiry | **2026-11-07 08:58:07 UTC** (14-day retention) |
| Independent artifact verification | Connector downloaded actual ZIP; ZIP integrity checked; exactly ONE APK extracted; computed SHA-256 from 36,691,339 actual bytes **matches** GitHub Actions `.sha256` checksum and the build log |
| Source compiled | Yes — combined `:app:assembleDebug` completed |
| JVM/JUnit tests / AndroidTest / Lint / emulator | **0 executed** (workflow contains no test/lint actions) |
| Device installation / phone tests | **NOT_RUN** |
| Technical T01–T15 | **15 NOT_RUN**, including T14's separate provenance audit; the build log exists but isn't a test execution |
| Manual M01–M45 | **45 NOT_RUN** |
| AC01–AC12 | **12 UNVERIFIED** |

**Security caveat:** This is a DEBUG package (`com.latticeward.musiccopilot.debug` due to Gradle `applicationIdSuffix=.debug`), not a signed production release. Build success establishes that production source compiled and APK was packaged, NOT whether focus/call/route/phone/Bluetooth/MediaSession privacy requirements function correctly on real devices.

## 3. Owner phone installation and physical QA instructions

1. Download [Artifact](https://github.com/latticeward-labs/music-copilot/actions/runs/40079909648/artifacts/12554742975) from GitHub while signed in to the repository; unzip it and install the **single** `app-debug.apk` on the owner's handset. A direct user-chat APK extraction was also provided; its computed hash matches the GitHub artifact.
2. On Android, permit the browser/files app to install apps from this source if prompted. Existing `com.latticeward.musiccopilot.debug` may need uninstall if previously signed by a different debug key; note this erases that debug application's local data. Never disable broad Android security controls. Only install this verified owner-requested build.
3. Prepare **1–2 disposable MP3 local files**, permissions granted as needed. Start with **M01**, proceed sequentially through [**M01–M45 phone test checklist**](ISSUE_8_FINAL_OWNER_PHONE_QA_CHECKLIST.md). Honor x7d Android 15 is the known user device from prior Issue work, but owner must confirm actual model/version when reporting.
4. For each case record **PASS, FAIL, BLOCKED, NOT_RUN or N/A** plus brief notes. If an accessory/second caller is missing, mark **BLOCKED** instead of fabricating PASS. Use low volume; on any leaked unexpected phone-speaker audio after headset/Bluetooth disconnect, immediately stop and record FAIL; vehicle/head-unit checks only while **parked**.
5. Send the report back citing: `Issue #8`; run `40079909648`, source `2951647da5a995f7e28fa2b9dfc89c596697715e`, APK SHA-256 `20259add73fcbe06db50dd65f4e440b4cdce823f28f12d50d6f7f86cbd80f19b`, actual handset Android/OEM and M01–M45 outcomes. The assistant will **only then** record actual evidence in GitHub.
6. No broad product claims: real telephone-call identification is Media3-focus-derived, not a Telephony call API; OEM headset, no-speaker-leak, Android15 FGS, real Voice session behavior and full Driving screen are still **not verified**. ADR-0006 excludes Android Auto.

## 4. Technical/acceptance and merge/release gate

- Do not mark **T01–T15** as PASS solely due to APK build. Build log could be reviewed as T14 evidence in a separate technical/provenance audit, but **no technical test was run** by the owner-authorized build-only job.
- Maintain **AC01–AC12 UNVERIFIED** until verified against genuine outputs. Existing P00–P12 source-level PASS statuses must never be treated as device QA.
- Issue **#8 OPEN**, PR not merged; branch retained. Next authorized step is **actual owner M01–M45 results**; no issue closure/merge or accidental additional Actions job.
- The uploaded artifact expires on November 7; owner can use the supplied direct APK (SHA verified) or the GitHub artifact before expiry. No new APK build is necessary to run phone tests on this APK.

### Gate summary

- [x] Exact user archive and source commit identified.
- [x] One build-only workflow triggered by owner-approved P13 marker.
- [x] Successful integrated APK `:app:assembleDebug`, no extra app-phase APK.
- [x] Real GitHub job/log/artifact ID, exact APK bytes and SHA-256 verified.
- [x] Full phone QA M01–M45 checklist delivered.
- [x] QA ledger records artifact as **built** while all 45 M, 15 T and 12 AC remain respectively NOT_RUN/UNVERIFIED.
- [x] No JUnit, Android instrumentation, Lint, emulator, hardware tests, PR merge, or issue close.
- [ ] Owner installs APK and reports actual M01–M45 results.
- [ ] Optional deferred technical / OEM / three parked vehicles evidence, independent review and merge decision.

**P13 SUCCESS = APK build and handoff only; product acceptance pending.**


---

## Superseding 2026-10-24 update — fixed APK and owner final MVP acceptance

All preceding “owner phone QA pending / issue open / no merge” statements reflect the **first** P13 build handoff at that point in history. After three reported media-control regressions, a **second owner-authorized build-only APK** succeeded: [run 40086837929](https://github.com/latticeward-labs/music-copilot/actions/runs/40086837929), source `fea9301fa29986439070fe50428bf8e90a2942df`, SHA-256 `318f801c3a972af6f96f6454c0f1219a196b178c7ddb3eb8aa38549e835b130c`. Owner now reports all accessible phone cases and the three fixes PASS and asks to merge/close. **M01–M43 PASS owner-attested only; M44/M45 Voice BLOCKED; T01–T15 NOT_RUN and AC01–AC12 UNVERIFIED.** [Read owner acceptance and deferred evidence](ISSUE_8_OWNER_MVP_ACCEPTANCE_AND_DEFERRED_EVIDENCE.md). Closing the implementation issue is not an assertion of full independent QA.
