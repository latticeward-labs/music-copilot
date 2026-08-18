# TS-02 — UI, Service, Task, and Process Lifecycle Evidence Protocol

**Status:** **PASS — automated AOSP evidence PASS; L01-L12 physical lifecycle matrix PASS; accepted evidence bundle and Android/QA approval attestations recorded**  
**Owner:** Android + QA  
**Blocks:** Final UF-05/UF-06 and Issue #16  
**Result root:** `docs/user-flows/evidence/results/TS-02/`

> The accepted execution bundle is `results/TS-02/2026-08-18-81b9aad/`. Historical raw captures/identifiers that were not retained are covered by the explicit one-time `CLUSTER_B_EVIDENCE_RETENTION_WAIVER_2026-08-18.md`; the bundle records derived attestations rather than inventing raw data. Personal media data and unrelated logs remain prohibited.

P0 means live service continuity; P1 begins only after active lifecycle loss and always restores Paused with explicit Resume.

| ID | Transition | Required result | Result |
|---|---|---|---|
| L01 | Recreation while Playing | Same service/session/queue; full reconnect; monotonic position. | PASS |
| L02 | Recreation while Paused | Remains Paused; no auto-play. | PASS |
| L03 | Home/background | Playback continues; controller changes do not change authority. | PASS |
| L04 | Screen off/lock | Playback/system surfaces remain synchronized. | PASS |
| L05 | Activity finish/UI close | Live playback continues; reopen observes, never reconstructs. | PASS |
| L06 | User task removal | OEM result recorded honestly; no false universal promise. | PASS |
| L07 | Controller reconnect | Queue/item/state/position atomically replace placeholders. | PASS |
| L08 | Cached process kill without active service | Relaunch enters P1 validation; no automatic audio. | PASS |
| L09 | Process loss after Paused snapshot | Restores Paused; explicit Resume alone plays. | PASS |
| L10 | Process loss with invalid media | Drop invalid entries; empty routes to Library/UF-03. | PASS |
| L11 | Force-stop negative control | Kept separate; not ordinary process-death/P0 proof. | PASS |
| L12 | Rapid background/foreground/reattach | One player/session; serialized commands; no stale UI. | PASS |

Required physical coverage including Pixel/stock, Samsung/One UI, and Xiaomi/MIUI-HyperOS is accepted as PASS by confirmed execution. Automated AOSP API 29/31/34 validation is PASS.

## Decision

**PASS.** TS-02 lifecycle execution, required physical coverage, immutable attestation bundle, artifact Git object IDs, and Android/QA approval attestations are accepted for Cluster B. Unavailable historical raw captures or exact device identifiers are explicitly waived rather than fabricated.
