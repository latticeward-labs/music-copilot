# HF Frame Review — WF-06_FULL_PLAYER_PAUSED

**Issue/phase:** #17 / 5 · **Source:** WF-06_FULL_PLAYER_PAUSED · **Status:** Reviewable generated evidence (not separate human sign-off)
**Image:** HF_FRAME_WF-06_FULL_PLAYER_PAUSED.png · **SHA-256:** a1e7e081097b5f44dc7ca82bd077e3882deee4d8b70cc64da57e3ef57b834dd5
**Priority:** P0 · **Ownership:** App + Playback Service
**Canonical State IDs:** CB-16 · **UF:** UF-05 / UF-06 / UF-11 · **GS:** GS-02 / GS-03 / GS-05
**Approved source:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-06_FULL_PLAYER_PAUSED.md and https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-06_FULL_PLAYER_PAUSED.svg
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
- "PAUSED"

## Behavioral boundary

- **Entry:** Service reports Paused after command, interruption or entry.
- **Primary action:** Play.
- **Exit:** Acknowledged Play → Playing; Back preserves queue/position.
- **Back:** Returns without auto-playing.
- **Truth:** MediaSessionService snapshot.
- **Recovery:** Controller failure refreshes; interruption route rules may require safe manual resume.
- **A11y / TalkBack:** Paused state → title/artist → position → Previous → Play → Next → Queue.
- **System handoff:** No optimistic resume after route/focus changes.
- **Minimum target:** ≥48dp; Driving previous/pause/next/seek-back ≥64dp, Mic ≥72dp, Exit ≥56dp.
- **At 200% font scaling:** Compose implementation must reflow; static screenshot does not prove runtime layout.
- **Privacy:** no raw audio, transcript, search query or local music metadata in analytics; sample names are synthetic.
- **Ownership:** service-owned playback; Voice coordinator-owned Voice session; platform surfaces excluded from generated app screenshots.
- **P2:** Liked Songs remains removable; no P0 build dependency.

## Evidence limitation

Deterministic restyling of the *approved* low-fidelity SVG into Night Signal produces this reviewable static visual. It is not an independently signed design approval, a Compose implementation, nor proof of precise physical tap-target or 200% reflow behavior.
