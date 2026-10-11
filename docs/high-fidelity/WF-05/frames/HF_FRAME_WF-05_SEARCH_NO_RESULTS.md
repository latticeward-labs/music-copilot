# HF Frame Review — WF-05_SEARCH_NO_RESULTS

**Issue/phase:** #17 / 5 · **Source:** WF-05_SEARCH_NO_RESULTS · **Status:** Reviewable generated evidence (not separate human sign-off)
**Image:** HF_FRAME_WF-05_SEARCH_NO_RESULTS.png · **SHA-256:** 36308e7b59cb33f7db96aee6da8a4ef21a03953b5b4dc0fa8792af4d69b4a9ef
**Priority:** P0 · **Ownership:** App
**Canonical State IDs:** CB-09, CB-11 · **UF:** UF-04 · **GS:** GS-02
**Approved source:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-05_SEARCH_NO_RESULTS.md and https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-05_SEARCH_NO_RESULTS.svg
**Structural deviations:** None: exact source SVG hierarchy retained; approved Night Signal tokens applied.

## Exact rendered app copy

- "Back"
- "Search"
- "aurora"
- "Clear"
- "□  No local results"
- "Edit or clear the preserved query."
- "Home"
- "Liked"
- "Settings"

## Behavioral boundary

- **Entry:** Local query completes with zero matches.
- **Primary action:** Edit query; Clear is secondary.
- **Exit:** Updated query → Results/No Result/Error; Back → Library.
- **Back:** Preserves no mutation; returns to Library.
- **Truth:** Completed local index query; no remote fallback.
- **Recovery:** Preserve query and Browse access; subsequent error uses Search Error.
- **A11y / TalkBack:** Search heading → query → No results label → Edit/Clear guidance.
- **System handoff:** No online catalog or network handoff.
- **Minimum target:** ≥48dp; Driving previous/pause/next/seek-back ≥64dp, Mic ≥72dp, Exit ≥56dp.
- **At 200% font scaling:** Compose implementation must reflow; static screenshot does not prove runtime layout.
- **Privacy:** no raw audio, transcript, search query or local music metadata in analytics; sample names are synthetic.
- **Ownership:** service-owned playback; Voice coordinator-owned Voice session; platform surfaces excluded from generated app screenshots.
- **P2:** Liked Songs remains removable; no P0 build dependency.

## Evidence limitation

Deterministic restyling of the *approved* low-fidelity SVG into Night Signal produces this reviewable static visual. It is not an independently signed design approval, a Compose implementation, nor proof of precise physical tap-target or 200% reflow behavior.
