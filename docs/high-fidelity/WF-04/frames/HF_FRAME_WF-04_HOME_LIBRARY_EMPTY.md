# HF Frame Review — WF-04_HOME_LIBRARY_EMPTY

**Issue/phase:** #17 / 5 · **Source:** WF-04_HOME_LIBRARY_EMPTY · **Status:** Reviewable generated evidence (not separate human sign-off)
**Image:** HF_FRAME_WF-04_HOME_LIBRARY_EMPTY.png · **SHA-256:** b5cc131d19e8c2b703f51fcb514ec34bdf8b1b7f387ad5b0b589a95f4731dc9f
**Priority:** P0 · **Ownership:** App
**Canonical State IDs:** S-04 · **UF:** UF-03 / UF-04 · **GS:** GS-01 / GS-02
**Approved source:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-04_HOME_LIBRARY_EMPTY.md and https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-04_HOME_LIBRARY_EMPTY.svg
**Structural deviations:** None: exact source SVG hierarchy retained; approved Night Signal tokens applied.

## Exact rendered app copy

- "Back"
- "Library"
- "More"
- "Songs"
- "Artists"
- "Albums"
- "Folders"
- "□  No local tracks"
- "No recommendations or online fallback."
- "Rescan"
- "Home"
- "Search"
- "Liked"
- "Settings"

## Behavioral boundary

- **Entry:** Successful scan/reconciliation produces zero accessible tracks.
- **Primary action:** Rescan; permission recovery appears only when access is actually missing.
- **Exit:** Ready after content appears, or permission recovery when revoked.
- **Back:** Normal app exit/back behavior; state remains truthful on return.
- **Truth:** MediaStore projection reconciled through the local index.
- **Recovery:** Rescan; no online catalog, recommendations or fabricated results.
- **A11y / TalkBack:** Library heading → Empty state → explanation → Rescan.
- **System handoff:** Android Settings is offered only for verified permission recovery.
- **Minimum target:** ≥48dp; Driving previous/pause/next/seek-back ≥64dp, Mic ≥72dp, Exit ≥56dp.
- **At 200% font scaling:** Compose implementation must reflow; static screenshot does not prove runtime layout.
- **Privacy:** no raw audio, transcript, search query or local music metadata in analytics; sample names are synthetic.
- **Ownership:** service-owned playback; Voice coordinator-owned Voice session; platform surfaces excluded from generated app screenshots.
- **P2:** Liked Songs remains removable; no P0 build dependency.

## Evidence limitation

Deterministic restyling of the *approved* low-fidelity SVG into Night Signal produces this reviewable static visual. It is not an independently signed design approval, a Compose implementation, nor proof of precise physical tap-target or 200% reflow behavior.
