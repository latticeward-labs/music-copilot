# HF Frame Review — WF-07_DRIVING_MODE_PAUSED

**Issue/phase:** #17 / 5 · **Source:** WF-07_DRIVING_MODE_PAUSED · **Status:** Reviewable generated evidence (not separate human sign-off)
**Image:** HF_FRAME_WF-07_DRIVING_MODE_PAUSED.png · **SHA-256:** e9416c98432b43620ef349d1a56d7ca480e1da0f291d907df988a026b5def564
**Priority:** P0 · **Ownership:** App + Playback Service
**Canonical State IDs:** CC-06, CC-16 · **UF:** UF-07 / UF-11 · **GS:** GS-04 / GS-05
**Approved source:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-07_DRIVING_MODE_PAUSED.md and https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-07_DRIVING_MODE_PAUSED.svg
**Structural deviations:** None: exact source SVG hierarchy retained; approved Night Signal tokens applied.

## Exact rendered app copy

- "Exit"
- "DRIVING MODE"
- "ARTWORK"
- "Aurora Lights"
- "Local artist"
- "PAUSED"
- "Mic · Voice"

## Behavioral boundary

- **Entry:** Complete snapshot reports Paused/Ended.
- **Primary action:** Play; Exit remains prominent.
- **Exit:** Acknowledged Play → Playing; Exit → prior surface.
- **Back:** Back equals immediate Exit; never auto-resumes.
- **Truth:** MediaSessionService Paused snapshot.
- **Recovery:** Unsafe route remains safely Paused; refresh/reconnect never surprises with audio.
- **A11y / TalkBack:** Paused → title/artist → Previous → Play → Next → Seek back → Mic → Exit.
- **System handoff:** Route/focus safety may disable Play with a textual reason.
- **Minimum target:** ≥48dp; Driving previous/pause/next/seek-back ≥64dp, Mic ≥72dp, Exit ≥56dp.
- **At 200% font scaling:** Compose implementation must reflow; static screenshot does not prove runtime layout.
- **Privacy:** no raw audio, transcript, search query or local music metadata in analytics; sample names are synthetic.
- **Ownership:** service-owned playback; Voice coordinator-owned Voice session; platform surfaces excluded from generated app screenshots.
- **P2:** Liked Songs remains removable; no P0 build dependency.

## Evidence limitation

Deterministic restyling of the *approved* low-fidelity SVG into Night Signal produces this reviewable static visual. It is not an independently signed design approval, a Compose implementation, nor proof of precise physical tap-target or 200% reflow behavior.
