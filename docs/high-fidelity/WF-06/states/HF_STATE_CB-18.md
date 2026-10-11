# High-Fidelity State — CB-18 · Queue View

**Issue:** #17 · **Phase:** 5 · **Disposition:** DEDICATED_HIFI / REVIEWABLE
**PNG:** HF_STATE_CB-18.png · **PNG SHA-256:** 5adfd996f48967cfdd6c6e2e33dc0d2a948d015fbaca1b2ea4b15ad1e3f9a402
**Priority:** P0 · **WF:** WF-06
**UF:** UF-05 · **GS:** — (not on GS-01..05 path)
**State:** CB-18 · **Owner:** App + Playback Service
**Screen owner:** UF-05 · Full Player/Queue · **Recovery owner:** UF-05
**Source selected frame:** WF-06_FULL_PLAYER_PLAYING
**Source Markdown/SVG:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-06_FULL_PLAYER_PLAYING.md / https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-06_FULL_PLAYER_PLAYING.svg

## Exact UI copy (device surface only)

- "Back"
- "Now playing"
- "Queue"
- "Current queue"
- "Aurora Lights"
- "Local artist"
- "PLAYING"
- "Night Drive"
- "Ocean Lines"

## State and structure contract

- **Source approved hierarchy:** WF-06_FULL_PLAYER_PLAYING; existing WF family only; no new app destination.
- **Visual-state specialization:** Existing Queue action is the entry point; the WF-06 player remains the owning surface. Queue presentation shares MediaSession ownership; no invented route.
- **Component mapping:** CMP-03, CMP-05, CMP-07, CMP-13. All component definitions are in docs/design-system/components/COMPONENT_TOKENS.json.
- **Structural delta:** Existing authorized state-content specialization (CB-09, CB-17, CB-18, CE-21, CE-22, CD-23); reviewed as within owning WF, with human UX approval still required for copy/layout refinements. No new navigation, runtime owner or capability.
- **Entry:** MediaSessionService acknowledges playback with a valid item.
- **Primary action:** Pause; Next/Previous/Seek/Queue remain available as supported.
- **Exit:** Back keeps playback; Driving Mode requires explicit entry; Queue remains same authority.
- **Back:** Returns to initiating app surface without stopping playback.
- **Recovery:** Command/controller failure refreshes actual snapshot; corrupt item uses bounded recovery.
- **Actual state owner / truth:** MediaSessionService snapshot: item, queue, state, position and supported actions.; App + Playback Service.
- **Preserve/restoration:** P0 live service state survives UI recreation.
- **System-only constraints:** Notification/lock/external controls are system-only synchronized surfaces.

## Related source frames

- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-06_FULL_PLAYER_PLAYING.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-06_FULL_PLAYER_PAUSED.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-06_FULL_PLAYER_CORRUPT_FILE_RECOVERY.md

## Accessibility / privacy / error containment

- **Reading order:** Now playing → title/artist → position → Previous → Pause → Next → Queue.
- **Semantics:** written state and a non-color cue; observable 48dp minimum, or larger Driving overrides.
- **200% scaling:** reflow required in Compose implementation, not claimed by this static artifact.
- **Voice:** no auto-listening; 6-second bounded capture, max one retry, at most two candidates, one short error/TTS sentence.
- **Driving:** one fixed layer; immediate Exit, no typing/long lists/nested navigation/automatic entry.
- **Analytics:** allowed aggregate success/latency classifications only; prohibit raw audio, full transcript, personal media titles/artists, precise position, queries and candidate labels.
- **P2 isolation:** if P2/Like removed, P0 and P1 behavior remains unaffected.

## Review status

Generated from approved source SVG using deterministic token substitutions; PNG has been decoded and checksum recorded. Design review is **AI-assisted candidate review only**. No human signatures are inferred or fabricated. State-specific behaviors share the approved canonical owning WF; they are not unapproved independent routes.
