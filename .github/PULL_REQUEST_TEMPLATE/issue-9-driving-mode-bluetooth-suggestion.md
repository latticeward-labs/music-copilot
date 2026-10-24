# Issue #9 — Driving Mode + optional Bluetooth suggestion (P13 DELIVERY TEMPLATE ONLY)

> **DO NOT OPEN THIS PR IN P01.** This is a template for one final *cumulative* P13 delivery PR after P02–P12 are actually implemented. All boxes default unchecked; do not prefill passes or invented owners.

**Issue:** https://github.com/latticeward-labs/music-copilot/issues/9  
**Proposed head:** `feature/9-driving-mode-bluetooth-suggestion`  
**Proposed base:** `main` (**project-specific exception to CONTRIBUTING.md’s preferred develop integration; must be explicitly approved before PR/merge**). P01 comparison: develop was 1,065 commits behind main. Recheck live branch divergence in P13.  
**Scope:** P0 approved WF-07 Driving and manually controlled playback, P1 removable selected-paired-Bluetooth suggestion. No Android Auto, backend, streaming, wake-word or route-policy rewrite.

## Source and actual one-APK identity
- [ ] All P00–P12 deliverables integrated on **same branch**; implementation HEAD SHA: `TBD`.
- [ ] One final owner-facing debug APK was assembled in P13 from that exact HEAD using **build-only** task; exact bytes SHA-256 `TBD`; exact working download URL `TBD`.
- [ ] Actual build run/workflow and result `TBD`; never claim CI/JVM/Lint or instrumentation PASS from `assembleDebug`.
- [ ] No intermediate per-phase APKs; no tests executed P00–P13 without new explicit user authorization.
- [ ] Manifest/change scope verified: P0 survives disabling/removing P1.

## Accepted sources and trace
- [P00 static audit](../../docs/implementation/ISSUE_9_PHASE0_READINESS_AUDIT.md)
- [P01 execution contract](../../docs/implementation/ISSUE_9_PHASE1_EXECUTION_CONTRACT.md)
- [AC/phase/M-case evidence index](../../docs/implementation/ISSUE_9_EVIDENCE_INDEX.json)
- [Full phase plan](../../docs/implementation/ISSUE_9_PHASED_EXECUTION_PLAN.md)
- [Owner M01–M46 instructions](../../docs/implementation/ISSUE_9_FINAL_PHONE_QA_CHECKLIST.md)
- [Actual status ledger](../../docs/implementation/ISSUE_9_QA_RESULTS.json)
- [HF-07 Screen/Components](../../docs/handoff/SCREEN_SPECS.md)

## Product acceptance (never copy initial source PASS into runtime)
- [ ] AC01 title/artist/artwork/four transports/Mic/Exit
- [ ] AC02 Playing/Paused/No Track
- [ ] AC03 only single service-owned Media3 Player/Queue, safe command ACKs
- [ ] AC04 48dp+ controls, contrast, TalkBack
- [ ] AC05 read order/200% font
- [ ] AC06 no typing/nested distracting controls
- [ ] AC07 explicit manual entry
- [ ] AC08 only optional selected-device Bluetooth suggestion
- [ ] AC09 P1 cleanly cuttable without P0 loss
- [ ] AC10 recreation tracks service state
- [ ] AC11 immediate Exit to approved origin
- [ ] AC12 parked-vehicle scenarios with real applicable source
- [ ] AC13 road-attention accurate disclaimer
- [ ] AC14 authored UI/a11y tests and **actual execution proof if claiming tested PASS**

## Real QA and provenance (do not infer)
- Owner M01–M46 case-by-case results on **same APK SHA**: `TBD`, reporting device, Android API/OEM and accessory; `NOT_RUN` stays `NOT_RUN`.
- GS-04 full STT/Next requires actual Issue #10, or explicitly `BLOCKED`; GS-05 must use **parked vehicle/head unit**, not merely AirPods/headset.
- JVM: `NOT_RUN` unless explicitly authorized and evidenced.
- Android instrumentation: `NOT_RUN` unless explicitly authorized and evidenced.
- Automated accessibility: `NOT_RUN` unless explicitly authorized and evidenced.
- Lint: `NOT_RUN` unless explicitly authorized and evidenced.
- OEM/multiple parked vehicle matrix: `NOT_RUN` unless actually measured.
- Review names/signatures: `TBD`; no AI-authored fictitious independent human approval.

## Regression/privacy/rollback disclosure
- [ ] Android media notification Play, tapping media card to open app and AirPods physical Play preserved.
- [ ] #8 audio focus/phone call/headset/BT disconnection safe Pause and no unintended speaker/autoplay preserved.
- [ ] Bluetooth permission Android 10–11 vs Android 12+, denial, revoke and local saved selection handled.
- [ ] No raw voice/transcript, personal file titles/paths, MAC/device IDs, precise location or contacts in telemetry/logs.
- [ ] P1 whole chooser/observer/suggestion cleanly removable; manual Driving retains actions.
- [ ] Security/manifest/permissions and any DataStore migrations documented as actual changes.
- [ ] Independent code review and issue acceptance policy fulfilled or deferred with owner-authorized, precisely described exception.

**Merge/closeout:** No merge or Issue #9 closure unless owner explicitly authorizes after reported P13 QA, with remaining technical/vehicle/Voice debt tracked; no fake `FULL_PASS`. PR title/description must say `Refs #9` until actual closure conditions are agreed.
