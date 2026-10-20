# Issue #5 — Evidence Index and Synthetic Test Contract

**Source:** [P00 canonical AC/test register](ISSUE_5_PHASE0_TRACEABILITY.json) · **Branch:** `feature/5-onboarding-permissions-recovery`.
**Status:** Phase-0 traceability fixture IDs remain specifications; actual P03 runtime/Phase-4 UI evidence is logged per phase below. The original integrated Issue #5 AC pass count is still 0/13; do not rewrite specification-only T01–T17 as fully passed.

Each future PASS must record tested commit SHA, run/time, actual test and CI path, expected/actual outcome and whether emulator, instrumented physical, or user-reported manual. Only synthetic media, IDs and speech may be used. Never upload real tracks, paths, titles, content URI, raw voice/transcripts, Bluetooth device IDs, permission outcomes or sensitive telemetry. Sanitize UI screenshots and logs.

| Test ID | Required synthetic seam / expected follow-up proof | Status |
|---|---|---|
| T01 | 2-slide fresh run/skip; fake first-install DataStore + Compose UI | NOT_RUN |
| T02 | DataStore write ACK/failure/cancel with fake repository | NOT_RUN |
| T03 | ActivityScenario recreation/restore, service playback authoritative | NOT_RUN |
| T04 | Non-mutating Settings education replay and queue/index unchanged | NOT_RUN |
| T05 | Media permission policy API29/32/33/35; native system request | NOT_RUN |
| T06 | JIT mic tap/practice; skip never prompts; provider separate | NOT_RUN |
| T07 | Notification prompt only when OS needs it after acknowledged playback | NOT_RUN |
| T08 | Granted media + synthetic MediaStore index: Ready or Empty | NOT_RUN |
| T09 | Requestable denial Retry/Not now without auto-loop | NOT_RUN |
| T10 | Permanent denial explicit Android Settings / Not now | NOT_RUN |
| T11 | Settings return recheck: Granted/Denied/Still Permanent | NOT_RUN |
| T12 | Cancel/Back/rotation/repeated callbacks crash-free | NOT_RUN |
| T13 | Revoked/moved/deleted synthetic audio; no false playability | NOT_RUN |
| T14 | Offline/failing speech provider not mistaken for mic denial | NOT_RUN |
| T15 | Remote STT stays OFF; provider-specific consent/Legal gate; no identity | NOT_RUN |
| T16 | TalkBack, focus order, 100/150/200% scaling, 48dp targets/contrast | NOT_RUN |
| T17 | Privacy telemetry negative fixtures with fake song/transcript/permission data | NOT_RUN |

## Evidence ownership

| Phases | Required artifact | Status |
|---|---|---|
| P00 | SHA-locked 12-PDF English source, 51-path/13-AC/17-case baseline, 4/4 DoR | PASS_SPEC_ONLY |
| P01 | Existing branch/ancestry, PR template, reviewer policy and machine CI contract; [initial real run #38819186656](https://github.com/latticeward-labs/music-copilot/actions/runs/38819186656) | PASS_DOC_CONTRACT_ONLY |
| P02 | Pure first-use/bootstrap/permission state machine, DataStore checkpoint, 19 JVM tests and real Android Gradle [CI #38864555346](https://github.com/latticeward-labs/music-copilot/actions/runs/38864555346) | **PASS CONTRACT/JVM ONLY** |
| P03 | Bootstrap, first launch route and real API29 first-run [CI #38869470585](https://github.com/latticeward-labs/music-copilot/actions/runs/38869470585), [emulator #38869470447](https://github.com/latticeward-labs/music-copilot/actions/runs/38869470447) | PASS_BOOTSTRAP_SCOPE |
| P04 | Approved WF-02/HF-02 two-page UI, [Kotlin/JVM/Lint/APK #38872429334](https://github.com/latticeward-labs/music-copilot/actions/runs/38872429334), [API29 UI/200% emulator #38873215678](https://github.com/latticeward-labs/music-copilot/actions/runs/38873215678), [screenshots+APK artifact #12034777234](https://github.com/latticeward-labs/music-copilot/actions/runs/38873215678/artifacts/12034777234) | **PASS_TWO_SLIDES_SCOPE** |
| P05–P06 | Android-owned media request and Granted/Denied/Permanently Denied native flows | NOT_RUN |
| P07 | Android Settings/revocation: 3/3 OS-observed API33 outcomes, native grant/revoke, single scan, JVM/recreation | **PASS_SCOPED** — [whole-green run #39020349923](https://github.com/latticeward-labs/music-copilot/actions/runs/39020349923), [native artifact #12096627786](https://github.com/latticeward-labs/music-copilot/actions/runs/39020349923/artifacts/12096627786); restored source/test workflow blob-identical to green commit. |
| P08 | Library readiness / full scan recovery | NOT_RUN |
| P09–P10 | Six-command education/JIT microphone and Notification/Cloud STT consent | **P09 PASS_SCOPED**; **P10 7/7 OWNER-REPORTED PHONE PASS**, separate technical verification pending |
| P11–P12 | Rotation/recreation, privacy-negative tests, Compose TalkBack/scaling/contrast | **P11 IMPLEMENTED / 11 JVM TESTS AUTHORED BUT NOT RUN / PHONE QA PENDING**; P12 NOT_STARTED |
| P13 | API29/32/33/35 emulator/physical/native Settings and permission QA; sanitized artifact identity | NOT_RUN |
| P14 | 13/13 actual AC, exact-head green CI, independent review, approved base exception, real PR merge/Issue closure | NOT_RUN |

## Gates never waived by P01

PH10-MIN-001 actual Compose accessibility QA; PH10-MIN-002 Legal/Privacy before any cloud STT; PH10-MIN-003 OEM/vehicle release QA (Issue #13); deferred Issue #17 human visual review. Issue #4 single-handset and expedited-review exception applies ONLY to #4. Design/static checks cannot stand in for Android permission/runtime proof.

**P01 files:** [Execution contract](ISSUE_5_PHASE1_GITHUB_EXECUTION.md) · [Machine validation](ISSUE_5_PHASE1_VALIDATION.json) · [P00–P14 plan](ISSUE_5_PHASED_EXECUTION_PLAN.md).


## Phase 0–4 owner-plan re-audit (2026-10-16)

[Audit and corrections](ISSUE_5_REAUDIT_P00_P04.md) · [Machine ledger](ISSUE_5_REAUDIT_P00_P04.json). Owner's uploaded 15-phase order controls. Original P00 traceability AC phase numbers were rebased (13/13 now have owner, truth, recovery and expected evidence). Native OS prompt before P05 was **not** added. P02 fakeable permission gateway and one-shot BlockedAction: [CI #38876126473](https://github.com/latticeward-labs/music-copilot/actions/runs/38876126473) SUCCESS. P03 foreground unsafe auto-scan removed: [CI #38876126464](https://github.com/latticeward-labs/music-copilot/actions/runs/38876126464) SUCCESS and [original API29 smoke #38876126480](https://github.com/latticeward-labs/music-copilot/actions/runs/38876126480) SUCCESS. P04 Settings replay/ACK safety: [CI #38876334876](https://github.com/latticeward-labs/music-copilot/actions/runs/38876334876) SUCCESS, [API29 original replay #38876334672](https://github.com/latticeward-labs/music-copilot/actions/runs/38876334672) SUCCESS. Extended [P04 150%/slide2-restart emulator #38876467719](https://github.com/latticeward-labs/music-copilot/actions/runs/38876467719) PENDING. **Issue-wide AC accepted remains 0/13** until final native and physical/review gates.


### Additional P04 destination ViewModel correction

The approved code structure expects a lifecycle-owned ViewModel/SavedState. `FirstUseViewModel.kt` now owns reducer and ACK effects; `FirstUseScreen.kt` is UI only. [P04 JVM+Android CI #38879558692](https://github.com/latticeward-labs/music-copilot/actions/runs/38879558692) SUCCESS on `b5bd121f`; [P03 regression #38879558699](https://github.com/latticeward-labs/music-copilot/actions/runs/38879558699) SUCCESS. [API29 updated ViewModel UI #38879465871](https://github.com/latticeward-labs/music-copilot/actions/runs/38879465871) still IN_PROGRESS at writing; no device success claimed for changed ViewModel until it completes.


**Final P00–P04 native re-audit correction:** [JVM/Lint/APK #38879558692](https://github.com/latticeward-labs/music-copilot/actions/runs/38879558692) SUCCESS; [API29 current ViewModel UI #38879465871](https://github.com/latticeward-labs/music-copilot/actions/runs/38879465871) SUCCESS. Screenshot/permission/APK [artifact #12037690290](https://github.com/latticeward-labs/music-copilot/actions/runs/38879465871/artifacts/12037690290) covers 100/200% and 150% Settings Replay, suspended slide-2 force-stop and no mic/media auto grants. Owner P05 next, 13 Issue ACs still pending.


### Phase 5 verified OS-native media permission evidence

P05 **PASS_SCOPED** — [JVM/Lint/APK #38884070402](https://github.com/latticeward-labs/music-copilot/actions/runs/38884070402) SUCCESS; [Android10/API29 + Android13/API33 native permission #38887620769](https://github.com/latticeward-labs/music-copilot/actions/runs/38887620769) **BOTH SUCCESS** on `41e0f6ed52bcfbbe5a67b15caaa6451483921741`. [API29 APK/screenshots #12040133723](https://github.com/latticeward-labs/music-copilot/actions/runs/38887620769/artifacts/12040133723) and [API33 APK/screenshots #12041121532](https://github.com/latticeward-labs/music-copilot/actions/runs/38887620769/artifacts/12041121532) contain redacted/synthetic media test evidence. True native Deny→manual fallback, explicit Allow→fresh OS permission check→one media scan, no implicit startup scan, no Mic/Notification request. P06–P14 still open; 13 original integrated ACs not formally checked.


### P06 three exact WF-03 result surfaces — real split-job native proof

[Audit](ISSUE_5_PHASE6_PERMISSION_OUTCOMES_AUDIT.md) · [machine results](ISSUE_5_PHASE6_VALIDATION.json). [8 new P06 Kotlin/JVM tests + full Lint/Detekt/Spotless/APK #38894487796](https://github.com/latticeward-labs/music-copilot/actions/runs/38894487796) **SUCCESS** and [P05 JVM regression #38894487783](https://github.com/latticeward-labs/music-copilot/actions/runs/38894487783) **SUCCESS**. [Older P06 native matrix #38894791348](https://github.com/latticeward-labs/music-copilot/actions/runs/38894791348) FAILED. See final split-job OS-proven PASS scope below; no parent failed matrix is relabeled green. Issue-wide ACs 0/13 accepted.


**P06 final phase-scope audit (2026-10-16):** [JVM/Lint/APK #38894487796](https://github.com/latticeward-labs/music-copilot/actions/runs/38894487796) SUCCESS. [API29 native job #116979950300](https://github.com/latticeward-labs/music-copilot/actions/runs/38903110035) SUCCESS, artifact [#12047376415](https://github.com/latticeward-labs/music-copilot/actions/runs/38903110035/artifacts/12047376415): DeniedRequestable/USER_SET and Granted. [API33 native job #116971017312](https://github.com/latticeward-labs/music-copilot/actions/runs/38900423830) SUCCESS, artifact [#12046745585](https://github.com/latticeward-labs/music-copilot/actions/runs/38900423830/artifacts/12046745585): DeniedRequestable/USER_FIXED Settings-only/Granted. **Parent P06 workflow runs themselves are failed due separate opposite-API emulator boot/SystemUI ANR; neither falsely marked green.** [P05 API29+API33 native regression #38903321428](https://github.com/latticeward-labs/music-copilot/actions/runs/38903321428) whole-run SUCCESS, 2 artifacts. P06 **PASS within UI/state contract across verified complementary OS jobs**, not API29 full USER_FIXED or physical QA. P07 next; original Issue #5 13 ACs remain formally unchecked.


### P07 Settings recovery and revocation — source-equivalent real native PASS (17 October 2026)

[P07 acceptance audit](ISSUE_5_PHASE7_SETTINGS_REVOCATION_AUDIT.md) · [P07 machine validation](ISSUE_5_PHASE7_VALIDATION.json). [Whole-green JVM + real Android13 Settings run #39020349923](https://github.com/latticeward-labs/music-copilot/actions/runs/39020349923) at `9ebe9d5`, [JVM/APK artifact #12096631289](https://github.com/latticeward-labs/music-copilot/actions/runs/39020349923/artifacts/12096631289), [native OS state/screenshots #12096627786](https://github.com/latticeward-labs/music-copilot/actions/runs/39020349923/artifacts/12096627786). Granted/DeniedRequestable/StillSettingsOnly, explicit Settings, Back, one-use Continue/scan, real Settings UI grant/revoke and Android process-kill recovery verified. Requestable return was created with explicitly recorded OS flag fixture, not fabricated Settings user gesture. Later commits modify only P07 test harness, not production code; their Android UI emulator runs #39020477545/#39021551274 failed at OS SystemUI/boot and are NOT called green. **P07 PASS_SCOPED**, no parent Issue #5 integrated AC accepted yet; API29 SettingsOnly, physical/OEM, formal a11y and independent review remain pending.


### P08 Library Readiness — PASS_SCOPED, verified composite evidence (17 October 2026)

[P08 audit](ISSUE_5_PHASE8_LIBRARY_READINESS_AUDIT.md) · [P08 validation JSON](ISSUE_5_PHASE8_VALIDATION.json). Real Android13 native job [#117375760627](https://github.com/latticeward-labs/music-copilot/actions/runs/39025569472) **SUCCESS**: native Allow, verified Empty, explicit Rescan, real Settings revoke, permission recovery, final native PASS marker. JVM/Static/Spotless/Detekt/Lint/APK job [#117379277415](https://github.com/latticeward-labs/music-copilot/actions/runs/39026317313) **SUCCESS**: fake MediaStore/Room permission scan, one-shot, revocation during scan, race, recovery and derived index tests. GitHub compare confirms byte-identical `app/src/main/**` source across both passed jobs and branch implementation; changes only in test code, harness and docs. **No whole-green single workflow or last-harness run is asserted.** Newer SystemUI ANR and zero-step GitHub runner failures remain recorded. P08 requirements met on complementary evidence; parent Issue #5 stays OPEN, P09 NEXT, full app 13 ACs pending.


### P09 — PASS_SCOPED, real JVM + Android13 whole-green CI (18 October 2026)

**PASS_SCOPED — both CI jobs SUCCESS on exact commit `9fb0603ac6af`.**
[Source-linked P09 audit](ISSUE_5_PHASE9_MIC_VOICE_BOUNDARY_AUDIT.md) · [machine validation](ISSUE_5_PHASE9_VALIDATION.json). The second approved Onboarding slide displays only PLAY, PAUSE, NEXT, PREVIOUS, SEEK_BACK, PLAY_ARTIST. Optional Practice shows independent purpose rationale before Android `RECORD_AUDIO`; the callback rechecks OS, then independently probes the existing on-device STT provider. New pure `VoicePracticeGate` delegates the one-use blocked action to P02 `PermissionMachine`, differentiates OS denied/provider unavailable/language unsupported/cloud consent declined, and preserves manual-only, no auto capture. No new session, cloud provider, direct playback/Queue action or mic on startup/Skip/Driving entry. [GitHub P09 CI #39031581585](https://github.com/latticeward-labs/music-copilot/actions/runs/39031581585) and [runner-image retry #39031814318](https://github.com/latticeward-labs/music-copilot/actions/runs/39031814318), including latter rerun attempt 2, all FAILED before first job step and generated no build/emulator evidence. Those October 17 failed-before-steps runs are **historical**; after the private repo transferred to `latticeward-labs`, [run #39122946667](https://github.com/latticeward-labs/music-copilot/actions/runs/39122946667) successfully executed *both* P09 jobs. [JVM artifact #12135072645](https://github.com/latticeward-labs/music-copilot/actions/runs/39122946667/artifacts/12135072645), [Android13 native artifact #12134589753](https://github.com/latticeward-labs/music-copilot/actions/runs/39122946667/artifacts/12134589753). Real Android OS Deny → manual fallback → explicit Allow → Skip to Media Recovery, no crash. P09 **PASS_SCOPED**. P08 accepted status unaffected; P10 not started. 


### P10 Notification Exemption / Cloud STT Consent — owner phone QA pending (18 October 2026)

[Implementation and platform audit](ISSUE_5_PHASE10_NOTIFICATION_CLOUD_CONSENT_AUDIT.md) · [Machine status](ISSUE_5_PHASE10_VALIDATION.json) · [Owner phone checklist](ISSUE_5_PHASE10_PHONE_QA_CHECKLIST.md). P10 adds no POST_NOTIFICATIONS request for MediaSession-only playback, no remote STT approval or endpoint; fail-closed privacy gate, DataStore consent revocation in Settings. Kotlin unit tests authored but **not executed** under user's low-cost APK-only / owner-performed phone QA constraint. Source commit and APK artifact to be recorded after separate marker-commit build. **NOT PASS without owner report and remaining technical gates.**

**P10 built APK evidence (18 October 2026):** [APK-only workflow run #39158645552](https://github.com/latticeward-labs/music-copilot/actions/runs/39158645552) SUCCESS, source+marker commit `58150070e9ace936eda6d43f4d562903fb784996`, Gradle `assembleDebug` success. [Download owner-phone APK artifact #12147657477](https://github.com/latticeward-labs/music-copilot/actions/runs/39158645552/artifacts/12147657477) (ZIP includes app-debug.apk, SHA-256, BUILD_PROVENANCE; 7-day retention). **No emulator or phone test conducted by assistant; owner's later report recorded below.** Unit tests authored, not run. Formal phase acceptance requires owner report and remaining technical gates. 


### P10 — Owner-reported physical phone QA, 7/7 PASS (18 October 2026)

**Source:** direct owner report in project chat. Device **HONOR X7d / Android 15**, APK **artifact #12147657477**. The owner explicitly reported **P10-01 PASS, P10-02 PASS, P10-03 PASS, P10-04 PASS, P10-05 PASS, P10-06 PASS, P10-07 PASS** and «همه پاس شد». Count: **7 owner-reported PASS, 0 FAIL, 0 NOT RUN**. See [owner's P10 device report](ISSUE_5_PHASE10_OWNER_PHONE_TEST_REPORT_2026-10-18.md) and [P10 QA checklist](ISSUE_5_PHASE10_PHONE_QA_CHECKLIST.md).

**Evidence quality:** owner self-report, **not independent device attestation**; no screenshots, logcat/dumpsys, local APK SHA-256, installation mode or BUILD_PROVENANCE file were supplied. Conditional audio/rotation/OS toggle substeps were not separately described. [APK build #39158645552](https://github.com/latticeward-labs/music-copilot/actions/runs/39158645552) remains build-only; P10 policy JVM tests **authored but NOT RUN**. No API29/32/33+ cross-device evidence is inferred from the owner's Android 15 handset, and there is no approved remote Cloud STT provider to exercise.

**P10 decision:** **PHONE_QA_OWNER_REPORTED_PASS_7_OF_7 / TECHNICAL_GATES_PENDING — NOT FULL PHASE PASS**. Issue #5 remains OPEN; 13 integrated ACs not automatically signed off, P11 untouched.


### P11 — Lifecycle, persistence and privacy implementation (18 October 2026)

**Implementation recorded on `feature/5-onboarding-permissions-recovery`.** [P11 audit](ISSUE_5_PHASE11_LIFECYCLE_PRIVACY_AUDIT.md) · [P11 machine ledger](ISSUE_5_PHASE11_VALIDATION.json) · [owner's phone checklist](ISSUE_5_PHASE11_PHONE_QA_CHECKLIST.md).

- **Permission UI restore:** Denied/SettingsOnly only in SavedStateHandle; OS permission and one-use scan authorization are never persisted. Existing Settings ticket restores without auto-launch, rechecks actual Android grant on return. A killed process does not restore a stale Granted UI as scan permission.
- **Mic/voice UI restore:** rationale/harmless terminal feedback and Android's already-pending ActivityResult are restored conservatively. ReadyForVoiceHandoff is not restored, no automatic capture, cloud access or second Android prompt.
- **First use/replay:** DataStore ACKed initial checkpoint remains authoritative; read-only Replay retains its page in SavedStateHandle without a DataStore update or extra onboarding slide.
- **Cancellation/privacy:** storage validation and quality event publisher rethrow CancellationException; no raw failure strings exposed. Existing typed/NoOp telemetry remains deny-by-default; P11 negative fixtures cover synthetic song/voice/path/URI/device/Bluetooth/location/permission-result strings.
- **Authored:** `Issue5Phase11LifecyclePrivacyTest.kt`, **12 JVM tests**, plus updated P07 restoration expectation. **NOT EXECUTED** under current owner-requested APK-only cost protocol; no automatic JVM/Lint/Detekt/static/a11y/emulator runs or success inferred. P11 phone cases P11-01..11 subsequently **11/11 PASS as owner-reported** (19 October 2026), without screenshots; see owner report below.

**P11 STATUS:** `IMPLEMENTED / FINAL APK BUILD SUCCESS / OWNER-PHONE-QA PENDING / TECHNICAL VALIDATION PENDING — NOT FULL PASS`. [final P11 APK-only run #39163991680](https://github.com/latticeward-labs/music-copilot/actions/runs/39163991680) **SUCCESS** (`assembleDebug`, no other validations), [artifact #12149209198](https://github.com/latticeward-labs/music-copilot/actions/runs/39163991680/artifacts/12149209198), SHA `629caa6a1a3ebafd676cfa3fa8fb5b808c24de46`, APK SHA-256 `d6f93b5a5860c889ae02357d933c0584b7a40a6c1e1b388a14906bca4c4df978`. Earlier compiled P11 run #39163624612 is superseded. The already owner-reported P10 7/7 PASS is preserved separately. P11 work does not close P10's pending JVM gates or satisfy the 13 issue-wide ACs; Issue #5 stays OPEN and P12 not started.


### P11 — Owner physical phone QA report, 19 October 2026

**Owner's explicit self-report:** HONOR X7d / Android 15 / [P11 APK artifact #12149209198](https://github.com/latticeward-labs/music-copilot/actions/runs/39163991680/artifacts/12149209198). `P11-01..P11-11` individually reported **PASS**, **11/11 owner-reported PASS; 0 FAIL; 0 NOT RUN**. Owner's direct notes: «موردی گزارش نشده» followed by «همه ی تست ها اوکی بوده». Follow-up clarified the Settings-only `Open Settings` button correctly opened Android App info; P11-07 and P11-08 remain **owner-reported** PASS, not independent automated confirmation of every underlying OS/scan action. [Attributed P11 owner report](ISSUE_5_PHASE11_OWNER_PHONE_TEST_REPORT_2026-10-19.md) · [P11 checklist](ISSUE_5_PHASE11_PHONE_QA_CHECKLIST.md) · [P11 validation JSON](ISSUE_5_PHASE11_VALIDATION.json).

**Screenshot handling:** Owner explicitly prohibited screenshot registration. **No image, attachment or screenshot URL was added** to the repository, Issue, or Evidence Index; no logs or personal media evidence submitted. APK local install mode, actual phone-side checksum, raw OS trace and conditional substeps were not separately documented.

**Acceptance:** `P11 IMPLEMENTED / APK BUILD SUCCESS / PHONE QA OWNER-REPORTED 11/11 PASS / TECHNICAL VALIDATION PENDING — NOT FULL P11 PASS`. 12 P11 lifecycle/privacy JVM tests authored but **NOT RUN**; P10 deferred JVM gates unchanged. P12/13 cross-API/accessibility/device requirements and P14 integrated acceptance remain outstanding. Issue #5 **OPEN** with 13 original AC boxes unchanged; no merge.


### P12 — Accessibility / automated and cross-API validation contract (19 October 2026)

**Source plan authority:** user-uploaded P12 contract; see [P12 17-case test matrix](ISSUE_5_TEST_MATRIX.md), [Accessibility audit](ISSUE_5_ACCESSIBILITY_AUDIT.md), [P12 phone QA](ISSUE_5_PHASE12_PHONE_QA_CHECKLIST.md) and [machine validation](ISSUE_5_PHASE12_VALIDATION.json).

- **Code updated:** media permission rationale now provides a one-time focusable semantic heading without native auto-request; Library/Settings/Bootstrap titles marked as TalkBack heading. Existing 48dp touch sizes and scrollable reflow retained; approved WF-02/WF-03, Media3 and permission ownership unchanged.
- **Automation authored, NOT RUN:** `Issue5Phase12AccessibilityPolicyTest.kt` **6 JVM tests** (token contrast, negative fixture, headings/touch targets, reflow, no auto-permission, privacy); `Issue5Phase12AccessibilityUiTest.kt` **6 Compose instrumented tests** (native rationale/denial/Settings/grant UI, pending action disabled, 200% font-scale/48dp actions). Existing P02–P11 regressions and P10/P11 deferred policy JVM tests are **not automatically PASS**.
- **Cross-API CI authored:** opt-in `issue-5-phase12-accessibility-validation.yml` (JVM/Spotless/Detekt/Lint and one selected Android OS emulator per run, API29/31/33/35, API36 optional), Android OS truth smoke script `scripts/issue5_phase12_native_test.sh`, deliberately zero screenshot attachments/log artifacts. **None run** while owner's APK-only cost control is in effect; no CI run ID or green matrix may be claimed.
- **Phone testing required:** TalkBack focus/order, accessible named actions, 200% font when supported, 48dp, high-contrast status, denial/Settings recovery, notification/Cloud STT truth, no double action on HOME/rotation. Owner tests P12-01..12 **NOT RUN pending P12 APK delivery and owner's results**. Per explicit instruction, do **not** register screenshots.

**Initial P12 status:** `IMPLEMENTATION_RECORDED / APK_BUILD_PENDING / P12 TECHNICAL AND OWNER QA PENDING — NOT FULL PASS`. P10 manual 7/7 and P11 manual 11/11 previously recorded remain unchanged, with their technical gates outstanding. Original 13 Issue #5 integrated ACs not accepted, Issue OPEN and P13 not started.


**P12 APK build evidence (19 October 2026):** [GitHub run #39243014999](https://github.com/latticeward-labs/music-copilot/actions/runs/39243014999) **SUCCESS**, exact build commit `a152312ca3ce77151df5df1329b804746e567333`, [artifact #12181988538](https://github.com/latticeward-labs/music-copilot/actions/runs/39243014999/artifacts/12181988538), APK SHA-256 `ba4425364016ab9c969095756a01a57f5c814624c8166e247e707cd41cc23dc8`, 35801343 bytes. Original ZIP included hash and provenance; extracted file independently matched. This is `assembleDebug` only. P12 **6 authored JVM + 6 authored Compose UI tests, opt-in full lint/static and native API matrix have NOT BEEN RUN**; owner P12-01..12 phone QA also pending. No screenshots added to GitHub. P12 `IMPLEMENTED / APK_BUILD_SUCCESS / PHONE_QA_AND_TECHNICAL_GATES_PENDING — NOT FULL PASS`.


### P12 — Owner-confirmed physical-phone QA 12/12 PASS (21 October 2026)

**Source:** owner directly confirmed in this conversation that all `P12-01` through `P12-12` tests were performed and passed on **HONOR X7d / Android 15**. Results **12 owner-reported PASS, 0 FAIL, 0 NOT RUN**. Association: [P12 APK-only run #39243014999](https://github.com/latticeward-labs/music-copilot/actions/runs/39243014999), [artifact #12181988538](https://github.com/latticeward-labs/music-copilot/actions/runs/39243014999/artifacts/12181988538). [Attributed owner report](ISSUE_5_PHASE12_OWNER_PHONE_TEST_REPORT_2026-10-21.md) · [P12 validation](ISSUE_5_PHASE12_VALIDATION.json).

**Evidence boundary:** This is owner self-attestation; no independent ADB/OS logs, measured screen-reader traversal, measured font-scale percentage, on-phone APK SHA, screenshot or separate per-conditional-step records. User explicitly prohibited screenshots in GitHub: **none added, embedded or linked**. In particular, an exact 200% font setting, 48dp native measurement, all OEM/API versions, zero accessibility blockers and privacy telemetry tests are **NOT** established by this report.

**Status:** `P12 IMPLEMENTED / APK BUILD SUCCESS / OWNER-REPORTED PHONE QA 12/12 PASS / 6 JVM + 6 Compose UI + STATIC/LINT + NATIVE API MATRIX PENDING — NOT FULL P12 PASS`. P10/P11 deferred technical gates unchanged; Issue #5 still OPEN with 13 original integrated AC unchecked, P13 not started; no merge.
