# HF Frame Review — WF-07_DRIVING_MODE_NO_TRACK

**Issue/phase:** #17 / 5 · **Source:** WF-07_DRIVING_MODE_NO_TRACK · **Status:** Reviewable generated evidence (not separate human sign-off)
**Image:** HF_FRAME_WF-07_DRIVING_MODE_NO_TRACK.png · **SHA-256:** b8069829816d98dfcb4d9eab6a67f0b2223eff3b198514baa76816d354bd91e7
**Priority:** P0 · **Ownership:** App + Playback Service
**Canonical State IDs:** CC-07, CC-15 · **UF:** UF-07 / UF-11 · **GS:** GS-04 / GS-05
**Approved source:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-07_DRIVING_MODE_NO_TRACK.md and https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-07_DRIVING_MODE_NO_TRACK.svg
**Structural deviations:** None: exact source SVG hierarchy retained; approved Night Signal tokens applied.

## Exact rendered app copy

- "Exit"
- "DRIVING MODE"
- "NO TRACK"
- "No current track"
- "Exit to browse local music"
- "Mic · Voice"

## Behavioral boundary

- **Entry:** Complete snapshot has no current playable item.
- **Primary action:** Exit; Play last is shown only when a valid local reference exists.
- **Exit:** Valid Play last → Playing/Paused; Exit → Home/Library.
- **Back:** Back equals Exit.
- **Truth:** Service snapshot plus at most one revalidated MediaStore item.
- **Recovery:** Failure remains No Track and routes to Library after Exit; no list/typing.
- **A11y / TalkBack:** No track heading → explanation → optional Play last → Mic → Exit.
- **System handoff:** No Android Auto, picker or system list is introduced.
- **Minimum target:** ≥48dp; Driving previous/pause/next/seek-back ≥64dp, Mic ≥72dp, Exit ≥56dp.
- **At 200% font scaling:** Compose implementation must reflow; static screenshot does not prove runtime layout.
- **Privacy:** no raw audio, transcript, search query or local music metadata in analytics; sample names are synthetic.
- **Ownership:** service-owned playback; Voice coordinator-owned Voice session; platform surfaces excluded from generated app screenshots.
- **P2:** Liked Songs remains removable; no P0 build dependency.

## Evidence limitation

Deterministic restyling of the *approved* low-fidelity SVG into Night Signal produces this reviewable static visual. It is not an independently signed design approval, a Compose implementation, nor proof of precise physical tap-target or 200% reflow behavior.
