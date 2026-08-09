# TS-06 — MediaStore, Permission, and Reconciliation Evidence Protocol

**Status:** **PARTIAL — automated AOSP evidence PASS; mandatory physical-device rows and Android/QA sign-off remain BLOCKED**  
**Owner:** Android + QA  
**Blocks:** Final UF-02/UF-03/UF-05 and Issue #16  
**Result root:** `docs/user-flows/evidence/results/TS-06/`

> This protocol is not executed evidence. PASS requires an installable Music Copilot build, required device/API/OEM coverage, build/repository SHAs, a timestamped state trace, expected-versus-actual results, immutable GitHub artifacts, and reviewer sign-off. Personal media data and unrelated logs are prohibited.

Android permission governs access; MediaStore governs existence/identity; Room is a rebuildable projection only.

| ID | Scenario | Required result |
|---|---|---|
| M01 | Grant + initial scan | Accessible supported audio appears once; no network/account. |
| M02 | Denied/requestable | No crash/false Ready; retry/manual recovery. |
| M03 | Permanently denied | Explicit Settings; return rechecks truth. |
| M04 | Revoke while inactive | Next access reconciles stale rows. |
| M05 | Revoke with queued/paused state | Affected selection fails truthfully; valid queue survives where possible. |
| M06 | Add then incremental scan | New item appears without duplicate full rebuild. |
| M07 | External delete | Stale row unavailable; no false playback. |
| M08 | Move/rename | Follow MediaStore; no path authority. |
| M09 | Corrupt/unreadable queue item | Bounded skip; continue or Paused/Completed if exhausted. |
| M10 | Removable volume unavailable/returned | Availability reconciles non-destructively. |
| M11 | Derived index deleted/corrupt | Rebuild truth without deleting audio. |
| M12 | 5,000-file initial scan | No blocker/ANR/unbounded memory/duplicates; timing recorded. |
| M13 | 5,000-file no-change incremental scan | No false mutations; timing/changed count recorded. |
| M14 | Incomplete metadata | Safe fallback; no personal media data in logs/telemetry. |

Run API 29/31/34+ with correct audio-permission model; repeat core cases on Pixel/Samsung/Xiaomi. Rebuild uses transactional scan generations, preserves last verified generation after interruption, never deletes media, and revalidates selected/queued items. PASS requires complete matrix, measured scale/incremental behavior, and no false playability, destructive change, crash, database authority, or privacy leak.
