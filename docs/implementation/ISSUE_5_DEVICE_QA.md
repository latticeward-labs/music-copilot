# Issue #5 / Phase 13 — Physical-device QA contract and owner execution checklist

**Status (updated 21 October 2026):** `OWNER_REPORTED_P13_17_OF_17_PASS / TECHNICAL_FULL_PASS_PENDING`. The original test checklist below remains the acceptance standard; [owner's attributed report](ISSUE_5_PHASE13_OWNER_PHONE_TEST_REPORT_2026-10-21.md) supersedes the former 'no tests reported' baseline. This file prepares owner-executed validation; it is NOT a device test report and makes no claim that GS-01 was executed.

## Evidence authority and phase gate

- User-provided `ISSUE_5_PHASED_EXECUTION_PLAN(1).md` P13: **minimum two representative physical Android devices**, ideally different OEM/Android versions; record user reports distinctly from **ADB/device-verified** output; fresh install, two slides/skip, media permission → scan → Ready/Empty, denied/retry/manual, all three Android Settings returns, revocation from Ready, JIT Mic, provider/cloud/manual fallback, Home/rotation/process recreation; GS-01 truthful source-of-truth. All required scenarios need real native evidence and no open P0 blockers.
- Official `MSM2(1)(1).rar` (SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`), PRD/MVP, ADR-0001..0006, UF-01/02/03, and repository [approved GS-01](../user-flows/golden-scenarios/GS-01_FIRST_LAUNCH_TO_READY_LIBRARY.md) define valid outcomes.
- Scope of **Issue #5** is first-use, permission/recovery and truthful local-library readiness. Actual Songs/Artists/Albums/Folders browse/search and playback remain downstream Issue #6 and other implementation issues; do not claim those features are working just because readiness is Ready.
- The owner expressly directed **NO GitHub-run application tests**. No actions workflow, JVM tests, lint, emulator, adb instrumentation, or remote phone tests may be launched by assistant for P13. **No screenshot, screenshot link, raw log, audio, filename/title, content URI, Bluetooth ID or sensitive payload** is to be uploaded to GitHub. Only text-only attributed test verdicts and nonsensitive technical metadata are permitted.

## Reusable APK — no new build needed

- [P12 APK-only successful run #39243014999](https://github.com/latticeward-labs/music-copilot/actions/runs/39243014999), [artifact #12181988538](https://github.com/latticeward-labs/music-copilot/actions/runs/39243014999/artifacts/12181988538) (retained by GitHub until ~26 October 2026; external expiration may change).
- APK debug application ID `com.latticeward.musiccopilot.debug`, build commit `a152312ca3ce77151df5df1329b804746e567333`, APK SHA-256 `ba4425364016ab9c969095756a01a57f5c814624c8166e247e707cd41cc23dc8` (35,801,343 bytes).
- GitHub commit comparison from APK commit to P13 planning head: **14 commits, zero changed app/ and Gradle build files**. These intervening commits were documentation and opt-in workflow updates, not app changes. **Therefore the P12 binary suffices for current P13 device QA.** Debug APK compilation alone does NOT mean native P13 tests PASS.
- Do NOT uninstall the owner's current app just to fake a fresh-install test; uninstall/clear storage would remove app-private settings and possibly derived local index. Run the **fresh-install** case on a second physical device, an independently disposable test profile if genuinely isolated/supported, or report `NOT_RUN`. Update-install tests should preserve app storage. Never delete or modify real music files for a test.

## Required device matrix

| Slot | Physical device | Android / API | OEM | Owner/ADB evidence | Current result |
| --- | --- | --- | --- | --- | --- |
| DEV-A | **HONOR X7d** | Android 15 / API 35 | HONOR | **OWNER to test P13 cases**; prior P10 (7/7), P11 (11/11), P12 (12/12) are historical reports, NOT P13 execution | **NOT RUN P13** |
| DEV-B | **Required second real phone, TBD** | Different Android version (prefer Android 10/12 or other available 14+) | Prefer Pixel, Samsung or Xiaomi (distinct OEM) | **Pending owner-supplied device and results**; no emulator substitutes | **NOT RUN / DEVICE NOT ASSIGNED** |

A single-phone result can be explicitly recorded as `P13_PHONE_QA_PARTIAL`, never as two-device PASS. Do not label a simulator/emulator as DEV-B. Precise phone identifiers, account details and personal location are not required.

## Device test cases and expected outcomes

Each `P13-xx` status MUST be `PASS`, `FAIL` or `NOT RUN` **separately for DEV-A and DEV-B**. Do not translate prior P12 verdicts into P13 PASS. If the Android/OEM condition is unavailable, choose `NOT RUN` with the factual reason, not an invented PASS.

| ID | Physical owner actions | Pass observation / negative guard | Source |
| --- | --- | --- | --- |
| P13-01 | Genuine first install on a spare phone/test profile, launch without permission | Startup truthful; exactly two onboarding slides available, no account/sign-in or unexpected OS dialog | UF-01, GS-01 steps 1–2 |
| P13-02 | Go through both slides; try Back and Skip; test resume from background | Correct local-first explanation, six supported commands on education, Skip does not request Mic/notification | UF-01 |
| P13-03 | At Review music access, examine rationale BEFORE Continue | OS music permission dialog opens only from explicit Continue, no pre-grant MediaStore scan/false Ready | UF-02, GS-01 step 3 |
| P13-04 | With **at least one user-owned safe local audio item** known to Android MediaStore, explicitly grant media access, then tap Continue | One consented scan/reconcile, truthful `Ready` **only when at least one accessible indexed file exists**; no account/streaming required | UF-03, GS-01 primary Ready |
| P13-05 | On a **genuinely empty or audio-filtered** physical profile, grant and explicitly scan | `Empty` (not fake Ready); text and Rescan/recovery visible; record `NOT RUN` if cannot arrange a genuine empty state safely | UF-03, GS-01 Empty branch |
| P13-06 | From ungranted state, deny a genuine Android permission request, select Not now | `Access denied` with non-crashing manual/library fallback; no silent scan | UF-02 |
| P13-07 | Explicit Try again after requestable denial, then deny again if OS presents dialog | Retry user-initiated; OS decides `requestable` versus `Settings-only`; no auto prompt loop | UF-02 |
| P13-08 | If Android enters Settings-only, tap Open Settings, make **no** change, Back | Re-check system truth, stay in Settings-only or truthfully switch to requestable; no auto re-open Settings or scan | UF-02, GS-01 recovery |
| P13-09 | From Settings-only enter Android Settings and explicitly **grant**, Back | Truthful `Access granted` and explicit Continue; **no scan before Continue**, then scan/Ready or Empty per actual MediaStore | UF-02, UF-03 |
| P13-10 | Exercise Settings return when permission is **Denied - requestable**, if Android/OEM provides that state | Show Try again/Not now, not fictitious `Granted`; if OS cannot present third branch, mark `NOT RUN` | UF-02 3-way return |
| P13-11 | After a genuine Ready, revoke music permission in Android Settings, return | Ready/stale playability removed; user data untouched; clear permission recovery | UF-03 revocation |
| P13-12 | From Empty or genuine recoverable scan error use Rescan/Retry *explicitly* | No duplicate auto-scan; legitimate Empty or updated Ready; never corrupt/delete music to produce an error | UF-03 |
| P13-13 | On education slide 2 select Practice Voice, review purpose, deny microphone | JIT prompt only after explicit intent; manual controls remain usable, no auto voice capture | UF-01/02, CA-02 |
| P13-14 | If appropriate, grant microphone; inspect local speech-provider availability | Permission and provider readiness are separate; unavailable/language unsupported is reported truthfully; no guarantee of fully offline recognition | UF-02, ADR-0004 |
| P13-15 | Inspect Settings Cloud STT disabled/revoked and MediaSession notification text | No remote adapter/account/consent bypass, no unnecessary runtime notification prompt; no private voice recording | UF-02, P10 |
| P13-16 | Home/back/recents, supported rotation, Settings return; test returning with education replay | No crash, stale consent/grant, duplicate native request, unwanted scan, autoplay or service-owned queue reset. Do not use destructive Clear Storage | UF-01..03, P11 |
| P13-17 | Complete GS-01 **end-to-end** under valid, accessible local-media Ready preconditions | Primary Ready golden branch only if genuinely indexed audio; if only Empty occurs, label `GS01_EMPTY_BRANCH_OBSERVED` rather than `GS01_READY_PASS` | Approved GS-01 |

**Optional or conditional cases:** `P13-04` (Ready) and `P13-05` (Empty) require DIFFERENT genuine library states, not an artificial UI selection; natural scan failure is not required to be deliberately induced. Settings 3-way projections may depend on platform flags and may be untestable on one OEM. A second device should help cover those gaps without mislabeling.

## GS-01 / P13 hard-stop criteria

1. `Ready` displayed without accessible music/indexed count → **FAIL / P0 candidate**.
2. `Empty` displayed simply because MediaStore was denied, null, or inaccessible → **FAIL / P0 candidate**.
3. Permission prompt or scanning launched on bootstrap, resume, Skip, or screen rotation without prior explicit user action → **FAIL / P0 candidate**.
4. Cloud speech/recording without approved separate consent or a personal-media filename/URI in diagnostics → **FAIL / P0 candidate**.
5. No safe Retry/Not now/Settings path or crash/duplicate action → **FAIL / P0 candidate**.
6. A missing Issue #6 full browse/playback UI must be documented as a **downstream scope limitation**, not a false P13 GS-01 Ready/CB-01 feature acceptance.

## Owner report format (text-only, no screenshots)

```text
Issue #5 P13 owner physical-device QA (text only)
Date/time in UTC (if known):
DEV-A manufacturer/model: HONOR X7d
DEV-A Android version/API: Android 15 / API35
DEV-A APK artifact: #12181988538
DEV-A build SHA: a152312ca3ce77151df5df1329b804746e567333
DEV-A installed APK hash checked by owner: YES/NO/UNKNOWN
DEV-A install type: UPDATE/FRESH/UNKNOWN
DEV-A local library fixture: NONEMPTY / EMPTY / UNKNOWN (NO SONG NAMES)
DEV-A P13-01: PASS/FAIL/NOT RUN
...repeat through P13-17...
DEV-A GS-01: READY_PASS / EMPTY_BRANCH_ONLY / FAIL / NOT RUN
DEV-B manufacturer/model: [or NOT AVAILABLE]
DEV-B Android version/API:
DEV-B APK artifact/build:
DEV-B P13-01..P13-17: PASS/FAIL/NOT RUN individually
DEV-B GS-01: READY_PASS / EMPTY_BRANCH_ONLY / FAIL / NOT RUN
Observed issues / unavailable OS conditions (no personal data):
TalkBack/scale if relevant:
NO SCREENSHOTS OR RAW LOGS IN GITHUB.
```

## Evidence and acceptance disposition

- Owner reports → annotate `OWNER_REPORTED_DEVICE_QA`; do not relabel as `ADB_VERIFIED`. ADB-backed runs require *real and separately supplied* native evidence before `ADB_VERIFIED`; no substitute from earlier GitHub emulator jobs.
- Maintain separate evidence for DEV-A vs DEV-B and the `GS01_READY_PASS` vs `EMPTY_BRANCH_ONLY` outcomes in [GS-01 execution](ISSUE_5_GS01_EXECUTION.md) and [machine validation](ISSUE_5_PHASE13_VALIDATION.json).
- **No P13 phone tests were run by the assistant**, no GitHub Actions or app tests triggered, no APK rebuild requested, no screenshot/log artifact uploaded. The user's results are the only source for future device observations.
- P13 full PASS requires all mandatory applicable physical cases with real native evidence, meaningful two-device coverage, verified GS-01 primary branch, zero open P0 issues and truthful tracking of untestable cases; P12's unexecuted technical gates remain separate.

[Issue #5](https://github.com/latticeward-labs/music-copilot/issues/5) · [Approved GS-01](../user-flows/golden-scenarios/GS-01_FIRST_LAUNCH_TO_READY_LIBRARY.md) · [P12 build provenance](ISSUE_5_PHASE12_VALIDATION.json).

## 21 October 2026 owner testing update

Owner explicitly declared **P13-01 through P13-17 passed (17/17 as a consolidated self-report)**. DEV-A HONOR X7d / Android 15 showed onboarding and Ready-facing local library; owner used a genuine **second physical phone** with no music and showed an Empty outcome. The second model/Android/API are unreported, and individual cases were not mapped across both phones. The owner's final collective PASS declaration covers any cases advanced with 'next' rather than a separate earlier verdict. **Do not conflate this owner assertion with 17 independently witnessed cases or 17 passes per phone.** The media Ready UI text alone does not prove positive indexed count; GS01 Ready and full two-device/native evidence gate remain pending.

**Record:** [P13 owner phone report](ISSUE_5_PHASE13_OWNER_PHONE_TEST_REPORT_2026-10-21.md) · [machine validation](ISSUE_5_PHASE13_VALIDATION.json). No screenshots, image links, ADB output or raw logs added to GitHub. P14 not automatically begun; Issue #5 stays open.
