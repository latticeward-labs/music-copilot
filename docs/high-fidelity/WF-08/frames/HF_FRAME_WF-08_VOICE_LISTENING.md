# HF Frame Review — WF-08_VOICE_LISTENING

**Issue/phase:** #17 / 5 · **Source:** WF-08_VOICE_LISTENING · **Status:** Reviewable generated evidence (not separate human sign-off)
**Image:** HF_FRAME_WF-08_VOICE_LISTENING.png · **SHA-256:** 2eb761ad69706522de07b3bc1974a5868af1a055efbd584dd34c3a10e7eac2b2
**Priority:** P0 · **Ownership:** App + Voice Coordinator
**Canonical State IDs:** CD-03, CC-14 · **UF:** UF-08 / UF-09 / UF-10 · **GS:** GS-04 recovery path
**Approved source:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_LISTENING.md and https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_LISTENING.svg
**Structural deviations:** None: exact source SVG hierarchy retained; approved Night Signal tokens applied.

## Exact rendered app copy

- "Listening"
- "Six-second attempt"
- "MIC"
- "Cancel"
- "Manual / Exit remains reachable"

## Behavioral boundary

- **Entry:** Explicit microphone tap after permission/provider capability passes.
- **Primary action:** Cancel.
- **Exit:** Result → Processing; failure → Failed/Retry; Cancel → initiating surface.
- **Back:** Back equals Cancel and releases capture/duck.
- **Truth:** Voice coordinator transient session; playback remains service-owned.
- **Recovery:** Timeout/no-match/provider failure preserves playback and exposes one Retry.
- **A11y / TalkBack:** Listening status → time limit → Cancel → underlying manual/Exit controls.
- **System handoff:** Android mic indicator/dialog is system-owned; capture never starts on mode entry.
- **Minimum target:** ≥48dp; Driving previous/pause/next/seek-back ≥64dp, Mic ≥72dp, Exit ≥56dp.
- **At 200% font scaling:** Compose implementation must reflow; static screenshot does not prove runtime layout.
- **Privacy:** no raw audio, transcript, search query or local music metadata in analytics; sample names are synthetic.
- **Ownership:** service-owned playback; Voice coordinator-owned Voice session; platform surfaces excluded from generated app screenshots.
- **P2:** Liked Songs remains removable; no P0 build dependency.

## Evidence limitation

Deterministic restyling of the *approved* low-fidelity SVG into Night Signal produces this reviewable static visual. It is not an independently signed design approval, a Compose implementation, nor proof of precise physical tap-target or 200% reflow behavior.
