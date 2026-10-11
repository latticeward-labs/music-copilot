# High-Fidelity State — CB-12 · Search Error

**Issue:** #17 · **Phase:** 5 · **Disposition:** DEDICATED_HIFI / REVIEWABLE
**PNG:** HF_STATE_CB-12.png · **PNG SHA-256:** 2730fbdf47478dedced29f158960e61ed3b682d74e804308897d69feac263fb1
**Priority:** P0 · **WF:** WF-05
**UF:** UF-04 · **GS:** GS-02
**State:** CB-12 · **Owner:** App
**Screen owner:** UF-04 · Search · **Recovery owner:** UF-04
**Source selected frame:** WF-05_SEARCH_ERROR
**Source Markdown/SVG:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-05_SEARCH_ERROR.md / https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-05_SEARCH_ERROR.svg

## Exact UI copy (device surface only)

- "Back"
- "Search"
- "aurora"
- "Clear"
- "!  Search failed"
- "The verified library is still available."
- "Retry"
- "Home"
- "Liked"
- "Settings"

## State and structure contract

- **Source approved hierarchy:** WF-05_SEARCH_ERROR; existing WF family only; no new app destination.
- **Visual-state specialization:** Retry/edit/clear; use UF-03 only if index truth is actually stale.
- **Component mapping:** CMP-03, CMP-10, CMP-11. All component definitions are in docs/design-system/components/COMPONENT_TOKENS.json.
- **Structural delta:** None. Source-aligned copy specialization only (where explicitly noted), not new controls or navigation.
- **Entry:** Typed local query/index failure.
- **Primary action:** Retry; Edit and Clear remain available.
- **Exit:** Retry → Results/No Result; Back → verified Library.
- **Back:** Returns to Library; unrelated playback is unchanged.
- **Recovery:** Retry/edit/clear; use UF-03 only if index truth is actually stale.
- **Actual state owner / truth:** Local search repository failure; verified library remains authoritative.; App.
- **Preserve/restoration:** Active query is retained; coarse error only.
- **System-only constraints:** No network fallback or Android surface.

## Related source frames

- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-05_SEARCH_RESULTS.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-05_SEARCH_NO_RESULTS.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-05_SEARCH_ERROR.md

## Accessibility / privacy / error containment

- **Reading order:** Error heading → concise reason → Retry → Edit/Clear → Back.
- **Semantics:** written state and a non-color cue; observable 48dp minimum, or larger Driving overrides.
- **200% scaling:** reflow required in Compose implementation, not claimed by this static artifact.
- **Voice:** no auto-listening; 6-second bounded capture, max one retry, at most two candidates, one short error/TTS sentence.
- **Driving:** one fixed layer; immediate Exit, no typing/long lists/nested navigation/automatic entry.
- **Analytics:** allowed aggregate success/latency classifications only; prohibit raw audio, full transcript, personal media titles/artists, precise position, queries and candidate labels.
- **P2 isolation:** if P2/Like removed, P0 and P1 behavior remains unaffected.

## Review status

Generated from approved source SVG using deterministic token substitutions; PNG has been decoded and checksum recorded. Design review is **AI-assisted candidate review only**. No human signatures are inferred or fabricated. State-specific behaviors share the approved canonical owning WF; they are not unapproved independent routes.
