# High-Fidelity State — CD-03 · Listening attempt 1

**Issue:** #17 · **Phase:** 5 · **Disposition:** DEDICATED_HIFI / REVIEWABLE
**PNG:** HF_STATE_CD-03.png · **PNG SHA-256:** 431025bd6937f514ff9fb10f636745eef521af8df2a84c2954f9234b7a9c68a9
**Priority:** P0 · **WF:** WF-08
**UF:** UF-08 · **GS:** GS-04
**State:** CD-03 · **Owner:** App + Voice coordinator
**Screen owner:** UF-08/09/10 · Voice overlay · **Recovery owner:** UF-08 / UF-09 / UF-10
**Source selected frame:** WF-08_VOICE_LISTENING
**Source Markdown/SVG:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_LISTENING.md / https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_LISTENING.svg

## Exact UI copy (device surface only)

- "DRIVING / PLAYER CONTEXT"
- "Listening"
- "Six-second attempt"
- "MIC"
- "Cancel"
- "Manual / Exit remains reachable"

## State and structure contract

- **Source approved hierarchy:** WF-08_VOICE_LISTENING; existing WF family only; no new app destination.
- **Visual-state specialization:** Timeout/no-match/provider failure preserves playback and exposes one Retry.
- **Component mapping:** CMP-19, CMP-06. All component definitions are in docs/design-system/components/COMPONENT_TOKENS.json.
- **Structural delta:** None. Source-aligned copy specialization only (where explicitly noted), not new controls or navigation.
- **Entry:** Explicit microphone tap after permission/provider capability passes.
- **Primary action:** Cancel.
- **Exit:** Result → Processing; failure → Failed/Retry; Cancel → initiating surface.
- **Back:** Back equals Cancel and releases capture/duck.
- **Recovery:** Timeout/no-match/provider failure preserves playback and exposes one Retry.
- **Actual state owner / truth:** Voice coordinator transient session; playback remains service-owned.; App + Voice coordinator.
- **Preserve/restoration:** Raw audio/transcript and attempt state are not persisted.
- **System-only constraints:** Android mic indicator/dialog is system-owned; capture never starts on mode entry.

## Related source frames

- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_LISTENING.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_PROCESSING.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_CONFIRMING.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_EXECUTED.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_FAILED_RETRY.md

## Accessibility / privacy / error containment

- **Reading order:** Listening status → time limit → Cancel → underlying manual/Exit controls.
- **Semantics:** written state and a non-color cue; observable 48dp minimum, or larger Driving overrides.
- **200% scaling:** reflow required in Compose implementation, not claimed by this static artifact.
- **Voice:** no auto-listening; 6-second bounded capture, max one retry, at most two candidates, one short error/TTS sentence.
- **Driving:** one fixed layer; immediate Exit, no typing/long lists/nested navigation/automatic entry.
- **Analytics:** allowed aggregate success/latency classifications only; prohibit raw audio, full transcript, personal media titles/artists, precise position, queries and candidate labels.
- **P2 isolation:** if P2/Like removed, P0 and P1 behavior remains unaffected.

## Review status

Generated from approved source SVG using deterministic token substitutions; PNG has been decoded and checksum recorded. Design review is **AI-assisted candidate review only**. No human signatures are inferred or fabricated. State-specific behaviors share the approved canonical owning WF; they are not unapproved independent routes.
