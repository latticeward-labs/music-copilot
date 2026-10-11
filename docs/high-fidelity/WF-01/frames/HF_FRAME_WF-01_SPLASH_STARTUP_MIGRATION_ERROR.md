# HF Frame Review — WF-01_SPLASH_STARTUP_MIGRATION_ERROR

**Issue/phase:** #17 / 5 · **Source:** WF-01_SPLASH_STARTUP_MIGRATION_ERROR · **Status:** Reviewable generated evidence (not separate human sign-off)
**Image:** HF_FRAME_WF-01_SPLASH_STARTUP_MIGRATION_ERROR.png · **SHA-256:** 0ebe6f274b70ffe960a75efd69283f1369830e5e8f6139d3eaeec6665eb3434a
**Priority:** P0 · **Ownership:** App
**Canonical State IDs:** S-01 · **UF:** UF-01 · **GS:** GS-01
**Approved source:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-01_SPLASH_STARTUP_MIGRATION_ERROR.md and https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-01_SPLASH_STARTUP_MIGRATION_ERROR.svg
**Structural deviations:** None: exact source SVG hierarchy retained; approved Night Signal tokens applied.

## Exact rendered app copy

- "Startup problem"
- "Your verified local data is retained."
- "Retry"
- "No destructive reset"

## Behavioral boundary

- **Entry:** Typed startup or local migration failure.
- **Primary action:** Retry startup once the user explicitly chooses Retry.
- **Exit:** Successful Retry routes from freshly verified state.
- **Back:** System Back exits; Retry never traps focus.
- **Truth:** Local migration result; no fabricated Empty/Ready state.
- **Recovery:** Retry; repeated failure remains concise and non-destructive.
- **A11y / TalkBack:** Error heading → explanation → Retry.
- **System handoff:** No Android Settings handoff is implied.
- **Minimum target:** ≥48dp; Driving previous/pause/next/seek-back ≥64dp, Mic ≥72dp, Exit ≥56dp.
- **At 200% font scaling:** Compose implementation must reflow; static screenshot does not prove runtime layout.
- **Privacy:** no raw audio, transcript, search query or local music metadata in analytics; sample names are synthetic.
- **Ownership:** service-owned playback; Voice coordinator-owned Voice session; platform surfaces excluded from generated app screenshots.
- **P2:** Liked Songs remains removable; no P0 build dependency.

## Evidence limitation

Deterministic restyling of the *approved* low-fidelity SVG into Night Signal produces this reviewable static visual. It is not an independently signed design approval, a Compose implementation, nor proof of precise physical tap-target or 200% reflow behavior.
