# HF Frame Review — WF-09_LIKED_SONGS_READY

**Issue/phase:** #17 / 5 · **Source:** WF-09_LIKED_SONGS_READY · **Status:** Reviewable generated evidence (not separate human sign-off)
**Image:** HF_FRAME_WF-09_LIKED_SONGS_READY.png · **SHA-256:** dd71e0166ee681c78f55aed5570cb25a7e4061e7689ed9174aa243276ab2d503
**Priority:** P2 / Removable · **Ownership:** App
**Canonical State IDs:** UL-04, UL-09, UL-10, UL-12 · **UF:** UF-13 · **GS:** — (P2; not required by GS-01..05)
**Approved source:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-09_LIKED_SONGS_READY.md and https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-09_LIKED_SONGS_READY.svg
**Structural deviations:** None: exact source SVG hierarchy retained; approved Night Signal tokens applied.

## Exact rendered app copy

- "Back"
- "Liked Songs"
- "P2 / REMOVABLE"
- "Play list"
- "Aurora Lights"
- "Liked · local track"
- "Unlike"
- "Night Drive"
- "Ocean Lines"
- "Home"
- "Search"
- "Liked"
- "Settings"

## Behavioral boundary

- **Entry:** P2 projection contains accessible liked tracks.
- **Primary action:** Play list or select a row; unlike preserves media files.
- **Exit:** Playback acknowledgement → Full Player; Back → Library.
- **Back:** Returns without stopping unrelated playback.
- **Truth:** Local Like repository plus accessible MediaStore projection; service owns playback.
- **Recovery:** Stale/rejected item refreshes projection; load failure preserves manual playback.
- **A11y / TalkBack:** P2/Removable label → Play list → rows → Like state/action → Back.
- **System handoff:** Feature remains optional/removable and never blocks P0/P1.
- **Minimum target:** ≥48dp; Driving previous/pause/next/seek-back ≥64dp, Mic ≥72dp, Exit ≥56dp.
- **At 200% font scaling:** Compose implementation must reflow; static screenshot does not prove runtime layout.
- **Privacy:** no raw audio, transcript, search query or local music metadata in analytics; sample names are synthetic.
- **Ownership:** service-owned playback; Voice coordinator-owned Voice session; platform surfaces excluded from generated app screenshots.
- **P2:** Liked Songs remains removable; no P0 build dependency.

## Evidence limitation

Deterministic restyling of the *approved* low-fidelity SVG into Night Signal produces this reviewable static visual. It is not an independently signed design approval, a Compose implementation, nor proof of precise physical tap-target or 200% reflow behavior.
