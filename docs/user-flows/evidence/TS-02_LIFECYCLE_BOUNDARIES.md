# TS-02 — UI, Service, Task, and Process Lifecycle Evidence Protocol

**Status:** **PARTIAL — automated AOSP evidence PASS; mandatory physical-device rows and Android/QA sign-off remain BLOCKED**  
**Owner:** Android + QA  
**Blocks:** Final UF-05/UF-06 and Issue #16  
**Result root:** `docs/user-flows/evidence/results/TS-02/`

> This protocol is not executed evidence. PASS requires an installable Music Copilot build, required device/API/OEM coverage, build/repository SHAs, a timestamped state trace, expected-versus-actual results, immutable GitHub artifacts, and reviewer sign-off. Personal media data and unrelated logs are prohibited.

P0 means live service continuity; P1 begins only after active lifecycle loss and always restores Paused with explicit Resume.

| ID | Transition | Required result |
|---|---|---|
| L01 | Recreation while Playing | Same service/session/queue; full reconnect; monotonic position. |
| L02 | Recreation while Paused | Remains Paused; no auto-play. |
| L03 | Home/background | Playback continues; controller changes do not change authority. |
| L04 | Screen off/lock | Playback/system surfaces remain synchronized. |
| L05 | Activity finish/UI close | Live playback continues; reopen observes, never reconstructs. |
| L06 | User task removal | OEM result recorded honestly; no false universal promise. |
| L07 | Controller reconnect | Queue/item/state/position atomically replace placeholders. |
| L08 | Cached process kill without active service | Relaunch enters P1 validation; no automatic audio. |
| L09 | Process loss after Paused snapshot | Restores Paused; explicit Resume alone plays. |
| L10 | Process loss with invalid media | Drop invalid entries; empty routes to Library/UF-03. |
| L11 | Force-stop negative control | Kept separate; not ordinary process-death/P0 proof. |
| L12 | Rapid background/foreground/reattach | One player/session; serialized commands; no stale UI. |

Run API 29/31/34+; repeat L03–L07 on Pixel/Samsung/Xiaomi. Trace Activity/service/session/controller callbacks, player identity, queue revision, redacted item token, state, position and command order. PASS requires no UI-owned player, proven P0 continuity, P1-only process-loss recovery, explicit Resume, and zero duplicate playback, surprise audio, false queue, crash, ANR, or sensitive log.
