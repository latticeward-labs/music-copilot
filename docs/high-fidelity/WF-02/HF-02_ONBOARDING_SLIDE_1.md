# HF-02_ONBOARDING_SLIDE_1 — Onboarding — Slide 1 — Local music

- **Issue:** #17
- **Phase:** 4 — Primary High-Fidelity
- **Status:** **APPROVED_BASE_HIFI / PASS COMPLETE**
- **Screen family:** `WF-02`
- **Source Wireframe:** `WF-02_ONBOARDING_SLIDE_1`
- **Image:** `HF-02_ONBOARDING_SLIDE_1.png`
- **Priority:** **P0**
- **Ownership:** **App**
- **Reference viewport:** **360×800dp**
- **Rendered PNG:** **1080×2400px (@3×)**
- **Source package SHA-256:** `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`
- **Phase-3 main baseline:** `b6113b0d094e0c9f5fd55842a80a90528cdc4e4f`
- **Structural deviation from approved Wireframe:** **NONE**

## Traceability

| Field | Value |
|---|---|
| User Flow | UF-01 |
| Canonical State IDs | S-02 |
| Golden Scenario | GS-01 |
| Source Wireframe | `docs/wireframes/app-states/WF-02_ONBOARDING_SLIDE_1.md` |
| Source low-fidelity visual | `docs/wireframes/app-states/WF-02_ONBOARDING_SLIDE_1.svg` |
| Design Foundations | `docs/design-system/foundations/FOUNDATIONS.md` + `TOKENS.json` |
| Component System | `docs/design-system/components/COMPONENT_CATALOG.md` + `COMPONENT_TOKENS.json` |

## High-fidelity hierarchy

1. Back affordance + Welcome app bar
2. Local-music illustration area
3. Primary educational heading
4. Supporting privacy/locality sentence
5. Two-page position indicator
6. Primary Continue action

The high-fidelity artifact preserves the approved source hierarchy. It does not add a destination, product capability, navigation branch, playback owner, Voice owner, system surface, or P0/P1/P2 scope.

## Exact UI copy

The image is approved only if these strings appear exactly where applicable. Android/system status-bar time and platform icons are review chrome and are not app-owned UI copy.

1. `Welcome`
2. `LOCAL MUSIC`
3. `Play local music. No account needed.`
4. `Your library stays on this device.`
5. `Continue`

**Local rendered-copy validation:** **PASS** — the deterministic render uses the strings above directly rather than image-model text generation.

## Reusable component composition

- `CMP-03` — AppBar
- `CMP-22` — PagerIndicator
- `CMP-01` — ActionButton

## Behavior

- **Primary action:** Continue to Slide 2.
- **Entry:** First run or explicit replay.
- **Exit:** Slide 2 / Skip Voice education.
- **Back:** Back exits or returns according to the owning first-run route without mutating completion.
- **Recovery:** Interrupted onboarding resumes the actual pending step.
- **Visible source of truth:** Static approved product education.

## Accessibility

- TalkBack: Heading → explanation → page position → Continue; Back is labeled.
- Global app interaction target minimum remains **48dp**.
- 200% text scaling/reflow remains a required implementation proof; this static 360×800dp image does not replace runtime Compose verification.
- Focus, selected, disabled, error and pending meaning must remain non-color-only.

## Privacy / analytics

- Copy states local-device behavior without account/cloud claims beyond the approved product contract.
- Raw Voice audio is not persisted by default.
- Personal media titles/artists, search queries/results and Voice candidate labels are prohibited from repository analytics/evidence payloads.
- The sample local-media names visible in this review artifact are non-user test data.

## System boundary

Android-owned permission dialogs, Android Settings, notification/lock-screen/external controls and platform status chrome remain system-owned. A visual annotation or simulated status bar in this review image does not create an app-owned system screen.

## Review gate

Before Phase 4 is recorded as PASS / COMPLETE:

- [x] PNG visibly renders in GitHub.
- [x] PNG hierarchy matches `WF-02_ONBOARDING_SLIDE_1`.
- [x] Exact UI copy matches this file.
- [x] No new navigation, control, capability or destination was introduced.
- [x] Component usage remains compatible with Phase 3.
- [x] Accessibility/privacy/system-boundary notes remain intact.

