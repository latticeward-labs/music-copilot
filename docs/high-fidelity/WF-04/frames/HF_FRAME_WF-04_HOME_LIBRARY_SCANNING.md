# HF Frame Review — WF-04_HOME_LIBRARY_SCANNING

**Issue/phase:** #17 / 5 · **Source:** WF-04_HOME_LIBRARY_SCANNING · **Status:** Reviewable generated evidence (not separate human sign-off)
**Image:** HF_FRAME_WF-04_HOME_LIBRARY_SCANNING.png · **SHA-256:** 70c712b7db32a8d67b5ee8a2892a30b80f162de6e132d793b9407d939cd23c57
**Priority:** P0 · **Ownership:** App
**Canonical State IDs:** S-04 · **UF:** UF-03 / UF-04 · **GS:** GS-01 / GS-02
**Approved source:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-04_HOME_LIBRARY_SCANNING.md and https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-04_HOME_LIBRARY_SCANNING.svg
**Structural deviations:** None: exact source SVG hierarchy retained; approved Night Signal tokens applied.

## Exact rendered app copy

- "Back"
- "Library"
- "More"
- "Songs"
- "Artists"
- "Albums"
- "Folders"
- "Scanning local music"
- "Reconciliation continues across UI recreation."
- "Home"
- "Search"
- "Liked"
- "Settings"

## Behavioral boundary

- **Entry:** First scan, rescan, reconciliation, or verified rebuild.
- **Primary action:** Wait; Back/navigation remains available when safe.
- **Exit:** Empty, Ready, or Recoverable Error.
- **Back:** Back leaves the surface without claiming scan cancellation unless supported.
- **Truth:** MediaStore existence plus rebuildable local index progress.
- **Recovery:** Typed failure routes to Recoverable Error; prior verified state is preserved when possible.
- **A11y / TalkBack:** Library heading → Scanning status/progress → available navigation.
- **System handoff:** Media permission loss hands off to WF-03; no fake Empty state.
- **Minimum target:** ≥48dp; Driving previous/pause/next/seek-back ≥64dp, Mic ≥72dp, Exit ≥56dp.
- **At 200% font scaling:** Compose implementation must reflow; static screenshot does not prove runtime layout.
- **Privacy:** no raw audio, transcript, search query or local music metadata in analytics; sample names are synthetic.
- **Ownership:** service-owned playback; Voice coordinator-owned Voice session; platform surfaces excluded from generated app screenshots.
- **P2:** Liked Songs remains removable; no P0 build dependency.

## Evidence limitation

Deterministic restyling of the *approved* low-fidelity SVG into Night Signal produces this reviewable static visual. It is not an independently signed design approval, a Compose implementation, nor proof of precise physical tap-target or 200% reflow behavior.
