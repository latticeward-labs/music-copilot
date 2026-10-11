# HF Frame Review — WF-04_HOME_LIBRARY_RECOVERABLE_ERROR

**Issue/phase:** #17 / 5 · **Source:** WF-04_HOME_LIBRARY_RECOVERABLE_ERROR · **Status:** Reviewable generated evidence (not separate human sign-off)
**Image:** HF_FRAME_WF-04_HOME_LIBRARY_RECOVERABLE_ERROR.png · **SHA-256:** 759102091ea44bc8633c9e181bfa05ca8b6a549f8d3189630d70a0172250117a
**Priority:** P0 · **Ownership:** App
**Canonical State IDs:** S-04 · **UF:** UF-03 / UF-04 · **GS:** GS-01 / GS-02
**Approved source:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-04_HOME_LIBRARY_RECOVERABLE_ERROR.md and https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-04_HOME_LIBRARY_RECOVERABLE_ERROR.svg
**Structural deviations:** None: exact source SVG hierarchy retained; approved Night Signal tokens applied.

## Exact rendered app copy

- "Back"
- "Library"
- "More"
- "Songs"
- "Artists"
- "Albums"
- "Folders"
- "!  Library unavailable"
- "Verified prior state is retained."
- "Retry"
- "Permission recovery is separate."
- "Home"
- "Search"
- "Liked"
- "Settings"

## Behavioral boundary

- **Entry:** Typed recoverable index, storage or reconciliation failure.
- **Primary action:** Retry scan/reconciliation.
- **Exit:** Scanning then Empty/Ready; permission issue routes to WF-03.
- **Back:** Back keeps any verified prior library/playback state intact.
- **Truth:** Typed repository/index failure plus fresh MediaStore/access check.
- **Recovery:** Retry; no reinstall or destructive reset by default.
- **A11y / TalkBack:** Error heading → concise reason → Retry → Back/navigation.
- **System handoff:** Permission recovery is a separate handoff, not embedded Settings UI.
- **Minimum target:** ≥48dp; Driving previous/pause/next/seek-back ≥64dp, Mic ≥72dp, Exit ≥56dp.
- **At 200% font scaling:** Compose implementation must reflow; static screenshot does not prove runtime layout.
- **Privacy:** no raw audio, transcript, search query or local music metadata in analytics; sample names are synthetic.
- **Ownership:** service-owned playback; Voice coordinator-owned Voice session; platform surfaces excluded from generated app screenshots.
- **P2:** Liked Songs remains removable; no P0 build dependency.

## Evidence limitation

Deterministic restyling of the *approved* low-fidelity SVG into Night Signal produces this reviewable static visual. It is not an independently signed design approval, a Compose implementation, nor proof of precise physical tap-target or 200% reflow behavior.
