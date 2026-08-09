# TS-01 — Notification and Foreground-Media Evidence Protocol

**Status:** **PARTIAL — automated AOSP evidence PASS; mandatory physical-device rows and Android/QA sign-off remain BLOCKED**  
**Owner:** Android + QA  
**Blocks:** Final UF-02/UF-06 and Issue #16  
**Result root:** `docs/user-flows/evidence/results/TS-01/`

> This protocol is not executed evidence. PASS requires an installable Music Copilot build, required device/API/OEM coverage, build/repository SHAs, a timestamped state trace, expected-versus-actual results, immutable GitHub artifacts, and reviewer sign-off. Personal media data and unrelated logs are prohibited.

## Mandatory matrix

Run API 29, 31, and 34+; cover Pixel/stock, Samsung/One UI, and Xiaomi/MIUI or HyperOS. Missing required coverage is BLOCKED.

| ID | Scenario | Required result |
|---|---|---|
| N01 | First playback | One service-owned player/session; notification matches actual state. |
| N02 | Home/background while Playing | Playback/position continue; foreground visibility is platform-correct. |
| N03 | Screen off/lock | Audio continues; lock/system state matches MediaSession. |
| N04 | Notification commands | Exactly one acknowledged Play/Pause/Next/Previous; all surfaces converge. |
| N05 | Runtime notification permission where applicable | Contextual prompt after value exists; grant creates no duplicate service. |
| N06 | Deny/Not now | No crash/coercion/false Stopped/lost app controls; exemption recorded exactly. |
| N07 | Permanent denial/Settings return | Explicit Settings only; return rechecks truth and preserves playback. |
| N08 | Task removal while Playing | Matches TS-02; never creates another player. |
| N09 | Controller reconnect | Complete snapshot; no false empty queue/Paused state. |
| N10 | Pause/complete with UI absent | Correct service/notification transition; no unexpected restart. |

Capture build/package/SDK/Media3 versions, device/OEM/API, permission before/after, service/session identity, state/position, surfaces, acknowledgements, redacted logcat and recording. PASS requires all mandatory rows, truthful permission behavior, one MediaSession, distinct P0/P1, and zero crash, ANR, duplicate authority, stale acknowledgement, or sensitive payload.
