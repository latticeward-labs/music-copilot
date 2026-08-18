# TS-02 — UI, Service, Task, and Process Lifecycle Evidence Protocol

**Status:** **PARTIAL — automated AOSP evidence PASS; operator-confirmed physical lifecycle matrix PASS; immutable result bundles and Android/QA sign-off remain OPEN**  
**Owner:** Android + QA  
**Blocks:** Final UF-05/UF-06 and Issue #16  
**Result root:** `docs/user-flows/evidence/results/TS-02/`

> This protocol is not executed evidence by itself. PASS closure still requires build/repository SHAs, timestamped state traces, expected-versus-actual results, immutable GitHub artifacts, and reviewer sign-off. Personal media data and unrelated logs are prohibited.

## Physical execution update — 18 August 2026

The project operator reports that the required lifecycle scenarios were executed across the previously required physical-device coverage, including Pixel/stock, Samsung/One UI, and Xiaomi/MIUI or HyperOS, and that all tested lifecycle behaviors passed. This closes the **physical execution matrix** for TS-02 at the operator-confirmation level. It does **not** fabricate missing immutable artifacts or reviewer approvals: formal TS-02 closure remains OPEN until the controlled result bundles and Android/QA sign-off are committed.

P0 means live service continuity; P1 begins only after active lifecycle loss and always restores Paused with explicit Resume.

| ID | Transition | Required result | Physical execution |
|---|---|---|---|
| L01 | Recreation while Playing | Same service/session/queue; full reconnect; monotonic position. | PASS — operator confirmed |
| L02 | Recreation while Paused | Remains Paused; no auto-play. | PASS — operator confirmed |
| L03 | Home/background | Playback continues; controller changes do not change authority. | PASS — operator confirmed |
| L04 | Screen off/lock | Playback/system surfaces remain synchronized. | PASS — operator confirmed |
| L05 | Activity finish/UI close | Live playback continues; reopen observes, never reconstructs. | PASS — operator confirmed |
| L06 | User task removal | OEM result recorded honestly; no false universal promise. | PASS — operator confirmed |
| L07 | Controller reconnect | Queue/item/state/position atomically replace placeholders. | PASS — operator confirmed |
| L08 | Cached process kill without active service | Relaunch enters P1 validation; no automatic audio. | PASS — operator confirmed |
| L09 | Process loss after Paused snapshot | Restores Paused; explicit Resume alone plays. | PASS — operator confirmed |
| L10 | Process loss with invalid media | Drop invalid entries; empty routes to Library/UF-03. | PASS — operator confirmed |
| L11 | Force-stop negative control | Kept separate; not ordinary process-death/P0 proof. | PASS — operator confirmed |
| L12 | Rapid background/foreground/reattach | One player/session; serialized commands; no stale UI. | PASS — operator confirmed |

Run API 29/31/34+; repeat L03–L07 on Pixel/Samsung/Xiaomi. Trace Activity/service/session/controller callbacks, player identity, queue revision, redacted item token, state, position and command order. Physical execution is recorded as PASS. **Formal TS-02 protocol PASS remains gated only by immutable evidence bundles and Android/QA reviewer sign-off.**
