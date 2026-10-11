# High-Fidelity State — CE-20 · Bluetooth Selection

**Issue:** #17 · **Phase:** 5 · **Disposition:** DEDICATED_HIFI / REVIEWABLE
**PNG:** HF_STATE_CE-20.png · **PNG SHA-256:** 399cd6039f8e3f3e9f6978a1539de442bedc003acc7bedd48a35342f58d93539
**Priority:** P1 · **WF:** WF-10
**UF:** UF-12 · **GS:** — (not on GS-01..05 path)
**State:** CE-20 · **Owner:** Hybrid
**Screen owner:** UF-12 · Bluetooth selection · **Recovery owner:** UF-12
**Source selected frame:** WF-10_SETTINGS_BLUETOOTH_UPDATED
**Source Markdown/SVG:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-10_SETTINGS_BLUETOOTH_UPDATED.md / https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-10_SETTINGS_BLUETOOTH_UPDATED.svg

## Exact UI copy (device surface only)

- "Back"
- "Settings"
- "P1 / REMOVABLE PREFERENCE"
- "Selected Bluetooth device"
- "Vehicle audio · locally stored"
- "Connection does not open Driving Mode."
- "Remove"
- "Save"
- "✓ Updated"
- "Home"
- "Search"
- "Liked"

## State and structure contract

- **Source approved hierarchy:** WF-10_SETTINGS_BLUETOOTH_UPDATED; existing WF family only; no new app destination.
- **Visual-state specialization:** Permission/unavailable routes to explicit outside-driving recovery; failure is textual.
- **Component mapping:** CMP-18, CMP-01, CMP-21. All component definitions are in docs/design-system/components/COMPONENT_TOKENS.json.
- **Structural delta:** None. Source-aligned copy specialization only (where explicitly noted), not new controls or navigation.
- **Entry:** P1 Bluetooth setup selected outside Driving Mode.
- **Primary action:** Save selection; Remove is explicit.
- **Exit:** Acknowledged update → Settings; failure retains previous value.
- **Back:** Cancels unsaved changes.
- **Recovery:** Permission/unavailable routes to explicit outside-driving recovery; failure is textual.
- **Actual state owner / truth:** Eligible platform devices plus locally stored selected preference.; Hybrid.
- **Preserve/restoration:** Selected identity stays local and is excluded from telemetry.
- **System-only constraints:** Connection alone never launches Driving Mode; platform identity remains protected.

## Related source frames

- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-10_SETTINGS_DEFAULT.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-10_SETTINGS_BLUETOOTH_UPDATED.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-10_SETTINGS_CLEAR_DATA_HISTORY_CONFIRMATION.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-10_SETTINGS_CLEARED_PRIVACY_STATUS.md

## Accessibility / privacy / error containment

- **Reading order:** P1 label → Bluetooth heading → selected device → Save/Remove → result.
- **Semantics:** written state and a non-color cue; observable 48dp minimum, or larger Driving overrides.
- **200% scaling:** reflow required in Compose implementation, not claimed by this static artifact.
- **Voice:** no auto-listening; 6-second bounded capture, max one retry, at most two candidates, one short error/TTS sentence.
- **Driving:** one fixed layer; immediate Exit, no typing/long lists/nested navigation/automatic entry.
- **Analytics:** allowed aggregate success/latency classifications only; prohibit raw audio, full transcript, personal media titles/artists, precise position, queries and candidate labels.
- **P2 isolation:** if P2/Like removed, P0 and P1 behavior remains unaffected.

## Review status

Generated from approved source SVG using deterministic token substitutions; PNG has been decoded and checksum recorded. Design review is **AI-assisted candidate review only**. No human signatures are inferred or fabricated. State-specific behaviors share the approved canonical owning WF; they are not unapproved independent routes.
