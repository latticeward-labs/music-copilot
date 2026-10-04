# HF-03_PERMISSIONS_RATIONALE_REQUEST — Permissions — Rationale / Request

- **Issue:** #17
- **Phase:** 4 — Primary High-Fidelity
- **Status:** **APPROVED_BASE_HIFI / PASS COMPLETE**
- **Screen family:** `WF-03`
- **Source Wireframe:** `WF-03_PERMISSIONS_RATIONALE_REQUEST`
- **Image:** `HF-03_PERMISSIONS_RATIONALE_REQUEST.png`
- **Priority:** **P0**
- **Ownership:** **Hybrid**
- **Reference viewport:** **360×800dp**
- **Rendered PNG:** **1080×2400px (@3×)**
- **Source package SHA-256:** `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`
- **Phase-3 main baseline:** `b6113b0d094e0c9f5fd55842a80a90528cdc4e4f`
- **Structural deviation from approved Wireframe:** **NONE**

## Traceability

| Field | Value |
|---|---|
| User Flow | UF-02, UF-10, UF-12 |
| Canonical State IDs | S-03, CA-02, SYS-01, SYS-02 |
| Golden Scenario | GS-01 |
| Source Wireframe | `docs/wireframes/app-states/WF-03_PERMISSIONS_RATIONALE_REQUEST.md` |
| Source low-fidelity visual | `docs/wireframes/app-states/WF-03_PERMISSIONS_RATIONALE_REQUEST.svg` |
| Design Foundations | `docs/design-system/foundations/FOUNDATIONS.md` + `TOKENS.json` |
| Component System | `docs/design-system/components/COMPONENT_CATALOG.md` + `COMPONENT_TOKENS.json` |

## High-fidelity hierarchy

1. Back affordance + Permissions app bar
2. App-owned permission education card
3. Capability title and purpose
4. Primary Continue action
5. Secondary Not now action
6. Explicit Android/system handoff annotation

The high-fidelity artifact preserves the approved source hierarchy. It does not add a destination, product capability, navigation branch, playback owner, Voice owner, system surface, or P0/P1/P2 scope.

## Exact UI copy

The image is approved only if these strings appear exactly where applicable. Android/system status-bar time and platform icons are review chrome and are not app-owned UI copy.

1. `Permissions`
2. `Media access`
3. `Index and play local music.`
4. `Continue`
5. `Not now`
6. `[ANDROID / SYSTEM] handoff`
7. `Fresh result is authoritative`

**Local rendered-copy validation:** **PASS** — the deterministic render uses the strings above directly rather than image-model text generation.

## Reusable component composition

- `CMP-03` — AppBar
- `CMP-14` — PermissionEducationCard
- `CMP-01` — ActionButton
- `CMP-21` — SystemBoundaryAnnotation

## Behavior

- **Primary action:** Continue to the Android-owned permission dialog.
- **Entry:** Media access is first required, or mic is explicitly tapped/practiced.
- **Exit:** Android dialog handoff; return routes to Granted/Denied.
- **Back:** Back/Not now returns to a usable manual surface.
- **Recovery:** Not now preserves manual controls; no coercive loop.
- **Visible source of truth:** Android permission state is authoritative.

## Accessibility

- TalkBack: Permission heading → purpose → Continue → Not now → system-boundary note.
- Global app interaction target minimum remains **48dp**.
- 200% text scaling/reflow remains a required implementation proof; this static 360×800dp image does not replace runtime Compose verification.
- Focus, selected, disabled, error and pending meaning must remain non-color-only.

## Privacy / analytics

- No Android permission result is fabricated or persisted as app truth.
- Raw Voice audio is not persisted by default.
- Personal media titles/artists, search queries/results and Voice candidate labels are prohibited from repository analytics/evidence payloads.
- The sample local-media names visible in this review artifact are non-user test data.

## System boundary

Android-owned permission dialogs, Android Settings, notification/lock-screen/external controls and platform status chrome remain system-owned. A visual annotation or simulated status bar in this review image does not create an app-owned system screen.

## Review gate

Before Phase 4 is recorded as PASS / COMPLETE:

- [x] PNG visibly renders in GitHub.
- [x] PNG hierarchy matches `WF-03_PERMISSIONS_RATIONALE_REQUEST`.
- [x] Exact UI copy matches this file.
- [x] No new navigation, control, capability or destination was introduced.
- [x] Component usage remains compatible with Phase 3.
- [x] Accessibility/privacy/system-boundary notes remain intact.

