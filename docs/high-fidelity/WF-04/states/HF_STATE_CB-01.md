# High-Fidelity State — CB-01 · Home / Library Ready

**Issue:** #17 · **Phase:** 5 · **Disposition:** DEDICATED_HIFI / REVIEWABLE
**PNG:** HF_STATE_CB-01.png · **PNG SHA-256:** 3102ab0ffcfe14604c9dee66d70bb4bf60e66f1f594dd086099d74297bb348bc
**Priority:** P0 · **WF:** WF-04
**UF:** UF-04 · **GS:** GS-01, GS-02
**State:** CB-01 · **Owner:** App
**Screen owner:** UF-04 · Home/Library · **Recovery owner:** UF-03 / UF-04
**Source selected frame:** WF-04_HOME_LIBRARY_READY
**Source Markdown/SVG:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-04_HOME_LIBRARY_READY.md / https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-04_HOME_LIBRARY_READY.svg

## Exact UI copy (device surface only)

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

## State and structure contract

- **Source approved hierarchy:** WF-04_HOME_LIBRARY_READY; existing WF family only; no new app destination.
- **Visual-state specialization:** Stale item → reconcile; revoked access → permissions; controller pending stays truthful.
- **Component mapping:** CMP-03, CMP-04, CMP-09, CMP-11. All component definitions are in docs/design-system/components/COMPONENT_TOKENS.json.
- **Structural delta:** None. Source-aligned copy specialization only (where explicitly noted), not new controls or navigation.
- **Entry:** Verified non-empty index or returning route with valid current state.
- **Primary action:** Open a category, Search, or select a track.
- **Exit:** Browse/Search/Full Player/Settings; mini-player opens Full Player.
- **Back:** Standard top-level navigation; Back does not stop service-owned playback.
- **Recovery:** Stale item → reconcile; revoked access → permissions; controller pending stays truthful.
- **Actual state owner / truth:** Derived index supplies library; MediaSession supplies mini-player.; App.
- **Preserve/restoration:** Playback survives UI recreation; library index is rebuildable.
- **System-only constraints:** No system surface is represented as app navigation.

## Related source frames

- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-04_HOME_LIBRARY_SCANNING.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-04_HOME_LIBRARY_EMPTY.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-04_HOME_LIBRARY_READY.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-04_HOME_LIBRARY_RECOVERABLE_ERROR.md

## Accessibility / privacy / error containment

- **Reading order:** Library heading → categories → tracks → mini-player → bottom navigation.
- **Semantics:** written state and a non-color cue; observable 48dp minimum, or larger Driving overrides.
- **200% scaling:** reflow required in Compose implementation, not claimed by this static artifact.
- **Voice:** no auto-listening; 6-second bounded capture, max one retry, at most two candidates, one short error/TTS sentence.
- **Driving:** one fixed layer; immediate Exit, no typing/long lists/nested navigation/automatic entry.
- **Analytics:** allowed aggregate success/latency classifications only; prohibit raw audio, full transcript, personal media titles/artists, precise position, queries and candidate labels.
- **P2 isolation:** if P2/Like removed, P0 and P1 behavior remains unaffected.

## Review status

Generated from approved source SVG using deterministic token substitutions; PNG has been decoded and checksum recorded. Design review is **AI-assisted candidate review only**. No human signatures are inferred or fabricated. State-specific behaviors share the approved canonical owning WF; they are not unapproved independent routes.
