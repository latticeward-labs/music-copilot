# Issue #17 · Phase 8 — Repository-native clickable prototype

**Authority:** Music Copilot MVP v1.0 English PRD/MVP, Issue #15 low-fidelity WF baseline, Issue #16 final Software Architecture, Phase 4–7 High-Fidelity + Accessibility, accepted GS-01..GS-05. **Attached source SHA-256:** `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`.

## Open the prototype

Clone/download the *repository*, preserving `docs/high-fidelity/` and `docs/prototype/` together. Open `docs/prototype/index.html` in a browser. No Figma account, backend, database, paid API, credentials or network access is required.

If your browser restricts local files, from the repository root:

    python3 -m http.server 8765

Then visit:

    http://localhost:8765/docs/prototype/index.html

GitHub's `blob/main/docs/prototype/index.html` link is **source view**, not a running hosted demo. GitHub Pages is not presumed configured, and no deployment is claimed.

## How to review

1. Select one of five GS scenario cards in the left rail. The middle phone shows the existing repository's High-Fidelity PNG, not a freshly invented screen.
2. Click the original on-screen control (transparent hotspot). Use **Show tap regions** to inspect exactly where its hotspot is.
3. The right rail repeats accessible button labels for review convenience. Those buttons are part of the **review shell**, not additional controls in the Android product.
4. For asynchronous MediaSession events, synthetic STT outcomes, MediaStore outcomes and native Android events, use the explicitly separated **Reviewer-only event simulator**. These do **not** appear inside the phone UI and are not evidence of real background playback, STT or OS integrations.
5. A System-only handoff shows only a **no-app-screen** boundary marker. Android owns the permission/Settings/notification/lock-screen/audio-route media surface, and all external outcomes are chosen in the review rail.
6. The bottom **Reviewer Back** and **Restart scenario** controls are review-shell functionality, not new Music Copilot navigation or behavior. Inside the phone, source-defined Back, Exit and Cancel controls are used.

## Scenario coverage

| Golden Scenario | Approved primary path | Main recoveries |
|---|---|---|
| GS-01 First launch / Library | Splash → Onboarding → Media purpose → Android media grant → Scanning → Ready Library | migration error, requestable/permanent denial, OS Settings return/recheck, verified Empty, recoverable scan error |
| GS-02 Find music / Start playback | Ready Library → local Search → results → source collection/selection validation → service-acknowledged Full Player | No Result, Search Error, stale/revoked selection, rejected queue, corrupt/inaccessible item, controller reconnect |
| GS-03 Playback outside app | Full Player → background (system) → native MediaSession controller → pending system command → app reconnect → verified Full Player | notification denied/unavailable, Settings/visibility, lost lifecycle with optional Paused Restore, no valid restore |
| GS-04 Driving / Voice NEXT | Full Player → explicit Driving entry → Mic → Listening → Processing → one command pending → Executed → same Driving layer | Paused/No Track, valid last item, route safe-paused, 1/2 immutable candidate confirmation, unknown/not-found/unsupported, 6s STT timeout + one Retry, terminal Manual, provider/mic denied, post-ack TTS failure |
| GS-05 Interruption / Safe recovery | Full Player → Android route event → disconnected Safe Paused → platform route reconnect → app Safe Paused → explicit user Play → service-acknowledged Full Player | four-guard transient focus resume, guard failure, permanent focus loss, unsafe route still paused |

Detailed source mapping: [PROTOTYPE_COVERAGE_MATRIX.md](PROTOTYPE_COVERAGE_MATRIX.md). Runtime/QA handoff: [TEST_PLAN.md](TEST_PLAN.md).

## Runtime ownership and scope

- The prototype runs static JavaScript with an explicit graph of approved source state IDs and image paths. It has **no** actual player, filesystem scanning, microphone capture, STT/TTS, MediaSession, Android permissions, cloud consent, location, Bluetooth APIs or telemetry. All music/artist data shown is synthetic demonstration copy inherited from the source PNG.
- No user data, transcript or real personal media title is stored or transmitted by this prototype; there is no localStorage, fetch or backend request.
- Drive is a single layer with immediate Exit. Voice is an overlay and every completion/cancel/failure returns to the same source-controlled layer. No nested menus, new navigation, Android Auto, wake-word, cloud streaming or inferred command execution.
- The image hotspots and action rail are **review artifacts**, not implementation-ready touch geometry. Actual Compose 200% font reflow, TalkBack semantics/focus, 48dp touch hitboxes, Media3 continuity and hardware route behavior remain downstream implementation QA (PH10-MIN-001 remains open).
- An existing PNG/spec remains controlling. Should the interactive demonstration differ from the accepted WF/UF/State/GS contract, the approved source contract wins.

## Validation and maintenance

Run from repository root:

    node docs/prototype/validate.cjs

This checks image source existence, 1080×2400 PNG signatures, canonical State IDs and ownership, 5 primary path connectivity, listed recovery reachability, minimum **prototype** hotspot rectangle dimensions, no app image for a platform-only state, one-Retry and no-third-try constraints, explicit manual Resume after route return, and no native/microphone/network code.

For optional real browser smoke (requires locally installed Playwright/Chromium):

    python3 -m http.server 8765
    node docs/prototype/browser-smoke.cjs

Results are recorded in [PHASE_8_VALIDATION.json](PHASE_8_VALIDATION.json), [PHASE_8_BROWSER_VALIDATION.json](PHASE_8_BROWSER_VALIDATION.json) and the synthetic-only [browser screenshot](PHASE_8_BROWSER_REVIEW.png) when that browser test runs successfully.

This prototype is **clickable design evidence**. Independent Product/Design/Android/Privacy and Accessibility approvals and tests on physical Android 10/12/14+ remain separate gates. Do not close Issue #17 purely based on this deliverable.
