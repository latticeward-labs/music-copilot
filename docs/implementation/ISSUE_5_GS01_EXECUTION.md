# Issue #5 / Phase 13 — GS-01 First Launch to Ready Library physical execution ledger

**Evidence state (updated 21 October 2026):** `GS01_OWNER_REPORTED_COMPLETE; DEV-B EMPTY OBSERVED; PRIMARY READY INDEPENDENT VERIFICATION PENDING`. The tables below preserve the pre-test baseline; owner execution update is appended below.

## Approved source contract — NOT the distinct driving/voice MVP sample

Canonical [GS-01_FIRST_LAUNCH_TO_READY_LIBRARY.md](../user-flows/golden-scenarios/GS-01_FIRST_LAUNCH_TO_READY_LIBRARY.md) is the **approved 23 August 2026 Stage 2 scenario definition**. Its approval **does not** mean GS-01 has been run on a real physical phone. This contract maps to [UF-01](../user-flows/UF-01_FIRST_LAUNCH_AND_EDUCATION.md) → [UF-02](../user-flows/UF-02_PERMISSION_RECOVERY.md) → [UF-03](../user-flows/UF-03_LIBRARY_READINESS.md), with handoff to UF-04/Issue #6. It is specifically **First Launch to Ready Library**, not the separate Driving Mode "Next song" MVP golden example.

**Goal:** On real Android, a user reaches truthful account-free local library readiness with no automatic grant, no cloud backend/catalog, and a safe user-controlled recovery after denial, scan errors or revocation. The Ready branch is the **primary golden success path**. Verified Empty is a valid **alternative completion** but **not** proof of primary Ready. Practice Voice is optional and may never block manual library value.

## Runtime source-of-truth requirements

| Contract | Runtime observable | Authoritative source | Status |
| --- | --- | --- | --- |
| S-01 startup/recoverable failure | Actual checked preparation and Retry, no fake readiness | Android+persisted local app preparation | NOT RUN P13 |
| S-02 education | At most two slides, Continue/Skip, replay non-mutating | Locally ACKed DataStore onboarding, UI view state | NOT RUN P13 |
| S-03 media access | Explanation first, explicit OS permission, never infer OS grant from UI copy | Android OS grant flags | NOT RUN P13 |
| S-04 scan | Only after explicit authorized entry; responsive progress and true result | MediaStore and local derived Room index | NOT RUN P13 |
| S-04 Ready primary | Nonempty *currently accessible* indexed audio, truthful count/Ready | OS permission+MediaStore existence+derived index | NOT RUN P13 |
| S-04 Empty alternative | Actual successful scan of zero accessible audio; visible Rescan | MediaStore query result, not denied/null cursor | NOT RUN P13 |
| S-04 failure/revocation | Recoverable Retry, invalidation of stale playability, no file deletion | OS permission+MediaStore recheck | NOT RUN P13 |
| CA-01/02 education | Six specified intents; no Mic until explicit practice; provider unavailable ≠ denied | Permission OS and provider policy, no capture automatically | NOT RUN P13 |
| CB-01/UF-04 downstream | Ready handoff does not falsely promise complete track browsing/player | Issue #6 owns feature delivery | NOT VERIFIED / OUTSIDE ISSUE #5 UI IMPLEMENTATION |

**Important implementation boundary:** The current Issue #5 app UI reports local-library readiness as text rather than providing full Songs/Artists/Albums/Folders/Search flows; those belong to Issue #6. Thus source-level presence of Ready cannot by itself demonstrate GS-01's full described downstream browsing affordances. Note this as an open **implementation-scope dependency**, not as a verified full GS-01 product journey.

## Owner E2E trace to execute on physical device

Owner supplies observed result for each step as `PASS / FAIL / NOT RUN` and device slot DEV-A or DEV-B; assistant must not fill this table from prior phases or CI emulator traces.

| Step | Action with approved local-only test data | Required observation | DEV-A HONOR X7d Android15 | DEV-B different device |
| --- | --- | --- | --- | --- |
| GS01-01 | Real first install without clearing owner's existing app | S-01 checks and S-02 initial two-slide education | NOT RUN | NOT RUN |
| GS01-02 | Complete/skip education, review media purpose | No Mic/notification request on Skip, and permission purpose before OS prompt | NOT RUN | NOT RUN |
| GS01-03 | Keep OS media permission denied initially | No pre-grant scanning or Ready; Not now/Retry available | NOT RUN | NOT RUN |
| GS01-04 | Explicitly grant OS permission for locally owned accessible audio | App observes true Android grant, doesn't infer from UI presentation | NOT RUN | NOT RUN |
| GS01-05 | Tap explicit Continue to scan (after OS granted) | Real initial MediaStore scan; only postscan verified state | NOT RUN | NOT RUN |
| GS01-06 | Nonempty local music / Ready primary outcome | Positive MediaStore/indexed accessible count; Ready and safe handoff | NOT RUN | NOT RUN |
| GS01-07 | On genuinely empty audio state, optional alternate branch | Verified Empty with Rescan; record as `EMPTY_BRANCH_ONLY` not Ready | NOT RUN | NOT RUN |
| GS01-08 | Settings-only and three-way return where supported | Granted / requestable / still Settings-only projection consistent with OS; no coercive loop | NOT RUN | NOT RUN |
| GS01-09 | Revoke permission after verified Ready | Inaccessible media no longer shown as currently playable; no user-file deletion | NOT RUN | NOT RUN |
| GS01-10 | Optional Practice Voice / deny Mic / inspect privacy | Manual fallback; no automatic capture, remote STT or provider fabrication | NOT RUN | NOT RUN |
| GS01-11 | Home, rotation/recreation and replay | State stays truthful, no duplicate native request, no autoplay/queue reset | NOT RUN | NOT RUN |
| GS01-12 | Final decision at the same observed build/device | `READY_PASS` or `EMPTY_BRANCH_ONLY` or `FAIL` or `NOT RUN` | NOT RUN | NOT RUN |

## Machine-readable acceptance definitions

`GS01_READY_PASS` requires real, attributed physical-device evidence of steps 1–6 and relevant permission/recovery guards, a verified non-empty local library, no P0 regression, and no false claim that Issue #6 browsing is already shipped. `GS01_EMPTY_BRANCH_ONLY` is acceptable truthful recovery evidence but **does not fulfill** the primary Ready path. `GS01_FAIL` includes any OS-grant mismatch, false Ready, scan without user grant, no safe fallback, crash, unauthorized remote activity or unreconciled stale files. `GS01_NOT_RUN` remains the initial status until the owner explicitly reports tested steps.

Stage 13 FULL PASS also requires two real devices at distinct enough OS/OEM coverage, all applicable scenario recovery paths evidenced, zero open P0 defects, appropriate device/ADB evidence rather than emulator substitution, and no invented privacy, accessibility or platform test results.

## Evidence ledger — deliberately empty

| Property | DEV-A | DEV-B |
| --- | --- | --- |
| Physical device | HONOR X7d | NOT ASSIGNED |
| Android OS/API | Android 15 / API35 | NOT REPORTED |
| Test date in UTC | NOT REPORTED | NOT REPORTED |
| GitHub APK build | #39243014999 / artifact #12181988538 (candidate) | Same candidate; owner confirmation pending |
| APK build SHA | `a152312ca3ce77151df5df1329b804746e567333` (candidate) | NOT CONFIRMED |
| APK SHA256 | `ba4425364016ab9c969095756a01a57f5c814624c8166e247e707cd41cc23dc8` (verified at artifact, not phone) | NOT CONFIRMED |
| Exact install mode / app-data safety | NOT REPORTED | NOT REPORTED |
| Safe local-audio fixture (zero or positive only, NO TITLES) | NOT REPORTED | NOT REPORTED |
| Evidence type | PENDING OWNER TEST | PENDING OWNER TEST |
| P13 outcomes and raw user report | NOT RECEIVED | NOT RECEIVED |
| GS-01 verdict | `NOT_RUN` | `NOT_RUN` |

## Redacted evidence rules

No screenshots, screen recordings, private files, filename/title/artist/path/URI, transcription, raw audio, location, device unique IDs, or raw OS logs in GitHub. If the owner later chooses to provide plain status text, record it as `OWNER_REPORTED_PHYSICAL_PHONE` and cite this ledger and [P13 device QA](ISSUE_5_DEVICE_QA.md). Do not claim independent `ADB_VERIFIED` without separate, genuinely executed and privacy-redacted evidence. If the owner does not have a second device, leave DEV-B pending and report **partial**, not PASS.

**P13 status:** `GS01_SCENARIO_MAPPED / OWNER_PHYSICAL_EXECUTION_PENDING / NO_GITHUB_APP_TESTS_RUN / NOT_FULL_PASS`.

[Issue #5](https://github.com/latticeward-labs/music-copilot/issues/5) · [P13 device test plan](ISSUE_5_DEVICE_QA.md) · [P13 validation](ISSUE_5_PHASE13_VALIDATION.json).

## 21 October 2026 owner-execution update — supersedes blank baseline

The owner finished the walkthrough and expressly declared all **P13-01..17 PASS**. During this conversation, the first phone's Library displayed `Local library indexing is available.` after permission/Continue; the owner also ran the Empty case on a **second real phone** without music and showed `No local music is indexed yet.` with a Rescan control. These are genuine **owner-reported phone UI observations**, not assistant-executed device tests. There is no phone-side indexed count or named DEV-B model/Android/API in the reports.

| GS-01 evidence area | Owner observation/attestation | Evidence disposition |
| --- | --- | --- |
| GS01-01..05 first use/permissions/explicit scan | Owner reports success; first-use and post-grant UI were shown | `OWNER_REPORTED_PASS`; detailed native evidence not independently reproduced |
| GS01-06 Ready primary with at least one accessible indexed audio file | Ready **UI text** was shown; no positive track count directly confirmed | `OWNER_REPORTED_PASS / PRIMARY_READY_VERIFICATION_PENDING` |
| GS01-07 Empty alternate | No-music result displayed on another phone | `EMPTY_BRANCH_ONLY_OWNER_OBSERVED` |
| GS01-08..11 recovery, microphone and lifecycle | Owner collectively declared P13 cases passed; device-by-device attribution not supplied | `OWNER_REPORTED_PASS`, conditional paths not independent-device-verified |
| GS01-12 final | Owner declares all Phase-13 cases PASSED | `OWNER_SIGNOFF_RECORDED`; not formal `GS01_READY_PASS` yet |

**Formal GS01_PRIMARY_READY_PASS cannot be issued from a Ready label alone**; accessible nonempty MediaStore/index evidence is required. DEV-B model/OS details and two-device scenario allocation also remain missing. The owner-reported complete Phase-13 QA is documented in [owner's report](ISSUE_5_PHASE13_OWNER_PHONE_TEST_REPORT_2026-10-21.md) and the [machine validation ledger](ISSUE_5_PHASE13_VALIDATION.json). We have not uploaded personal screenshots or run CI/ADB/app tests. No claim of Issue #6 browse/search completion or Phase-14 acceptance is made.
