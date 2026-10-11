# High-Fidelity State — CD-23 · Cloud consent required/declined

**Issue:** #17 · **Phase:** 5 · **Disposition:** DEDICATED_HIFI / REVIEWABLE
**PNG:** HF_STATE_CD-23.png · **PNG SHA-256:** fec2fdec595b47d0b5512cd588c92135ab736b7caa9ed1821717bd54800e892b
**Priority:** P0 · **WF:** WF-10
**UF:** UF-10 / UF-12 · **GS:** — (not on GS-01..05 path)
**State:** CD-23 · **Owner:** App + provider boundary
**Screen owner:** Settings · Cloud consent · **Recovery owner:** UF-12
**Source selected frame:** WF-10_SETTINGS_DEFAULT
**Source Markdown/SVG:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-10_SETTINGS_DEFAULT.md / https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-10_SETTINGS_DEFAULT.svg

## Exact UI copy (device surface only)

- "Back"
- "Settings"
- "Permissions"
- "Current platform status"
- "Open"
- "Bluetooth suggestion"
- "P1 preference"
- "Privacy"
- "Local-first controls"
- "Clear data / history"
- "Selected categories only"
- "Home"
- "Search"
- "Liked"

## State and structure contract

- **Source approved hierarchy:** WF-10_SETTINGS_DEFAULT; existing WF family only; no new app destination.
- **Visual-state specialization:** Consent may be presented only through the already approved non-driving Settings/Privacy boundary; never during Driving Mode.
- **Component mapping:** CMP-18, CMP-01, CMP-21. All component definitions are in docs/design-system/components/COMPONENT_TOKENS.json.
- **Structural delta:** None. Source-aligned copy specialization only (where explicitly noted), not new controls or navigation.
- **Entry:** Settings opened from non-driving navigation.
- **Primary action:** Open one settings section.
- **Exit:** Section/update/handoff; Back returns to prior surface.
- **Back:** Returns without changing values or playback.
- **Recovery:** Load/update error retains previous value and exposes Retry where safe.
- **Actual state owner / truth:** Preferences plus fresh platform/provider truth.; App + provider boundary.
- **Preserve/restoration:** Only approved local settings/consent are stored.
- **System-only constraints:** Android Settings handoff is explicit and never launched from active Driving Mode.

## Related source frames

- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-10_SETTINGS_DEFAULT.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-10_SETTINGS_BLUETOOTH_UPDATED.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-10_SETTINGS_CLEAR_DATA_HISTORY_CONFIRMATION.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-10_SETTINGS_CLEARED_PRIVACY_STATUS.md

## Accessibility / privacy / error containment

- **Reading order:** Settings heading → section headings → current values/actions → bottom navigation.
- **Semantics:** written state and a non-color cue; observable 48dp minimum, or larger Driving overrides.
- **200% scaling:** reflow required in Compose implementation, not claimed by this static artifact.
- **Voice:** no auto-listening; 6-second bounded capture, max one retry, at most two candidates, one short error/TTS sentence.
- **Driving:** one fixed layer; immediate Exit, no typing/long lists/nested navigation/automatic entry.
- **Analytics:** allowed aggregate success/latency classifications only; prohibit raw audio, full transcript, personal media titles/artists, precise position, queries and candidate labels.
- **P2 isolation:** if P2/Like removed, P0 and P1 behavior remains unaffected.

## Review status

Generated from approved source SVG using deterministic token substitutions; PNG has been decoded and checksum recorded. Design review is **AI-assisted candidate review only**. No human signatures are inferred or fabricated. State-specific behaviors share the approved canonical owning WF; they are not unapproved independent routes.
