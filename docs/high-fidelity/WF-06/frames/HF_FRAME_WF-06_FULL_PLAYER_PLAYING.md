# HF Frame Review — WF-06_FULL_PLAYER_PLAYING

**Issue/phase:** #17 / 5 · **Source:** WF-06_FULL_PLAYER_PLAYING · **Status:** Reviewable generated evidence (not separate human sign-off)
**Image:** HF_FRAME_WF-06_FULL_PLAYER_PLAYING.png · **SHA-256:** 6a216c4bcaf37203b94e76e618f4db8b0561d0d979efa81eba7aa153f0c972ea
**Priority:** P0 · **Ownership:** App + Playback Service
**Canonical State IDs:** CB-15 · **UF:** UF-05 / UF-06 / UF-11 · **GS:** GS-02 / GS-03 / GS-05
**Approved source:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-06_FULL_PLAYER_PLAYING.md and https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-06_FULL_PLAYER_PLAYING.svg
**Structural deviations:** None: exact source SVG hierarchy retained; approved Night Signal tokens applied.

## Exact rendered app copy

- "Back"
- "Now playing"
- "Queue"
- "ARTWORK"
- "Aurora Lights"
- "Local artist"
- "01:26"
- "03:42"
- "PLAYING"

## Behavioral boundary

- **Entry:** MediaSessionService acknowledges playback with a valid item.
- **Primary action:** Pause; Next/Previous/Seek/Queue remain available as supported.
- **Exit:** Back keeps playback; Driving Mode requires explicit entry; Queue remains same authority.
- **Back:** Returns to initiating app surface without stopping playback.
- **Truth:** MediaSessionService snapshot: item, queue, state, position and supported actions.
- **Recovery:** Command/controller failure refreshes actual snapshot; corrupt item uses bounded recovery.
- **A11y / TalkBack:** Now playing → title/artist → position → Previous → Pause → Next → Queue.
- **System handoff:** Notification/lock/external controls are system-only synchronized surfaces.
- **Minimum target:** ≥48dp; Driving previous/pause/next/seek-back ≥64dp, Mic ≥72dp, Exit ≥56dp.
- **At 200% font scaling:** Compose implementation must reflow; static screenshot does not prove runtime layout.
- **Privacy:** no raw audio, transcript, search query or local music metadata in analytics; sample names are synthetic.
- **Ownership:** service-owned playback; Voice coordinator-owned Voice session; platform surfaces excluded from generated app screenshots.
- **P2:** Liked Songs remains removable; no P0 build dependency.

## Evidence limitation

Deterministic restyling of the *approved* low-fidelity SVG into Night Signal produces this reviewable static visual. It is not an independently signed design approval, a Compose implementation, nor proof of precise physical tap-target or 200% reflow behavior.
