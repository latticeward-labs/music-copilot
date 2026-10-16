# Issue #5 — Independent re-audit of Phases 0–4 against owner-uploaded plan

**Date:** 2026-10-16 · **Repository:** `music-copilot-organization/music-copilot` · **Branch:** `feature/5-onboarding-permissions-recovery` · **Issue:** [#5](https://github.com/music-copilot-organization/music-copilot/issues/5) (OPEN).

## Basis and precedence

1. Owner-uploaded `ISSUE_5_PHASED_EXECUTION_PLAN(1).md` SHA-256 `32f8095cac6c4f85a9654bd006ca73e6c27f0db3644a5bd4adef847cf738276f`, dated 2026-10-16, is the audit checklist for this review. Its introductory statement that Issue #4/PR #71 was still Draft and Issue #5 not started is **historical/stale**, not current GitHub truth.
2. Actual attached MSM2 archive has SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`, 12 PDF manifests and source hashes cross-checked under Phase 0. Approved live English PRD/MVP, accepted ADR/Issue #16, approved UF-01/02/03, GS-01, WF-01..04, Issue #17 Hi-Fi/Compose and actual Android OS truth remain source of engineering behavior.
3. [PR #71](https://github.com/music-copilot-organization/music-copilot/pull/71) merged **2026-10-15 22:09:56 UTC** at `4536cccced3cfbfa0e517bd0d81bd4a5a9060364`; [Issue #4](https://github.com/music-copilot-organization/music-copilot/issues/4) CLOSED. [PR #68](https://github.com/music-copilot-organization/music-copilot/pull/68) merged **2026-10-12 09:50:18 UTC** at `567df7ff2cc4ffd1e202a2214b19df98574dca83`; [Issue #17](https://github.com/music-copilot-organization/music-copilot/issues/17) CLOSED. Issue #5 branch is independent, no default branch mutation, 13 ACs still unchecked.
4. **User's 15-phase order is binding:** P00 traceability, P01 GitHub contract, P02 Permission Gateway and State Machine, P03 Bootstrap, P04 two Onboarding slides, P05 audio permission request, P06 OS denial statuses, P07 Settings/revocation, P08 Library gate, P09 six intents/mic, P10 notifications/cloud, P11 lifecycle/privacy, P12 accessibility/CI, P13 physical QA, P14 audit/merge. Earlier internal numbering for P04–P10 was obsolete, corrected in Phase0 machine traceability.

## Verified initial defects (not retroactively declared PASS)

| Phase | Observed discrepancy in previous deliverable | Corrective action and accountability |
|---|---|---|
| **P00** | `ISSUE_5_PHASE0_TRACEABILITY.json` mapped some ACs to **obsolete internal phase numbers** and omitted explicit implementation owner/evidence type per row | Rebased **13/13 AC** rows to the 15-phase owner plan and added `implementation_owner`, `source_of_truth`, `recovery_contract`, `expected_evidence_type`; preserved original Git history, 17 named test scenarios and all original 13 ACs in Issue |
| **P01** | No blocking branch/process change identified; original plan's #4 Draft warning had become stale | Authenticated #4/#17 PR merge and Issue closure independently verified; main ancestry and separate feature branch retained |
| **P02** | Pure reducers existed but **no fakeable Android permission gateway** and **no explicit cancellable one-use `pendingBlockedAction`** | New `core/application/permission/AndroidPermissionGateway.kt`, conservative fail-closed read-only native adapter `app/di/ReadOnlyAndroidPermissionGateway.kt`, `BlockedAction` typed purpose mapping and **ACK-generation-dependent one-shot grant consumption** in `PermissionMachine`; added fake-based JVM tests. Native request and real Android Settings launcher remain P05/P07, **not implemented or claimed early**. `PermanentlyDenied` is a policy projection, never OS boolean certainty. |
| **P03** | `ApplicationBindings.onForeground` called `libraryRepository.reconcile()` as soon as onboarding flag was complete, **without consulting fresh OS media grant**; could initiate unapproved scan | Removed all startup/foreground reconciliation. Only service/controller connection is observed on foreground; real scan is gated in P05/P08. Added regression checks. Verified native media permission is still OS-owned, library/readiness is not fabricated. |
| **P04** | Functional two-page UI lacked an actual Settings-to-education replay entry; **150% font and interrupted second-slide re-entry were not proven** | Activated only the existing approved `Destination.Settings` as a minimal read-only UF-01 education entry; wired `FirstUseScreen(replay=true)` into `FirstUseMachine.EducationReplay` and back to Settings origin. No DataStore, MediaStore/Library, Permission, Queue, Player or Voice mutating effect is emitted during Replay. Extended JVM tests and API29 emulator workflow for 150% two-page screenshots, completed-first-run cold return and interrupted slide-2 continuation. Other Settings WF-10 sections not falsely claimed delivered. |

## Controlled source ownership and negative gates

- **Architecture:** Kotlin Android + Compose single `:app` module; `feature` references MaterialTheme/public application ports; `core.application` remains Android-independent; playback remains Media3 Service owner; no new Account, Identity, STT, cloud, streaming or analytics event.
- **Permission truth:** Android grant is never stored in DataStore. Before P05/P08, the app intentionally does not perform MediaStore scan even on cold/warm startup. A user-facing manual Library fallback never means playback library is actually Ready.
- **P02 outcome:** Fakeable gateway and typed purpose/action contracts, duplicate/stale OS callback guard, explicit request/Settings effects only; Android runtime `ActivityResultContracts.RequestPermission` and `ACTION_APPLICATION_DETAILS_SETTINGS` are **future P05/P07 responsibilities**, not P02 native-PASS.
- **P04 outcome:** Exact two-page HF-02 strings, `Continue`/`Skip`/`Finish without Voice`/`Back` ACKed checkpoint/completion and no mic dialog; `Practice Voice` visibly deferred until P09. Pager semantics, scroll, minimum 48dp controls and 100/200% API29 UI proof retained. New 150% and interruption proof is tracked in a separate real emulator run.

## CI and test evidence after corrections

| Coverage | Actual check | Status at audit writing |
|---|---|---|
| Source & 13 AC matrix | [P01 contract CI #38877216814](https://github.com/music-copilot-organization/music-copilot/actions/runs/38877216814) | **SUCCESS** on updated P00 AC ownership/phase mapping |
| P02 gateway + 5 fake-based tests | [Android Gradle/JUnit/Lint CI #38876126473](https://github.com/music-copilot-organization/music-copilot/actions/runs/38876126473) | **SUCCESS** on `16c644b1b44456d3fd2ee0fb8e4003b2b019fcf5` |
| P03 startup scan safety and Settings return | [Android Gradle/JUnit CI #38876126464](https://github.com/music-copilot-organization/music-copilot/actions/runs/38876126464) · [API29 original first-run UI #38876126480](https://github.com/music-copilot-organization/music-copilot/actions/runs/38876126480) | **SUCCESS** both on `16c644b1b44456d3fd2ee0fb8e4003b2b019fcf5` |
| P04 replay JVM & build + original UI | [P04 Android/Gradle/JUnit #38876334876](https://github.com/music-copilot-organization/music-copilot/actions/runs/38876334876) · [API29/replay 100/200 #38876334672](https://github.com/music-copilot-organization/music-copilot/actions/runs/38876334672) | **SUCCESS** both on `57d2033b0a4866ce85775c882e04e94b4842ae48` |
| P04 **150% and interrupted slide 2** | [API29 extended smoke #38876467719](https://github.com/music-copilot-organization/music-copilot/actions/runs/38876467719) | **PENDING** until actual workflow conclusion is checked; NOT assumed PASS |

**Evidence rules:** report real run and its commit, not claims inferred from scripts. 150% screenshots and Resume evidence count only once the extended emulator run returns SUCCESS. P12 accessibility (TalkBack focus/contrast independent audit), P13 physical/varied API devices and P14 human final review are **still unverified** and are not required to manufacture P04 phase PASS beyond its scoped emulated UI gate.

## P00–P04 acceptance and Issue #5 boundary

The five phases have independent engineering checkpoints, NOT an Issue #5 final acceptance waiver. The original **13 AC checkboxes stay unchecked** pending P05–P14 integrated native/physical/independent review evidence. No PR was merged and Issue #5 remains OPEN. Issue #4 owner-granted expedited phone review does not automatically transfer to Issue #5.

**Readiness to start P05 depends on:** successful extended emulator re-audit, readback of latest branch, P00 AC traceability green, actual Issue annotation and no newly discovered blocker. Do not label P04 complete against the uploaded plan if any of these evidence gates fail.
