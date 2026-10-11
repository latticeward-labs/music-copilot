# High-Fidelity State — CD-05 · Processing

**Issue:** #17 · **Phase:** 5 · **Disposition:** DEDICATED_HIFI / REVIEWABLE
**PNG:** HF_STATE_CD-05.png · **PNG SHA-256:** f6286422689bb936f4c713798ac6ef60b7f3930ad83ae9dd9b225663c06cde23
**Priority:** P0 · **WF:** WF-08
**UF:** UF-08 · **GS:** GS-04
**State:** CD-05 · **Owner:** App + Voice coordinator
**Screen owner:** UF-08/09/10 · Voice overlay · **Recovery owner:** UF-08 / UF-09 / UF-10
**Source selected frame:** WF-08_VOICE_PROCESSING
**Source Markdown/SVG:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_PROCESSING.md / https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_PROCESSING.svg

## Exact UI copy (device surface only)

- "DRIVING / PLAYER CONTEXT"
- "Processing"
- "Mapping one of six intents"
- "Cancel"
- "Manual / Exit remains reachable"

## State and structure contract

- **Source approved hierarchy:** WF-08_VOICE_PROCESSING; existing WF family only; no new app destination.
- **Visual-state specialization:** Unknown/unsupported/not-found becomes safe failure, never approximation.
- **Component mapping:** CMP-19, CMP-06. All component definitions are in docs/design-system/components/COMPONENT_TOKENS.json.
- **Structural delta:** None. Source-aligned copy specialization only (where explicitly noted), not new controls or navigation.
- **Entry:** STT result arrives and capture ends.
- **Primary action:** Cancel/manual fallback.
- **Exit:** Certain → command pending/executed; ambiguous → Confirming; failure → Failed/Retry.
- **Back:** Back cancels stale callbacks and returns immediately.
- **Recovery:** Unknown/unsupported/not-found becomes safe failure, never approximation.
- **Actual state owner / truth:** Restricted mapper supports exactly six intents; service not yet mutated.; App + Voice coordinator.
- **Preserve/restoration:** No transcript or exact confidence telemetry.
- **System-only constraints:** Provider processing boundary follows approved disclosure/consent.

## Related source frames

- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_LISTENING.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_PROCESSING.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_CONFIRMING.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_EXECUTED.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_FAILED_RETRY.md

## Accessibility / privacy / error containment

- **Reading order:** Processing status → Cancel → underlying Exit/manual controls.
- **Semantics:** written state and a non-color cue; observable 48dp minimum, or larger Driving overrides.
- **200% scaling:** reflow required in Compose implementation, not claimed by this static artifact.
- **Voice:** no auto-listening; 6-second bounded capture, max one retry, at most two candidates, one short error/TTS sentence.
- **Driving:** one fixed layer; immediate Exit, no typing/long lists/nested navigation/automatic entry.
- **Analytics:** allowed aggregate success/latency classifications only; prohibit raw audio, full transcript, personal media titles/artists, precise position, queries and candidate labels.
- **P2 isolation:** if P2/Like removed, P0 and P1 behavior remains unaffected.

## Review status

Generated from approved source SVG using deterministic token substitutions; PNG has been decoded and checksum recorded. Design review is **AI-assisted candidate review only**. No human signatures are inferred or fabricated. State-specific behaviors share the approved canonical owning WF; they are not unapproved independent routes.
