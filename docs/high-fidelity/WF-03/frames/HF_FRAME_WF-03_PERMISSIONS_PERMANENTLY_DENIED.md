# HF Frame Review — WF-03_PERMISSIONS_PERMANENTLY_DENIED

**Issue/phase:** #17 / 5 · **Source:** WF-03_PERMISSIONS_PERMANENTLY_DENIED · **Status:** Reviewable generated evidence (not separate human sign-off)
**Image:** HF_FRAME_WF-03_PERMISSIONS_PERMANENTLY_DENIED.png · **SHA-256:** e1cdb9550399f08976a9f138763838290241b287713bae0480ade64fcd862f34
**Priority:** P0 · **Ownership:** Hybrid
**Canonical State IDs:** S-03, CA-02, SYS-04 · **UF:** UF-02 / UF-10 / UF-12 · **GS:** GS-01
**Approved source:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-03_PERMISSIONS_PERMANENTLY_DENIED.md and https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-03_PERMISSIONS_PERMANENTLY_DENIED.svg
**Structural deviations:** None: exact source SVG hierarchy retained; approved Night Signal tokens applied.

## Exact rendered app copy

- "Back"
- "Permissions"
- "PERMISSION"
- "↗"
- "Open Android Settings"
- "Permission must be changed explicitly."
- "Open Settings"
- "Not now"
- "[ANDROID / SYSTEM] handoff"
- "Fresh result is authoritative"

## Behavioral boundary

- **Entry:** Android reports permanent denial or Settings-only recovery.
- **Primary action:** Open Settings; Not now preserves manual use.
- **Exit:** Open Settings → Android-owned surface; return → three-way recheck.
- **Back:** Back/Not now returns to initiating manual surface.
- **Truth:** Android permission result after every return.
- **Recovery:** Granted, requestable-denied, or still-permanent are separate return outcomes.
- **A11y / TalkBack:** Permanently denied heading → consequence → Open Settings → Not now → system handoff.
- **System handoff:** [ANDROID / SYSTEM] Settings is a handoff, never an app destination.
- **Minimum target:** ≥48dp; Driving previous/pause/next/seek-back ≥64dp, Mic ≥72dp, Exit ≥56dp.
- **At 200% font scaling:** Compose implementation must reflow; static screenshot does not prove runtime layout.
- **Privacy:** no raw audio, transcript, search query or local music metadata in analytics; sample names are synthetic.
- **Ownership:** service-owned playback; Voice coordinator-owned Voice session; platform surfaces excluded from generated app screenshots.
- **P2:** Liked Songs remains removable; no P0 build dependency.

## Evidence limitation

Deterministic restyling of the *approved* low-fidelity SVG into Night Signal produces this reviewable static visual. It is not an independently signed design approval, a Compose implementation, nor proof of precise physical tap-target or 200% reflow behavior.
