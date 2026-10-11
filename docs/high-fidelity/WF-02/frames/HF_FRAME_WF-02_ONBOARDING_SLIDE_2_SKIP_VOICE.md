# HF Frame Review — WF-02_ONBOARDING_SLIDE_2_SKIP_VOICE

**Issue/phase:** #17 / 5 · **Source:** WF-02_ONBOARDING_SLIDE_2_SKIP_VOICE · **Status:** Reviewable generated evidence (not separate human sign-off)
**Image:** HF_FRAME_WF-02_ONBOARDING_SLIDE_2_SKIP_VOICE.png · **SHA-256:** 9f30d1cfc46d716e9122d094eef19734f0929ef929337d507cec5dbe9439bc8b
**Priority:** P0 · **Ownership:** App
**Canonical State IDs:** S-02, CA-01, CA-02 · **UF:** UF-01 · **GS:** GS-01
**Approved source:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-02_ONBOARDING_SLIDE_2_SKIP_VOICE.md and https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-02_ONBOARDING_SLIDE_2_SKIP_VOICE.svg
**Structural deviations:** None: exact source SVG hierarchy retained; approved Night Signal tokens applied.

## Exact rendered app copy

- "Back"
- "Welcome"
- "Skip"
- "OPTIONAL VOICE"
- "6 supported commands"
- "Voice starts only after your tap."
- "Skip keeps all manual controls."
- "●  ●"
- "Practice Voice"
- "Finish without Voice"

## Behavioral boundary

- **Entry:** Continue from Slide 1 or resume pending education.
- **Primary action:** Finish; optional Practice Voice is a separate explicit action.
- **Exit:** Permission/library readiness path; Skip/Finish requests no microphone.
- **Back:** Back returns to Slide 1 without changing permission or playback.
- **Truth:** Exactly six supported intents; Voice provider/permission remains unrequested until explicit practice.
- **Recovery:** Skip or provider decline returns to manual local control.
- **A11y / TalkBack:** Heading → six-command summary → page position → Practice Voice → Finish without Voice.
- **System handoff:** Microphone dialog may occur only after explicit Practice Voice.
- **Minimum target:** ≥48dp; Driving previous/pause/next/seek-back ≥64dp, Mic ≥72dp, Exit ≥56dp.
- **At 200% font scaling:** Compose implementation must reflow; static screenshot does not prove runtime layout.
- **Privacy:** no raw audio, transcript, search query or local music metadata in analytics; sample names are synthetic.
- **Ownership:** service-owned playback; Voice coordinator-owned Voice session; platform surfaces excluded from generated app screenshots.
- **P2:** Liked Songs remains removable; no P0 build dependency.

## Evidence limitation

Deterministic restyling of the *approved* low-fidelity SVG into Night Signal produces this reviewable static visual. It is not an independently signed design approval, a Compose implementation, nor proof of precise physical tap-target or 200% reflow behavior.
