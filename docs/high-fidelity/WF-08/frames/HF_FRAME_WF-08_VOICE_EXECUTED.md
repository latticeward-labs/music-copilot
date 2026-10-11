# HF Frame Review — WF-08_VOICE_EXECUTED

**Issue/phase:** #17 / 5 · **Source:** WF-08_VOICE_EXECUTED · **Status:** Reviewable generated evidence (not separate human sign-off)
**Image:** HF_FRAME_WF-08_VOICE_EXECUTED.png · **SHA-256:** 277ab34efd7327ba29e82f0f67847685a403c2e9cdd354136efff286c3c4b87b
**Priority:** P0 · **Ownership:** App + Voice Coordinator
**Canonical State IDs:** CD-07 · **UF:** UF-08 / UF-09 / UF-10 · **GS:** GS-04 recovery path
**Approved source:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_EXECUTED.md and https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_EXECUTED.svg
**Structural deviations:** None: exact source SVG hierarchy retained; approved Night Signal tokens applied.

## Exact rendered app copy

- "Done"
- "Next track acknowledged"
- "✓"
- "Playback refreshed from service"
- "Dismiss"
- "Manual / Exit remains reachable"

## Behavioral boundary

- **Entry:** Playback service acknowledges the submitted direct/confirmed command.
- **Primary action:** Dismiss/return to manual controls.
- **Exit:** Returns to the initiating surface with actual refreshed state.
- **Back:** Back dismisses; it does not undo acknowledged playback.
- **Truth:** MediaSession refresh; optional TTS is non-authoritative.
- **Recovery:** TTS failure remains Executed; stale/rejected command uses Failed.
- **A11y / TalkBack:** Executed status → short result → Dismiss/manual controls.
- **System handoff:** No system surface is created.
- **Minimum target:** ≥48dp; Driving previous/pause/next/seek-back ≥64dp, Mic ≥72dp, Exit ≥56dp.
- **At 200% font scaling:** Compose implementation must reflow; static screenshot does not prove runtime layout.
- **Privacy:** no raw audio, transcript, search query or local music metadata in analytics; sample names are synthetic.
- **Ownership:** service-owned playback; Voice coordinator-owned Voice session; platform surfaces excluded from generated app screenshots.
- **P2:** Liked Songs remains removable; no P0 build dependency.

## Evidence limitation

Deterministic restyling of the *approved* low-fidelity SVG into Night Signal produces this reviewable static visual. It is not an independently signed design approval, a Compose implementation, nor proof of precise physical tap-target or 200% reflow behavior.
