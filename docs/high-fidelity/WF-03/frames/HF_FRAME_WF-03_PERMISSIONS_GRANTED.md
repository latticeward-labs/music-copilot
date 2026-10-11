# HF Frame Review — WF-03_PERMISSIONS_GRANTED

**Issue/phase:** #17 / 5 · **Source:** WF-03_PERMISSIONS_GRANTED · **Status:** Reviewable generated evidence (not separate human sign-off)
**Image:** HF_FRAME_WF-03_PERMISSIONS_GRANTED.png · **SHA-256:** 417e1a30dbbb2bdbdbe0bc5133df09db0ed7c5d367fd8535eec7076f37abac16
**Priority:** P0 · **Ownership:** Hybrid
**Canonical State IDs:** S-03, CA-02 · **UF:** UF-02 / UF-10 / UF-12 · **GS:** GS-01
**Approved source:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-03_PERMISSIONS_GRANTED.md and https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-03_PERMISSIONS_GRANTED.svg
**Structural deviations:** None: exact source SVG hierarchy retained; approved Night Signal tokens applied.

## Exact rendered app copy

- "Back"
- "Permissions"
- "PERMISSION"
- "✓"
- "Access granted"
- "Continue to the blocked action."
- "Continue"

## Behavioral boundary

- **Entry:** Android reports Granted after request or Settings return.
- **Primary action:** Continue automatically or explicitly to the blocked app action.
- **Exit:** Media grant → scan/readiness; mic grant → provider/capability check.
- **Back:** Back returns to the initiating surface.
- **Truth:** Fresh Android permission recheck.
- **Recovery:** If capability changes before use, re-enter the appropriate recovery state.
- **A11y / TalkBack:** Granted status → next action.
- **System handoff:** Provider availability/cloud consent remains distinct from microphone grant.
- **Minimum target:** ≥48dp; Driving previous/pause/next/seek-back ≥64dp, Mic ≥72dp, Exit ≥56dp.
- **At 200% font scaling:** Compose implementation must reflow; static screenshot does not prove runtime layout.
- **Privacy:** no raw audio, transcript, search query or local music metadata in analytics; sample names are synthetic.
- **Ownership:** service-owned playback; Voice coordinator-owned Voice session; platform surfaces excluded from generated app screenshots.
- **P2:** Liked Songs remains removable; no P0 build dependency.

## Evidence limitation

Deterministic restyling of the *approved* low-fidelity SVG into Night Signal produces this reviewable static visual. It is not an independently signed design approval, a Compose implementation, nor proof of precise physical tap-target or 200% reflow behavior.
