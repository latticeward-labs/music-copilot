# TS-06 — MediaStore, Permission, and Reconciliation Evidence Protocol

**Status:** **PARTIAL — automated AOSP evidence PASS; physical-device scenario execution M01-M14 operator-confirmed PASS; immutable evidence bundle and Android/QA sign-off remain OPEN**  
**Owner:** Android + QA  
**Blocks:** Final UF-02/UF-03/UF-05 and Issue #16  
**Result root:** `docs/user-flows/evidence/results/TS-06/`

> Physical execution status below is recorded from explicit project-owner/operator confirmation on 18 August 2026. This records the test result; it does not fabricate missing artifacts or reviewer signatures. Formal protocol closure still requires build/repository SHAs, timestamped expected-versus-actual evidence, immutable GitHub artifacts, and Android/QA sign-off. Personal media data and unrelated logs are prohibited.

Android permission governs access; MediaStore governs existence/identity; Room is a rebuildable projection only.

| ID | Scenario | Required result | Execution |
|---|---|---|---|
| M01 | Grant + initial scan | Accessible supported audio appears once; no network/account. | PASS — operator confirmed |
| M02 | Denied/requestable | No crash/false Ready; retry/manual recovery. | PASS — operator confirmed |
| M03 | Permanently denied | Explicit Settings; return rechecks truth. | PASS — operator confirmed |
| M04 | Revoke while inactive | Next access reconciles stale rows. | PASS — operator confirmed |
| M05 | Revoke with queued/paused state | Affected selection fails truthfully; valid queue survives where possible. | PASS — operator confirmed |
| M06 | Add then incremental scan | New item appears without duplicate full rebuild. | PASS — operator confirmed |
| M07 | External delete | Stale row unavailable; no false playback. | PASS — operator confirmed |
| M08 | Move/rename | Follow MediaStore; no path authority. | PASS — operator confirmed |
| M09 | Corrupt/unreadable queue item | Bounded skip; continue or Paused/Completed if exhausted. | PASS — operator confirmed |
| M10 | Removable volume unavailable/returned | Availability reconciles non-destructively. | PASS — operator confirmed |
| M11 | Derived index deleted/corrupt | Rebuild truth without deleting audio. | PASS — operator confirmed |
| M12 | 5,000-file initial scan | No blocker/ANR/unbounded memory/duplicates; timing recorded. | PASS — operator confirmed |
| M13 | 5,000-file no-change incremental scan | No false mutations; timing/changed count recorded. | PASS — operator confirmed |
| M14 | Incomplete metadata | Safe fallback; no personal media data in logs/telemetry. | PASS — operator confirmed |

Required API/OEM physical coverage for the core cases is recorded PASS by operator confirmation, alongside the existing automated AOSP API 29/31/34+ evidence. Formal closure remains open until the result bundle records device/build identifiers, measured M12/M13 timing and changed counts, state traces/logs where required, and Android/QA reviewer sign-off.

Rebuild uses transactional scan generations, preserves last verified generation after interruption, never deletes media, and revalidates selected/queued items. Formal PASS requires the immutable evidence bundle and reviewer sign-off in addition to the execution result recorded above.
