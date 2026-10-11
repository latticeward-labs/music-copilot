# Phase 8 — Strict source + safe recovery validation

**Issue:** #17 · **Date:** 11 October 2026 · **Source archive SHA-256:** `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`

**Result:** PASS for the explicitly requested HTML clickable GS-01..GS-05 prototype, all mandatory clickable recovery paths with a safe terminal, and the unchanged Android/System ownership boundary.

| Gate | Actual result | Decision |
|---|---:|---|
| Five approved primary paths end-to-end | 5/5 | PASS |
| Scenario-scoped recovery branches with explicit safe terminal | 38/38 | PASS |
| Real Chromium browser click cases | 14/14 | PASS |
| Source-locked prototype graph nodes (not canonical new states) | 67 | PASS |
| Approved-image app contexts | 54 | PASS |
| OS/MediaSession handoff annotations without fake Android screen | 13 | PASS |
| Graph edges | 187 | PASS |
| Existing-control clickable image hotspots | 75 | PASS |
| Referenced PNGs missing/invalid | 0 | PASS |
| Missing or duplicate routes/recovery targets | 0 | PASS |
| Routes JSON parity with JS source | exact | PASS |
| Physical Android/TalkBack/Media3/STT/vehicle QA | NOT RUN | downstream |

## Scenario-scoped safe endpoint proof

| GS | Branches | In-scope safe terminal paths |
|---|---:|---:|
| GS-01 | 6 | 6 |
| GS-02 | 5 | 5 |
| GS-03 | 7 | 7 |
| GS-04 | 16 | 16 |
| GS-05 | 4 | 4 |

Each branch is checked twice: it must be reachable from its GS start **without leaving the GS-approved node set**, and it must be able to reach one of that GS's explicitly declared safe/manual/paused/error endpoints without crossing into any other Golden Scenario. Earlier global-reachability tests were weaker and have been superseded.

The machine-readable evidence includes every branch ID and shortest safe-terminal path: [PHASE_8_VALIDATION.json](PHASE_8_VALIDATION.json). The route contract exists in [routes.json](routes.json) and is generated from [routes.js](routes.js) with [export-routes.cjs](export-routes.cjs).

## Actual browser execution

The updated [Chromium validation](PHASE_8_BROWSER_VALIDATION.json) reports **14 passed / 0 failed** and covers all five primary journeys plus Permission denied → Settings return, Empty Library Rescan, Search No Result, stale selection, corrupt media, native lock-screen/media-key boundary, P1 service ACK, Voice Not Found, bounded STT Retry and safe paused route recovery. [Synthetic screenshot](PHASE_8_BROWSER_REVIEW.png) was also captured.

## Boundaries

This is an HTML/JS image-surface walkthrough, **not** a fully functional Android music player. No live media, STT/TTS, Android Settings dialogs, system notifications, lockscreen or vehicle UI are fabricated or called. Audio and Voice correctness must be validated in the future Kotlin/Compose/Media3 implementation; 200% Compose accessibility PH10-MIN-001 and independent cross-role human reviews are not falsely represented as complete.

[Strict re-audit and corrections](PHASE_8_STRICT_REAUDIT_2026-10-11.md) · [QA handoff](TEST_PLAN.md).
