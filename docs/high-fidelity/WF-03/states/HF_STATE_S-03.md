# High-Fidelity State — S-03 · Permissions - Granted, Denied-requestable, Permanently Denied

**Issue:** #17 · **Phase:** 5 · **Disposition:** DEDICATED_HIFI / REVIEWABLE
**PNG:** HF_STATE_S-03.png · **PNG SHA-256:** e1cdb9550399f08976a9f138763838290241b287713bae0480ade64fcd862f34
**Priority:** P0 · **WF:** WF-03
**UF:** UF-02 · **GS:** GS-01
**State:** S-03 · **Owner:** Hybrid
**Screen owner:** UF-02 · Permission recovery · **Recovery owner:** UF-02
**Source selected frame:** WF-03_PERMISSIONS_PERMANENTLY_DENIED
**Source Markdown/SVG:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-03_PERMISSIONS_PERMANENTLY_DENIED.md / https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-03_PERMISSIONS_PERMANENTLY_DENIED.svg

## Exact UI copy (device surface only)

- "Back"
- "Permissions"
- "PERMISSION"
- "↗"
- "Open Android Settings"
- "Permission must be changed explicitly."
- "Open Settings"
- "Not now"
- "[ANDROID / SYSTEM] handoff"
- "Fresh result is authoritative"

## State and structure contract

- **Source approved hierarchy:** WF-03_PERMISSIONS_PERMANENTLY_DENIED; existing WF family only; no new app destination.
- **Visual-state specialization:** Granted, requestable-denied, or still-permanent are separate return outcomes.
- **Component mapping:** CMP-01, CMP-14, CMP-21. All component definitions are in docs/design-system/components/COMPONENT_TOKENS.json.
- **Structural delta:** None. Source-aligned copy specialization only (where explicitly noted), not new controls or navigation.
- **Entry:** Android reports permanent denial or Settings-only recovery.
- **Primary action:** Open Settings; Not now preserves manual use.
- **Exit:** Open Settings → Android-owned surface; return → three-way recheck.
- **Back:** Back/Not now returns to initiating manual surface.
- **Recovery:** Granted, requestable-denied, or still-permanent are separate return outcomes.
- **Actual state owner / truth:** Android permission result after every return.; Hybrid.
- **Preserve/restoration:** No assumption of Settings success; no auto-loop.
- **System-only constraints:** [ANDROID / SYSTEM] Settings is a handoff, never an app destination.

## Related source frames

- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-03_PERMISSIONS_RATIONALE_REQUEST.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-03_PERMISSIONS_GRANTED.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-03_PERMISSIONS_DENIED.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-03_PERMISSIONS_PERMANENTLY_DENIED.md

## Accessibility / privacy / error containment

- **Reading order:** Permanently denied heading → consequence → Open Settings → Not now → system handoff.
- **Semantics:** written state and a non-color cue; observable 48dp minimum, or larger Driving overrides.
- **200% scaling:** reflow required in Compose implementation, not claimed by this static artifact.
- **Voice:** no auto-listening; 6-second bounded capture, max one retry, at most two candidates, one short error/TTS sentence.
- **Driving:** one fixed layer; immediate Exit, no typing/long lists/nested navigation/automatic entry.
- **Analytics:** allowed aggregate success/latency classifications only; prohibit raw audio, full transcript, personal media titles/artists, precise position, queries and candidate labels.
- **P2 isolation:** if P2/Like removed, P0 and P1 behavior remains unaffected.

## Review status

Generated from approved source SVG using deterministic token substitutions; PNG has been decoded and checksum recorded. Design review is **AI-assisted candidate review only**. No human signatures are inferred or fabricated. State-specific behaviors share the approved canonical owning WF; they are not unapproved independent routes.
