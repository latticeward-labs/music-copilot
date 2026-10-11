# High-Fidelity State — CC-06 · Driving Paused/Ended

**Issue:** #17 · **Phase:** 5 · **Disposition:** DEDICATED_HIFI / REVIEWABLE
**PNG:** HF_STATE_CC-06.png · **PNG SHA-256:** 15e2041d756ca076fccf9097f314ca54f22c0db76ef7113dcc34cecc88b75fb9
**Priority:** P0 · **WF:** WF-07
**UF:** UF-07 · **GS:** GS-04
**State:** CC-06 · **Owner:** App + Playback Service
**Screen owner:** UF-07 · Driving Mode · **Recovery owner:** UF-07
**Source selected frame:** WF-07_DRIVING_MODE_PAUSED
**Source Markdown/SVG:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-07_DRIVING_MODE_PAUSED.md / https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-07_DRIVING_MODE_PAUSED.svg

## Exact UI copy (device surface only)

- "Exit"
- "DRIVING MODE"
- "ARTWORK"
- "Aurora Lights"
- "Local artist"
- "PAUSED"
- "Mic · Voice"
- "One layer · no typing · no lists"

## State and structure contract

- **Source approved hierarchy:** WF-07_DRIVING_MODE_PAUSED; existing WF family only; no new app destination.
- **Visual-state specialization:** Unsafe route remains safely Paused; refresh/reconnect never surprises with audio.
- **Component mapping:** CMP-05, CMP-06, CMP-07. All component definitions are in docs/design-system/components/COMPONENT_TOKENS.json.
- **Structural delta:** None. Source-aligned copy specialization only (where explicitly noted), not new controls or navigation.
- **Entry:** Complete snapshot reports Paused/Ended.
- **Primary action:** Play; Exit remains prominent.
- **Exit:** Acknowledged Play → Playing; Exit → prior surface.
- **Back:** Back equals immediate Exit; never auto-resumes.
- **Recovery:** Unsafe route remains safely Paused; refresh/reconnect never surprises with audio.
- **Actual state owner / truth:** MediaSessionService Paused snapshot.; App + Playback Service.
- **Preserve/restoration:** No auto-play on entry or recreation.
- **System-only constraints:** Route/focus safety may disable Play with a textual reason.

## Related source frames

- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-07_DRIVING_MODE_PLAYING.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-07_DRIVING_MODE_PAUSED.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-07_DRIVING_MODE_NO_TRACK.md

## Accessibility / privacy / error containment

- **Reading order:** Paused → title/artist → Previous → Play → Next → Seek back → Mic → Exit.
- **Semantics:** written state and a non-color cue; observable 48dp minimum, or larger Driving overrides.
- **200% scaling:** reflow required in Compose implementation, not claimed by this static artifact.
- **Voice:** no auto-listening; 6-second bounded capture, max one retry, at most two candidates, one short error/TTS sentence.
- **Driving:** one fixed layer; immediate Exit, no typing/long lists/nested navigation/automatic entry.
- **Analytics:** allowed aggregate success/latency classifications only; prohibit raw audio, full transcript, personal media titles/artists, precise position, queries and candidate labels.
- **P2 isolation:** if P2/Like removed, P0 and P1 behavior remains unaffected.

## Review status

Generated from approved source SVG using deterministic token substitutions; PNG has been decoded and checksum recorded. Design review is **AI-assisted candidate review only**. No human signatures are inferred or fabricated. State-specific behaviors share the approved canonical owning WF; they are not unapproved independent routes.
