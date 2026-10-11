# HF Frame Review — WF-08_VOICE_FAILED_RETRY

**Issue/phase:** #17 / 5 · **Source:** WF-08_VOICE_FAILED_RETRY · **Status:** Reviewable generated evidence (not separate human sign-off)
**Image:** HF_FRAME_WF-08_VOICE_FAILED_RETRY.png · **SHA-256:** 6dc662de5ecc535e98279c530e57aa1f0ba2b8d11b7882660236075d8c743294
**Priority:** P0 · **Ownership:** App + Voice Coordinator
**Canonical State IDs:** CD-08, CD-17, CD-18, CD-19 · **UF:** UF-08 / UF-09 / UF-10 · **GS:** GS-04 recovery path
**Approved source:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_FAILED_RETRY.md and https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_FAILED_RETRY.svg
**Structural deviations:** None: exact source SVG hierarchy retained; approved Night Signal tokens applied.

## Exact rendered app copy

- "DRIVING / PLAYER CONTEXT"
- "Voice failed"
- "One Retry, then Manual"
- "!"
- "Manual controls"
- "Retry once"
- "Manual / Exit remains reachable"

## Behavioral boundary

- **Entry:** Timeout/no-match/provider/language/network/audio or execution rejection.
- **Primary action:** Retry once; Manual/Cancel remains available.
- **Exit:** Retry → second Listening; dismiss/manual → initiating surface; second failure is terminal.
- **Back:** Back returns immediately and exhausts no hidden action.
- **Truth:** Typed Voice/session failure; actual playback remains service-owned.
- **Recovery:** Exactly one explicit Retry; no auto-capture, coercive countdown or third attempt.
- **A11y / TalkBack:** Failed heading → short reason → Retry once → Manual → Cancel/Exit.
- **System handoff:** Settings recovery is available only outside active Driving Mode.
- **Minimum target:** ≥48dp; Driving previous/pause/next/seek-back ≥64dp, Mic ≥72dp, Exit ≥56dp.
- **At 200% font scaling:** Compose implementation must reflow; static screenshot does not prove runtime layout.
- **Privacy:** no raw audio, transcript, search query or local music metadata in analytics; sample names are synthetic.
- **Ownership:** service-owned playback; Voice coordinator-owned Voice session; platform surfaces excluded from generated app screenshots.
- **P2:** Liked Songs remains removable; no P0 build dependency.

## Evidence limitation

Deterministic restyling of the *approved* low-fidelity SVG into Night Signal produces this reviewable static visual. It is not an independently signed design approval, a Compose implementation, nor proof of precise physical tap-target or 200% reflow behavior.
