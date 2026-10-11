# HF Frame Review — WF-05_SEARCH_ERROR

**Issue/phase:** #17 / 5 · **Source:** WF-05_SEARCH_ERROR · **Status:** Reviewable generated evidence (not separate human sign-off)
**Image:** HF_FRAME_WF-05_SEARCH_ERROR.png · **SHA-256:** 2730fbdf47478dedced29f158960e61ed3b682d74e804308897d69feac263fb1
**Priority:** P0 · **Ownership:** App
**Canonical State IDs:** CB-09, CB-12 · **UF:** UF-04 · **GS:** GS-02
**Approved source:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-05_SEARCH_ERROR.md and https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-05_SEARCH_ERROR.svg
**Structural deviations:** None: exact source SVG hierarchy retained; approved Night Signal tokens applied.

## Exact rendered app copy

- "Back"
- "Search"
- "aurora"
- "Clear"
- "!  Search failed"
- "The verified library is still available."
- "Retry"
- "Home"
- "Liked"
- "Settings"

## Behavioral boundary

- **Entry:** Typed local query/index failure.
- **Primary action:** Retry; Edit and Clear remain available.
- **Exit:** Retry → Results/No Result; Back → verified Library.
- **Back:** Returns to Library; unrelated playback is unchanged.
- **Truth:** Local search repository failure; verified library remains authoritative.
- **Recovery:** Retry/edit/clear; use UF-03 only if index truth is actually stale.
- **A11y / TalkBack:** Error heading → concise reason → Retry → Edit/Clear → Back.
- **System handoff:** No network fallback or Android surface.
- **Minimum target:** ≥48dp; Driving previous/pause/next/seek-back ≥64dp, Mic ≥72dp, Exit ≥56dp.
- **At 200% font scaling:** Compose implementation must reflow; static screenshot does not prove runtime layout.
- **Privacy:** no raw audio, transcript, search query or local music metadata in analytics; sample names are synthetic.
- **Ownership:** service-owned playback; Voice coordinator-owned Voice session; platform surfaces excluded from generated app screenshots.
- **P2:** Liked Songs remains removable; no P0 build dependency.

## Evidence limitation

Deterministic restyling of the *approved* low-fidelity SVG into Night Signal produces this reviewable static visual. It is not an independently signed design approval, a Compose implementation, nor proof of precise physical tap-target or 200% reflow behavior.
