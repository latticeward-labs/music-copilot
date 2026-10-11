# HF Frame Review — WF-07_DRIVING_MODE_PLAYING

**Issue/phase:** #17 / 5 · **Source:** WF-07_DRIVING_MODE_PLAYING · **Status:** Reviewable generated evidence (not separate human sign-off)
**Image:** HF_FRAME_WF-07_DRIVING_MODE_PLAYING.png · **SHA-256:** e495c7b4fb103755ebff64426a41400ab318ad189f24499c17aa421686b8f24c
**Priority:** P0 · **Ownership:** App + Playback Service
**Canonical State IDs:** CC-05, CC-14, CC-16 · **UF:** UF-07 / UF-11 · **GS:** GS-04 / GS-05
**Approved source:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-07_DRIVING_MODE_PLAYING.md and https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-07_DRIVING_MODE_PLAYING.svg
**Structural deviations:** None: exact source SVG hierarchy retained; approved Night Signal tokens applied.

## Exact rendered app copy

- "Exit"
- "DRIVING MODE"
- "ARTWORK"
- "Aurora Lights"
- "Local artist"
- "PLAYING"
- "Mic · Voice"

## Behavioral boundary

- **Entry:** Explicit Driving Mode entry with a complete Playing snapshot.
- **Primary action:** Pause; four large controls, Mic and Exit remain fixed.
- **Exit:** Exit is immediate; Mic opens an overlay, not a new destination.
- **Back:** Back behaves as Exit and returns to the prior non-driving surface.
- **Truth:** Complete MediaSessionService snapshot and supported actions.
- **Recovery:** Pending/rejected commands refresh actual state; route events hand off to UF-11.
- **A11y / TalkBack:** Playing → title/artist → Previous → Pause → Next → Seek back → Mic → Exit.
- **System handoff:** Calls/focus/routes are system events reflected through service truth.
- **Minimum target:** ≥48dp; Driving previous/pause/next/seek-back ≥64dp, Mic ≥72dp, Exit ≥56dp.
- **At 200% font scaling:** Compose implementation must reflow; static screenshot does not prove runtime layout.
- **Privacy:** no raw audio, transcript, search query or local music metadata in analytics; sample names are synthetic.
- **Ownership:** service-owned playback; Voice coordinator-owned Voice session; platform surfaces excluded from generated app screenshots.
- **P2:** Liked Songs remains removable; no P0 build dependency.

## Evidence limitation

Deterministic restyling of the *approved* low-fidelity SVG into Night Signal produces this reviewable static visual. It is not an independently signed design approval, a Compose implementation, nor proof of precise physical tap-target or 200% reflow behavior.
