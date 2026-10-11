# HF Frame Review — WF-05_SEARCH_RESULTS

**Issue/phase:** #17 / 5 · **Source:** WF-05_SEARCH_RESULTS · **Status:** Reviewable generated evidence (not separate human sign-off)
**Image:** HF_FRAME_WF-05_SEARCH_RESULTS.png · **SHA-256:** aae27437eb262fcd9145334219506de4b25b74c9544d80e001f76e755876cde9
**Priority:** P0 · **Ownership:** App
**Canonical State IDs:** CB-09, CB-10, CB-13 · **UF:** UF-04 · **GS:** GS-02
**Approved source:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-05_SEARCH_RESULTS.md and https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-05_SEARCH_RESULTS.svg
**Structural deviations:** None: exact source SVG hierarchy retained; approved Night Signal tokens applied.

## Exact rendered app copy

- "Back"
- "Search"
- "aurora"
- "Clear"
- "3 local results"
- "Aurora"
- "Artist result"
- "Open"
- "Aurora Lights"
- "Track · 03:42"
- "Play"
- "Northern Sky"
- "Album result"
- "Home"
- "Liked"
- "Settings"

## Behavioral boundary

- **Entry:** Local query returns one or more indexed matches.
- **Primary action:** Select a result to request service-owned playback.
- **Exit:** Validated selection → Full Player; edit/clear remains in Search.
- **Back:** Back returns to Library with no playback mutation.
- **Truth:** Local derived-index query; selection revalidated against accessible media.
- **Recovery:** Stale result preserves query and reconciles; rejected playback keeps context.
- **A11y / TalkBack:** Search heading → query field → result count → rows → bottom navigation.
- **System handoff:** Typing and result lists are unavailable in active Driving Mode.
- **Minimum target:** ≥48dp; Driving previous/pause/next/seek-back ≥64dp, Mic ≥72dp, Exit ≥56dp.
- **At 200% font scaling:** Compose implementation must reflow; static screenshot does not prove runtime layout.
- **Privacy:** no raw audio, transcript, search query or local music metadata in analytics; sample names are synthetic.
- **Ownership:** service-owned playback; Voice coordinator-owned Voice session; platform surfaces excluded from generated app screenshots.
- **P2:** Liked Songs remains removable; no P0 build dependency.

## Evidence limitation

Deterministic restyling of the *approved* low-fidelity SVG into Night Signal produces this reviewable static visual. It is not an independently signed design approval, a Compose implementation, nor proof of precise physical tap-target or 200% reflow behavior.
