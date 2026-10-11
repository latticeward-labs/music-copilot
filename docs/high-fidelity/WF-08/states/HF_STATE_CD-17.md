# High-Fidelity State — CD-17 · Attempt-one failure

**Issue:** #17 · **Phase:** 5 · **Disposition:** DEDICATED_HIFI / REVIEWABLE
**PNG:** HF_STATE_CD-17.png · **PNG SHA-256:** 0e4636e9efcb3d19e75f8fb59c4b99217f36437f7a1ec01c302de98b13ea28e5
**Priority:** P0 · **WF:** WF-08
**UF:** UF-10 · **GS:** — (not on GS-01..05 path)
**State:** CD-17 · **Owner:** App + Voice coordinator
**Screen owner:** UF-08/09/10 · Voice overlay · **Recovery owner:** UF-08 / UF-09 / UF-10
**Source selected frame:** WF-08_VOICE_FAILED_RETRY
**Source Markdown/SVG:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_FAILED_RETRY.md / https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_FAILED_RETRY.svg

## Exact UI copy (device surface only)

- "DRIVING / PLAYER CONTEXT"
- "Voice attempt failed"
- "One Retry, then Manual"
- "!"
- "Manual controls"
- "Retry once"
- "Manual / Exit remains reachable"

## State and structure contract

- **Source approved hierarchy:** WF-08_VOICE_FAILED_RETRY; existing WF family only; no new app destination.
- **Visual-state specialization:** State-specific, one-sentence error copy on the approved Voice Failed overlay; **no guessed match**.
- **Component mapping:** CMP-19, CMP-06. All component definitions are in docs/design-system/components/COMPONENT_TOKENS.json.
- **Structural delta:** None. Source-aligned copy specialization only (where explicitly noted), not new controls or navigation.
- **Entry:** Timeout/no-match/provider/language/network/audio or execution rejection.
- **Primary action:** Retry once; Manual/Cancel remains available.
- **Exit:** Retry → second Listening; dismiss/manual → initiating surface; second failure is terminal.
- **Back:** Back returns immediately and exhausts no hidden action.
- **Recovery:** Exactly one explicit Retry; no auto-capture, coercive countdown or third attempt.
- **Actual state owner / truth:** Typed Voice/session failure; actual playback remains service-owned.; App + Voice coordinator.
- **Preserve/restoration:** Retry budget is ephemeral; coarse failure only.
- **System-only constraints:** Settings recovery is available only outside active Driving Mode.

## Related source frames

- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_LISTENING.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_PROCESSING.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_CONFIRMING.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_EXECUTED.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_FAILED_RETRY.md

## Accessibility / privacy / error containment

- **Reading order:** Failed heading → short reason → Retry once → Manual → Cancel/Exit.
- **Semantics:** written state and a non-color cue; observable 48dp minimum, or larger Driving overrides.
- **200% scaling:** reflow required in Compose implementation, not claimed by this static artifact.
- **Voice:** no auto-listening; 6-second bounded capture, max one retry, at most two candidates, one short error/TTS sentence.
- **Driving:** one fixed layer; immediate Exit, no typing/long lists/nested navigation/automatic entry.
- **Analytics:** allowed aggregate success/latency classifications only; prohibit raw audio, full transcript, personal media titles/artists, precise position, queries and candidate labels.
- **P2 isolation:** if P2/Like removed, P0 and P1 behavior remains unaffected.

## Review status

Generated from approved source SVG using deterministic token substitutions; PNG has been decoded and checksum recorded. Design review is **AI-assisted candidate review only**. No human signatures are inferred or fabricated. State-specific behaviors share the approved canonical owning WF; they are not unapproved independent routes.
