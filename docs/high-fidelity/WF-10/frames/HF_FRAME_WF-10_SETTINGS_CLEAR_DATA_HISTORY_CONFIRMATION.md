# HF Frame Review — WF-10_SETTINGS_CLEAR_DATA_HISTORY_CONFIRMATION

**Issue/phase:** #17 / 5 · **Source:** WF-10_SETTINGS_CLEAR_DATA_HISTORY_CONFIRMATION · **Status:** Reviewable generated evidence (not separate human sign-off)
**Image:** HF_FRAME_WF-10_SETTINGS_CLEAR_DATA_HISTORY_CONFIRMATION.png · **SHA-256:** d93d3c4bbfd400ac14197891173b310aa2e2713341972ec435028a7a006840c9
**Priority:** P0 · **Ownership:** App + platform/provider boundary
**Canonical State IDs:** CE-23, CE-24 · **UF:** UF-12 · **GS:** — (not on GS-01..05 path)
**Approved source:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-10_SETTINGS_CLEAR_DATA_HISTORY_CONFIRMATION.md and https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-10_SETTINGS_CLEAR_DATA_HISTORY_CONFIRMATION.svg
**Structural deviations:** None: exact source SVG hierarchy retained; approved Night Signal tokens applied.

## Exact rendered app copy

- "Back"
- "Settings"
- "Clear selected data?"
- "✓ Voice history"
- "✓ Diagnostics queue"
- "Preserved: local music files"
- "Preserved: active playback"
- "Confirm clear"
- "Cancel"
- "Home"
- "Search"
- "Liked"

## Behavioral boundary

- **Entry:** At least one non-preselected clear category is selected.
- **Primary action:** Confirm clear; Cancel is equally reachable.
- **Exit:** Confirm → Clearing/Result; Cancel → selection or Settings.
- **Back:** Back equals Cancel; no deletion occurs.
- **Truth:** Selected local categories and documented provider boundaries.
- **Recovery:** No destructive default; partial/provider failure is reported truthfully later.
- **A11y / TalkBack:** Warning heading → selected categories → preserved categories → Confirm clear → Cancel.
- **System handoff:** Never claims remote deletion unless the provider confirms it.
- **Minimum target:** ≥48dp; Driving previous/pause/next/seek-back ≥64dp, Mic ≥72dp, Exit ≥56dp.
- **At 200% font scaling:** Compose implementation must reflow; static screenshot does not prove runtime layout.
- **Privacy:** no raw audio, transcript, search query or local music metadata in analytics; sample names are synthetic.
- **Ownership:** service-owned playback; Voice coordinator-owned Voice session; platform surfaces excluded from generated app screenshots.
- **P2:** Liked Songs remains removable; no P0 build dependency.

## Evidence limitation

Deterministic restyling of the *approved* low-fidelity SVG into Night Signal produces this reviewable static visual. It is not an independently signed design approval, a Compose implementation, nor proof of precise physical tap-target or 200% reflow behavior.
