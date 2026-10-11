# High-Fidelity State — CB-11 · Search No Result

**Issue:** #17 · **Phase:** 5 · **Disposition:** DEDICATED_HIFI / REVIEWABLE
**PNG:** HF_STATE_CB-11.png · **PNG SHA-256:** 36308e7b59cb33f7db96aee6da8a4ef21a03953b5b4dc0fa8792af4d69b4a9ef
**Priority:** P0 · **WF:** WF-05
**UF:** UF-04 · **GS:** GS-02
**State:** CB-11 · **Owner:** App
**Screen owner:** UF-04 · Search · **Recovery owner:** UF-04
**Source selected frame:** WF-05_SEARCH_NO_RESULTS
**Source Markdown/SVG:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-05_SEARCH_NO_RESULTS.md / https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-05_SEARCH_NO_RESULTS.svg

## Exact UI copy (device surface only)

- "Back"
- "Search"
- "aurora"
- "Clear"
- "□  No local results"
- "Edit or clear the preserved query."
- "Home"
- "Liked"
- "Settings"

## State and structure contract

- **Source approved hierarchy:** WF-05_SEARCH_NO_RESULTS; existing WF family only; no new app destination.
- **Visual-state specialization:** Preserve query and Browse access; subsequent error uses Search Error.
- **Component mapping:** CMP-03, CMP-10, CMP-11. All component definitions are in docs/design-system/components/COMPONENT_TOKENS.json.
- **Structural delta:** None. Source-aligned copy specialization only (where explicitly noted), not new controls or navigation.
- **Entry:** Local query completes with zero matches.
- **Primary action:** Edit query; Clear is secondary.
- **Exit:** Updated query → Results/No Result/Error; Back → Library.
- **Back:** Preserves no mutation; returns to Library.
- **Recovery:** Preserve query and Browse access; subsequent error uses Search Error.
- **Actual state owner / truth:** Completed local index query; no remote fallback.; App.
- **Preserve/restoration:** Query persists only while Search remains active.
- **System-only constraints:** No online catalog or network handoff.

## Related source frames

- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-05_SEARCH_RESULTS.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-05_SEARCH_NO_RESULTS.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-05_SEARCH_ERROR.md

## Accessibility / privacy / error containment

- **Reading order:** Search heading → query → No results label → Edit/Clear guidance.
- **Semantics:** written state and a non-color cue; observable 48dp minimum, or larger Driving overrides.
- **200% scaling:** reflow required in Compose implementation, not claimed by this static artifact.
- **Voice:** no auto-listening; 6-second bounded capture, max one retry, at most two candidates, one short error/TTS sentence.
- **Driving:** one fixed layer; immediate Exit, no typing/long lists/nested navigation/automatic entry.
- **Analytics:** allowed aggregate success/latency classifications only; prohibit raw audio, full transcript, personal media titles/artists, precise position, queries and candidate labels.
- **P2 isolation:** if P2/Like removed, P0 and P1 behavior remains unaffected.

## Review status

Generated from approved source SVG using deterministic token substitutions; PNG has been decoded and checksum recorded. Design review is **AI-assisted candidate review only**. No human signatures are inferred or fabricated. State-specific behaviors share the approved canonical owning WF; they are not unapproved independent routes.
