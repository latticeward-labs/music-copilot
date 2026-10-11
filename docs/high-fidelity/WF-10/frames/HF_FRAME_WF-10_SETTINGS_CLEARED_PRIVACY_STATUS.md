# HF Frame Review — WF-10_SETTINGS_CLEARED_PRIVACY_STATUS

**Issue/phase:** #17 / 5 · **Source:** WF-10_SETTINGS_CLEARED_PRIVACY_STATUS · **Status:** Reviewable generated evidence (not separate human sign-off)
**Image:** HF_FRAME_WF-10_SETTINGS_CLEARED_PRIVACY_STATUS.png · **SHA-256:** 80e6b5eb7d54a8895b043b7c512a785af6111c8b0c4e5667909e8e26ffbd5ac9
**Priority:** P0 · **Ownership:** App + platform/provider boundary
**Canonical State IDs:** CE-21, CE-25, CE-26 · **UF:** UF-12 · **GS:** — (not on GS-01..05 path)
**Approved source:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-10_SETTINGS_CLEARED_PRIVACY_STATUS.md and https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-10_SETTINGS_CLEARED_PRIVACY_STATUS.svg
**Structural deviations:** None: exact source SVG hierarchy retained; approved Night Signal tokens applied.

## Exact rendered app copy

- "Back"
- "Settings"
- "PRIVACY / CLEAR RESULT"
- "✓ Voice history · Cleared"
- "! Diagnostics · Partial"
- "✓ Local files · Preserved"
- "✓ Playback · Unaffected"
- "Retry failed"
- "Done"
- "Never claim unconfirmed remote deletion."
- "Home"
- "Search"
- "Liked"

## Behavioral boundary

- **Entry:** Clear plan settles or Privacy details is opened after result.
- **Primary action:** Done; Retry only a failed boundary.
- **Exit:** Done → Settings; Retry → failed boundary only.
- **Back:** Returns to Settings without repeating deletion.
- **Truth:** Per-boundary repository/provider result; unresolved work remains explicit.
- **Recovery:** Partial/failed results never claim success; protected media/playback remains unaffected.
- **A11y / TalkBack:** Privacy/result heading → each category status → protected data note → Retry failed → Done.
- **System handoff:** Remote/provider result is distinguished from local deletion.
- **Minimum target:** ≥48dp; Driving previous/pause/next/seek-back ≥64dp, Mic ≥72dp, Exit ≥56dp.
- **At 200% font scaling:** Compose implementation must reflow; static screenshot does not prove runtime layout.
- **Privacy:** no raw audio, transcript, search query or local music metadata in analytics; sample names are synthetic.
- **Ownership:** service-owned playback; Voice coordinator-owned Voice session; platform surfaces excluded from generated app screenshots.
- **P2:** Liked Songs remains removable; no P0 build dependency.

## Evidence limitation

Deterministic restyling of the *approved* low-fidelity SVG into Night Signal produces this reviewable static visual. It is not an independently signed design approval, a Compose implementation, nor proof of precise physical tap-target or 200% reflow behavior.
