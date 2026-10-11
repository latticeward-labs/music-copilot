# High-Fidelity State — CB-09 · Search Idle / Query

**Issue:** #17 · **Phase:** 5 · **Disposition:** DEDICATED_HIFI / REVIEWABLE
**PNG:** HF_STATE_CB-09.png · **PNG SHA-256:** aae27437eb262fcd9145334219506de4b25b74c9544d80e001f76e755876cde9
**Priority:** P0 · **WF:** WF-05
**UF:** UF-04 · **GS:** — (not on GS-01..05 path)
**State:** CB-09 · **Owner:** App
**Screen owner:** UF-04 · Search · **Recovery owner:** UF-04
**Source selected frame:** WF-05_SEARCH_RESULTS
**Source Markdown/SVG:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-05_SEARCH_RESULTS.md / https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-05_SEARCH_RESULTS.svg

## Exact UI copy (device surface only)

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

## State and structure contract

- **Source approved hierarchy:** WF-05_SEARCH_RESULTS; existing WF family only; no new app destination.
- **Visual-state specialization:** Stale result preserves query and reconciles; rejected playback keeps context.
- **Component mapping:** CMP-03, CMP-10, CMP-11. All component definitions are in docs/design-system/components/COMPONENT_TOKENS.json.
- **Structural delta:** None. Source-aligned copy specialization only (where explicitly noted), not new controls or navigation.
- **Entry:** Local query returns one or more indexed matches.
- **Primary action:** Select a result to request service-owned playback.
- **Exit:** Validated selection → Full Player; edit/clear remains in Search.
- **Back:** Back returns to Library with no playback mutation.
- **Recovery:** Stale result preserves query and reconciles; rejected playback keeps context.
- **Actual state owner / truth:** Local derived-index query; selection revalidated against accessible media.; App.
- **Preserve/restoration:** Query is active-surface state only; no query/result telemetry.
- **System-only constraints:** Typing and result lists are unavailable in active Driving Mode.

## Related source frames

- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-05_SEARCH_RESULTS.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-05_SEARCH_NO_RESULTS.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-05_SEARCH_ERROR.md

## Accessibility / privacy / error containment

- **Reading order:** Search heading → query field → result count → rows → bottom navigation.
- **Semantics:** written state and a non-color cue; observable 48dp minimum, or larger Driving overrides.
- **200% scaling:** reflow required in Compose implementation, not claimed by this static artifact.
- **Voice:** no auto-listening; 6-second bounded capture, max one retry, at most two candidates, one short error/TTS sentence.
- **Driving:** one fixed layer; immediate Exit, no typing/long lists/nested navigation/automatic entry.
- **Analytics:** allowed aggregate success/latency classifications only; prohibit raw audio, full transcript, personal media titles/artists, precise position, queries and candidate labels.
- **P2 isolation:** if P2/Like removed, P0 and P1 behavior remains unaffected.

## Review status

Generated from approved source SVG using deterministic token substitutions; PNG has been decoded and checksum recorded. Design review is **AI-assisted candidate review only**. No human signatures are inferred or fabricated. State-specific behaviors share the approved canonical owning WF; they are not unapproved independent routes.
