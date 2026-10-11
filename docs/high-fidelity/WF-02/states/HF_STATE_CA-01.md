# High-Fidelity State — CA-01 · Six-Command Education - View, Skip, Replay, Practice Choice

**Issue:** #17 · **Phase:** 5 · **Disposition:** DEDICATED_HIFI / REVIEWABLE
**PNG:** HF_STATE_CA-01.png · **PNG SHA-256:** 9f30d1cfc46d716e9122d094eef19734f0929ef929337d507cec5dbe9439bc8b
**Priority:** P0 · **WF:** WF-02
**UF:** UF-01 · **GS:** GS-01
**State:** CA-01 · **Owner:** App
**Screen owner:** UF-01 · Onboarding/Education · **Recovery owner:** UF-01
**Source selected frame:** WF-02_ONBOARDING_SLIDE_2_SKIP_VOICE
**Source Markdown/SVG:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-02_ONBOARDING_SLIDE_2_SKIP_VOICE.md / https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-02_ONBOARDING_SLIDE_2_SKIP_VOICE.svg

## Exact UI copy (device surface only)

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

## State and structure contract

- **Source approved hierarchy:** WF-02_ONBOARDING_SLIDE_2_SKIP_VOICE; existing WF family only; no new app destination.
- **Visual-state specialization:** Skip or provider decline returns to manual local control.
- **Component mapping:** CMP-01, CMP-22. All component definitions are in docs/design-system/components/COMPONENT_TOKENS.json.
- **Structural delta:** None. Source-aligned copy specialization only (where explicitly noted), not new controls or navigation.
- **Entry:** Continue from Slide 1 or resume pending education.
- **Primary action:** Finish; optional Practice Voice is a separate explicit action.
- **Exit:** Permission/library readiness path; Skip/Finish requests no microphone.
- **Back:** Back returns to Slide 1 without changing permission or playback.
- **Recovery:** Skip or provider decline returns to manual local control.
- **Actual state owner / truth:** Exactly six supported intents; Voice provider/permission remains unrequested until explicit practice.; App.
- **Preserve/restoration:** Education completion only; permission/provider state is re-read separately.
- **System-only constraints:** Microphone dialog may occur only after explicit Practice Voice.

## Related source frames

- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-02_ONBOARDING_SLIDE_1.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-02_ONBOARDING_SLIDE_2_SKIP_VOICE.md

## Accessibility / privacy / error containment

- **Reading order:** Heading → six-command summary → page position → Practice Voice → Finish without Voice.
- **Semantics:** written state and a non-color cue; observable 48dp minimum, or larger Driving overrides.
- **200% scaling:** reflow required in Compose implementation, not claimed by this static artifact.
- **Voice:** no auto-listening; 6-second bounded capture, max one retry, at most two candidates, one short error/TTS sentence.
- **Driving:** one fixed layer; immediate Exit, no typing/long lists/nested navigation/automatic entry.
- **Analytics:** allowed aggregate success/latency classifications only; prohibit raw audio, full transcript, personal media titles/artists, precise position, queries and candidate labels.
- **P2 isolation:** if P2/Like removed, P0 and P1 behavior remains unaffected.

## Review status

Generated from approved source SVG using deterministic token substitutions; PNG has been decoded and checksum recorded. Design review is **AI-assisted candidate review only**. No human signatures are inferred or fabricated. State-specific behaviors share the approved canonical owning WF; they are not unapproved independent routes.
