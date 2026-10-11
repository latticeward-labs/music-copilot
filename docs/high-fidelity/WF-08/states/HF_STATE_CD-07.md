# High-Fidelity State — CD-07 · Executed

**Issue:** #17 · **Phase:** 5 · **Disposition:** DEDICATED_HIFI / REVIEWABLE
**PNG:** HF_STATE_CD-07.png · **PNG SHA-256:** 277ab34efd7327ba29e82f0f67847685a403c2e9cdd354136efff286c3c4b87b
**Priority:** P0 · **WF:** WF-08
**UF:** UF-08 · **GS:** GS-04
**State:** CD-07 · **Owner:** App + Voice coordinator
**Screen owner:** UF-08/09/10 · Voice overlay · **Recovery owner:** UF-08 / UF-09 / UF-10
**Source selected frame:** WF-08_VOICE_EXECUTED
**Source Markdown/SVG:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_EXECUTED.md / https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_EXECUTED.svg

## Exact UI copy (device surface only)

- "Done"
- "Next track acknowledged"
- "✓"
- "Playback refreshed from service"
- "Dismiss"
- "Manual / Exit remains reachable"

## State and structure contract

- **Source approved hierarchy:** WF-08_VOICE_EXECUTED; existing WF family only; no new app destination.
- **Visual-state specialization:** TTS failure remains Executed; stale/rejected command uses Failed.
- **Component mapping:** CMP-19, CMP-06. All component definitions are in docs/design-system/components/COMPONENT_TOKENS.json.
- **Structural delta:** None. Source-aligned copy specialization only (where explicitly noted), not new controls or navigation.
- **Entry:** Playback service acknowledges the submitted direct/confirmed command.
- **Primary action:** Dismiss/return to manual controls.
- **Exit:** Returns to the initiating surface with actual refreshed state.
- **Back:** Back dismisses; it does not undo acknowledged playback.
- **Recovery:** TTS failure remains Executed; stale/rejected command uses Failed.
- **Actual state owner / truth:** MediaSession refresh; optional TTS is non-authoritative.; App + Voice coordinator.
- **Preserve/restoration:** Only service playback state persists; no transcript.
- **System-only constraints:** No system surface is created.

## Related source frames

- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_LISTENING.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_PROCESSING.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_CONFIRMING.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_EXECUTED.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_FAILED_RETRY.md

## Accessibility / privacy / error containment

- **Reading order:** Executed status → short result → Dismiss/manual controls.
- **Semantics:** written state and a non-color cue; observable 48dp minimum, or larger Driving overrides.
- **200% scaling:** reflow required in Compose implementation, not claimed by this static artifact.
- **Voice:** no auto-listening; 6-second bounded capture, max one retry, at most two candidates, one short error/TTS sentence.
- **Driving:** one fixed layer; immediate Exit, no typing/long lists/nested navigation/automatic entry.
- **Analytics:** allowed aggregate success/latency classifications only; prohibit raw audio, full transcript, personal media titles/artists, precise position, queries and candidate labels.
- **P2 isolation:** if P2/Like removed, P0 and P1 behavior remains unaffected.

## Review status

Generated from approved source SVG using deterministic token substitutions; PNG has been decoded and checksum recorded. Design review is **AI-assisted candidate review only**. No human signatures are inferred or fabricated. State-specific behaviors share the approved canonical owning WF; they are not unapproved independent routes.
