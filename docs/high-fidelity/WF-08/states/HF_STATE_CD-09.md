# High-Fidelity State — CD-09 · Confirming one candidate

**Issue:** #17 · **Phase:** 5 · **Disposition:** DEDICATED_HIFI / REVIEWABLE
**PNG:** HF_STATE_CD-09.png · **PNG SHA-256:** 89eb4d69a23362fe7e5ac01134e9d2a1b7e74e85e2621a81c515385c3e922e4f
**Priority:** P0 · **WF:** WF-08
**UF:** UF-09 · **GS:** — (not on GS-01..05 path)
**State:** CD-09 · **Owner:** App + Voice coordinator
**Screen owner:** UF-08/09/10 · Voice overlay · **Recovery owner:** UF-08 / UF-09 / UF-10
**Source selected frame:** WF-08_VOICE_CONFIRMING
**Source Markdown/SVG:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_CONFIRMING.md / https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_CONFIRMING.svg

## Exact UI copy (device surface only)

- "DRIVING / PLAYER CONTEXT"
- "Which artist?"
- "Choose one presented candidate"
- "1 · Aurora"
- "Neither"
- "Manual / Exit remains reachable"

## State and structure contract

- **Source approved hierarchy:** WF-08_VOICE_CONFIRMING; existing WF family only; no new app destination.
- **Visual-state specialization:** Exactly one immutable source-supplied candidate; second candidate control is hidden; Neither and system Back/Cancel remain.
- **Component mapping:** CMP-19, CMP-06. All component definitions are in docs/design-system/components/COMPONENT_TOKENS.json.
- **Structural delta:** None. Source-aligned copy specialization only (where explicitly noted), not new controls or navigation.
- **Entry:** Deterministic resolver returns one low-confidence candidate or two artists.
- **Primary action:** Choose one of at most two large options; Neither/Cancel is reachable.
- **Exit:** Presented selection → single command pending; reject/timeout → safe failure/manual.
- **Back:** Back equals Cancel; no execution.
- **Recovery:** One bounded question; never keyboard, long list or guessed execution.
- **Actual state owner / truth:** Immutable session candidate set.; App + Voice coordinator.
- **Preserve/restoration:** Candidates and names are not telemetry/evidence.
- **System-only constraints:** No new permission or system destination.

## Related source frames

- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_LISTENING.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_PROCESSING.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_CONFIRMING.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_EXECUTED.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_FAILED_RETRY.md

## Accessibility / privacy / error containment

- **Reading order:** Prompt → candidate 1 → candidate 2 when present → Neither → Cancel.
- **Semantics:** written state and a non-color cue; observable 48dp minimum, or larger Driving overrides.
- **200% scaling:** reflow required in Compose implementation, not claimed by this static artifact.
- **Voice:** no auto-listening; 6-second bounded capture, max one retry, at most two candidates, one short error/TTS sentence.
- **Driving:** one fixed layer; immediate Exit, no typing/long lists/nested navigation/automatic entry.
- **Analytics:** allowed aggregate success/latency classifications only; prohibit raw audio, full transcript, personal media titles/artists, precise position, queries and candidate labels.
- **P2 isolation:** if P2/Like removed, P0 and P1 behavior remains unaffected.

## Review status

Generated from approved source SVG using deterministic token substitutions; PNG has been decoded and checksum recorded. Design review is **AI-assisted candidate review only**. No human signatures are inferred or fabricated. State-specific behaviors share the approved canonical owning WF; they are not unapproved independent routes.
