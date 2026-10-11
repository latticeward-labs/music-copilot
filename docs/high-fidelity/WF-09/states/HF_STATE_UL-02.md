# High-Fidelity State — UL-02 · Entry loading

**Issue:** #17 · **Phase:** 5 · **Disposition:** DEDICATED_HIFI / REVIEWABLE
**PNG:** HF_STATE_UL-02.png · **PNG SHA-256:** f6ac819de3c4e1471ee71235829d23f151e6a3c8f683c44783dd7002711c0e93
**Priority:** P2 · **WF:** WF-09
**UF:** UF-13 · **GS:** — (not on GS-01..05 path)
**State:** UL-02 · **Owner:** App
**Screen owner:** UF-13 · Liked Songs · **Recovery owner:** UF-13
**Source selected frame:** WF-09_LIKED_SONGS_EMPTY
**Source Markdown/SVG:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-09_LIKED_SONGS_EMPTY.md / https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-09_LIKED_SONGS_EMPTY.svg

## Exact UI copy (device surface only)

- "Back"
- "Liked Songs"
- "P2 / REMOVABLE"
- "♡  No liked songs"
- "No recommendations are invented."
- "Back to Library"
- "Home"
- "Search"
- "Liked"
- "Settings"

## State and structure contract

- **Source approved hierarchy:** WF-09_LIKED_SONGS_EMPTY; existing WF family only; no new app destination.
- **Visual-state specialization:** Load/reconcile failure exposes Retry/Back; missing media is reconciled without file mutation.
- **Component mapping:** CMP-04, CMP-11, CMP-20. All component definitions are in docs/design-system/components/COMPONENT_TOKENS.json.
- **Structural delta:** None. Source-aligned copy specialization only (where explicitly noted), not new controls or navigation.
- **Entry:** P2 entry load completes with zero accessible liked tracks.
- **Primary action:** Back to Library; no invented recommendations.
- **Exit:** Back; future acknowledged Like can produce Ready.
- **Back:** Returns to initiating surface.
- **Recovery:** Load/reconcile failure exposes Retry/Back; missing media is reconciled without file mutation.
- **Actual state owner / truth:** Local user-state repository projected through accessible MediaStore items.; App.
- **Preserve/restoration:** Empty is a valid P2 terminal state.
- **System-only constraints:** No P0/P1 dependency; feature can be removed without placeholder.

## Related source frames

- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-09_LIKED_SONGS_EMPTY.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-09_LIKED_SONGS_READY.md

## Accessibility / privacy / error containment

- **Reading order:** P2/Removable label → heading → Empty explanation → Back.
- **Semantics:** written state and a non-color cue; observable 48dp minimum, or larger Driving overrides.
- **200% scaling:** reflow required in Compose implementation, not claimed by this static artifact.
- **Voice:** no auto-listening; 6-second bounded capture, max one retry, at most two candidates, one short error/TTS sentence.
- **Driving:** one fixed layer; immediate Exit, no typing/long lists/nested navigation/automatic entry.
- **Analytics:** allowed aggregate success/latency classifications only; prohibit raw audio, full transcript, personal media titles/artists, precise position, queries and candidate labels.
- **P2 isolation:** if P2/Like removed, P0 and P1 behavior remains unaffected.

## Review status

Generated from approved source SVG using deterministic token substitutions; PNG has been decoded and checksum recorded. Design review is **AI-assisted candidate review only**. No human signatures are inferred or fabricated. State-specific behaviors share the approved canonical owning WF; they are not unapproved independent routes.
