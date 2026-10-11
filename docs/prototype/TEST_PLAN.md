# Phase 8 · Prototype acceptance tests and device handoff

**Status:** The browser interaction smoke and route/asset audits are prototype-level evidence only. This is not production feature or physical Android evidence.

## Automated checks

Run `node docs/prototype/validate.cjs` to verify the complete GS path graph and recoveries, State/WF/system boundary ownership, image dimensions/signatures, hotspot bounds, no third Retry, no invalid Driving link to settings/search/queue, and route reconnection not auto-starting.

Run `node docs/prototype/browser-smoke.cjs` while serving the repository root to exercise real Chromium clicks through:

1. GS-01 OS-owned permission grant → validated library Ready.
2. GS-02 Search tab → scripted local results → service-acknowledged Player.
3. GS-03 app background → native boundary → system key → controller reconnect.
4. GS-04 Player Driving entry → Mic → Processing → command ack → Executed → Driving.
5. GS-04 first STT timeout → exactly one Retry → second attempt terminal → Manual controls.
6. GS-04 two-candidate confirmation and post-ack TTS failure without rollback.
7. GS-05 external route disconnect → Safe Paused → reconnect boundary → explicit Play → acknowledged Player.
8. Narrow/mobile viewport hotspot visibility and a synthetic-only screenshot.
9. GS-01 Denied → Not now → Permanently denied → Android Settings return, Empty Library and actual Rescan hotspot.
10. GS-02 Stale Selection → saved results → Corrupt Item → safe Paused; Search No Result → Clear.
11. GS-03 explicit CB-22/23/24 sequence, CB-27 lock-screen native boundary, and CB-25 lifecycle loss.
12. GS-03 P1 Paused Restore → manual Play hotspot → pending service ACK or rejection → service-confirmed Playing.
13. GS-04 Voice Artist Not Found → Manual Only, no Retry.

**Latest captured Chromium evidence: 14/14 PASS**, compared with the original 8/8 smoke. The source-graph validator now separately proves all **38 recovery terminal paths inside each GS**. See [strict audit](PHASE_8_STRICT_REAUDIT_2026-10-11.md).

## Manual reviewer checklist — NOT RUN until actual reviewer evidence

| ID | Check | Expected | Status |
|---|---|---|---|
| PR-01 | Inspect each visible hotspot against paired HF/WF image and copy | All app controls match approved visual bounds and action affordances | NOT RUN |
| PR-02 | Test keyboard Tab/Shift+Tab, Enter and Space | Review shell controls keyboard operable, visible focus | NOT RUN |
| PR-03 | Inspect narrow viewport and 200% browser zoom | No lost review-only action or unreadable instruction | NOT RUN |
| PR-04 | Check source-specific Back/Cancel/Exit against all five GS | One-level Driving and Voice Cancel; no invented navigation | NOT RUN |
| PR-05 | Inspect Android boundary card against SYS/CB/CE OS register | No new Android Settings/dialog/lockscreen/media-key app screens | NOT RUN |
| PR-06 | Audit screenshot and DOM for personal data | Synthetic fixtures only; no raw mic/transcript/paths/IDs/location | NOT RUN |
| PR-07 | Compare standalone HF-06 driving entry vs screenshot and hotspot | Existing Driving action is visible and independent of Next | NOT RUN |
| PR-08 | Compare CD-13/14/15 failure with Phase-6 corrected art | No ineligible Retry, only manual/Exit | NOT RUN |
| PR-09 | Verify CD-19 terminal | No third Retry, a new session requires a separate future mic tap | NOT RUN |
| PR-10 | Verify CE-09 route handoff | System-only, Safe Paused on return, manual Play only | NOT RUN |

## Real Android / implementation handoff — not executed in this prototype

- Android 10/12/14+ on Pixel, Samsung, Xiaomi and three vehicle/head-unit combinations.
- MediaSessionService playback survives background/screen-off where allowed; one session/queue, actual controller reconnect.
- Permission/Settings/native notification/lockscreen/system controls remain platform owned and accessible to TalkBack.
- Real Play/Pause/Next/Seek and Safe Paused behavior under focus/call/route events; no output leaking to speaker on Bluetooth/wired route disconnect.
- STT/TTS provider fail-closed, six intents, max one 6s Retry and one immutable confirmation, optional TTS one sentence, no rollback after ack.
- Compose semantic traversal, fontScale 100/150/200% and minimum 48dp, Driving 64/72/56dp targets and focus restore (PH10-MIN-001 OPEN).
- Privacy/telemetry allowlist enforced at runtime; cloud STT off until reviewed consent/provider requirements (PH10-MIN-002).

**Separation:** A successful Chromium static prototype demonstrates click routing, not those actual Android requirements. Human review must record identity/date/finding severity and screenshot/evidence links before final Issue #17 approval.
