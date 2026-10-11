# HF Frame Review — WF-10_SETTINGS_DEFAULT

**Issue/phase:** #17 / 5 · **Source:** WF-10_SETTINGS_DEFAULT · **Status:** Reviewable generated evidence (not separate human sign-off)
**Image:** HF_FRAME_WF-10_SETTINGS_DEFAULT.png · **SHA-256:** fec2fdec595b47d0b5512cd588c92135ab736b7caa9ed1821717bd54800e892b
**Priority:** P0 · **Ownership:** App + platform/provider boundary
**Canonical State IDs:** CE-15, CE-21, CE-22 · **UF:** UF-12 · **GS:** — (not on GS-01..05 path)
**Approved source:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-10_SETTINGS_DEFAULT.md and https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-10_SETTINGS_DEFAULT.svg
**Structural deviations:** None: exact source SVG hierarchy retained; approved Night Signal tokens applied.

## Exact rendered app copy

- "Back"
- "Settings"
- "Permissions"
- "Current platform status"
- "Open"
- "Bluetooth suggestion"
- "P1 preference"
- "Privacy"
- "Local-first controls"
- "Clear data / history"
- "Selected categories only"
- "Home"
- "Search"
- "Liked"

## Behavioral boundary

- **Entry:** Settings opened from non-driving navigation.
- **Primary action:** Open one settings section.
- **Exit:** Section/update/handoff; Back returns to prior surface.
- **Back:** Returns without changing values or playback.
- **Truth:** Preferences plus fresh platform/provider truth.
- **Recovery:** Load/update error retains previous value and exposes Retry where safe.
- **A11y / TalkBack:** Settings heading → section headings → current values/actions → bottom navigation.
- **System handoff:** Android Settings handoff is explicit and never launched from active Driving Mode.
- **Minimum target:** ≥48dp; Driving previous/pause/next/seek-back ≥64dp, Mic ≥72dp, Exit ≥56dp.
- **At 200% font scaling:** Compose implementation must reflow; static screenshot does not prove runtime layout.
- **Privacy:** no raw audio, transcript, search query or local music metadata in analytics; sample names are synthetic.
- **Ownership:** service-owned playback; Voice coordinator-owned Voice session; platform surfaces excluded from generated app screenshots.
- **P2:** Liked Songs remains removable; no P0 build dependency.

## Evidence limitation

Deterministic restyling of the *approved* low-fidelity SVG into Night Signal produces this reviewable static visual. It is not an independently signed design approval, a Compose implementation, nor proof of precise physical tap-target or 200% reflow behavior.
