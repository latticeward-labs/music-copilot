# High-Fidelity State — CC-07 · Driving No Track

**Issue:** #17 · **Phase:** 5 · **Disposition:** DEDICATED_HIFI / REVIEWABLE
**PNG:** HF_STATE_CC-07.png · **PNG SHA-256:** a7a1960095fcea3fddd3ed689f22b21aa65e9b07dd31d0753f424f6552f2ce07
**Priority:** P0 · **WF:** WF-07
**UF:** UF-07 · **GS:** GS-04
**State:** CC-07 · **Owner:** App + Playback Service
**Screen owner:** UF-07 · Driving Mode · **Recovery owner:** UF-07
**Source selected frame:** WF-07_DRIVING_MODE_NO_TRACK
**Source Markdown/SVG:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-07_DRIVING_MODE_NO_TRACK.md / https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-07_DRIVING_MODE_NO_TRACK.svg

## Exact UI copy (device surface only)

- "Exit"
- "DRIVING MODE"
- "NO TRACK"
- "No current track"
- "Exit to browse local music"
- "Mic · Voice"
- "One layer · no typing · no lists"

## State and structure contract

- **Source approved hierarchy:** WF-07_DRIVING_MODE_NO_TRACK; existing WF family only; no new app destination.
- **Visual-state specialization:** Failure remains No Track and routes to Library after Exit; no list/typing.
- **Component mapping:** CMP-05, CMP-06, CMP-07. All component definitions are in docs/design-system/components/COMPONENT_TOKENS.json.
- **Structural delta:** None. Source-aligned copy specialization only (where explicitly noted), not new controls or navigation.
- **Entry:** Complete snapshot has no current playable item.
- **Primary action:** Exit; Play last is shown only when a valid local reference exists.
- **Exit:** Valid Play last → Playing/Paused; Exit → Home/Library.
- **Back:** Back equals Exit.
- **Recovery:** Failure remains No Track and routes to Library after Exit; no list/typing.
- **Actual state owner / truth:** Service snapshot plus at most one revalidated MediaStore item.; App + Playback Service.
- **Preserve/restoration:** Only an already-approved local last-item reference may be considered.
- **System-only constraints:** No Android Auto, picker or system list is introduced.

## Related source frames

- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-07_DRIVING_MODE_PLAYING.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-07_DRIVING_MODE_PAUSED.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-07_DRIVING_MODE_NO_TRACK.md

## Accessibility / privacy / error containment

- **Reading order:** No track heading → explanation → optional Play last → Mic → Exit.
- **Semantics:** written state and a non-color cue; observable 48dp minimum, or larger Driving overrides.
- **200% scaling:** reflow required in Compose implementation, not claimed by this static artifact.
- **Voice:** no auto-listening; 6-second bounded capture, max one retry, at most two candidates, one short error/TTS sentence.
- **Driving:** one fixed layer; immediate Exit, no typing/long lists/nested navigation/automatic entry.
- **Analytics:** allowed aggregate success/latency classifications only; prohibit raw audio, full transcript, personal media titles/artists, precise position, queries and candidate labels.
- **P2 isolation:** if P2/Like removed, P0 and P1 behavior remains unaffected.

## Review status

Generated from approved source SVG using deterministic token substitutions; PNG has been decoded and checksum recorded. Design review is **AI-assisted candidate review only**. No human signatures are inferred or fabricated. State-specific behaviors share the approved canonical owning WF; they are not unapproved independent routes.
