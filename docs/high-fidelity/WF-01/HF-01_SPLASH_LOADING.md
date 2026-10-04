# HF-01_SPLASH_LOADING — Splash — Loading

- **Issue:** #17
- **Phase:** 4 — Primary High-Fidelity
- **Status:** **APPROVED_BASE_HIFI / PASS COMPLETE**
- **Screen family:** `WF-01`
- **Source Wireframe:** `WF-01_SPLASH_LOADING`
- **Image:** `HF-01_SPLASH_LOADING.png`
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
| Canonical State IDs | S-01 |
| Golden Scenario | GS-01 |
| Source Wireframe | `docs/wireframes/app-states/WF-01_SPLASH_LOADING.md` |
| Source low-fidelity visual | `docs/wireframes/app-states/WF-01_SPLASH_LOADING.svg` |
| Design Foundations | `docs/design-system/foundations/FOUNDATIONS.md` + `TOKENS.json` |
| Component System | `docs/design-system/components/COMPONENT_CATALOG.md` + `COMPONENT_TOKENS.json` |

## High-fidelity hierarchy

1. Android/system status chrome (system-owned review simulation)
2. Centered Music Copilot brand mark
3. Written Loading status
4. Android/system navigation chrome (system-owned review simulation)

The high-fidelity artifact preserves the approved source hierarchy. It does not add a destination, product capability, navigation branch, playback owner, Voice owner, system surface, or P0/P1/P2 scope.

## Exact UI copy

The image is approved only if these strings appear exactly where applicable. Android/system status-bar time and platform icons are review chrome and are not app-owned UI copy.

1. `MUSIC COPILOT`
2. `Loading local state…`

**Local rendered-copy validation:** **PASS** — the deterministic render uses the strings above directly rather than image-model text generation.

## Reusable component composition

- `CMP-15` — StatusBlock — Loading

## Behavior

- **Primary action:** Wait; no repeated action while routing is active.
- **Entry:** Cold or warm app start.
- **Exit:** Verified pending onboarding, permission recovery, or Home/Library state.
- **Back:** System Back exits the launch task; no in-app Back control.
- **Recovery:** A bounded startup failure routes to the approved migration-error frame with Retry.
- **Visible source of truth:** Verified local migration/readiness and platform state.

## Accessibility

- TalkBack: App name → Loading status. No app interaction target is required on the primary state.
- Global app interaction target minimum remains **48dp**.
- 200% text scaling/reflow remains a required implementation proof; this static 360×800dp image does not replace runtime Compose verification.
- Focus, selected, disabled, error and pending meaning must remain non-color-only.

## Privacy / analytics

- No personal media metadata or raw Voice content is shown or exported.
- Raw Voice audio is not persisted by default.
- Personal media titles/artists, search queries/results and Voice candidate labels are prohibited from repository analytics/evidence payloads.
- The sample local-media names visible in this review artifact are non-user test data.

## System boundary

Android-owned permission dialogs, Android Settings, notification/lock-screen/external controls and platform status chrome remain system-owned. A visual annotation or simulated status bar in this review image does not create an app-owned system screen.

## Review gate

Before Phase 4 is recorded as PASS / COMPLETE:

- [x] PNG visibly renders in GitHub.
- [x] PNG hierarchy matches `WF-01_SPLASH_LOADING`.
- [x] Exact UI copy matches this file.
- [x] No new navigation, control, capability or destination was introduced.
- [x] Component usage remains compatible with Phase 3.
- [x] Accessibility/privacy/system-boundary notes remain intact.

