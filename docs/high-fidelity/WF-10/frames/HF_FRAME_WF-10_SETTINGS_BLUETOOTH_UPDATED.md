# HF Frame Review — WF-10_SETTINGS_BLUETOOTH_UPDATED

**Issue/phase:** #17 / 5 · **Source:** WF-10_SETTINGS_BLUETOOTH_UPDATED · **Status:** Reviewable generated evidence (not separate human sign-off)
**Image:** HF_FRAME_WF-10_SETTINGS_BLUETOOTH_UPDATED.png · **SHA-256:** 399cd6039f8e3f3e9f6978a1539de442bedc003acc7bedd48a35342f58d93539
**Priority:** P1 / Removable preference · **Ownership:** App + platform/provider boundary
**Canonical State IDs:** CE-20, CE-16, CE-17 · **UF:** UF-12 · **GS:** — (not on GS-01..05 path)
**Approved source:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-10_SETTINGS_BLUETOOTH_UPDATED.md and https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-10_SETTINGS_BLUETOOTH_UPDATED.svg
**Structural deviations:** None: exact source SVG hierarchy retained; approved Night Signal tokens applied.

## Exact rendered app copy

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

## Behavioral boundary

- **Entry:** P1 Bluetooth setup selected outside Driving Mode.
- **Primary action:** Save selection; Remove is explicit.
- **Exit:** Acknowledged update → Settings; failure retains previous value.
- **Back:** Cancels unsaved changes.
- **Truth:** Eligible platform devices plus locally stored selected preference.
- **Recovery:** Permission/unavailable routes to explicit outside-driving recovery; failure is textual.
- **A11y / TalkBack:** P1 label → Bluetooth heading → selected device → Save/Remove → result.
- **System handoff:** Connection alone never launches Driving Mode; platform identity remains protected.
- **Minimum target:** ≥48dp; Driving previous/pause/next/seek-back ≥64dp, Mic ≥72dp, Exit ≥56dp.
- **At 200% font scaling:** Compose implementation must reflow; static screenshot does not prove runtime layout.
- **Privacy:** no raw audio, transcript, search query or local music metadata in analytics; sample names are synthetic.
- **Ownership:** service-owned playback; Voice coordinator-owned Voice session; platform surfaces excluded from generated app screenshots.
- **P2:** Liked Songs remains removable; no P0 build dependency.

## Evidence limitation

Deterministic restyling of the *approved* low-fidelity SVG into Night Signal produces this reviewable static visual. It is not an independently signed design approval, a Compose implementation, nor proof of precise physical tap-target or 200% reflow behavior.
