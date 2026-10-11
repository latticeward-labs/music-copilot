# HF Frame Review — WF-03_PERMISSIONS_DENIED

**Issue/phase:** #17 / 5 · **Source:** WF-03_PERMISSIONS_DENIED · **Status:** Reviewable generated evidence (not separate human sign-off)
**Image:** HF_FRAME_WF-03_PERMISSIONS_DENIED.png · **SHA-256:** 27b38b282a317d9b90ab7e1acc0496a410d2a26206f8f121733cfbfb6a6b9956
**Priority:** P0 · **Ownership:** Hybrid
**Canonical State IDs:** S-03, CA-02 · **UF:** UF-02 / UF-10 / UF-12 · **GS:** GS-01
**Approved source:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-03_PERMISSIONS_DENIED.md and https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-03_PERMISSIONS_DENIED.svg
**Structural deviations:** None: exact source SVG hierarchy retained; approved Night Signal tokens applied.

## Exact rendered app copy

- "Back"
- "Permissions"
- "PERMISSION"
- "!"
- "Access denied"
- "Manual controls remain available."
- "Try again"
- "Not now"

## Behavioral boundary

- **Entry:** Android denies while another request remains permitted.
- **Primary action:** Try again; Not now remains equally reachable.
- **Exit:** Try again → rationale/request; Not now → initiating manual surface.
- **Back:** Same as Not now; no automatic Settings launch.
- **Truth:** Fresh Android permission result.
- **Recovery:** Retry or manual fallback; local browse/playback remains available when mic is denied.
- **A11y / TalkBack:** Denied heading → consequence → Try again → Not now.
- **System handoff:** No Android Settings handoff is required for requestable denial.
- **Minimum target:** ≥48dp; Driving previous/pause/next/seek-back ≥64dp, Mic ≥72dp, Exit ≥56dp.
- **At 200% font scaling:** Compose implementation must reflow; static screenshot does not prove runtime layout.
- **Privacy:** no raw audio, transcript, search query or local music metadata in analytics; sample names are synthetic.
- **Ownership:** service-owned playback; Voice coordinator-owned Voice session; platform surfaces excluded from generated app screenshots.
- **P2:** Liked Songs remains removable; no P0 build dependency.

## Evidence limitation

Deterministic restyling of the *approved* low-fidelity SVG into Night Signal produces this reviewable static visual. It is not an independently signed design approval, a Compose implementation, nor proof of precise physical tap-target or 200% reflow behavior.
