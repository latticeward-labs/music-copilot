# High-Fidelity State — CC-11 · P1 selected-device connected

**Issue:** #17 · **Phase:** 5 · **Disposition:** DEDICATED_HIFI / REVIEWABLE
**PNG:** HF_STATE_CC-11.png · **PNG SHA-256:** 2decf43fd87ed95b2c81778454b7d3a34620cc2aaaaadfae4f55be32979ed801
**Priority:** P1 · **WF:** WF-07
**UF:** UF-07 / UF-12 · **GS:** — (not on GS-01..05 path)
**State:** CC-11 · **Owner:** Hybrid
**Screen owner:** UF-07 · Entry suggestion · **Recovery owner:** UF-07 / UF-12
**Source selected frame:** WF-07_DRIVING_MODE_PLAYING
**Source Markdown/SVG:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-07_DRIVING_MODE_PLAYING.md / https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-07_DRIVING_MODE_PLAYING.svg

## Exact UI copy (device surface only)

- "Exit"
- "DRIVING MODE"
- "ARTWORK"
- "Aurora Lights"
- "Local artist"
- "PLAYING"
- "Mic · Voice"
- "One layer · no typing · no lists"

## State and structure contract

- **Source approved hierarchy:** WF-07_DRIVING_MODE_PLAYING; existing WF family only; no new app destination.
- **Visual-state specialization:** P1 connected-device truth is an annotation only; no Bluetooth picker, deep menu or consent in Driving.
- **Component mapping:** CMP-05, CMP-06, CMP-07. All component definitions are in docs/design-system/components/COMPONENT_TOKENS.json.
- **Structural delta:** None. Source-aligned copy specialization only (where explicitly noted), not new controls or navigation.
- **Entry:** Explicit Driving Mode entry with a complete Playing snapshot.
- **Primary action:** Pause; four large controls, Mic and Exit remain fixed.
- **Exit:** Exit is immediate; Mic opens an overlay, not a new destination.
- **Back:** Back behaves as Exit and returns to the prior non-driving surface.
- **Recovery:** Pending/rejected commands refresh actual state; route events hand off to UF-11.
- **Actual state owner / truth:** Complete MediaSessionService snapshot and supported actions.; Hybrid.
- **Preserve/restoration:** UI owns no player state; Driving Mode is not auto-entered.
- **System-only constraints:** Calls/focus/routes are system events reflected through service truth.

## Related source frames

- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-07_DRIVING_MODE_PLAYING.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-07_DRIVING_MODE_PAUSED.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-07_DRIVING_MODE_NO_TRACK.md

## Accessibility / privacy / error containment

- **Reading order:** Playing → title/artist → Previous → Pause → Next → Seek back → Mic → Exit.
- **Semantics:** written state and a non-color cue; observable 48dp minimum, or larger Driving overrides.
- **200% scaling:** reflow required in Compose implementation, not claimed by this static artifact.
- **Voice:** no auto-listening; 6-second bounded capture, max one retry, at most two candidates, one short error/TTS sentence.
- **Driving:** one fixed layer; immediate Exit, no typing/long lists/nested navigation/automatic entry.
- **Analytics:** allowed aggregate success/latency classifications only; prohibit raw audio, full transcript, personal media titles/artists, precise position, queries and candidate labels.
- **P2 isolation:** if P2/Like removed, P0 and P1 behavior remains unaffected.

## Review status

Generated from approved source SVG using deterministic token substitutions; PNG has been decoded and checksum recorded. Design review is **AI-assisted candidate review only**. No human signatures are inferred or fabricated. State-specific behaviors share the approved canonical owning WF; they are not unapproved independent routes.
