# Issue #17 · Phase 8 strict acceptance re-audit

**Date:** 11 October 2026  
**Repository:** `latticeward-labs/music-copilot`  
**Attached `MSM2(1)(1).rar` SHA-256:** `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`  
**Source authority:** accepted Golden Scenarios GS-01..GS-05, WF-01..WF-10, 126-State Phase-5 register, specialized Voice/Driving Phase-6, Phase-7 System-only/Privacy and accepted Software Architecture/ADRs.

## Findings and fixes

| Severity | Original gap | Implemented correction | Verification |
|---|---|---|---|
| Major | Empty Library Rescan image hotspot at y=430 was outside actual displayed Rescan button | Source WF-04-aligned Rescan rectangle at x=65 y=368, 231×54dp | Browser clicks screenshot Rescan → S-04 Scanning |
| Major | Old `validate.cjs` proved only a recovery's reachability from the GS start using the entire globally connected graph (many GS reached 58/61 unrelated nodes) | Explicit `recoverySafety.allowed` and `terminals` per GS and shortest in-scope safe-terminal path test for every recovery | 38/38 in-scope branches have a safe endpoint; cross-GS shortcut forbidden |
| Major | GS-03 skipped CB-22 active-session and CB-24 device lock/screen-off milestones, limiting actual platform boundary traceability | Added CB-22/24/25/27/28 as Android/System-only annotation nodes with no image and outside-app event outcomes | Full CB-22→23→24→26→29→21 primary; CB-27 lockscreen smoke |
| Major | P1 Paused Restore used a reviewer event to switch directly to Playing without service acknowledgement | CB-32 source image has real Play hotspot → separate same-state pending annotation → playback ACK → Playing; rejected/invalid returns Paused | Chromium test checks pending status, rejection and approved ACK |
| Minor | Visible media-permission `Not now` button lacked a hotspot | Added exact WF-03 denied control rectangle x=38, y=553, 284×54dp | Chromium taps on-image Not now safely without false Ready |
| Minor | `routes.json` requested as an example but absent | Generated exact [routes.json](routes.json) from the [runtime routes.js](routes.js); added [export-routes.cjs](export-routes.cjs) and deep-equality validator | JSON parity machine-checked at CI |
| Major | Browser smoke tested 8 cases but did not click all explicitly named recovery classes | Expanded Chromium to 14 concrete click scenarios: Settings/permission, Empty Library Rescan, No Result, Stale/Corrupt, GS-03 system/Paused Restore, Voice Not Found/Retry, GS-05 Safe Paused | 14/14 browser smoke successful, zero failures |

## Verified acceptance matrix

| Criterion | Result |
|---|---|
| GS-01 First Launch to Ready Library | PASS — Source Splash/Onboarding/contextual media permission, native Android boundary, scanning, Ready; bounded failure/denial/empty exits |
| GS-02 Find Music and Start Playback | PASS — local Search/selection, service ACK, preserved query/context, stale and corrupt fallbacks |
| GS-03 Continue Playback Outside the App | PASS — Android active session/background/screen off, native MediaSession notification/lock/vehicle media keys, actual reconnect, no fake OS screen |
| GS-04 Drive and Execute a Direct Voice Command | PASS — explicit Driving, Mic, six-intent processing, one confirmation, one eligible Retry, not found/unsupported/manual only, success and TTS degradation |
| GS-05 Recover Safely from an Interruption | PASS — OS focus/route event, Safe Paused, reconnection still Paused, explicit manual Play and service ACK |
| All declared recovery branch safe endpoints | PASS — 6/5/7/16/4 = 38/38 with GS-scoped bounded paths |
| Application hotspot tests | PASS — 75 geometrically bounded 48dp+ hotspots; critical corrections visually compared to screenshots |
| Canonical identity/no extra app screens | PASS — 67 prototype contexts reuse existing 126 State IDs; 54 app-image contexts, 13 true OS-only annotations, no invented native app screen |
| Chrome browser click QA | PASS — 14/14 cases, no page error; synthetic responsive browser screenshot |
| Device/Media3/real STT, Android platform UI, TalkBack at 200% | NOT RUN — outside this prototype phase |

**No source design hierarchy, Phase-5 canonical State, playback owner, Android Auto/backend/streaming or remote-STT consent scope was expanded.**

## Implementation and audit limitations

The `docs/prototype/` HTML is a **repository-native clickable design demonstration**. The panel of simulated OS/STT/Media3 results is expressly not part of Android UI. Neither the simulator nor successful Chromium tests prove actual media playback continuity, OS permission handling, audio-route safety or Compose screenreader/reflow. These require implementation and release QA, including the still open PH10-MIN-001.

The original PR #61 remains preserved as history. This corrective PR supersedes its weaker reachability-count and click-smoke claims with stricter reproducible evidence; it does not overwrite the approved Golden Scenario or Wireframe source files.

## Evidence and reproduction

- [Canonical route JSON](routes.json)
- [Executable route graph](routes.js)
- [Scenario-scoped integrity validator](validate.cjs)
- [Chromium end-to-end click script](browser-smoke.cjs)
- [Machine proof for all 38 safe endpoints](PHASE_8_VALIDATION.json)
- [Actual 14/14 browser result](PHASE_8_BROWSER_VALIDATION.json)
- [Synthetic-only browser screenshot](PHASE_8_BROWSER_REVIEW.png)
- [Consolidated validation table](PHASE_8_VALIDATION.md)
- [Developer and human QA handoff](TEST_PLAN.md)

**Decision: PASS for Phase 8 HTML clickable prototype and safe recovery criterion, subject to the ordinary repository PR CI and merge.**
