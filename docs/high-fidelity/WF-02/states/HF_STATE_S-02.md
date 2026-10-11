# High-Fidelity State — S-02 · Onboarding - First Run, Incomplete Resume, Skip, Replay

**Issue:** #17 · **Phase:** 5 · **Disposition:** DEDICATED_HIFI / REVIEWABLE
**PNG:** HF_STATE_S-02.png · **PNG SHA-256:** 3db57751e1c7f55e54c4caa804181c86a7859b7756612f9650c6fa901175c022
**Priority:** P0 · **WF:** WF-02
**UF:** UF-01 · **GS:** GS-01
**State:** S-02 · **Owner:** App
**Screen owner:** UF-01 · Onboarding/Education · **Recovery owner:** UF-01
**Source selected frame:** WF-02_ONBOARDING_SLIDE_1
**Source Markdown/SVG:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-02_ONBOARDING_SLIDE_1.md / https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-02_ONBOARDING_SLIDE_1.svg

## Exact UI copy (device surface only)

- "Back"
- "Welcome"
- "LOCAL MUSIC"
- "Device music placeholder"
- "Play local music. No account needed."
- "Your library stays on this device."
- "●  ○"
- "Continue"

## State and structure contract

- **Source approved hierarchy:** WF-02_ONBOARDING_SLIDE_1; existing WF family only; no new app destination.
- **Visual-state specialization:** Interrupted onboarding resumes the actual pending step.
- **Component mapping:** CMP-01, CMP-22. All component definitions are in docs/design-system/components/COMPONENT_TOKENS.json.
- **Structural delta:** None. Source-aligned copy specialization only (where explicitly noted), not new controls or navigation.
- **Entry:** First run or explicit replay.
- **Primary action:** Continue to Slide 2.
- **Exit:** Slide 2 / Skip Voice education.
- **Back:** Back exits or returns according to the owning first-run route without mutating completion.
- **Recovery:** Interrupted onboarding resumes the actual pending step.
- **Actual state owner / truth:** Static approved product education.; App.
- **Preserve/restoration:** Completion is stored only after Continue/Skip reaches its committed step.
- **System-only constraints:** No permission is requested on this slide.

## Related source frames

- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-02_ONBOARDING_SLIDE_1.md
- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-02_ONBOARDING_SLIDE_2_SKIP_VOICE.md

## Accessibility / privacy / error containment

- **Reading order:** Heading → explanation → page position → Continue.
- **Semantics:** written state and a non-color cue; observable 48dp minimum, or larger Driving overrides.
- **200% scaling:** reflow required in Compose implementation, not claimed by this static artifact.
- **Voice:** no auto-listening; 6-second bounded capture, max one retry, at most two candidates, one short error/TTS sentence.
- **Driving:** one fixed layer; immediate Exit, no typing/long lists/nested navigation/automatic entry.
- **Analytics:** allowed aggregate success/latency classifications only; prohibit raw audio, full transcript, personal media titles/artists, precise position, queries and candidate labels.
- **P2 isolation:** if P2/Like removed, P0 and P1 behavior remains unaffected.

## Review status

Generated from approved source SVG using deterministic token substitutions; PNG has been decoded and checksum recorded. Design review is **AI-assisted candidate review only**. No human signatures are inferred or fabricated. State-specific behaviors share the approved canonical owning WF; they are not unapproved independent routes.
