# High-Fidelity State — UL-04 · Ready

**Issue:** #17 · **Phase:** 5 · **Disposition:** DEDICATED_HIFI / REVIEWABLE
**PNG:** HF_STATE_UL-04.png · **PNG SHA-256:** dd71e0166ee681c78f55aed5570cb25a7e4061e7689ed9174aa243276ab2d503
**Priority:** P2 · **WF:** WF-09
**UF:** UF-13 · **GS:** — (not on GS-01..05 path)
**State:** UL-04 · **Owner:** App
**Screen owner:** UF-13 · Liked Songs · **Recovery owner:** UF-13
**Source selected frame:** WF-09_LIKED_SONGS_READY
**Source Markdown/SVG:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-09_LIKED_SONGS_READY.md / https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-09_LIKED_SONGS_READY.svg

## Exact UI copy (device surface only)

- "Back"
- "Liked Songs"
- "P2 / REMOVABLE"
- "Play list"
- "Aurora Lights"
- "Liked · local track"
- "Unlike"
- "Night Drive"
- "Ocean Lines"
- "Home"
- "Search"
- "Liked"
- "Settings"

## State and structure contract

- **Source approved hierarchy:** WF-09_LIKED_SONGS_READY; existing WF family only; no new app destination.
- **Visual-state specialization:** Stale/rejected item refreshes projection; load failure preserves manual playback.
- **Component mapping:** CMP-04, CMP-11, CMP-20. All component definitions are in docs/design-system/components/COMPONENT_TOKENS.json.
- **Structural delta:** None. Source-aligned copy specialization only (where explicitly noted), not new controls or navigation.
- **Entry:** P2 projection contains accessible liked tracks.
- **Primary action:** Play list or select a row; unlike preserves media files.
- **Exit:** Playback acknowledgement → Full Player; Back → Library.
- **Back:** Returns without stopping unrelated playback.
- **Recovery:** Stale/rejected item refreshes projection; load failure preserves manual playback.
- **Actual state owner / truth:** Local Like repository plus accessible MediaStore projection; service owns playback.; App.
- **Preserve/restoration:** Acknowledged likes persist locally; missing media reference is cleaned, media untouched.
- **System-only constraints:** Feature remains optional/removable and never blocks P0/P1.

## Related source frames

- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-09_LIKED_SONGS_EMPTY.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-09_LIKED_SONGS_READY.md

## Accessibility / privacy / error containment

- **Reading order:** P2/Removable label → Play list → rows → Like state/action → Back.
- **Semantics:** written state and a non-color cue; observable 48dp minimum, or larger Driving overrides.
- **200% scaling:** reflow required in Compose implementation, not claimed by this static artifact.
- **Voice:** no auto-listening; 6-second bounded capture, max one retry, at most two candidates, one short error/TTS sentence.
- **Driving:** one fixed layer; immediate Exit, no typing/long lists/nested navigation/automatic entry.
- **Analytics:** allowed aggregate success/latency classifications only; prohibit raw audio, full transcript, personal media titles/artists, precise position, queries and candidate labels.
- **P2 isolation:** if P2/Like removed, P0 and P1 behavior remains unaffected.

## Review status

Generated from approved source SVG using deterministic token substitutions; PNG has been decoded and checksum recorded. Design review is **AI-assisted candidate review only**. No human signatures are inferred or fabricated. State-specific behaviors share the approved canonical owning WF; they are not unapproved independent routes.
