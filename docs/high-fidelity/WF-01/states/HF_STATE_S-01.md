# High-Fidelity State — S-01 · Splash - Loading, Migration Error

**Issue:** #17 · **Phase:** 5 · **Disposition:** DEDICATED_HIFI / REVIEWABLE
**PNG:** HF_STATE_S-01.png · **PNG SHA-256:** 0ebe6f274b70ffe960a75efd69283f1369830e5e8f6139d3eaeec6665eb3434a
**Priority:** P0 · **WF:** WF-01
**UF:** UF-01 · **GS:** GS-01
**State:** S-01 · **Owner:** App
**Screen owner:** UF-01 · Splash · **Recovery owner:** UF-01
**Source selected frame:** WF-01_SPLASH_STARTUP_MIGRATION_ERROR
**Source Markdown/SVG:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-01_SPLASH_STARTUP_MIGRATION_ERROR.md / https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-01_SPLASH_STARTUP_MIGRATION_ERROR.svg

## Exact UI copy (device surface only)

- "Startup problem"
- "Your verified local data is retained."
- "Retry"
- "No destructive reset"

## State and structure contract

- **Source approved hierarchy:** WF-01_SPLASH_STARTUP_MIGRATION_ERROR; existing WF family only; no new app destination.
- **Visual-state specialization:** Retry; repeated failure remains concise and non-destructive.
- **Component mapping:** CMP-01. All component definitions are in docs/design-system/components/COMPONENT_TOKENS.json.
- **Structural delta:** None. Source-aligned copy specialization only (where explicitly noted), not new controls or navigation.
- **Entry:** Typed startup or local migration failure.
- **Primary action:** Retry startup once the user explicitly chooses Retry.
- **Exit:** Successful Retry routes from freshly verified state.
- **Back:** System Back exits; Retry never traps focus.
- **Recovery:** Retry; repeated failure remains concise and non-destructive.
- **Actual state owner / truth:** Local migration result; no fabricated Empty/Ready state.; App.
- **Preserve/restoration:** Verified prior data is retained; no reinstall/clear-data default.
- **System-only constraints:** No Android Settings handoff is implied.

## Related source frames

- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-01_SPLASH_LOADING.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-01_SPLASH_STARTUP_MIGRATION_ERROR.md

## Accessibility / privacy / error containment

- **Reading order:** Error heading → explanation → Retry.
- **Semantics:** written state and a non-color cue; observable 48dp minimum, or larger Driving overrides.
- **200% scaling:** reflow required in Compose implementation, not claimed by this static artifact.
- **Voice:** no auto-listening; 6-second bounded capture, max one retry, at most two candidates, one short error/TTS sentence.
- **Driving:** one fixed layer; immediate Exit, no typing/long lists/nested navigation/automatic entry.
- **Analytics:** allowed aggregate success/latency classifications only; prohibit raw audio, full transcript, personal media titles/artists, precise position, queries and candidate labels.
- **P2 isolation:** if P2/Like removed, P0 and P1 behavior remains unaffected.

## Review status

Generated from approved source SVG using deterministic token substitutions; PNG has been decoded and checksum recorded. Design review is **AI-assisted candidate review only**. No human signatures are inferred or fabricated. State-specific behaviors share the approved canonical owning WF; they are not unapproved independent routes.
