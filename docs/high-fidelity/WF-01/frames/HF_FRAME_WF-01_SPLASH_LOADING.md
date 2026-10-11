# HF Frame Review — WF-01_SPLASH_LOADING

**Issue/phase:** #17 / 5 · **Source:** WF-01_SPLASH_LOADING · **Status:** Reviewable generated evidence (not separate human sign-off)
**Image:** HF_FRAME_WF-01_SPLASH_LOADING.png · **SHA-256:** d780d952042fd0a492dc9544c003ad118524c9591a0950037465a7fc2dd615e7
**Priority:** P0 · **Ownership:** App
**Canonical State IDs:** S-01 · **UF:** UF-01 · **GS:** GS-01
**Approved source:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-01_SPLASH_LOADING.md and https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-01_SPLASH_LOADING.svg
**Structural deviations:** None: exact source SVG hierarchy retained; approved Night Signal tokens applied.

## Exact rendered app copy

- "MUSIC COPILOT"
- "Loading local state…"

## Behavioral boundary

- **Entry:** Cold or warm app start.
- **Primary action:** Wait; no repeated action while routing is active.
- **Exit:** Verified pending onboarding, permission recovery, or Home/Library state.
- **Back:** System Back exits the launch task; no in-app Back control.
- **Truth:** Verified local migration/readiness and platform state.
- **Recovery:** A bounded failure changes to the migration-error frame with Retry.
- **A11y / TalkBack:** App name → Loading status.
- **System handoff:** System splash/status chrome remains Android-owned.
- **Minimum target:** ≥48dp; Driving previous/pause/next/seek-back ≥64dp, Mic ≥72dp, Exit ≥56dp.
- **At 200% font scaling:** Compose implementation must reflow; static screenshot does not prove runtime layout.
- **Privacy:** no raw audio, transcript, search query or local music metadata in analytics; sample names are synthetic.
- **Ownership:** service-owned playback; Voice coordinator-owned Voice session; platform surfaces excluded from generated app screenshots.
- **P2:** Liked Songs remains removable; no P0 build dependency.

## Evidence limitation

Deterministic restyling of the *approved* low-fidelity SVG into Night Signal produces this reviewable static visual. It is not an independently signed design approval, a Compose implementation, nor proof of precise physical tap-target or 200% reflow behavior.
