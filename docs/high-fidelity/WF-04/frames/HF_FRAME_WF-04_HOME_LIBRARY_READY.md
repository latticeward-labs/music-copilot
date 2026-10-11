# HF Frame Review — WF-04_HOME_LIBRARY_READY

**Issue/phase:** #17 / 5 · **Source:** WF-04_HOME_LIBRARY_READY · **Status:** Reviewable generated evidence (not separate human sign-off)
**Image:** HF_FRAME_WF-04_HOME_LIBRARY_READY.png · **SHA-256:** 3102ab0ffcfe14604c9dee66d70bb4bf60e66f1f594dd086099d74297bb348bc
**Priority:** P0 · **Ownership:** App
**Canonical State IDs:** S-04, CB-01, CB-20 · **UF:** UF-03 / UF-04 · **GS:** GS-01 / GS-02
**Approved source:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-04_HOME_LIBRARY_READY.md and https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-04_HOME_LIBRARY_READY.svg
**Structural deviations:** None: exact source SVG hierarchy retained; approved Night Signal tokens applied.

## Exact rendered app copy

- "Back"
- "Library"
- "More"
- "Songs"
- "Artists"
- "Albums"
- "Folders"
- "Aurora"
- "Local artist · 03:42"
- "Play"
- "Night Drive"
- "Local artist · 04:10"
- "Ocean Lines"
- "Local artist · 02:58"
- "Current track · Playing"
- "Pause"
- "Home"
- "Search"
- "Liked"
- "Settings"

## Behavioral boundary

- **Entry:** Verified non-empty index or returning route with valid current state.
- **Primary action:** Open a category, Search, or select a track.
- **Exit:** Browse/Search/Full Player/Settings; mini-player opens Full Player.
- **Back:** Standard top-level navigation; Back does not stop service-owned playback.
- **Truth:** Derived index supplies library; MediaSession supplies mini-player.
- **Recovery:** Stale item → reconcile; revoked access → permissions; controller pending stays truthful.
- **A11y / TalkBack:** Library heading → categories → tracks → mini-player → bottom navigation.
- **System handoff:** No system surface is represented as app navigation.
- **Minimum target:** ≥48dp; Driving previous/pause/next/seek-back ≥64dp, Mic ≥72dp, Exit ≥56dp.
- **At 200% font scaling:** Compose implementation must reflow; static screenshot does not prove runtime layout.
- **Privacy:** no raw audio, transcript, search query or local music metadata in analytics; sample names are synthetic.
- **Ownership:** service-owned playback; Voice coordinator-owned Voice session; platform surfaces excluded from generated app screenshots.
- **P2:** Liked Songs remains removable; no P0 build dependency.

## Evidence limitation

Deterministic restyling of the *approved* low-fidelity SVG into Night Signal produces this reviewable static visual. It is not an independently signed design approval, a Compose implementation, nor proof of precise physical tap-target or 200% reflow behavior.
