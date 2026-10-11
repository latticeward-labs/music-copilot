# HF Frame Review — WF-09_LIKED_SONGS_EMPTY

**Issue/phase:** #17 / 5 · **Source:** WF-09_LIKED_SONGS_EMPTY · **Status:** Reviewable generated evidence (not separate human sign-off)
**Image:** HF_FRAME_WF-09_LIKED_SONGS_EMPTY.png · **SHA-256:** f6ac819de3c4e1471ee71235829d23f151e6a3c8f683c44783dd7002711c0e93
**Priority:** P2 / Removable · **Ownership:** App
**Canonical State IDs:** UL-02, UL-03 · **UF:** UF-13 · **GS:** — (P2; not required by GS-01..05)
**Approved source:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-09_LIKED_SONGS_EMPTY.md and https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-09_LIKED_SONGS_EMPTY.svg
**Structural deviations:** None: exact source SVG hierarchy retained; approved Night Signal tokens applied.

## Exact rendered app copy

- "Back"
- "Liked Songs"
- "P2 / REMOVABLE"
- "♡  No liked songs"
- "No recommendations are invented."
- "Back to Library"
- "Home"
- "Search"
- "Liked"
- "Settings"

## Behavioral boundary

- **Entry:** P2 entry load completes with zero accessible liked tracks.
- **Primary action:** Back to Library; no invented recommendations.
- **Exit:** Back; future acknowledged Like can produce Ready.
- **Back:** Returns to initiating surface.
- **Truth:** Local user-state repository projected through accessible MediaStore items.
- **Recovery:** Load/reconcile failure exposes Retry/Back; missing media is reconciled without file mutation.
- **A11y / TalkBack:** P2/Removable label → heading → Empty explanation → Back.
- **System handoff:** No P0/P1 dependency; feature can be removed without placeholder.
- **Minimum target:** ≥48dp; Driving previous/pause/next/seek-back ≥64dp, Mic ≥72dp, Exit ≥56dp.
- **At 200% font scaling:** Compose implementation must reflow; static screenshot does not prove runtime layout.
- **Privacy:** no raw audio, transcript, search query or local music metadata in analytics; sample names are synthetic.
- **Ownership:** service-owned playback; Voice coordinator-owned Voice session; platform surfaces excluded from generated app screenshots.
- **P2:** Liked Songs remains removable; no P0 build dependency.

## Evidence limitation

Deterministic restyling of the *approved* low-fidelity SVG into Night Signal produces this reviewable static visual. It is not an independently signed design approval, a Compose implementation, nor proof of precise physical tap-target or 200% reflow behavior.
