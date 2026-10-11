# High-Fidelity State — CB-17 · End of Queue / Paused-Completed

**Issue:** #17 · **Phase:** 5 · **Disposition:** DEDICATED_HIFI / REVIEWABLE
**PNG:** HF_STATE_CB-17.png · **PNG SHA-256:** cbef23e39f37540d1ddc0126f02731da3b0d78db64837370903f003561d908d1
**Priority:** P0 · **WF:** WF-06
**UF:** UF-05 · **GS:** — (not on GS-01..05 path)
**State:** CB-17 · **Owner:** App + Playback Service
**Screen owner:** UF-05 · Full Player/Queue · **Recovery owner:** UF-05
**Source selected frame:** WF-06_FULL_PLAYER_PAUSED
**Source Markdown/SVG:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-06_FULL_PLAYER_PAUSED.md / https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-06_FULL_PLAYER_PAUSED.svg

## Exact UI copy (device surface only)

- "Back"
- "Now playing"
- "Queue"
- "Aurora Lights"
- "Local artist"
- "Play"
- "COMPLETED"

## State and structure contract

- **Source approved hierarchy:** WF-06_FULL_PLAYER_PAUSED; existing WF family only; no new app destination.
- **Visual-state specialization:** Paused/Completed derives from the actual service snapshot; never display Playing after queue end.
- **Component mapping:** CMP-03, CMP-05, CMP-07, CMP-13. All component definitions are in docs/design-system/components/COMPONENT_TOKENS.json.
- **Structural delta:** Existing authorized state-content specialization (CB-09, CB-17, CB-18, CE-21, CE-22, CD-23); reviewed as within owning WF, with human UX approval still required for copy/layout refinements. No new navigation, runtime owner or capability.
- **Entry:** Service reports Paused after command, interruption or entry.
- **Primary action:** Play.
- **Exit:** Acknowledged Play → Playing; Back preserves queue/position.
- **Back:** Returns without auto-playing.
- **Recovery:** Controller failure refreshes; interruption route rules may require safe manual resume.
- **Actual state owner / truth:** MediaSessionService snapshot.; App + Playback Service.
- **Preserve/restoration:** Active service retains queue/position; UI is observer only.
- **System-only constraints:** No optimistic resume after route/focus changes.

## Related source frames

- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-06_FULL_PLAYER_PLAYING.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-06_FULL_PLAYER_PAUSED.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-06_FULL_PLAYER_CORRUPT_FILE_RECOVERY.md

## Accessibility / privacy / error containment

- **Reading order:** Paused state → title/artist → position → Previous → Play → Next → Queue.
- **Semantics:** written state and a non-color cue; observable 48dp minimum, or larger Driving overrides.
- **200% scaling:** reflow required in Compose implementation, not claimed by this static artifact.
- **Voice:** no auto-listening; 6-second bounded capture, max one retry, at most two candidates, one short error/TTS sentence.
- **Driving:** one fixed layer; immediate Exit, no typing/long lists/nested navigation/automatic entry.
- **Analytics:** allowed aggregate success/latency classifications only; prohibit raw audio, full transcript, personal media titles/artists, precise position, queries and candidate labels.
- **P2 isolation:** if P2/Like removed, P0 and P1 behavior remains unaffected.

## Review status

Generated from approved source SVG using deterministic token substitutions; PNG has been decoded and checksum recorded. Design review is **AI-assisted candidate review only**. No human signatures are inferred or fabricated. State-specific behaviors share the approved canonical owning WF; they are not unapproved independent routes.
