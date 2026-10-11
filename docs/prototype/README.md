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


## Strict acceptance re-audit and correction (11 October 2026)

The earlier PR #61 prototype passed 8 Chromium smoke cases but **was not sufficient for the user's stronger safe-endpoint requirement**. PR review found and corrected the following:
- **Real Rescan hotspot:** `Empty Library → Rescan` was below the button. The coordinate rectangle now overlaps the approved WF-04 Rescan control and is tested by clicking the actual image hotspot.
- **Permission manual branch:** the visible denied-state `Not now` button now has an actual hotspot. Permission denial → permanent denial → **real Android Settings handoff** → platform recheck → Empty → Rescan → verified Ready is browser-tested, without fabricating Android Settings UI.
- **Safe terminal proof, per GS:** the old global-graph reachability could pass via another Golden Scenario. The new `recoverySafety` policy and `validate.cjs` restrict traversal to each scenario's declared node set and prove **an explicit safe endpoint for each of all 38 branch entries**. No cross-scenario shortcut counts toward acceptance.
- **GS-03 source fidelity:** `CB-22` active-session handoff, `CB-23` background, `CB-24` screen-off, `CB-27` lock-screen media controller, `CB-28` external vehicle/headset key and `CB-25` OEM lifecycle check now remain genuine **system-only annotations**; there are no imitated Android screens.
- **P1 Paused Restore guard:** CB-32 Play is a genuine manual hotspot, retains Paused while awaiting the playback service acknowledgement, and goes to Playing only after an explicit verified ACK; rejected command returns Paused.
- **Exactly requested route file:** [routes.json](routes.json) is generated directly from [routes.js](routes.js) by [export-routes.cjs](export-routes.cjs). Validator asserts exact JSON/code parity.

**Latest validated:** GS 5/5, recoveries **38/38 safe terminals in own GS**, 67 source-locked review nodes (54 reused app-image contexts, 13 system-only annotations), 187 edges, 75 image hotspots, **14/14 headless Chromium clicked cases** with zero failures. Original 126-State baseline unaffected.

Evidence: [Updated machine report](PHASE_8_VALIDATION.json), [14-test Chromium result](PHASE_8_BROWSER_VALIDATION.json), [browser screenshot](PHASE_8_BROWSER_REVIEW.png), [strict audit rationale](PHASE_8_STRICT_REAUDIT_2026-10-11.md).

**Status:** PASS for the requested repository-native interactive prototype and evidence-backed safe-recovery gate. No Android hardware/Media3/STT/TTS/runtime accessibility/human reviewer approval is implied.

## Validation and maintenance

Run from repository root:

    node docs/prototype/export-routes.cjs
    node docs/prototype/validate.cjs

This checks image source existence, 1080×2400 PNG signatures, canonical State IDs and ownership, 5 primary path connectivity, listed recovery reachability, minimum **prototype** hotspot rectangle dimensions, no app image for a platform-only state, one-Retry and no-third-try constraints, explicit manual Resume after route return, and no native/microphone/network code.

For optional real browser smoke (requires locally installed Playwright/Chromium):

    python3 -m http.server 8765
    node docs/prototype/browser-smoke.cjs

Results are recorded in [PHASE_8_VALIDATION.json](PHASE_8_VALIDATION.json), [PHASE_8_BROWSER_VALIDATION.json](PHASE_8_BROWSER_VALIDATION.json) and the synthetic-only [browser screenshot](PHASE_8_BROWSER_REVIEW.png) when that browser test runs successfully.

This prototype is **clickable design evidence**. Independent Product/Design/Android/Privacy and Accessibility approvals and tests on physical Android 10/12/14+ remain separate gates. Do not close Issue #17 purely based on this deliverable.


## Phase 10 verification of this prototype

[Full traceability / scope audit](../audit/phase-10/README.md) independently verifies the 67 route nodes against the frozen 126-State authority, preserved HF/WF/UF/GS and 38 scenario-local safe recovery endpoints. All system-only handoffs have null in-app image and no fake Android pages; optional P2 Liked Songs is not inserted in the five mandatory GS. See [machine gate](../audit/phase-10/PHASE_10_VALIDATION.json). This does not represent an actual Android/STT/MediaSession runtime integration.

## Phase 11 reviewer Back versus actual Android Back clarification

The phone-shell **Reviewer Back** and Escape controls in this *HTML review tool* navigate the reviewer's visit history only; they are **not** Android Back correctness evidence. The binding native app rule is in [SCREEN_SPECS](../handoff/SCREEN_SPECS.md): active Voice cancels and releases mic first, then returns to the recorded origin; Driving Exit/Back is immediate. [Phase 11 six-role review](../design-system/review/ISSUE_17_CROSS_ROLE_REVIEW_2026-10-11.md) retains this as an explicit manual-review distinction (PH11-MIN-004), not a misleading pass from the browser simulator.
