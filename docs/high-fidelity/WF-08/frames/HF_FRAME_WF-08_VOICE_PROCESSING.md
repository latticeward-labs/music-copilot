# HF Frame Review — WF-08_VOICE_PROCESSING

**Issue/phase:** #17 / 5 · **Source:** WF-08_VOICE_PROCESSING · **Status:** Reviewable generated evidence (not separate human sign-off)
**Image:** HF_FRAME_WF-08_VOICE_PROCESSING.png · **SHA-256:** 24d80db6ed96414a686d8f90b876732e2aa1de370f4c3879b052f40a1862d0b7
**Priority:** P0 · **Ownership:** App + Voice Coordinator
**Canonical State IDs:** CD-05 · **UF:** UF-08 / UF-09 / UF-10 · **GS:** GS-04 recovery path
**Approved source:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_PROCESSING.md and https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_PROCESSING.svg
**Structural deviations:** None: exact source SVG hierarchy retained; approved Night Signal tokens applied.

## Exact rendered app copy

- "Processing"
- "Mapping one of six intents"
- "Cancel"
- "Manual / Exit remains reachable"

## Behavioral boundary

- **Entry:** STT result arrives and capture ends.
- **Primary action:** Cancel/manual fallback.
- **Exit:** Certain → command pending/executed; ambiguous → Confirming; failure → Failed/Retry.
- **Back:** Back cancels stale callbacks and returns immediately.
- **Truth:** Restricted mapper supports exactly six intents; service not yet mutated.
- **Recovery:** Unknown/unsupported/not-found becomes safe failure, never approximation.
- **A11y / TalkBack:** Processing status → Cancel → underlying Exit/manual controls.
- **System handoff:** Provider processing boundary follows approved disclosure/consent.
- **Minimum target:** ≥48dp; Driving previous/pause/next/seek-back ≥64dp, Mic ≥72dp, Exit ≥56dp.
- **At 200% font scaling:** Compose implementation must reflow; static screenshot does not prove runtime layout.
- **Privacy:** no raw audio, transcript, search query or local music metadata in analytics; sample names are synthetic.
- **Ownership:** service-owned playback; Voice coordinator-owned Voice session; platform surfaces excluded from generated app screenshots.
- **P2:** Liked Songs remains removable; no P0 build dependency.

## Evidence limitation

Deterministic restyling of the *approved* low-fidelity SVG into Night Signal produces this reviewable static visual. It is not an independently signed design approval, a Compose implementation, nor proof of precise physical tap-target or 200% reflow behavior.
