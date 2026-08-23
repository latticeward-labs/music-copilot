# TS-06 — MediaStore, Permission, and Reconciliation Evidence Protocol

**Status:** **PASS — automated AOSP evidence PASS; M01-M14 physical MediaStore/reconciliation execution PASS; accepted retrospective measurement waiver; Android/QA approval attestations recorded**  
**Owner:** Android + QA  
**Blocks:** Final UF-02/UF-03/UF-05 and Issue #16  
**Result root:** `docs/user-flows/evidence/results/TS-06/`  
**Latest automated revalidation:** **PASS — [workflow run #32](https://github.com/latticeward-labs/music-copilot/actions/runs/32640155802); 3/3 host reconciliation tests and synthetic MediaStore instrumentation on AOSP API 29/31/34 passed; see the [23 August record](TS-01_TS-03_TS-06_REVALIDATION_2026-08-23.md)**

> The accepted execution bundle is `results/TS-06/2026-08-18-81b9aad/`. Historical raw captures, exact identifiers, and M12/M13 raw numeric measurements that were not retained are covered by the explicit one-time `CLUSTER_B_EVIDENCE_RETENTION_WAIVER_2026-08-18.md`; the bundle records the confirmed PASS outcomes and derived attestations without inventing values. Personal media data and unrelated logs remain prohibited.

Android permission governs access; MediaStore governs existence/identity; Room is a rebuildable projection only.

| ID | Scenario | Required result | Result |
|---|---|---|---|
| M01 | Grant + initial scan | Accessible supported audio appears once; no network/account. | PASS |
| M02 | Denied/requestable | No crash/false Ready; retry/manual recovery. | PASS |
| M03 | Permanently denied | Explicit Settings; return rechecks truth. | PASS |
| M04 | Revoke while inactive | Next access reconciles stale rows. | PASS |
| M05 | Revoke with queued/paused state | Affected selection fails truthfully; valid queue survives where possible. | PASS |
| M06 | Add then incremental scan | New item appears without duplicate full rebuild. | PASS |
| M07 | External delete | Stale row unavailable; no false playback. | PASS |
| M08 | Move/rename | Follow MediaStore; no path authority. | PASS |
| M09 | Corrupt/unreadable queue item | Bounded skip; continue or Paused/Completed if exhausted. | PASS |
| M10 | Removable volume unavailable/returned | Availability reconciles non-destructively. | PASS |
| M11 | Derived index deleted/corrupt | Rebuild truth without deleting audio. | PASS |
| M12 | 5,000-file initial scan | No blocker/ANR/unbounded memory/duplicates. | PASS — raw historical timing retention waived |
| M13 | 5,000-file no-change incremental scan | No false mutations. | PASS — raw timing/changed-count retention waived |
| M14 | Incomplete metadata | Safe fallback; no personal media data in logs/telemetry. | PASS |

Required physical coverage is accepted as PASS by confirmed execution, alongside automated AOSP API 29/31/34 evidence.

## Decision

**PASS.** TS-06 M01-M14 execution, required physical coverage, immutable attestation bundle, artifact Git object IDs, and Android/QA approval attestations are accepted for Cluster B. Unavailable historical M12/M13 numeric measurements and other raw captures are explicitly waived rather than fabricated.
