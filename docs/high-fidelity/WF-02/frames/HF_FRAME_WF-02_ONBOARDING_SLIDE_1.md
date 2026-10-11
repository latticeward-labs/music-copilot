# HF Frame Review — WF-02_ONBOARDING_SLIDE_1

**Issue/phase:** #17 / 5 · **Source:** WF-02_ONBOARDING_SLIDE_1 · **Status:** Reviewable generated evidence (not separate human sign-off)
**Image:** HF_FRAME_WF-02_ONBOARDING_SLIDE_1.png · **SHA-256:** 3db57751e1c7f55e54c4caa804181c86a7859b7756612f9650c6fa901175c022
**Priority:** P0 · **Ownership:** App
**Canonical State IDs:** S-02 · **UF:** UF-01 · **GS:** GS-01
**Approved source:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-02_ONBOARDING_SLIDE_1.md and https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-02_ONBOARDING_SLIDE_1.svg
**Structural deviations:** None: exact source SVG hierarchy retained; approved Night Signal tokens applied.

## Exact rendered app copy

- "Back"
- "Welcome"
- "LOCAL MUSIC"
- "Device music placeholder"
- "Play local music. No account needed."
- "Your library stays on this device."
- "●  ○"
- "Continue"

## Behavioral boundary

- **Entry:** First run or explicit replay.
- **Primary action:** Continue to Slide 2.
- **Exit:** Slide 2 / Skip Voice education.
- **Back:** Back exits or returns according to the owning first-run route without mutating completion.
- **Truth:** Static approved product education.
- **Recovery:** Interrupted onboarding resumes the actual pending step.
- **A11y / TalkBack:** Heading → explanation → page position → Continue.
- **System handoff:** No permission is requested on this slide.
- **Minimum target:** ≥48dp; Driving previous/pause/next/seek-back ≥64dp, Mic ≥72dp, Exit ≥56dp.
- **At 200% font scaling:** Compose implementation must reflow; static screenshot does not prove runtime layout.
- **Privacy:** no raw audio, transcript, search query or local music metadata in analytics; sample names are synthetic.
- **Ownership:** service-owned playback; Voice coordinator-owned Voice session; platform surfaces excluded from generated app screenshots.
- **P2:** Liked Songs remains removable; no P0 build dependency.

## Evidence limitation

Deterministic restyling of the *approved* low-fidelity SVG into Night Signal produces this reviewable static visual. It is not an independently signed design approval, a Compose implementation, nor proof of precise physical tap-target or 200% reflow behavior.
