# High-Fidelity State — CE-23 · Clear Selection

**Issue:** #17 · **Phase:** 5 · **Disposition:** DEDICATED_HIFI / REVIEWABLE
**PNG:** HF_STATE_CE-23.png · **PNG SHA-256:** d93d3c4bbfd400ac14197891173b310aa2e2713341972ec435028a7a006840c9
**Priority:** P0 · **WF:** WF-10
**UF:** UF-12 · **GS:** — (not on GS-01..05 path)
**State:** CE-23 · **Owner:** App + provider boundary
**Screen owner:** UF-12 · Privacy/data controls · **Recovery owner:** UF-12
**Source selected frame:** WF-10_SETTINGS_CLEAR_DATA_HISTORY_CONFIRMATION
**Source Markdown/SVG:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-10_SETTINGS_CLEAR_DATA_HISTORY_CONFIRMATION.md / https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-10_SETTINGS_CLEAR_DATA_HISTORY_CONFIRMATION.svg

## Exact UI copy (device surface only)

- "Back"
- "Settings"
- "Clear selected data?"
- "✓ Voice history"
- "✓ Diagnostics queue"
- "Preserved: local music files"
- "Preserved: active playback"
- "Confirm clear"
- "Cancel"
- "Home"
- "Search"
- "Liked"

## State and structure contract

- **Source approved hierarchy:** WF-10_SETTINGS_CLEAR_DATA_HISTORY_CONFIRMATION; existing WF family only; no new app destination.
- **Visual-state specialization:** No destructive default; partial/provider failure is reported truthfully later.
- **Component mapping:** CMP-18, CMP-01, CMP-21. All component definitions are in docs/design-system/components/COMPONENT_TOKENS.json.
- **Structural delta:** None. Source-aligned copy specialization only (where explicitly noted), not new controls or navigation.
- **Entry:** At least one non-preselected clear category is selected.
- **Primary action:** Confirm clear; Cancel is equally reachable.
- **Exit:** Confirm → Clearing/Result; Cancel → selection or Settings.
- **Back:** Back equals Cancel; no deletion occurs.
- **Recovery:** No destructive default; partial/provider failure is reported truthfully later.
- **Actual state owner / truth:** Selected local categories and documented provider boundaries.; App + provider boundary.
- **Preserve/restoration:** No deletion before confirmation; playback and user media are protected.
- **System-only constraints:** Never claims remote deletion unless the provider confirms it.

## Related source frames

- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-10_SETTINGS_DEFAULT.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-10_SETTINGS_BLUETOOTH_UPDATED.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-10_SETTINGS_CLEAR_DATA_HISTORY_CONFIRMATION.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-10_SETTINGS_CLEARED_PRIVACY_STATUS.md

## Accessibility / privacy / error containment

- **Reading order:** Warning heading → selected categories → preserved categories → Confirm clear → Cancel.
- **Semantics:** written state and a non-color cue; observable 48dp minimum, or larger Driving overrides.
- **200% scaling:** reflow required in Compose implementation, not claimed by this static artifact.
- **Voice:** no auto-listening; 6-second bounded capture, max one retry, at most two candidates, one short error/TTS sentence.
- **Driving:** one fixed layer; immediate Exit, no typing/long lists/nested navigation/automatic entry.
- **Analytics:** allowed aggregate success/latency classifications only; prohibit raw audio, full transcript, personal media titles/artists, precise position, queries and candidate labels.
- **P2 isolation:** if P2/Like removed, P0 and P1 behavior remains unaffected.

## Review status

Generated from approved source SVG using deterministic token substitutions; PNG has been decoded and checksum recorded. Design review is **AI-assisted candidate review only**. No human signatures are inferred or fabricated. State-specific behaviors share the approved canonical owning WF; they are not unapproved independent routes.
