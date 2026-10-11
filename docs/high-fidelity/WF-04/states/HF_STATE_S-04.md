# High-Fidelity State — S-04 · Home / Library - Scanning, Empty, Ready, Recoverable Failure, Revoked-Reconciled

**Issue:** #17 · **Phase:** 5 · **Disposition:** DEDICATED_HIFI / REVIEWABLE
**PNG:** HF_STATE_S-04.png · **PNG SHA-256:** 70c712b7db32a8d67b5ee8a2892a30b80f162de6e132d793b9407d939cd23c57
**Priority:** P0 · **WF:** WF-04
**UF:** UF-03 · **GS:** GS-01
**State:** S-04 · **Owner:** App
**Screen owner:** UF-03 · Home/Library readiness · **Recovery owner:** UF-03
**Source selected frame:** WF-04_HOME_LIBRARY_SCANNING
**Source Markdown/SVG:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-04_HOME_LIBRARY_SCANNING.md / https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-04_HOME_LIBRARY_SCANNING.svg

## Exact UI copy (device surface only)

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

## State and structure contract

- **Source approved hierarchy:** WF-04_HOME_LIBRARY_SCANNING; existing WF family only; no new app destination.
- **Visual-state specialization:** Typed failure routes to Recoverable Error; prior verified state is preserved when possible.
- **Component mapping:** CMP-03, CMP-04, CMP-09, CMP-11. All component definitions are in docs/design-system/components/COMPONENT_TOKENS.json.
- **Structural delta:** None. Source-aligned copy specialization only (where explicitly noted), not new controls or navigation.
- **Entry:** First scan, rescan, reconciliation, or verified rebuild.
- **Primary action:** Wait; Back/navigation remains available when safe.
- **Exit:** Empty, Ready, or Recoverable Error.
- **Back:** Back leaves the surface without claiming scan cancellation unless supported.
- **Recovery:** Typed failure routes to Recoverable Error; prior verified state is preserved when possible.
- **Actual state owner / truth:** MediaStore existence plus rebuildable local index progress.; App.
- **Preserve/restoration:** Index work survives UI recreation; UI progress is not authoritative.
- **System-only constraints:** Media permission loss hands off to WF-03; no fake Empty state.

## Related source frames

- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-04_HOME_LIBRARY_SCANNING.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-04_HOME_LIBRARY_EMPTY.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-04_HOME_LIBRARY_READY.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-04_HOME_LIBRARY_RECOVERABLE_ERROR.md

## Accessibility / privacy / error containment

- **Reading order:** Library heading → Scanning status/progress → available navigation.
- **Semantics:** written state and a non-color cue; observable 48dp minimum, or larger Driving overrides.
- **200% scaling:** reflow required in Compose implementation, not claimed by this static artifact.
- **Voice:** no auto-listening; 6-second bounded capture, max one retry, at most two candidates, one short error/TTS sentence.
- **Driving:** one fixed layer; immediate Exit, no typing/long lists/nested navigation/automatic entry.
- **Analytics:** allowed aggregate success/latency classifications only; prohibit raw audio, full transcript, personal media titles/artists, precise position, queries and candidate labels.
- **P2 isolation:** if P2/Like removed, P0 and P1 behavior remains unaffected.

## Review status

Generated from approved source SVG using deterministic token substitutions; PNG has been decoded and checksum recorded. Design review is **AI-assisted candidate review only**. No human signatures are inferred or fabricated. State-specific behaviors share the approved canonical owning WF; they are not unapproved independent routes.
