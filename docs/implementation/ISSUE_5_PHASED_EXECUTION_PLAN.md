# Issue #5 — 15-Phase Execution Plan (P00–P14)

**Current stage:** P00 **PASS** as of 2026-10-16. **Execution branch:** `feature/5-onboarding-permissions-recovery`. **Implementation gate:** Issue #4 main merge verified. **Issue #5:** OPEN. Do not implement later phases within Phase 0.

**Source + audit:** [Phase-0 Source/Traceability/DoR](ISSUE_5_PHASE0_SOURCE_TRACEABILITY_DOR.md) · [machine traceability](ISSUE_5_PHASE0_TRACEABILITY.json) · [machine validation](ISSUE_5_PHASE0_VALIDATION.json).

## Phase order and per-phase delivery contract

Every implementation phase must record real code/configuration, relevant unit/Compose UI/integration tests, CI result, screenshot or device proof when claimed, safety/privacy proof and a source-linked PASS decision. A phase with missing evidence is PARTIAL, never self-declared PASS. P00 is a document-only exception with **no app feature coverage**.

| Phase | Purpose | Required exit |
|---|---|---|
| P00 | Source, traceability, Definition of Ready audit | This audit only; baseline frozen and linked. **PASS_DOCUMENTATION_ONLY** |
| P01 | Execution contracts and test fixtures | Create review/evidence index, synthetic fixture, phase gates; no product UX yet. **NOT_STARTED** |
| P02 | Bootstrap and state ownership | Wire application/use cases, DataStore ack, lifecycle-safe state holders. **NOT_STARTED** |
| P03 | Two-slide onboarding UI | Implement WF-02 value copy, Continue/Skip/Back, replay, six-command education. **NOT_STARTED** |
| P04 | Android permission capability policy | Version-aware permission types, OS current-truth states, rationale/request history. **NOT_STARTED** |
| P05 | Media permission rationale and denial screens | WF-03 native handoff, granted/requestable/permanent branching. **NOT_STARTED** |
| P06 | Settings, cancellation and revocation recovery | Explicit Settings, three-way return, no loops, safe re-entry. **NOT_STARTED** |
| P07 | JIT microphone and optional practice gate | Explicit mic action, provider separate from grant, Voice preserves manual flow. **NOT_STARTED** |
| P08 | Deferred notification permission | Version and value contextual handling, preserving playback on denial. **NOT_STARTED** |
| P09 | Cloud consent/privacy and absence-of-identity gates | Fail closed; no provider-specific remote STT without approval; telemetry negative gates. **NOT_STARTED** |
| P10 | Local library readiness and GS-01 integration | MediaStore scan/index truth, Empty/Ready/error/revoked, no false playability. **NOT_STARTED** |
| P11 | State restoration and resilience tests | Rotation, process death, background/Settings, cancellation/error idempotence. **NOT_STARTED** |
| P12 | Compose accessibility and automated test coverage | TalkBack semantics, scalable text and contrast, unit/UI/integration, privacy. **NOT_STARTED** |
| P13 | Android API/device and full Golden Scenario QA | Emulators and physical QA evidence: UI, Settings, multiple API levels, fallback. **NOT_STARTED** |
| P14 | Final audit, green CI, PR, review, merge and issue closeout | Only after 13 real AC pass; independent reviews per governance or explicit documented exception. **NOT_STARTED** |

## Sequencing and authority

1. P01 freezes branch/PR/evidence workflow and synthetic test design.
2. P02 must precede UI/permission implementation: state ownership, cancellation-safe storage and OS authority.
3. P03–P09 build UI and native handoffs according to WF-02, WF-03 and WF-06 only. No extra destination or Android-dialog mock.
4. P10 integrates GS-01 truthful local readiness; the broader UF-04 browsing implementation belongs to its assigned downstream Issue and is not falsely claimed here.
5. P11–P13 require recovery, lifecycle, privacy, API/device, accessibility and synthetic CI proof. **Cloud STT is off by default; activation requires independent consent/disclosure/Legal gates, not this plan.**
6. P14 updates only after 13/13 current Issue #5 criteria have demonstrable evidence, required governance/reviews and green CI. Merge and close Issue #5 only after verified GitHub operations.

## Phase-0 checklist
- [x] Archive and 12/12 source member hashes match frozen authority.
- [x] GitHub current `main`, #4/#15/#16/#17 gates and 51/51 source paths verified.
- [x] 13/13 Issue #5 acceptance criteria traced to sources, phases and test matrix; 17 test scenarios specified.
- [x] DoR 4/4 evidence-linked; runtime/downstream non-claims preserved.
- [ ] P01 execution setup.
- [ ] Issue #5 implementation and final acceptance (13 criteria).

**No source PDF, accepted ADR, Wireframe or Hi-Fi design was changed in Phase 0.**