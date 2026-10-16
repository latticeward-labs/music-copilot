# Issue #5 — 15-Phase Execution Plan (P00–P14)

**Current stage:** P00 PASS, P01 **PASS documentation/CI contract**; P02 State Machine/Permission contract PASS; P03 Bootstrap and First Launch routing PASS; P04 approved two-page Onboarding Compose implementation **IN VALIDATION** as of 2026-10-16. **Execution branch:** `feature/5-onboarding-permissions-recovery`. **Implementation gate:** Issue #4 main merge verified. **Issue #5:** OPEN. Do not implement later phases within Phase 0.

**Source + audit:** [Phase-0 Source/Traceability/DoR](ISSUE_5_PHASE0_SOURCE_TRACEABILITY_DOR.md) · [machine traceability](ISSUE_5_PHASE0_TRACEABILITY.json) · [machine validation](ISSUE_5_PHASE0_VALIDATION.json).

## Phase order and per-phase delivery contract

Every implementation phase must record real code/configuration, relevant unit/Compose UI/integration tests, CI result, screenshot or device proof when claimed, safety/privacy proof and a source-linked PASS decision. A phase with missing evidence is PARTIAL, never self-declared PASS. P00 is a document-only exception with **no app feature coverage**.

| Phase | Purpose | Required exit |
|---|---|---|
| P00 | Source, traceability, Definition of Ready audit | This audit only; baseline frozen and linked. **PASS_DOCUMENTATION_ONLY** |
| P01 | Execution contracts and test fixtures | Create review/evidence index, synthetic fixture, phase gates; no product UX yet. **P01 PASS — CONTRACT/CI ONLY** |
| P02 | Bootstrap and state ownership | Define pure bootstrap/first-use/permission State Machines, persist checkpoint via DataStore, unit-test transition ownership; Android/native/UI binding later. **PASS — COMPILED STATE MACHINE + DATASTORE/UNIT CI [#38864555346](https://github.com/music-copilot-organization/music-copilot/actions/runs/38864555346), NO RUNTIME OS REQUESTS** |
| P03 | Real Bootstrap and First Launch routing | Verified preferences/Room/OS access, four registered destinations, preliminary functional UI, DataStore ACK; **PASS — JVM/Android CI #38869470585, API29 UI #38869470447**. Exact approved UI-copy/design fidelity belongs P04. |
| P04 | **Two approved Onboarding Compose slides** | Match WF-02/HF-02 exact copy/hierarchy; Back/Skip/Finish, local persistence, 48dp, TalkBack labels, scroll/reflow; real JVM + Android 10 UI evidence. **IMPLEMENTED; CI/EMULATOR VERIFICATION PENDING**. Practice Voice affordance disabled until P09. |
| P05 | Request access to audio files | API-aware Android media permission request only after an explicit rationale; no scan without a grant. **NOT_STARTED** |
| P06 | Granted / Denied / Permanently Denied | Native OS outcomes and requestable/permanent distinction, clear explanations and safe fallback. **NOT_STARTED** |
| P07 | Android Settings recovery and permission revocation | Explicit Settings handoff, fresh OS recheck, cancellation/revoked safe recovery. **NOT_STARTED** |
| P08 | Connect permissions to truthful Library Readiness | MediaStore/Room authorization, Empty/Ready/Recoverable, never false playability. **NOT_STARTED** |
| P09 | Six-command education and microphone access | Exactly 6 supported commands, optional Practice Voice, explicit JIT RECORD_AUDIO and separate provider readiness. **NOT_STARTED** |
| P10 | Notification Permission and Cloud STT Consent | Conditional API33+ notification handling and separate processor/purpose/retention disclosure/consent; remote STT stays OFF until Legal/Privacy gate. **NOT_STARTED** |
| P11 | Lifecycle, persistence and Privacy robustness | Rotation, process death, pending operations, no sensitive telemetry/account/audio persistence. **NOT_STARTED** |
| P12 | Accessibility, automated tests and CI | TalkBack/focus, reflow and 48dp at target versions, Compose/integration and negative guards. **NOT_STARTED** |
| P13 | Real Android device QA and Golden Scenario | Physical Android/API/OEM permission recovery, UF/GS-01 truthful readiness evidence. **NOT_STARTED** |
| P14 | Final Acceptance Audit, PR, Merge, close Issue | All 13 real AC evidence, green exact-head CI, independent review or approved documented exception and verified GitHub merge. **NOT_STARTED** |

## Sequencing and authority

1. P01 freezes branch/PR/evidence workflow and synthetic test design.
2. P02 must precede UI/permission implementation: state ownership, cancellation-safe storage and OS authority.
3. P03–P10 build UI and native handoffs according to WF-02, WF-03 and WF-06 only. No extra destination or Android-dialog mock.
4. P08 integrates GS-01 truthful local readiness; the broader UF-04 browsing implementation belongs to its assigned downstream Issue and is not falsely claimed here.
5. P11–P13 require recovery, lifecycle, privacy, API/device, accessibility and synthetic CI proof. **Cloud STT is off by default; activation requires independent consent/disclosure/Legal gates, not this plan.**
6. P14 updates only after 13/13 current Issue #5 criteria have demonstrable evidence, required governance/reviews and green CI. Merge and close Issue #5 only after verified GitHub operations.

## Phase-0 checklist
- [x] Archive and 12/12 source member hashes match frozen authority.
- [x] GitHub current `main`, #4/#15/#16/#17 gates and 51/51 source paths verified.
- [x] 13/13 Issue #5 acceptance criteria traced to sources, phases and test matrix; 17 test scenarios specified.
- [x] DoR 4/4 evidence-linked; runtime/downstream non-claims preserved.
- [x] P01 branch/PR review/evidence/fixture contract prepared on feature branch.
- [x] P01 [real contract GitHub CI run #38819186656](https://github.com/music-copilot-organization/music-copilot/actions/runs/38819186656) PASS; post-closeout final head recheck is recorded in Issue #5.
- [x] P02 [real Gradle/JVM+APK CI #38864555346](https://github.com/music-copilot-organization/music-copilot/actions/runs/38864555346) PASS on implementation head `0e943e03a0f8`; final report-only commit recheck recorded separately in Issue #5.
- [x] P03 [real JVM/Android CI #38869470585](https://github.com/music-copilot-organization/music-copilot/actions/runs/38869470585) on code SHA `f4f071a0`, and [real API29 first-launch emulator #38869071281](https://github.com/music-copilot-organization/music-copilot/actions/runs/38869071281) with screenshot artifact; 13 Issue-wide ACs still pending final acceptance.
- [ ] P04 exact-head JVM/Android UI/emulator audit is pending; P05–P14 further phases remain not started.
- [ ] Issue #5 implementation and final acceptance (13 criteria).

**No source PDF, accepted ADR, Wireframe or Hi-Fi design was changed in Phases 0–1.** Phase-1 CI is a documentation contract gate, not Android testing.

**P04:** [Approved Onboarding UI audit](ISSUE_5_PHASE4_TWO_SLIDE_ONBOARDING_AUDIT.md) · [P04 validation](ISSUE_5_PHASE4_VALIDATION.json). This phase sequence supersedes the proposed P04–P10 names in prior internal plans; prior audit history is preserved.

**P03:** [Bootstrap & First Launch audit](ISSUE_5_PHASE3_BOOTSTRAP_FIRST_LAUNCH_AUDIT.md) · [P03 machine validation](ISSUE_5_PHASE3_VALIDATION.json).

**P02:** [State Machine and Permission Contracts](ISSUE_5_PHASE2_STATE_MACHINE_AND_PERMISSION_CONTRACTS.md) · [Machine validation](ISSUE_5_PHASE2_VALIDATION.json).

**P01:** [Execution contract](ISSUE_5_PHASE1_GITHUB_EXECUTION.md) · [Evidence index](ISSUE_5_EVIDENCE_INDEX.md) · [Validation](ISSUE_5_PHASE1_VALIDATION.json). Proposed final PR to main needs explicit P14 review of develop policy.