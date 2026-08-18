# TS-01 — Notification and Foreground-Media Evidence Protocol

**Status:** **PASS — automated AOSP evidence PASS; Samsung physical N01-N10 PASS; Pixel/stock and Xiaomi/MIUI-HyperOS physical TS-01 rows APPROVED N/A / WAIVED; accepted evidence bundle and Android/QA approval attestations recorded**  
**Owner:** Android + QA  
**Blocks:** Final UF-02/UF-06 and Issue #16  
**Result root:** `docs/user-flows/evidence/results/TS-01/`

> The accepted execution bundle is `results/TS-01/2026-08-18-81b9aad/`. Historical raw captures that were not retained are covered by the explicit one-time `CLUSTER_B_EVIDENCE_RETENTION_WAIVER_2026-08-18.md`; the bundle records derived attestations rather than inventing raw data. Personal media data and unrelated logs remain prohibited.

## Matrix and approved waiver

API 29, 31, and 34+ are covered by the automated AOSP matrix. Samsung/One UI physical execution completed N01-N10. Pixel/stock and Xiaomi/MIUI or HyperOS physical TS-01 rows are **Approved N/A / Waived** by explicit project-owner decision dated 18 August 2026.

| ID | Scenario | Required result | Result |
|---|---|---|---|
| N01 | First playback | One service-owned player/session; notification matches actual state. | PASS |
| N02 | Home/background while Playing | Playback/position continue; foreground visibility is platform-correct. | PASS |
| N03 | Screen off/lock | Audio continues; lock/system state matches MediaSession. | PASS |
| N04 | Notification commands | Exactly one acknowledged Play/Pause/Next/Previous; all surfaces converge. | PASS |
| N05 | Runtime notification permission where applicable | Contextual prompt after value exists; grant creates no duplicate service. | PASS |
| N06 | Deny/Not now | No crash/coercion/false Stopped/lost app controls; exemption recorded exactly. | PASS |
| N07 | Permanent denial/Settings return | Explicit Settings only; return rechecks truth; platform/OEM notification-setting behavior is recorded truthfully and must not create duplicate authority or an app-specific crash/ANR. | PASS — Samsung OEM behavior reviewed/accepted |
| N08 | Task removal while Playing | Matches TS-02; never creates another player. | PASS |
| N09 | Controller reconnect | Complete snapshot; no false empty queue/Paused state. | PASS |
| N10 | Pause/complete with UI absent | Correct service/notification transition; no unexpected restart. | PASS |

## Decision

**PASS.** Required non-waived rows passed, Samsung physical execution passed, automated AOSP coverage passed, the immutable attestation bundle is committed, artifact Git object IDs are recorded, and Android/QA decisions are accepted by project-owner attestation. Unavailable historical raw captures are explicitly waived rather than fabricated.
