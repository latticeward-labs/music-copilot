# Phase 8 — Cross-role Wireframe Review and Correction

**Date:** 27 August 2026  
**Issue:** [#15](https://github.com/latticeward-labs/music-copilot/issues/15)  
**Scope:** all 45 Markdown/SVG pairs, 126 State dispositions, GS-01…GS-05, Foundations, navigation/recovery and the PRD/MVP/ADR constraints represented in the repository  
**Result:** **PASS / COMPLETE at Wireframe feasibility level**  
**Open severity:** **Blocker 0 · Major 0 · Minor 2 downstream/non-blocking**

## Reviewer registry — no fabricated identity

| Name | Reviewer type | Review roles | Date | Result |
|---|---|---|---|---|
| OpenAI Codex | AI-assisted cross-role evidence reviewer | Product; Design/UX; Android feasibility; Accessibility; Privacy | 27 August 2026 | PASS at Wireframe feasibility level |

The authenticated repository actor is `latticeward-labs`. That account records the artifacts and commits; it is not presented as five different people. This review does not claim independent human signatures, Legal approval, a running Compose UI, physical-device execution or vehicle testing.

## Source authorities reviewed

- [Repository PRD](../../PRD.md) and [MVP](../../MVP.md), which mirror the attached official English PRD/MVP.
- ADR-0001…ADR-0006 and repository Architecture/Technical contracts.
- [45-pair artifact index](../README.md) and [machine register](../ARTIFACT_REGISTER.json).
- [Phase 7 traceability](../PHASE_7_TRACEABILITY_VALIDATION.md), navigation and link audits.
- Attached `MSM2(2).rar`: official English PRD, MVP, ADR-0001…0006, architecture, technical documentation and User Flow Phase 0/1 sources.

## Role decisions

| Review | Decision | Evidence |
|---|---|---|
| Product | PASS | P0/P1/P2 scope remains locked; 10 screen families, 13 UFs and five Golden Scenarios remain traceable; no backend/account/streaming/Android Auto/iOS scope entered. |
| Design/UX | PASS after PH8-MAJ-001 | One primary action, Entry/Exit/Back, bounded Empty/Error/Denied/Retry, fixed Driving layer and GitHub Markdown/SVG parity remain coherent. |
| Android feasibility | PASS after PH8-MAJ-001 | Android 10+ system ownership is explicit; playback remains service-owned; dialogs/Settings/notification/lock/route surfaces remain Android handoffs; all App SVG reference frames now match 360×800dp. |
| Accessibility | PASS at specification level | 159 targets remain at least 48dp; 45 visuals have accessible title/description; 32 App records specify TalkBack order, 200% scaling and non-color-only state. Runtime execution remains PH8-MIN-001. |
| Privacy | PASS at Wireframe level | Mic is explicit-action only; raw audio/transcript/media identity are excluded; cloud Voice consent is separate, revocable and forbidden in active Driving Mode; deletion has confirmation and protected categories. Legal/provider approval remains PH8-MIN-002. |

## Findings and correction

| ID | Severity | Finding | Resolution / status |
|---|---|---|---|
| PH8-MAJ-001 | Major | 32/32 App SVGs used a 360×820 inner frame against the 360×800dp Foundations contract. | **Resolved** in [geometry correction commit](https://github.com/latticeward-labs/music-copilot/commit/7480843ce01ee7b8054f6d1313d6f58d88de0785); all 32 corrected. |
| PH8-MAJ-002 | Major | 45/45 records were still approval-pending and no Phase 8 artifact-level evidence package existed. | **Resolved** by this Phase 8 package and dated 45/45 dispositions. |
| PH8-MIN-001 | Minor | Static SVG/Markdown cannot execute Compose semantics, focus order or 200% runtime reflow. | **Open downstream / non-blocking** for Issue #16/#17 implementation QA. |
| PH8-MIN-002 | Minor | Wireframe privacy feasibility is not Product/Legal approval of provider-specific copy. | **Open downstream / non-blocking** for release gate. |

All Blockers and Majors are closed. The two Minors are explicit non-claims and do not require a Wireframe redesign.

## GitHub-native screenshots and Node replacement

Figma Nodes are not an acceptance dependency. The stable `WF-*.md` path is the behavioral node-equivalent; its paired `WF-*.svg` is the rendered visual; an immutable commit URL provides provenance.

- [Product Review evidence](PRODUCT_REVIEW_EVIDENCE.svg)
- [Design/UX Review evidence](DESIGN_UX_REVIEW_EVIDENCE.svg)
- [Android feasibility evidence](ANDROID_FEASIBILITY_REVIEW_EVIDENCE.svg)
- [Accessibility Review evidence](ACCESSIBILITY_REVIEW_EVIDENCE.svg)
- [Privacy Review evidence](PRIVACY_REVIEW_EVIDENCE.svg)
- [Machine-readable findings](PHASE_8_FINDINGS_REGISTER.json)
- [Validation results](PHASE_8_VALIDATION.md)

## Exit decision

The complete repository-native Wireframe set is feasible and internally consistent after correction. There is no open P0 Wireframe gap, invented Android screen, navigation contradiction, Blocker or Major. Phase 8 is **PASS / COMPLETE**. Review changes are carried by [PR #42](https://github.com/latticeward-labs/music-copilot/pull/42); the immutable evidence commit is [`475089654254c96b6adda27c19b697e26dfe798d`](https://github.com/latticeward-labs/music-copilot/commit/475089654254c96b6adda27c19b697e26dfe798d). Issue #15 is ready to close after merge.
