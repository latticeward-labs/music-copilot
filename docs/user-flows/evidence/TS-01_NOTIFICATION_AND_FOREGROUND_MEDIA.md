# TS-01 — Notification and Foreground-Media Evidence Protocol

**Status:** **PARTIAL — automated AOSP evidence PASS; Samsung physical N01-N10 PASS; Pixel/stock and Xiaomi/MIUI-HyperOS physical TS-01 rows APPROVED N/A / WAIVED by project-owner decision; Android/QA sign-off remains OPEN**  
**Owner:** Android + QA  
**Blocks:** Final UF-02/UF-06 and Issue #16  
**Result root:** `docs/user-flows/evidence/results/TS-01/`

> This protocol is not executed evidence by itself. PASS requires an installable Music Copilot build, required non-waived device/API coverage, build/repository SHAs, a timestamped state trace, expected-versus-actual results, immutable GitHub artifacts, and reviewer sign-off. Personal media data and unrelated logs are prohibited.

## Matrix and approved waiver

Run API 29, 31, and 34+ through the existing automated AOSP matrix. For physical TS-01 OEM execution, Samsung/One UI remains the required physical row and has been executed for N01-N10. Pixel/stock and Xiaomi/MIUI or HyperOS physical TS-01 rows are **Approved N/A / Waived** by explicit project-owner decision dated 18 August 2026.

The waiver means those two physical OEM rows are no longer blockers for TS-01. It does **not** claim that Pixel or Xiaomi were physically tested, does not fabricate device evidence, and does not automatically waive any requirement in TS-02, TS-06, or TS-08. Android/QA sign-off and the immutable TS-01 result bundle remain required before TS-01 can be marked fully executed PASS.

| ID | Scenario | Required result |
|---|---|---|
| N01 | First playback | One service-owned player/session; notification matches actual state. |
| N02 | Home/background while Playing | Playback/position continue; foreground visibility is platform-correct. |
| N03 | Screen off/lock | Audio continues; lock/system state matches MediaSession. |
| N04 | Notification commands | Exactly one acknowledged Play/Pause/Next/Previous; all surfaces converge. |
| N05 | Runtime notification permission where applicable | Contextual prompt after value exists; grant creates no duplicate service. |
| N06 | Deny/Not now | No crash/coercion/false Stopped/lost app controls; exemption recorded exactly. |
| N07 | Permanent denial/Settings return | Explicit Settings only; return rechecks truth; platform/OEM notification-setting behavior is recorded truthfully and must not create duplicate authority or an app-specific crash/ANR. |
| N08 | Task removal while Playing | Matches TS-02; never creates another player. |
| N09 | Controller reconnect | Complete snapshot; no false empty queue/Paused state. |
| N10 | Pause/complete with UI absent | Correct service/notification transition; no unexpected restart. |

Capture build/package/SDK/Media3 versions, device/OEM/API, permission before/after, service/session identity, state/position, surfaces, acknowledgements, redacted logcat and recording. PASS requires all non-waived mandatory rows, truthful permission behavior, one MediaSession, distinct P0/P1, and zero crash, ANR, duplicate authority, stale acknowledgement, or sensitive payload.
